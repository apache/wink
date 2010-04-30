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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerContainerAssertions;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class ValidationDuringTargettingTest extends TestCase {

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/exceptional";
    }

    protected HttpClient client;

    @Override
    public void setUp() {
        client = new HttpClient();
    }

    /**
     * Tests that a GET method to various paths only differing by
     * {@link Produces} works.
     * 
     * @throws Exception
     */
    public void testGETOnlyDifferByProduces() throws Exception {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/targeting/resourceonlyproduces");
        try {
            getMethod.addRequestHeader("Accept", "application/json");
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello JSON Produces", getMethod.getResponseBodyAsString());
            assertEquals("application/json", getMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/targeting/resourceonlyproduces");

        try {
            getMethod.addRequestHeader("Accept", "application/xml");
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello XML Produces", getMethod.getResponseBodyAsString());
            assertEquals("application/xml", getMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/targeting/resourceonlyproduces");

        try {
            getMethod.addRequestHeader("Accept", "text/xml");
            client.executeMethod(getMethod);

            assertEquals(406, getMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(406, getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a GET method to various paths only differing by
     * {@link Consumes} works.
     * 
     * @throws Exception
     */
    public void testGETOnlyDifferByConsumes() throws Exception {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/targeting/resourceonlyconsumes");
        getMethod.setRequestHeader("Content-Type", "application/json");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello JSON Consumes", getMethod.getResponseBodyAsString());
            assertEquals("text/plain", getMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/targeting/resourceonlyconsumes");
        getMethod.setRequestHeader("Content-Type", "text/xml");

        try {
            client.executeMethod(getMethod);

            assertEquals(415, getMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(415, getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a GET method to various paths only differing by
     * {@link Consumes} works.
     * 
     * @throws Exception
     */
    public void testGETOnlyDifferByConsumesAndProduces() throws Exception {
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/targeting/resourceconsumesandproduces");
        getMethod.setRequestHeader("Content-Type", "application/json");
        getMethod.setRequestHeader("Accept", "application/json");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello JSON Consumes And Produces", getMethod.getResponseBodyAsString());
            assertEquals("application/json", getMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/targeting/resourceconsumesandproduces");
        getMethod.setRequestHeader("Content-Type", "application/json");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello JSON Consumes And Produces", getMethod.getResponseBodyAsString());
            assertEquals("application/json", getMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * due to no request Accept header, this is actually undefined behavior
         * whether it hits the JSON or the XML on the Produces side
         */
        getMethod = new GetMethod(getBaseURI() + "/targeting/resourceconsumesandproduces");
        getMethod.setRequestHeader("Content-Type", "application/xml");
        try {
            client.executeMethod(getMethod);
            if (MediaType.APPLICATION_JSON_TYPE.isCompatible(MediaType.valueOf(getMethod
                .getResponseHeader("Content-Type").getValue()))) {
                assertEquals(200, getMethod.getStatusCode());
                assertEquals("Hello XML Consumes And JSON Produces", getMethod
                    .getResponseBodyAsString());
                assertEquals("application/json", getMethod
                    .getResponseHeader("Content-Type").getValue());
            } else {
                assertEquals(200, getMethod.getStatusCode());
                assertEquals("Hello XML Consumes And Produces", getMethod.getResponseBodyAsString());
                assertEquals("application/xml", getMethod
                    .getResponseHeader("Content-Type").getValue());
            }
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/targeting/resourceconsumesandproduces");
        getMethod.setRequestHeader("Content-Type", "application/xml");
        getMethod.setRequestHeader("Accept", "application/xml");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello XML Consumes And Produces", getMethod.getResponseBodyAsString());
            assertEquals("application/xml", getMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/targeting/resourceconsumesandproduces");
        getMethod.setRequestHeader("Content-Type", "application/xml");
        getMethod.setRequestHeader("Accept", "application/json");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello XML Consumes And JSON Produces", getMethod
                .getResponseBodyAsString());
            assertEquals("application/json", getMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            getMethod.releaseConnection();
        }
    }
}
