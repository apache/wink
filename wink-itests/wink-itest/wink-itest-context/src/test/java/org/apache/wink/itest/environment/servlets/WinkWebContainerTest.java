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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.wink.itest.environment.servlets;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkWebContainerTest extends TestCase {

    private static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/environment/webcontainer/context";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/webcontainer"
            + "/environment/webcontainer/context";
    }

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Tests that a HTTPServletRequest can be injected.
     * 
     * @throws Exception
     */
    public void testHTTPServletRequestInjection() throws Exception {
        ClientResponse response = client.resource(getBaseURI()).get();
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            assertEquals(204, response.getStatusCode());
        } else {
            assertEquals(200, response.getStatusCode());
            assertTrue(getBaseURI().endsWith(response.getEntity(String.class)));
        }
    }

    /**
     * Tests that an injected HTTPServletResponse can take over the response
     * instead of further processing by the runtime engine.
     * 
     * @throws Exception
     */
    public void testHTTPServletResponseInjection() throws Exception {
        ClientResponse response = client.resource(getBaseURI()).post(null);
        assertEquals(200, response.getStatusCode());
        assertEquals("responseheadervalue", response.getHeaders().getFirst("responseheadername"));
        assertEquals("Hello World -- I was committted", response.getEntity(String.class));
    }

    /**
     * Tests that a ServletContext can be injected.
     * 
     * @throws Exception
     */
    public void testServletContextInjection() throws Exception {
        ClientResponse response = client.resource(getBaseURI() + "/servletcontext").get();
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getEntity(String.class).contains("testing 1-2-3"));
    }

    /**
     * Tests that a ServletConfig can be injected.
     * 
     * @throws Exception
     */
    public void testServletConfigInjection() throws Exception {
        ClientResponse response = client.resource(getBaseURI() + "/servletconfig").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("WebContainerTests", response.getEntity(String.class));
    }
}
