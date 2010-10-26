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

import java.util.Formatter;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;

/**
 * Tests the debug information for multiple resources.
 */
public class DebugMultipleResourcesAppStartupTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyResource1.class, MyResource2.class, MyResource3.class,
            MyResource4.class};
    }

    @Path("/hello")
    public static class MyResource1 {

        @GET
        public String get() {
            return null;
        }
    }

    @Path("/hello2")
    public static class MyResource2 {

        @DELETE
        @Path("subpath")
        public String get() {
            return null;
        }
    }

    @Path("/longpath/longpath/longpath")
    public static class MyResource3 {

        @Path("sublocator")
        public String get() {
            return null;
        }
    }

    @Path("/somepath")
    public static class MyResource4 {

        @PUT
        public String put(byte[] someBytes) {
            return null;
        }

        @POST
        @Path("subresource")
        public String getSubresource(String someEntity) {
            return null;
        }

        @Path("sublocator")
        public String getSublocator(@QueryParam("q") String something) {
            return null;
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

        assertEquals("The server has registered the JAX-RS resource class org.apache.wink.server.serviceability.DebugMultipleResourcesAppStartupTest$MyResource3 with @Path(/longpath/longpath/longpath).",
                     records.get(10).getMessage());
        assertEquals(Level.INFO, records.get(10).getLevel());

        assertEquals("The server has registered the JAX-RS resource class org.apache.wink.server.serviceability.DebugMultipleResourcesAppStartupTest$MyResource4 with @Path(/somepath).",
                     records.get(11).getMessage());
        assertEquals(Level.INFO, records.get(11).getLevel());

        assertEquals("The server has registered the JAX-RS resource class org.apache.wink.server.serviceability.DebugMultipleResourcesAppStartupTest$MyResource2 with @Path(/hello2).",
                     records.get(12).getMessage());
        assertEquals(Level.INFO, records.get(12).getLevel());

        assertEquals("The server has registered the JAX-RS resource class org.apache.wink.server.serviceability.DebugMultipleResourcesAppStartupTest$MyResource1 with @Path(/hello).",
                     records.get(13).getMessage());
        assertEquals(Level.INFO, records.get(13).getLevel());

        StringBuffer resourceTable = new StringBuffer();
        Formatter f = new Formatter(resourceTable);
        f.format("Registered JAX-RS resources: %n%1$-80s %2$-13s %3$-20s %4$-20s %5$s",
                 "Path",
                 "HTTP Method",
                 "Consumes",
                 "Produces",
                 "Resource Method");
        f
            .format("%n%1$-80s %2$-13s %3$-20s %4$-20s %5$s",
                    "/longpath/longpath/longpath/sublocator",
                    "(Sub-Locator)",
                    "[*/*]",
                    "[*/*]",
                    "org.apache.wink.server.serviceability.DebugMultipleResourcesAppStartupTest$MyResource3.get()");

        f
            .format("%n%1$-80s %2$-13s %3$-20s %4$-20s %5$s",
                    "/somepath",
                    "PUT",
                    "[*/*]",
                    "[*/*]",
                    "org.apache.wink.server.serviceability.DebugMultipleResourcesAppStartupTest$MyResource4.put(byte[])");
        f
            .format("%n%1$-80s %2$-13s %3$-20s %4$-20s %5$s",
                    "/somepath/subresource",
                    "POST",
                    "[*/*]",
                    "[*/*]",
                    "org.apache.wink.server.serviceability.DebugMultipleResourcesAppStartupTest$MyResource4.getSubresource(String)");
        f
            .format("%n%1$-80s %2$-13s %3$-20s %4$-20s %5$s",
                    "/somepath/sublocator",
                    "(Sub-Locator)",
                    "[*/*]",
                    "[*/*]",
                    "org.apache.wink.server.serviceability.DebugMultipleResourcesAppStartupTest$MyResource4.getSublocator(String)");
        f
            .format("%n%1$-80s %2$-13s %3$-20s %4$-20s %5$s",
                    "/hello2/subpath",
                    "DELETE",
                    "[*/*]",
                    "[*/*]",
                    "org.apache.wink.server.serviceability.DebugMultipleResourcesAppStartupTest$MyResource2.get()");

        f
            .format("%n%1$-80s %2$-13s %3$-20s %4$-20s %5$s",
                    "/hello",
                    "GET",
                    "[*/*]",
                    "[*/*]",
                    "org.apache.wink.server.serviceability.DebugMultipleResourcesAppStartupTest$MyResource1.get()");

        assertEquals(resourceTable.toString(), records.get(14).getMessage());
        assertEquals(Level.FINE, records.get(14).getLevel());

        assertEquals("There are no custom JAX-RS providers defined in the application.", records.get(15)
            .getMessage());
        assertEquals(Level.INFO, records.get(15).getLevel());

        assertEquals(16, records.size());
    }

}
