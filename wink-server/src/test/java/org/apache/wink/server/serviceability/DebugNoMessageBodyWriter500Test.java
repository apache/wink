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
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.dom.DOMSource;

import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class DebugNoMessageBodyWriter500Test extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyResource.class, MyResourceMediaType.class};
    }

    public static class MyObject {

    }

    @Path("noWriterForJavaType")
    public static class MyResource {
        @GET
        public Object get() {
            return new MyObject();
        }
    }

    @Path("noWriterForMediaType")
    public static class MyResourceMediaType {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Object get() {
            return new DOMSource();
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

    public void testLogNoWriterForJavaType() throws Exception {
        List<LogRecord> records = handler.getRecords();

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "noWriterForJavaType",
                                                        MediaType.WILDCARD);
        MockHttpServletResponse response = invoke(request);
        assertEquals(500, response.getStatus());
        assertEquals("", response.getContentAsString());

        assertEquals(Level.SEVERE, records.get(21).getLevel());
        assertTrue(records
            .get(21)
            .getMessage()
            .indexOf("The system could not find a javax.ws.rs.ext.MessageBodyWriter or a DataSourceProvider class for the org.apache.wink.server.serviceability.DebugNoMessageBodyWriter500Test$MyObject type and") != -1 && records
            .get(21)
            .getMessage()
            .indexOf("mediaType.  Ensure that a javax.ws.rs.ext.MessageBodyWriter exists in the JAX-RS application for the type and media type specified.") != -1);

        assertEquals(30, records.size());
    }
//
//    public void testLogNoWriterForMediaType() throws Exception {
//        List<LogRecord> records = handler.getRecords();
//
//        MockHttpServletRequest request =
//            MockRequestConstructor.constructMockRequest("GET",
//                                                        "noWriterForMediaType",
//                                                        MediaType.WILDCARD);
//        MockHttpServletResponse response = invoke(request);
//        assertEquals(500, response.getStatus());
//        assertEquals("", response.getContentAsString());
//
//        assertEquals(Level.SEVERE, records.get(5).getLevel());
//        assertEquals("The system could not find a javax.ws.rs.ext.MessageBodyWriter or a DataSourceProvider class for the javax.xml.transform.dom.DOMSource type and application/json mediaType.  Ensure that a javax.ws.rs.ext.MessageBodyWriter exists in the JAX-RS application for the type and media type specified.",
//                     records.get(5).getMessage());
//
//        assertEquals(7, records.size());
//    }

}
