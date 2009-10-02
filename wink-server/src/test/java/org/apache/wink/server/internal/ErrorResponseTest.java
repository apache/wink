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

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests complete dispatch process up to resource throwing an error. See JAX-RS
 * 3.2 last paragraph
 */
public class ErrorResponseTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {ErrorResource.class};
    }

    @Path("errors/")
    public static class ErrorResource {

        @GET
        @Path("queryparam")
        @Produces(MediaType.TEXT_PLAIN)
        public String handleGetQueryParam(@QueryParam("queryParam") Integer query) {
            return "helloQueryParam" + query.toString();
        }

        @GET
        @Path("{pathParam}/pathparam")
        @Produces(MediaType.TEXT_PLAIN)
        public String handleGetPathParam(@PathParam("pathParam") Integer query) {
            return "helloPathParam" + query.toString();
        }

        @GET
        @Path("matrixparam")
        @Produces(MediaType.TEXT_PLAIN)
        public String handleGetMatrixParam(@MatrixParam("matrixParam") Integer query) {
            return "helloMatrixParam" + query.toString();
        }

        @GET
        @Path("headerparam")
        @Produces(MediaType.TEXT_PLAIN)
        public String handleGetHeaderParam(@HeaderParam("headerParam") Integer query) {
            return "helloHeaderParam" + query.toString();
        }

        @GET
        @Path("cookieparam")
        @Produces(MediaType.TEXT_PLAIN)
        public String handleGetCookieParam(@CookieParam("cookieParam") Integer query) {
            return "helloCookieParam" + query.toString();
        }

        @GET
        @Path("formparam")
        @Produces(MediaType.TEXT_PLAIN)
        public String handleGetFormParam(@FormParam("formParam") Integer query) {
            return "helloFormParam" + query.toString();
        }

    } // class ErrorResource

    /*
     * tests below come in pairs, such as "testPathParamSuccess" and
     * "testPathParamError"
     */

    // sanity check for successful path param construction
    public void testPathParamSuccess() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/errors/100/pathparam",
                                                        MediaType.TEXT_PLAIN);
        MockHttpServletResponse servletResponse = assertInvocation(servletRequest, HttpStatus.OK);
        // make sure we called the right GET method
        assertEquals("helloPathParam100", servletResponse.getContentAsString());
    }

    // failure to construct path param should result in 404
    public void testPathParamError() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/errors/bob/pathParam",
                                                        MediaType.TEXT_PLAIN);
        assertInvocation(servletRequest, HttpStatus.NOT_FOUND);
    }

    // sanity check for successful query param construction
    public void testQueryParamSuccess() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/errors/queryparam",
                                                        MediaType.TEXT_PLAIN);
        servletRequest.setQueryString("queryParam=100");
        MockHttpServletResponse servletResponse = assertInvocation(servletRequest, HttpStatus.OK);
        // make sure we called the right GET method
        assertEquals("helloQueryParam100", servletResponse.getContentAsString());
    }

    // failure to construct query param should result in 404
    public void testQueryParamError() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/errors/queryparam",
                                                        MediaType.TEXT_PLAIN);
        servletRequest.setQueryString("queryParam=bob");
        assertInvocation(servletRequest, HttpStatus.NOT_FOUND);
    }

    // sanity check for successful matrix param construction
    public void testMatrixParamSuccess() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/errors/matrixparam;matrixParam=100",
                                                        MediaType.TEXT_PLAIN);
        MockHttpServletResponse servletResponse = assertInvocation(servletRequest, HttpStatus.OK);
        // make sure we called the right GET method
        assertEquals("helloMatrixParam100", servletResponse.getContentAsString());
    }

    // failure to construct matrix param should result in 404
    public void testMatrixParamError() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/errors/matrixparam;matrixParam=bob",
                                                        MediaType.TEXT_PLAIN);
        assertInvocation(servletRequest, HttpStatus.NOT_FOUND);
    }

    // sanity check for successful header param construction
    public void testHeaderParamSuccess() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/errors/headerparam",
                                                        MediaType.TEXT_PLAIN);
        servletRequest.addHeader("headerParam", "100");
        MockHttpServletResponse servletResponse = assertInvocation(servletRequest, HttpStatus.OK);
        // make sure we called the right GET method
        assertEquals("helloHeaderParam100", servletResponse.getContentAsString());
    }

    // failure to construct header param should result in 400
    public void testHeaderParamError() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/errors/headerparam",
                                                        MediaType.TEXT_PLAIN);
        servletRequest.addHeader("headerParam", "bob");
        assertInvocation(servletRequest, HttpStatus.BAD_REQUEST);
    }

    // sanity check for successful cookie param construction
    public void testCookieParamSuccess() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/errors/cookieparam",
                                                        MediaType.TEXT_PLAIN);
        servletRequest.addHeader("Cookie", "$Version=1; cookieParam=100");
        MockHttpServletResponse servletResponse = assertInvocation(servletRequest, HttpStatus.OK);
        // make sure we called the right GET method
        assertEquals("helloCookieParam100", servletResponse.getContentAsString());
    }

    // failure to construct cookie param should result in 400
    public void testCookieParamError() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/errors/cookieparam",
                                                        MediaType.TEXT_PLAIN);
        servletRequest.addHeader("Cookie", "$Version=1; cookieParam=bob");
        assertInvocation(servletRequest, HttpStatus.BAD_REQUEST);
    }

    // sanity check for successful form param construction
    // see JAX-RS E010
    // http://jcp.org/aboutJava/communityprocess/maintenance/jsr311/311ChangeLog.html
    public void testFormParamSuccess() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/errors/formparam",
                                                        MediaType.TEXT_PLAIN);
        servletRequest.addHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        servletRequest.setContent("formParam=100".getBytes());
        servletRequest.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MockHttpServletResponse servletResponse = assertInvocation(servletRequest, HttpStatus.OK);
        // make sure we called the right GET method
        assertEquals("helloFormParam100", servletResponse.getContentAsString());
    }

    // failure to construct form param should result in 400
    // see JAX-RS E010
    // http://jcp.org/aboutJava/communityprocess/maintenance/jsr311/311ChangeLog.html
    public void testFormParamError() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/errors/formparam",
                                                        MediaType.TEXT_PLAIN);
        servletRequest.addHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        servletRequest.setContent("formParam=bob".getBytes());
        servletRequest.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation(servletRequest, HttpStatus.BAD_REQUEST);
    }

    // utility method

    private MockHttpServletResponse assertInvocation(MockHttpServletRequest servletRequest,
                                                     HttpStatus httpStatus) throws Exception {
        MockHttpServletResponse mockHttpServletResponse = invoke(servletRequest);
        try {
            assertEquals("http status", httpStatus.getCode(), mockHttpServletResponse.getStatus());
            return mockHttpServletResponse;
        } catch (AssertionError ae) {
            System.err.println(mockHttpServletResponse.getContentAsString());
            throw ae;
        }
    }
}
