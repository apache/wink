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
package org.apache.wink.itest.returntypes;

import javax.ws.rs.core.Response.Status;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkClientReturnTypeStatusTest extends TestCase {

    protected RestClient client;

    protected static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/returntypestatus";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/returntypes" + "/returntypestatus";
    }

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Tests that a void return type results in a response that has:
     * <ul>
     * <li>HTTP status code of 204
     * <li>empty response body
     * </ul>
     */
    public void testVoidReturnType() {
        ClientResponse response = client.resource(getBaseURI() + "/void").get();
        assertEquals(204, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
    }

    /**
     * Tests that a null return type on a method that returns an arbitrary
     * object results in a response that has:
     * <ul>
     * <li>HTTP status code of 204
     * <li>empty response body
     * </ul>
     */
    public void testNullObjectReturnType() {
        ClientResponse response = client.resource(getBaseURI() + "/null").get();
        assertEquals(204, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
    }

    /**
     * Tests that a null return on a method that is suppose to return a Response
     * object type results in a response that has:
     * <ul>
     * <li>HTTP status code of 204
     * <li>empty response body
     * </ul>
     */
    public void testNullResponseReturnType() {
        ClientResponse response = client.resource(getBaseURI() + "/nullresponse").get();
        assertEquals(204, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
    }

    /**
     * Tests that the status code can be set and returned correctly via a method
     * that returns a <code>Response</code> return type.
     */
    public void testStatusCodeSetResponseReturnType() {
        for (Status status : Status.values()) {

            ClientResponse response =
                client.resource(getBaseURI() + "/responsestatus?code=" + status.name()).get();
            int result = response.getStatusCode();
            String responseBody = response.getEntity(String.class);
            assertEquals(status.getStatusCode(), result);
            if (status.equals(Status.NO_CONTENT) || status.equals(Status.NOT_MODIFIED)) {
                assertEquals("", responseBody);
            } else {
                assertEquals("Requested status: " + status.getStatusCode() + " " + status.name(),
                             responseBody);
            }
        }
    }

    /**
     * Resource method returns a custom application response which does not have
     * the status code set but does have an entity. Expect the status code to be
     * 200 and the entity to be returned.
     */
    public void testStatusCodeNotSetResponseReturnType() {
        ClientResponse response =
            client.resource(getBaseURI() + "/CustomResponseStatusNotSet").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("CustomApplicationResponse", response.getEntity(String.class));
    }

    /**
     * Resource method returns a custom application response which does not have
     * the status code set and does not have an entity. Expect the status code
     * to be 204 and no content.
     */
    public void testStatusCodeNotSetNullEntityResponseReturnType() {
        ClientResponse response =
            client.resource(getBaseURI() + "/CustomNullResponseStatusNotSet").get();
        assertEquals(204, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
    }
}
