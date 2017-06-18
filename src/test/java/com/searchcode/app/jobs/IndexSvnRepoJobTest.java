package com.searchcode.app.jobs;

import com.searchcode.app.jobs.repository.IndexSvnRepoJob;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

public class IndexSvnRepoJobTest extends TestCase {

    // TODO most of whats in here is just copied into the SVNRepoJob, so we need to test it properly
    public void testSomething() {
        IndexSvnRepoJob svnRepoJob = new IndexSvnRepoJob();
        //svnRepoJob.checkoutSvnRepository("test", "https://moreterra.svn.codeplex.com/svn", "", "", "/tmp/", false);
        //svnRepoJob.updateSvnRepository("test", "https://moreterra.svn.codeplex.com/svn", "", "", "/tmp/", false);
    }

    public void testGetCurrentRevision() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            String test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<info>\n" +
                    "    <entry kind=\"dir\" path=\".\" revision=\"44458\">\n" +
                    "        <url>https://moreterra.svn.codeplex.com/svn</url>\n" +
                    "        <relative-url>^/</relative-url>\n" +
                    "        <repository>\n" +
                    "            <root>https://moreterra.svn.codeplex.com/svn</root>\n" +
                    "            <uuid>8ac85a5f-fc25-4b94-8d5e-fa21b7f00f96</uuid>\n" +
                    "        </repository>\n" +
                    "        <wc-info>\n" +
                    "            <wcroot-abspath>/private/tmp/test</wcroot-abspath>\n" +
                    "            <schedule>normal</schedule>\n" +
                    "            <depth>infinity</depth>\n" +
                    "        </wc-info>\n" +
                    "        <commit revision=\"38053\">\n" +
                    "            <author>unknown</author>\n" +
                    "            <date>2014-10-30T18:28:31.610000Z</date>\n" +
                    "        </commit>\n" +
                    "    </entry>\n" +
                    "</info>";

            Document doc = dBuilder.parse(new ByteArrayInputStream(test.getBytes()));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("entry");

            for  (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String something = "Current Revision: " + eElement.getAttribute("revision");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void testGetFileAuthor() {

        String test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<info>\n" +
                "    <entry kind=\"file\" path=\"MoreTerra.sln\" revision=\"44458\">\n" +
                "        <url>https://moreterra.svn.codeplex.com/svn/MoreTerra/MoreTerra.sln</url>\n" +
                "        <relative-url>^/MoreTerra/MoreTerra.sln</relative-url>\n" +
                "        <repository>\n" +
                "            <root>https://moreterra.svn.codeplex.com/svn</root>\n" +
                "            <uuid>8ac85a5f-fc25-4b94-8d5e-fa21b7f00f96</uuid>\n" +
                "        </repository>\n" +
                "        <wc-info>\n" +
                "            <wcroot-abspath>/private/tmp/test</wcroot-abspath>\n" +
                "            <schedule>normal</schedule>\n" +
                "            <depth>infinity</depth>\n" +
                "            <text-updated>2016-05-16T22:18:53.000000Z</text-updated>\n" +
                "            <checksum>602b3b9386d05c99e4f3b577e777afc810627e35</checksum>\n" +
                "        </wc-info>\n" +
                "        <commit revision=\"31664\">\n" +
                "            <author>Steve Calzone</author>\n" +
                "            <date>2014-03-07T17:41:33.150000Z</date>\n" +
                "        </commit>\n" +
                "    </entry>\n" +
                "</info>";

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(test.getBytes()));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("entry");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    Node node = eElement.getElementsByTagName("commit").item(0);
                    Element e = (Element) node;

                    String something = e.getElementsByTagName("author").item(0).getTextContent();

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void testGetDifferences() {

        String test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<diff>\n" +
                "    <paths>\n" +
                "        <path item=\"modified\" kind=\"file\" props=\"none\">WorldView/SettingsManager.cs</path>\n" +
                "        <path item=\"modified\" kind=\"file\" props=\"none\">WorldView/Program.cs</path>\n" +
                "        <path item=\"modified\" kind=\"file\" props=\"none\">WorldView/Structures/World.cs</path>\n" +
                "        <path item=\"modified\" kind=\"file\" props=\"none\">WorldView/WorldMapper.cs</path>\n" +
                "        <path item=\"modified\" kind=\"file\" props=\"none\">WorldView/FormWorldView.cs</path>\n" +
                "    </paths>\n" +
                "</diff>";

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(test.getBytes()));
            doc.getDocumentElement().normalize();

            Element node = (Element)doc.getElementsByTagName("diff").item(0);
            node = (Element)node.getElementsByTagName("paths").item(0);

            NodeList nList = node.getElementsByTagName("path");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String something = eElement.getAttribute("item") + " " +eElement.getTextContent();

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
