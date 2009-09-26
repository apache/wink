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
package org.apache.wink.itest.addressbook;

import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * These tests drive calls to a REST resource that is deployed on our test
 * framework server. The resources being tested by this class exchange data via
 * String objects.
 */
public class WinkClientStringTest extends TestCase {

    protected static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/unittests/addresses";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/addressBook/unittests/addresses";
    }

    protected RestClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = new RestClient();

        /*
         * clear the database entries
         */
        clearDatabase();
    }

    public void clearDatabase() {
        ClientResponse response = client.resource(getBaseURI() + "/clear").post(null);
        assertEquals(204, response.getStatusCode());
    }

    /**
     * This will drive a GET request with no input parameters
     */
    public void testGetNoParams() {
        String responseBody = client.resource(getBaseURI()).get(String.class);

        Address addr = AddressBook.defaultAddress;
        assertTrue(responseBody.contains(addr.getEntryName()));
        assertTrue(responseBody.contains(addr.getStreetAddress()));
        assertTrue(responseBody.contains(addr.getZipCode()));
        assertTrue(responseBody.contains(addr.getCity()));
        assertTrue(responseBody.contains(addr.getCountry()));
        assertTrue(responseBody.contains(addr.getState()));
    }

    /**
     * This will drive a POST request with parameters from the query string
     */
    public void testPostWithQueryParams() {
        MultivaluedMapImpl<String, String> queryParams = new MultivaluedMapImpl<String, String>();
        queryParams.putSingle("entryName", "newAddress");
        queryParams.putSingle("streetAddress", "1234 Any Street");
        queryParams.putSingle("city", "AnyTown");
        queryParams.putSingle("zipCode", "90210");
        queryParams.putSingle("state", "TX");
        queryParams.putSingle("country", "US");
        client.resource(getBaseURI()).queryParams(queryParams).post(null);

        // now let's see if the address we just created is available
        ClientResponse response = client.resource(getBaseURI() + "/newAddress").get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody, responseBody.contains("newAddress"));
        assertTrue(responseBody, responseBody.contains("1234 Any Street"));
        assertTrue(responseBody, responseBody.contains("AnyTown"));
        assertTrue(responseBody, responseBody.contains("90210"));
        assertTrue(responseBody, responseBody.contains("TX"));
        assertTrue(responseBody, responseBody.contains("US"));
    }

    /**
     * This will drive a POST, GET, UPDATE, and DELETE on the
     * AddressBookResource
     */
    public void testAddressBookResource() {
        // make sure everything is clear before testing
        String input = "tempAddress&1234 Any Street&AnyTown&90210&TX&US";
        ClientResponse response =
            client.resource(getBaseURI() + "/fromBody").contentType(MediaType.TEXT_XML).post(input);
        assertEquals(204, response.getStatusCode());

        // now let's see if the address we just created is available
        String responseBody = client.resource(getBaseURI() + "/tempAddress").get(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody, responseBody.contains("tempAddress"));
        assertTrue(responseBody, responseBody.contains("1234 Any Street"));
        assertTrue(responseBody, responseBody.contains("AnyTown"));
        assertTrue(responseBody, responseBody.contains("90210"));
        assertTrue(responseBody, responseBody.contains("TX"));
        assertTrue(responseBody, responseBody.contains("US"));

        // let's update the state
        client.resource(getBaseURI()).queryParam("entryName", "tempAddress")
            .queryParam("streetAddress", "1234 Any Street").queryParam("city", "AnyTown")
            .queryParam("zipCode", "90210").queryParam("state", "AL").queryParam("country", "US")
            .put(null);

        // make sure the state has been updated
        responseBody = client.resource(getBaseURI() +"/tempAddress").get(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody, responseBody.contains("tempAddress"));
        assertFalse(responseBody, responseBody.contains("TX"));
        assertTrue(responseBody, responseBody.contains("AL"));

        // now let's delete the address
        response = client.resource(getBaseURI() + "/tempAddress").delete();
        assertEquals(204, response.getStatusCode());

        // now try to get the address
        response = client.resource(getBaseURI() + "/tempAddress").get();
        assertEquals(404, response.getStatusCode());
    }

}
