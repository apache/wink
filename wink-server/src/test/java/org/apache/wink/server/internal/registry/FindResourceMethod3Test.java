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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class FindResourceMethod3Test extends MockServletInvocationTest {

    static List<Class<?>> resourceClasses = new LinkedList<Class<?>>();

    static {
        List<Class<?>> allResources = new LinkedList<Class<?>>();
        for (Class<?> cls : FindResourceMethod3Test.class.getClasses()) {
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
        @Produces( {"image/*", "image/jpeg"})
        public String getImageAny() {
            return "ResourceSimpleGetProduces.getImageAny";
        }

        @GET
        @Produces( {"*/*"})
        public Response get(@Context HttpHeaders headers) {
            List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
            for (int i = 0; i < mediaTypes.size(); i++) {
                MediaType mediaType = mediaTypes.get(i);
                String qualityString = mediaType.getParameters().get("q");
                if (qualityString != null) {
                    Double d = Double.parseDouble(qualityString);
                    if (d.equals(Double.valueOf(0))) {
                        // intentionally returning the accept media type with
                        // q=0 to ensure outbound flow rejects it
                        ResponseBuilder responseBuilder =
                            RuntimeDelegate.getInstance().createResponseBuilder();
                        // send back a media type that has q=0 -- this should be
                        // detected somewhere
                        responseBuilder.type(new MediaType("image", "jpeg"));
                        return responseBuilder.status(Response.Status.OK).entity("unknown string")
                            .build();
                    }
                }
            }
            return RuntimeDelegate.getInstance().createResponseBuilder().status(Response.Status.OK)
                .entity("ResourceSimpleGetProduces.getAny").build();
        }

    }

    @Path("/hello")
    public static class ResourceHello {

        @GET
        @Produces("*/*")
        public String get() {
            return "ResourceHello.get";
        }
    }

    /**
     * test the q=0 param on accept-header media types. See http spec section 14
     * for q=0 behavior: http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
     */
    public void testFindResourceSimple() throws Exception {
        MockHttpServletRequest request = null;
        MockHttpServletResponse response = null;

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/simpleGetProduces",
                                                        "application/xml");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getXml");

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/simpleGetProduces",
                                                        "application/atom+xml");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getAtom");

        request =
            MockRequestConstructor.constructMockRequest("GET", "/simpleGetProduces", "text/plain");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getText");

        request =
            MockRequestConstructor.constructMockRequest("GET", "/simpleGetProduces", "text/html");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getText");

        request =
            MockRequestConstructor.constructMockRequest("GET", "/simpleGetProduces", "image/jpeg");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getImageJpeg");

        request =
            MockRequestConstructor.constructMockRequest("GET", "/simpleGetProduces", "image/gif");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getImageAny");

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/simpleGetProduces",
                                                        "image/gif;q=0.6,image/jpeg;q=0.5");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getImageAny");

        // q=0 means do not return that media type
        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/simpleGetProduces",
                                                        "image/gif;q=0,image/jpeg;q=0.5");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getImageJpeg");

        // q=0 means do not return that media type
        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/simpleGetProduces",
                                                        "image/gif;q=0.5,image/jpeg;q=0");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getImageAny");

        // q=0 means do not return that media type
        request =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/simpleGetProduces",
                                      "*/*,image/gif;q=0,image/jpeg;q=0,application/atom+xml;q=0");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getXml");

        // q=0 means do not return that media type
        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/simpleGetProduces",
                                                        "*/*,image/*;q=0,application/*;q=0");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getText");

        // q=0 means do not return that media type
        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/simpleGetProduces",
                                                        "text/plain,text/html;q=0");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getText");

        // q=0 means do not return that media type
        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/simpleGetProduces",
                                                        "text/plain;q=0,text/html");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getText");

        // q=0 means do not return that media type
        request =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/simpleGetProduces",
                                      "*/*,text/plain;q=0,text/html;q=0,application/atom+xml;q=0");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getXml");

        // q=0 means do not return that media type. In this test, we accept
        // nothing.
        request =
            MockRequestConstructor.constructMockRequest("GET", "/simpleGetProduces", "*/*;q=0");
        response = invoke(request);
        assertMethodNotFound(response);

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/simpleGetProduces",
                                                        "text/plain;q=0.4,image/jpeg;q=0.5");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getImageJpeg");

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/simpleGetProduces",
                                                        "mytype/mysubtype");
        response = invoke(request);
        assertMethodFound(response, ResourceSimpleGetProduces.class, "getAny");

        // https://issues.apache.org/jira/browse/WINK-106 (positive result)
        request = MockRequestConstructor.constructMockRequest("GET", "/hello", "text/xml");
        response = invoke(request);
        assertMethodFound(response, ResourceHello.class, "get");

        // https://issues.apache.org/jira/browse/WINK-106 (negative result)
        request = MockRequestConstructor.constructMockRequest("GET", "/hello", "text/xml;q=0");
        response = invoke(request);
        assertMethodNotFound(response);
    }

    // // TODO: review and implement test
    // public void testPopulateResponseMediaType() throws Exception {
    // MockHttpServletRequest request = null;
    // MockHttpServletResponse response = null;
    //        
    // // q=0 means do not return that media type
    // // since our method that supports * / * will try to return one of the
    // excluded media types,
    // // PopulateResponseMediaTypeHandler should detect this and return a 406.
    // request = MockRequestConstructor.constructMockRequest("GET",
    // "/simpleGetProduces", "*/*,image/*;q=0,application/*;q=0,text/*;q=0");
    // response = invoke(request);
    // assertMethodNotFound(response);
    //        
    // }

    static void assertMethodFound(MockHttpServletResponse response,
                                  Class<?> expectedResource,
                                  String expectedMethod) throws UnsupportedEncodingException {
        assertEquals(200, response.getStatus());

        /*
         * avoid a bug in the MockServletResponse#setContentType where it tries
         * to parse the charset there could be a
         * "text/plain;charset=UTF-8;otherParam=otherValue" but
         * MockServletResponse will treat charset as
         * "UTF-8;otherParam=otherValue" instead of just "UTF-8"
         */
        String charset =
            MediaType.valueOf(response.getContentType()).getParameters().get("charset");
        response.setCharacterEncoding(charset);

        String expected = expectedResource.getSimpleName() + "." + expectedMethod;
        assertEquals(expected, response.getContentAsString());
    }

    static void assertMethodNotFound(MockHttpServletResponse response)
        throws UnsupportedEncodingException {
        assertEquals(406, response.getStatus());
    }

}
