/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.wink.common.internal.i18n;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * 
 * Intent of MessagesTest class is to check the specified translation properties files against the java source code for:
 * 
 * 1)  checks that the strings that should be externalized are externalized (only debug messages do not need to be externalized)
 * 2)  checks that all keys referred to by Messages.getMessage actually exist
 * 3)  checks that there are no unused keys in the resource.properties file
 * 4)  checks that the number of params matches up with the number of braces {} in a formatted log string
 *
 */
public class MessagesTest extends TestCase {
    
    private String workSpacePath = null;
    private MessageStringsCache messageStrings = null;
    private Properties unusedProps;
    
    // some necessary pre-compiled patterns:
    static final Pattern patternForNoLogger = Pattern.compile("\\G.*?((Messages\\s*?\\.\\s*?getMessage.*?));", Pattern.COMMENTS);
    static final Pattern patternIntRequired = Pattern.compile("\\G.*?\\{(\\d+?)}", Pattern.COMMENTS);
    static final Pattern patternIntNotRequired = Pattern.compile("\\G.*?\\{}", Pattern.COMMENTS);
    
    // default resource file in case of unittest environment
    private static String defaultResourceFile = "wink-common/src/main/resources/org/apache/wink/common/internal/i18n/resource.properties";
    static {
        defaultResourceFile = defaultResourceFile.replace("/", System.getProperty("file.separator"));
    }
    
    
    /**
     * 
     * A cache to hold the formatted strings with their brace counts, so counts have to be taken again
     * and again for a formatted string that is used many times.
     *
     */
    private static class MessageStringsCache {
        
        // the cache
        private HashMap<String, Integer> stringsToBraceCount = new HashMap<String, Integer>();
        private Properties messageProps = null;
        
        /**
         * Keeps a copy of the original message properties as a convenience to users of this class.
         * 
         * @param props original message properties
         */
        public MessageStringsCache(Properties props) {
            messageProps = new Properties();
            messageProps.putAll(props);
        }
        
        /**
         * 
         * @param key into the messages properties
         * @param filePath param is passed to produce meaningful failure message only
         * @return
         */
        public String getFormattedStringByKey(String key, String filePath) {
            String formattedString = messageProps.getProperty(key);
            if (formattedString == null) {
                fail("Expected to find non-null property with key \n" + key + "\n used by\n" + filePath);
            } else if (formattedString.equals("")) {
                fail("Expected to find non-empty property with key \n" + key + "\n used by\n" + filePath);
            }
            return formattedString;
        }
        
        /**
         * 
         * @param key srcFile into the messages properties
         * @param intRequired if braces are formatted with an integer n, like {n}, intRequired should be set to true
         * @param filePath param is passed to produce meaningful failure message only
         * @return count of all {} when intRequired = false or unique {n} occurrences when intRequired = true
         */
        public int getBraceCountByKey(String key, boolean intRequired, String filePath) {
            String formattedString = getFormattedStringByKey(key, filePath);
            if (formattedString != null) {
                return getBraceCount(formattedString, intRequired);
            }
            return -1;
        }
        
        /**
         * 
         * @param string the actual formatted message string
         * @param intRequired if braces are formatted with an integer n, like {n}, intRequired should be set to true
         * @return count of all {} when intRequired = false or unique {n} occurrences when intRequired = true
         */
        public int getBraceCount(String string, boolean intRequired) {
            if (!stringsToBraceCount.containsKey(string)) {
                // count the number of occurrences of {} or {n} where n is an int
                Pattern pattern;
                if (intRequired) {
                    pattern = patternIntRequired;
                } else {
                    pattern = patternIntNotRequired;
                }
                Matcher matcher = pattern.matcher(string);
                int counter = 0;
                if (intRequired) {
                    // string may contain multiple {0} constructs.  We want to count the unique integers
                    HashSet<String> ints = new HashSet<String>();
                    while(matcher.find()) {
                        ints.add(matcher.group(1));
                    }
                    counter = ints.size();
                } else {
                    while(matcher.find()) {
                        counter++;
                    }
                }
                stringsToBraceCount.put(string, counter);
            }
            return stringsToBraceCount.get(string);
        }
        
    }
    

    @Override
    public void setUp() {
        try {
            unusedProps = new Properties();
            System.out.println("Loading properties from: " + getWorkspacePath() + defaultResourceFile);
            unusedProps.load(new FileInputStream(getWorkspacePath() + defaultResourceFile));
            messageStrings = new MessageStringsCache(unusedProps);
        } catch (Throwable t) {
            fail("Could not load properties due to: " + t + ": " + t.getMessage());
        }
    }
    
