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
package org.apache.wink.itest.headers;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkHeadersTest extends TestCase {

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/headers";
    }

    public void testGetWithCookies() throws Exception {
        ClientResponse response =
            client.resource(new URI(getBaseURI() + "/headers/cookie"))
                .cookie("$Version=\"1\";login=\"jdoe\"").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("jdoe", response.getHeaders().getFirst("login"));
    }

    public void testGetWithLanguage() throws Exception {
        ClientResponse response =
            client.resource(new URI(getBaseURI() + "/headers/language")).header("Content-Language",
                                                                                "en-us").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("en:US", response.getHeaders().getFirst("language"));
    }

    public void testGetWithContent() throws Exception {
        ClientResponse response =
            client.resource(new URI(getBaseURI() + "/headers/content"))
                .contentType("application/html").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("application/html", response.getHeaders().getFirst("content"));
    }

    public void testGetWithAccept() throws Exception {
        ClientResponse response =
            client.resource(new URI(getBaseURI() + "/headers/accept"))
                .accept("text/*, text/html, text/html;level=1, */*").get();
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst("test-accept"));

        // all q-values = 1, should come back like it was sent
        String value = response.getHeaders().getFirst("test-accept");
        assertTrue(value, value.endsWith("*/*"));
        assertTrue(value, value.contains("text/*"));
        assertTrue(value, value.contains("text/html"));
        assertTrue(value, value.contains("text/html;level=1"));
    }

    public void testGetWithAcceptLanguage() throws Exception {
        ClientResponse response =
            client.resource(new URI(getBaseURI() + "/headers/acceptlang")).acceptLanguage("fr")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("fr", response.getHeaders().getFirst("acceptlang"));
    }

    public void testGetHeadersWithCase() throws Exception {
        ClientResponse response =
            client.resource(new URI(getBaseURI() + "/headers/headercase")).header("Custom-Header",
                                                                                  "MyValue").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("MyValue", response.getHeaders().getFirst("Custom-Header"));
    }

    public void testGetHeadersAcceptAsParam() throws Exception {
        ClientResponse response =
            client.resource(new URI(getBaseURI() + "/headers/headeraccept")).accept("text/xml")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("text/xml", response.getHeaders().getFirst("test-accept"));
    }

    public void testGetHeadersAcceptAsArg() throws Exception {
        ClientResponse response =
            client.resource(new URI(getBaseURI() + "/headers/headersasarg"))
                .accept("text/xml application/xml").contentType(MediaType.APPLICATION_XML_TYPE)
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("text/xml application/xml", response.getHeaders().getFirst("test-accept"));
        assertEquals("application/xml", response.getHeaders().getFirst("test-content-type"));
    }

    public void testAllowHeaders() throws Exception {
        ClientResponse response =
            client.resource(new URI(getBaseURI() + "/headersallow1/allow1")).options();
        assertEquals(204, response.getStatusCode());

        List<String> allowedMethods =
            Arrays.asList(response.getHeaders().getFirst("Allow").split(", "));
        assertEquals(3, allowedMethods.size());
        assertTrue(allowedMethods.contains("HEAD"));
        assertTrue(allowedMethods.contains("OPTIONS"));
        assertTrue(allowedMethods.contains("GET"));

        response = client.resource(new URI(getBaseURI() + "/headersallow2")).options();
        assertEquals(204, response.getStatusCode());

        allowedMethods = Arrays.asList(response.getHeaders().getFirst("Allow").split(", "));
        System.out.println(allowedMethods);
        assertEquals(6, allowedMethods.size());
        assertTrue(allowedMethods.contains("HEAD"));
        assertTrue(allowedMethods.contains("OPTIONS"));
        assertTrue(allowedMethods.contains("GET"));
        assertTrue(allowedMethods.contains("PUT"));
        assertTrue(allowedMethods.contains("POST"));
        assertTrue(allowedMethods.contains("DELETE"));

        response = client.resource(new URI(getBaseURI() + "/headersallow3/sublocator")).options();
        assertEquals(204, response.getStatusCode());

        allowedMethods = Arrays.asList(response.getHeaders().getFirst("Allow").split(", "));
        System.out.println(allowedMethods);
        assertEquals(3, allowedMethods.size());
        assertTrue(allowedMethods.contains("HEAD"));
        assertTrue(allowedMethods.contains("OPTIONS"));
        assertTrue(allowedMethods.contains("GET"));
        assertEquals("", response.getEntity(String.class));
    }
}
