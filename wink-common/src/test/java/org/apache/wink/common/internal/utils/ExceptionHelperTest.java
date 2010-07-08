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

import junit.framework.TestCase;

/**
 * Test for class ExceptionHelper
 * 
 * @see org.apache.wink.common.internal.utils.ExceptionHelper
 */
public class ExceptionHelperTest extends TestCase {
    private static int       i             = 0;
    private static final int MAX_RECURSIVE = 200;

    /**
     * Test converting Throwable message and stacktrace to String. No
     * truncating.
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
                     expectedStackTrace,
                     testedStackTrace);
    }

    /**
     * Test converting Throwable message and stacktrace to String. Check that
     * long stacktrace gets truncated.
     */
    public void testStackTraceToStringTruncated() {
        // create long stacktrace and serialize it by helper
        Throwable t = createThrowableWithLongStacktrace();
        String testedStackTrace = ExceptionHelper.stackTraceToString(t);
        assertTrue("Stack trace of Throwable is truncated. Tested stacktrace:" + testedStackTrace,
                   testedStackTrace.contains("... [ too long - truncated ]"));
    }

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
