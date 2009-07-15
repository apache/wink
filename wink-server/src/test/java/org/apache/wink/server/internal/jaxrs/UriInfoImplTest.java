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
package org.apache.wink.server.internal.jaxrs;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class UriInfoImplTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {FooResource.class, TestResource.class};
    }

    @Path("/te st/{id}")
    public static class TestResource {
        @GET
        @Produces("text/plain")
        public void getFoo(@Context UriInfo uriInfo) {
            assertNotNull(uriInfo.getAbsolutePath());
            assertEquals("http://localhost:80/te%20st/5", uriInfo.getAbsolutePath().toString());
            assertNotNull(uriInfo.getBaseUri());
            assertEquals("http://localhost:80/", uriInfo.getBaseUri().toString());
            assertNotNull(uriInfo.getPath());
            assertEquals("te st/5", uriInfo.getPath().toString());
            assertEquals("te%20st/5", uriInfo.getPath(false).toString());
            MultivaluedMap<String, String> pathParameters = uriInfo.getPathParameters();
            assertNotNull(pathParameters);
            assertEquals(1, pathParameters.size());
            List<String> paramValue = pathParameters.get("id");
            assertNotNull(paramValue);
            assertEquals(1, paramValue.size());
            assertEquals("5", paramValue.get(0));

            List<PathSegment> pathSegmentsDecoded = uriInfo.getPathSegments();
            assertNotNull(pathSegmentsDecoded);
            assertEquals(2, pathSegmentsDecoded.size());
            assertEquals("te st", pathSegmentsDecoded.get(0).getPath());
            assertEquals("5", pathSegmentsDecoded.get(1).getPath());
            List<PathSegment> pathSegmentsEncoded = uriInfo.getPathSegments(false);
            assertEquals("te%20st", pathSegmentsEncoded.get(0).getPath());
            assertEquals(2, pathSegmentsEncoded.size());
            assertEquals("5", pathSegmentsEncoded.get(1).getPath());

            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            assertNotNull(queryParameters);
            assertEquals(1, queryParameters.size());
            List<String> queryParam = queryParameters.get("abc");
            assertNotNull(queryParam);
            assertEquals(1, queryParam.size());
            assertEquals("6", queryParam.get(0));

            assertEquals("http://localhost:80/te%20st/5?abc=6", uriInfo.getRequestUri().toString());

            return;
        }
    }

    @Path("/foo")
    public static class FooResource {
        @GET
        @Produces("text/plain")
        public String getFoo(@Context UriInfo uriInfo) {

            // test matched URIs
            assertNotNull(uriInfo);
            assertNotNull(uriInfo.getMatchedURIs());
            assertEquals(1, uriInfo.getMatchedURIs().size());
            assertEquals("foo", uriInfo.getMatchedURIs().get(0));

            // test matched Resources
            assertNotNull(uriInfo.getMatchedResources());
            assertEquals(1, uriInfo.getMatchedResources().size());
            Object matchedResource = uriInfo.getMatchedResources().get(0);
            assertNotNull(matchedResource);
            assertTrue(matchedResource instanceof FooResource);
            return "foo";
        }

        @Path("bar")
        public BarResource getBarResource(@Context UriInfo uriInfo) {
            // test matched URIs
            assertNotNull(uriInfo);
            assertNotNull(uriInfo.getMatchedURIs());
            assertEquals(2, uriInfo.getMatchedURIs().size());
            String firstUri = uriInfo.getMatchedURIs().get(0);
            assertEquals("foo/bar", firstUri);
            String secondUri = uriInfo.getMatchedURIs().get(1);
            assertEquals("foo", secondUri);

            // test matched Resources
            assertNotNull(uriInfo.getMatchedResources());
            assertEquals(1, uriInfo.getMatchedResources().size());
            Object matchedResource = uriInfo.getMatchedResources().get(0);
            assertNotNull(matchedResource);
            assertTrue(matchedResource instanceof FooResource);
            return new BarResource();
        }

        @GET
        @Path("bar1")
        public String getBar1Resource(@Context UriInfo uriInfo) {
            // test matched URIs
            assertNotNull(uriInfo);
            assertNotNull(uriInfo.getMatchedURIs());
            assertEquals(2, uriInfo.getMatchedURIs().size());
            String firstUri = uriInfo.getMatchedURIs().get(0);
            assertEquals("foo/bar1", firstUri);
            String secondUri = uriInfo.getMatchedURIs().get(1);
            assertEquals("foo", secondUri);

            // test matched Resources
            assertNotNull(uriInfo.getMatchedResources());
            assertEquals(1, uriInfo.getMatchedResources().size());
            Object matchedResource = uriInfo.getMatchedResources().get(0);
            assertNotNull(matchedResource);
            assertTrue(matchedResource instanceof FooResource);
            return "Bar Resource";
        }

    }

    public static class BarResource {
        @GET
        @Produces("text/plain")
        public String getBar(@Context UriInfo uriInfo) {

            // test matched URIs
            assertNotNull(uriInfo);
            assertNotNull(uriInfo.getMatchedURIs());
            assertEquals(2, uriInfo.getMatchedURIs().size());
            String firstUri = uriInfo.getMatchedURIs().get(0);
            assertEquals("foo/bar", firstUri);
            String secondUri = uriInfo.getMatchedURIs().get(1);
            assertEquals("foo", secondUri);

            // test matched Resources
            assertNotNull(uriInfo.getMatchedResources());
            assertEquals(2, uriInfo.getMatchedResources().size());
            Object matchedResource = uriInfo.getMatchedResources().get(0);
            assertTrue(matchedResource instanceof BarResource);

            matchedResource = uriInfo.getMatchedResources().get(1);
            assertTrue(matchedResource instanceof FooResource);

            return "Bar Resurse";
        }

        @Path("level3")
        @Produces("text/plain")
        public BarResourceLevel3 getBarLevel3(@Context UriInfo uriInfo) {

            // test matched URIs
            assertNotNull(uriInfo);
            assertNotNull(uriInfo.getMatchedURIs());
            assertEquals(3, uriInfo.getMatchedURIs().size());
            String firstUri = uriInfo.getMatchedURIs().get(0);
            assertEquals("foo/bar/level3", firstUri);
            String secondUri = uriInfo.getMatchedURIs().get(1);
            assertEquals("foo/bar", secondUri);
            String thirdUri = uriInfo.getMatchedURIs().get(2);
            assertEquals("foo", thirdUri);

            // test matched Resources
            assertNotNull(uriInfo.getMatchedResources());
            assertEquals(2, uriInfo.getMatchedResources().size());
            Object matchedResource = uriInfo.getMatchedResources().get(0);
            assertTrue(matchedResource instanceof BarResource);

            matchedResource = uriInfo.getMatchedResources().get(1);
            assertTrue(matchedResource instanceof FooResource);

            return new BarResourceLevel3();
        }
    }

    public static class BarResourceLevel3 {
        @GET
        @Produces("text/plain")
        public String getBar(@Context UriInfo uriInfo) {

            // test matched URIs
            assertNotNull(uriInfo);
            assertNotNull(uriInfo.getMatchedURIs());
            assertEquals(3, uriInfo.getMatchedURIs().size());
            String firstUri = uriInfo.getMatchedURIs().get(0);
            assertEquals("foo/bar/level3", firstUri);
            String secondUri = uriInfo.getMatchedURIs().get(1);
            assertEquals("foo/bar", secondUri);
            String thirdUri = uriInfo.getMatchedURIs().get(2);
            assertEquals("foo", thirdUri);

            // test matched Resources
            assertNotNull(uriInfo.getMatchedResources());
            assertEquals(3, uriInfo.getMatchedResources().size());

            Object matchedResource = uriInfo.getMatchedResources().get(0);
            assertTrue(matchedResource instanceof BarResourceLevel3);

            matchedResource = uriInfo.getMatchedResources().get(1);
            assertTrue(matchedResource instanceof BarResource);

            matchedResource = uriInfo.getMatchedResources().get(2);
            assertTrue(matchedResource instanceof FooResource);

            return "Bar Resourse Level 3";
        }
    }

    @Test
    public void testUriInfoMatchedResourcesAndURIs() throws Exception {

        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET", "/foo", "text/plain");
        invoke(servletRequest);

        servletRequest = MockRequestConstructor.constructMockRequest("GET", "/foo/bar", "text/plain");
        invoke(servletRequest);

        servletRequest = MockRequestConstructor.constructMockRequest("GET", "/foo/bar1", "text/plain");
        invoke(servletRequest);

        servletRequest = MockRequestConstructor.constructMockRequest("GET", "/foo/bar/level3", "text/plain");
        invoke(servletRequest);

    }

    @Test
    public void testUriInfo() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET", "/te%20st/5", "text/plain");
        servletRequest.setQueryString("abc=6");
        invoke(servletRequest);
    }

    @Test
    public void testUriInfoNormalization() throws Exception {

        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET", "/foo/../foo", "text/plain");
        invoke(servletRequest);

        servletRequest = MockRequestConstructor.constructMockRequest("GET", "/foo/../foo/bar", "text/plain");
        invoke(servletRequest);

        servletRequest = MockRequestConstructor.constructMockRequest("GET", "/foo/bar1/../bar1", "text/plain");
        invoke(servletRequest);

        servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/foo/../foo/bar/../bar/level3/../level3/nonsense/..",
                                                        "text/plain");
        invoke(servletRequest);
    }

}
