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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class FindResourceMethod1Test extends MockServletInvocationTest {

    static List<Class<?>> resourceClasses = new LinkedList<Class<?>>();

    static {
        resourceClasses = new LinkedList<Class<?>>();
        resourceClasses.add(ContinuedSearchResource.class);
        resourceClasses.add(ResourceSimpleGet.class);
        resourceClasses.add(ResourceWithSubResourceMethodSimpleGet.class);
        resourceClasses.add(ContinuedSearchResourceLocatorBad.class);
    }

    @Override
    protected Class<?>[] getClasses() {
        return resourceClasses.toArray(new Class<?>[resourceClasses.size()]);
    }

    // /// -- Resources --

    @Path("/{fallback}")
    public static class ResourceFallback {

        @GET
        public String get() {
            return "ResourceFallback.get";
        }
    }

    @Path("/simpleGet")
    public static class ResourceSimpleGet {

        @GET
        public String get(@Context UriInfo uriInfo) {
            List<Object> matchedResources = uriInfo.getMatchedResources();
            assertNotNull(matchedResources);
            assertEquals(1, matchedResources.size());
            assertEquals(ResourceSimpleGet.class, matchedResources.get(0).getClass());

            List<String> matchedURIs = uriInfo.getMatchedURIs(false);
            assertNotNull(matchedURIs);
            assertEquals(1, matchedURIs.size());
            assertEquals("simpleGet", matchedURIs.get(0));

            return "ResourceSimpleGet.get";
        }
    }

    @Path("/simpleGetAndPost")
    public static class ResourceSimpleGetAndPost {

        @GET
        public String get() {
            return "ResourceSimpleGetAndPost.get";
        }

        @POST
        public String post() {
            return "ResourceSimpleGetAndPost.post";
        }
    }

    @Path("/simplePostConsumes")
    public static class ResourceSimplePostConsumes {

        @POST
        @Consumes( {"application/xml"})
        public String postXml() {
            return "ResourceSimplePostConsumes.postXml";
        }

        @POST
        @Consumes( {"application/atom+xml"})
        public String postAtom() {
            return "ResourceSimplePostConsumes.postAtom";
        }

        @POST
        @Consumes( {"text/plain", "text/html"})
        public String postText() {
            return "ResourceSimplePostConsumes.postText";
        }

        @POST
        @Consumes( {"image/*"})
        public String postImage() {
            return "ResourceSimplePostConsumes.postImage";
        }

        @POST
        @Consumes( {"*/*"})
        public String postAny() {
            return "ResourceSimplePostConsumes.postAny";
        }
    }

    @Path("/simpleGetProduces")
    public static class ResourceSimpleGetProduces {

        @GET
        @Produces( {"application/xml"})
        public String getXml() {
            return "ResourceSimpleGetProduces.getXml";
        }

        @GET
        @Produces( {"application/atom+xml"})
        public String getAtom() {
            return "ResourceSimpleGetProduces.getAtom";
        }

        @GET
        @Produces( {"text/plain", "text/html"})
        public String getText() {
            return "ResourceSimpleGetProduces.getText";
        }

        @GET
        @Produces( {"image/jpeg"})
        public String getImageJpeg() {
            return "ResourceSimpleGetProduces.getImageJpeg";
        }

        @GET
        @Produces( {"image/*"})
        public String getImageAny() {
            return "ResourceSimpleGetProduces.getImageAny";
        }

        @GET
        @Produces( {"*/*"})
        public String getAny() {
            return "ResourceSimpleGetProduces.getAny";
        }
    }

    @Path("/simpleConsumesAndProduces")
    public static class ResourceSimpleConsumesAndProduces {

        @POST
        @Consumes( {"text/*"})
        @Produces( {"text/plain"})
        public String postConsumesTextAny() {
            return "ResourceSimpleConsumesAndProduces.postConsumesTextAny";
        }

        @POST
        @Consumes( {"text/plain"})
        @Produces( {"text/*"})
        public String postProducesTextAny() {
            return "ResourceSimpleConsumesAndProduces.postProducesTextAny";
        }

        @POST
        @Consumes( {"text/plain", "text/html"})
        @Produces( {"text/plain"})
        public String postConsumesTextHtml() {
            return "ResourceSimpleConsumesAndProduces.postConsumesTextHtml";
        }

        @POST
        @Consumes( {"text/plain"})
        @Produces( {"text/plain", "text/html"})
        public String postProducesTextHtml() {
            return "ResourceSimpleConsumesAndProduces.postProducesTextHtml";
        }

        @POST
        @Consumes( {"text/plain", "application/*", "image/*"})
        @Produces( {"application/xml", "image/jpeg"})
        public String postConsumesManyProduceMany() {
            return "ResourceSimpleConsumesAndProduces.postConsumesManyProduceMany";
        }
    }

    @Path("/subResourceMethodSimpleGet")
    public static class ResourceWithSubResourceMethodSimpleGet {
        private boolean located = false;

        public ResourceWithSubResourceMethodSimpleGet() {
        }

        public ResourceWithSubResourceMethodSimpleGet(boolean located) {
            this.located = located;
        }

        @GET
        @Produces( {"application/atom+xml"})
        public String getAtom() {
            return "ResourceSimpleGetProduces.getAtom";
        }

        @GET
        @Path("{id}")
        public String getAny(@Context UriInfo uriInfo) {
            List<Object> matchedResources = uriInfo.getMatchedResources();
            assertNotNull(matchedResources);
            List<String> matchedURIs = uriInfo.getMatchedURIs(false);
            assertNotNull(matchedURIs);
            if (located) {
                assertEquals(2, matchedResources.size());
                assertEquals(ResourceWithSubResourceMethodSimpleGet.class, matchedResources.get(0)
                    .getClass());
                assertEquals(ResourceWithSubResourceLocatorSimpleGet.class, matchedResources.get(1)
                    .getClass());
                assertEquals(3, matchedURIs.size());
                assertEquals("subResourceLocatorSimpleGet/1/2", matchedURIs.get(0));
                assertEquals("subResourceLocatorSimpleGet/1", matchedURIs.get(1));
                assertEquals("subResourceLocatorSimpleGet", matchedURIs.get(2));
            } else {
                assertEquals(1, matchedResources.size());
                assertEquals(ResourceWithSubResourceMethodSimpleGet.class, matchedResources.get(0)
                    .getClass());
                assertEquals(2, matchedURIs.size());
                assertEquals("subResourceMethodSimpleGet/1", matchedURIs.get(0));
                assertEquals("subResourceMethodSimpleGet", matchedURIs.get(1));
            }
            return "ResourceWithSubResourceMethodSimpleGet.getAny";
        }

        @GET
        @Path("{id}")
        @Produces("text/plain")
        public String getTextPlain() {
            return "ResourceWithSubResourceMethodSimpleGet.getTextPlain";
        }

        @GET
        @Path("{id}")
        @Produces("text/html")
        public String getTextHtml() {
            return "ResourceWithSubResourceMethodSimpleGet.getTextHtml";
        }

        @GET
        @Path("{id}/4")
        @Produces("text/xhtml")
        public String getSubId4() {
            return "ResourceWithSubResourceMethodSimpleGet.getSubId4";
        }

        @GET
        @Path("{id}/{sub-id}")
        @Produces("text/xhtml")
        public String getSubIdAny() {
            return "ResourceWithSubResourceMethodSimpleGet.getSubIdAny";
        }

        @POST
        @Path("{id}")
        @Consumes("text/*")
        @Produces("text/html")
        public String postTextAny() {
            return "ResourceWithSubResourceMethodSimpleGet.postTextAny";
        }

        @POST
        @Path("{id}")
        @Consumes("image/*")
        @Produces("text/html")
        public String postImageAny() {
            return "ResourceWithSubResourceMethodSimpleGet.postImageAny";
        }

    }

    @Path("/subResourceLocatorSimpleGet")
    public static class ResourceWithSubResourceLocatorSimpleGet {

        @Path("{id}")
        public ResourceWithSubResourceMethodSimpleGet getAny(@PathParam("id") String id,
                                                             @Context UriInfo uriInfo) {
            List<Object> matchedResources = uriInfo.getMatchedResources();
            assertNotNull(matchedResources);
            assertEquals(1, matchedResources.size());
            assertEquals(ResourceWithSubResourceLocatorSimpleGet.class, matchedResources.get(0)
                .getClass());

            List<String> matchedURIs = uriInfo.getMatchedURIs(false);
            assertNotNull(matchedURIs);
            assertEquals(2, matchedURIs.size());
            assertEquals("subResourceLocatorSimpleGet/" + id, matchedURIs.get(0));
            assertEquals("subResourceLocatorSimpleGet", matchedURIs.get(1));

            return new ResourceWithSubResourceMethodSimpleGet(true);
        }

        @Path("{id}/4")
        public ResourceWithSubResourceMethodSimpleGet getSubId4(@PathParam("id") String id) {
            assertEquals("1", id);
            return new ResourceWithSubResourceMethodSimpleGet();
        }

        @Path("ignore-consumes")
        @Consumes("text/kuku")
        public ResourceWithSubResourceMethodSimpleGet ignoreConsumes() {
            return new ResourceWithSubResourceMethodSimpleGet();
        }

        @Path("ignore-produces")
        @Produces("text/kuku")
        public ResourceWithSubResourceMethodSimpleGet ignoreProduces() {
            return new ResourceWithSubResourceMethodSimpleGet();
        }

        // locators cannot have an entity param
        @Path("bad-locator")
        public void badLocator(String entity) {
            fail("locator method should not have been invoked");
        }

    }

    @Path("/mixed/{id}")
    public static class ResourceMixed {

        // methods
        @GET
        @Produces("application/xml")
        public String getXml() {
            return "ResourceMixed.getXml";
        }

        @POST
        @Consumes("application/xml")
        public String postXml() {
            return "ResourceMixed.postXml";
        }

        @POST
        @Consumes("image/gif")
        @Produces("image/jpeg")
        public String postImage() {
            return "ResourceMixed.postImage";
        }

        // sub-resource methods
        @GET
        @Path("{sub-id}")
        @Produces("text/plain")
        public String getTextPlain() {
            return "ResourceMixed.getTextPlain";
        }

        @Path("locate")
        public ResourceMixed locateTextPlainSpecific() {
            return new ResourceMixed();
        }

        @POST
        @Path("{sub-id}")
        @Consumes("text/*")
        @Produces("text/html")
        public String postTextAny() {
            return "ResourceMixed.postTextAny";
        }

        // sub-resource locators
        @Path("{sub-id}")
        public ResourceMixed locateTextPlain() {
            return new ResourceMixed();
        }

        @Path("locateNull")
        public ResourceMixed locateNull() {
            return null;
        }
    }

    // == resources for continued search policy testing

    @Path("/{continued}")
    public static class ContinuedSearchResource {
        @PUT
        public String put(@Context UriInfo uriInfo) {
            MultivaluedMap<String, String> variables = uriInfo.getPathParameters();
            assertEquals("simpleGet", variables.getFirst("continued"));
            return "ContinuedSearchResource.put";
        }

        @PUT
        @Path("{subPutId}")
        public String subPut(@Context UriInfo uriInfo) {
            MultivaluedMap<String, String> variables = uriInfo.getPathParameters();
            assertEquals("subResourceMethodSimpleGet", variables.getFirst("continued"));
            assertEquals("1", variables.getFirst("subPutId"));
            return "ContinuedSearchResource.subPut";
        }

        @Path("{subLocatorId}")
        public LocatedContinuedSearchResource subLocator() {
            return new LocatedContinuedSearchResource();
        }
    }

    @Path("/continuedSearchResourceLocatorBad")
    public static class ContinuedSearchResourceLocatorBad {
        @Path("{badSubLocatorId}")
        public ResourceWithSubResourceMethodSimpleGet subLocator() {
            return new ResourceWithSubResourceMethodSimpleGet();
        }
    }

    public static class LocatedContinuedSearchResource {
        @PUT
        @Path("{locatedSubPutId}")
        public String subPut(@Context UriInfo uriInfo) {
            MultivaluedMap<String, String> variables = uriInfo.getPathParameters();
            assertEquals("continuedSearchResourceLocatorBad", variables.getFirst("continued"));
            assertEquals("1", variables.getFirst("subLocatorId"));
            assertEquals("2", variables.getFirst("locatedSubPutId"));
            assertNull(variables.getFirst("badSubLocatorId"));
            assertNull(variables.getFirst("id"));
            return "LocatedContinuedSearchResource.subPut";
        }
    }

    // /// -- Tests --

    public void testContinuedSearch_1_1() throws Exception {
        MockHttpServletRequest request = null;
        MockHttpServletResponse response = null;

        // 1. test resource method
        // 1.1. negative test - make sure that ContinuedSearchResource is not
        // reachable when continued search policy is off
        request =
            MockRequestConstructor.constructMockRequest("PUT",
                                                        "/simpleGet",
                                                        "text/plain",
                                                        "text/plain",
                                                        null);
        response = invoke(request);
        assertEquals(405, response.getStatus());

    }

    public void testContinuedSearch_2_1() throws Exception {
        MockHttpServletRequest request = null;
        MockHttpServletResponse response = null;

        // 2. test sub-resource method
        // 2.1. negative test - make sure that ContinuedSearchResource is not
        // reachable when continued search policy is off
        request =
            MockRequestConstructor.constructMockRequest("PUT",
                                                        "/subResourceMethodSimpleGet/1",
                                                        "text/plain",
                                                        "text/plain",
                                                        null);
        response = invoke(request);
        assertEquals(405, response.getStatus());

    }

    public void testContinuedSearch_3_1() throws Exception {
        MockHttpServletRequest request = null;
        MockHttpServletResponse response = null;

        // 3. test sub-resource locator
        // 3.1. negative test - make sure that ContinuedSearchResource is not
        // reachable when continued search policy is off
        request =
            MockRequestConstructor.constructMockRequest("PUT",
                                                        "/continuedSearchResourceLocatorBad/1/2",
                                                        "text/plain",
                                                        "text/plain",
                                                        null);
        response = invoke(request);
        assertEquals(405, response.getStatus());

    }

    // // -- Helpers --

    static void assertMethodFound(MockHttpServletResponse response,
                                  Class<?> expectedResource,
                                  String expectedMethod) throws UnsupportedEncodingException {
        assertEquals(200, response.getStatus());
        String expected = expectedResource.getSimpleName() + "." + expectedMethod;
        assertEquals(expected, response.getContentAsString());
    }

}
