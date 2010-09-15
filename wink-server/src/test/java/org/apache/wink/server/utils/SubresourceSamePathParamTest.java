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
package org.apache.wink.server.utils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/*
 * test what happens when deeply nested subresources
 * use the same path param identifier in the @Path annotation
 */
public class SubresourceSamePathParamTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        Class<?>[] classes = new Class<?>[]{TopResource.class};
        return classes;
    }
    
    @Path("/top")
    public static class TopResource {
        @Path("{id}")
        public MidResource getByid(@PathParam("id") int id) {
            if (id == 1) {
                return new MidResource();
            }
            return null;
        }
    }
    
    public static class MidResource {
        @Path("{id}")  // same name as in TopResource
        public BottomResource getByid(@PathParam("id") int id) {
            if (id == 2) {  // different number than the conditional in TopResource.getByid
                return new BottomResource();
            }
            return null;
        }
    }
    
    public static class BottomResource {
        @GET
        public String get() {
            return "you found me";
        }
    }
    
    public void testMultiSubresources() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/top/1/2",
                                                        MediaType.TEXT_PLAIN);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("you found me", response.getContentAsString());
    }
    
}
