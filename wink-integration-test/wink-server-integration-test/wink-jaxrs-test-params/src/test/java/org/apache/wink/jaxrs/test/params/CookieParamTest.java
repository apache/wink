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

package org.apache.wink.jaxrs.test.params;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests the use of cookie parameter.
 */
public class CookieParamTest extends TestCase {

    protected HttpClient        httpclient = new HttpClient();

    final private static String BASE_URI   =
                                               ServerEnvironmentInfo.getBaseURI() + "/params/cookiemonster";

    /**
     * Tests that a cookie parameter is retrieved.
     */
    public void testCookieParam() {

        try {
            PutMethod httpMethod = new PutMethod();
            httpMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
            httpMethod.setURI(new URI(BASE_URI, false));
            System.out.println("Request headers:");
            System.out.println(Arrays.asList(httpMethod.getRequestHeaders()));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                System.out.println("Response headers:");
                System.out.println(Arrays.asList(httpMethod.getResponseHeaders()));
                assertEquals(200, result);
                assertEquals("swiped:" + 0, responseBody);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                fail(ioe.getMessage());
            } finally {
                httpMethod.releaseConnection();
            }

            System.out.println("---");

            httpMethod = new PutMethod();
            httpMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
            httpMethod.setURI(new URI(BASE_URI, false));
            httpMethod.setRequestHeader("Cookie", "jar=1");
            System.out.println("Request headers:");
            System.out.println(Arrays.asList(httpMethod.getRequestHeaders()));
            httpclient = new HttpClient();
            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                System.out.println("Response headers:");
                System.out.println(Arrays.asList(httpMethod.getResponseHeaders()));
                assertEquals(200, result);
                assertEquals("swiped:" + 1, responseBody);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                fail(ioe.getMessage());
            } finally {
                httpMethod.releaseConnection();
            }
        } catch (URIException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
