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
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.internal.handlers.AcceptHeaderHandler;
import org.apache.wink.itest.client.jaxb.Echo;
import org.apache.wink.providers.json.JsonProvider;
import org.apache.wink.test.integration.ServerEnvironmentInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Tests the Accept Header Handler is disabled when the appropriate client
 * config option is set.
 */
public class NoAcceptHeaderHandlerTest extends TestCase {

    public static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI();
    }

    RestClient client = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client =
            new RestClient(new ClientConfig().acceptHeaderAutoSet(false)
                .applications(new Application() {

                    @Override
                    public Set<Class<?>> getClasses() {
                        Set<Class<?>> classes = new HashSet<Class<?>>();
                        classes.add(JsonProvider.class);
                        return classes;
                    }

                }));
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
     * If the Accept header is not set, then let the client set a default to
     * send. In regular RestClient, it is set to {@link MediaType.WILDCARD}
     */
    public void testAcceptHeaderNotSetString() {
        String s = client.resource(getBaseURI() + "/echoaccept").get(String.class);
        assertEquals("echo: " + MediaType.WILDCARD, s);
    }

    /**
     * If no entity class is specified in the initial GET, then the
     * {@link AcceptHeaderHandler} should not set anything. However, the
     * underlying client may set the header as a failsafe.
     * 
     * @throws JSONException
     */
    public void testAcceptHeaderNoEntity() {
        ClientResponse resp = client.resource(getBaseURI() + "/echoaccept").get();
        /*
         * in this case the underlying client set the WILDCARD header for
         * default HttpURLConnection based client.
         */
        assertEquals("echo: " + MediaType.WILDCARD, resp.getEntity(String.class));
    }

    /**
     * For JAXB objects, there will be an error as the resource will return a
     * text/plain representation.
     */
    public void testAcceptHeaderForJAXB() {
        try {
            Echo e = client.resource(getBaseURI() + "/echoaccept").get(Echo.class);
            fail();
            // String value = e.getValue();
            // assertTrue(value, value.contains(MediaType.APPLICATION_JSON));
            // assertTrue(value, value.contains(MediaType.APPLICATION_XML));
            // assertTrue(value, value.contains(MediaType.TEXT_XML));
            // assertTrue(value, value.contains("application/javascript"));
        } catch (RuntimeException e) {
            assertEquals(e.getMessage(),
                         String
                             .format("No javax.ws.rs.ext.MessageBodyReader found for type class %s and media type %s .  Verify that all entity providers are correctly registered.",
                                     Echo.class.getName(),
                                     MediaType.TEXT_PLAIN));
        }
    }

    /**
     * For JSON objects, there will be an error as the resource will return a
     * text/plain representation.
     */
    public void testAcceptHeaderForJSON() throws JSONException {
        try {
            JSONObject j = client.resource(getBaseURI() + "/echoaccept").get(JSONObject.class);
            fail();
            // String value = j.getString("value");
            // assertTrue(value, value.contains(MediaType.APPLICATION_JSON));
            // assertTrue(value, value.contains("application/javascript"));
        } catch (RuntimeException e) {
            assertEquals(e.getMessage(),
                         String
                             .format("No javax.ws.rs.ext.MessageBodyReader found for type class %s and media type %s .  Verify that all entity providers are correctly registered.",
                                     JSONObject.class.getName(),
                                     MediaType.TEXT_PLAIN));
        }
    }
}
