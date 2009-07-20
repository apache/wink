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

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test of dispatch process with Resource using Response.
 */
public class ResponseDispatchTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {ResponseResource.class};
    }

    @Path("a")
    public static class ResponseResource {

        @PUT
        @Consumes("*/*")
        @Produces(MediaType.APPLICATION_ATOM_XML)
        public Response put() throws URISyntaxException {
            Response response =
                Response.created(new URI("unknown")).entity(new SyndEntry()).build();
            return response;
        }

    } // class ResponseResource

    public void testPut() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("PUT", "a", "*/*");
        servletRequest.setContentType("text/xml");
        MockHttpServletResponse response = invoke(servletRequest);
        assertEquals("status", 201, response.getStatus());
        assertEquals("content", "<entry xmlns=\"http://www.w3.org/2005/Atom\"/>", response
            .getContentAsString().trim());
    }

}
