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

package org.apache.wink.itest.client;

import java.io.StringReader;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.internal.handlers.AcceptHeaderHandler;
import org.apache.wink.itest.client.jaxb.Echo;
import org.apache.wink.test.integration.ServerEnvironmentInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Tests the Accept Header Handler. The {@link AcceptHeaderHandler}
 * automatically sets the Accept header if it is not already set by the client.
 */
public class AcceptHeaderHandlerTest extends TestCase {

    public static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI();
    }

    RestClient client = null;

    public void setUp() throws Exception {
        super.setUp();
        client = new RestClient();
    }

    /**
     * If the Accept header is already set, then the Accept Header handler
     * should not attempt to set it. This is particularly useful for types like
     * String which would do MediaType.WILDCARD.
     * 
     * @throws JSONException
     * @throws JAXBException
     */
    public void testAcceptHeaderSet() throws JSONException, JAXBException {
        String s =
            client.resource(getBaseURI() + "/echoaccept").accept(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        JSONObject j = new JSONObject(new JSONTokener(s));
        assertEquals("echo: " + MediaType.APPLICATION_JSON, j.get("value"));

        s =
            client.resource(getBaseURI() + "/echoaccept").accept(MediaType.TEXT_XML)
                .get(String.class);
        Echo e =
            (Echo)JAXBContext.newInstance(Echo.class).createUnmarshaller()
                .unmarshal(new StringReader(s));
        assertEquals(e.getValue(), "echo: " + MediaType.TEXT_XML);

        /*
         * this is actually a trick to make sure that plain text is returned.
         * the server side piece does not produce APPLICATION_XML but will
         * produce TEXT_PLAIN for any non-produced type. it really should return
         * 415 status code.
         */
        s =
            client.resource(getBaseURI() + "/echoaccept").accept(MediaType.APPLICATION_XML)
                .get(String.class);
        assertEquals("echo: " + MediaType.APPLICATION_XML, s);
    }

    /**
     * If the Accept header is not set, then let the {@link AcceptHeaderHandler}
     * set it automatically.
     */
    public void testAcceptHeaderNotSetString() {
        String s = client.resource(getBaseURI() + "/echoaccept").get(String.class);
        assertEquals("echo: " + MediaType.WILDCARD, s);
    }

    /**
     * If no entity class is specified in the initial GET, then the
     * {@link AcceptHeaderHandler} should not set anything. However, the
     * underlying client may set the header as a failsafe.
     */
    public void testAcceptHeaderNoEntity() {
        RestClient client = new RestClient();
        ClientResponse resp = client.resource(getBaseURI() + "/echoaccept").get();
        /*
         * in this case the underlying client set the WILDCARD header for
         * default HttpURLConnection based client.
         */
        assertEquals("echo: " + MediaType.WILDCARD, resp.getEntity(String.class));
    }

    /**
     * For JAXB objects, the {@link AcceptHeaderHandler} should automatically
     * take care of the Accept header.
     */
    public void testAcceptHeaderForJAXB() {
        Echo p = client.resource(getBaseURI() + "/echoaccept").get(Echo.class);
        assertTrue(p.getValue().contains(MediaType.TEXT_XML) && p.getValue()
            .contains(MediaType.APPLICATION_XML));
    }

    /**
     * For JSON objects, the {@link AcceptHeaderHandler} should automatically
     * take care of the Accept header.
     */
    public void testAcceptHeaderForJSON() throws JSONException {
        JSONObject j = client.resource(getBaseURI() + "/echoaccept").get(JSONObject.class);
        String v = j.getString("value");
        assertTrue(v.contains(MediaType.APPLICATION_JSON) && v.contains("application/javascript"));
    }

}
