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

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class FindResourceMethod4Test extends MockServletInvocationTest {

    static List<Class<?>> resourceClasses = new LinkedList<Class<?>>();

    static {
        List<Class<?>> allResources = new LinkedList<Class<?>>();
        for (Class<?> cls : FindResourceMethod4Test.class.getClasses()) {
            if (cls.getSimpleName().startsWith("Resource")) {
                allResources.add(cls);
            }
        }
        resourceClasses = new LinkedList<Class<?>>(allResources);
    }

    @Override
    protected Class<?>[] getClasses() {
        return resourceClasses.toArray(new Class<?>[resourceClasses.size()]);
    }

    // /// -- Resources --

    @Path("/simplePostConsumes")
    public static class ResourceSimplePostConsumes {

        @POST
        @Consumes( {"application/xml;type=set"})
        @Produces("application/xml")
        public String postSet(String foo) {
            return "ResourceSimplePostConsumes.postSet";
        }

        @POST
        @Consumes( {"application/xml"})
        @Produces("application/xml")
        public String postSingle(String foo) {
            return "ResourceSimplePostConsumes.postSingle";
        }

        @POST
        @Consumes( {"application/xml;type=collection"})
        @Produces("application/xml")
        public String postCollection(String foo) {
            return "ResourceSimplePostConsumes.postCollection";
        }

        @POST
        @Path("/onlyone")
        @Consumes( {"application/xml;type=collection"})
        @Produces("application/xml")
        public String postCollectionOnlyOneMethod(String foo) {
            return "ResourceSimplePostConsumes.postCollectionOnlyOneMethod";
        }

        @POST
        @Path("/twocompeting")
        @Consumes( {"application/xml;type=collection"})
        @Produces("application/xml")
        public String postCollectionSubresourceMethod(String foo) {
            return "ResourceSimplePostConsumes.postCollectionSubresourceMethod";
        }

        @POST
        @Path("/twocompeting")
        @Consumes( {"application/xml;type=set"})
        @Produces("application/xml")
        public String postSetSubresourceMethod(String foo) {
            return "ResourceSimplePostConsumes.postSetSubresourceMethod";
        }

        @POST
        @Path("/twounrelated")
        @Consumes( {"application/xml;foo=bar"})
        @Produces("application/xml")
        public String postSubresourceMethodFooUnrelatedParams(String foo) {
            return "ResourceSimplePostConsumes.postSubresourceMethodFooUnrelatedParams";
        }

        @POST
        @Path("/twounrelated")
        @Consumes( {"application/xml;type=collection"})
        @Produces("application/xml")
        public String postSubresourceMethodTypeUnrelatedParams(String foo) {
            return "ResourceSimplePostConsumes.postSubresourceMethodTypeUnrelatedParams";
        }
    }

    public void testFindResourceBasedOnConsumesParameters() throws Exception {
        MockHttpServletRequest request = null;
        MockHttpServletResponse response = null;

        /*
         * Test simple case where required
         */
        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes",
                                                        "application/xml",
                                                        "application/xml",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response, ResourceSimplePostConsumes.class, "postSingle");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes",
                                                        "application/xml",
                                                        "application/xml;type=set",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response, ResourceSimplePostConsumes.class, "postSet");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes",
                                                        "application/xml",
                                                        "application/xml;type=collection",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response, ResourceSimplePostConsumes.class, "postCollection");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes",
                                                        "application/xml",
                                                        "application/xml;foo=bar",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response, ResourceSimplePostConsumes.class, "postSingle");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes",
                                                        "application/xml",
                                                        "application/xml;type=notacollection",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response, ResourceSimplePostConsumes.class, "postSingle");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes",
                                                        "application/xml",
                                                        "application/xml;type=collection;foo=bar",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response, ResourceSimplePostConsumes.class, "postCollection");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes",
                                                        "application/xml",
                                                        "application/xml;foo=bar;type=collection",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response, ResourceSimplePostConsumes.class, "postCollection");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes",
                                                        "application/xml",
                                                        "application/xml;foo=bar;type=set",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response, ResourceSimplePostConsumes.class, "postSet");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes/onlyone",
                                                        "application/xml",
                                                        "application/xml;foo=bar;type=set",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response, ResourceSimplePostConsumes.class, "postCollectionOnlyOneMethod");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes/onlyone",
                                                        "application/xml",
                                                        "application/xml",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response, ResourceSimplePostConsumes.class, "postCollectionOnlyOneMethod");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes/onlyone",
                                                        "application/xml",
                                                        "application/xml;type=collection",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response, ResourceSimplePostConsumes.class, "postCollectionOnlyOneMethod");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes/twocompeting",
                                                        "application/xml",
                                                        "application/xml;type=collection",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response,
                          ResourceSimplePostConsumes.class,
                          "postCollectionSubresourceMethod");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes/twocompeting",
                                                        "application/xml",
                                                        "application/xml;type=set",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response, ResourceSimplePostConsumes.class, "postSetSubresourceMethod");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes/twocompeting",
                                                        "application/xml",
                                                        "application/xml",
                                                        new byte[] {});
        response = invoke(request);
        assertEquals(200, response.getStatus());
        String resp = response.getContentAsString();
        assertTrue(resp,
                   (ResourceSimplePostConsumes.class.getSimpleName() + ".postSetSubresourceMethod")
                       .equals(resp) || (ResourceSimplePostConsumes.class.getSimpleName() + ".postCollectionSubresourceMethod")
                       .equals(resp));

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes/twounrelated",
                                                        "application/xml",
                                                        "application/xml;type=collection",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response,
                          ResourceSimplePostConsumes.class,
                          "postSubresourceMethodTypeUnrelatedParams");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes/twounrelated",
                                                        "application/xml",
                                                        "application/xml;foo=bar",
                                                        new byte[] {});
        response = invoke(request);
        assertMethodFound(response,
                          ResourceSimplePostConsumes.class,
                          "postSubresourceMethodFooUnrelatedParams");

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/simplePostConsumes/twounrelated",
                                                        "application/xml",
                                                        "application/xml",
                                                        new byte[] {});
        response = invoke(request);
        assertEquals(200, response.getStatus());
        resp = response.getContentAsString();
        assertTrue(resp,
                   (ResourceSimplePostConsumes.class.getSimpleName() + ".postSubresourceMethodTypeUnrelatedParams")
                       .equals(resp) || (ResourceSimplePostConsumes.class.getSimpleName() + ".postSubresourceMethodFooUnrelatedParams")
                       .equals(resp));
    }

    static void assertMethodFound(MockHttpServletResponse response,
                                  Class<?> expectedResource,
                                  String expectedMethod) throws UnsupportedEncodingException {
        assertEquals(200, response.getStatus());
        String expected = expectedResource.getSimpleName() + "." + expectedMethod;
        assertEquals(expected, response.getContentAsString());
    }
}
