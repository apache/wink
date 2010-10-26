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
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;

public class InfoNoProvidersAppStartupTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {};
    }

    private InMemoryHandler handler;

    private Logger          winkLogger = Logger.getLogger("org.apache.wink");

    @Override
    protected void setUp() throws Exception {
        handler = new InMemoryHandler();
        handler.setLevel(Level.INFO);

        winkLogger.setLevel(Level.INFO);
        winkLogger.addHandler(handler);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        winkLogger.removeHandler(handler);
        winkLogger.setLevel(Level.INFO);
        super.tearDown();
    }

    /**
     * Tests that the JAX-RS application sub-class init params is logged and
     * other normal startup.
     * 
     * @throws Exception
     */
    public void testLogNoProviders() throws Exception {
        List<LogRecord> records = handler.getRecords();

        assertEquals("The system is using the org.apache.wink.server.internal.servlet.MockServletInvocationTest$MockApplication JAX-RS application class that is named in the javax.ws.rs.Application init-param initialization parameter.",
                     records.get(0).getMessage());
        assertEquals(Level.INFO, records.get(0).getLevel());
        assertEquals("The following JAX-RS application has been processed: org.apache.wink.server.internal.servlet.MockServletInvocationTest$MockApplication",
                     records.get(1).getMessage());
        assertEquals(Level.INFO, records.get(1).getLevel());
        assertEquals("There are no custom JAX-RS providers defined in the application.", records.get(2)
            .getMessage());
        assertEquals(Level.INFO, records.get(2).getLevel());
        assertEquals(3, records.size());
    }

}
