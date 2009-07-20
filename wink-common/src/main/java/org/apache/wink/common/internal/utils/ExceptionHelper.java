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

package org.apache.wink.common.internal.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Converts exception to a formatted string. If property
 * <code>print.exception.stack.trace</code> is set to true, the stack trace is
 * returned, otherwise only exception name and message is returned.
 */
public final class ExceptionHelper {

    private static final int     MAX_CHARS         = 20000;
    private static final boolean PRINT_STACK_TRACE = true;

    private ExceptionHelper() {
    }

    /**
     * Converts throwable to string.
     * 
     * @param t the throwable to be converted
     * @return string serialization of the throwable
     */
    public static String toString(Throwable t) {
        String s = stackTraceToString(t);
        if (s == null)
            s = "Exception " + t.getClass().getName() + " : " + t.getMessage();
        return s;
    }

    /**
     * Print exception stack trace to string if allowed.
     * 
     * @param t exception to print
     * @return string with exception or null if stack trace printing disabled
     */
    public static String stackTraceToString(Throwable t) {
        if (PRINT_STACK_TRACE) {
            StringWriter descr = new StringWriter() {
                boolean writeOK = true;

                public void write(char[] cbuf, int off, int len) {
                    if (canWrite())
                        super.write(cbuf, off, len);
                }

                public void write(int c) {
                    if (canWrite())
                        super.write(c);
                }

                public void write(String str, int off, int len) {
                    if (canWrite())
                        super.write(str, off, len);
                }

                public void write(String str) {
                    if (canWrite())
                        super.write(str);
                }

                private boolean canWrite() {
                    if (writeOK && this.getBuffer().length() > MAX_CHARS) {
                        super.write(" ... [ too long - truncated ]");
                        writeOK = false;
                    }
                    return writeOK;
                }
            };
            t.printStackTrace(new PrintWriter(descr));
            return descr.toString();
        } else {
            return null;
        }
    }

    // private static final byte[] stackTraceElementStreamPrefix;
    //
    // static {
    // byte[] value;
    // try {
    // // Create "stream" containing single StackTraceElement with second
    // instance prefix
    // ByteArrayOutputStream bos = new ByteArrayOutputStream();
    // ObjectOutputStream oos = new ObjectOutputStream(bos);
    // oos.writeObject(new Throwable().getStackTrace()[0]);
    // oos.flush();
    // bos.write(ObjectOutputStream.TC_OBJECT);
    // bos.write(ObjectOutputStream.TC_REFERENCE);
    // bos.write(0); bos.write(0x7e); bos.write(0); bos.write(0);
    // bos.close();
    // value = bos.toByteArray();
    // } catch (IOException e) {
    // value = null;
    // }
    // stackTraceElementStreamPrefix = value;
    // }
    //    
    // /**
    // * Create new stack trace element.
    // *
    // * @param className class name
    // * @param methodName method name
    // * @param fileName file name (null if unknown)
    // * @param lineNo line number, -1 if unknown, -2 if native method
    // * @return stack trace element or null if unable to create
    // */
    // private static StackTraceElement createStackTraceElement(String
    // className, String methodName, String fileName, int lineNo) {
    // try {
    // // Write string parameters into stream
    // ByteArrayOutputStream bos = new ByteArrayOutputStream();
    // ObjectOutputStream oos = new ObjectOutputStream(bos);
    // oos.writeObject(className);
    // oos.writeObject(fileName);
    // oos.writeObject(methodName);
    // oos.close();
    // byte[] db = bos.toByteArray();
    // // Now merge it with stackTraceElementPrefix and override second stream's
    // magic with line number field value
    // byte[] ib = new byte[stackTraceElementStreamPrefix.length + db.length];
    // System.arraycopy(stackTraceElementStreamPrefix, 0, ib, 0,
    // stackTraceElementStreamPrefix.length);
    // db[0] = (byte)((lineNo >> 24) & 0xff);
    // db[1] = (byte)((lineNo >> 16) & 0xff);
    // db[2] = (byte)((lineNo >> 8) & 0xff);
    // db[3] = (byte)(lineNo & 0xff);
    // System.arraycopy(db, 0, ib, stackTraceElementStreamPrefix.length,
    // db.length);
    // // Finally read the elements - discard first (comes from prefix), use
    // second one
    // ObjectInputStream ois = new ObjectInputStream(new
    // ByteArrayInputStream(ib));
    // ois.readObject();
    // return (StackTraceElement)ois.readObject();
    // } catch (Exception e) {
    // return null;
    // }
    // }

