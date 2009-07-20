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

package org.apache.wink.server.internal;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.wink.common.internal.http.Accept;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for case when it is unknown exactly, which data type (e.g.
 * documentation pdf, work, postscript, ...) of resource will be available.
 * <p/>
 * <em>Note: </em>Initiated by bug <tt>#39213</tt>.
 */
public class UnknownMimeTypeTest extends MockServletInvocationTest {

    private static final String PDF             = "pdf";
    private static final String THROW_EXCEPTION = "throw_exception";

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {UnknownMimeTypeResource.class};
    }

    /**
     * resource for tests.
     */
    @Path("get/{variable}")
    public static class UnknownMimeTypeResource {

        /**
         * Resource method for testing GET for unknown MIME type. The produced
         * MIME type is unknown. However the type of document can be requested
         * in URI, as "get/pdf", meaning the requested document is
         * application/pdf. In the latter case header <tt>Content-Type</tt> is
         * set in response explicitly here.
         * 
         * @param typeName type of requested document as part of URI (get/pdf ->
         *            pdf document)
         * @param request SDK request
         * @return resource representation with content type set according to
         *         the actual resource MIME type
         */
        @GET
        @Produces(value = MediaType.WILDCARD)
        public Response get(@PathParam("variable") String typeName, @Context HttpHeaders headers) {
            if (typeName.equals("unknown") || typeName.equals("atom")) {
                return RuntimeDelegate.getInstance().createResponseBuilder()
                    .status(Response.Status.OK).entity("unknown string").build();
            } else if (typeName.equals("pdf")) {
                Accept accept = new Accept(headers.getAcceptableMediaTypes());
                if (!accept.isAcceptable(MediaTypeUtils.PDF_TYPE)) {
                    return RuntimeDelegate.getInstance().createResponseBuilder()
                        .status(Response.Status.NOT_ACCEPTABLE).build();
                }
                return RuntimeDelegate.getInstance().createResponseBuilder()
                    .status(Response.Status.OK).entity("pdf string")
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypeUtils.PDF).build();
            } else if (typeName.equals("throw_exception")) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            return null;
        }

    } // 

    /**
     * Test that request for resource of unknown type is processed.
     * 
     * @throws IOException problem with getting resource for parsing
     * @throws XmlException problem with parsing by xmlbeans
     */
    public void testGetUnknown() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest(HttpMethod.GET.toString(),
                                                        "get/unknown",
                                                        MediaType.WILDCARD);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertEquals("entry id", "unknown string", response.getContentAsString());
    }

    /**
     * Test that if resource is pdf that content type in response is correctly
     * set.
     * 
     * @throws IOException problem with getting resource for parsing
     * @throws XmlException problem with parsing by xmlbeans
     */
    public void testGetPdf() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest(HttpMethod.GET.toString(),
                                                        "get/" + PDF,
                                                        MediaType.WILDCARD);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertTrue("content type", response.getContentType().contains(MediaTypeUtils.PDF));
    }

    // Test http://qcweb/qcweb/showBug.jsp?bug=39720
    public void testGetPdfWithConcreteType() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest(HttpMethod.GET.toString(),
                                                        "get/" + PDF,
                                                        MediaTypeUtils.PDF);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertTrue("content type", response.getContentType().contains(MediaTypeUtils.PDF));
    }

    public void testGetPdfWithWrongConreteType() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest(HttpMethod.GET.toString(),
                                                        "get/" + PDF,
                                                        MediaType.APPLICATION_OCTET_STREAM);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 406, response.getStatus());
    }

    public void testAtomWithWrongConreteType() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest(HttpMethod.GET.toString(),
                                                        "get/atom",
                                                        MediaType.APPLICATION_OCTET_STREAM);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());
    }

    /**
     * Test that resource returns correct error code
     */
    public void testRestException() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest(HttpMethod.GET.toString(),
                                                        "get/" + THROW_EXCEPTION,
                                                        MediaType.WILDCARD);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 400, response.getStatus());
    }

}
