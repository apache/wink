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

import javax.ws.rs.HeaderParam;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerContainerAssertions;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests the <code>@HeaderParam</code>.
 * 
 * @see HeaderParam
 */
public class HeaderParamTest extends TestCase {

    final private static String BASE_URI = ServerEnvironmentInfo.getBaseURI() + "/params/header";

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/params";
    }

    /**
     * Tests that a custom header is sent and received properly. Uses
     * constructor, property, field, and parameter parameters.
     */
    public void testCustomHeaderParam() {
        HttpClient httpclient = new HttpClient();
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod.setURI(new URI(BASE_URI, false));
            httpMethod.setRequestHeader("customHeaderParam", "somevalue");
            httpMethod.setRequestHeader(new Header("User-Agent", "httpclient"));
            httpMethod.setRequestHeader("Accept-Language", "en");
            System.out.println(Arrays.asList(httpMethod.getRequestHeaders()));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("secret", httpMethod.getResponseHeader("custResponseHeader")
                    .getValue());
                assertEquals("getHeaderParam:somevalue;User-Agent:httpclient;Accept-Language:en;language-method:en",
                             responseBody);
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
     * Tests that headers are properly set with <code>@DefaultValue</code>s set.
     */
    public void testHeaderDefaultValue() throws IOException {
        HttpClient httpclient = new HttpClient();

        /*
         * the default values with no headers set.
         */
        GetMethod getMethod = new GetMethod(getBaseURI() + "/params/headerparam/default");
        // System.out.println(Arrays.asList(getMethod.getRequestHeaders()));

        try {
            int result = httpclient.executeMethod(getMethod);
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals(result, 200);
            assertEquals("", responseBody);
            assertEquals("MyCustomPropertyHeader", getMethod
                .getResponseHeader("RespCustomPropertyHeader").getValue());
            assertEquals("MyCustomConstructorHeader", getMethod
                .getResponseHeader("RespCustomConstructorHeader").getValue());
            assertEquals("Jakarta Commons-HttpClient/3.1", getMethod
                .getResponseHeader("RespUserAgent").getValue());
            assertEquals("english", getMethod.getResponseHeader("RespAccept-Language").getValue());
            assertEquals("MyCustomMethodHeader", getMethod
                .getResponseHeader("RespCustomMethodHeader").getValue());
            // System.out.println(Arrays.asList(getMethod.getResponseHeaders()));
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * set values for custom headers
         */
        getMethod = new GetMethod(getBaseURI() + "/params/headerparam/default");
        getMethod.setRequestHeader("CustomPropertyHeader", "setCustPropertyHeader");
        getMethod.setRequestHeader("CustomConstructorHeader", "setCustConstructorHeader");
        getMethod.setRequestHeader("Accept-Language", "da;en-gb;en");
        getMethod.setRequestHeader("CustomMethodHeader", "12345678910");
        // System.out.println(Arrays.asList(getMethod.getRequestHeaders()));

        try {
            int result = httpclient.executeMethod(getMethod);
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals(result, 200);
            assertEquals("", responseBody);
            assertEquals("setCustPropertyHeader", getMethod
                .getResponseHeader("RespCustomPropertyHeader").getValue());
            assertEquals("setCustConstructorHeader", getMethod
                .getResponseHeader("RespCustomConstructorHeader").getValue());
            assertEquals("Jakarta Commons-HttpClient/3.1", getMethod
                .getResponseHeader("RespUserAgent").getValue());
            assertEquals("da;en-gb;en", getMethod.getResponseHeader("RespAccept-Language")
                .getValue());
            assertEquals("12345678910", getMethod.getResponseHeader("RespCustomMethodHeader")
                .getValue());
            // System.out.println(Arrays.asList(getMethod.getResponseHeaders()));
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a custom header with a primitive type (int) can be used.
     */
    public void testHeaderParamPrimitiveException() throws IOException {
        HttpClient httpclient = new HttpClient();

        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/params/headerparam/exception/primitive");
        getMethod.setRequestHeader("CustomNumHeader", "314");

        try {
            int result = httpclient.executeMethod(getMethod);
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals(200, result);
            assertEquals("", responseBody);
            assertEquals("314", getMethod.getResponseHeader("RespCustomNumHeader").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/params/headerparam/exception/primitive");
        getMethod.setRequestHeader("CustomNumHeader", "abcd");

        try {
            int result = httpclient.executeMethod(getMethod);
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals(400, result);
            ServerContainerAssertions.assertExceptionBodyFromServer(400, responseBody);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a custom header with a custom constructor can be used.
     * <ul>
     * <li>If the header is not set, then the header parameter is set to null.</li>
     * <li>If the header constructor throws an exception, then 400 Bad Request
     * status is returned.</li>
     * <li>If a <code>WebApplicationException</code> is thrown during parameter
     * construction, then use that.</li>
     * </ul>
     */
    public void testHeaderParamStringConstructorException() throws IOException, HttpException {
        executeStringConstructorHeaderTest("/params/headerparam/exception/constructor",
                                           "CustomStringHeader");
    }

    /**
     * Tests that a custom header with a custom static valueOf method can be
     * used.
     * <ul>
     * <li>If the header is not set, then the header parameter is set to null.</li>
     * <li>If the header valueOf throws an exception, then 400 Bad Request
     * status is returned.</li>
     * <li>If a <code>WebApplicationException</code> is thrown during parameter
     * valueOf construction, then use that.</li>
     * </ul>
     */
    public void testHeaderParamValueOfException() throws IOException, HttpException {
        executeValueOfHeaderTest("/params/headerparam/exception/valueof", "CustomValueOfHeader");
    }

    /**
     * Tests that a custom header is set correctly in a List of a type with a
     * custom static valueOf method.
     * <ul>
     * <li>If the header is not set, then the header parameter is set to null.</li>
     * <li>If the header valueOf throws an exception, then 400 Bad Request
     * status is returned.</li>
     * <li>If a <code>WebApplicationException</code> is thrown during parameter
     * valueOf construction, then use that.</li>
     * </ul>
     */
    public void testHeaderParamListValueOfException() throws IOException {
        executeValueOfHeaderTest("/params/headerparam/exception/listvalueof",
                                 "CustomListValueOfHeader");
    }

    /**
     * Tests that a custom header is set correctly in a Set of a type with a
     * custom static valueOf method.
     * <ul>
     * <li>If the header is not set, then the header parameter is set to null.</li>
     * <li>If the header valueOf throws an exception, then 400 Bad Request
     * status is returned.</li>
     * <li>If a <code>WebApplicationException</code> is thrown during parameter
     * valueOf construction, then use that.</li>
     * </ul>
     */
    public void testHeaderParamSetValueOfException() throws IOException {
        executeValueOfHeaderTest("/params/headerparam/exception/setvalueof",
                                 "CustomSetValueOfHeader");
    }

    /**
     * Tests that a custom header is set correctly in a Set of a type with a
     * custom static valueOf method.
     * <ul>
     * <li>If the header is not set, then the header parameter is set to null.</li>
     * <li>If the header valueOf throws an exception, then 400 Bad Request
     * status is returned.</li>
     * <li>If a <code>WebApplicationException</code> is thrown during parameter
     * valueOf construction, then use that.</li>
     * </ul>
     */
    public void testHeaderParamSortedSetValueOfException() throws IOException {
        executeValueOfHeaderTest("/params/headerparam/exception/sortedsetvalueof",
                                 "CustomSortedSetValueOfHeader");
    }

    /**
     * Tests that a custom header is set correctly in a field with a String
     * constructor type.
     * <ul>
     * <li>If the header is not set, then the header parameter is set to null.</li>
     * <li>If the header valueOf throws an exception, then 400 Bad Request
     * status is returned.</li>
     * <li>If a <code>WebApplicationException</code> is thrown during parameter
     * valueOf construction, then use that.</li>
     * </ul>
     */
    public void testHeaderFieldStringConstructorException() throws IOException {
        executeStringConstructorHeaderTest("/params/headerparam/exception/fieldstrcstr",
                                           "CustomStringConstructorFieldHeader");
    }

    /**
     * Tests that a custom header is set correctly in a field with a static
     * valueOf method.
     * <ul>
     * <li>If the header is not set, then the header parameter is set to null.</li>
     * <li>If the header valueOf throws an exception, then 400 Bad Request
     * status is returned.</li>
     * <li>If a <code>WebApplicationException</code> is thrown during parameter
     * valueOf construction, then use that.</li>
     * </ul>
     */
    public void testHeaderFieldValueOfException() throws IOException {
        executeValueOfHeaderTest("/params/headerparam/exception/fieldvalueof",
                                 "CustomValueOfFieldHeader");
    }

    /**
     * Tests that a custom header is set correctly in a field with a string
     * constructor.
     * <ul>
     * <li>If the header is not set, then the header parameter is set to null.</li>
     * <li>If the header valueOf throws an exception, then 400 Bad Request
     * status is returned.</li>
     * <li>If a <code>WebApplicationException</code> is thrown during parameter
     * valueOf construction, then use that.</li>
     * </ul>
     */
    public void testHeaderPropertyStringConstructorException() throws IOException {
        executeStringConstructorHeaderTest("/params/headerparam/exception/propertystrcstr",
                                           "CustomStringConstructorPropertyHeader");
    }

    /**
     * Tests that a custom header is set correctly in a field with a type with a
     * static valueOf method.
     * <ul>
     * <li>If the header is not set, then the header parameter is set to null.</li>
     * <li>If the header valueOf throws an exception, then 400 Bad Request
     * status is returned.</li>
     * <li>If a <code>WebApplicationException</code> is thrown during parameter
     * valueOf construction, then use that.</li>
     * </ul>
     */
    public void testHeaderPropertyValueOfException() throws IOException {
        executeValueOfHeaderTest("/params/headerparam/exception/propertyvalueof",
                                 "CustomValueOfPropertyHeader");
    }

    /**
     * Tests a custom string constructor type.
     * 
     * @param path
     * @param header
     * @throws IOException
     * @throws HTTPException
     */
    private void executeStringConstructorHeaderTest(String path, String header) throws IOException {
        HttpClient httpclient = new HttpClient();

        /* normal */
        GetMethod getMethod = new GetMethod(getBaseURI() + path);
        getMethod.setRequestHeader(header, "MyCustomHeaderValue");

        try {
            int result = httpclient.executeMethod(getMethod);
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals(200, result);
            assertEquals("", responseBody);
            assertEquals("MyCustomHeaderValue", getMethod.getResponseHeader("Resp" + header)
                .getValue());
        } finally {
            getMethod.releaseConnection();
        }

        /* no header set */
        getMethod = new GetMethod(getBaseURI() + path);

        try {
            int result = httpclient.executeMethod(getMethod);
            assertEquals(500, result);
        } finally {
            getMethod.releaseConnection();
        }

        /* web app ex thrown */
        getMethod = new GetMethod(getBaseURI() + path);
        getMethod.setRequestHeader(header, "throwWeb");

        try {
            int result = httpclient.executeMethod(getMethod);
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals(499, result);
            assertEquals("HeaderStringConstructorWebAppEx", responseBody);
        } finally {
            getMethod.releaseConnection();
        }

        /* runtime exception thrown */
        getMethod = new GetMethod(getBaseURI() + path);
        getMethod.setRequestHeader(header, "throwNull");
        try {
            int result = httpclient.executeMethod(getMethod);
            assertEquals(400, result);
            ServerContainerAssertions.assertExceptionBodyFromServer(400, getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /* exception thrown */
        getMethod = new GetMethod(getBaseURI() + path);
        getMethod.setRequestHeader(header, "throwEx");
        try {
            int result = httpclient.executeMethod(getMethod);
            assertEquals(400, result);
            ServerContainerAssertions.assertExceptionBodyFromServer(400, getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests a custom valueOf header.
     * 
     * @param path the path to the resource
     * @param header the name of the header to test
     * @throws IOException
     * @throws HTTPException
     */
    private void executeValueOfHeaderTest(String path, String header) throws IOException,
        HttpException {
        HttpClient httpclient = new HttpClient();

        /* normal */
        GetMethod getMethod = new GetMethod(getBaseURI() + path);
        getMethod.setRequestHeader(header, "MyCustomHeaderValue");

        try {
            int result = httpclient.executeMethod(getMethod);
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals(200, result);
            assertEquals("", responseBody);
            assertEquals("MyCustomHeaderValue", getMethod.getResponseHeader("Resp" + header)
                .getValue());
        } finally {
            getMethod.releaseConnection();
        }

        /* no header set */
        getMethod = new GetMethod(getBaseURI() + path);

        try {
            int result = httpclient.executeMethod(getMethod);
            assertEquals(500, result);
        } finally {
            getMethod.releaseConnection();
        }

        /* web app ex thrown */
        getMethod = new GetMethod(getBaseURI() + path);
        getMethod.setRequestHeader(header, "throwWeb");

        try {
            int result = httpclient.executeMethod(getMethod);
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals(498, result);
            assertEquals("HeaderValueOfWebAppEx", responseBody);
        } finally {
            getMethod.releaseConnection();
        }

        /* runtime exception thrown */
        getMethod = new GetMethod(getBaseURI() + path);
        getMethod.setRequestHeader(header, "throwNull");
        try {
            int result = httpclient.executeMethod(getMethod);
            assertEquals(400, result);
            ServerContainerAssertions.assertExceptionBodyFromServer(400, getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /* exception thrown */
        getMethod = new GetMethod(getBaseURI() + path);
        getMethod.setRequestHeader(header, "throwEx");
        try {
            int result = httpclient.executeMethod(getMethod);
            assertEquals(400, result);
            ServerContainerAssertions.assertExceptionBodyFromServer(400, getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }
}
