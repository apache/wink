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

package org.apache.wink.itest;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests <code>@DefaultValue</code> annotation.
 */
public class DefaultValueParamTest extends TestCase {

    protected HttpClient  httpclient = new HttpClient();

    private static String BASE_URI   = ServerEnvironmentInfo.getBaseURI() + "/params/defaultvalue";

    static {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            BASE_URI = ServerEnvironmentInfo.getBaseURI() + "/defaultvalue";
        }
    }

    /**
     * Test that if no parameters are passed, the default values are used.
     */
    public void testDefaultValue() {

        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI, false));
            System.out.println(Arrays.asList(httpMethod.getRequestHeaders()));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getRow:" + "offset="
                    + "0"
                    + ";version="
                    + "1.0"
                    + ";limit="
                    + "100"
                    + ";sort="
                    + "normal", responseBody);
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
     * Test using some default values.
     */
    public void testUseSomeDefaultValue() {

        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI + "?sort=backward&offset=314", false));
            System.out.println(Arrays.asList(httpMethod.getRequestHeaders()));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getRow:" + "offset="
                    + "314"
                    + ";version="
                    + "1.0"
                    + ";limit="
                    + "100"
                    + ";sort="
                    + "backward", responseBody);
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
