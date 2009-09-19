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

import org.apache.commons.httpclient.HttpException;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkURIInfoDetailedMethodTest extends TestCase {

    private static String appBase = "/uriinfo";

    static {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            appBase = "";
        }
    }

    private static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/uriinfo";
    }

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Tests the {@link UriInfo#getAbsolutePath()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetAbsolutePath() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getAbsolutePath")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals(getBaseURI() + "/context/uriinfo/detailed", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getAbsolutePathBuilder()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetAbsoluteBuilder() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getAbsolutePathBuilder")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals(getBaseURI().replace(ServerEnvironmentInfo.getHostname(), "abcd") + "/context/uriinfo/detailed",
                     response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getBaseUri()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetBaseUri() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getBaseUri").get();
        assertEquals(200, response.getStatusCode());
        assertEquals(getBaseURI() + "/", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getBaseUriBuilder()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetBaseUriBuilder() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getBaseUriBuilder")
                .get();
        assertEquals(200, response.getStatusCode());

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
        assertEquals(baseUri, response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPath()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPath() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPath").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context/uriinfo/detailed", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPath(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathDecoded() throws HttpException, IOException {
        /*
         * the client automatically encodes URIs
         */
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathDecodedTrue")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context/uriinfo/detailed", response.getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathDecodedFalse")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context/uriinfo/detailed", response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/decoded/!@%23$%25%5E&*()?decoded=false")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context/uriinfo/detailed/decoded/!@%23$%25%5E&*()", response
            .getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/decoded/!%40%23%24%25%5E%26*()?decoded=true")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context/uriinfo/detailed/decoded/!@#$%^&*()", response
            .getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getMatchedResources()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedResourcesSimple() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getMatchedResources")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals(UriInfoDetailedMethods.class.getName() + ":", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getMatchedResources()} in a sub resource method.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedResourcesSubresource() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed/matchedresources").get();
        assertEquals(200, response.getStatusCode());
        assertEquals(UriInfoDetailedMethods.class.getName() + ":"
            + "-"
            + MatchedResourcesSubResource.class.getName()
            + ":"
            + UriInfoDetailedMethods.class.getName()
            + ":", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getMatchedURIs()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedURIs() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getMatchedURIs")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context/uriinfo/detailed" + ":", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getMatchedURIs()} in a sub-resource method.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedURIsSubresource() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed/matcheduris").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context/uriinfo/detailed/matcheduris" + ":"
            + "context/uriinfo/detailed"
            + ":"
            + "-"
            + "context/uriinfo/detailed/matcheduris"
            + ":"
            + "context/uriinfo/detailed"
            + ":", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getMatchedURIs(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedURIsDecodeTrue() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getMatchedURIsDecodedTrue")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context/uriinfo/detailed" + ":", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getMatchedURIs(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedURIsDecodeFalse() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getMatchedURIsDecodedFalse")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context/uriinfo/detailed" + ":", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getMatchedURIs(boolean)} in a sub-locator.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedURIsSublocatorDecodeTrue() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/matchedurisdecoded/!%40%23%24%25%5E%26*()?decoded=true")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context/uriinfo/detailed/matchedurisdecoded/!@#$%^&*()", response
            .getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getMatchedURIs(boolean)} in a sub-locator.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetMatchedURIsSublocatorDecodeFalse() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/matchedurisdecoded/!@%23$%25%5E&*()?decoded=false")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context/uriinfo/detailed/matchedurisdecoded/!@%23$%25%5E&*()", response
            .getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPathParameters()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersZero() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathParameters")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPathParameters()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersOne() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=/:", response.getEntity(String.class));

        response = client.resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsone").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=:", response.getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/foo").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=/foo:", response.getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/foo/bar").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=/foo/bar:", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPathParameters()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersMany() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsmany/foo/bar/xyz")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=foo:p2=b:p3=ar/xyz:", response.getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsmany/foo/bar")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=foo:p2=b:p3=ar:", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPathParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersZeroDecodedFalse() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathParametersDecodedFalse")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPathParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersOneDecodedFalse() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/?decoded=false")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=/:", response.getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsone?decoded=false")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=:", response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/!@%23$%25%5E&*()?decoded=false")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=/!@%23$%25%5E&*():", response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/!@%23$%25%5E&*()/!@%23$%25%5E&*()?decoded=false")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=/!@%23$%25%5E&*()/!@%23$%25%5E&*():", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPathParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersManyDecodedFalse() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsmany/foo/bar/xyz?decoded=false")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=foo:p2=b:p3=ar/xyz:", response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsmany/foo/bar?decoded=false")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=foo:p2=b:p3=ar:", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPathParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersZeroDecodedTrue() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathParametersDecodedTrue")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPathParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersOneDecodedTrue() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/?decoded=true")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=/:", response.getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsone?decoded=true")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=:", response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/!%40%23%24%25%5E%26*()?decoded=true")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=/!@#$%^&*():", response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsone/!%40%23%24%25%5E%26*()/!%40%23%24%25%5E%26*()?decoded=true")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=/!@#$%^&*()/!@#$%^&*():", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPathParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathParametersManyDecodedTrue() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsmany/!%40%23%24%25%5E%26*()/bar/xyz?decoded=true")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=!@#$%^&*():p2=b:p3=ar/xyz:", response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/pathparamsmany/!%40%23%24%25%5E%26*()/bar?decoded=true")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("p1=!@#$%^&*():p2=b:p3=ar:", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPathSegments()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathSegments() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathSegments")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context#:uriinfo#:detailed#:", response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context;matrixp1=value1;matrixp2=value2;foo=bar/uriinfo/detailed?reqInfo=getPathSegments")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context#foo=bar:matrixp1=value1:matrixp2=value2::uriinfo#:detailed#:",
                     response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context;matrixp1=!%40%23%24%25%5E%26*();matrixp2=value2;foo=bar/uriinfo/detailed?reqInfo=getPathSegments")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context#foo=bar:matrixp1=!@#$%^&*():matrixp2=value2::uriinfo#:detailed#:",
                     response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getPathSegments(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetPathSegmentsDecodedFalse() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getPathSegmentsDecodedFalse")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context#:uriinfo#:detailed#:", response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context;matrixp1=value1;matrixp2=value2;foo=bar/uriinfo/detailed?reqInfo=getPathSegmentsDecodedFalse")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context#foo=bar:matrixp1=value1:matrixp2=value2::uriinfo#:detailed#:",
                     response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context;matrixp1=!@%23$%25%5E&*();matrixp2=value2;foo=bar/uriinfo/detailed?reqInfo=getPathSegmentsDecodedFalse")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("context#foo=bar:matrixp1=!@%23$%25%5E&*():matrixp2=value2::uriinfo#:detailed#:",
                     response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getQueryParameters()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetQueryParametersZero() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed/queryparams").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getQueryParameters()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetQueryParametersOne() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getQueryParameters")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("reqInfo=getQueryParameters:", response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/queryparams?q1=value1&q1=value2")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("q1=value1:value2:", response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/queryparams?q1=value1&q1=value2")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("q1=value1:value2:", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getQueryParameters()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetQueryParametersMany() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/queryparams?q1=value1&q2=value2")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("q1=value1:q2=value2:", response.getEntity(String.class));

        response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/queryparams?q1=!%40%23%24%25%5E%26*()&q2=value2")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("q1=!@#$%^&*():q2=value2:", response.getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getQueryParameters(boolean)}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetQueryParametersManyDecodedFalse() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed/queryparams?q1=!%40%23%24%25%5E%26*()&q2=value2&decoded=false")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("decoded=false:q1=!%40%23%24%25%5E%26*():q2=value2:", response
            .getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getRequestUri()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetRequestUri() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getRequestUri").get();
        assertEquals(200, response.getStatusCode());
        assertEquals(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getRequestUri", response
            .getEntity(String.class));
    }

    /**
     * Tests the {@link UriInfo#getRequestUriBuilder()}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testURIInfoGetRequestUriBuilder() throws HttpException, IOException {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/context/uriinfo/detailed?reqInfo=getRequestUriBuilder")
                .get();
        assertEquals(200, response.getStatusCode());
        String expected =
            (getBaseURI() + "/context/uriinfo/detailed?reqInfo=getRequestUriBuilder")
                .replace(ServerEnvironmentInfo.getHostname(), "abcd");
        assertEquals(expected, response.getEntity(String.class));
    }
}
