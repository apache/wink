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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.RestException;
import org.apache.wink.common.model.synd.SyndFeed;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test customized Html Representation for collection resource.
 */
public class HtmlRepresentationCollectionCustomizedTest extends HtmlMockServletInvocationTest {

    @Path("/defectsCustomized")
    public static class DefectsCustomizedResource {

        @GET
        @Produces(MediaType.TEXT_HTML)
        public Object getSomeDefects(@Context HttpServletResponse httpServletResponse,
                                     @Context HttpServletRequest httpServletRequest) {
            // HtmlCollectionResource resource = new
            // HtmlCollectionResource(CUSTOMIZED_COLLECTION_URL,
            // httpServletRequest, httpServletResponse);
            //
            // return resource;
            return new HtmlDescriptor(new SyndFeed(), CUSTOMIZED_COLLECTION_URL);
        }
    } // class DefectsCustomizedResource

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {DefectsCustomizedResource.class};
    }

    /**
     * The method invokes the Resource and check the response.
     * 
     * @throws IOException
     */
    public void testGetCollectionHtmlCust() throws Exception {
        try {
            MockHttpServletRequest request =
                constructMockRequest("GET", "/defectsCustomized", MediaType.TEXT_HTML);
            MockHttpServletResponse response = invoke(request);
            assertEquals("HTTP status", 200, response.getStatus());
            String content = response.getContentAsString();
            assertEquals("body", CUSTOMIZED_COLLECTION_URL, content);
        } catch (RestException e) {
            assertEquals("exception message",
                         "The file requested cannot be retrieved.", e.getMessage()); //$NON-NLS-1$
        }
    }

}
