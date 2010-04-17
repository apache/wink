/*******************************************************************************
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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.wink.server.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test parameter conversion.
 */
public class EmptyPathParamTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {PathResource.class, PathResource2.class, PathResource3.class,
            MultipleSegmentsInRoot.class, EncodedPathSegments.class, InRootPathSegments.class};
    }

    @Path("all/in/root")
    public static class MultipleSegmentsInRoot {
        @GET
        @Path("/first/{firstParams:.*}/second/{secondParams:.*}")
        @Produces(MediaType.TEXT_PLAIN)
        public String findTestRuns(@PathParam("firstParams") final PathSegment firstParams,
                                   @PathParam("secondParams") final PathSegment secondParams) {

            assertEquals("", firstParams.getPath());
            assertEquals("bob", firstParams.getMatrixParameters().get("name").get(0));
            assertEquals("", secondParams.getPath());
            assertEquals("blue", secondParams.getMatrixParameters().get("eyes").get(0));
            return "hi2";

        }
    }

    @Path("%21/more")
    public static class EncodedPathSegments {
        @GET
        @Path("/first/{firstParams:.*}/second/{secondParams:.*}")
        @Produces(MediaType.TEXT_PLAIN)
        public String findTestRuns(@PathParam("firstParams") final PathSegment firstParams,
                                   @PathParam("secondParams") final PathSegment secondParams) {

            assertEquals("", firstParams.getPath());
            assertEquals("bob", firstParams.getMatrixParameters().get("name").get(0));
            assertEquals("", secondParams.getPath());
            assertEquals("blue", secondParams.getMatrixParameters().get("eyes").get(0));
            return "hi2";

        }
    }

    @Path("inroot/first/{firstParams:.*}/second/{secondParams:.*}")
    public static class InRootPathSegments {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String findTestRuns(@PathParam("firstParams") final PathSegment firstParams,
                                   @PathParam("secondParams") final PathSegment secondParams) {

            assertEquals("", firstParams.getPath());
            assertEquals("bob", firstParams.getMatrixParameters().get("name").get(0));
            assertEquals("", secondParams.getPath());
            assertEquals("blue", secondParams.getMatrixParameters().get("eyes").get(0));
            return "hi2";

        }
    }

    @Path("/p")
    public static class PathResource {

        @GET
        @Path("/first{firstParams:.*}/second{secondParams:.*}")
        @Produces(MediaType.TEXT_PLAIN)
        public String findTestRuns(@PathParam("firstParams") final PathSegment firstParams,
                                   @PathParam("secondParams") final PathSegment secondParams) {

            assertEquals("first", firstParams.getPath());
            assertEquals("bob", firstParams.getMatrixParameters().get("name").get(0));
            assertEquals("second", secondParams.getPath());
            assertEquals("blue", secondParams.getMatrixParameters().get("eyes").get(0));
            return "hi";

        }

    }

    @Path("/p2")
    public static class PathResource2 {

        @GET
        @Path("/first/{firstParams:.*}/second/{secondParams:.*}")
        @Produces(MediaType.TEXT_PLAIN)
        public String findTestRuns(@PathParam("firstParams") final PathSegment firstParams,
                                   @PathParam("secondParams") final PathSegment secondParams) {

            assertEquals("", firstParams.getPath());
            assertEquals("bob", firstParams.getMatrixParameters().get("name").get(0));
            assertEquals("", secondParams.getPath());
            assertEquals("blue", secondParams.getMatrixParameters().get("eyes").get(0));
            return "hi2";

        }

    }

    @Path("")
    // "" is the essence of this test
    public static class PathResource3 {
        @GET
        @Path("/first{firstParams:.*}/second{secondParams:.*}")
        @Produces(MediaType.TEXT_PLAIN)
        public String findTestRuns(@PathParam("firstParams") final PathSegment firstParams,
                                   @PathParam("secondParams") final PathSegment secondParams) {

            assertEquals("first", firstParams.getPath());
            assertEquals("bob", firstParams.getMatrixParameters().get("name").get(0));
            assertEquals("second", secondParams.getPath());
            assertEquals("blue", secondParams.getMatrixParameters().get("eyes").get(0));
            return "hi3";

        }
    }

    public void testEmptyPathParam() throws Exception {
        MockHttpServletRequest getRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/p/first;name=bob/second;eyes=blue",
                                                        MediaType.TEXT_PLAIN);
        MockHttpServletResponse getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi", getResponse.getContentAsString());
    }

    public void testEmptyPathParam2() throws Exception {
        MockHttpServletRequest getRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/p2/first/;name=bob/second/;eyes=blue",
                                                        MediaType.TEXT_PLAIN);
        MockHttpServletResponse getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());

        getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/p2/first/../first/;name=bob/second/;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());
    }

    public void testEmptyPathParam3() throws Exception {
        MockHttpServletRequest getRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/first;name=bob/second;eyes=blue",
                                                        MediaType.TEXT_PLAIN);
        MockHttpServletResponse getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi3", getResponse.getContentAsString());
    }

    public void testMultipleSegmentsInRoot() throws Exception {
        MockHttpServletRequest getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/all/in/root/first/;name=bob/second/;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        MockHttpServletResponse getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());

        getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/all///in////root///first///;name=bob///second///;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());

        getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/all/../all/in/root/first/;name=bob/second/;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());

        getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/all/../all/in/root/first/../first/;name=bob/second/;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());

        getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/all/../all/in/root/first/../first/;name=bob/second/;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());

        getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/all/../all/in/root/first%21/../first/;name=bob/second/;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());
    }

    public void testEncodedPathSegments() throws Exception {
        MockHttpServletRequest getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/%21/more/first/;name=bob/second/;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        MockHttpServletResponse getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());
    }

    public void testInRootPathSegment() throws Exception {
        MockHttpServletRequest getRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "inroot/first/;name=bob/second/;eyes=blue",
                                                        MediaType.TEXT_PLAIN);
        MockHttpServletResponse getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());

        getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/inroot///first///;name=bob///second///;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());

        getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/inroot/../inroot/first/;name=bob/second/;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());

        getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/inroot///..////inroot/first/../first/;name=bob/second/;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());

        getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/abcdefg/../inroot/first/../first/;name=bob/second/;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());

        getRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/inroot/../inroot/first%21/../first/;name=bob/second/;eyes=blue",
                                      MediaType.TEXT_PLAIN);
        getResponse = invoke(getRequest);
        assertEquals(200, getResponse.getStatus());
        assertEquals("hi2", getResponse.getContentAsString());
    }
}
