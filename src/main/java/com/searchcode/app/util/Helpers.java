/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.5
 */

package com.searchcode.app.util;


import com.glaforge.i18n.io.CharsetToolkit;
import com.searchcode.app.config.Values;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Stream;

/**
 * Generic helper methods
 * Anything taken from stackoverflow is used as quote/fair use as seen here
 * https://stackoverflow.uservoice.com/forums/1722-general/suggestions/25546-clarify-the-ownership-and-license-of-code-snippets
 * which seems reasonable as they are all small 10 line methods at most
 * however since they are easy to reimplement if this becomes an issue they will be rewritten
 * http://meta.stackexchange.com/questions/12527/do-i-have-to-worry-about-copyright-issues-for-code-posted-on-stack-overflow
 */
public class Helpers {

    /**
     * Calculate MD5 for a file. Using other methods for this (so this is actually dead code)
     * but we may want to use it in the future so keeping here for the moment.
     */
    public static String calculateMd5(String filePath) {
        String md5 = "";
        try {
            FileInputStream fis = new FileInputStream(new File(filePath));
            md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
            fis.close();
        } // Both the below should be caught before this point
        catch(FileNotFoundException ex) {}
        catch(IOException ex) {}

        return md5;
    }

    /**
     * Similar to the C# Int.TryParse where you pass in a string and if no good it will use the
     * default value which is also parsed... which seems odd now I think about it
     * TODO investigate if the second argument could be an int
     */
    public static int tryParseInt(String toParse, String defaultValue) {
        int result;

        try {
            result = Integer.parseInt(toParse);
        }
        catch(NumberFormatException ex){
            result = Integer.parseInt(defaultValue);
        }

        return result;
    }

    /**
     * Reads a certain amount of lines deep into a file to save on memory
     */
    public static List<String> readFileLines(String filePath, int maxFileLineDepth) throws FileNotFoundException {
        List<String> lines = new ArrayList<>();

        Scanner input = new Scanner(new File(filePath));

        int counter = 0;
        while(input.hasNextLine() && counter < maxFileLineDepth)
        {
            lines.add(input.nextLine());
            counter++;
        }

        return lines;
    }

    public static List<String> readFileLinesGuessEncoding(String filePath, int maxFileLineDepth) throws IOException {
        BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream(filePath), guessCharset(new File(filePath))));

        List<String> fileLines = new ArrayList<>();
        String line = "";

        int lineCount = 0;
        while ((line = reader.readLine()) != null) {
            lineCount++;

            fileLines.add(line);

            if (lineCount == maxFileLineDepth) {
                return fileLines;
            }
        }

        return fileLines;
    }

    public static Charset guessCharset(File file) throws IOException {
        return CharsetToolkit.guessEncoding(file, 4096, StandardCharsets.UTF_8);
    }

    /**
     * Crappy implementation of the C# is nullEmptyOrWhitespace which is occasionally useful
     */
    public static boolean isNullEmptyOrWhitespace(String test) {
        if (test == null) {
            return true;
        }

        if (test.trim().length() == 0) {
            return true;
        }

        return false;
    }

    /**
     * Byte order mark issue fix see
     * http://stackoverflow.com/questions/4569123/content-is-not-allowed-in-prolog-saxparserexception
     */
    public static final String UTF8_BOM = "\uFEFF";
    public static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * Sorts a map by value taken from
     * http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();

        st.sorted( Map.Entry.comparingByValue() ).forEachOrdered( e -> result.put(e.getKey(), e.getValue()) );

        return result;
    }

    public static String getLogPath() {
        String path = (String) Properties.getProperties().getOrDefault(Values.LOG_PATH, Values.DEFAULT_LOG_PATH);

        if (path.toUpperCase().equals("STDOUT")) {
            return path.toUpperCase();
        }

        if (!(path.endsWith("/") || path.endsWith("\\"))) {
            path = path + "/";
        }

        return path;
    }

    public static void closeQuietly(ResultSet resultSet) {
        try {
            resultSet.close();
        }
        catch (Exception ex) {}
    }

    public static void closeQuietly(PreparedStatement preparedStatement) {
        try {
            preparedStatement.close();
        }
        catch (Exception ex) {}
    }

    public static void closeQuietly(Connection connection) {
        try {
            connection.close();
        }
        catch (Exception ex) {}
    }

    public static void closeQuietly(Process process) {
        try {
            process.destroy();
        }
        catch (Exception ex) {}
    }
}
