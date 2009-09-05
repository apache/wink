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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests various <code>@HttpMethod</code> scenarios.
 */
public class HttpMethodTest extends TestCase {

    protected HttpClient httpclient = new HttpClient();

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/customannotations";
    }

    final private static String BASE_URI = getBaseURI() + "/httpmethod";

    final private static String ALT_URI  = getBaseURI() + "/customhttpmethod";

    /**
     * Tests that it can find a custom GET HttpMethod annotation.
     */
    public void testUserDefinedGETAnnotation() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI, false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("You found my GET method!", responseBody);
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

    /**
     * Tests that an OPTIONS request can be sent to resource containing only a
     * GET method.
     */
    public void testOPTIONSRequest() {
        try {
            OptionsMethod httpMethod = new OptionsMethod();
            httpMethod.setURI(new URI(BASE_URI, false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(204, result);
                Enumeration<?> allowedMethods = httpMethod.getAllowedMethods();
                assertNotNull(allowedMethods);
                assertTrue(allowedMethods.hasMoreElements());
                List<String> methods = new ArrayList<String>();
                while (allowedMethods.hasMoreElements()) {
                    methods.add((String)allowedMethods.nextElement());
                }
                assertTrue(methods.contains("HEAD"));
                assertTrue(methods.contains("GET"));
                assertTrue(methods.contains("OPTIONS"));
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

    /**
     * Tests that a HEAD request can be sent to resource containing only a GET
     * method.
     */
    public void testHEADRequest() {
        try {
            HeadMethod httpMethod = new HeadMethod();
            httpMethod.setURI(new URI(BASE_URI, false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals(null, responseBody);
                Header[] headers = httpMethod.getResponseHeaders();
                assertNotNull(headers);
                assertTrue("Response for HEAD request contained no headers", headers.length > 0);
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

    /**
     * Tests that a HEAD request can be sent to resource annotated with a custom
     * HEAD annotation.
     */
    public void testCustomHEADRequest() {
        try {
            HeadMethod httpMethod = new HeadMethod();
            httpMethod.setURI(new URI(ALT_URI, false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals(null, responseBody);
                Header header = httpMethod.getResponseHeader("HEAD");
                assertNotNull(header);
                assertEquals("TRUE", header.getValue());
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

    /**
     * Tests that a OPTIONS request can be sent to resource annotated with a
     * custom OPTIONS annotation.
     */
    public void testCustomOPTIONSRequest() {
        try {
            OptionsMethod httpMethod = new OptionsMethod();
            httpMethod.setURI(new URI(ALT_URI, false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("", responseBody);
                Header header = httpMethod.getResponseHeader("Allow");
                assertNotNull(header);
                String value = header.getValue();
                assertTrue(value.contains("HEAD"));
                assertTrue(value.contains("OPTIONS"));
                assertTrue(value.contains("GET"));
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

    /*
     * TODO: Add more tests. - Test custom (non-standard) HTTP Method. - Test
     * OPTIONS method.
     */
}
