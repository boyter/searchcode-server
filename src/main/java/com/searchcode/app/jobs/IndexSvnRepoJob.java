/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.jobs;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.CodeOwner;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.CodeSearcher;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchcodeLib;
import com.searchcode.app.util.UniqueRepoQueue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * This job is responsible for pulling and indexing svn repositories
 *
 * TODO add more tests as they are lacking
 * TODO use inheritance/template methods to combine the common stuff between this and git job then subclass
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class IndexSvnRepoJob extends IndexBaseRepoJob {

    private int SLEEPTIME = 5000;
    private boolean LOWMEMORY;
    private String SVNBINARYPATH;
    private boolean ENABLED = true;
    public int MAXFILELINEDEPTH = Helpers.tryParseInt(Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH);

    public IndexSvnRepoJob() {
        this.LOWMEMORY = true;
        this.SVNBINARYPATH = Properties.getProperties().getProperty(Values.SVNBINARYPATH, Values.DEFAULTSVNBINARYPATH);

        File f = new File(this.SVNBINARYPATH);
        if (!f.exists()) {
            this.ENABLED = false;
            Singleton.getLogger().severe("\n///////////////////////////////////////////////////////////////////////////\n// Property svn_binary_path in properties file appears to be incorrect.  //\n// will not be able to index any SVN repository until this is resolved.  //\n///////////////////////////////////////////////////////////////////////////");
        }
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (this.ENABLED == false) {
            return;
        }

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        while(CodeIndexer.shouldPauseAdding()) {
            Singleton.getLogger().info("Pausing parser.");
            return;
        }

        // Pull the next repo to index from the queue
        UniqueRepoQueue repoQueue = Singleton.getUniqueSvnRepoQueue();

        RepoResult repoResult = repoQueue.poll();
        AbstractMap<String, Integer> runningIndexRepoJobs = Singleton.getRunningIndexRepoJobs();

        if (repoResult != null && !runningIndexRepoJobs.containsKey(repoResult.getName())) {
            Singleton.getLogger().info("Indexing " + repoResult.getName());
            try {
                runningIndexRepoJobs.put(repoResult.getName(), (int) (System.currentTimeMillis() / 1000));

                JobDataMap data = context.getJobDetail().getJobDataMap();

                String repoName = repoResult.getName();
                String repoRemoteLocation = repoResult.getUrl();
                String repoUserName = repoResult.getUsername();
                String repoPassword = repoResult.getPassword();

                String repoLocations = data.get("REPOLOCATIONS").toString();
                this.LOWMEMORY = Boolean.parseBoolean(data.get("LOWMEMORY").toString());

                // Check if sucessfully cloned, and if not delete and restart
                boolean cloneSucess = checkCloneUpdateSucess(repoLocations + repoName);
                if (cloneSucess == false) {
                    // Delete the folder
                    try {
                        FileUtils.deleteDirectory(new File(repoLocations + repoName + "/"));
                        CodeIndexer.deleteByReponame(repoName);
                    } catch (IOException ex) {
                        Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + "\n with message: " + ex.getMessage());
                        return;
                    }
                }
                deleteCloneUpdateSuccess(repoLocations + repoName);

                String repoGitLocation = repoLocations + repoName + "/.svn/";

                File f = new File(repoGitLocation);
                boolean existingRepo = f.exists();
                boolean useCredentials = repoUserName != null && !repoUserName.isEmpty();
                RepositoryChanged repositoryChanged;

                if (existingRepo) {
                    repositoryChanged = this.updateSvnRepository(repoName, repoRemoteLocation, repoUserName, repoPassword, repoLocations, useCredentials);
                } else {
                    repositoryChanged = this.checkoutSvnRepository(repoName, repoRemoteLocation, repoUserName, repoPassword, repoLocations, useCredentials);
                }

                // Write file indicating we have sucessfully cloned
                createCloneUpdateSuccess(repoLocations + repoName);

                if (repositoryChanged.isChanged()) {
                    Singleton.getLogger().info("Update found indexing " + repoRemoteLocation);
                    this.updateIndex(repoName, repoLocations, repoRemoteLocation, existingRepo, repositoryChanged);
                }
            }
            finally {
                // Clean up the job
                runningIndexRepoJobs.remove(repoResult.getName());
            }
        }
    }

    public void updateIndex(String repoName, String repoLocations, String repoRemoteLocation, boolean existingRepo, RepositoryChanged repositoryChanged) {
        String repoSvnLocation = repoLocations + repoName;
        Path docDir = Paths.get(repoSvnLocation);

        // Was the previous index sucessful? if not then index by path
        boolean indexsucess = checkIndexSucess(repoSvnLocation);
        deleteIndexSuccess(repoSvnLocation);

        if (!repositoryChanged.isClone() && indexsucess == false) {
            Singleton.getLogger().info("Failed to index " + repoName + " fully, performing a full index.");
        }

        if (repositoryChanged.isClone() || indexsucess == false) {
            Singleton.getLogger().info("Doing full index of files for " + repoName);
            this.indexDocsByPath(docDir, repoName, repoLocations, repoRemoteLocation, existingRepo);
        }
        else {
            Singleton.getLogger().info("Doing delta index of files " + repoName);
            this.indexDocsByDelta(docDir, repoName, repoLocations, repoRemoteLocation, repositoryChanged);
        }

        // Write file indicating that the index was sucessful
        Singleton.getLogger().info("Sucessfully processed writing index success for " + repoName);
        createIndexSuccess(repoSvnLocation);
    }


    /**
     * Indexes all the documents in the repository changed file effectively performing a delta update
     * Should only be called when there is a genuine update IE something was indexed previously and
     * has has a new commit.
     */
    public void indexDocsByDelta(Path path, String repoName, String repoLocations, String repoRemoteLocation, RepositoryChanged repositoryChanged) {
        SearchcodeLib scl = Singleton.getSearchCodeLib(); // Should have data object by this point
        Queue<CodeIndexDocument> codeIndexDocumentQueue = Singleton.getCodeIndexQueue();
        String fileRepoLocations = FilenameUtils.separatorsToUnix(repoLocations);

        Singleton.getLogger().info("Repository Changed File List " + repositoryChanged.getChangedFiles());

        for(String changedFile: repositoryChanged.getChangedFiles()) {

            Singleton.getLogger().info("Indexing " + changedFile + " in " + repoName);

            while(CodeIndexer.shouldPauseAdding()) {
                Singleton.getLogger().info("Pausing parser.");
                try {
                    Thread.sleep(SLEEPTIME);
                } catch (InterruptedException ex) {}
            }

            String[] split = changedFile.split("/");
            String fileName = split[split.length - 1];
            changedFile = fileRepoLocations + repoName + "/" + changedFile;

            String md5Hash = Values.EMPTYSTRING;
            List<String> codeLines = null;

            try {
                codeLines = Helpers.readFileLines(changedFile, MAXFILELINEDEPTH);
            } catch (IOException ex) {
                Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
                break;
            }

            try {
                FileInputStream fis = new FileInputStream(new File(changedFile));
                md5Hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
                fis.close();
            } catch (IOException ex) {
                Singleton.getLogger().warning("Unable to generate MD5 for " + changedFile);
            }

            if(scl.isMinified(codeLines)) {
                Singleton.getLogger().info("Appears to be minified will not index  " + changedFile);
                break;
            }

            String languageName = scl.languageGuesser(changedFile, codeLines);
            String fileLocation = changedFile.replace(fileRepoLocations, Values.EMPTYSTRING).replace(fileName, Values.EMPTYSTRING);
            String fileLocationFilename = changedFile.replace(fileRepoLocations, Values.EMPTYSTRING);
            String repoLocationRepoNameLocationFilename = changedFile;


            String newString = getBlameFilePath(fileLocationFilename);
            String codeOwner = getInfoExternal(codeLines.size(), repoName, fileRepoLocations, newString).getName();


            if (codeLines != null) {
                if (this.LOWMEMORY) {
                    try {
                        CodeIndexer.indexDocument(new CodeIndexDocument(repoLocationRepoNameLocationFilename, repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
                    } catch (IOException ex) {
                        Singleton.incrementCodeIndexLinesCount(codeLines.size());
                        Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
                    }
                } else {
                    codeIndexDocumentQueue.add(new CodeIndexDocument(repoLocationRepoNameLocationFilename, repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
                }
            }
        }

        for(String deletedFile: repositoryChanged.getDeletedFiles()) {
            Singleton.getLogger().info("Missing from disk, removing from index " + deletedFile);
            try {
                CodeIndexer.deleteByFileLocationFilename(deletedFile);
            } catch (IOException ex) {
                Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
            }
        }
    }

    /**
     * Indexes all the documents in the path provided. Will also remove anything from the index if not on disk
     * Generally this is a slow update used only for the inital clone of a repository
     * NB this can be used for updates but it will be much slower as it needs to to walk the contents of the disk
     */
    public void indexDocsByPath(Path path, String repoName, String repoLocations, String repoRemoteLocation, boolean existingRepo) {
        SearchcodeLib scl = Singleton.getSearchCodeLib(); // Should have data object by this point
        List<String> fileLocations = new ArrayList<>();
        Queue<CodeIndexDocument> codeIndexDocumentQueue = Singleton.getCodeIndexQueue();

        // Convert once outside the main loop
        String fileRepoLocations = FilenameUtils.separatorsToUnix(repoLocations);
        boolean lowMemory = this.LOWMEMORY;

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    while(CodeIndexer.shouldPauseAdding()) {
                        Singleton.getLogger().info("Pausing parser.");
                        try {
                            Thread.sleep(SLEEPTIME);
                        } catch (InterruptedException ex) {}
                    }

                    // Convert Path file to unix style that way everything is easier to reason about
                    String fileParent = FilenameUtils.separatorsToUnix(file.getParent().toString());
                    String fileToString = FilenameUtils.separatorsToUnix(file.toString());
                    String fileName = file.getFileName().toString();
                    String md5Hash = Values.EMPTYSTRING;

                    if (fileParent.endsWith("/.svn") || fileParent.contains("/.svn/")) {
                        return FileVisitResult.CONTINUE;
                    }


                    List<String> codeLines;
                    try {
                        codeLines = Helpers.readFileLines(fileToString, MAXFILELINEDEPTH);
                    } catch (IOException ex) {
                        return FileVisitResult.CONTINUE;
                    }

                    try {
                        FileInputStream fis = new FileInputStream(new File(fileToString));
                        md5Hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
                        fis.close();
                    } catch (IOException ex) {
                        Singleton.getLogger().warning("Unable to generate MD5 for " + fileToString);
                    }

                    // is the file minified?
                    if(scl.isMinified(codeLines)) {
                        Singleton.getLogger().info("Appears to be minified will not index  " + fileToString);
                        return FileVisitResult.CONTINUE;
                    }

                    String languageName = scl.languageGuesser(fileName, codeLines);
                    String fileLocation = fileToString.replace(fileRepoLocations, Values.EMPTYSTRING).replace(fileName, Values.EMPTYSTRING);
                    String fileLocationFilename = fileToString.replace(fileRepoLocations, Values.EMPTYSTRING);
                    String repoLocationRepoNameLocationFilename = fileToString;


                    String newString = getBlameFilePath(fileLocationFilename);
                    String codeOwner = getInfoExternal(codeLines.size(), repoName, fileRepoLocations, newString).getName();

                    // If low memory don't add to the queue, just index it directly
                    if (lowMemory) {
                        CodeIndexer.indexDocument(new CodeIndexDocument(repoLocationRepoNameLocationFilename, repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
                    }
                    else {
                        Singleton.incrementCodeIndexLinesCount(codeLines.size());
                        codeIndexDocumentQueue.add(new CodeIndexDocument(repoLocationRepoNameLocationFilename, repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
                    }

                    fileLocations.add(fileLocationFilename);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
        }

        if (existingRepo) {
            CodeSearcher cs = new CodeSearcher();
            List<String> indexLocations = cs.getRepoDocuments(repoName);

            for (String file : indexLocations) {
                if (!fileLocations.contains(file)) {
                    Singleton.getLogger().info("Missing from disk, removing from index " + file);
                    try {
                        CodeIndexer.deleteByFileLocationFilename(file);
                    } catch (IOException ex) {
                        Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
                    }
                }
            }
        }
    }

    public String getBlameFilePath(String fileLocationFilename) {
        String[] split = fileLocationFilename.split("/");
        String newString = String.join("/", Arrays.asList(split).subList(1, split.length));
        return newString;
    }


    private CodeOwner getInfoExternal(int codeLinesSize, String repoName, String repoLocations, String fileName) {
        CodeOwner owner = new CodeOwner("Unknown", codeLinesSize, (int)(System.currentTimeMillis() / 1000));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "info", "--xml", fileName);
            processBuilder.directory(new File(repoLocations + repoName));

            Process process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuffer bf = new StringBuffer();


            while ((line = br.readLine()) != null) {
                bf.append(Helpers.removeUTF8BOM(line));
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(bf.toString().getBytes()));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("entry");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    Node node = eElement.getElementsByTagName("commit").item(0);
                    Element e = (Element) node;

                    owner.setName(e.getElementsByTagName("author").item(0).getTextContent());
                }
            }

        } catch (IOException | ParserConfigurationException | SAXException ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + "\n with message: " + ex.getMessage());
        }

        return owner;
    }


    public RepositoryChanged updateSvnRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, boolean useCredentials) {
        boolean changed = false;
        List<String> changedFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();
        Singleton.getLogger().info("SVN: attempting to pull latest from " + repoRemoteLocation + " for " + repoName);

        try {
            String previousRevision = this.getCurrentRevision(repoLocations, repoName);
            Singleton.getLogger().info("SVN: update previous revision " + previousRevision);

            ProcessBuilder processBuilder;
            if (useCredentials) {
                processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "update");
            }
            else {
                processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "update", "--username", repoUserName, "--password", repoPassword);
            }

            processBuilder.directory(new File(repoLocations + repoName));
            Process process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                Singleton.getLogger().info("svn update: " + line);
            }

            String currentRevision = this.getCurrentRevision(repoLocations, repoName);
            Singleton.getLogger().info("SVN: update current revision " + currentRevision);

            if (!previousRevision.equals(currentRevision)) {
                return this.getDiffBetweenRevisions(repoLocations, repoName, previousRevision);
            }
        } catch (IOException | InvalidPathException ex) {
            changed = false;
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
        }

        return new RepositoryChanged(changed, changedFiles, deletedFiles);
    }

    public RepositoryChanged getDiffBetweenRevisions(String repoLocations, String repoName, String startRevision) {
        // svn diff -r 4000:HEAD --summarize --xml

        List<String> changedFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "diff", "-r", startRevision + ":HEAD", "--summarize", "--xml");

            processBuilder.directory(new File(repoLocations + repoName));
            Process process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuffer sb = new StringBuffer();

            while ((line = br.readLine()) != null) {
                Singleton.getLogger().info("svn diff: " + line);
                sb.append(Helpers.removeUTF8BOM(line));
            }

            Singleton.getLogger().info("Before XML parsing: " + sb.toString());

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(sb.toString().getBytes()));
            doc.getDocumentElement().normalize();

            Element node = (Element)doc.getElementsByTagName("diff").item(0);
            node = (Element)node.getElementsByTagName("paths").item(0);

            NodeList nList = node.getElementsByTagName("path");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String type = eElement.getAttribute("item");
                    String path = eElement.getTextContent();

                    if ("modified".equals(type) || "added".equals(type)) {
                        changedFiles.add(path);
                    }
                    else {
                        deletedFiles.add(path);
                    }

                }
            }
        }
        catch(IOException | ParserConfigurationException | SAXException ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
        }

        return new RepositoryChanged(true, changedFiles, deletedFiles);
    }

    public String getCurrentRevision(String repoLocations, String repoName) {
        String currentRevision = "";

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "info", "--xml");
            processBuilder.directory(new File(repoLocations + repoName));
            Process process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(Helpers.removeUTF8BOM(line));
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Singleton.getLogger().info("getCurrentRevision: " + repoName + " " + sb.toString());

            Document doc = dBuilder.parse(new ByteArrayInputStream(sb.toString().getBytes()));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("entry");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    currentRevision = eElement.getAttribute("revision");
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
        }

        return currentRevision;
    }


    public RepositoryChanged checkoutSvnRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, boolean useCredentials) {
        boolean successful = false;
        Singleton.getLogger().info("Attempting to checkout " + repoRemoteLocation);

        try {
            File f = new File(repoLocations);
            if (!f.exists()) {
                f.mkdir();
            }

            ProcessBuilder processBuilder;

            // http://serverfault.com/questions/158349/how-to-stop-subversion-from-prompting-about-server-certificate-verification-fai
            // http://stackoverflow.com/questions/34687/subversion-ignoring-password-and-username-options#38386
            if(useCredentials) {
                processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "checkout", "--no-auth-cache", "--non-interactive", repoRemoteLocation, repoName);
            }
            else {
                processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "checkout", "--no-auth-cache", "--non-interactive", "--username", repoUserName, "--password", repoPassword, repoRemoteLocation, repoName);
            }

            processBuilder.directory(new File(repoLocations));

            Process process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                Singleton.getLogger().info(line);
            }

            successful = true;

        } catch (IOException ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + "\n with message: " + ex.getMessage());
        }

        RepositoryChanged repositoryChanged = new RepositoryChanged(successful);
        repositoryChanged.setClone(true);

        return repositoryChanged;
    }
}
