/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.jobs.repository;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeOwner;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchCodeLib;
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

    private String SVN_BINARY_PATH;
    private boolean ENABLED = true;

    public IndexSvnRepoJob() {
        this.LOWMEMORY = true;
        this.SVN_BINARY_PATH = Properties.getProperties().getProperty(Values.SVNBINARYPATH, Values.DEFAULTSVNBINARYPATH);

        File f = new File(this.SVN_BINARY_PATH);
        if (!f.exists()) {
            this.ENABLED = false;
            Singleton.getLogger().severe("\n///////////////////////////////////////////////////////////////////////////\n// Property svn_binary_path in properties file appears to be incorrect.  //\n// will not be able to index any SVN repository until this is resolved.  //\n///////////////////////////////////////////////////////////////////////////");
        }
    }

    @Override
    public RepositoryChanged updateExistingRepository(RepoResult repoResult, String repoLocations, boolean useCredentials) {
        return this.updateSvnRepository(repoResult, repoLocations, useCredentials);
    }

    @Override
    public RepositoryChanged getNewRepository(RepoResult repoResult, String repoLocations, boolean useCredentials) {
        return this.checkoutSvnRepository(repoResult, repoLocations, useCredentials);
    }

    @Override
    public UniqueRepoQueue getNextQueuedRepo() {
        return Singleton.getUniqueSvnRepoQueue();
    }

    @Override
    public String getCodeOwner(List<String> codeLines, String newString, String repoName, String fileRepoLocations, SearchCodeLib scl) {
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

    public void updateIndex(RepoResult repoResult, String repoLocations, String repoRemoteLocation, boolean existingRepo, RepositoryChanged repositoryChanged) {
        String repoSvnLocation = repoLocations + repoResult.getName();
        Path docDir = Paths.get(repoSvnLocation);

        Singleton.getLogger().info("Doing full index of files for " + repoResult.getName());
        this.indexDocsByPath(docDir, repoResult, repoLocations, repoRemoteLocation, existingRepo);

        Singleton.getLogger().info("Sucessfully processed writing index success for " + repoResult.getName());
    }

    private CodeOwner getInfoExternal(int codeLinesSize, String repoName, String repoLocations, String fileName) {
        CodeOwner owner = new CodeOwner("Unknown", codeLinesSize, (int)(System.currentTimeMillis() / 1000));

        ProcessBuilder processBuilder = new ProcessBuilder(this.SVN_BINARY_PATH, "info", "--xml", fileName);
        processBuilder.directory(new File(repoLocations + repoName));

        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, Values.CHARSET_UTF8);
            bufferedReader = new BufferedReader(isr);
            String line;
            StringBuilder bf = new StringBuilder();


            while ((line = bufferedReader.readLine()) != null) {
                bf.append(Singleton.getHelpers().removeUTF8BOM(line));
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
            Singleton.getHelpers().closeQuietly(process);
            Singleton.getHelpers().closeQuietly(bufferedReader);
        }

        return owner;
    }

    public RepositoryChanged updateSvnRepository(RepoResult repoResult, String repoLocations, boolean useCredentials) {
        boolean changed = false;
        List<String> changedFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();
        Singleton.getLogger().info("SVN: attempting to pull latest from " + repoResult.getUrl() + " for " + repoResult.getName());


        ProcessBuilder processBuilder;
        if (useCredentials) {
            processBuilder = new ProcessBuilder(this.SVN_BINARY_PATH, "update");
        }
        else {
            processBuilder = new ProcessBuilder(this.SVN_BINARY_PATH, "update", "--username", repoResult.getUsername(), "--password", repoResult.getPassword());
        }

        processBuilder.directory(new File(repoLocations + repoResult.getDirectoryName()));
        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            String previousRevision = this.getCurrentRevision(repoLocations, repoResult.getDirectoryName());
            Singleton.getLogger().info("SVN: update previous revision " + previousRevision);

            process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, Values.CHARSET_UTF8);
            bufferedReader = new BufferedReader(isr);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                Singleton.getLogger().info("svn update: " + line);
            }

            String currentRevision = this.getCurrentRevision(repoLocations, repoResult.getDirectoryName());
            Singleton.getLogger().info("SVN: update current revision " + currentRevision);

            if (!previousRevision.equals(currentRevision)) {
                return this.getDiffBetweenRevisions(repoLocations, repoResult.getDirectoryName(), previousRevision);
            }
        } catch (IOException | InvalidPathException ex) {
            changed = false;
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " updateSvnRepository for " + repoResult.getName() + "\n with message: " + ex.getMessage());
        }
        finally {
            Singleton.getHelpers().closeQuietly(process);
            Singleton.getHelpers().closeQuietly(bufferedReader);
        }

        return new RepositoryChanged(changed, changedFiles, deletedFiles);
    }

    public RepositoryChanged getDiffBetweenRevisions(String repoLocations, String repoName, String startRevision) {
        // svn diff -r 4000:HEAD --summarize --xml

        List<String> changedFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();

        ProcessBuilder processBuilder = new ProcessBuilder(this.SVN_BINARY_PATH, "diff", "-r", startRevision + ":HEAD", "--summarize", "--xml");

        processBuilder.directory(new File(repoLocations + repoName));
        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, Values.CHARSET_UTF8);
            bufferedReader = new BufferedReader(isr);
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                Singleton.getLogger().info("svn diff: " + line);
                sb.append(Singleton.getHelpers().removeUTF8BOM(line));
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
            Singleton.getHelpers().closeQuietly(process);
            Singleton.getHelpers().closeQuietly(bufferedReader);
        }

        return new RepositoryChanged(true, changedFiles, deletedFiles);
    }

    public String getCurrentRevision(String repoLocations, String repoName) {
        String currentRevision = "";

        ProcessBuilder processBuilder = new ProcessBuilder(this.SVN_BINARY_PATH, "info", "--xml");
        processBuilder.directory(new File(repoLocations + repoName));
        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, Values.CHARSET_UTF8);
            bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(Singleton.getHelpers().removeUTF8BOM(line));
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
            Singleton.getHelpers().closeQuietly(process);
            Singleton.getHelpers().closeQuietly(bufferedReader);
        }

        return currentRevision;
    }


    public RepositoryChanged checkoutSvnRepository(RepoResult repoResult, String repoLocations, boolean useCredentials) {
        boolean successful = false;
        Singleton.getLogger().info("Attempting to checkout " + repoResult.getUrl());

        ProcessBuilder processBuilder;

        // http://serverfault.com/questions/158349/how-to-stop-subversion-from-prompting-about-server-certificate-verification-fai
        // http://stackoverflow.com/questions/34687/subversion-ignoring-password-and-username-options#38386
        if (useCredentials == false) {
            processBuilder = new ProcessBuilder(this.SVN_BINARY_PATH, "checkout", "--no-auth-cache", "--non-interactive", repoResult.getUrl(), repoResult.getDirectoryName());
        }
        else {
            processBuilder = new ProcessBuilder(this.SVN_BINARY_PATH, "checkout", "--no-auth-cache", "--non-interactive", "--username", repoResult.getUsername(), "--password", repoResult.getPassword(), repoResult.getUrl(), repoResult.getDirectoryName());
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
            InputStreamReader isr = new InputStreamReader(is, Values.CHARSET_UTF8);
            bufferedReader = new BufferedReader(isr);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                Singleton.getLogger().info(line);
            }

            successful = true;

        } catch (IOException ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " checkoutSvnRepository for " + repoResult.getName() + "\n with message: " + ex.getMessage());
        }
        finally {
            Singleton.getHelpers().closeQuietly(process);
            Singleton.getHelpers().closeQuietly(bufferedReader);
        }

        RepositoryChanged repositoryChanged = new RepositoryChanged(successful);
        repositoryChanged.setClone(true);

        return repositoryChanged;
    }
}