    // /**
    // * Parse stack trace element from single stack trace line.
    // * Retuns null if cannot parse.
    // *
    // * @param line line to parse
    // * @return parsed stack trace element or null if "wrong format" of line
    // */
    // private static StackTraceElement parseStackTraceElement(String line) {
    // // JDK line:
    // // \tat class-name.method-name(Native method | file:line | file | Unknown
    // source)\n
    // // JRockit line:
    // // \tat class-name.method-name(method-signature[:???])(Native method |
    // file:line | file | Unknown source)\n
    // if (!line.startsWith("\tat ")) return null;
    // String className, methodName, fileName;
    // int lineNo;
    // // Discard 'at'
    // line = line.substring(4);
    // // Split by last '(' (because of JRockit)
    // int i = line.lastIndexOf('(');
    // if (i == -1) return null;
    // String left = line.substring(0, i);
    // String right = line.substring(i + 1);
    // // Split into class and method
    // i = left.lastIndexOf('.');
    // if (i == -1) {
    // className = left;
    // methodName = "";
    // } else {
    // className = left.substring(0, i);
    // methodName = left.substring(i + 1);
    // }
    // // Split into file and line number
    // i = right.indexOf(')');
    // if (i == -1) return null;
    // right = right.substring(0, i);
    // i = right.lastIndexOf(':');
    // if (right.startsWith("Native Method")) {
    // fileName = null;
    // lineNo = -2;
    // } else if (right.startsWith("Unknown Source")) {
    // fileName = null;
    // lineNo = -1;
    // } else if (i == -1) {
    // fileName = right;
    // lineNo = -1;
    // } else {
    // fileName = right.substring(0, i);
    // try {
    // lineNo = Integer.valueOf(right.substring(i + 1)).intValue();
    // } catch (NumberFormatException e) {
    // lineNo = -1;
    // }
    // }
    // return createStackTraceElement(className, methodName, fileName, lineNo);
    // }

    // /**
    // * Parse exception from serialized stack trace.
    // *
    // * @param s string containing stack trace
    // * @return parsed exception or null if failed
    // */
    // public static Throwable parseException(String s) {
    // if (stackTraceElementStreamPrefix == null || s == null || s.length() ==
    // 0) return null;
    // StringTokenizer tokenizer = new StringTokenizer(s, "\n\r");
    // if (!tokenizer.hasMoreTokens()) return null;
    // return parseException(tokenizer.nextToken(), tokenizer, new
    // LinkedList<Object>());
    // }

    // /**
    // * Do the real stack trace import. Called recursivelly for 'Caused by'
    // exceptions.
    // *
    // * @param firstLine first line of stack trace
    // * @param tokenizer rest of stack trace as tokenizer
    // * @param superStackTrace stack trace of wrapping exception
    // * @return parsed exception
    // */
    // private static Throwable parseException(String firstLine, StringTokenizer
    // tokenizer, List<Object> superStackTrace) {
    //
    // // Format:
    // // exc-class[: exc-message]\n
    // // \tat class-name.method-name(Native method | file:line | file | Unknown
    // source)\n
    // // \t... nn more\n
    // // Caused by: ...
    //
    // // Gather multiline message here
    // StringBuffer message = new StringBuffer();
    // message.append(firstLine);
    // boolean inMessage = true;
    // // Gather stack trace elements here
    // List<Object> list = new LinkedList<Object>();
    // // Cause here - if any
    // Throwable cause = null;
    // int numOfElementsMore = -1;
    // while (tokenizer.hasMoreTokens()) {
    // String token = tokenizer.nextToken();
    // StackTraceElement e = parseStackTraceElement(token);
    // if (e != null) {
    // // Stack trace element read - message finished
    // inMessage = false;
    // list.add(e);
    // } else if (token.startsWith("Caused by: ")) {
    // // Import cause
    // inMessage = false;
    // token = token.substring(11);
    // list.addAll(superStackTrace);
    // cause = parseException(token, tokenizer, list);
    // // End
    // break;
    // } else if (token.startsWith("\t... ")) {
    // // Seems like "... nn more"
    // // make sure the stack trace doens't contain more than the specified
    // number of elements
    // inMessage = false;
    // int index = token.indexOf(" more");
    // String numOfElementsMoreStr = token.substring(5, index);
    // numOfElementsMore = Integer.parseInt(numOfElementsMoreStr);
    // } else {
    // // Some other text - either message continuation or error (stop parsing)
    // if (inMessage) {
    // message.append('\n');
    // message.append(token);
    // } else break;
    // }
    // }
    // // Extract exception class name and message
    // int i = message.indexOf(":");
    // Throwable exception;
    // if (i == -1) exception = new ParsedException(message.toString(), null);
    // else exception = new ParsedException(message.substring(0, i),
    // message.substring(i + 2));
    // // Initialize exception
    // if (cause == null) {
    // // Super stack trace not imported yet
    // list.addAll(superStackTrace);
    // } else {
    // // Already imported when cause was created
    // exception.initCause(cause);
    // }
    //        
    // if (numOfElementsMore > -1) {
    // while (list.size() > numOfElementsMore) {
    // list.remove(list.size() - 1);
    // }
    // }
    //        
    // exception.setStackTrace(list.toArray(new
    // StackTraceElement[list.size()]));
    // return exception;
    // }

    // /**
    // * Exception parsed from stack trace. Overrides toString() method
    // * to mimic original exception by hiding own class name.
    // */
    // private static class ParsedException extends Throwable {
    // private static final long serialVersionUID = -549126552723814501L;
    //
    // private String className;
    //
    // ParsedException(String className, String message) {
    // super(message);
    // this.className = className;
    // }
    //
    // public String toString() {
    // String message = getMessage();
    // return (message != null) ? (className + ": " + message) : className;
    // }
    // }
}
