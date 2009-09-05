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

import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class ParamQueryNotSetTest extends TestCase {

    protected HttpClient  httpclient = new HttpClient();

    private static Random r          = new Random();

    private String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/params";
    }

    /**
     * Tests that given a HttpMethod with a query or matrix parameter, if the
     * parameter is not sent, then the default value is given back for basic
     * Java types.
     */
    public void testCharParamEmpty() throws Exception {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/char");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals('\u0000' + "", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/char?letter=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals('a' + "", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * don't send in the right query
         */
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/char?lette=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals('\u0000' + "", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/char;letter=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals('a' + "", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/char");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals('\u0000' + "", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/char;lette=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals('\u0000' + "", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that given a HttpMethod with a query or matrix parameter, if the
     * parameter is not sent, then the default value is given back for basic
     * Java types.
     */
    public void testByteParamEmpty() throws Exception {
        /*
         * query parameters
         */
        GetMethod getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/byte");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        byte b = (byte)r.nextInt(Byte.MAX_VALUE);
        System.out.println(b);
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/byte?b=" + b);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("" + b, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * don't send in the right query
         */
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/byte?b1=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * matrix parameters
         */
        b = (byte)r.nextInt(Byte.MAX_VALUE);
        System.out.println(b);
        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/byte;b=" + b);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("" + b, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/byte");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/byte;b1=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that given a HttpMethod with a query or matrix parameter, if the
     * parameter is not sent, then the default value is given back for basic
     * Java types.
     */
    public void testDoubleParamEmpty() throws Exception {
        /*
         * query parameters
         */
        GetMethod getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/double");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0.0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        double d = r.nextDouble();
        System.out.println(d);
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/double?d=" + d);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("" + d, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * don't send in the right query
         */
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/double?d1=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0.0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * matrix parameters
         */
        d = r.nextDouble();
        System.out.println(d);
        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/double;count=" + d);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("" + d, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/double");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0.0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/double;coun=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0.0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that given a HttpMethod with a query or matrix parameter, if the
     * parameter is not sent, then the default value is given back for basic
     * Java types.
     */
    public void testFloatParamEmpty() throws Exception {
        /*
         * query parameters
         */
        GetMethod getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/float");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0.0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        float f = r.nextFloat();
        System.out.println(f);
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/float?floatCount=" + f);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("" + f, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * don't send in the right query
         */
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/float?floatCount1=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0.0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * matrix parameters
         */
        f = r.nextFloat();
        System.out.println(f);
        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/float;floatCount=" + f);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("" + f, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/float");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0.0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/float;floatCoun=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0.0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that given a HttpMethod with a query or matrix parameter, if the
     * parameter is not sent, then the default value is given back for basic
     * Java types.
     */
    public void testIntParamEmpty() throws Exception {
        /*
         * query parameters
         */
        GetMethod getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/int");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        int i = r.nextInt();
        System.out.println(i);
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/int?count=" + i);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("" + i, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * don't send in the right query
         */
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/int?coun=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * matrix parameters
         */
        i = r.nextInt();
        System.out.println(i);
        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/int;count=" + i);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("" + i, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/int");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/int;coun=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that given a HttpMethod with a query or matrix parameter, if the
     * parameter is not sent, then the default value is given back for basic
     * Java types.
     */
    public void testShortParamEmpty() throws Exception {
        /*
         * query parameters
         */
        GetMethod getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/short");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        short i = (short)r.nextInt(Short.MAX_VALUE);
        System.out.println(i);
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/short?smallCount=" + i);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("" + i, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * don't send in the right query
         */
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/short?smallcount=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * matrix parameters
         */
        i = (short)r.nextInt(Short.MAX_VALUE);
        System.out.println(i);
        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/short;smallCount=" + i);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("" + i, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/short");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/short;smallCoun=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that given a HttpMethod with a query or matrix parameter, if the
     * parameter is not sent, then the default value is given back for basic
     * Java types.
     */
    public void testLongParamEmpty() throws Exception {
        /*
         * query parameters
         */
        GetMethod getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/long");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        long i = r.nextLong();
        System.out.println(i);
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/long?longCount=" + i);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("" + i, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * don't send in the right query
         */
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/long?longount=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * matrix parameters
         */
        i = r.nextLong();
        System.out.println(i);
        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/long;longCount=" + i);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("" + i, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/long");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/long;longCoun=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that given a HttpMethod with a query or matrix parameter, if the
     * parameter is not sent, then the default value is given back for basic
     * Java types.
     */
    public void testSetParamEmpty() throws Exception {
        /*
         * query parameters
         */
        GetMethod getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/set");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        int i = r.nextInt();
        System.out.println(i);
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/set?bag=" + i);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("1", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * don't send in the right query
         */
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/set?bg=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * matrix parameters
         */
        i = r.nextInt();
        System.out.println(i);
        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/set;bag=" + i);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("1", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/set");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/set;bg=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that given a HttpMethod with a query or matrix parameter, if the
     * parameter is not sent, then the default value is given back for basic
     * Java types.
     */
    public void testListParamEmpty() throws Exception {
        /*
         * query parameters
         */
        GetMethod getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/list");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        char c = 'b';
        System.out.println(c);
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/list?letter=" + c);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("1", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * don't send in the right query
         */
        getMethod = new GetMethod(getBaseURI() + "/queryparamnotset/list?lette=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * matrix parameters
         */
        System.out.println(c);
        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/list;letter=" + c);
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("1", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/list");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/matrixparamnotset/list;lette=a");
        try {
            httpclient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("0", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }
}
