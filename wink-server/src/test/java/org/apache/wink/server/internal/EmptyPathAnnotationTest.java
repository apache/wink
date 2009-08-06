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
package org.apache.wink.server.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test for https://issues.apache.org/jira/browse/WINK-119
 */
public class EmptyPathAnnotationTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {EmptyRoot.class, EmptySubResourceLocator.class};
    }

    @Path("/")
    public static class EmptyRoot {
        @GET
        @Path("hello")
        public String getHelloWorld() {
            return "Hello world";
        }
    }
    
    @Path("world")
    public static class EmptySubResourceLocator {
        @GET
        public String getWorld() {
            return "World";
        }
        
        @Path("")
        public EmptyRoot getRoot() {
            return new EmptyRoot();
        }
    }
    
    
    public void testRoot() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/hello", "*/*");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        
        request = MockRequestConstructor.constructMockRequest("GET", "hello", "*/*");
        response = invoke(request);
        assertEquals(200, response.getStatus());
    }
    
    public void testSubResourceLocator() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "world", "*/*");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());

        request = MockRequestConstructor.constructMockRequest("GET", "world/hello", "*/*");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        
        request = MockRequestConstructor.constructMockRequest("GET", "world/not-found", "*/*");
        response = invoke(request);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    }

}
