/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.8
 */

package com.searchcode.app.jobs.repository;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeOwner;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchcodeLib;
import com.searchcode.app.util.UniqueRepoQueue;
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

    private String SVNBINARYPATH;
    private boolean ENABLED = true;

    public IndexSvnRepoJob() {
        this.LOWMEMORY = true;
        this.SVNBINARYPATH = Properties.getProperties().getProperty(Values.SVNBINARYPATH, Values.DEFAULTSVNBINARYPATH);

        File f = new File(this.SVNBINARYPATH);
        if (!f.exists()) {
            this.ENABLED = false;
            Singleton.getLogger().severe("\n///////////////////////////////////////////////////////////////////////////\n// Property svn_binary_path in properties file appears to be incorrect.  //\n// will not be able to index any SVN repository until this is resolved.  //\n///////////////////////////////////////////////////////////////////////////");
        }
    }

    @Override
    public RepositoryChanged updateExistingRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String repoBranch, boolean useCredentials) {
        return this.updateSvnRepository(repoName, repoRemoteLocation, repoUserName, repoPassword, repoLocations, useCredentials);
    }

    @Override
    public RepositoryChanged getNewRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String repoBranch, boolean useCredentials) {
        return this.checkoutSvnRepository(repoName, repoRemoteLocation, repoUserName, repoPassword, repoLocations, useCredentials);
    }

    @Override
    public UniqueRepoQueue getNextQueuedRepo() {
        return Singleton.getUniqueSvnRepoQueue();
    }

    @Override
    public String getCodeOwner(List<String> codeLines, String newString, String repoName, String fileRepoLocations, SearchcodeLib scl) {
        return getInfoExternal(codeLines.size(), repoName, fileRepoLocations, newString).getName();
    }

    @Override
    public boolean isEnabled() {
        return this.ENABLED;
    }

    @Override
    public boolean ignoreFile(String fileParent) {
        if (fileParent.endsWith("/.svn") || fileParent.contains("/.svn/")) {
            return true;
        }

        return false;
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

    private CodeOwner getInfoExternal(int codeLinesSize, String repoName, String repoLocations, String fileName) {
        CodeOwner owner = new CodeOwner("Unknown", codeLinesSize, (int)(System.currentTimeMillis() / 1000));

        ProcessBuilder processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "info", "--xml", fileName);
        processBuilder.directory(new File(repoLocations + repoName));

        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            bufferedReader = new BufferedReader(isr);
            String line;
            StringBuilder bf = new StringBuilder();


            while ((line = bufferedReader.readLine()) != null) {
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
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " getInfoExternal for " + repoName + " " + fileName + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(process);
            Helpers.closeQuietly(bufferedReader);
        }

        return owner;
    }


    public RepositoryChanged updateSvnRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, boolean useCredentials) {
        boolean changed = false;
        List<String> changedFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();
        Singleton.getLogger().info("SVN: attempting to pull latest from " + repoRemoteLocation + " for " + repoName);


        ProcessBuilder processBuilder;
        if (useCredentials) {
            processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "update");
        }
        else {
            processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "update", "--username", repoUserName, "--password", repoPassword);
        }

        processBuilder.directory(new File(repoLocations + repoName));
        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            String previousRevision = this.getCurrentRevision(repoLocations, repoName);
            Singleton.getLogger().info("SVN: update previous revision " + previousRevision);

            process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            bufferedReader = new BufferedReader(isr);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                Singleton.getLogger().info("svn update: " + line);
            }

            String currentRevision = this.getCurrentRevision(repoLocations, repoName);
            Singleton.getLogger().info("SVN: update current revision " + currentRevision);

            if (!previousRevision.equals(currentRevision)) {
                return this.getDiffBetweenRevisions(repoLocations, repoName, previousRevision);
            }
        } catch (IOException | InvalidPathException ex) {
            changed = false;
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " updateSvnRepository for " + repoName + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(process);
            Helpers.closeQuietly(bufferedReader);
        }

        return new RepositoryChanged(changed, changedFiles, deletedFiles);
    }

    public RepositoryChanged getDiffBetweenRevisions(String repoLocations, String repoName, String startRevision) {
        // svn diff -r 4000:HEAD --summarize --xml

        List<String> changedFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();

        ProcessBuilder processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "diff", "-r", startRevision + ":HEAD", "--summarize", "--xml");

        processBuilder.directory(new File(repoLocations + repoName));
        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            bufferedReader = new BufferedReader(isr);
            String line;
            StringBuffer sb = new StringBuffer();

            while ((line = bufferedReader.readLine()) != null) {
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
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " getDiffBetweenRevisions for " + repoName + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(process);
            Helpers.closeQuietly(bufferedReader);
        }

        return new RepositoryChanged(true, changedFiles, deletedFiles);
    }

    public String getCurrentRevision(String repoLocations, String repoName) {
        String currentRevision = "";

        ProcessBuilder processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "info", "--xml");
        processBuilder.directory(new File(repoLocations + repoName));
        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
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
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  " getCurrentRevision for " + repoName + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(process);
            Helpers.closeQuietly(bufferedReader);
        }

        return currentRevision;
    }


    public RepositoryChanged checkoutSvnRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, boolean useCredentials) {
        boolean successful = false;
        Singleton.getLogger().info("Attempting to checkout " + repoRemoteLocation);

        ProcessBuilder processBuilder;

        // http://serverfault.com/questions/158349/how-to-stop-subversion-from-prompting-about-server-certificate-verification-fai
        // http://stackoverflow.com/questions/34687/subversion-ignoring-password-and-username-options#38386
        if (useCredentials) {
            processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "checkout", "--no-auth-cache", "--non-interactive", repoRemoteLocation, repoName);
        }
        else {
            processBuilder = new ProcessBuilder(this.SVNBINARYPATH, "checkout", "--no-auth-cache", "--non-interactive", "--username", repoUserName, "--password", repoPassword, repoRemoteLocation, repoName);
        }

        processBuilder.directory(new File(repoLocations));

        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            File file = new File(repoLocations);
            if (!file.exists()) {
                boolean success = file.mkdir();
                if (!success) {
                    throw new IOException("Was unable to create directory " + repoLocations);
                }
            }

            process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            bufferedReader = new BufferedReader(isr);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                Singleton.getLogger().info(line);
            }

            successful = true;

        } catch (IOException ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " checkoutSvnRepository for " + repoName + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(process);
            Helpers.closeQuietly(bufferedReader);
        }

        RepositoryChanged repositoryChanged = new RepositoryChanged(successful);
        repositoryChanged.setClone(true);

        return repositoryChanged;
    }
}
