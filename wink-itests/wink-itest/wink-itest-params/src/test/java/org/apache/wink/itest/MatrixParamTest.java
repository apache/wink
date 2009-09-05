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
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests the <code>@MatrixParam</code>.
 */
public class MatrixParamTest extends TestCase {

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
     * Tests that no matrix parameters sent still calls proper resource.
     */
    public void testNoParam() {
        assertEquals("deleteConstructorMatrixParam:null",
                     sendGoodRequestAndGetResponse("matrix", DeleteMethod.class));
        assertEquals("getConstructorMatrixParam:null",
                     sendGoodRequestAndGetResponse("matrix", GetMethod.class));
        assertEquals("putConstructorMatrixParam:null",
                     sendGoodRequestAndGetResponse("matrix", PutMethod.class));
        assertEquals("postConstructorMatrixParam:null",
                     sendGoodRequestAndGetResponse("matrix", PostMethod.class));
    }

    /**
     * Tests the constructor matrix parameter is processed.
     */
    public void testConstructorParam() {
        assertEquals("getConstructorMatrixParam:HelloWorld",
                     sendGoodRequestAndGetResponse("matrix;cstrparam=HelloWorld", GetMethod.class));
        assertEquals("deleteConstructorMatrixParam:HelloWorld",
                     sendGoodRequestAndGetResponse("matrix;cstrparam=HelloWorld",
                                                   DeleteMethod.class));
        assertEquals("putConstructorMatrixParam:HelloWorld",
                     sendGoodRequestAndGetResponse("matrix;cstrparam=HelloWorld", PutMethod.class));
        assertEquals("postConstructorMatrixParam:HelloWorld",
                     sendGoodRequestAndGetResponse("matrix;cstrparam=HelloWorld", PostMethod.class));
    }

    /**
     * Tests both the simple constructor and method matrix parameter are
     * processed.
     */
    public void testSimpleMatrixParam() {
        assertEquals("getSimpleMatrixParam:Hello;good",
                     sendGoodRequestAndGetResponse("matrix/simple;cstrparam=Hello;life=good",
                                                   GetMethod.class));
        assertEquals("putSimpleMatrixParam:Hello;good",
                     sendGoodRequestAndGetResponse("matrix/simple;cstrparam=Hello;life=good",
                                                   PutMethod.class));
        assertEquals("postSimpleMatrixParam:Hello;good",
                     sendGoodRequestAndGetResponse("matrix/simple;cstrparam=Hello;life=good",
                                                   PostMethod.class));
        assertEquals("deleteSimpleMatrixParam:Hello;good",
                     sendGoodRequestAndGetResponse("matrix/simple;cstrparam=Hello;life=good",
                                                   DeleteMethod.class));
    }

    /**
     * Tests that a no constructor matrix parameter is set.
     */
    public void testNoConstructorMatrixParamAndSimpleMatrixParam() {
        assertEquals("deleteSimpleMatrixParam:null;erase",
                     sendGoodRequestAndGetResponse("matrix/simple;life=erase", DeleteMethod.class));
        assertEquals("getSimpleMatrixParam:null;good",
                     sendGoodRequestAndGetResponse("matrix/simple;life=good", GetMethod.class));
        assertEquals("postSimpleMatrixParam:null;new",
                     sendGoodRequestAndGetResponse("matrix/simple;life=new", PostMethod.class));
        assertEquals("putSimpleMatrixParam:null;progress",
                     sendGoodRequestAndGetResponse("matrix/simple;life=progress", PutMethod.class));
    }

    /**
     * Tests the constructor and simple matrix parameter can be out of order.
     */
    public void testOutOfOrderMatrixParam() {
        assertEquals("getSimpleMatrixParam:Hello;good",
                     sendGoodRequestAndGetResponse("matrix/simple;life=good;cstrparam=Hello;",
                                                   GetMethod.class));
        assertEquals("putSimpleMatrixParam:Hello;good",
                     sendGoodRequestAndGetResponse("matrix/simple;life=good;cstrparam=Hello;",
                                                   PutMethod.class));
        assertEquals("postSimpleMatrixParam:Hello;good",
                     sendGoodRequestAndGetResponse("matrix/simple;life=good;cstrparam=Hello",
                                                   PostMethod.class));
        assertEquals("deleteSimpleMatrixParam:Hello;good",
                     sendGoodRequestAndGetResponse("matrix/simple;life=good;cstrparam=Hello",
                                                   DeleteMethod.class));
    }

