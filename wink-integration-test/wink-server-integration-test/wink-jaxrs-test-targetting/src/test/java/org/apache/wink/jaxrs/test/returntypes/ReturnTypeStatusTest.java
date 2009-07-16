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

package org.apache.wink.jaxrs.test.returntypes;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests return type status codes.
 */
public class ReturnTypeStatusTest extends TestCase {

    protected HttpClient httpclient = new HttpClient();

    public static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/returntypes";
    }

    final private static String BASE_URI = getBaseURI()
            + "/returntypestatus";

    /**
     * Tests that a void return type results in a response that has:
     * <ul>
     * <li>HTTP status code of 204
     * <li>empty response body
     * </ul>
     */
    public void testVoidReturnType() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/void", false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(204, result);
                assertNull(responseBody);
                /*
                 * TODO: actually make sure the method was called and not just
                 * returned/voided
                 */
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
     * Tests that a null return type on a method that returns an arbitrary
     * object results in a response that has:
     * <ul>
     * <li>HTTP status code of 204
     * <li>empty response body
     * </ul>
     */
    public void testNullObjectReturnType() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/null", false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(204, result);
                assertNull(responseBody);
                /*
                 * TODO: actually make sure the method was called and not just
                 * returned/voided
                 */
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
     * Tests that a null return on a method that is suppose to return a Response
     * object type results in a response that has:
     * <ul>
     * <li>HTTP status code of 204
     * <li>empty response body
     * </ul>
     */
    public void testNullResponseReturnType() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/nullresponse", false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(204, result);
                assertNull(responseBody);
                /*
                 * TODO: actually make sure the method was called and not just
                 * returned/voided
                 */
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
     * Tests that the status code can be set and returned correctly via a method
     * that returns a <code>Response</code> return type.
     */
    public void testStatusCodeSetResponseReturnType() {
        for (Status status : Status.values()) {
            try {
                GetMethod httpMethod = new GetMethod();
                httpMethod.setURI(new URI(BASE_URI + "/responsestatus?code="
                        + status.name(), false));
                httpclient = new HttpClient();

                try {
                    int result = httpclient.executeMethod(httpMethod);
                    System.out.println("Response status code: " + result);
                    System.out.println("Response body: ");
                    String responseBody = httpMethod.getResponseBodyAsString();
                    System.out.println(responseBody);
                    assertEquals(status.getStatusCode(), result);
                    if (status.equals(Status.NO_CONTENT)
                            || status.equals(Status.NOT_MODIFIED)) {
                        assertEquals(null, responseBody);
                    } else {
                        assertEquals("Requested status: "
                                + status.getStatusCode() + " " + status.name(),
                                responseBody);
                    }
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

    /**
     * Resource method returns a custom application response which does not have
     * the status code set but does have an entity. Expect the status code to be
     * 200 and the entity to be returned.
     */
    public void testStatusCodeNotSetResponseReturnType() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/CustomResponseStatusNotSet",
                    false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("CustomApplicationResponse", responseBody);
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
     * Resource method returns a custom application response which does not have
     * the status code set and does not have an entity. Expect the status code
     * to be 204 and no content.
     */
    public void testStatusCodeNotSetNullEntityResponseReturnType() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI
                    + "/CustomNullResponseStatusNotSet", false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(204, result);
                assertNull(responseBody);
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
     * TODO: Test GenericEntity.
     */
}
