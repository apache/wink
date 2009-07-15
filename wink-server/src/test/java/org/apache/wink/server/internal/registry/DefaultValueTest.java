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

package org.apache.wink.server.internal.registry;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class DefaultValueTest extends MockServletInvocationTest {

    private static final String   DEFAULT_VALUE = "MyDefaultValue";
    private static List<Class<?>> resources     = new LinkedList<Class<?>>();

    static {
        for (Class<?> cls : DefaultValueTest.class.getClasses()) {
            if (cls.getSimpleName().endsWith("Resource")) {
                resources.add(cls);
            }
        }
    }

    @Override
    protected Class<?>[] getClasses() {
        return resources.toArray(new Class<?>[resources.size()]);
    }

    @Path("/a")
    public static class AResource {

        @QueryParam("hulu")
        String      noDefault;

        @DefaultValue(DEFAULT_VALUE)
        @QueryParam("bulu")
        String      defaultValue;

        @QueryParam("mulu")
        Set<String> noDefaultSet;

        @DefaultValue(DEFAULT_VALUE)
        @QueryParam("mulu")
        Set<String> defaultSet;

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get(@DefaultValue("12345") @QueryParam("qulu") int defaultQueryParam,
                          @HeaderParam("hulu") int noDefaultQueryParam) {
            return noDefault + ":"
                + defaultValue
                + ":"
                + noDefaultSet
                + ":"
                + defaultSet
                + ":"
                + defaultQueryParam
                + ":"
                + noDefaultQueryParam;
        }

        @GET
        @Produces(MediaType.TEXT_HTML)
        @DefaultValue("98765")
        public String getParam(@QueryParam("qulu") Integer qulu, @HeaderParam("hulu") int x) {
            return noDefault + ":"
                + defaultValue
                + ":"
                + noDefaultSet
                + ":"
                + defaultSet
                + ":"
                + qulu
                + ":"
                + x;
        }

    }

    public void testAll() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/a", MediaType.TEXT_PLAIN);
        MockHttpServletResponse mockResponse = invoke(mockRequest);
        assertEquals(200, mockResponse.getStatus());
        assertEquals("null:" + DEFAULT_VALUE + ":[]:[" + DEFAULT_VALUE + "]:12345:0", mockResponse
            .getContentAsString());

        // Test @DefaultValue on method
        mockRequest = MockRequestConstructor.constructMockRequest("GET", "/a", MediaType.TEXT_HTML);
        mockResponse = invoke(mockRequest);
        assertEquals(200, mockResponse.getStatus());
        assertEquals("null:" + DEFAULT_VALUE + ":[]:[" + DEFAULT_VALUE + "]:98765:98765",
                     mockResponse.getContentAsString());
    }
}
