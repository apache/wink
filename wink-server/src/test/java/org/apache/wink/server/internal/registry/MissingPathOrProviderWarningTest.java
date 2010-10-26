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

package org.apache.wink.server.internal.registry;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that a class that does not have a {@link Provider} or {@link Path} will
 * cause a warning to be emitted.
 */
public class MissingPathOrProviderWarningTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {TestResource.class};
    }

    public static class TestResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getCollection() {
            return "";
        }

    }

    private InMemoryHandler handler;
    private Logger          winkLogger = Logger.getLogger("org.apache.wink");

    @Before
    @Override
    public void setUp() throws Exception {
        handler = new InMemoryHandler();
        handler.setLevel(Level.INFO);

        winkLogger.addHandler(handler);
        winkLogger.setLevel(Level.INFO);
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        winkLogger.removeHandler(handler);
    }

    @Test
    public void testMissingPathOrProvider() throws Exception {
        List<LogRecord> records = handler.getRecords();

        assertEquals("The org.apache.wink.server.internal.registry.MissingPathOrProviderWarningTest$TestResource class is neither a resource nor a provider. The runtime is ignoring this class. It was returned from a javax.ws.rs.core.Application subclass. Add either a @javax.ws.rs.Path or a @javax.ws.rs.core.Provider annotation to the class.",
                     records.get(1).getMessage());
        assertEquals(Level.WARNING, records.get(1).getLevel());

        assertEquals(4, records.size());
    }

}
