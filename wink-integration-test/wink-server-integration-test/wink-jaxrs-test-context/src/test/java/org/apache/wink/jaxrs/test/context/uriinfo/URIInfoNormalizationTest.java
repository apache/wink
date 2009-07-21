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

package org.apache.wink.jaxrs.test.context.uriinfo;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class URIInfoNormalizationTest extends TestCase {

    private final String appRoot = "/uriinfo";

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + appRoot;
    }

    /**
     * Tests that a normal "good" path is returned.
     * 
     * @throws Exception
     */
    public void testPathNormal() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/uriinfo?info=path");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("uriinfo", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a path which removes the initial path to the resource class
     * but adds it back in is okay.
     * 
     * @throws Exception
     */
    public void testRemoveResourcePathThenAddItBack() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/uriinfo/../uriinfo" + "?info=path");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("uriinfo", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/uriinfo/." + "?info=path");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("uriinfo/", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/uriinfo/./" + "?info=path");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("uriinfo/", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/uriinfo/./.././uriinfo/./" + "?info=path");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("uriinfo/", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/uriinfo/../uriinfo/" + "?info=path");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("uriinfo/", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/uriinfo/sub/../uriinfo/../sub" + "?info=path");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("uriinfo/sub", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/uriinfo/sub/../uriinfo/../sub/" + "?info=path");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("uriinfo/sub/", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests adding some extra paths to resource paths and then removing them.
     * 
     * @throws Exception
     */
    public void testAddPathThenRemoveIt() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/uriinfo/something/../" + "?info=path");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("uriinfo/", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/uriinfo/something/.." + "?info=path");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("uriinfo/", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(getBaseURI() + "/uriinfo/sub/../uriinfo/../sub/something/../"
                + "?info=path");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("uriinfo/sub/", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(getBaseURI() + "/uriinfo/sub/../uriinfo/../sub/something/.."
                + "?info=path");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("uriinfo/sub/", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the capitalization is correct.
     * 
     * @throws Exception
     */
    public void testCapitalization() throws Exception {
        HttpClient client = new HttpClient();
        String contextRoot = ServerEnvironmentInfo.getContextRoot();
        if (!"".equals(contextRoot)) {
            contextRoot = "/" + contextRoot;
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/uriinfo/something/../" + "?info=host");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals(ServerEnvironmentInfo.getHostname().toLowerCase(), getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * should be the same as first test above
         */
        getMethod =
            new GetMethod("http://" + ServerEnvironmentInfo.getHostname()
                + ":"
                + ServerEnvironmentInfo.getPort()
                + contextRoot
                + appRoot
                + "/uriinfo/something/../"
                + "?info=host");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals(ServerEnvironmentInfo.getHostname().toLowerCase(), getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * uppercased
         */
        getMethod =
            new GetMethod("http://" + ServerEnvironmentInfo.getHostname().toUpperCase()
                + ":"
                + ServerEnvironmentInfo.getPort()
                + contextRoot
                + appRoot
                + "/uriinfo/something/../"
                + "?info=host");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals(ServerEnvironmentInfo.getHostname().toLowerCase(), getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * uppercased
         */
        getMethod =
            new GetMethod("HTTP://" + ServerEnvironmentInfo.getHostname().toUpperCase()
                + ":"
                + ServerEnvironmentInfo.getPort()
                + contextRoot
                + appRoot
                + "/uriinfo/something/../"
                + "?info=protocol");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("http", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the capitalization is correct.
     * 
     * @throws Exception
     */
    public void testPercentEncoding() throws Exception {
        HttpClient client = new HttpClient();

        /*
         * regular query
         */
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/uriinfo/something/../" + "?info=query&hello1=%3F");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("info=query&hello1=?", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * raw query
         */
        getMethod =
            new GetMethod(getBaseURI() + "/uriinfo/something/../" + "?info=rawquery&hello1=%3F");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("info=rawquery&hello1=%3F", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(getBaseURI() + "/uriinfo/something/../" + "?info=query&hello%31=%3F");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("info=query&hello1=?", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        /*
         * %75 should eventually be normalized to /uriinfo/something %31 should
         * be normalized to 1
         */
        getMethod =
            new GetMethod(getBaseURI() + "/%75riinfo/something/../" + "?info=rawquery&hello%31=%3F");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            /*
             * in this case, the %31 should remain encoded
             */
            assertEquals("info=rawquery&hello%31=%3F", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }
}
