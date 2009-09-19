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

package org.apache.wink.itest.uriinfo;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkURIInfoNormalizationTest extends TestCase {

    private static String appRoot = "/uriinfo";

    static {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            appRoot = "";
        }
    }

    public String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + appRoot;
    }

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Tests that a normal "good" path is returned.
     * 
     * @throws Exception
     */
    public void testPathNormal() throws Exception {
        ClientResponse response = client.resource(getBaseURI() + "/uriinfo?info=path").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("uriinfo", response.getEntity(String.class));
    }

    /**
     * Tests that a path which removes the initial path to the resource class
     * but adds it back in is okay.
     * 
     * @throws Exception
     */
    public void testRemoveResourcePathThenAddItBack() throws Exception {
        ClientResponse response =
            client.resource(getBaseURI() + "/uriinfo/../uriinfo" + "?info=path").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("uriinfo", response.getEntity(String.class));

        response = client.resource(getBaseURI() + "/uriinfo/." + "?info=path").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("uriinfo/", response.getEntity(String.class));

        response = client.resource(getBaseURI() + "/uriinfo/./" + "?info=path").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("uriinfo/", response.getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/uriinfo/./.././uriinfo/./" + "?info=path").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("uriinfo/", response.getEntity(String.class));

        response = client.resource(getBaseURI() + "/uriinfo/../uriinfo/" + "?info=path").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("uriinfo/", response.getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/uriinfo/sub/../uriinfo/../sub" + "?info=path").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("uriinfo/sub", response.getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/uriinfo/sub/../uriinfo/../sub/" + "?info=path").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("uriinfo/sub/", response.getEntity(String.class));
    }

    /**
     * Tests adding some extra paths to resource paths and then removing them.
     * 
     * @throws Exception
     */
    public void testAddPathThenRemoveIt() throws Exception {
        ClientResponse response =
            client.resource(getBaseURI() + "/uriinfo/something/../" + "?info=path").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("uriinfo/", response.getEntity(String.class));

        response = client.resource(getBaseURI() + "/uriinfo/something/.." + "?info=path").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("uriinfo/", response.getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/uriinfo/sub/../uriinfo/../sub/something/../"
                + "?info=path").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("uriinfo/sub/", response.getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/uriinfo/sub/../uriinfo/../sub/something/.."
                + "?info=path").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("uriinfo/sub/", response.getEntity(String.class));
    }

    /**
     * Tests that the capitalization is correct.
     * 
     * @throws Exception
     */
    public void testCapitalization() throws Exception {
        String contextRoot = ServerEnvironmentInfo.getContextRoot();
        if (!"".equals(contextRoot)) {
            contextRoot = "/" + contextRoot;
        }

        ClientResponse response =
            client.resource(getBaseURI() + "/uriinfo/something/../" + "?info=host").get();
        assertEquals(200, response.getStatusCode());
        assertEquals(ServerEnvironmentInfo.getHostname().toLowerCase(), response
            .getEntity(String.class));

        /*
         * should be the same as first test above
         */
        response =
            client.resource("http://" + ServerEnvironmentInfo.getHostname()
                + ":"
                + ServerEnvironmentInfo.getPort()
                + contextRoot
                + appRoot
                + "/uriinfo/something/../"
                + "?info=host").get();
        assertEquals(200, response.getStatusCode());
        assertEquals(ServerEnvironmentInfo.getHostname().toLowerCase(), response
            .getEntity(String.class));

        /*
         * uppercased
         */
        response =
            client.resource("http://" + ServerEnvironmentInfo.getHostname().toUpperCase()
                + ":"
                + ServerEnvironmentInfo.getPort()
                + contextRoot
                + appRoot
                + "/uriinfo/something/../"
                + "?info=host").get();
        assertEquals(200, response.getStatusCode());
        assertEquals(ServerEnvironmentInfo.getHostname().toUpperCase(), response
            .getEntity(String.class));

        /*
         * uppercased
         */
        response =
            client.resource("HTTP://" + ServerEnvironmentInfo.getHostname().toUpperCase()
                + ":"
                + ServerEnvironmentInfo.getPort()
                + contextRoot
                + appRoot
                + "/uriinfo/something/../"
                + "?info=protocol").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("http", response.getEntity(String.class));
    }

    /**
     * Tests that the capitalization is correct.
     * 
     * @throws Exception
     */
    public void testPercentEncoding() throws Exception {
        /*
         * regular query
         */
        ClientResponse response =
            client.resource(getBaseURI() + "/uriinfo/something/../" + "?info=query&hello1=%3F")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("info=query&hello1=?", response.getEntity(String.class));

        /*
         * raw query
         */
        response =
            client.resource(getBaseURI()
                + "/uriinfo/something/../"
                + "?info=rawquery&hello1=%3F").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("info=rawquery&hello1=%3F", response.getEntity(String.class));

        response =
            client.resource(getBaseURI()
                + "/uriinfo/something/../"
                + "?info=query&hello%31=%3F").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("info=query&hello1=?", response.getEntity(String.class));

        /*
         * %75 should eventually be normalized to /uriinfo/something %31 should
         * be normalized to 1
         */
        response =
            client.resource(getBaseURI()
                + "/%75riinfo/something/../"
                + "?info=rawquery&hello%31=%3F").get();
        assertEquals(200, response.getStatusCode());
        /*
         * in this case, the %31 should remain encoded
         */
        assertEquals("info=rawquery&hello%31=%3F", response.getEntity(String.class));
    }
}
