/*******************************************************************************
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *     
 *      http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 *******************************************************************************/
package org.apache.wink.server.internal.registry;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class EncodedTest extends MockServletInvocationTest {

    private static final String   NOT_ENCODED = "bulu bulu";
    private static final String   ENCODED     = "bulu+bulu";
    private static List<Class<?>> resources   = new LinkedList<Class<?>>();

    static {
        for (Class<?> cls : EncodedTest.class.getClasses()) {
            if (cls.getSimpleName().endsWith("Resource")) {
                resources.add(cls);
            }
        }
    }

    @Override
    protected Class<?>[] getClasses() {
        return resources.toArray(new Class<?>[resources.size()]);
    }

    @Path("/a/{action}")
    public static class AResource {

        @QueryParam("hulu")
        String notEncoded;

        @Encoded
        @QueryParam("bulu")
        String encoded;

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get(@PathParam("action") String action,
            @Encoded @QueryParam("bulu") String encodedParam) {
            if (action.equals("encoded")) {
                return encoded;
            }
            if (action.equals("bulubulu")) {
                return encodedParam;
            }
            return notEncoded;
        }

        @GET
        @Produces(MediaType.TEXT_HTML)
        @Encoded
        public String getParam(@QueryParam("kulu") String kulu) {
            return kulu;
        }

    }

    @Encoded
    @Path("/encoded")
    public static class EncodedResource {
        @GET
        @Produces(MediaType.TEXT_HTML)
        public String getParam(@QueryParam("kulu") String kulu) {
            return kulu;
        }
    }

    public void testAll() throws Exception {
        MockHttpServletRequest mockRequest = MockRequestConstructor.constructMockRequest("GET",
            "/a/notencoded", MediaType.TEXT_PLAIN);
        mockRequest.setQueryString("hulu=" + ENCODED);
        MockHttpServletResponse mockResponse = invoke(mockRequest);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(NOT_ENCODED, mockResponse.getContentAsString());

        mockRequest = MockRequestConstructor.constructMockRequest("GET", "/a/encoded",
            MediaType.TEXT_PLAIN);
        mockRequest.setQueryString("bulu=" + ENCODED);
        mockResponse = invoke(mockRequest);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(ENCODED, mockResponse.getContentAsString());

        mockRequest = MockRequestConstructor.constructMockRequest("GET", "/a/bulubulu",
            MediaType.TEXT_PLAIN);
        mockRequest.setQueryString("bulu=" + ENCODED);
        mockResponse = invoke(mockRequest);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(ENCODED, mockResponse.getContentAsString());

        mockRequest = MockRequestConstructor.constructMockRequest("GET", "/a/bulubulu",
            MediaType.TEXT_HTML);
        mockRequest.setQueryString("kulu=" + ENCODED);
        mockResponse = invoke(mockRequest);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(ENCODED, mockResponse.getContentAsString());
        
        mockRequest = MockRequestConstructor.constructMockRequest("GET", "/encoded",
            MediaType.TEXT_HTML);
        mockRequest.setQueryString("kulu=" + ENCODED);
        mockResponse = invoke(mockRequest);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(ENCODED, mockResponse.getContentAsString());
    }

}
