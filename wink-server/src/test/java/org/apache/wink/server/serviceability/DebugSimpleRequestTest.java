/*
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
 */
package org.apache.wink.server.serviceability;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.internal.log.Requests;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class DebugSimpleRequestTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyResource.class};
    }

    @Path("abcd")
    public static class MyResource {

        @GET
        public String get() {
            return "Hello world GET!";
        }

        @POST
        public String get(String entity) {
            return "Hello world!";
        }
    }

    private InMemoryHandler handler;

    private Logger          winkLogger = Logger.getLogger("org.apache.wink");

    private Handler         consoleHandler;

    @Override
    protected void setUp() throws Exception {
        Handler[] defaultHandlers = Logger.getLogger("").getHandlers();
        if (defaultHandlers.length == 1) {
            consoleHandler = defaultHandlers[0];
            consoleHandler.setLevel(Level.FINE);
        }

        handler = new InMemoryHandler();
        handler.setLevel(Level.FINE);

        winkLogger.setLevel(Level.FINE);
        winkLogger.addHandler(handler);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        if (consoleHandler != null) {
            consoleHandler.setLevel(Level.INFO);
        }
        winkLogger.removeHandler(handler);
        winkLogger.setLevel(Level.INFO);
        super.tearDown();
    }

    /**
     * Tests that an empty entity request is still okay.
     */
    public void testEmptyRequestEntityStillOK() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        StringBuffer sb = new StringBuffer();
        for (int c = 0; c < 1000; ++c) {
            sb.append("Hello world!  Goodbye!");
        }
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/abcd",
                                                        MediaType.WILDCARD,
                                                        MediaType.TEXT_PLAIN,
                                                        new byte[] {});

        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("Hello world!", response.getContentAsString());

        int isNoRequestEntityReadMsgCount = 0;
        int countNumMessagesFromRequestsHandler = 0;
        for (LogRecord record : records) {
            if ("The request entity was not read from the HttpServletRequest.getInputStream()."
                .equals(record.getMessage())) {
                ++isNoRequestEntityReadMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (record.getLoggerName().equals((Requests.class.getName()))) {
                ++countNumMessagesFromRequestsHandler;
            }
        }

        assertEquals(1, isNoRequestEntityReadMsgCount);
        assertEquals(3, countNumMessagesFromRequestsHandler);
    }

    /**
     * Tests that an empty entity request is still okay.
     */
    public void testNoRequestEntityExpectedStillOK() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        StringBuffer sb = new StringBuffer();
        for (int c = 0; c < 1000; ++c) {
            sb.append("Hello world!  Goodbye!");
        }
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/abcd",
                                                        MediaType.WILDCARD,
                                                        MediaType.TEXT_PLAIN,
                                                        new byte[] {});

        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("Hello world GET!", response.getContentAsString());

        int isNoRequestEntityReadMsgCount = 0;
        int countNumMessagesFromRequestsHandler = 0;
        for (LogRecord record : records) {
            if ("The request entity was not read from the HttpServletRequest.getInputStream()."
                .equals(record.getMessage())) {
                ++isNoRequestEntityReadMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (record.getLoggerName().equals((Requests.class.getName()))) {
                ++countNumMessagesFromRequestsHandler;
            }
        }

        assertEquals(1, isNoRequestEntityReadMsgCount);
        assertEquals(3, countNumMessagesFromRequestsHandler);
    }

    /**
     * Tests that a request is sent but it is never read still works.
     */
    public void testRequestEntityNeverReadBecauseNoEntityParam() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        StringBuffer sb = new StringBuffer();
        for (int c = 0; c < 1000; ++c) {
            sb.append("Hello world!  Goodbye!");
        }
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/abcd",
                                                        MediaType.WILDCARD,
                                                        MediaType.TEXT_PLAIN,
                                                        "Hello world!".getBytes());

        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("Hello world GET!", response.getContentAsString());

        int isNoRequestEntityReadMsgCount = 0;
        int countNumMessagesFromRequestsHandler = 0;
        for (LogRecord record : records) {
            if ("The request entity was not read from the HttpServletRequest.getInputStream()."
                .equals(record.getMessage())) {
                ++isNoRequestEntityReadMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (record.getLoggerName().equals((Requests.class.getName()))) {
                ++countNumMessagesFromRequestsHandler;
            }
        }

        assertEquals(1, isNoRequestEntityReadMsgCount);
        assertEquals(3, countNumMessagesFromRequestsHandler);
    }

    /**
     * Tests that a request entity is sent and output in the log.
     */
    public void testRequestEntityStringOutput() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/abcd",
                                                        MediaType.WILDCARD,
                                                        MediaType.TEXT_PLAIN,
                                                        "Hello this is the request!".getBytes());

        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("Hello world!", response.getContentAsString());

        int requestEntityAsStringMsgCount = 0;
        int requestEntityContentAsStringMsgCount = 0;
        int countNumMessagesFromRequestsHandler = 0;
        for (LogRecord record : records) {
            if ("The request entity as a String in the default encoding:".equals(record
                .getMessage())) {
                ++requestEntityAsStringMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if ("Hello this is the request!".equals(record.getMessage())) {
                ++requestEntityContentAsStringMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (record.getLoggerName().equals((Requests.class.getName()))) {
                ++countNumMessagesFromRequestsHandler;
            }
        }

        assertEquals(1, requestEntityAsStringMsgCount);
        assertEquals(1, requestEntityContentAsStringMsgCount);
        assertEquals(4, countNumMessagesFromRequestsHandler);
    }

    /**
     * Tests that a request entity is sent and output in the log.
     */
    public void testMultipleRequestEntityStringOutput() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        for (int c = 0; c < 100; ++c) {
            MockHttpServletRequest request =
                MockRequestConstructor
                    .constructMockRequest("POST",
                                          "/abcd",
                                          MediaType.WILDCARD,
                                          MediaType.TEXT_PLAIN,
                                          "Hello this is the request!".getBytes());

            MockHttpServletResponse response = invoke(request);
            assertEquals(200, response.getStatus());
            assertEquals("Hello world!", response.getContentAsString());
        }

        int requestEntityAsStringMsgCount = 0;
        int requestEntityContentAsStringMsgCount = 0;
        int countNumMessagesFromRequestsHandler = 0;
        for (LogRecord record : records) {
            if ("The request entity as a String in the default encoding:".equals(record
                .getMessage())) {
                ++requestEntityAsStringMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if ("Hello this is the request!".equals(record.getMessage())) {
                ++requestEntityContentAsStringMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (record.getLoggerName().equals((Requests.class.getName()))) {
                ++countNumMessagesFromRequestsHandler;
            }
        }

        assertEquals(100, requestEntityAsStringMsgCount);
        assertEquals(100, requestEntityContentAsStringMsgCount);
        assertEquals(400, countNumMessagesFromRequestsHandler);
    }

    /**
     * Tests that a request entity is broken up into sizable page breaks. This
     * is mainly to flush out the buffer. (i.e. someone sending 1GB of data will
     * not like us taking 1GB of memory to hold the string).
     */
    public void testPageBreak() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        StringBuffer sb = new StringBuffer();
        /*
         * 1 million characters but will only log up to 8192 total in 4096
         * blocks by default
         */
        for (int c = 0; c < 100000; ++c) {
            sb.append("1234567890");
        }

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/abcd",
                                                        MediaType.WILDCARD,
                                                        MediaType.TEXT_PLAIN,
                                                        sb.toString().getBytes());

        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("Hello world!", response.getContentAsString());

        int requestEntityAsStringMsgCount = 0;
        int requestEntityContentAsStringMsgCount = 0;
        int requestEntityContentAsStringMsg2Count = 0;
        int countNumMessagesFromRequestsHandler = 0;
        StringBuffer expected = new StringBuffer();
        for (int c = 1; c < 4097; ++c) {
            expected.append(c % 10);
        }

        StringBuffer expected2 = new StringBuffer();
        for (int c = 4097; c < 8193; ++c) {
            expected2.append(c % 10);
        }

        for (LogRecord record : records) {
            if ("The request entity as a String in the default encoding:".equals(record
                .getMessage())) {
                ++requestEntityAsStringMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (expected.toString().equals(record.getMessage())) {
                ++requestEntityContentAsStringMsgCount;
                assertEquals(Level.FINE, record.getLevel());
                assertEquals(0, requestEntityContentAsStringMsg2Count);
            }
            if (expected2.toString().equals(record.getMessage())) {
                ++requestEntityContentAsStringMsg2Count;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (record.getLoggerName().equals((Requests.class.getName()))) {
                ++countNumMessagesFromRequestsHandler;
            }
        }

        assertEquals(1, requestEntityAsStringMsgCount);
        assertEquals(1, requestEntityContentAsStringMsgCount);
        assertEquals(1, requestEntityContentAsStringMsg2Count);
        assertEquals(5, countNumMessagesFromRequestsHandler);
    }
}
