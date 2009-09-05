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
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests that <code>@Encoded</code> annotated method and parameter level works.
 */
public class EncodingParamTest extends TestCase {

    protected HttpClient        httpclient      = new HttpClient();

    private static String BASE_URI_DECODE =
                                                    ServerEnvironmentInfo.getBaseURI() + "/params/decodedparams";

    private static String BASE_URI_ENCODE =
                                                    ServerEnvironmentInfo.getBaseURI() + "/params/encodingparam";

    static {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            BASE_URI_DECODE = ServerEnvironmentInfo.getBaseURI() + "/decodedparams";
            BASE_URI_ENCODE = ServerEnvironmentInfo.getBaseURI() + "/encodingparam";
        }
    }

    // /**
    // * Test that if regular parameters are passed, the parameters are correct.
    // */
    // public void testRegularParametersEncodedMethod() {
    // try {
    // GetMethod httpMethod = new GetMethod();
    // httpMethod.setURI(new URI(BASE_URI_ENCODE
    // + "/city/Austin/;appversion=1.1?q=Pizza", false));
    // httpclient = new HttpClient();
    //
    // try {
    // int result = httpclient.executeMethod(httpMethod);
    // System.out.println("Response status code: " + result);
    // System.out.println("Response body: ");
    // String responseBody = httpMethod.getResponseBodyAsString();
    // System.out.println(responseBody);
    // assertEquals(200, result);
    // assertEquals("getShopInCity:q=Pizza;city=Austin;appversion=1.1",
    // responseBody);
    // } catch (IOException ioe) {
    // ioe.printStackTrace();
    // fail(ioe.getMessage());
    // } finally {
    // httpMethod.releaseConnection();
    // }
    // } catch (URIException e) {
    // e.printStackTrace();
    // fail(e.getMessage());
    // }
    // }

    // /**
    // * Tests that if
    // */
    // public void testEncodedMethod() {
    // try {
    // GetMethod httpMethod = new GetMethod();
    // httpMethod.setURI(new URI(BASE_URI_ENCODE
    // + "/city/Earth, TX/;appversion=1.1+?q=Dave %26 Buster's",
    // false));
    // httpclient = new HttpClient();
    //
    // try {
    // int result = httpclient.executeMethod(httpMethod);
    // System.out.println("Response status code: " + result);
    // System.out.println("Response body: ");
    // String responseBody = httpMethod.getResponseBodyAsString();
    // System.out.println(responseBody);
    // assertEquals(200, result);
    // assertEquals(
    // "getShopInCity:q=Dave%20%26%20Buster's;city=Earth%2C%%20%TX%20D.C.;appversion=1.1"
    // ,
    // responseBody);
    // } catch (IOException ioe) {
    // ioe.printStackTrace();
    // fail(ioe.getMessage());
    // } finally {
    // httpMethod.releaseConnection();
    // }
    // } catch (URIException e) {
    // e.printStackTrace();
    // fail(e.getMessage());
    // }
    // }
    //
    // public void testLocationDecodedMethod() {
    // try {
    // GetMethod httpMethod = new GetMethod();
    // httpMethod.setURI(new URI(BASE_URI_ENCODE
    // +
    // "/loc/Earth%2C%20TX/;appversion=1.1%2B?q=Dave%20%26%20Buster's"
    // ,
    // true));
    // httpclient = new HttpClient();
    //
    // try {
    // int result = httpclient.executeMethod(httpMethod);
    // System.out.println("Response status code: " + result);
    // System.out.println("Response body: ");
    // String responseBody = httpMethod.getResponseBodyAsString();
    // System.out.println(responseBody);
    // assertEquals(200, result);
    // assertEquals(
    // "getShopInLocation:q=Dave%20%26%20Buster's;location=Earth%2C%20TX;appversion=1.1%2B"
    // ,
    // responseBody);
    // } catch (IOException ioe) {
    // ioe.printStackTrace();
    // fail(ioe.getMessage());
    // } finally {
    // httpMethod.releaseConnection();
    // }
    // } catch (URIException e) {
    // e.printStackTrace();
    // fail(e.getMessage());
    // }
    // }
    //
    // public void testPathParamEncoded() {
    // try {
    // GetMethod httpMethod = new GetMethod();
    // httpMethod.setURI(new URI(BASE_URI_ENCODE
    // + "/country/United%20States/;appversion=1.1%2B", true));
    // httpclient = new HttpClient();
    //
    // try {
    // int result = httpclient.executeMethod(httpMethod);
    // System.out.println("Response status code: " + result);
    // System.out.println("Response body: ");
    // String responseBody = httpMethod.getResponseBodyAsString();
    // System.out.println(responseBody);
    // assertEquals(200, result);
    // assertEquals("getShopInCountry:location=United%20States;appversion=1.1%2B"
    // ,
    // responseBody);
    // } catch (IOException ioe) {
    // ioe.printStackTrace();
    // fail(ioe.getMessage());
    // } finally {
    // httpMethod.releaseConnection();
    // }
    // } catch (URIException e) {
    // e.printStackTrace();
    // fail(e.getMessage());
    // }
    // }
    //
    // public void testDecodedMethod() {
    // try {
    // GetMethod httpMethod = new GetMethod();
    // httpMethod
    // .setURI(new URI(BASE_URI_DECODE
    // +
    // "/city/Washington D.C./;appversion=1 1?q=Austin's City Pizza"
    // ,
    // false));
    // httpclient = new HttpClient();
    //
    // try {
    // int result = httpclient.executeMethod(httpMethod);
    // System.out.println("Response status code: " + result);
    // System.out.println("Response body: ");
    // String responseBody = httpMethod.getResponseBodyAsString();
    // System.out.println(responseBody);
    // assertEquals(200, result);
    // assertEquals("getRow:" + "offset=" + "0" + ";version=" + "1.0" +
    // ";limit=" + "100"
    // + ";sort=" + "normal", responseBody);
    // } catch (IOException ioe) {
    // ioe.printStackTrace();
    // fail(ioe.getMessage());
    // } finally {
    // httpMethod.releaseConnection();
    // }
    // } catch (URIException e) {
    // e.printStackTrace();
    // fail(e.getMessage());
    // }
    // }

    public void testSingleDecodedQueryParam() {
        try {
            GetMethod httpMethod = new GetMethod();
            // httpMethod.setURI(new URI(BASE_URI_DECODE
            // +
            // "/city;appversion=1.1?location=! * ' ( ) ; : @ & = + $ , / ? % # [ ]"
            // , false));

            httpMethod
                .setURI(new URI(
                                BASE_URI_DECODE + "/city;appversion=1.1?location=%21%20%2A%20%27%20%28%20%29%20%3B%20%3A%20%40%20%26%20%3D%20%2B%20%24%20%2C%20%2F%20%3F%20%25%20%23%20%5B%20%5D",
                                true));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getShopInCityDecoded:location=! * ' ( ) ; : @ & = + $ , / ? % # [ ];appversion=1.1",
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

    public void testSingleEncodedQueryParam() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod
                .setURI(new URI(
                                BASE_URI_ENCODE + "/city;appversion=1.1%2B?location=Austin%2B%20Texas",
                                true));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getShopInCity:location=Austin%2B%20Texas;appversion=1.1%2B",
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

    public void testSingleEncodedQueryParamMethod() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod
                .setURI(new URI(
                                BASE_URI_ENCODE + "/method/city;appversion=1.1%2B?location=Austin%2B%20Texas",
                                true));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                System.out.println(responseBody);
                assertEquals("getShopInCityMethod:location=Austin%2B%20Texas;appversion=1.1%2B",
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

    public void testSingleDecodedPathParm() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod
                .setURI(new URI(
                                BASE_URI_DECODE + "/country/United%20States%20of%20America;appversion=1.1%2C2",
                                true));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getShopInCountryDecoded:location=United States of America;appversion=1.1,2",
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

    public void testSingleEncodedPathParam() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod
                .setURI(new URI(
                                BASE_URI_ENCODE + "/country/United%20States%20of%20America;appversion=1.1%2B",
                                true));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getShopInCountry:location=United%20States%20of%20America;appversion=1.1%2B",
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

    public void testSingleEncodedPathParamMethod() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod
                .setURI(new URI(
                                BASE_URI_ENCODE + "/method/country/United%20States%20of%20America;appversion=1.1%2B",
                                true));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getShopInCountryMethod:location=United%20States%20of%20America;appversion=1.1%2B",
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

    public void testSingleDecodedMatrixParam() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod
                .setURI(new URI(
                                BASE_URI_DECODE + "/street;location=Burnet%20Road;appversion=1.1%2B",
                                true));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getShopOnStreetDecoded:location=Burnet Road;appversion=1.1+",
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

    public void testSingleEncodedMatrixParam() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod
                .setURI(new URI(
                                BASE_URI_ENCODE + "/street;location=Burnet%20Road;appversion=1.1%2B",
                                true));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getShopOnStreet:location=Burnet%20Road;appversion=1.1%2B",
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

    public void testSingleEncodedMatrixParamMethod() {
        try {
            GetMethod httpMethod = new GetMethod();
            httpMethod
                .setURI(new URI(
                                BASE_URI_ENCODE + "/method/street;location=Burnet%2B%20Road;appversion=1.1%2B",
                                true));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getShopOnStreetMethod:location=Burnet%2B%20Road;appversion=1.1%2B",
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

    public void testSingleDecodedFormParam() throws Exception {
        try {
            PostMethod httpMethod = new PostMethod();
            httpMethod.setURI(new URI(BASE_URI_DECODE + "/region;appversion=", true));
            // httpMethod.setParameter("location", "The%20Southwest");
            httpMethod
                .setRequestEntity(new StringRequestEntity(
                                                          "location=%21%20%2A%20%27%20%28%20%29%20%3B%20%3A%20%40%20%26%20%3D%20%2B%20%24%20%2C%20%2F%20%3F%20%25%20%23%20%5B%20%5D",
                                                          "application/x-www-form-urlencoded",
                                                          "UTF-8"));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getShopInRegionDecoded:location=! * ' ( ) ; : @ & = + $ , / ? % # [ ];appversion=",
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

    public void testSingleEncodedFormParam() throws Exception {
        try {
            PostMethod httpMethod = new PostMethod();
            httpMethod.setURI(new URI(BASE_URI_ENCODE + "/region;appversion=1.1%2B", true));
            // httpMethod.setParameter("location", "The%20Southwest");
            httpMethod
                .setRequestEntity(new StringRequestEntity("location=The%20Southwest",
                                                          "application/x-www-form-urlencoded",
                                                          "UTF-8"));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getShopInRegion:location=The%20Southwest;appversion=1.1%2B",
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

    public void testSingleEncodedFormParamMethod() throws Exception {
        try {
            PostMethod httpMethod = new PostMethod();
            httpMethod.setURI(new URI(BASE_URI_ENCODE + "/method/region;appversion=1.1%2B", true));
            httpMethod
                .setRequestEntity(new StringRequestEntity("location=The%20Southwest",
                                                          "application/x-www-form-urlencoded",
                                                          "UTF-8"));
            httpclient = new HttpClient();

            try {
                int result = httpclient.executeMethod(httpMethod);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: ");
                String responseBody = httpMethod.getResponseBodyAsString();
                System.out.println(responseBody);
                assertEquals(200, result);
                assertEquals("getShopInRegionMethod:location=The%20Southwest;appversion=1.1%2B",
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
}
