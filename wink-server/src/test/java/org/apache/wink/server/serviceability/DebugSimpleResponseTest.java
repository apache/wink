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

import java.util.Formatter;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.internal.log.Responses;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class DebugSimpleResponseTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyResource.class, MyExceptionMapper.class};
    }

    @Provider
    public static class MyExceptionMapper implements ExceptionMapper<NullPointerException> {

        public Response toResponse(NullPointerException exception) {
            return Response.ok("GOT A NPE").build();
        }

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

        @GET
        @Path("emptyvoid")
        public void emptyVoid() {

        }

        @GET
        @Path("emptynull")
        public Object emptyObj() {
            return null;
        }

        @GET
        @Path("emptyresponse")
        public Response emptyResponse() {
            return Response.noContent().build();
        }

        @GET
        @Path("thrownNPE")
        public Response NPEResponse() {
            throw new NullPointerException();
        }

        @GET
        @Path("responseWithHeaders")
        public Response getHeaders() {
            return Response.ok().header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .header(HttpHeaders.COOKIE, "abcd").header(HttpHeaders.SET_COOKIE, "lmnop")
                .header(HttpHeaders.SET_COOKIE, "abcd").header(HttpHeaders.WWW_AUTHENTICATE,
                                                               "realm=something").build();
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
     * Tests that an empty entity response is still okay.
     */
    public void testEmptyResponseEntityStillOK() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/abcd/emptyvoid",
                                                        MediaType.WILDCARD);

        MockHttpServletResponse response = invoke(request);
        assertEquals(204, response.getStatus());

        int isNoResponseEntityWrittenMsgCount = 0;
        int countNumMessagesFromResponsesHandler = 0;
        for (LogRecord record : records) {
            if ("The response entity was not written to the HttpServletResponse.getOutputStream()."
                .equals(record.getMessage())) {
                ++isNoResponseEntityWrittenMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (record.getLoggerName().equals((Responses.class.getName()))) {
                ++countNumMessagesFromResponsesHandler;
            }
        }

        assertEquals(1, isNoResponseEntityWrittenMsgCount);
        assertEquals(2, countNumMessagesFromResponsesHandler);
    }

    /**
     * Tests that an empty entity response is still okay.
     */
    public void testNullObjectStillOK() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/abcd/emptynull",
                                                        MediaType.WILDCARD);

        MockHttpServletResponse response = invoke(request);
        assertEquals(204, response.getStatus());

        int isNoResponseEntityWrittenMsgCount = 0;
        int countNumMessagesFromResponsesHandler = 0;
        for (LogRecord record : records) {
            if ("The response entity was not written to the HttpServletResponse.getOutputStream()."
                .equals(record.getMessage())) {
                ++isNoResponseEntityWrittenMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (record.getLoggerName().equals((Responses.class.getName()))) {
                ++countNumMessagesFromResponsesHandler;
            }
        }

        assertEquals(1, isNoResponseEntityWrittenMsgCount);
        assertEquals(2, countNumMessagesFromResponsesHandler);
    }

    /**
     * Tests that an empty entity response is still okay.
     */
    public void test204ResponseStillOK() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/abcd/emptyresponse",
                                                        MediaType.WILDCARD);

        MockHttpServletResponse response = invoke(request);
        assertEquals(204, response.getStatus());

        int isNoResponseEntityWrittenMsgCount = 0;
        int countNumMessagesFromResponsesHandler = 0;
        for (LogRecord record : records) {
            if ("The response entity was not written to the HttpServletResponse.getOutputStream()."
                .equals(record.getMessage())) {
                ++isNoResponseEntityWrittenMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (record.getLoggerName().equals((Responses.class.getName()))) {
                ++countNumMessagesFromResponsesHandler;
            }
        }

        assertEquals(1, isNoResponseEntityWrittenMsgCount);
        assertEquals(2, countNumMessagesFromResponsesHandler);
    }

    /**
     * Tests that a response entity is sent and output in the log.
     */
    public void testResponseEntityStringOutput() throws Exception {
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

        int responseEntityAsStringMsgCount = 0;
        int responseEntityContentAsStringMsgCount = 0;
        int countNumMessagesFromResponsesHandler = 0;
        for (LogRecord record : records) {
            if ("The response entity as a String in the default encoding:".equals(record
                .getMessage())) {
                ++responseEntityAsStringMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if ("Hello world!".equals(record.getMessage())) {
                ++responseEntityContentAsStringMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (record.getLoggerName().equals((Responses.class.getName()))) {
                ++countNumMessagesFromResponsesHandler;
            }
        }

        assertEquals(1, responseEntityAsStringMsgCount);
        assertEquals(1, responseEntityContentAsStringMsgCount);
        assertEquals(3, countNumMessagesFromResponsesHandler);
    }

    /**
     * Tests that a response entity is sent and output in the log.
     */
    public void testResponseEntityStringOutputViaException() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/abcd/thrownNPE",
                                                        MediaType.WILDCARD);

        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("GOT A NPE", response.getContentAsString());

        int responseEntityAsStringMsgCount = 0;
        int responseEntityContentAsStringMsgCount = 0;
        int countNumMessagesFromResponsesHandler = 0;
        int countErrorFlowMessage = 0;
        for (LogRecord record : records) {
            if ("The response entity as a String in the default encoding:".equals(record
                .getMessage())) {
                ++responseEntityAsStringMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if ("GOT A NPE".equals(record.getMessage())) {
                ++responseEntityContentAsStringMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (record.getLoggerName().equals((Responses.class.getName()))) {
                ++countNumMessagesFromResponsesHandler;
            }

            if ("An error occurred when handling the initial request/response, so wrote the entity and headers in the error response handlers chain."
                .equals(record.getMessage())) {
                ++countErrorFlowMessage;
                assertEquals(Level.FINE, record.getLevel());
            }
        }

        assertEquals(1, responseEntityAsStringMsgCount);
        assertEquals(1, responseEntityContentAsStringMsgCount);
        assertEquals(1, countErrorFlowMessage);
        assertEquals(4, countNumMessagesFromResponsesHandler);
    }

    /**
     * Tests that a response header is sent and output in the log.
     */
    public void testResponseHeadersOutput() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/abcd/responseWithHeaders",
                                                        MediaType.WILDCARD);

        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("", response.getContentAsString());

        int responseEntityNotWritten = 0;
        int responseEntityContentAsStringMsgCount = 0;
        int countNumMessagesFromResponsesHandler = 0;
        int countHeadersMessage = 0;
        StringBuilder headersOutput = new StringBuilder();
        Formatter f = new Formatter(headersOutput);
        f.format("The written response headers:");
        f.format("%n%1$-30s%2$s", "Content-Encoding", "gzip");
        f.format("%n%1$-30s%2$s", "Cookie", "abcd");
        f.format("%n%1$-30s%2$s", "Set-Cookie", "lmnop");
        f.format("%n%1$-30s%2$s", "Set-Cookie", "abcd");
        f.format("%n%1$-30s%2$s", "WWW-Authenticate", "realm=something");

        for (LogRecord record : records) {
            if ("The response entity was not written to the HttpServletResponse.getOutputStream()."
                .equals(record.getMessage())) {
                ++responseEntityNotWritten;
                assertEquals(Level.FINE, record.getLevel());
            }
            if ("".equals(record.getMessage())) {
                ++responseEntityContentAsStringMsgCount;
                assertEquals(Level.FINE, record.getLevel());
            }
            if (record.getLoggerName().equals((Responses.class.getName()))) {
                ++countNumMessagesFromResponsesHandler;
            }
            if (headersOutput.toString().equals(record.getMessage())) {
                ++countHeadersMessage;
                assertEquals(Level.FINE, record.getLevel());
            }
        }

        assertEquals(1, responseEntityNotWritten);
        assertEquals(0, responseEntityContentAsStringMsgCount);
        assertEquals(1, countHeadersMessage);
        assertEquals(2, countNumMessagesFromResponsesHandler);
    }
}
