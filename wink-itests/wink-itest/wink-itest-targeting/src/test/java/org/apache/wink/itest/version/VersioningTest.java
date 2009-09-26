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

package org.apache.wink.itest.version;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Demonstrates different versioning techniques
 */
public class VersioningTest extends TestCase {

    protected HttpClient httpclient = new HttpClient();

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/taxform";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/version" + "/taxform";
    }

    public void testVersionByAccept() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(getBaseURI(), false));
            httpMethod.setQueryString("form=1040");
            System.out.println(Arrays.asList(httpMethod.getRequestHeaders()));
            httpclient = new HttpClient();

            try {
                httpMethod.setRequestHeader("Accept", "application/taxform+2007");
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertTrue("Response does not contain expected 'deductions' element", responseBody
                    .contains("<deductions>"));
                assertEquals(200, result);

                httpMethod.setRequestHeader("Accept", "application/taxform+2008");
                result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertFalse("Response contains unexpected 'deductions' element", responseBody
                    .contains("<deductions>"));
                assertEquals(200, result);
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

    public void testVersionByQueryString() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(getBaseURI() + "/1040", false));
            httpMethod.setQueryString("version=2007");
            System.out.println(Arrays.asList(httpMethod.getRequestHeaders()));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertTrue("Response does not contain expected 'deductions' element", responseBody
                    .contains("<deductions>"));
                assertEquals(200, result);

                httpMethod.setURI(new URI(getBaseURI() + "/1040", false));
                httpMethod.setQueryString("version=2008");
                result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertFalse("Response contains unexpected 'deductions' element", responseBody
                    .contains("<deductions>"));
                assertEquals(200, result);
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

    public void testVersionByPath() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(getBaseURI() + "/1040/2007", false));
            System.out.println(Arrays.asList(httpMethod.getRequestHeaders()));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertTrue("Response does not contain expected 'deductions' element", responseBody
                    .contains("<deductions>"));
                assertEquals(200, result);

                httpMethod.setURI(new URI(getBaseURI() + "/1040/2008", false));
                result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertFalse("Response contains unexpected 'deductions' element", responseBody
                    .contains("<deductions>"));
                assertEquals(200, result);
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
