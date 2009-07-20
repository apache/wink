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

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test the functionality of configured base uri.
 */
public class DefaultLocationHeaderConfigurationTest extends MockServletInvocationTest {

    private static final String HTTP_HOST_PORT_LOCATION = "http://host:port/location";

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {DefaultLocationConfigurationResource.class};
    }

    @Path("/locationHeader")
    public static class DefaultLocationConfigurationResource {

        @Path("/missing")
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public Response getNoLocation(@Context UriInfo uriInfo) {
            return Response.status(Status.CREATED).build();
        }

        @Path("/exist")
        @GET
        @Produces(MediaType.TEXT_XML)
        public Response getWithLocation(@Context UriInfo uriInfo) {
            return Response.status(Status.CREATED).location(URI.create(HTTP_HOST_PORT_LOCATION))
                .build();
        }
    }

    public void testLocationHeaderMissing() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/locationHeader/missing",
                                                        "text/plain");
        request.setSecure(false);
        request.setServerPort(9090);
        MockHttpServletResponse response = invoke(request);
        assertTrue(response.getStatus() == Status.CREATED.getStatusCode());
        assertTrue(response.getHeader(HttpHeaders.LOCATION) == null);
    }

    public void testLocationHeaderExists() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/locationHeader/exist", "text/xml");
        request.setSecure(false);
        request.setServerPort(9090);
        MockHttpServletResponse response = invoke(request);
        assertTrue(response.getStatus() == Status.CREATED.getStatusCode());
        assertTrue(response.getHeader(HttpHeaders.LOCATION).equals(HTTP_HOST_PORT_LOCATION));

    }

}
