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
 *  
 */
package org.apache.wink.server.serviceability;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;

/**
 * Tests the logger at debug level for basic application startup.
 */
public class DebugNoResourcesAppStartupTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {};
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
     * Tests that the JAX-RS application sub-class init params is logged and
     * other debug startup.
     * 
     * @throws Exception
     */
    public void testLogNoResource() throws Exception {
        List<LogRecord> records = handler.getRecords();

        assertEquals("Configuration Settings:", records.get(0).getMessage());
        assertEquals(Level.FINE, records.get(0).getLevel());

        assertEquals("Request User Handlers: []", records.get(1).getMessage());
        assertEquals(Level.FINE, records.get(1).getLevel());

        assertEquals("Response User Handlers: []", records.get(2).getMessage());
        assertEquals(Level.FINE, records.get(2).getLevel());

        assertEquals("Error User Handlers: []", records.get(3).getMessage());
        assertEquals(Level.FINE, records.get(3).getLevel());

        assertTrue(records.get(4).getMessage(), records.get(4).getMessage()
            .startsWith("MediaTypeMapper: "));
        assertEquals(Level.FINE, records.get(4).getLevel());

        assertTrue(records.get(5).getMessage(), records.get(5).getMessage()
            .startsWith("AlternateShortcutMap: "));
        assertEquals(Level.FINE, records.get(5).getLevel());

        assertTrue(records.get(6).getMessage(), records.get(6).getMessage()
            .startsWith("Properties: "));
        assertEquals(Level.FINE, records.get(6).getLevel());

        assertEquals("HttpMethodOverrideHeaders: []", records.get(7).getMessage());
        assertEquals(Level.FINE, records.get(7).getLevel());

        assertEquals("The system is using the org.apache.wink.server.internal.servlet.MockServletInvocationTest$MockApplication JAX-RS application class that is named in the javax.ws.rs.Application init-param initialization parameter.",
                     records.get(8).getMessage());
        assertEquals(Level.INFO, records.get(8).getLevel());
        assertEquals("The following JAX-RS application has been processed: org.apache.wink.server.internal.servlet.MockServletInvocationTest$MockApplication",
                     records.get(9).getMessage());
        assertEquals(Level.INFO, records.get(9).getLevel());

        assertEquals("There are no @javax.ws.rs.Path annotated classes defined in the application.",
                     records.get(10).getMessage());
        assertEquals(Level.FINE, records.get(10).getLevel());
        assertEquals("There are no custom JAX-RS providers defined in the application.", records.get(11)
            .getMessage());
        assertEquals(Level.INFO, records.get(11).getLevel());

        assertEquals(12, records.size());
    }

}
