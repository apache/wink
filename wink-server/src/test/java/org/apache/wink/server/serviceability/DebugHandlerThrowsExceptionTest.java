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

package org.apache.wink.server.serviceability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.log.LogUtils;
import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.HandlersFactory;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.apache.wink.server.internal.RequestProcessor;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;

/*
 * 
 * Test what is printed to the log when a resource class (from the developer's application) throws an exception.
 * An exception may be thrown on purpose from a resource as part of normal transactions for this particular app,
 * thus we don't want to report it to INFO due to the possibility of quickly filling up logs.  Instead, data is
 * logged when DEBUG is turned on, and logged only once.
 * 
 */

public class DebugHandlerThrowsExceptionTest extends MockServletInvocationTest {

    public static class MyRequestHandler implements RequestHandler {

        public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
            throw new RuntimeException("reqHandlerHR");
        }

        public void init(Properties props) {
            // no need to test what happens if init throws exception, RestServlet.init will catch and log error, and servlet will be unavailable
        }
    }

    public static class UserHandlersProvider extends HandlersFactory {

        @Override
        public List<? extends RequestHandler> getRequestHandlers() {
            return Arrays.asList(new MyRequestHandler());
        }

    }

    @Path("/hello")
    public static class MyResource {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "hello";
        }

    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyResource.class};
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
    
    public void testReqHandlerHRException() throws Exception {
        
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/hello",
                                                        MediaType.TEXT_PLAIN);
        
        try {
            invoke(request);
        } catch (Throwable t) {
            // not checking what was thrown, just want to verify logs... so...
        }
        
        // keep a count of how many logRecords contain the stack trace from the exception that originated from a handler
        int logRecordsWithStackTrace = 0;

        List<LogRecord> logRecordsContainingRequestProcessor = new ArrayList<LogRecord>();
        
        for (LogRecord record : records) {
            // record originates from RequestProcessor
            if (record.getLoggerName().equals(RequestProcessor.class.getName())) {
                if (record.getLevel().equals(Level.FINE)) {
                    logRecordsContainingRequestProcessor.add(record);
                    if ((record.getThrown() != null) && LogUtils.stackToDebugString(record.getThrown()).contains(RequestProcessor.class.getName())) {
                        logRecordsWithStackTrace++;
                    }
                }
            }
        }
        
        // stack trace of an exception that originated in a handler should only be logged once:
        assertEquals(1, logRecordsWithStackTrace);
        
        int expectedRecords = 4;
        assertEquals(expectedRecords, logRecordsContainingRequestProcessor.size());
        
        for (int i = 0; i < expectedRecords; i++) {
            assertEquals(Level.FINE, logRecordsContainingRequestProcessor.get(i).getLevel());
        }
        
        String newLine = System.getProperty("line.separator");
        
        assertEquals("Processing GET request to http://localhost:80/hello, source content type is null, acceptable media types include text/plain", logRecordsContainingRequestProcessor.get(0).getMessage());
        String expectedString = "The following error occurred during the invocation of the handlers chain: RuntimeException with message 'reqHandlerHR' while processing GET request sent to http://localhost:80/hello";
        assertTrue(logRecordsContainingRequestProcessor.get(1).getMessage().startsWith(expectedString));
        assertNotNull(logRecordsContainingRequestProcessor.get(1).getThrown());
        assertEquals("Registered JAX-RS resources: " + newLine + "  Path: hello; ClassMetadata: Class: org.apache.wink.server.serviceability.DebugHandlerThrowsExceptionTest$MyResource" + newLine + "  Path: ; ClassMetadata: Class: org.apache.wink.server.internal.resources.HtmlServiceDocumentResource", logRecordsContainingRequestProcessor.get(2).getMessage());
        assertTrue(logRecordsContainingRequestProcessor.get(3).getMessage().startsWith("The following user-defined JAX-RS providers are registered: \nRawType: interface javax.ws.rs.ext.MessageBodyReader"));
    }

    @Override
    protected String getPropertiesFile() {
        return getClass().getName().replaceAll("\\.", "/") + ".properties";
    }
    
}
