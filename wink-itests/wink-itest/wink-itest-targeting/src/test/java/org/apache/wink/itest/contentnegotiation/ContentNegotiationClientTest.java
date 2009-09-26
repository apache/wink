/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.wink.itest.contentnegotiation;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * These tests drive calls to a REST resource that is deployed on our test
 * framework server. The resources being tested by this class exchange data via
 * String objects.
 */
public class ContentNegotiationClientTest extends TestCase {

    private HttpClient httpclient = new HttpClient();

    protected static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/customerservice";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/contentNegotiation" + "/customerservice";
    }

    public void testGetReturningXML() throws JAXBException {
        // Sent HTTP GET request to query customer info, expect XML
        System.out.println("Sent HTTP GET request to query customer info, expect XML");
        GetMethod get = new GetMethod(getBaseURI() + "/customers/123");
        get.addRequestHeader("Accept", "application/xml");

        try {
            int result = httpclient.executeMethod(get);
            System.out.println("Response status code: " + result);
            System.out.println("Response body: ");
            String responseBody = get.getResponseBodyAsString();
            System.out.println(responseBody);
            assertTrue(result == 200);
            Unmarshaller unmarshaller =
                JAXBContext.newInstance(ObjectFactory.class.getPackage().getName())
                    .createUnmarshaller();
            Customer c = (Customer)unmarshaller.unmarshal(get.getResponseBodyAsStream());
            assertEquals(123, c.getId());
            assertEquals("John", c.getName());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail(ioe.getMessage());
        } finally {
            get.releaseConnection();
        }

    }

    public void testGetReturningJSON() throws IOException, JSONException {
        // Sent HTTP GET request to query customer info, expect JSON.
        System.out.println("\n");
        System.out.println("Sent HTTP GET request to query customer info, expect JSON");
        GetMethod get = new GetMethod(getBaseURI() + "/customers/123");
        get.addRequestHeader("Accept", "application/json");
        httpclient = new HttpClient();

        try {
            int result = httpclient.executeMethod(get);
            System.out.println("Response status code: " + result);
            System.out.println("Response body: ");
            String responseBody = get.getResponseBodyAsString();
            System.out.println(responseBody);
            assertEquals(200, result);
            JSONTokener tokenizer = new JSONTokener(responseBody);
            JSONObject jObj = new JSONObject(tokenizer);
            assertEquals("John", jObj.get("name"));
            assertEquals(123L, jObj.getLong("id"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail(ioe.getMessage());
        } finally {
            get.releaseConnection();
        }
    }

    public void testGetForCustomerInfoReturningJSON() throws JSONException {

        // Sent HTTP GET request to query customer info, expect JSON.
        System.out.println("\n");
        System.out.println("Sent HTTP GET request to query customer info, expect JSON");
        // The default behavior without setting Accept header explicitly is
        // depending on your client.
        // In the case of HTTP Client, the Accept header will be absent. The CXF
        // server will treat this
        // as "*/*", JSON format is returned
        GetMethod get = new GetMethod(getBaseURI() + "/customers/123");
        httpclient = new HttpClient();

        try {
            int result = httpclient.executeMethod(get);
            System.out.println("Response status code: " + result);
            System.out.println("Response body: ");
            String responseBody = get.getResponseBodyAsString();
            System.out.println(responseBody);
            assertEquals(200, result);
            JSONTokener tokenizer = new JSONTokener(responseBody);
            JSONObject jObj = new JSONObject(tokenizer);

            assertEquals("John", jObj.get("name"));
            assertEquals(123L, jObj.getLong("id"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail(ioe.getMessage());
        } finally {
            get.releaseConnection();
        }

        System.out.println("\n");
        System.out.println("Client Invoking is succeeded!");

    }
}
