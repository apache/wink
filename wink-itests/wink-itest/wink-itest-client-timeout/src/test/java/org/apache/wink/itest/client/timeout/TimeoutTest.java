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

package org.apache.wink.itest.client.timeout;

import junit.framework.TestCase;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.ClientRuntimeException;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class TimeoutTest extends TestCase {

    public static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/timeout";
    }

    /**
     * Test that the wink.client.readTimeout property value of 10000 is read
     */
    public void testReadTimeoutValue() {
        assertEquals(10000, getDefaultClient().getConfig().getReadTimeout());
    }

    /**
     * Test that the wink.client.connectTimeout property value of 10000 is read
     */
    public void testConnectTimeoutValue() {
        assertEquals(10000, getDefaultClient().getConfig().getConnectTimeout());
    }

    /**
     * Test that the system property for the read timeout can be overwritten
     * programatically
     */
    public void testOverrideReadTimeout() {
        assertEquals(20000, getReadTimeoutClient().getConfig().getReadTimeout());
    }

    /**
     * Test that the system property for the connect timeout can be overwritten
     * programmatically
     */
    public void testOverrideConnectTimeout() {
        assertEquals(20000, getConnectTimeoutClient().getConfig().getConnectTimeout());
    }

    /**
     * Test that a request is processed if it takes less time than the timeout
     * value
     */
    public void testReadTimeoutNoTimeout() {
        RestClient client = getDefaultClient();
        Resource resource = client.resource(getBaseURI() + "?timeout=5000");
        ClientResponse response = resource.get();
        assertEquals(200, response.getStatusCode());
        assertEquals("request processed", response.getEntity(String.class));
    }

    /**
     * Test that the client times out if the request is not processed in less
     * than the readTimeout value
     */
    public void testReadTimeoutTimeout() {
        RestClient client = getDefaultClient();
        Resource resource = client.resource(getBaseURI() + "?timeout=30000");
        try {
            resource.get();
            fail("The client did not timeout after waiting more than 20000 milliseconds for the request.");
        } catch (ClientRuntimeException e) {
            assertTrue(e.getMessage().indexOf("SocketTimeoutException") != -1);
        }
    }

    public RestClient getDefaultClient() {
        return new RestClient();
    }

    public RestClient getReadTimeoutClient() {
        ClientConfig config = new ClientConfig();
        config.readTimeout(20000);
        return new RestClient(config);
    }

    public RestClient getConnectTimeoutClient() {
        ClientConfig config = new ClientConfig();
        config.connectTimeout(20000);
        return new RestClient(config);
    }
}
