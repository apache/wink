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

package org.apache.wink.server.internal.registry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.annotations.Scope;
import org.apache.wink.common.annotations.Scope.ScopeType;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 */
public class PrototypeResourceTest extends MockServletInvocationTest {

    private static final int ITERATIONS = 23;
    public static int        counter    = 0;

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {TestResource.class};
    }

    @Scope(ScopeType.PROTOTYPE)
    @Path("/stam")
    public static class TestResource {

        public TestResource() {
            ++counter;
        }

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getCollection() {
            return "";
        }

    }

    public void testMultipleInvocation() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/stam",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);

        for (int i = 0; i < ITERATIONS; ++i) {
            invoke(mockRequest);
        }
        assertEquals(ITERATIONS, counter);
    }
}
