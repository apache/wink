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
package org.apache.wink.itest.lifecycles;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpException;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkClientLifeCycleTest extends TestCase {

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/lifecycles";
    }

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Tests that providers are singletons no matter what.
     * 
     * @throws HttpException
     * @throws IOException
     * @throws URISyntaxException
     */
    public void testProvidersAreSingleton() throws URISyntaxException {
        StringBuffer sb = new StringBuffer();
        for (long c = 0; c < 5000; ++c) {
            sb.append("a");
        }

        ClientResponse response =
            client.resource(getBaseURI() + "/jaxrs/tests/lifecycles").delete();
        assertEquals(204, response.getStatusCode());

        for (int counter = 0; counter < 100; ++counter) {
            String responseBody =
                client.resource(new URI(getBaseURI() + "/jaxrs/tests/lifecycles"))
                    .contentType("text/plain").post(String.class, sb.toString());
            assertEquals(sb.toString(), responseBody);
        }

        response = client.resource(getBaseURI() + "/jaxrs/tests/lifecycles").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("1:100:100:101:100:1", response.getEntity(String.class));
    }
}
