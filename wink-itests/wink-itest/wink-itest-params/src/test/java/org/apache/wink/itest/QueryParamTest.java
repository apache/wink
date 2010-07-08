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

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.wink.test.integration.ServerContainerAssertions;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests the <code>@QueryParam</code> annotation on a simple JAX-RS resource.
 * 
 * @see QueryParamResource
 */
public class QueryParamTest extends TestCase {

    protected HttpClient  httpclient = new HttpClient();

    private static String BASE_URI   = ServerEnvironmentInfo.getBaseURI() + "/params/";

    static {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            BASE_URI = ServerEnvironmentInfo.getBaseURI() + "/";
        }
    }

    protected String sendGoodRequestAndGetResponse(String aPartialRequestURL,
                                                   Class<? extends HttpMethod> aClass) {
        try {
            HttpMethod httpMethod = aClass.newInstance();
            httpMethod.setURI(new URI(BASE_URI + aPartialRequestURL, false));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(result, 200);
                return responseBody;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                fail(ioe.getMessage());
            } finally {
                httpMethod.releaseConnection();
            }
        } catch (URIException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InstantiationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return null;
    }

    /**
     * Tests that no query parameters sent still calls proper resource.
     */
    public void testNoQueryParam() {
        assertEquals("deleteConstructorQueryID:null",
                     sendGoodRequestAndGetResponse("query", DeleteMethod.class));
        assertEquals("getConstructorQueryID:null", sendGoodRequestAndGetResponse("query",
                                                                                 GetMethod.class));
        assertEquals("postConstructorQueryID:null", sendGoodRequestAndGetResponse("query",
                                                                                  PostMethod.class));
        assertEquals("putConstructorQueryID:null", sendGoodRequestAndGetResponse("query",
                                                                                 PutMethod.class));
    }

    /**
     * Tests the constructor query parameter is processed.
     */
    public void testConstructorQueryParam() {
        assertEquals("deleteConstructorQueryID:HelloWorld",
                     sendGoodRequestAndGetResponse("query?queryid=HelloWorld", DeleteMethod.class));
        assertEquals("getConstructorQueryID:HelloWorld",
                     sendGoodRequestAndGetResponse("query?queryid=HelloWorld", GetMethod.class));
        assertEquals("postConstructorQueryID:HelloWorld",
                     sendGoodRequestAndGetResponse("query?queryid=HelloWorld", PostMethod.class));
        assertEquals("putConstructorQueryID:HelloWorld",
                     sendGoodRequestAndGetResponse("query?queryid=HelloWorld", PutMethod.class));
    }

    /**
     * Tests both the simple constructor and method parameter are processed.
     */
    public void testSimpleQueryParam() {
        assertEquals("deleteSimpleQueryParameter:somequeryid;hi",
                     sendGoodRequestAndGetResponse("query/simple?queryid=somequeryid&simpleParam=hi",
                                                   DeleteMethod.class));
        assertEquals("getSimpleQueryParameter:somequeryid;hi",
                     sendGoodRequestAndGetResponse("query/simple?queryid=somequeryid&simpleParam=hi",
                                                   GetMethod.class));
        assertEquals("postSimpleQueryParameter:somequeryid;hi",
                     sendGoodRequestAndGetResponse("query/simple?queryid=somequeryid&simpleParam=hi",
                                                   PostMethod.class));
        assertEquals("putSimpleQueryParameter:somequeryid;hi",
                     sendGoodRequestAndGetResponse("query/simple?queryid=somequeryid&simpleParam=hi",
                                                   PutMethod.class));
    }

    /**
     * Tests that a no constructor query parameter is set.
     */
    public void testNoConstructorQueryParamAndSimpleQueryParam() {
        assertEquals("deleteSimpleQueryParameter:null;hi",
                     sendGoodRequestAndGetResponse("query/simple/?simpleParam=hi",
                                                   DeleteMethod.class));
        assertEquals("getSimpleQueryParameter:null;hi",
                     sendGoodRequestAndGetResponse("query/simple/?simpleParam=hi", GetMethod.class));
        assertEquals("postSimpleQueryParameter:null;hi",
                     sendGoodRequestAndGetResponse("query/simple/?simpleParam=hi", PostMethod.class));
        assertEquals("putSimpleQueryParameter:null;hi",
                     sendGoodRequestAndGetResponse("query/simple/?simpleParam=hi", PutMethod.class));
    }

    /**
     * Tests the constructor and simple query parameter can be out of order.
     */
    public void testOutOfOrderSimpleQueryParam() {
        assertEquals("deleteSimpleQueryParameter:somequeryid;hi",
                     sendGoodRequestAndGetResponse("query/simple?simpleParam=hi&queryid=somequeryid",
                                                   DeleteMethod.class));
        assertEquals("getSimpleQueryParameter:somequeryid;hi",
                     sendGoodRequestAndGetResponse("query/simple?simpleParam=hi&queryid=somequeryid",
                                                   GetMethod.class));
        assertEquals("postSimpleQueryParameter:somequeryid;hi",
                     sendGoodRequestAndGetResponse("query/simple?simpleParam=hi&queryid=somequeryid",
                                                   PostMethod.class));
        assertEquals("putSimpleQueryParameter:somequeryid;hi",
                     sendGoodRequestAndGetResponse("query/simple?simpleParam=hi&queryid=somequeryid",
                                                   PutMethod.class));
    }

    /**
     * Tests that query parameters are case sensitive.
     */
    public void testLowercaseQueryParam() {
        assertEquals("getSimpleQueryParameter:null;null",
                     sendGoodRequestAndGetResponse("query/simple/?simpleparam=hi&QUERYID=abcd",
                                                   GetMethod.class));
        assertEquals("postSimpleQueryParameter:null;null",
                     sendGoodRequestAndGetResponse("query/simple/?simpleparam=hi&QUERYID=abcd",
                                                   PostMethod.class));
        assertEquals("putSimpleQueryParameter:null;null",
                     sendGoodRequestAndGetResponse("query/simple/?simpleparam=hi&QUERYID=abcd",
                                                   PutMethod.class));
        assertEquals("deleteSimpleQueryParameter:null;null",
                     sendGoodRequestAndGetResponse("query/simple/?simpleparam=hi&QUERYID=abcd",
                                                   DeleteMethod.class));
    }

    /**
     * Tests multiple query parameters sent to same resource.
     */
    public void testMultipleQueryParam() {
        assertEquals("getMultiQueryParameter:somequeryid;hi;789;1moreparam2go",
                     sendGoodRequestAndGetResponse("query/multiple?queryid=somequeryid&multiParam1=hi&123Param=789&1MOREParam=1moreparam2go",
                                                   GetMethod.class));
        assertEquals("deleteMultiQueryParameter:somequeryid;hi;789;1moreparam2go",
                     sendGoodRequestAndGetResponse("query/multiple?queryid=somequeryid&multiParam1=hi&123Param=789&1MOREParam=1moreparam2go",
                                                   DeleteMethod.class));
        assertEquals("putMultiQueryParameter:somequeryid;hi;789;1moreparam2go",
                     sendGoodRequestAndGetResponse("query/multiple?queryid=somequeryid&multiParam1=hi&123Param=789&1MOREParam=1moreparam2go",
                                                   PutMethod.class));
        assertEquals("postMultiQueryParameter:somequeryid;hi;789;1moreparam2go",
                     sendGoodRequestAndGetResponse("query/multiple?queryid=somequeryid&multiParam1=hi&123Param=789&1MOREParam=1moreparam2go",
                                                   PostMethod.class));
    }

    /**
     * Tests that primitive types are accepted in query parameters.
     */
    public void testPrimitiveTypedQueryParameter() {
        assertEquals("getQueryParameterPrimitiveTypes:false;12;3.14;3;b;1234567890;32456;123.0",
                     sendGoodRequestAndGetResponse("query/types/primitive?bool=false&intNumber=12&dbl=3.14&bite=3&ch=b&lng=1234567890&float=32456&short=123",
                                                   GetMethod.class));
    }

    /**
     * Tests that primitive types are accepted in query parameters.
     */
    public void testParameterTypeWithStringConstructor() {
        assertEquals("getQueryParameterStringConstructor:1234",
                     sendGoodRequestAndGetResponse("query/types/stringcstr?paramStringConstructor=1234",
                                                   GetMethod.class));
    }

    /**
     * Tests that primitive types are accepted in query parameters.
     */
    public void testParameterTypeWithValueOfMethod() {
        assertEquals("getQueryParameterValueOf:456789",
                     sendGoodRequestAndGetResponse("query/types/valueof?staticValueOf=456",
                                                   GetMethod.class));
    }

    public void testQueryParamException() throws Exception {
        HttpClient httpclient = new HttpClient();

        /*
         * query constructor field exceptions
         */
        GetMethod httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/fieldstrcstr?CustomStringConstructorFieldQuery=throwWeb");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(499, httpMethod.getStatusCode());
            assertEquals("ParamStringConstructor", httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/fieldstrcstr?CustomStringConstructorFieldQuery=throwNull");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(404, httpMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, httpMethod
                .getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/fieldstrcstr?CustomStringConstructorFieldQuery=throwEx");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(404, httpMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, httpMethod
                .getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        /*
         * query value of field exceptions
         */
        httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/fieldvalueof?CustomValueOfFieldQuery=throwWeb");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(498, httpMethod.getStatusCode());
            assertEquals("ParamValueOfWebAppEx", httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/fieldvalueof?CustomValueOfFieldQuery=throwNull");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(404, httpMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, httpMethod
                .getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/fieldvalueof?CustomValueOfFieldQuery=throwEx");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(404, httpMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, httpMethod
                .getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        /*
         * query string constructor property exceptions
         */
        httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/propertystrcstr?CustomStringConstructorPropertyHeader=throwWeb");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(499, httpMethod.getStatusCode());
            assertEquals("ParamStringConstructor", httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/propertystrcstr?CustomStringConstructorPropertyHeader=throwNull");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(404, httpMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, httpMethod
                .getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/propertystrcstr?CustomStringConstructorPropertyHeader=throwEx");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(404, httpMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, httpMethod
                .getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        /*
         * query value of property exceptions
         */
        httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/propertyvalueof?CustomValueOfPropertyHeader=throwWeb");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(498, httpMethod.getStatusCode());
            assertEquals("ParamValueOfWebAppEx", httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/propertyvalueof?CustomValueOfPropertyHeader=throwNull");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(404, httpMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, httpMethod
                .getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/propertyvalueof?CustomValueOfPropertyHeader=throwEx");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(404, httpMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, httpMethod
                .getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod =
            new GetMethod(
                          BASE_URI + "params/queryparam/exception/primitive?CustomNumQuery=notANumber");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(404, httpMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, httpMethod
                .getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }
    }
}
