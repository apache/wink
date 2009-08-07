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

package org.apache.wink.itest.uriinfo;

import java.io.IOException;

import javax.ws.rs.core.UriInfo;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.itest.uriinfo.MatchedResourcesSubResource;
import org.apache.wink.itest.uriinfo.UriInfoDetailedMethods;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests that various {@link UriInfo} methods work as expected.
 */
public class URIInfoDetailedMethodTest extends TestCase {

    public String appBase = "/uriinfo";

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + appBase;
    }

    /**
     * Tests the {@link UriInfo#getAbsolutePath()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetAbsolutePath() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getAbsolutePath");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals(getBaseURI() + "/context/uriinfo/detailed", getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getAbsolutePathBuilder()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetAbsoluteBuilder() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getAbsolutePathBuilder");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals(getBaseURI().replace(ServerEnvironmentInfo.getHostname(), "abcd") + "/context/uriinfo/detailed",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getBaseUri()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetBaseUri() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getBaseUri");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals(getBaseURI() + "/", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getBaseUriBuilder()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetBaseUriBuilder() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getBaseUriBuilder");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String contextRoot = ServerEnvironmentInfo.getContextRoot();
            if (!"".equals(contextRoot)) {
                contextRoot = "/" + contextRoot;
            }
            String baseUri =
                "http://" + "abcd"
                    + ":"
                    + ServerEnvironmentInfo.getPort()
                    + contextRoot
                    + appBase
                    + "/";
            assertEquals(baseUri, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPath()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPath() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPath");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context/uriinfo/detailed", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPath(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathDecoded() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathDecodedTrue");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context/uriinfo/detailed", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathDecodedFalse");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context/uriinfo/detailed", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/decoded/!%40%23%24%25%5E%26*()?decoded=false");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context/uriinfo/detailed/decoded/!%40%23%24%25%5E%26*()", getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/decoded/!%40%23%24%25%5E%26*()?decoded=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context/uriinfo/detailed/decoded/!@#$%^&*()", getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

    }

    /**
     * Tests the {@link UriInfo#getMatchedResources()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedResourcesSimple() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getMatchedResources");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals(UriInfoDetailedMethods.class.getName() + ":", getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getMatchedResources()} in a sub resource method.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedResourcesSubresource() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed/matchedresources");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals(UriInfoDetailedMethods.class.getName() + ":"
                + "-"
                + MatchedResourcesSubResource.class.getName()
                + ":"
                + UriInfoDetailedMethods.class.getName()
                + ":", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getMatchedURIs()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedURIs() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getMatchedURIs");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context/uriinfo/detailed" + ":", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getMatchedURIs()} in a sub-resource method.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedURIsSubresource() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/uriinfo/detailed/matcheduris");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context/uriinfo/detailed/matcheduris" + ":"
                + "context/uriinfo/detailed"
                + ":"
                + "-"
                + "context/uriinfo/detailed/matcheduris"
                + ":"
                + "context/uriinfo/detailed"
                + ":", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getMatchedURIs(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedURIsDecodeTrue() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed?reqInfo=getMatchedURIsDecodedTrue");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context/uriinfo/detailed" + ":", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getMatchedURIs(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedURIsDecodeFalse() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed?reqInfo=getMatchedURIsDecodedFalse");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context/uriinfo/detailed" + ":", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getMatchedURIs(boolean)} in a sub-locator.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedURIsSublocatorDecodeTrue() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/matchedurisdecoded/!%40%23%24%25%5E%26*()?decoded=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context/uriinfo/detailed/matchedurisdecoded/!@#$%^&*()", getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getMatchedURIs(boolean)} in a sub-locator.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedURIsSublocatorDecodeFalse() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/matchedurisdecoded/!%40%23%24%25%5E%26*()?decoded=false");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context/uriinfo/detailed/matchedurisdecoded/!%40%23%24%25%5E%26*()",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPathParameters()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersZero() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathParameters");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPathParameters()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersOne() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=/:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/uriinfo/detailed/pathparamsone");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/foo");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=/foo:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/foo/bar");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=/foo/bar:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPathParameters()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersMany() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed/pathparamsmany/foo/bar/xyz");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=foo:p2=b:p3=ar/xyz:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed/pathparamsmany/foo/bar");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=foo:p2=b:p3=ar:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPathParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersZeroDecodedFalse() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathParametersDecodedFalse");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPathParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersOneDecodedFalse() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/?decoded=false");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=/:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed/pathparamsone?decoded=false");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/pathparamsone/!%40%23%24%25%5E%26*()?decoded=false");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=/!%40%23%24%25%5E%26*():", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/pathparamsone/!%40%23%24%25%5E%26*()/!%40%23%24%25%5E%26*()?decoded=false");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=/!%40%23%24%25%5E%26*()/!%40%23%24%25%5E%26*():", getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPathParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersManyDecodedFalse() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/pathparamsmany/foo/bar/xyz?decoded=false");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=foo:p2=b:p3=ar/xyz:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/pathparamsmany/foo/bar?decoded=false");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=foo:p2=b:p3=ar:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPathParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersZeroDecodedTrue() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathParametersDecodedTrue");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPathParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersOneDecodedTrue() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/?decoded=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=/:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed/pathparamsone?decoded=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/pathparamsone/!%40%23%24%25%5E%26*()?decoded=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=/!@#$%^&*():", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/pathparamsone/!%40%23%24%25%5E%26*()/!%40%23%24%25%5E%26*()?decoded=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=/!@#$%^&*()/!@#$%^&*():", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPathParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersManyDecodedTrue() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/pathparamsmany/!%40%23%24%25%5E%26*()/bar/xyz?decoded=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=!@#$%^&*():p2=b:p3=ar/xyz:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/pathparamsmany/!%40%23%24%25%5E%26*()/bar?decoded=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("p1=!@#$%^&*():p2=b:p3=ar:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPathSegments()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathSegments() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathSegments");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context#:uriinfo#:detailed#:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context;matrixp1=value1;matrixp2=value2;foo=bar/uriinfo/detailed?reqInfo=getPathSegments");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context#foo=bar:matrixp1=value1:matrixp2=value2::uriinfo#:detailed#:",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context;matrixp1=!%40%23%24%25%5E%26*();matrixp2=value2;foo=bar/uriinfo/detailed?reqInfo=getPathSegments");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context#foo=bar:matrixp1=!@#$%^&*():matrixp2=value2::uriinfo#:detailed#:",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getPathSegments(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathSegmentsDecodedFalse() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathSegmentsDecodedFalse");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context#:uriinfo#:detailed#:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context;matrixp1=value1;matrixp2=value2;foo=bar/uriinfo/detailed?reqInfo=getPathSegmentsDecodedFalse");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context#foo=bar:matrixp1=value1:matrixp2=value2::uriinfo#:detailed#:",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context;matrixp1=!%40%23%24%25%5E%26*();matrixp2=value2;foo=bar/uriinfo/detailed?reqInfo=getPathSegmentsDecodedFalse");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("context#foo=bar:matrixp1=!%40%23%24%25%5E%26*():matrixp2=value2::uriinfo#:detailed#:",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getQueryParameters()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetQueryParametersZero() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/uriinfo/detailed/queryparams");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getQueryParameters()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetQueryParametersOne() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getQueryParameters");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("reqInfo=getQueryParameters:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/queryparams?q1=value1&q1=value2");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("q1=value1:value2:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/queryparams?q1=value1&q1=value2");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("q1=value1:value2:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getQueryParameters()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetQueryParametersMany() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/queryparams?q1=value1&q2=value2");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("q1=value1:q2=value2:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/queryparams?q1=!%40%23%24%25%5E%26*()&q2=value2");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("q1=!@#$%^&*():q2=value2:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getQueryParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetQueryParametersManyDecodedFalse() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/uriinfo/detailed/queryparams?q1=!%40%23%24%25%5E%26*()&q2=value2&decoded=false");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("decoded=false:q1=!%40%23%24%25%5E%26*():q2=value2:", getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getRequestUri()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetRequestUri() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getRequestUri");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getRequestUri",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests the {@link UriInfo#getRequestUriBuilder()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetRequestUriBuilder() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getRequestUriBuilder");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String expected =
                (getBaseURI() + "/context/uriinfo/detailed?reqInfo=getRequestUriBuilder")
                    .replace(ServerEnvironmentInfo.getHostname(), "abcd");
            assertEquals(expected, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }
}
