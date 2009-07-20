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

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test invocation to resource which cannot be dispatched.
 */
public class DispatchErrorTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {NotDispatchedResource.class};
    }

    @Path("/path")
    public static class NotDispatchedResource {

        @PUT
        @Consumes(MediaType.TEXT_HTML)
        @Produces(MediaType.TEXT_HTML)
        public Response put() {
            // empty
            return null;
        }

    }

    public void NotFound() throws Exception {
        MockHttpServletResponse response =
            invoke(MockRequestConstructor.constructMockRequest("GET", "zzz", "*/*"));
        assertEquals("status", HttpStatus.NOT_FOUND.getCode(), response.getStatus());
    }

    public void testMethodNotAllowed() throws Exception {
        MockHttpServletResponse response =
            invoke(MockRequestConstructor.constructMockRequest("GET", "path", "*/*"));
        assertEquals("status", HttpStatus.METHOD_NOT_ALLOWED.getCode(), response.getStatus());
    }

    public void testNotAcceptable() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("PUT", "path", "text/plain");
        servletRequest.setContentType(MediaType.TEXT_HTML);
        MockHttpServletResponse response = invoke(servletRequest);
        assertEquals("status", HttpStatus.NOT_ACCEPTABLE.getCode(), response.getStatus());
    }

    public void testUnsupportedMediaType() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("PUT", "path", MediaType.TEXT_XML);
        servletRequest.setContentType("text/vnd.x-test");
        MockHttpServletResponse response = invoke(servletRequest);
        assertEquals("status", HttpStatus.UNSUPPORTED_MEDIA_TYPE.getCode(), response.getStatus());
    }

}
