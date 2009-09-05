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

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class FormParamTest extends TestCase {

    public String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/params/form";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/params/params/form";
    }

    public void testOnlyEntityFormParam() throws Exception {
        HttpClient httpclient = new HttpClient();

        PostMethod httpMethod = new PostMethod(getBaseURI() + "/withOnlyEntity");
        try {
            StringRequestEntity s =
                new StringRequestEntity("firstkey=somevalue&someothervalue=somethingelse",
                                        "application/x-www-form-urlencoded", null);
            httpMethod.setRequestEntity(s);
            httpclient.executeMethod(httpMethod);
            assertEquals(200, httpMethod.getStatusCode());
            String resp = httpMethod.getResponseBodyAsString();
            System.out.println(resp);
            assertTrue(resp, resp.contains("someothervalue=somethingelse"));
            assertTrue(resp, resp.contains("firstkey=somevalue"));
        } finally {
            httpMethod.releaseConnection();
        }
    }

    // TODO Fails due to WINK-35 which is not going to be resolved for the time
    // being
    // public void testEntityFormParamWithOneFormParam() throws Exception {
    // HttpClient httpclient = new HttpClient();
    //
    // PostMethod httpMethod = new PostMethod(getBaseURI() +
    // "/withOneKeyAndEntity");
    // try {
    // StringRequestEntity s =
    // new
    // StringRequestEntity("firstkey=somevalue&someothervalue=somethingelse",
    // "application/x-www-form-urlencoded", null);
    // httpMethod.setRequestEntity(s);
    // httpclient.executeMethod(httpMethod);
    // assertEquals(200, httpMethod.getStatusCode());
    // String resp = httpMethod.getResponseBodyAsString();
    // System.out.println(resp);
    // assertTrue(resp, resp.startsWith("firstkey=somevalue"));
    // assertTrue(resp, resp.contains("someothervalue=somethingelse"));
    // assertTrue(resp, resp.contains("firstkey=somevalue"));
    // } finally {
    // httpMethod.releaseConnection();
    // }
    // }

    /**
     * In a weird instance, client posts a form encoded data but the resource is
     * expecting something else (say a String) as its entity. The engine should
     * not mangle the InputStream with ServletRequest.getParameter until
     * absolutely required.
     * 
     * @throws Exception
     */
    public void testPostFormEntityButResourceDoesNotExpect() throws Exception {
        HttpClient httpclient = new HttpClient();

        PostMethod httpMethod = new PostMethod(getBaseURI() + "/withStringEntity");
        try {
            StringRequestEntity s =
                new StringRequestEntity("firstkey=somevalue&someothervalue=somethingelse",
                                        "application/x-www-form-urlencoded", null);
            httpMethod.setRequestEntity(s);
            httpclient.executeMethod(httpMethod);
            assertEquals(200, httpMethod.getStatusCode());
            String resp = httpMethod.getResponseBodyAsString();
            System.out.println(resp);
            assertEquals(resp, "str:firstkey=somevalue&someothervalue=somethingelse");
        } finally {
            httpMethod.releaseConnection();
        }
    }
}
