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
package org.apache.wink.itest.methodannotations;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkHttpMethodWarningsTest extends TestCase {

    protected RestClient httpclient;

    @Override
    public void setUp() {
        httpclient = new RestClient();
    }

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/httpmethodwarning";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/customannotations" + "/httpmethodwarning";
    }

    /**
     * Tests that two or more <code>@HttpMethod</code> annotated annotations on
     * a method generates an error. Vague on specification but it seems to be an
     * error if two or more annotations (which each have a HttpMethod annotation
     * on them) are on the same resource method. Based on error, it is probably
     * expected that the resource is unavailable. TODO: So this test could be
     * that two custom annotations which are annotated each with
     * <code>@HttpMethod</code> are annotated on the same method.
     */
    public void testTwoOrMoreAnnotationsOnMethodError() {
        ClientResponse response = httpclient.resource(getBaseURI()).post(null);
        assertEquals(404, response.getStatusCode());

        response = httpclient.resource(getBaseURI()).put(null);
        int result = response.getStatusCode();

        String responseBody = response.getEntity(String.class);
        System.out.println(responseBody);
        /*
         * if a filter is used, then the 404 will fall through to the container
         */
        assertTrue("The result is " + result, (result == 403 && "tomcat"
            .equals(ServerEnvironmentInfo.getContainerName())) || result == 405
            || result == 404);
    }

    /**
     * Tests that non-public HttpMethod annotations generate a warning.
     */
    public void testNonPublicMethodsWarning() {
        ClientResponse response = httpclient.resource(getBaseURI() + "/abcd").post(null);
        assertEquals(404, response.getStatusCode());

        response = httpclient.resource(getBaseURI()).delete();
        int result = response.getStatusCode();
        /*
         * if a filter is used, then the 404 will fall through to the container
         */
        assertTrue("The result is " + result, (result == 403 && "tomcat"
            .equals(ServerEnvironmentInfo.getContainerName())) || result == 405
            || result == 404);
    }

}
