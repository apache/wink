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

package org.apache.wink.jaxrs.test.newcookie;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Test that all the possible cookie fields can be sent by the runtime
 */
public class CookieFieldsTest extends TestCase {

    protected HttpClient        httpclient = new HttpClient();

    final private static String BASE_URI   =
                                               ServerEnvironmentInfo.getBaseURI() + "/newcookies/cookiestests";

    /**
     * Test that the HttpHeaders.getCookies() method returns correct cookies and
     * information
     * 
     * @throws Exception
     */
    public void testHttpHeadersGetCookie() throws Exception {
        httpclient = new HttpClient();
        setCookies();
        // call get to exercise HttpHeaders.getCookies()
        GetMethod getHttpMethod = new GetMethod();
        getHttpMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
        getHttpMethod.setURI(new URI(BASE_URI + "/getAll", false));
        System.out.println("Request headers:");
        System.out.println(Arrays.asList(getHttpMethod.getRequestHeaders()));
        try {
            int result = httpclient.executeMethod(getHttpMethod);
            System.out.println("Response status code: " + result);
            System.out.println("Response body: ");
            String responseBody = getHttpMethod.getResponseBodyAsString();
            System.out.println(responseBody);
            System.out.println("Response headers:");
            List<Header> headers = Arrays.asList(getHttpMethod.getResponseHeaders());
            System.out.println(headers);
            assertEquals(200, result);

            StringTokenizer t = new StringTokenizer(responseBody, "\r\n\t\f");
            String next = null;
            boolean name3Found = false;
            boolean name2Found = false;
            boolean nameFound = false;
            String contextRoot = ServerEnvironmentInfo.getContextRoot();
            if (!"".equals(contextRoot)) {
                contextRoot = "/" + contextRoot;
            }
            while (t.hasMoreTokens()) {
                next = t.nextToken();
                if (next.startsWith("name3")) {
                    assertEquals("name3,value3," + contextRoot
                        + "/newcookies/cookiestests,"
                        + ServerEnvironmentInfo.getHostname(), next);
                    name3Found = true;
                } else if (next.startsWith("name2")) {
                    assertEquals("name2,value2," + contextRoot
                        + "/newcookies/cookiestests,"
                        + ServerEnvironmentInfo.getHostname(), next);
                    name2Found = true;
                } else if (next.startsWith("name")) {
                    assertEquals("name,value," + contextRoot
                        + "/newcookies/cookiestests,"
                        + ServerEnvironmentInfo.getHostname(), next);
                    nameFound = true;
                } else
                    fail("Received an unexpected cookie: " + next);
            }
            if (!nameFound || !name2Found || !name3Found)
                fail("Did not receive all the expected cookies." + nameFound
                    + name2Found
                    + name3Found);
        } finally {
            getHttpMethod.releaseConnection();
        }
    }

    /**
     * Test the @CookieParameter annotation on a private class field
     * 
     * @throws Exception
     */
    public void testCookieParamPrivateVar() throws Exception {
        httpclient = new HttpClient();
        setCookies();
        GetMethod getHttpMethod = new GetMethod();
        getHttpMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
        getHttpMethod.setURI(new URI(BASE_URI + "/getValue2", false));
        System.out.println("Request headers:");
        System.out.println(Arrays.asList(getHttpMethod.getRequestHeaders()));
        try {
            int result = httpclient.executeMethod(getHttpMethod);
            System.out.println("Response status code: " + result);
            System.out.println("Response body: ");
            String responseBody = getHttpMethod.getResponseBodyAsString();
            System.out.println(responseBody);
            System.out.println("Response headers:");
            List<Header> headers = Arrays.asList(getHttpMethod.getResponseHeaders());
            System.out.println(headers);
            assertEquals(400, result);
            assertEquals("value2", responseBody.trim());
        } finally {
            getHttpMethod.releaseConnection();
        }
    }

    // /**
    // * Test the @CookieParameter annotation on a static class field
    // *
    // * @throws Exception
    // */
    // public void testCookieParamStaticField() throws Exception {
    // httpclient = new HttpClient();
    // setCookies();
    // GetMethod getHttpMethod = new GetMethod();
    // getHttpMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
    // getHttpMethod.setURI(new URI(BASE_URI+"/getStaticValue", false));
    // System.out.println("Request headers:");
    // System.out.println(Arrays.asList(getHttpMethod.getRequestHeaders()));
    // try {
    // int result = httpclient.executeMethod(getHttpMethod);
    // System.out.println("Response status code: " + result);
    // System.out.println("Response body: ");
    // String responseBody = getHttpMethod.getResponseBodyAsString();
    // System.out.println(responseBody);
    // System.out.println("Response headers:");
    // List<Header> headers = Arrays.asList(getHttpMethod.getResponseHeaders());
    // System.out.println(headers);
    // assertEquals(400, result);
    // assertEquals("value", responseBody.trim());
    // } finally {
    // getHttpMethod.releaseConnection();
    // }
    // }

    /**
     * Test the @CookieParameter annotation bean property
     * 
     * @throws Exception
     */
    // public void testCookieParamBeanProp() throws Exception {
    // httpclient = new HttpClient();
    // setCookies();
    // GetMethod getHttpMethod = new GetMethod();
    // getHttpMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
    // getHttpMethod.setURI(new URI(BASE_URI+"/getValue3", false));
    // System.out.println("Request headers:");
    // System.out.println(Arrays.asList(getHttpMethod.getRequestHeaders()));
    // try {
    // int result = httpclient.executeMethod(getHttpMethod);
    // System.out.println("Response status code: " + result);
    // System.out.println("Response body: ");
    // String responseBody = getHttpMethod.getResponseBodyAsString();
    // System.out.println(responseBody);
    // System.out.println("Response headers:");
    // List<Header> headers = Arrays.asList(getHttpMethod.getResponseHeaders());
    // System.out.println(headers);
    // assertEquals(400, result);
    // assertEquals("value3", responseBody.trim());
    // } finally {
    // getHttpMethod.releaseConnection();
    // }
    // }
    private void setCookies() throws Exception {
        // call put to set the cookies
        PutMethod putHttpMethod = new PutMethod();
        putHttpMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
        putHttpMethod.setURI(new URI(BASE_URI, false));
        System.out.println("Request headers:");
        System.out.println(Arrays.asList(putHttpMethod.getRequestHeaders()));
        try {
            int result = httpclient.executeMethod(putHttpMethod);
            System.out.println("Response status code: " + result);
            System.out.println("Response body: ");
            String responseBody = putHttpMethod.getResponseBodyAsString();
            System.out.println(responseBody);
            System.out.println("Response headers:");
            List<Header> headers = Arrays.asList(putHttpMethod.getResponseHeaders());
            System.out.println(headers);
            assertEquals(200, result);
        } finally {
            putHttpMethod.releaseConnection();
        }
    }
}
