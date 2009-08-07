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

package org.apache.wink.itest.headers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class HeadersTest extends TestCase {

    private HttpClient          httpClient;

    final private static String BASE_URI = getBaseURI() + "/headers";

    public static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/headers";
    }

    public void testGetWithCookies() throws Exception {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/cookie", false));
            httpClient = new HttpClient();
            httpMethod.setRequestHeader("Cookie", "$Version=\"1\";login=\"jdoe\"");
            try {
                int result = httpClient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                assertEquals(200, result);
                assertNotNull(httpMethod.getResponseHeader("login"));
                assertEquals("jdoe", httpMethod.getResponseHeader("login").getValue());
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

    public void testGetWithLanguage() throws Exception {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/language", false));
            httpClient = new HttpClient();
            httpMethod.setRequestHeader("Content-Language", "en-us");
            try {
                int result = httpClient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                assertEquals(200, result);
                assertNotNull(httpMethod.getResponseHeader("language"));
                assertEquals("en:US", httpMethod.getResponseHeader("language").getValue());
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

    public void testGetWithContent() throws Exception {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/content", false));
            httpClient = new HttpClient();
            httpMethod.setRequestHeader("Content-Type", "application/html");
            try {
                int result = httpClient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                assertEquals(200, result);
                assertNotNull(httpMethod.getResponseHeader("content"));
                assertEquals("application/html", httpMethod.getResponseHeader("content").getValue());
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

    public void testGetWithAccept() throws Exception {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/accept", false));
            httpClient = new HttpClient();
            httpMethod.setRequestHeader("Accept", "text/*, text/html, text/html;level=1, */*");
            try {
                int result = httpClient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                assertEquals(200, result);
                assertNotNull(httpMethod.getResponseHeader("test-accept"));

                // all q-values = 1, should come back like it was sent
                String value = httpMethod.getResponseHeader("test-accept").getValue();
                assertTrue(value, value.endsWith("*/*"));
                assertTrue(value, value.contains("text/*"));
                assertTrue(value, value.contains("text/html"));
                assertTrue(value, value.contains("text/html;level=1"));
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

    public void testGetWithAcceptLanguage() throws Exception {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/acceptlang", false));
            httpClient = new HttpClient();
            httpMethod.setRequestHeader("Accept-Language", "fr");
            try {
                int result = httpClient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                assertEquals(200, result);
                assertNotNull(httpMethod.getResponseHeader("acceptlang"));
                assertEquals("fr", httpMethod.getResponseHeader("acceptlang").getValue());
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

    public void testGetHeadersWithCase() throws Exception {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/headercase", false));
            httpClient = new HttpClient();
            httpMethod.setRequestHeader("Custom-Header", "MyValue");
            try {
                int result = httpClient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                assertEquals(200, result);
                assertNotNull(httpMethod.getResponseHeader("Custom-Header"));
                assertEquals("MyValue", httpMethod.getResponseHeader("Custom-Header").getValue());
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

    public void testGetHeadersAcceptAsParam() throws Exception {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/headeraccept", false));
            httpClient = new HttpClient();
            httpMethod.setRequestHeader("Accept", "text/xml");
            try {
                int result = httpClient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                assertEquals(200, result);
                assertNotNull(httpMethod.getResponseHeader("test-accept"));
                assertEquals("text/xml", httpMethod.getResponseHeader("test-accept").getValue());
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

    public void testGetHeadersAcceptAsArg() throws Exception {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/headersasarg", false));
            httpClient = new HttpClient();
            httpMethod.setRequestHeader("Accept", "text/xml application/xml");
            httpMethod.setRequestHeader("Content-Type", "application/xml");
            try {
                int result = httpClient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                assertEquals(200, result);
                assertNotNull(httpMethod.getResponseHeader("test-accept"));
                assertEquals("text/xml application/xml", httpMethod
                    .getResponseHeader("test-accept").getValue());
                assertNotNull(httpMethod.getResponseHeader("test-content-type"));
                assertEquals("application/xml", httpMethod.getResponseHeader("test-content-type")
                    .getValue());
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

    public void testAllowHeaders() throws Exception {
        try {
            OptionsMethod httpMethod = new OptionsMethod();
            httpMethod.setURI(new URI(getBaseURI() + "/headersallow1/allow1", false));
            httpClient = new HttpClient();
            try {
                int result = httpClient.executeMethod(httpMethod);
                assertEquals(204, result);
                assertNotNull(httpMethod.getResponseHeader("Allow"));
                List<String> allowedMethods =
                    Arrays.asList(httpMethod.getResponseHeader("Allow").getValue().split(", "));
                System.out.println(allowedMethods);
                assertEquals(3, allowedMethods.size());
                assertTrue(allowedMethods.contains("HEAD"));
                assertTrue(allowedMethods.contains("OPTIONS"));
                assertTrue(allowedMethods.contains("GET"));
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

        try {
            OptionsMethod httpMethod = new OptionsMethod();
            httpMethod.setURI(new URI(getBaseURI() + "/headersallow2", false));
            httpClient = new HttpClient();
            try {
                int result = httpClient.executeMethod(httpMethod);
                assertEquals(204, result);
                assertNotNull(httpMethod.getResponseHeader("Allow"));
                List<String> allowedMethods =
                    Arrays.asList(httpMethod.getResponseHeader("Allow").getValue().split(", "));
                System.out.println(allowedMethods);
                assertEquals(6, allowedMethods.size());
                assertTrue(allowedMethods.contains("HEAD"));
                assertTrue(allowedMethods.contains("OPTIONS"));
                assertTrue(allowedMethods.contains("GET"));
                assertTrue(allowedMethods.contains("PUT"));
                assertTrue(allowedMethods.contains("POST"));
                assertTrue(allowedMethods.contains("DELETE"));
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
