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

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests various errors and warnings in <code>@HTTPMethod</code> conditions.
 */
public class HttpMethodWarningsTest extends TestCase {

    protected HttpClient httpclient = new HttpClient();

    public static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/customannotations";
    }

    final private static String BASE_URI = getBaseURI() + "/httpmethodwarning";

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
        try {
            PostMethod httpMethod = new PostMethod();
            httpMethod.setURI(new URI(BASE_URI, false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(404, result);
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
            PutMethod putMethod = new PutMethod();
            putMethod.setURI(new URI(BASE_URI, false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(putMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = putMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(404, result);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                fail(ioe.getMessage());
            } finally {
                putMethod.releaseConnection();
            }
        } catch (URIException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests that non-public HttpMethod annotations generate a warning.
     */
    public void testNonPublicMethodsWarning() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "/abcd", false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(404, result);
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
            DeleteMethod httpMethod = new DeleteMethod();
            httpMethod.setURI(new URI(BASE_URI, false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(404, result);
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
