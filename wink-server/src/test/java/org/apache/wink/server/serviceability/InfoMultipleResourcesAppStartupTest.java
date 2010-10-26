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
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;

/**
 * Tests that a message indicating multiple registered resources were added.
 */
public class InfoMultipleResourcesAppStartupTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyResource1.class, MyResource2.class, MyResource3.class};
    }

    @Path("/hello")
    public static class MyResource1 {

    }

    @Path("helloabcd")
    public static class MyResource2 {
        @GET
        public void get() {

        }
    }

    @Path("/hello2")
    public static class MyResource3 {

        @GET
        @Path("more")
        public void get() {

        }
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
     * Tests the INFO logging for multiple resources.
     * 
     * @throws Exception
     */
    public void testLogResource() throws Exception {
        List<LogRecord> records = handler.getRecords();

        /*
         * This is actually a deterministic order. The @Path dictates when they
         * are listed (i.e. JAX-RS @Path search order so the first resource
         * check is listed first).
         */
        final String resource2Message =
            "The server has registered the JAX-RS resource class org.apache.wink.server.serviceability.InfoMultipleResourcesAppStartupTest$MyResource2 with @Path(helloabcd).";
        assertEquals(resource2Message, records.get(2).getMessage());
        assertEquals(Level.INFO, records.get(2).getLevel());
        final String resource3Message =
            "The server has registered the JAX-RS resource class org.apache.wink.server.serviceability.InfoMultipleResourcesAppStartupTest$MyResource3 with @Path(/hello2).";
        assertEquals(resource3Message, records.get(3).getMessage());
        assertEquals(Level.INFO, records.get(3).getLevel());
        final String resource1Message =
            "The server has registered the JAX-RS resource class org.apache.wink.server.serviceability.InfoMultipleResourcesAppStartupTest$MyResource1 with @Path(/hello).";
        assertEquals(resource1Message, records.get(4).getMessage());
        assertEquals(Level.INFO, records.get(4).getLevel());

        assertEquals(6, records.size());
    }
}
