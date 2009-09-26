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
package org.apache.wink.itest.contentnegotiation;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class WinkContentNegotiationClientTest extends TestCase {

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    protected static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/customerservice";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/contentNegotiation" + "/customerservice";
    }

    public void testGetReturningXML() throws JAXBException {
        ClientResponse response =
            client.resource(getBaseURI() + "/customers/123").accept("application/xml").get();
        assertEquals(200, response.getStatusCode());
        Unmarshaller unmarshaller =
            JAXBContext.newInstance(ObjectFactory.class.getPackage().getName())
                .createUnmarshaller();
        Customer c = (Customer)unmarshaller.unmarshal(response.getEntity(InputStream.class));
        assertEquals(123, c.getId());
        assertEquals("John", c.getName());
    }

    public void testGetReturningJSON() throws IOException, JSONException {
        // Sent HTTP GET request to query customer info, expect JSON.
        ClientResponse response =
            client.resource(getBaseURI() + "/customers/123").accept("application/json").get();
        String responseBody = response.getEntity(String.class);
        assertEquals(200, response.getStatusCode());
        JSONTokener tokenizer = new JSONTokener(responseBody);
        JSONObject jObj = new JSONObject(tokenizer);
        assertEquals("John", jObj.get("name"));
        assertEquals(123L, jObj.getLong("id"));
    }

    public void testGetForCustomerInfoReturningJSON() throws JSONException {
        ClientResponse response = client.resource(getBaseURI() + "/customers/123").get();
        String responseBody = response.getEntity(String.class);
        assertEquals(200, response.getStatusCode());
        JSONTokener tokenizer = new JSONTokener(responseBody);
        JSONObject jObj = new JSONObject(tokenizer);
        assertEquals("John", jObj.get("name"));
        assertEquals(123L, jObj.getLong("id"));
    }
}
