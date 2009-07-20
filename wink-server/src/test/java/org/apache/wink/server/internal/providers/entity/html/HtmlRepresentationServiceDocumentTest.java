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

package org.apache.wink.server.internal.providers.entity.html;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.RestException;
import org.apache.wink.common.annotations.Workspace;
import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test Service Document in Html Representation.
 */
// please note - the method doesn't need to extend the
// HtmlMockServletInvocationTest
// since the response in this case will not be type of
// OutputStreamHttpServletResponseWrapper.
public class HtmlRepresentationServiceDocumentTest extends HtmlMockServletInvocationTest {

    /**
     * This class represents Resource that has HTML representation.
     */
    @Workspace(workspaceTitle = "Check HTML", collectionTitle = "HTML links")
    @Path("/htmlExist")
    public static class ExistHtmlResource {

        @GET
        @Produces(MediaType.TEXT_HTML)
        public Object getHtmlExists() {
            return null;
        }
    } // class ExistHtmlResource

    /**
     * This class represents Resource that doesn't have HTML representation.
     */
    @Workspace(workspaceTitle = "Check HTML", collectionTitle = "HTML no links")
    @Path("/htmlNotExist")
    public static class NoHtmlResource {

        @GET
        @Produces(MediaType.APPLICATION_ATOM_XML)
        public Object getNoHtml() {
            return null;
        }
    } // class NoHtmlResource

    /**
     * The method invokes the Resource for Service Document in HTML and checks
     * the response.
     * 
     * @param relPath The path to invoke
     * @throws IOException
     */
    public void executeTestServiceDocumentHtml(String relPath) throws Exception {
        try {
            MockHttpServletResponse response =
                invoke(MockRequestConstructor.constructMockRequest("GET",
                                                                   relPath,
                                                                   MediaType.TEXT_HTML));
            assertEquals("HTTP status", 200, response.getStatus());
            // check that the content contains at least one link with HTML
            String content = response.getContentAsString();
            assertTrue("link includes HTML", content.indexOf(UriEncoder
                .encodeString(MediaType.TEXT_HTML)) > 0);

        } catch (RestException e) {
            assertEquals("exception message", "The file requested cannot be retrieved.", e.getMessage()); //$NON-NLS-1$
        }
    }

    /**
     * The method invokes the tests.
     * 
     * @throws IOException
     */

    public void testServiceDocumentHtml() throws Exception {
        executeTestServiceDocumentHtml("");
        executeTestServiceDocumentHtml("/");
    }

}
