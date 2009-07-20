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

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.wink.common.http.HttpHeadersEx;
import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.http.OPTIONS;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.junit.Assert;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class OptionsMethodTest extends MockServletInvocationTest {

    private static final String[] METHODS = {"HEAD", "GET", "PUT", "DELETE", "POST", "OPTIONS"};

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {OptionsResource.class, CustomOptionsResource.class};
    }

    @Path(OptionsResource.PATH)
    public static class OptionsResource {

        public static final String PATH = "/test";

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON})
        public void get() {
        }

        @PUT
        @Consumes( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON})
        public void put() {
        }

        @DELETE
        public void delete() {
        }

        @POST
        @Consumes( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON})
        @Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON})
        public void post() {
        }
    }

    @Path(CustomOptionsResource.PATH)
    public static class CustomOptionsResource extends OptionsResource {

        public static final String PATH          = "/test-custom";

        public static final String CUSTOM_HEADER = "x-myheader";

        @OPTIONS
        public Response options() {
            Response response =
                RuntimeDelegate.getInstance().createResponseBuilder().status(204)
                    .header(CUSTOM_HEADER, "value").build();

            return response;
        }
    }

    public void testDefaultOptionsMethod() throws Exception {
        checkOptions(OptionsResource.PATH, METHODS, false);
    }

    public void testCustomOptionsMethod() throws Exception {
        checkOptions(CustomOptionsResource.PATH, METHODS, true);
    }

    private void checkOptions(String path, String[] methods, boolean customHeader) throws Exception {

        // request
        MockHttpServletRequest request = new MockHttpServletRequest() {
            public String getPathTranslated() {
                return null; // prevent Spring to resolve the file on the file
                             // system which fails
            }
        };
        request.setMethod("OPTIONS");
        request.setRequestURI(path);
        MockHttpServletResponse response = invoke(request);

        // response
        // check status code
        Assert.assertEquals(HttpStatus.NO_CONTENT.getCode(), response.getStatus());
        // custom header
        if (customHeader) {
            Assert.assertNotNull(response.getHeader(CustomOptionsResource.CUSTOM_HEADER));
        } else {
            // check allow
            String allowStr = (String)response.getHeader(HttpHeadersEx.ALLOW);
            List<String> allows = Arrays.asList(allowStr.split("\\s*,\\s*"));
            Assert.assertEquals(methods.length, allows.size());
            for (String method : methods) {
                Assert.assertTrue(allows.contains(method));
            }
            Assert.assertNull(response.getHeader(CustomOptionsResource.CUSTOM_HEADER));
        }
    }
}
