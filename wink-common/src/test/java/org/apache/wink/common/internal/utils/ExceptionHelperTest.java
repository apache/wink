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

import org.apache.wink.common.internal.utils.ExceptionHelper;

import junit.framework.TestCase;

/**
 * Test for class ExceptionHelper
 *
 * @see org.apache.wink.common.internal.utils.ExceptionHelper
 */
public class ExceptionHelperTest extends TestCase {
    private static int i = 0;
    private static final int MAX_RECURSIVE = 200;

    /**
     * Test converting Throwable message and stacktrace to String.
     * No truncating.
     */
    public void testStackTraceToStringFull() {
        // create normal exception with short stacktarce
        Throwable t = new Throwable("full stacktrace test");
        // serialize stacktrace by helper
        String testedStackTrace = ExceptionHelper.toString(t);
        // serialize stacktrace myself
        StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter));
        String expectedStackTrace = stringWriter.toString();
        assertEquals("Stack trace of Throwable is printed fully. Tested stacktrace:" + testedStackTrace,
                expectedStackTrace, testedStackTrace);
    }

    /**
     * Test converting Throwable message and stacktrace to String.
     * Check that long stacktrace gets truncated.
     */
    public void testStackTraceToStringTruncated() {
        // create long stacktrace and serialize it by helper
        Throwable t = createThrowableWithLongStacktrace();
        String testedStackTrace = ExceptionHelper.stackTraceToString(t);
        assertTrue("Stack trace of Throwable is truncated. Tested stacktrace:" + testedStackTrace,
                testedStackTrace.contains("... [ too long - truncated ]"));
    }

//    /**
//     * Execute test with parsing String with stacktrace to Throwable.
//     * Check that throwable stacktrace is identical before and after parsing
//     *
//     * @param expectedThrowable tested throwable
//     * @param message           message for assert
//     */
//    private void executeParseException(Throwable expectedThrowable, String message) {
//        // serialize throwable to string (expected string)
//        StringWriter stringWriter = new StringWriter();
//        expectedThrowable.printStackTrace(new PrintWriter(stringWriter));
//        String expectedStackTrace = stringWriter.toString();
//        // parse string to parsed throwable
//        Throwable parsedThrowable = ExceptionHelper.parseException(expectedStackTrace);
//        // serialize parsed throwable to resulting string
//        stringWriter = new StringWriter();
//        parsedThrowable.printStackTrace(new PrintWriter(stringWriter));
//        String resultStackTrace = stringWriter.toString();
//        assertEquals(message + "Tested stacktrace:\n" + resultStackTrace,
//                expectedStackTrace, resultStackTrace);
//    }

//    /**
//     * Execute test with parsing String with erroneous stacktrace to Throwable.
//     * Check that throwable stacktrace is identical to expected result
//     *
//     * @param inputStackTrace
//     * @param expectedStackTrace
//     * @param message
//     */
//    private void executeParseExceptionErroneous(String inputStackTrace, String expectedStackTrace, String message, int limit) {
//        Throwable resultThrowable = ExceptionHelper.parseException(inputStackTrace);
//        // serialize parsed throwable to tested string
//        StringWriter resultStringWriter = new StringWriter();
//        resultThrowable.printStackTrace(new PrintWriter(resultStringWriter));
//        String actualStackTrace = resultStringWriter.toString();
//
//        // compare result with expected string regardless line separator
//        Object[] expectedTokenArray = createTokenArray(expectedStackTrace);
//        Object[] actualTokenArray = createTokenArray(actualStackTrace);
//
//        assertEquals(message + "(Number of lines is identical)", expectedTokenArray.length, actualTokenArray.length);
//        assertTrue(message + " (Expected and actual string is not empty)",
//                ((String) expectedTokenArray[0]).length() > 0 && ((String) actualTokenArray[0]).length() > 0);
//        for (int i = 0; i < expectedTokenArray.length && (i < limit || i == -1); i++) {
//            assertEquals(message + " (Strings are identical regardless line separator):\n" + expectedStackTrace,
//                    (String) expectedTokenArray[i], (String) actualTokenArray[i]);
//        }
//    }
//
//    private Object[] createTokenArray(String stackTrace) {
//        StringTokenizer tokenizer = new StringTokenizer(stackTrace, "\n\r");
//        List<String> tokenList = new ArrayList<String>();
//        while (tokenizer.hasMoreElements()) {
//            String token = tokenizer.nextToken();
//            tokenList.add(token);
//        }
//        return tokenList.toArray();
//    }

//    /**
//     * Test parsing String of stacktrace to Throwable.
//     * Check that throwable stacktrace is identical before and after parsing
//     */
//    public void testParseException() {
//        // create throwable
//        Throwable expectedThrowable = new Throwable("parse exception test");
//        executeParseException(expectedThrowable, "Stack trace of Throwable is same before and after parsing.\n");
//    }

//    /**
//     * Test parsing String of stacktrace to Throwable with caused by throwable.
//     * Check that throwable stacktrace is identical before and after parsing
//     */
//    public void testParseExceptionCausedBy() {
//        // create throwable
//        Throwable expectedThrowable = new Throwable("parse exception test",
//                new Throwable("caused by test", new Throwable("caused by test")));
//        executeParseException(expectedThrowable, "Stack trace of Throwable with cause is same before and after parsing.\n");
//    }

