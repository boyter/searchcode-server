/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
 */

package com.searchcode.app.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.Deflater;

/**
 * File for playing around with custom dictionaries when compressing source code as a way to save file space.
 * Could only get to about 5% improvement which seems low considering the additional work. Something to look into
 * in the future though hence keeping this class. If nothing else it should make an interesting blog post.
 */
public class CompressTest {

    public static int totalUncompressedLength = 0;
    public static int totalCompressedLength = 0;
    public static String DICTIONARY = "";


    public static void main(String[] args) throws Exception {

        CompressTest ct = new CompressTest();

        ct.checkDirectory();
        System.out.println("UnCompressed: " + totalUncompressedLength);
        System.out.println("  Compressed: " + totalCompressedLength);

        int previousCompressed = totalCompressedLength;

        System.out.println("");


        DICTIONARY = Files.readAllLines(Paths.get("/tmp/dictionary"), StandardCharsets.UTF_8).get(0);
        totalCompressedLength = 0;
        totalUncompressedLength = 0;
        ct.checkDirectory();
        System.out.println("UnCompressed: " + totalUncompressedLength);
        System.out.println("  Compressed: " + totalCompressedLength);

        System.out.println("");

        System.out.println("     Savings: " + (previousCompressed - totalCompressedLength));



//        // Decompress the bytes
//        Inflater decompresser = new Inflater();
//        decompresser.setInput(output, 0, compressedDataLength);
//        byte[] result = new byte[utf8Bytes.length];
//        decompresser.inflate(result);
//        decompresser.setDictionary(dict);
//        int resultLength = decompresser.inflate(result);
//        decompresser.end();
//
//        // Decode the bytes into a String
//        String outputString = new String(result, 0, resultLength, "UTF-8");
//        System.out.println("Decompressed String: " + outputString);
    }

    public void checkDirectory() throws Exception {
        Files.walkFileTree(Paths.get("/Users/boyter/Desktop/searchcode/"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                //if (!file.toString().endsWith(".java") && !file.toString().endsWith(".xml")) {
                //if (!file.toString().endsWith(".java") && !file.toString().endsWith(".py") && !file.toString().endsWith(".rb")) {
                //    return FileVisitResult.CONTINUE;
                //}

                //System.out.println(file.toString());

                try {
                    List<String> code = Files.readAllLines(Paths.get(file.toString()), StandardCharsets.UTF_8);
                    StringBuilder sb = new StringBuilder();

                    for (String line : code) {
                        sb.append(line);
                    }

                    String inputString = sb.toString();

                    final byte[] utf8Bytes = inputString.getBytes("UTF-8");
                    //System.out.println("UncompressedDataLength: " + utf8Bytes.length);
                    totalUncompressedLength += utf8Bytes.length;

                    byte[] input = inputString.getBytes("UTF-8");
                    byte[] dict = DICTIONARY.getBytes("UTF-8");

                    // Compress the bytes
                    byte[] output = new byte[utf8Bytes.length];
                    Deflater compresser = new Deflater();
                    compresser.setInput(input);
                    compresser.setDictionary(dict);
                    compresser.finish();
                    int compressedDataLength = compresser.deflate(output);

                    //System.out.println("filename:" + file.toString() + " uncompressed:" + totalUncompressedLength + " compressed:" + compressedDataLength);

                    //System.out.println("CompressedDataLength: " + compressedDataLength);
                    totalCompressedLength += compressedDataLength;
                }
                catch(Exception ex) {}

                return FileVisitResult.CONTINUE;
            }
        });
    }

    /* This is from a test to get the blame put here in test/temp class because we want to use it later
    public String Blame(String repo, String fileName) {
        try {
            String repoLoc = "./repo/"+repo+"/.git";

            System.out.println(repoLoc);
            Repository localRepository = new FileRepository(repoLoc);
            BlameCommand blamer = new BlameCommand(localRepository);

            ObjectId commitID = localRepository.resolve("HEAD");
            blamer.setStartCommit(commitID);


            System.out.println("Getting blame for " + fileName + " from " + repoLoc);
            blamer.setFilePath(fileName);
            BlameResult blame = blamer.call();

            if (blame ==null) {
                System.out.println("Blame is null");
                return "";
            }

            for(int i=0;i<10;i++) {
                RevCommit commit = blame.getSourceCommit(i);

                PersonIdent authorIdent = commit.getAuthorIdent();
                Date authorDate = authorIdent.getWhen();
                Calendar cal = Calendar.getInstance();
                cal.setTime(authorDate);

                SimpleDateFormat simpleDateFormat =
                        new SimpleDateFormat("EE MMM dd HH:mm:ss zzz yyyy");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                System.out.println("Line: " + i + ": " + commit.getCommitTime() + " " + commit.getId() + " " + commit.getAuthorIdent().getName() + " " + commit.getShortMessage() + " " + simpleDateFormat.format(cal.getTime()) );
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        return "";
    }
     */
}
