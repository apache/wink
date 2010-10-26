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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.internal.log.LogUtils;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.internal.RequestProcessor;
import org.apache.wink.server.internal.handlers.FlushResultHandler;
import org.apache.wink.server.internal.handlers.InvokeMethodHandler;
import org.apache.wink.server.internal.registry.ServerInjectableFactory;
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

public class DebugProviderThrowsExceptionTest extends MockServletInvocationTest {

    enum BAD_METHOD {
        isReadable,
        readFrom,
        getSize,
        isWriteable,
        writeTo,
    }
    
    static BAD_METHOD whichMethod;
    
    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyResource.class, MyProvider.class};
    }
    
    @Path("/root")
    public static class MyResource {

        @POST
        public String post(String input) {
            return "";
        }
        
    }

    @Provider
    public static class MyProvider implements MessageBodyReader<String>, MessageBodyWriter<String> {

        public boolean isReadable(Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            if (whichMethod == BAD_METHOD.isReadable) {
                throw new RuntimeException("isReadable");
            }
            return true;
        }

        public String readFrom(Class<String> type, Type genericType,
                Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            if (whichMethod == BAD_METHOD.readFrom) { 
                throw new RuntimeException("readFrom");
            }
            return "readFrom";
        }

        public long getSize(String t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            if (whichMethod == BAD_METHOD.getSize) { 
                throw new RuntimeException("getSize");
            }
            return -1;
        }

        public boolean isWriteable(Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            if (whichMethod == BAD_METHOD.isWriteable) {
                throw new RuntimeException("isWriteable");
            }
            return true;
        }

        public void writeTo(String t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                throws IOException {
            if (whichMethod == BAD_METHOD.writeTo) { 
                throw new RuntimeException("writeTo");
            }
            entityStream.write("writeTo".getBytes());
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
    
    public void testIsReadableException() throws Exception {
        whichMethod = BAD_METHOD.isReadable;
        
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/root",
                                                        MediaType.TEXT_PLAIN,
                                                        MediaType.TEXT_PLAIN, "data".getBytes());
        
        try {
            invoke(request);
        } catch (Throwable t) {
            // not checking what was thrown, just want to verify logs... so...
        }
        
        // keep a count of how many logRecords contain the stack trace from the exception that originated from a provider
        int logRecordsWithStackTrace = 0;

        List<LogRecord> logRecordsContainingProvidersRegistry = new ArrayList<LogRecord>();
        
        for (LogRecord record : records) {
            // record does not originate from ProvidersRegistry
            if (record.getMessage().contains(ProvidersRegistry.class.getName())) {
                logRecordsContainingProvidersRegistry.add(record);
                logRecordsWithStackTrace++;
            } else if ((record.getThrown() != null) && LogUtils.stackToDebugString(record.getThrown()).contains(InvokeMethodHandler.class.getName())) {
                logRecordsWithStackTrace++;
            }
        }
        
        // stack trace of an exception that originated in a resource should only be logged once:
        assertEquals(1, logRecordsWithStackTrace);
        
        assertEquals(1, logRecordsContainingProvidersRegistry.size());
        assertEquals(Level.FINE, logRecordsContainingProvidersRegistry.get(0).getLevel());
        String expectedString = "java.lang.RuntimeException with message \"isReadable\" was encountered during invocation of method org.apache.wink.server.serviceability.DebugProviderThrowsExceptionTest$MyProvider.isReadable( class java.lang.String, class java.lang.String, ";
        assertTrue(logRecordsContainingProvidersRegistry.get(0).getMessage().startsWith(expectedString));
        assertTrue(logRecordsContainingProvidersRegistry.get(0).getLoggerName().equals(RequestProcessor.class.getName()));  // make sure the RequestProcessor is who logged this record
        assertTrue(logRecordsContainingProvidersRegistry.get(0).getMessage().contains("\tDEBUG_FRAME = "));  // make sure we have a stack printed with "DEBUG_FRAME = " instead of " at "
    }
    
    public void testReadFromException() throws Exception {
        whichMethod = BAD_METHOD.readFrom;
        
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/root",
                                                        MediaType.TEXT_PLAIN,
                                                        MediaType.TEXT_PLAIN, "data".getBytes());
        
        try {
            invoke(request);
        } catch (Throwable t) {
            // not checking what was thrown, just want to verify logs... so...
        }
        
        // keep a count of how many logRecords contain the stack trace from the exception that originated from a provider
        int logRecordsWithStackTrace = 0;

        List<LogRecord> logRecordsContainingServerInjectableFactory = new ArrayList<LogRecord>();
        
        for (LogRecord record : records) {
            // record does not originate from ServerInjectableFactory
            if (record.getMessage().contains(ServerInjectableFactory.class.getName())) {
                logRecordsContainingServerInjectableFactory.add(record);
                logRecordsWithStackTrace++;
            } else if ((record.getThrown() != null) && LogUtils.stackToDebugString(record.getThrown()).contains(InvokeMethodHandler.class.getName())) {
                logRecordsWithStackTrace++;
            }
        }
        
        // stack trace of an exception that originated in a resource should only be logged once:
        assertEquals(1, logRecordsWithStackTrace);
        
        assertEquals(1, logRecordsContainingServerInjectableFactory.size());
        assertEquals(Level.FINE, logRecordsContainingServerInjectableFactory.get(0).getLevel());
        String expectedString = "java.lang.RuntimeException with message \"readFrom\" was encountered during invocation of method org.apache.wink.server.serviceability.DebugProviderThrowsExceptionTest$MyProvider.readFrom( class java.lang.String, class java.lang.String, ";
        assertTrue(logRecordsContainingServerInjectableFactory.get(0).getMessage().startsWith(expectedString));
        assertTrue(logRecordsContainingServerInjectableFactory.get(0).getLoggerName().equals(RequestProcessor.class.getName()));  // make sure the RequestProcessor is who logged this record
        assertTrue(logRecordsContainingServerInjectableFactory.get(0).getMessage().contains("\tDEBUG_FRAME = "));  // make sure we have a stack printed with "DEBUG_FRAME = " instead of " at "
    }
    
    public void testGetSizeException() throws Exception {
        whichMethod = BAD_METHOD.getSize;
        
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/root",
                                                        MediaType.TEXT_PLAIN,
                                                        MediaType.TEXT_PLAIN, "data".getBytes());
        
        try {
            invoke(request);
        } catch (Throwable t) {
            // not checking what was thrown, just want to verify logs... so...
        }
        
        // keep a count of how many logRecords contain the stack trace from the exception that originated from a provider
        int logRecordsWithStackTrace = 0;

        List<LogRecord> logRecordsContainingFlushResultHandler = new ArrayList<LogRecord>();
        
        for (LogRecord record : records) {
            // record does not originate from FlushResultHandler
            if (record.getMessage().contains(FlushResultHandler.class.getName())) {
                logRecordsContainingFlushResultHandler.add(record);
                logRecordsWithStackTrace++;
            } else if ((record.getThrown() != null) && LogUtils.stackToDebugString(record.getThrown()).contains(InvokeMethodHandler.class.getName())) {
                logRecordsWithStackTrace++;
            }
        }
        
        // stack trace of an exception that originated in a resource should only be logged once:
        assertEquals(1, logRecordsWithStackTrace);
        
        assertEquals(1, logRecordsContainingFlushResultHandler.size());
        assertEquals(Level.FINE, logRecordsContainingFlushResultHandler.get(0).getLevel());
        String expectedString = "java.lang.RuntimeException with message \"getSize\" was encountered during invocation of method org.apache.wink.server.serviceability.DebugProviderThrowsExceptionTest$MyProvider.getSize( ";
        assertTrue(logRecordsContainingFlushResultHandler.get(0).getMessage().startsWith(expectedString));
        assertTrue(logRecordsContainingFlushResultHandler.get(0).getLoggerName().equals(RequestProcessor.class.getName()));  // make sure the RequestProcessor is who logged this record
        assertTrue(logRecordsContainingFlushResultHandler.get(0).getMessage().contains("\tDEBUG_FRAME = "));  // make sure we have a stack printed with "DEBUG_FRAME = " instead of " at "
    }
    
    public void testIsWriteableException() throws Exception {
        whichMethod = BAD_METHOD.isWriteable;
        
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/root",
                                                        MediaType.TEXT_PLAIN,
                                                        MediaType.TEXT_PLAIN, "data".getBytes());
        
        try {
            invoke(request);
        } catch (Throwable t) {
            // not checking what was thrown, just want to verify logs... so...
        }
        
        // keep a count of how many logRecords contain the stack trace from the exception that originated from a provider
        int logRecordsWithStackTrace = 0;

        List<LogRecord> logRecordsContainingProvidersRegistry = new ArrayList<LogRecord>();
        
        for (LogRecord record : records) {
            // record does not originate from ProvidersRegistry
            if (record.getMessage().contains(ProvidersRegistry.class.getName())) {
                logRecordsContainingProvidersRegistry.add(record);
                logRecordsWithStackTrace++;
            } else if ((record.getThrown() != null) && LogUtils.stackToDebugString(record.getThrown()).contains(InvokeMethodHandler.class.getName())) {
                logRecordsWithStackTrace++;
            }
        }
        
        // stack trace of an exception that originated in a resource should only be logged once:
        assertEquals(1, logRecordsWithStackTrace);
        
        assertEquals(1, logRecordsContainingProvidersRegistry.size());
        assertEquals(Level.FINE, logRecordsContainingProvidersRegistry.get(0).getLevel());
        String expectedString = "java.lang.RuntimeException with message \"isWriteable\" was encountered during invocation of method org.apache.wink.server.serviceability.DebugProviderThrowsExceptionTest$MyProvider.isWriteable( class java.lang.String, class java.lang.String, ";
        assertTrue(logRecordsContainingProvidersRegistry.get(0).getMessage().startsWith(expectedString));
        assertTrue(logRecordsContainingProvidersRegistry.get(0).getLoggerName().equals(RequestProcessor.class.getName()));  // make sure the RequestProcessor is who logged this record
        assertTrue(logRecordsContainingProvidersRegistry.get(0).getMessage().contains("\tDEBUG_FRAME = "));  // make sure we have a stack printed with "DEBUG_FRAME = " instead of " at "
    }
    
    public void testWriteToException() throws Exception {
        whichMethod = BAD_METHOD.writeTo;
        
        handler.getRecords().clear();
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/root",
                                                        MediaType.TEXT_PLAIN,
                                                        MediaType.TEXT_PLAIN, "data".getBytes());
        
        try {
            invoke(request);
        } catch (Throwable t) {
            // not checking what was thrown, just want to verify logs... so...
        }
        
        // keep a count of how many logRecords contain the stack trace from the exception that originated from a provider
        int logRecordsWithStackTrace = 0;

        List<LogRecord> logRecordsContainingFlushResultHandler = new ArrayList<LogRecord>();
        
        for (LogRecord record : records) {
            // record does not originate from FlushResultHandler
            if (record.getMessage().contains(FlushResultHandler.class.getName())) {
                logRecordsContainingFlushResultHandler.add(record);
                logRecordsWithStackTrace++;
            } else if ((record.getThrown() != null) && LogUtils.stackToDebugString(record.getThrown()).contains(InvokeMethodHandler.class.getName())) {
                logRecordsWithStackTrace++;
            }
        }
        
        // stack trace of an exception that originated in a resource should only be logged once:
        assertEquals(1, logRecordsWithStackTrace);
        
        assertEquals(1, logRecordsContainingFlushResultHandler.size());
        assertEquals(Level.FINE, logRecordsContainingFlushResultHandler.get(0).getLevel());
        String expectedString = "java.lang.RuntimeException with message \"writeTo\" was encountered during invocation of method org.apache.wink.server.serviceability.DebugProviderThrowsExceptionTest$MyProvider.writeTo( ";
        assertTrue(logRecordsContainingFlushResultHandler.get(0).getMessage().startsWith(expectedString));
        assertTrue(logRecordsContainingFlushResultHandler.get(0).getLoggerName().equals(RequestProcessor.class.getName()));  // make sure the RequestProcessor is who logged this record
        assertTrue(logRecordsContainingFlushResultHandler.get(0).getMessage().contains("\tDEBUG_FRAME = "));  // make sure we have a stack printed with "DEBUG_FRAME = " instead of " at "
    }
    
}