//    /**
//     * Test parsing String of various (including erroneous) stacktrace to Throwable.
//     * Check that problem is handled in parsing.
//     */
//    public void testParseExceptionError() {
//        // test missing left bracket
//        String inputStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionExceptionHelperTest.java:75)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        String expectedStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n";
//        executeParseExceptionErroneous(inputStackTrace, expectedStackTrace, "Missing left bracket", -1);
//
//        // test missing right bracket
//        inputStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:75\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        expectedStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n";
//        executeParseExceptionErroneous(inputStackTrace, expectedStackTrace, "Missing right bracket", -1);
//
//        // test preceding line
//        inputStackTrace = "bla\n" +
//                "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:75)\n";
//        expectedStackTrace = "bla\n" +
//                "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:75)\n";
//        executeParseExceptionErroneous(inputStackTrace, expectedStackTrace, "Preceding line", -1);
//
//        // test empty preceding line
//        inputStackTrace = "\t\n" +
//                "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:75)\n";
//        expectedStackTrace = "\t\n" +
//                "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:75)\n";
//        executeParseExceptionErroneous(inputStackTrace, expectedStackTrace, "Empty preceding line", -1);
//
//        // test Unknown Source
//        inputStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(Unknown Source)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        expectedStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(Unknown Source)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        executeParseExceptionErroneous(inputStackTrace, expectedStackTrace, "Unknown Source", -1);
//
//        // test Native Method
//        inputStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(Native Method)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        expectedStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(Native Method)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        executeParseExceptionErroneous(inputStackTrace, expectedStackTrace, "Native Method", -1);
//
//        // test Nonsense method
//        inputStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(Nonsense)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        expectedStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(Nonsense)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        executeParseExceptionErroneous(inputStackTrace, expectedStackTrace, "Nonsense method", -1);
//
//        // test missing dots
//        inputStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat testParseException(HelperTest:75)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        expectedStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat testParseException.(HelperTest:75)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        executeParseExceptionErroneous(inputStackTrace, expectedStackTrace, "Missing dots", -1);
//
//        // test badly formated line number
//        inputStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:7a5)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        expectedStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        executeParseExceptionErroneous(inputStackTrace, expectedStackTrace, "Badly formated line number", -1);
//
//        // test missing colon
//        inputStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java75)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        expectedStackTrace = "java.lang.Throwable: parse exception test\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:74)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java75)\n" +
//                "\tat com.hp.systinet.rest.util.ExceptionHelperTest.testParseExceptionException(HelperTest.java:76)\n";
//        executeParseExceptionErroneous(inputStackTrace, expectedStackTrace, "Missing colon", -1);
//
//        // test javadocs stacktrace
//        inputStackTrace = "HighLevelException: MidLevelException: LowLevelException\n" +
//                "\tat Junk.a(Junk.java:13)\n" +
//                "\tat Junk.main(Junk.java:4)\n" +
//                "Caused by: MidLevelException: LowLevelException\n" +
//                "\tat Junk.c(Junk.java:23)\n" +
//                "\tat Junk.b(Junk.java:17)\n" +
//                "\tat Junk.a(Junk.java:11)\n" +
//                "\t... 1 more\n" +
//                "Caused by: LowLevelException\n" +
//                "\tat Junk.e(Junk.java:30)\n" +
//                "\tat Junk.d(Junk.java:27)\n" +
//                "\tat Junk.c(Junk.java:21)\n" +
//                "\t... 3 more\n";
//        expectedStackTrace = "HighLevelException: MidLevelException: LowLevelException\n" +
//                "\tat Junk.a(Junk.java:13)\n" +
//                "\tat Junk.main(Junk.java:4)\n" +
//                "Caused by: MidLevelException: LowLevelException\n" +
//                "\tat Junk.c(Junk.java:23)\n" +
//                "\tat Junk.b(Junk.java:17)\n" +
//                "\tat Junk.a(Junk.java:11)\n" +
//                "\t... 1 more\n" +
//                "Caused by: LowLevelException\n" +
//                "\tat Junk.e(Junk.java:30)\n" +
//                "\tat Junk.d(Junk.java:27)\n" +
//                "\tat Junk.c(Junk.java:21)\n" +
//                "\t... 3 more\n";
//        // Known issue: test is limitted to checking first 7 lines only. There is problem with printStackTrace of generated
//        // Throwable, since values in "... x more" line differ from expected
//        executeParseExceptionErroneous(inputStackTrace, expectedStackTrace, "Javadocs stacktrace", 7);
//
//        // test for onecharacter string
//        inputStackTrace = "n";
//        expectedStackTrace = "n";
//        executeParseExceptionErroneous(inputStackTrace, expectedStackTrace, "Onecharacter string", -1);
//
//        // test for null input
//        Throwable nullThrowable = ExceptionHelper.parseException(null);
//        assertNull("Null input for parsing", nullThrowable);
//
//        // test for empty input
//        Throwable emptyThrowable = ExceptionHelper.parseException("");
//        assertNull("Empty input for parsing", emptyThrowable);
//    }

    /**
     * Creates Throwable with a long stacktrace by calling itself recursively
     *
     * @return long stacktrace
     */
    private Throwable createThrowableWithLongStacktrace() {
        Throwable t;
        if (i < MAX_RECURSIVE) {
            i++;
            t = createThrowableWithLongStacktrace();
        } else {
            t = new Throwable("long stacktrace test");
        }
        i = 0;
        return t;
    }
}
