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

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.wink.test.integration.ServerContainerAssertions;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * These tests drive calls to a REST resource that is deployed on our test
 * framework server. The resources being tested by this class exchange data via
 * String objects.
 */
public class StringTest extends TestCase {

    protected static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/unittests/addresses";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/addressBook/unittests/addresses";
    }

    protected HttpClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = new HttpClient();

        /*
         * clear the database entries
         */
        clearDatabase();
    }

    public void clearDatabase() {
        /*
         * clear the database entries
         */
        HttpMethod method = null;
        try {
            method = new PostMethod(getBaseURI() + "/clear");
            client.executeMethod(method);
            assertEquals(204, method.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    /**
     * This will drive a GET request with no input parameters
     */
    public void testGetNoParams() {

        HttpMethod method = null;
        try {
            HttpClient client = new HttpClient();
            method = new GetMethod(getBaseURI());
            client.executeMethod(method);
            String responseBody = method.getResponseBodyAsString();
            Address addr = AddressBook.defaultAddress;
            assertTrue(responseBody.contains(addr.getEntryName()));
            assertTrue(responseBody.contains(addr.getStreetAddress()));
            assertTrue(responseBody.contains(addr.getZipCode()));
            assertTrue(responseBody.contains(addr.getCity()));
            assertTrue(responseBody.contains(addr.getCountry()));
            assertTrue(responseBody.contains(addr.getState()));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    /**
     * This will drive a POST request with parameters from the query string
     */
    public void testPostWithQueryParams() {
        HttpMethod method = null;
        HttpMethod getMethod = null;
        try {

            // make sure everything is clear before testing
            HttpClient client = new HttpClient();
            method = new PostMethod(getBaseURI());
            method
                .setQueryString("entryName=newAddress&streetAddress=1234+Any+Street&city=" + "AnyTown&zipCode=90210&state=TX&country=US");
            client.executeMethod(method);

            // now let's see if the address we just created is available
            getMethod = new GetMethod(getBaseURI() + "/newAddress");
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertNotNull(responseBody);
            assertTrue(responseBody, responseBody.contains("newAddress"));
            assertTrue(responseBody, responseBody.contains("1234 Any Street"));
            assertTrue(responseBody, responseBody.contains("AnyTown"));
            assertTrue(responseBody, responseBody.contains("90210"));
            assertTrue(responseBody, responseBody.contains("TX"));
            assertTrue(responseBody, responseBody.contains("US"));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    /**
     * This will drive a POST, GET, UPDATE, and DELETE on the
     * AddressBookResource
     */
    public void testAddressBookResource() {
        PostMethod method = null;
        GetMethod getMethod = null;
        PutMethod put = null;
        DeleteMethod deleteMethod = null;
        try {

            // make sure everything is clear before testing
            HttpClient client = new HttpClient();
            method = new PostMethod(getBaseURI() + "/fromBody");
            String input = "tempAddress&1234 Any Street&AnyTown&90210&TX&US";
            RequestEntity entity = new ByteArrayRequestEntity(input.getBytes(), "text/xml");
            method.setRequestEntity(entity);
            client.executeMethod(method);

            // now let's see if the address we just created is available
            getMethod = new GetMethod(getBaseURI() + "/tempAddress");
            client.executeMethod(getMethod);
            String responseBody = getMethod.getResponseBodyAsString();
            getMethod.releaseConnection();
            assertNotNull(responseBody);
            assertTrue(responseBody, responseBody.contains("tempAddress"));
            assertTrue(responseBody, responseBody.contains("1234 Any Street"));
            assertTrue(responseBody, responseBody.contains("AnyTown"));
            assertTrue(responseBody, responseBody.contains("90210"));
            assertTrue(responseBody, responseBody.contains("TX"));
            assertTrue(responseBody, responseBody.contains("US"));

            // let's update the state
            String query =
                "entryName=tempAddress&streetAddress=1234+Any+Street&city=" + "AnyTown&zipCode=90210&state=AL&country=US";
            client = new HttpClient();
            put = new PutMethod(getBaseURI());
            put.setQueryString(query);
            client.executeMethod(put);

            // make sure the state has been updated
            client = new HttpClient();
            client.executeMethod(getMethod);
            responseBody = getMethod.getResponseBodyAsString();
            assertNotNull(responseBody);
            assertTrue(responseBody.contains("tempAddress"));
            assertFalse(responseBody.contains("TX"));
            assertTrue(responseBody.contains("AL"));

            // now let's delete the address
            client = new HttpClient();
            deleteMethod = new DeleteMethod(getBaseURI() + "/tempAddress");
            client.executeMethod(deleteMethod);
            assertEquals(204, deleteMethod.getStatusCode());

            // now try to get the address
            client = new HttpClient();
            client.executeMethod(getMethod);
            assertEquals(404, getMethod.getStatusCode());
            responseBody = getMethod.getResponseBodyAsString();
            ServerContainerAssertions.assertExceptionBodyFromServer(404, getMethod
                .getResponseBodyAsString());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
            if (put != null) {
                put.releaseConnection();
            }
            if (deleteMethod != null) {
                deleteMethod.releaseConnection();
            }
        }
    }

}
