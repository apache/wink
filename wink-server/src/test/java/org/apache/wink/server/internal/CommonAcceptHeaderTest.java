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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.wink.server.internal;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for common accept headers.
 */
public class CommonAcceptHeaderTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {EmptyRoot.class};
    }

    @Path("/")
    public static class EmptyRoot {
        @GET
        @Path("countaccepttypes")
        public String getHelloWorld(@Context HttpHeaders requestHeaders) {
            List<MediaType> acceptMediaTypes = requestHeaders.getAcceptableMediaTypes();
            if (acceptMediaTypes == null || acceptMediaTypes.isEmpty()) {
                return "0";
            }
            // System.out.println(acceptMediaTypes);
            return acceptMediaTypes.size() + "";
        }
    }

    public void testWildcardOnly() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/countaccepttypes", "*");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("1", response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("GET", "/countaccepttypes", "*/;q=0.8");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("1", response.getContentAsString());

        request = MockRequestConstructor.constructMockRequest("GET", "/countaccepttypes", "*/");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("1", response.getContentAsString());
    }

    public void testEmptyStringAcceptHeader() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/countaccepttypes", "");
        try {
            invoke(request);
            fail();
        } catch (IllegalArgumentException e) {

        }
        request = MockRequestConstructor.constructMockRequest("GET", "/countaccepttypes", "      ");
        try {
            invoke(request);
            fail();
        } catch (IllegalArgumentException e) {

        }
    }

    public void testHttpURLConnectionAcceptHeader() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/countaccepttypes",
                                      "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("5", response.getContentAsString());
    }

    public void testFirefoxAcceptHeader() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/countaccepttypes",
                                      "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("4", response.getContentAsString());
    }

    public void testIE7AcceptHeader() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/countaccepttypes",
                                                        "image/gif, " + "image/jpeg, "
                                                            + "image/pjpeg, "
                                                            + "image/pjpeg, "
                                                            + "application/x-shockwave-flash, "
                                                            + "application/x-ms-application, "
                                                            + "application/x-ms-xbap, "
                                                            + "application/vnd.ms-xpsdocument, "
                                                            + "application/xaml+xml, "
                                                            + "*/*");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("10", response.getContentAsString());
    }

    public void testIEAcceptHeaderWithFlashAndSilverlightAndOfficeAcceptHeader() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor
                .constructMockRequest("GET", "/countaccepttypes", "image/gif, " + "image/jpeg, "
                    + "image/pjpeg, "
                    + "application/x-ms-application, "
                    + "application/vnd.ms-xpsdocument, "
                    + "application/xaml+xml, "
                    + "application/x-ms-xbap, "
                    + "application/x-shockwave-flash, application/x-silverlight-2-b2, "
                    + "application/x-silverlight, application/vnd.ms-excel, "
                    + "application/vnd.ms-powerpoint, "
                    + "application/msword, */*");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("14", response.getContentAsString());
    }

    public void testWebKitAcceptHeader() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/countaccepttypes",
                                      "application/xml," + "application/xhtml+xml,"
                                          + "text/html;q=0.9,"
                                          + "text/plain;q=0.8,"
                                          + "image/png,*/*;q=0.5");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("6", response.getContentAsString());
    }
}
