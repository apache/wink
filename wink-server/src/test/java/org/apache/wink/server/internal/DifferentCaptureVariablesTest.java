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
 */
package org.apache.wink.server.internal;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Even if the regular expression used in a sub-resource method is the same
 * (which it must be according to JAX-RS specification), the capture variable
 * names can be different in 2 different HTTP methods. Need to use the proper
 * capture variable names.
 */
public class DifferentCaptureVariablesTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {TestResource.class, TestResource2.class};
    }

    @Path("/test")
    public static class TestResource {

        @GET
        @Path("{id1}")
        public String getMethod(@PathParam("id1") String a) {
            return a;
        }

        @POST
        public String doSomething() {
            return null;
        }

        @DELETE
        @Path("{id3}")
        public String deleteMethod(@PathParam("id3") String id) {
            return id;
        }
    }

    @Path("/test2")
    public static class TestResource2 {

        @GET
        @Path("{id1}")
        public String getMethod(@PathParam("id1") String id) {
            return id;
        }

        @POST
        @Path("{id2}/a")
        public String doSomething(@PathParam("id2") String id) {
            return id;
        }

        @DELETE
        @Path("{id3}")
        public String deleteMethod(@PathParam("id3") String id) {
            return id;
        }
    }

    public void testDifferentCaptureVariableNames() throws Exception {
        MockHttpServletResponse resp =
            invoke(MockRequestConstructor.constructMockRequest("GET", "/test/1234", "*/*"));
        assertEquals("", "1234", resp.getContentAsString());

        resp = invoke(MockRequestConstructor.constructMockRequest("DELETE", "/test/5678", "*/*"));
        assertEquals("", "5678", resp.getContentAsString());
    }

    public void testDifferentCaptureVariableNames2() throws Exception {
        MockHttpServletResponse resp =
            invoke(MockRequestConstructor.constructMockRequest("DELETE", "/test2/1234", "*/*"));
        assertEquals("", "1234", resp.getContentAsString());

        resp =
            invoke(MockRequestConstructor.constructMockRequest("POST", "/test2/9012/a", "*/*"));
        assertEquals("", "9012", resp.getContentAsString());
        
        resp = invoke(MockRequestConstructor.constructMockRequest("GET", "/test/5678", "*/*"));
        assertEquals("", "5678", resp.getContentAsString());
    }

}
