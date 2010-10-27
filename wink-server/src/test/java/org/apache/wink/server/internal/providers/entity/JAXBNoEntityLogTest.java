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
package org.apache.wink.server.internal.providers.entity;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JAXBNoEntityLogTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyResource1.class};
    }

    @XmlRootElement
    public static class Book {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String s) {
            name = s;
        }
    }

    @Path("res1")
    public static class MyResource1 {

        @POST
        public Book get(Book b) {
            return b;
        }

    }

    private InMemoryHandler handler;

    private Logger          winkLogger = Logger.getLogger("org.apache.wink");

    private Handler         consoleHandler;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Handler[] defaultHandlers = Logger.getLogger("").getHandlers();
        if (defaultHandlers.length == 1) {
            consoleHandler = defaultHandlers[0];
            consoleHandler.setLevel(Level.FINE);
        }

        handler = new InMemoryHandler();
        handler.setLevel(Level.INFO);

        winkLogger.setLevel(Level.INFO);
        winkLogger.addHandler(handler);
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
     * Tests that a Provider is logged at the debug level.
     */
    public void testNoEntityRefLog() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "res1",
                                                        MediaType.WILDCARD,
                                                        MediaType.APPLICATION_XML,
                                                        "".getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(400, response.getStatus());

        assertEquals(0, handler.getRecords().size());
    }
}
