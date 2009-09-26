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
package org.apache.wink.itest.exceptions;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerContainerAssertions;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkValidationDuringTargetingTest extends TestCase {

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/exceptional";
    }

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Tests that a GET method to various paths only differing by
     * {@link Produces} works.
     * 
     * @throws Exception
     */
    public void testGETOnlyDifferByProduces() throws Exception {
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/resourceonlyproduces")
                .accept(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello JSON Produces", response.getEntity(String.class));
        assertEquals("application/json", response.getHeaders().getFirst("Content-Type"));

        response =
            client.resource(getBaseURI() + "/targeting/resourceonlyproduces")
                .accept(MediaType.APPLICATION_XML).get();
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello XML Produces", response.getEntity(String.class));
        assertEquals("application/xml", response.getHeaders().getFirst("Content-Type"));

        response =
            client.resource(getBaseURI() + "/targeting/resourceonlyproduces")
                .accept(MediaType.TEXT_XML).get();
        assertEquals(406, response.getStatusCode());
        ServerContainerAssertions.assertExceptionBodyFromServer(406, response
            .getEntity(String.class));
    }

    /**
     * Tests that a GET method to various paths only differing by
     * {@link Consumes} works.
     * 
     * @throws Exception
     */
    public void testGETOnlyDifferByConsumes() throws Exception {
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/resourceonlyconsumes")
                .contentType(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello JSON Consumes", response.getEntity(String.class));
        assertEquals("text/plain", response.getHeaders().getFirst("Content-Type"));

        response =
            client.resource(getBaseURI() + "/targeting/resourceonlyconsumes")
                .contentType(MediaType.TEXT_XML).get();
        assertEquals(415, response.getStatusCode());
        ServerContainerAssertions.assertExceptionBodyFromServer(415, response
            .getEntity(String.class));
    }

    /**
     * Tests that a GET method to various paths only differing by
     * {@link Consumes} works.
     * 
     * @throws Exception
     */
    public void testGETOnlyDifferByConsumesAndProduces() throws Exception {
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/resourceconsumesandproduces")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello JSON Consumes And Produces", response.getEntity(String.class));
        assertEquals("application/json", response.getHeaders().getFirst("Content-Type"));

        response =
            client.resource(getBaseURI() + "/targeting/resourceconsumesandproduces")
                .contentType(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello JSON Consumes And Produces", response.getEntity(String.class));
        assertEquals("application/json", response.getHeaders().getFirst("Content-Type"));

        /*
         * due to no request Accept header, this is actually undefined behavior
         * whether it hits the JSON or the XML on the Produces side
         */
        response =
            client.resource(getBaseURI() + "/targeting/resourceconsumesandproduces")
                .contentType(MediaType.APPLICATION_XML).get();
        assertEquals(200, response.getStatusCode());
        if ("application/json".equals(response.getHeaders().getFirst("Content-Type"))) {
            assertEquals(200, response.getStatusCode());
            assertEquals("Hello XML Consumes And JSON Produces", response.getEntity(String.class));
            assertEquals("application/json", response.getHeaders().getFirst("Content-Type"));
        } else {
            assertEquals(200, response.getStatusCode());
            assertEquals("Hello XML Consumes And Produces", response.getEntity(String.class));
            assertEquals("application/xml", response.getHeaders().getFirst("Content-Type"));
        }

        response =
            client.resource(getBaseURI() + "/targeting/resourceconsumesandproduces")
                .contentType(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).get();
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello XML Consumes And Produces", response.getEntity(String.class));
        assertEquals("application/xml", response.getHeaders().getFirst("Content-Type"));

        response =
            client.resource(getBaseURI() + "/targeting/resourceconsumesandproduces")
                .contentType(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello XML Consumes And JSON Produces", response.getEntity(String.class));
        assertEquals("application/json", response.getHeaders().getFirst("Content-Type"));
    }
}
