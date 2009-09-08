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

package org.apache.wink.server.internal.providers.entity;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.wink.common.annotations.Asset;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class SyndFeedLocatorProviderTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class};
    }

    private static final SyndFeed SYND_FEED = new SyndFeed(new SyndText("title"), "id");
    private static final String   FEED      =
                                                "<feed xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <id>id</id>\n"
                                                    + "    <title type=\"text\">title</title>\n"
                                                    + "</feed>\n";

    @Asset
    public static class TestAsset {

        SyndFeed synd;

        @Produces("application/atom+xml")
        public SyndFeed getSyndFeed() {
            return synd;
        }

        @Consumes("application/atom+xml")
        public void setSyndFeed(SyndFeed synd) {
            this.synd = synd;
        }
    }

    @Path("test")
    public static class TestResource {

        @GET
        @Produces("application/atom+xml")
        public TestAsset getAsset() {
            TestAsset asset = new TestAsset();
            asset.setSyndFeed(SYND_FEED);
            return asset;
        }

        @POST
        @Produces("application/atom+xml")
        @Consumes("application/atom+xml")
        public TestAsset postAsset(TestAsset asset) {
            assertEquals(SYND_FEED, asset.synd);
            return asset;
        }
    }

    public void testGetAtomFeed() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test", "application/atom+xml");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(FEED, response.getContentAsString());
        assertNull(msg, msg);
    }

    public void testPostAtomFeed() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST", "/test", "application/atom+xml");
        request.setContentType("application/atom+xml");
        request.setContent(FEED.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(FEED, response.getContentAsString());
        assertNull(msg, msg);
    }

}