    /**
     * Filter to determine which files to scan.  Scanner will only accept *.java files, but additional
     * exclusion filters may be specified on the command line.
     *
     */
    private static class JavaSrcFilenameFilter implements FilenameFilter {

        /**
         * @param dir path up to, but not including, the filename
         * @param name of the file
         * @return true if dir and name satisfy all of the filter rules
         */
        public boolean accept(File dir, String name) {
            // try to filter down to just production code source
            String dirString = dir.toString();
            if (!dirString.contains(".svn")
                    && !dirString.contains("src" + System.getProperty("file.separator") + "test")
                    && !dirString.contains("wink-examples")
                    && !dirString.contains("wink-itests")
                    && !dirString.contains("wink-component-test-support")
                    && !dirString.contains("wink-assembly")
                    && name.endsWith(".java")) {
                return true;
            }
            return false;
        }
        
    }


    /**
     * recursively collect list of filtered files
     * 
     * @param directory
     * @param filter
     * @return
     */
    private static Collection<File> listFiles(File directory,
            FilenameFilter filter) {
        Vector<File> files = new Vector<File>();
        File[] entries = directory.listFiles();
        for (File entry : entries) {
            if (filter == null || filter.accept(directory, entry.getName())) {
                files.add(entry);
            }
            if (entry.isDirectory()) {
                files.addAll(listFiles(entry, filter));
            }
        }
        return files;
    }
    
    
    /**
     * Used in junit only
     * @return full filesystem path to the workspace root
     */
    private String getWorkspacePath() {
        if (workSpacePath == null) {
            // set up the default properties file in the location where the RestServlet will find it upon test execution
            String classPath = System.getProperty("java.class.path");

            StringTokenizer tokenizer = new StringTokenizer(classPath, System.getProperty("path.separator"));
            while (tokenizer.hasMoreElements()) {
                String temp = tokenizer.nextToken();
                if (temp.endsWith("test-classes")) {
                    if (!temp.startsWith(System.getProperty("file.separator"))) {
                        // must be on Windows.  get rid of "c:"
                        temp = temp.substring(2, temp.length());
                    }
                    workSpacePath = temp;
                    break;
                }
            }

            if (workSpacePath == null) {
                fail("Failed to find test-classes directory to assist in finding workspace root");
            }
            // move up to peer path of wink-common (so, minus wink-common/target/test-classes
            workSpacePath = workSpacePath.substring(0, workSpacePath.length() - 31);
        }
        return workSpacePath;
    }
    
