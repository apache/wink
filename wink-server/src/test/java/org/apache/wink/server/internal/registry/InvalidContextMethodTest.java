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
package org.apache.wink.server.internal.registry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class InvalidContextMethodTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {TestResource.class};
    }

    @Path("abcd")
    public static class TestResource {
        
        public void foo(String a, Object b) {
            
        }

        @Context
        public void setSecurityContext(SecurityContext context, SecurityContext anotherContext) {
            /* do nothing */
        }

        public void doSomething(String a) {

        }

        public void bah() {

        }

        @GET
        @Produces( {MediaType.TEXT_PLAIN})
        public String getCollection() {
            return "Hello world";
        }

    }

    @Test
    public void testStillValidResource() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/abcd", MediaType.TEXT_PLAIN);
        MockHttpServletResponse response = invoke(mockRequest);
        assertEquals("Hello world", response.getContentAsString());
        assertEquals(200, response.getStatus());
    }

}
