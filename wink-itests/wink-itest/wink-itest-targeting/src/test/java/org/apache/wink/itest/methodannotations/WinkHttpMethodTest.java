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
package org.apache.wink.itest.methodannotations;

import java.util.List;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkHttpMethodTest extends TestCase {

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/customannotations";
    }

    final private static String BASE_URI = getBaseURI() + "/httpmethod";

    final private static String ALT_URI  = getBaseURI() + "/customhttpmethod";

    /**
     * Tests that it can find a custom GET HttpMethod annotation.
     */
    public void testUserDefinedGETAnnotation() {
        ClientResponse response = client.resource(BASE_URI).get();
        assertEquals(200, response.getStatusCode());
        assertEquals("You found my GET method!", response.getEntity(String.class));
    }

    /**
     * Tests that an OPTIONS request can be sent to resource containing only a
     * GET method.
     */
    public void testOPTIONSRequest() {
        ClientResponse response = client.resource(BASE_URI).options();
        assertEquals(204, response.getStatusCode());
        String allowHeader = response.getHeaders().getFirst("Allow");
        assertTrue(allowHeader.contains("HEAD"));
        assertTrue(allowHeader.contains("GET"));
        assertTrue(allowHeader.contains("OPTIONS"));
    }

    /**
     * Tests that a HEAD request can be sent to resource containing only a GET
     * method.
     */
    public void testHEADRequest() {
        ClientResponse response = client.resource(BASE_URI).head();
        assertEquals(200, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
        assertTrue(response.getHeaders().toString(), response.getHeaders().size() > 0);
    }

    /**
     * Tests that a HEAD request can be sent to resource annotated with a custom
     * HEAD annotation.
     */
    public void testCustomHEADRequest() {
        ClientResponse response = client.resource(ALT_URI).head();
        assertEquals(200, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
        assertEquals("TRUE", response.getHeaders().getFirst("HEAD"));
    }

    /**
     * Tests that a OPTIONS request can be sent to resource annotated with a
     * custom OPTIONS annotation.
     */
    public void testCustomOPTIONSRequest() {
        ClientResponse response = client.resource(ALT_URI).options();
        assertEquals(200, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
        List<String> allowHeader = response.getHeaders().get("Allow");
        assertTrue(allowHeader.toString(), allowHeader.contains("HEAD"));
        assertTrue(allowHeader.toString(), allowHeader.contains("OPTIONS"));
        assertTrue(allowHeader.toString(), allowHeader.contains("GET"));
    }
}