    /**
     * extracts the quoted string, and splits the string at the first comma not in quotes.
     * String parameter will be something like either of the following:
     * 
     * Messages.getMessage("someKeyToMessageProps", object1, object2)
     * Messages.getMessage(SOME_STATIC_VAR, object1)
     * Messages.getMessage(SOME_STATIC_VAR), object1
     * "there was a problem with {} and {}", object1, object2
     * 
     * Result will be an array of strings, like:
     * 
     * {"someKeyToMessageProps", " object1, object2"}
     * {"SOME_STATIC_VAR", " object1"}
     * {"SOME_STATIC_VAR"}
     * {"there was a problem with {} and {}", " object1, object2"}
     * 
     * @param string to parse
     * @param fileText the full text of the file being scanned, in case we need to go retrieve the value of a static var
     * @param filePath param is passed to produce meaningful failure message only
     * @return
     */
    private String[] splitString(String string, String fileText, String filePath) {
        String copy = new String(string);
        copy = copy.replace("\\\"", "");  // replace any escaped quotes
        
        // extract the part past Messages.getMessage, if necessary:
        if (!copy.startsWith("\"")) {
            // get whatever is between the matched parens:
            Pattern extractStringInParen = Pattern.compile("Messages\\s*\\.\\s*getMessage(FromBundle)??\\s*\\(\\s*(.*)");
            Matcher matcher = extractStringInParen.matcher(copy);
            if (matcher.matches()) {
                copy = matcher.group(2);
            }
        }
        
        if (!copy.startsWith("\"")) {
            // it's likely a static var, not a hard-coded string, so split on the commas and be done with it; best effort
            StringTokenizer tokenizer = new StringTokenizer(copy, ",");
            String[] strings = new String[2];
            String staticVar = tokenizer.nextToken().trim();
            
            // go extract the real value of staticVar, which will be the key into the resource properties file
            Pattern extractStaticVarValuePattern = Pattern.compile(".*" + staticVar + "\\s*=\\s*\"(.*?)\"\\s*;.*");
            Matcher matcher = extractStaticVarValuePattern.matcher(fileText);
            if (matcher.matches()) {
                strings[0] = matcher.group(1);
            } else {
                fail("Could not find value of variable " + staticVar + " in " + filePath);
            }
            
            String restOfString = null;
            if (tokenizer.hasMoreTokens()) {
                restOfString = "";
                while (tokenizer.hasMoreTokens()) {
                    restOfString += "," + tokenizer.nextToken().trim();
                }
                restOfString = restOfString.substring(1);// skip first comma
            }
            strings[1] = restOfString;
            return strings;
        }
        
        // look for a the sequence quote followed by comma
        ByteArrayInputStream bais = new ByteArrayInputStream(copy.getBytes());
        boolean outsideQuotedString = false;
        int endHardStringCounter = 1;
        // skip past the first quote
        int ch = bais.read();
        // find the matched quote and end paren; best effort here
        int endParenCounter = 1;
        int parenDepth = 1;
        boolean hardStringDone = false;
        while ((ch = bais.read()) != -1) {
            if (ch == '"' && !outsideQuotedString)
                outsideQuotedString = true;
            else if ((ch == ',') && outsideQuotedString)
                hardStringDone = true;
            else if ((ch == ')') && outsideQuotedString && ((--parenDepth) == 0))
                break;
            else if ((ch == '(') && outsideQuotedString) {
                parenDepth++;
            }
            else if (ch == '"' && outsideQuotedString)  // the quoted string continues, like: "we have " + count + " apples"
                outsideQuotedString = false;
            
            endParenCounter++;
            if (!hardStringDone)
                endHardStringCounter++;
        }
        try {
            bais.close();
        } catch (IOException e) {
        }
        String hardCodedString = copy.substring(1, endHardStringCounter-1).trim();
        
        // clean up, if necessary:
        while (hardCodedString.endsWith("\""))
            hardCodedString = hardCodedString.substring(0, hardCodedString.length()-1);
        
        String restOfString = null;
        if (endHardStringCounter < copy.length()) {
            restOfString = copy.substring(endHardStringCounter, endParenCounter);
            restOfString = restOfString.substring(restOfString.indexOf(",")+1);  // skip the first comma
            restOfString = restOfString.trim();
        }
        return new String[]{hardCodedString, restOfString};
    }
    
    /*
     * inspect the string.  Note the parens of the
     * passed String parameter may not be balanced.
     * 
     * String will be something like either of the following:
     * 
     *     Messages.getMessage("someKeyToMessageProps"), object1, object2
     *     "there was a problem with {} and {}", object1, object2
     * 
     * srcFile param is so we can print an informative failure message.
     * unusedProps is so we can delete key/value pairs as we encounter them in source, so we can make sure there
     *     are no unnecessary key/value pairs in the message file
     */
    private void parseAndInspect(String string, boolean externalizationRequired, String fileText, String filePath, Properties unusedProps) {
        // expect a string with unmatched parens, but we don't care.  We just want to know if the messages file has
        // the string if Messages.getMessage is called, and if the number of {} in the string matches up with the num of params

        // clean up a bit
        string = string.trim();
        if (string.endsWith(")")) {
            string = string.substring(0, string.length() - 1);
            string = string.trim();
        }
        
        if (!string.startsWith("Messages") && string.startsWith("\"") && externalizationRequired) {
            fail("Externalization is required for parameter " + "\"" + string + "\" statement in " + filePath);
        }
        
        // short circuit:  message passed to logger is just a variable, like Exception.getMessage(), so there's nothing to check
        if (!string.startsWith("Messages") && !string.startsWith("\"")) {
            return;
        }

        String[] splitString;
        splitString = splitString(string, fileText, filePath);  // split between quoted part of the first param, and the rest
        
        if (splitString.length == 0) {
            // means we couldn't find the value of the static var used as the key into Messages.getMessage
            // error message already printed, nothing else to check
            return;
        }
        
        int chickenLips = 0;
        if (string.startsWith("Messages")) {
            chickenLips = messageStrings.getBraceCountByKey(splitString[0], true, filePath);
            if (chickenLips == -1) {
                // no key was found, error message already printed, nothing else to check
                return;
            }
            unusedProps.remove(splitString[0]);
        } else if (string.startsWith("\"")) {
            chickenLips = messageStrings.getBraceCount(splitString[0], false);
        }
        // ok, there better be chickenLips many more tokens!
        int remainingParams = 0;
        if (splitString[1] != null) {
            StringTokenizer tokenizer = new StringTokenizer(splitString[1], ",");
            remainingParams = tokenizer.countTokens();
        }
        // SLF4J logger can take an extra exception param
        if (chickenLips == remainingParams-1) {
            // token count may be one greater than chickenlips, since messages may be something like:
            // logger.trace("abcd", new RuntimeException());
            // or:
            // logger.error(Messages.getMessage("saxParseException", type.getName()), e);
//            System.out.print("\nWARNING: Expected " + chickenLips + " parameters, but found " + tokenizer.countTokens() + (string.startsWith("Messages") ? " for key " : " for formatted string ") +
//                    "\"" + splitString[0] + "\" in " + srcFile + ".  SLF4J allows an Exception as a parameter with no braces in the formatted message, but you should confirm this is ok.");
            return;
        }
        if (remainingParams != chickenLips) {
            fail("Expected " + chickenLips + " parameters, but found " + remainingParams + (string.startsWith("Messages") ? " for key " : " for formatted string ") +
                    "\"" + splitString[0] + "\" in " + filePath);
        }
    }
    
