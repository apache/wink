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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 */
package org.apache.wink.server.internal;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ResponseTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {LocationUriResource.class};
    }

    @Path("/baseUri")
    public static class LocationUriResource {

        @GET
        public Response getBaseUri() {
            // add this here to make sure null doesn't blow up
            return Response.created(URI.create("abc")).build();
        }

        @GET
        @Path("null")
        public Response getNullUri() {
            // add this here to make sure null doesn't blow up
            return Response.created(URI.create("value")).location(null).build();
        }

        @GET
        @Path("fragment")
        public Response getBaseUriWithFragment() {
            return Response.created(URI.create("def#frag")).build();
        }

        @GET
        @Path("absolute")
        public Response getAbsolute() {
            return Response.created(URI.create("http://example.com/xyz#frag")).build();
        }

        @GET
        @Path("location")
        public Response getLocation() {
            return Response.status(499).location(URI.create("abcd")).build();
        }

        @GET
        @Path("contentLocation")
        public Response getContentLocation() {
            // note that content-location is different and is relative as
            // defined in the spec
            return Response.status(499).contentLocation(URI.create("abcd")).build();
        }

    } // class GetBaseUriResource

    public void testDetection() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/baseUri", "*/*");
        MockHttpServletResponse response = invoke(request);
        assertEquals("http://localhost:80/abc", response.getHeader("Location").toString());

        request = MockRequestConstructor.constructMockRequest("GET", "/baseUri/fragment", "*/*");
        response = invoke(request);
        assertEquals("http://localhost:80/def#frag", response.getHeader("Location").toString());

        request = MockRequestConstructor.constructMockRequest("GET", "/baseUri/absolute", "*/*");
        response = invoke(request);
        assertEquals("http://example.com/xyz#frag", response.getHeader("Location").toString());

        request = MockRequestConstructor.constructMockRequest("GET", "/baseUri/null", "*/*");
        response = invoke(request);
        assertEquals(201, response.getStatus());
        assertNull(response.getHeader("Location"));

        request = MockRequestConstructor.constructMockRequest("GET", "/baseUri/location", "*/*");
        response = invoke(request);
        assertEquals(499, response.getStatus());
        assertEquals("http://localhost:80/abcd", response.getHeader("Location").toString());

        request =
            MockRequestConstructor.constructMockRequest("GET", "/baseUri/contentLocation", "*/*");
        response = invoke(request);
        assertEquals(499, response.getStatus());
        assertEquals("abcd", response.getHeader("Content-Location").toString());

    }
}
