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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test of Bug #39614: LinkProcessor#convertAllLinksToExpectedForm does not
 * reflect '/' at the end of request URI.
 */
public class SlashEndingUriTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {UriResoure.class, UriRootResource.class};
    }

    @Override
    protected String getPropertiesFile() {
        return "configuration.properties";
    }

    @Path("service/hello/{collection}/subcollection")
    public static class UriResoure {

        @GET
        @Produces("plain/text")
        public String getFile(@Context UriInfo uriInfo) {
            return uriInfo.getRequestUri().toString();
        }

    } // class

    @Path("/")
    public static class UriRootResource {

        @GET
        @Produces("plain/text")
        public String getFile(@Context UriInfo uriInfo) {
            return uriInfo.getRequestUri().toString();
        }

    } //

    public void testFinalSlash() throws Exception {
        final String withSlash = "service/hello/collection/subcollection/";
        final String withoutSlash = withSlash.substring(0, withSlash.length() - 1);

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", withSlash, "*/*");
        MockHttpServletResponse response = invoke(request);
        assertEquals("should end with slash", "http://localhost:8080/rest/" + withSlash, response
            .getContentAsString());

        MockHttpServletRequest request2 =
            MockRequestConstructor.constructMockRequest("GET", withoutSlash, "*/*");
        MockHttpServletResponse response2 = invoke(request2);
        assertEquals("must not end with slash",
                     "http://localhost:8080/rest/" + withoutSlash,
                     response2.getContentAsString());
    }

    public void testSeveralSlashes() throws Exception {
        final String withSlash = "service/hello/collection/subcollection/";
        final String withSlash2 = withSlash + "/";
        final String withSlash3 = withSlash2 + "/";
        final String withSlash4 = withSlash3 + "/";

        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", withSlash2, "*/*");

        final String requestUriWithSlash = "http://localhost:8080/rest/" + withSlash;

        MockHttpServletResponse response = invoke(request);
        assertEquals("should end with slash", requestUriWithSlash, response.getContentAsString());

        request = MockRequestConstructor.constructMockRequest("GET", withSlash3, "*/*");
        response = invoke(request);
        assertEquals("should end with slash", requestUriWithSlash, response.getContentAsString());

        request = MockRequestConstructor.constructMockRequest("GET", withSlash4, "*/*");
        response = invoke(request);
        assertEquals("should end with slash", requestUriWithSlash, response.getContentAsString());
    }

    public void testRootUri() throws Exception {
        final String requestUri = "http://localhost:8080/rest/";

        MockHttpServletRequest request = null;
        MockHttpServletResponse response = null;

        request = MockRequestConstructor.constructMockRequest("GET", "", "*/*");
        response = invoke(request);
        assertEquals("should end with slash", requestUri, response.getContentAsString());

        request = MockRequestConstructor.constructMockRequest("GET", "/", "*/*");
        response = invoke(request);
        assertEquals("should end with slash", requestUri, response.getContentAsString());

        request = MockRequestConstructor.constructMockRequest("GET", "//", "*/*");
        response = invoke(request);
        assertEquals("should end with slash", requestUri, response.getContentAsString());

        request = MockRequestConstructor.constructMockRequest("GET", "///", "*/*");
        response = invoke(request);
        assertEquals("should end with slash", requestUri, response.getContentAsString());

        request = MockRequestConstructor.constructMockRequest("GET", "////", "*/*");
        response = invoke(request);
        assertEquals("should end with slash", requestUri, response.getContentAsString());
    }

}
