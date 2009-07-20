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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test the functionality of automatic base uri detection from http servlet
 * request.
 */
public class BaseUriDetectionTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {GetBaseUriResource.class};
    }

    @Path("/baseUri")
    public static class GetBaseUriResource {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String getBaseUri(@Context UriInfo uriInfo) {
            return uriInfo.getBaseUri().toString();
        }

    } // class GetBaseUriResource

    public void testDetection() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/context/baseUri", "*/*");
        request.setScheme("http");
        request.setServerName("myServer");
        request.setContextPath("/context");
        request.setServerPort(9090);
        MockHttpServletResponse response = invoke(request);
        String content = response.getContentAsString();
        assertEquals("base URI in content", "http://myServer:9090/context/", content);

        request = MockRequestConstructor.constructMockRequest("GET", "/con%20text/baseUri", "*/*");
        request.setScheme("https");
        request.setServerName("backupSrv");
        request.setContextPath("/con%20text");
        request.setServerPort(2);
        MockHttpServletResponse response2 = invoke(request);
        String content2 = response2.getContentAsString();
        assertEquals("escaped base URI in content", "https://backupSrv:2/con%20text/", content2);
    }

}