    /**
     * getFilteredFileContents will filter out all comments in .java source files and return the contents
     * in a single line.  A single space character replaces newlines.
     * @param file
     * @return
     * @throws IOException
     */
    private static String getFilteredFileContents(File file)  {
        String fileText = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while((line = br.readLine()) != null) {
                // get rid of single-line comments
                int eol = line.indexOf("//");
                if (eol == -1) {
                    fileText += line;
                } else {
                    fileText += line.substring(0, eol);
                }
                fileText += " ";  // to be safe, since we're smashing the whole file down into one line
            }
            br.close();
            fis.close();
        } catch (IOException e) {
            fail(e.getMessage() + " while reading " + file.getAbsolutePath());
        }
        return fileText.replaceAll("/\\*.*?\\*/", "");  // get rid of comment blocks
        
    }


    // check all production code .java files for all calls to Logger.trace, error, warn, and info to ensure
    // that the formatted string is correct, and that the reference, if any, to resource.properties keys is correct.
    public void testMessages() throws IOException {
        
        // to find the Logger variable name:
        Pattern patternLoggerRef = Pattern.compile(".*?\\s+?Logger\\s+?([\\p{Alnum}|_]+).*");

        int progressCounter = 0;
        ArrayList<File> files = new ArrayList<File>();
        String path = getWorkspacePath();
        
        System.out.println("Collecting list of files to scan...");
        files.addAll(listFiles(new File(path), new JavaSrcFilenameFilter()));
        System.out.println("Checking " + files.size() + " files.");
        for (File file: files) {
            String fileText = getFilteredFileContents(file);
            Matcher matcher = patternLoggerRef.matcher(fileText);
            String loggerVariableName = null;

            // indicate some progress for IDE users
            System.out.print(".");
            progressCounter++;
            if(progressCounter % 10 == 0) {
                System.out.println(progressCounter);
            }

            
            if (matcher.matches()) {
                loggerVariableName = matcher.group(1);
            }
            
            // now that we know what the logger variable name is, we can inspect any calls made to its methods:
            // (we can't really use regex here to match balanced parentheses)
            ArrayList<Pattern> betweenLoggersPatterns = new ArrayList<Pattern>();
            if (loggerVariableName != null) {
                betweenLoggersPatterns.add(Pattern.compile("\\G.*?" + loggerVariableName + "\\s*?\\.\\s*?(info|trace|debug|error|warn)\\s*?\\((.*?);", Pattern.COMMENTS));
                betweenLoggersPatterns.add(patternForNoLogger);  // some patterns may get checked twice, but that's ok
            } else {
                betweenLoggersPatterns.add(patternForNoLogger);
            }
            
            for (Pattern betweenLoggersPattern: betweenLoggersPatterns.toArray(new Pattern[]{})) {
                Matcher betweenLoggersMatcher = betweenLoggersPattern.matcher(fileText);
                while (betweenLoggersMatcher.find()) {
                    boolean externalizationRequired = !betweenLoggersMatcher.group(1).equals("debug") && !betweenLoggersMatcher.group(1).equals("trace");
                    parseAndInspect(betweenLoggersMatcher.group(2), externalizationRequired, fileText, file.getAbsolutePath(), unusedProps);
                }
            }
        }
        if (!unusedProps.isEmpty()) {
            Set<Object> keys = unusedProps.keySet();
            for (Object key : keys.toArray()) {
                System.err.println("key \"" + key + "\" is unused.");
            }
            fail("There are some unused key/value pairs in one or more of your properties message files.  See System.err for this test for the list of unused keys.");
        }
        System.out.println("Done.");
    }
    
}