    /**
     * Tests that matrix parameters are case sensitive.
     */
    public void testLowercaseMatrixParam() {
        assertEquals("getSimpleMatrixParam:null;null",
                     sendGoodRequestAndGetResponse("matrix/simple;LIFE=good;cstrParam=Hello",
                                                   GetMethod.class));
        assertEquals("postSimpleMatrixParam:null;null",
                     sendGoodRequestAndGetResponse("matrix/simple;LIFE=good;cstrParam=Hello",
                                                   PostMethod.class));
        assertEquals("putSimpleMatrixParam:null;null",
                     sendGoodRequestAndGetResponse("matrix/simple;LIFE=good;cstrParam=Hello",
                                                   PutMethod.class));
        assertEquals("deleteSimpleMatrixParam:null;null",
                     sendGoodRequestAndGetResponse("matrix/simple;LIFE=good;cstrParam=Hello",
                                                   DeleteMethod.class));
    }

    /**
     * Tests multiple matrix parameters sent to same resource.
     */
    public void testMultipleMatrixParam() {
        assertEquals("getMultipleMatrixParam:first;capital;done",
                     sendGoodRequestAndGetResponse("matrix/multiple;1st=first;ONEMOREPARAM=capital;onemoreparam=done",
                                                   GetMethod.class));
        assertEquals("deleteMultipleMatrixParam:first;capital;done",
                     sendGoodRequestAndGetResponse("matrix/multiple;1st=first;ONEMOREPARAM=capital;onemoreparam=done",
                                                   DeleteMethod.class));
        assertEquals("postMultipleMatrixParam:first;capital;done",
                     sendGoodRequestAndGetResponse("matrix/multiple;1st=first;ONEMOREPARAM=capital;onemoreparam=done",
                                                   PostMethod.class));
        assertEquals("putMultipleMatrixParam:first;capital;done",
                     sendGoodRequestAndGetResponse("matrix/multiple;1st=first;ONEMOREPARAM=capital;onemoreparam=done",
                                                   PutMethod.class));
    }

    /**
     * Tests that primitive types are accepted in matrix parameters.
     */
    public void testPrimitiveTypedMatrixParameter() {
        assertEquals("getMatrixParameterPrimitiveTypes:false;12;3.14;3;b;1234567890;32456;123.0",
                     sendGoodRequestAndGetResponse("matrix/types/primitive;bool=false;intNumber=12;dbl=3.14;bite=3;ch=b;lng=1234567890;float=32456;short=123",
                                                   GetMethod.class));
    }

    /**
     * Tests that primitive types are accepted in parameters.
     */
    public void testParameterTypeWithStringConstructor() {
        assertEquals("getMatrixParameterStringConstructor:1234",
                     sendGoodRequestAndGetResponse("matrix/types/stringcstr;paramStringConstructor=1234",
                                                   GetMethod.class));
    }

    /**
     * Tests that primitive types are accepted in parameters.
     */
    public void testParameterTypeWithValueOfMethod() {
        assertEquals("getMatrixParameterValueOf:456789",
                     sendGoodRequestAndGetResponse("matrix/types/valueof;staticValueOf=456",
                                                   GetMethod.class));
    }

    /*
     * TODO: More tests. Need to add urlencoding tests Need to add "weird"
     * parameter tests (i.e. not standard inputs) Need to add precedence tests
     * Need to add where both constructor and major get are in the same URL Need
     * to add invalid tests. Need to add mixed tests and error tests for special
     * parameters (i.e. not strings) Need to test cases where more/less inputs
     * are given than expected
     */
}
