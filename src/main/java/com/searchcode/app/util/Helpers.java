/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.9
 */

package com.searchcode.app.util;


import com.glaforge.i18n.io.CharsetToolkit;
import com.searchcode.app.config.Values;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
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

    private java.util.Properties properties;

    public Helpers() {
        this(Properties.getProperties());
    }

    public Helpers(java.util.Properties properties) {
        this.properties = properties;
    }

    /**
     * Calculate MD5 for a file. Using other methods for this (so this is actually dead code)
     * but we may want to use it in the future so keeping here for the moment.
     */
    public String calculateMd5(String filePath) {
        String md5 = Values.EMPTYSTRING;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(filePath));
            md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fileInputStream);
        } // Both the below should be caught before this point
        catch(FileNotFoundException ex) {}
        catch(IOException ex) {}
        finally {
            IOUtils.closeQuietly(fileInputStream);
        }

        return md5;
    }

    /**
     * Similar to the C# Int.TryParse where you pass in a string and if no good it will use the
     * default value which is also parsed... which seems odd now I think about it
     */
    public int tryParseInt(String toParse, String defaultValue) {
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
    public List<String> readFileLines(String filePath, int maxFileLineDepth) throws FileNotFoundException {
        List<String> lines = new ArrayList<>();
        Scanner scanner = null;
        int counter = 0;

        try {
            scanner = new Scanner(new File(filePath));

            while (scanner.hasNextLine() && counter < maxFileLineDepth) {
                lines.add(scanner.nextLine());
                counter++;
            }
        }
        finally {
            IOUtils.closeQuietly(scanner);
        }

        return lines;
    }

    public int getCurrentTimeSeconds() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public List<String> readFileLinesGuessEncoding(String filePath, int maxFileLineDepth) throws IOException {
        List<String> fileLines = new ArrayList<>();
        BufferedReader bufferedReader = null;
        String line;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), guessCharset(new File(filePath))));

            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineCount++;

                fileLines.add(line);

                if (lineCount == maxFileLineDepth) {
                    return fileLines;
                }
            }
        }
        finally {
            IOUtils.closeQuietly(bufferedReader);
        }

        return fileLines;
    }

    public Charset guessCharset(File file) throws IOException {
        return CharsetToolkit.guessEncoding(file, 4096, StandardCharsets.UTF_8);
    }

    /**
     * Crappy implementation of the C# is nullEmptyOrWhitespace which is occasionally useful
     */
    public boolean isNullEmptyOrWhitespace(String test) {
        if (test == null) {
            return true;
        }

        if (test.trim().length() == 0) {
            return true;
        }

        return false;
    }

    /**
     * Generic file paths that should be ignored
     */
    public boolean ignoreFiles(String fileParent) {
        if (fileParent.endsWith("/.git") || fileParent.contains("/.git/") || fileParent.contains(".git/") || fileParent.equals(".git")) {
            return true;
        }

        if (fileParent.endsWith("/.svn") || fileParent.contains("/.svn/")) {
            return true;
        }

        if (!this.isNullEmptyOrWhitespace((String)this.properties.get(Values.DIRECTORY_BLACK_LIST))) {
            String[] toIgnoreArray = ((String) this.properties.get(Values.DIRECTORY_BLACK_LIST)).split(",");

            for(String toIgnore: toIgnoreArray) {
                if (fileParent.endsWith("/" + toIgnore) || fileParent.endsWith("/" + toIgnore + "/")) {
                    return true;
                }
            }
        }

        return false;
    }

    public String timeAgo(Instant instant) {
        if (instant == null) {
            return "not yet";
        }

        long seconds = Instant.now().getEpochSecond() - instant.getEpochSecond();
        int minutes = Math.round(seconds / 60);
        int hours = Math.round(seconds / 3600);
        int days = Math.round(seconds / 86400);
        int weeks = Math.round(seconds / 604800);
        int months = Math.round(seconds / 2600640);
        int years = Math.round(seconds / 31207680);

        if (seconds <= 60) {
            if (seconds == 1) {
                return "one second ago";
            } else {
                return seconds + " seconds ago";
            }
        }
        else if (minutes <= 60) {
            if (minutes == 1) {
                return "one minute ago";
            } else {
                return minutes + " minutes ago";
            }
        }
        else if (hours <= 24) {
            if (hours == 1) {
                return "an hour ago";
            } else {
                return hours + " hrs ago";
            }
        }
        else if (days <= 7) {
            if (days == 1) {
                return "yesterday";
            } else {
                return days + " days ago";
            }
        }
        else if (weeks <= 4.3) {
            if (weeks == 1) {
                return "a week ago";
            } else {
                return weeks + " weeks ago";
            }
        }
        else if (months <= 12) {
            if (months == 1) {
                return "a month ago";
            } else {
                return months + " months ago";
            }
        }
        else {
            if (years == 1) {
                return "one year ago";
            } else {
                return years + " years ago";
            }
        }
    }

    /**
     * Byte order mark issue fix see
     * http://stackoverflow.com/questions/4569123/content-is-not-allowed-in-prolog-saxparserexception
     */
    private final String UTF8_BOM = "\uFEFF";
    public String removeUTF8BOM(String s) {
        if (s.startsWith(this.UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * Sorts a map by value taken from
     * http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
     */
    public <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();

        st.sorted( Map.Entry.comparingByValue() ).forEachOrdered( e -> result.put(e.getKey(), e.getValue()) );

        return result;
    }

    public String getLogPath() {
        String path = (String) this.properties.getOrDefault(Values.LOG_PATH, Values.DEFAULT_LOG_PATH);

        if (path.toUpperCase().equals("STDOUT")) {
            return path.toUpperCase();
        }

        if (!(path.endsWith("/") || path.endsWith("\\"))) {
            path = path + "/";
        }

        if (!this.isNullEmptyOrWhitespace(this.properties.getProperty(Values.DIRECTORY_BLACK_LIST, Values.DEFAULT_DIRECTORY_BLACK_LIST))) {
            // Split and check if we end with them
        }

        return path;
    }

    public void closeQuietly(ResultSet resultSet) {
        try {
            resultSet.close();
        }
        catch (Exception ex) {}
    }

    public void closeQuietly(PreparedStatement preparedStatement) {
        try {
            preparedStatement.close();
        }
        catch (Exception ex) {}
    }

    public void closeQuietly(Connection connection) {
        try {
            connection.close();
        }
        catch (Exception ex) {}
    }

    public void closeQuietly(Process process) {
        try {
            process.destroy();
        }
        catch (Exception ex) {}
    }

    public void closeQuietly(BufferedReader bufferedReader) {
        try {
            bufferedReader.close();
        }
        catch (Exception ex) {}
    }

    public void closeQuietly(Repository repository) {
        try {
           repository.close();
        }
        catch (Exception ex) {}
    }

    public void closeQuietly(Git git) {
        try {
            git.close();
        }
        catch (Exception ex) {}
    }
}
