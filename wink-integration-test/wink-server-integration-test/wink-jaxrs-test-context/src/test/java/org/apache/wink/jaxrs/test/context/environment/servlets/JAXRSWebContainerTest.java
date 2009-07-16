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

package org.apache.wink.jaxrs.test.context.environment.servlets;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Simple tests for web container spec compliance.
 */
public class JAXRSWebContainerTest extends TestCase {

    private static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/webcontainer"
            + "/environment/webcontainer/context/";
    }

    /**
     * Tests that a HTTPServletRequest can be injected.
     * 
     * @throws Exception
     */
    public void testHTTPServletRequestInjection() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI());
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertTrue(getBaseURI().endsWith(getMethod.getResponseBodyAsString()));
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that an injected HTTPServletResponse can take over the response
     * instead of further processing by the runtime engine.
     * 
     * @throws Exception
     */
    public void testHTTPServletResponseInjection() throws Exception {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI());
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("responseheadervalue", postMethod.getResponseHeader("responseheadername")
                .getValue());
            assertEquals("Hello World -- I was committted", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a ServletContext can be injected.
     * 
     * @throws Exception
     */
    public void testServletContextInjection() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/servletcontext");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            System.out.println(getMethod.getResponseBodyAsString());
            assertTrue(getMethod.getResponseBodyAsString().contains("testing 1-2-3"));
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a ServletConfig can be injected.
     * 
     * @throws Exception
     */
    public void testServletConfigInjection() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/servletconfig");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            System.out.println(getMethod.getResponseBodyAsString());
            assertEquals("WebContainerTests", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }
}
