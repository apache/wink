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
package org.apache.wink.spring;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.providers.entity.StringProvider;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.SpringMockServletInvocationTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class RegistrarTest extends SpringMockServletInvocationTest {

    private static final int    RESOURCE_INVOCATIONS = 100;
    private static final String RESOURCE_PATH        = "resource";
    private static final String PATH                 = "path";
    private static final String HELLO_FROM_RESOURCE  = "Hello from Resource";
    private static final String HELLO_2              = "Hello2";

    private static int          resource_counter     = 0;
    private static int          provider_counter     = 0;

    @Path("/" + RESOURCE_PATH)
    public static class Resource {

        public Resource() {
            ++resource_counter;
        }

        @GET
        public String get() {
            return HELLO_FROM_RESOURCE;
        }

    }

    @Path("/" + PATH)
    public static class AnotherResource {

        @GET
        public String get() {
            return HELLO_2;
        }
    }

    @javax.ws.rs.ext.Provider
    public static class Provider extends StringProvider {

        public Provider() {
            ++provider_counter;
        }

    }

    public void testRegistration() throws Exception {

        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/" + RESOURCE_PATH,
                                                        MediaType.WILDCARD_TYPE);
        for (int i = 0; i < RESOURCE_INVOCATIONS; ++i) {
            MockHttpServletResponse mockResponse = invoke(mockRequest);
            assertEquals(HttpServletResponse.SC_OK, mockResponse.getStatus());
            assertEquals(HELLO_FROM_RESOURCE, mockResponse.getContentAsString());
        }
        assertEquals(RESOURCE_INVOCATIONS, resource_counter);

        mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/" + PATH, MediaType.WILDCARD_TYPE);
        MockHttpServletResponse mockResponse = invoke(mockRequest);
        assertEquals(HttpServletResponse.SC_OK, mockResponse.getStatus());
        assertEquals(HELLO_2, mockResponse.getContentAsString());

        assertEquals(1, provider_counter);
    }

}
