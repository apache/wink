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

package org.apache.wink.common.internal.log;

public class LogUtils {

    /**
     * Get a string containing the exception and stack of the specified exception.
     *
     * @param e
     * @return
     */
    public static String exceptionToDebugString(Throwable e) {
        String text = e.getClass().getName() + ": " + e.getMessage() + System.getProperty("line.separator");
        text += stackToDebugString(e);
        return text;
    }
    
    /**
     * Get a string containing the stack of the specified exception.
     * Careful: this method skips to the first "at" in the exception
     * stack; it does NOT report the exception itself.
     *
     * @param e
     * @return
     */
    public static String stackToDebugString(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
        java.io.PrintWriter pw = new java.io.PrintWriter(bw);
        e.printStackTrace(pw);
        pw.close();
        String text = sw.getBuffer().toString();
        // Jump past the throwable
        text = "\t" + text.substring(text.indexOf("at "));
        // stop at the servlet or filter, which is the entry point for Wink -- no need to log all the container or JVM stuff
        // find line of entry:
        int entryIndex = text.indexOf("at org.apache.wink.server.internal.servlet");
        int endOfEntryLineIndex = text.indexOf(System.getProperty("line.separator"), entryIndex);
        text = text.substring(0, endOfEntryLineIndex);
        text = replace(text, "at ", "DEBUG_FRAME = ");
        return text;
    }

    /**
     * replace: Like String.replace except that the old and new items are strings.
     *
     * @param name string
     * @param oldT old text to replace
     * @param newT new text to use
     * @return replacement string
     */
    private static final String replace(String name,
                                       String oldT, String newT) {

        if (name == null) return "";

        // Create a string buffer that is twice initial length.
        // This is a good starting point.
        StringBuffer sb = new StringBuffer(name.length() * 2);

        int len = oldT.length();
        try {
            int start = 0;
            int i = name.indexOf(oldT, start);

            while (i >= 0) {
                sb.append(name.substring(start, i));
                sb.append(newT);
                start = i + len;
                i = name.indexOf(oldT, start);
            }
            if (start < name.length()) {
                sb.append(name.substring(start));
            }
        } catch (NullPointerException e) {
            // nothing
        }

        return new String(sb);
    }

    
}
