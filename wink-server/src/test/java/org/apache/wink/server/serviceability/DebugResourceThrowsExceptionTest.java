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
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.log.LogUtils;
import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.internal.RequestProcessor;
import org.apache.wink.server.internal.handlers.InvokeMethodHandler;
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

public class DebugResourceThrowsExceptionTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyResource.class};
    }

    @Path("/root")
    public static class MyResource {

        @GET
        @Path("getRuntimeException")
        public String getruntimeException() {
            throw new NullPointerException("something was null");
        }
        
        @GET
        @Path("getwebappException")
        public String getwebappexEmpty() {
            throw new javax.ws.rs.WebApplicationException(new NullPointerException("nully null null"));
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
    
    public void testRuntimeException() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/getRuntimeException",
                                                        MediaType.WILDCARD,
                                                        MediaType.TEXT_PLAIN, null);
        
        try {
            invoke(request);
        } catch (Throwable t) {
            // not checking what was thrown, just want to verify logs... so...
        }
        
        // keep a count of how many logRecords contain the stack trace from the exception that originated from a resource
        int logRecordsWithStackTrace = 0;

        List<LogRecord> logRecordsContainingInvokeMethodHandler = new ArrayList<LogRecord>();
        
        for (LogRecord record : records) {
            // record does not originate from InvokeMethodHandler
            if (record.getMessage().contains(InvokeMethodHandler.class.getName())) {
                logRecordsContainingInvokeMethodHandler.add(record);
                logRecordsWithStackTrace++;
            } else if ((record.getThrown() != null) && LogUtils.stackToDebugString(record.getThrown()).contains(InvokeMethodHandler.class.getName())) {
                logRecordsWithStackTrace++;
            }
        }
        
        // stack trace of an exception that originated in a resource will be logged in RequestProcessor.logException only
        assertEquals(1, logRecordsWithStackTrace);
        
        assertEquals(1, logRecordsContainingInvokeMethodHandler.size());
        assertEquals(Level.FINE, logRecordsContainingInvokeMethodHandler.get(0).getLevel());
        String expectedString = "java.lang.NullPointerException with message \"something was null\" was encountered during invocation of method"
                + " getruntimeException of declaring class org.apache.wink.server.serviceability.DebugResourceThrowsExceptionTest$MyResource";
        assertTrue(logRecordsContainingInvokeMethodHandler.get(0).getMessage().startsWith(expectedString));
        assertTrue(logRecordsContainingInvokeMethodHandler.get(0).getLoggerName().equals(RequestProcessor.class.getName()));  // make sure the RequestProcessor is who logged this record
        assertTrue(logRecordsContainingInvokeMethodHandler.get(0).getMessage().contains(" with parameters "));  // make sure more info is spit out under debug
        assertTrue(logRecordsContainingInvokeMethodHandler.get(0).getMessage().contains("\tDEBUG_FRAME = "));  // make sure we have a stack printed with "DEBUG_FRAME = " instead of " at "
    }
    
    public void testWebAppException() throws Exception {
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/getwebappException",
                                                        MediaType.WILDCARD,
                                                        MediaType.TEXT_PLAIN, null);
        
        try {
            invoke(request);
        } catch (Throwable t) {
            // not checking what was thrown, just want to verify logs... so...
        }
        
        // keep a count of how many logRecords contain the stack trace from the exception that originated from a resource
        int logRecordsWithStackTrace = 0;

        List<LogRecord> logRecordsContainingInvokeMethodHandler = new ArrayList<LogRecord>();
        
        for (LogRecord record : records) {
            if (record.getMessage().contains(InvokeMethodHandler.class.getName())) {
                logRecordsContainingInvokeMethodHandler.add(record);
                logRecordsWithStackTrace++;
            } else if ((record.getThrown() != null) && LogUtils.stackToDebugString(record.getThrown()).contains(InvokeMethodHandler.class.getName())) {
                logRecordsWithStackTrace++;
            }
        }
        
        // stack trace of an exception that originated in a resource should only be logged once:
        assertEquals(1, logRecordsWithStackTrace);
        
        assertEquals(1, logRecordsContainingInvokeMethodHandler.size());
        assertEquals(Level.FINE, logRecordsContainingInvokeMethodHandler.get(0).getLevel());
        String expectedString = "javax.ws.rs.WebApplicationException with message \"java.lang.NullPointerException: nully null null\" was encountered during invocation of method"
                + " getwebappexEmpty of declaring class org.apache.wink.server.serviceability.DebugResourceThrowsExceptionTest$MyResource";
        assertTrue(logRecordsContainingInvokeMethodHandler.get(0).getMessage().startsWith(expectedString));
        assertTrue(logRecordsContainingInvokeMethodHandler.get(0).getLoggerName().equals(RequestProcessor.class.getName()));  // make sure the RequestProcessor is who logged this record
        assertTrue(logRecordsContainingInvokeMethodHandler.get(0).getMessage().contains(" with parameters "));  // make sure more info is spit out under debug
        assertTrue(logRecordsContainingInvokeMethodHandler.get(0).getMessage().contains("\tDEBUG_FRAME = "));  // make sure we have a stack printed with "DEBUG_FRAME = " instead of " at "
    }
    
}
