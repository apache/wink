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

import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test for defect #40482: HEAD request on entry returns response 405
 * Method Not Allowed.
 */
public class HeadTest extends MockServletInvocationTest {

    public static final String CONTENT = "Some cOnTeNt";
    public static final String PATH    = "path";

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {Resource.class};
    }

    @Path(PATH)
    public static class Resource {

        @GET
        @Produces("plain/text")
        public String getFile() {
            return CONTENT;
        }

    }

    public void testHead() throws Exception {
        MockHttpServletRequest getRequest =
            MockRequestConstructor.constructMockRequest("GET", PATH, "*/*");
        MockHttpServletResponse getResponse = invoke(getRequest);
        assertEquals("get OK", HttpStatus.OK.getCode(), getResponse.getStatus());
        assertEquals("get content", CONTENT, getResponse.getContentAsString());

        MockHttpServletRequest headRequest =
            MockRequestConstructor.constructMockRequest("HEAD", PATH, "*/*");
        MockHttpServletResponse headResponse = invoke(headRequest);
        assertEquals("head OK", HttpStatus.OK.getCode(), headResponse.getStatus());

        assertEquals("content-type", getResponse.getContentType(), headResponse.getContentType());
        assertEquals("content-length", CONTENT.length(), headResponse.getContentLength());
        assertEquals("head content", "", headResponse.getContentAsString());
    }

}
