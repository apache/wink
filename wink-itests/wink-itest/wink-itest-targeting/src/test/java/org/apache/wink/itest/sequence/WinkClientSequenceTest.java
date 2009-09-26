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
package org.apache.wink.itest.sequence;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Verifies that a sequence of basic calls to the same resource have the
 * appropriate resource life-cycles.
 */
public class WinkClientSequenceTest extends TestCase {

    protected RestClient client;

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/sequence";
    }

    @Override
    public void setUp() throws Exception {
        client = new RestClient();
    }

    /**
     * Calls a resource (which is not a singleton) several times. Verifies that
     * the resource instance is created each time.
     * 
     * @throws Exception
     */
    public void testHit100TimesRegularResource() throws Exception {
        ClientResponse response = client.resource(getBaseURI() + "/sequence/static").delete();
        assertEquals(204, response.getStatusCode());

        response = client.resource(getBaseURI() + "/sequence/constructor").delete();
        assertEquals(204, response.getStatusCode());

        for (int c = 0; c < 10; ++c) {
            response = client.resource(getBaseURI() + "/sequence").get();
            assertEquals(200, response.getStatusCode());
            assertEquals("0", response.getEntity(String.class));

            response = client.resource(getBaseURI() + "/sequence/static").get();
            assertEquals(200, response.getStatusCode());
            assertEquals("" + c, response.getEntity(String.class));

            response = client.resource(getBaseURI() + "/sequence").post(null);
            assertEquals(200, response.getStatusCode());
            assertEquals("1", response.getEntity(String.class));

            response = client.resource(getBaseURI() + "/sequence/static").get();
            assertEquals(200, response.getStatusCode());
            assertEquals("" + (c + 1), response.getEntity(String.class));

            response = client.resource(getBaseURI() + "/sequence/constructor").get();
            assertEquals(200, response.getStatusCode());
            assertEquals("" + ((c + 1) * 5), response.getEntity(String.class));
        }
    }

    /**
     * Calls a singleton resource several times. Verifies that the resource
     * instance is re-used each time.
     * 
     * @throws Exception
     */
    public void testHit100TimesSingletonResource() throws Exception {
        ClientResponse response =
            client.resource(getBaseURI() + "/singletonsequence/static").delete();
        assertEquals(204, response.getStatusCode());

        response = client.resource(getBaseURI() + "/singletonsequence/").delete();
        assertEquals(204, response.getStatusCode());

        for (int c = 0; c < 10; ++c) {
            response = client.resource(getBaseURI() + "/singletonsequence/").get();
            assertEquals(200, response.getStatusCode());
            assertEquals("" + c, response.getEntity(String.class));

            response = client.resource(getBaseURI() + "/singletonsequence/static").get();
            assertEquals(200, response.getStatusCode());
            assertEquals("" + c, response.getEntity(String.class));

            response = client.resource(getBaseURI() + "/singletonsequence/").post(null);
            assertEquals(200, response.getStatusCode());
            assertEquals("" + (c + 1), response.getEntity(String.class));

            response = client.resource(getBaseURI() + "/singletonsequence/static").get();
            assertEquals(200, response.getStatusCode());
            assertEquals("" + (c + 1), response.getEntity(String.class));

            /*
             * the constructor for this resource should never be more than 1.
             * note the constructor hit count is never cleared.
             */
            response = client.resource(getBaseURI() + "/singletonsequence/constructor").get();
            assertEquals(200, response.getStatusCode());
            assertEquals("1", response.getEntity(String.class));
        }
    }
}
