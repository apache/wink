/*******************************************************************************
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *     
 *      http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 *******************************************************************************/
package org.apache.wink.server.internal.handlers;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public class PopulateResponseMediaTypeHandlerTest extends MockServletInvocationTest {


    @Path("/")
    public static class MyResource {

        @GET
        @Path("/hellotext")
        @Produces(MediaType.TEXT_PLAIN)
        public String getText() {
            return "hello text";
        }

        @GET
        @Path("/hello")
        public String get() {
            return "hello";
        }
    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyResource.class};
    }

    public void testAcceptableContentType() throws Exception {
        MockHttpServletResponse response = invoke(MediaType.TEXT_PLAIN);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
    }

    public void testNoAcceptableContentType() throws Exception {
        MockHttpServletResponse response = invoke(MediaType.APPLICATION_JSON);
        assertEquals(406, response.getStatus());
    }

    public void testFindBestContentType() throws Exception {
        StringBuilder acceptHeader = new StringBuilder();
        acceptHeader.append(MediaType.APPLICATION_JSON).append(","); // 1.0 quality (implicit)
        acceptHeader.append(MediaType.APPLICATION_XML).append(";q=0.8").append(",");
        acceptHeader.append(MediaType.TEXT_PLAIN).append(";q=0.5");

        MockHttpServletResponse response = invoke(acceptHeader.toString());
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
    }

    public void testSkipGeneralContentType() throws Exception {
        StringBuilder acceptHeader = new StringBuilder();
        acceptHeader.append(MediaType.WILDCARD).append(";q=0.1").append(",");
        acceptHeader.append(MediaType.TEXT_PLAIN).append(";q=0.5");

        MockHttpServletResponse response = invoke(acceptHeader.toString());
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
    }

    public void testMultipleSameQuality() throws Exception {
        StringBuilder acceptHeader = new StringBuilder();
        acceptHeader.append(MediaType.APPLICATION_JSON).append(";q=0.5").append(",");
        acceptHeader.append(MediaType.TEXT_PLAIN).append(";q=0.5");

        MockHttpServletResponse response = invoke(acceptHeader.toString());
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
    }

    public void testMalformedAccept() throws Exception {
        assertEquals(400, invoke("text\nplain").getStatus());
        assertEquals(400, invoke("sure").getStatus());
        assertEquals(400, invoke("text/plain,text/xml,text////").getStatus());
        assertEquals(400, invoke("text/plain,text/xml;q=yes").getStatus());
        assertEquals(400, invoke("text/plain,text/xml;q=Inf").getStatus());
//        assertEquals(400, invoke("text/plain,text/xml;q=NaN").getStatus()); // TODO investigate (returns 200)
        assertEquals(400, invoke("text/plain,text/xml;q=33").getStatus());
    }

    public void testWildcardSubtype() throws Exception {
        StringBuilder acceptHeader = new StringBuilder();
        acceptHeader.append("text/*").append(";q=0.5");

        MockHttpServletResponse response = invoke(acceptHeader.toString());
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
    }

    public void testSkipWildcardSubtype() throws Exception {
        StringBuilder acceptHeader = new StringBuilder();
        acceptHeader.append("text/*").append(";q=0.3").append(",");
        acceptHeader.append(MediaType.TEXT_HTML).append(";q=0.3").append(",");
        acceptHeader.append(MediaType.TEXT_PLAIN).append(";q=0.1");

        MockHttpServletResponse response = invoke(acceptHeader.toString());
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
    }

    public void testNoProduces() throws Exception {
        MockHttpServletResponse response = pathInvoke("/hello", MediaType.TEXT_PLAIN);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getContentType());
    }

    private MockHttpServletResponse invoke(String acceptHeader) throws Exception {
        MockHttpServletRequest mockRequest =
                MockRequestConstructor.constructMockRequest("GET", "/hellotext", acceptHeader);
        return invoke(mockRequest);
    }

    private MockHttpServletResponse pathInvoke(String path, String acceptHeader) throws Exception {
        MockHttpServletRequest mockRequest =
                MockRequestConstructor.constructMockRequest("GET", path, acceptHeader);
        return invoke(mockRequest);
    }
}
