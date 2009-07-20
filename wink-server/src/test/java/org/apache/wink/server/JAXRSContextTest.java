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

package org.apache.wink.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JAXRSContextTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {ContextUriInfoResource.class, ContextHttpHeadersResource.class,
            ContextRequestResource.class, ContextSecurityResource.class,
            ContextProvidersResource.class};
    }

    @Path("/context/uriInfo")
    public static class ContextUriInfoResource {

        @GET
        @Produces
        public String getContext(@Context UriInfo uriInfo) {
            return uriInfo.getPath();
        }
    }

    @Path("/context/httpHeaders")
    public static class ContextHttpHeadersResource {

        @GET
        @Produces
        public String getContext(@Context HttpHeaders headers) {
            return headers.getRequestHeader("Test").get(0);
        }
    }

    @Path("/context/request")
    public static class ContextRequestResource {

        @GET
        @Produces
        public String getContext(@Context javax.ws.rs.core.Request request) {
            return request.getMethod();
        }
    }

    @Path("/context/security")
    public static class ContextSecurityResource {

        @GET
        @Produces
        public String getContext(@Context SecurityContext security) {
            return Boolean.toString(security != null);
        }
    }

    @Path("/context/providers")
    public static class ContextProvidersResource {

        @GET
        @Produces
        public String getContext(@Context Providers providers) {
            return Boolean.toString(providers != null);
        }
    }

    public void testUriInfoContext() throws Exception {
        // TODO test all context fields
        MockHttpServletResponse resp =
            invoke(MockRequestConstructor.constructMockRequest("GET", "/context/uriInfo", "*/*"));
        String result = resp.getContentAsString();
        assertEquals("result", "context/uriInfo", result);
    }

    public void testHttpHeadersContext() throws Exception {
        // TODO test all context fields
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/context/httpHeaders", "*/*");
        mockRequest.addHeader("Test", "Header Value");
        MockHttpServletResponse resp = invoke(mockRequest);
        String result = resp.getContentAsString();
        assertEquals("result", "Header Value", result);
    }

    public void testRequestContext() throws Exception {
        // TODO test all context fields
        MockHttpServletResponse resp =
            invoke(MockRequestConstructor.constructMockRequest("GET", "/context/request", "*/*"));
        String result = resp.getContentAsString();
        assertEquals("result", "GET", result);
    }

    public void testSecurityContext() throws Exception {
        // TODO test all context fields
        MockHttpServletResponse resp =
            invoke(MockRequestConstructor.constructMockRequest("GET", "/context/security", "*/*"));
        String result = resp.getContentAsString();
        assertEquals("result", "true", result);
    }

    public void testProvidersContext() throws Exception {
        // TODO test all context fields
        MockHttpServletResponse resp =
            invoke(MockRequestConstructor.constructMockRequest("GET", "/context/providers", "*/*"));
        String result = resp.getContentAsString();
        assertEquals("result", "true", result);
    }

}
