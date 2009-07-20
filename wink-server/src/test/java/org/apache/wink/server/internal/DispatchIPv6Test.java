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

import java.io.StringReader;
import java.net.URI;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.annotations.Workspace;
import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.app.AppCollection;
import org.apache.wink.common.model.app.AppService;
import org.apache.wink.common.model.app.AppWorkspace;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.synd.SyndContent;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.server.utils.LinkBuilders;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test for dispatching URI with host in IPv6
 * <p/>
 * <em>Note: </em>Initiated by bug
 * <tt>#38790: URIs with IPv6 hosts - audit and testing</tt>
 */
public class DispatchIPv6Test extends MockServletInvocationTest {

    String http  = "http://[2001:0db8:85a3:08d3:1319:8a2e:0370:7344]:8080/";
    String https = "https://[2001:0db8:85a3:08d3:1319:8a2e:0370:7344]:8443/";

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {DocumentResource.class, CollectionResource.class};
    }

    @Override
    protected String getPropertiesFile() {
        String name = getClass().getName();
        String fileName = TestUtils.packageToPath(name) + ".properties";
        return fileName;
    }

    @Path("/get/entry")
    static public class DocumentResource {

        @GET
        @Produces(MediaType.APPLICATION_ATOM_XML)
        public SyndEntry getDocument(@Context LinkBuilders linkBuilders, @Context UriInfo uriInfo) {
            SyndEntry entry = new SyndEntry("Test", "test:1", new Date());
            String baseUri = uriInfo.getBaseUri().toString();
            entry.setBase(baseUri);
            String path = uriInfo.getPath();
            entry.setContent(new SyndContent(path + "/csv", "text/csv", true));
            linkBuilders.createSystemLinksBuilder().relativize(false).build(entry.getLinks());
            return entry;
        }
    }

    @Path("/get/feed")
    @Workspace(workspaceTitle = "Test Service", collectionTitle = "Feed")
    static public class CollectionResource {

        @GET
        @Produces(MediaType.APPLICATION_ATOM_XML)
        public SyndFeed getCollection(@Context LinkBuilders linkBuilders, @Context UriInfo uriInfo) {
            SyndFeed feed = new SyndFeed(new SyndText("Test"), "test:1", new Date());
            feed.setBase(uriInfo.getBaseUri().toString());

            SyndEntry entry = new SyndEntry(new SyndText("Test"), "test:1", new Date());
            URI requestUri = uriInfo.getAbsolutePath();
            entry.setContent(new SyndContent(requestUri + "/csv", "text/csv", true));
            feed.addEntry(entry);

            linkBuilders.createSystemLinksBuilder().relativize(false).build(feed.getLinks());
            linkBuilders.createSystemLinksBuilder().relativize(false)
                .resource(DocumentResource.class).build(entry.getLinks());

            return feed;
        }
    }

    public void testIPv6HttpServiceDocument() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/",
                                                        MediaTypeUtils.ATOM_SERVICE_DOCUMENT_TYPE);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", HttpStatus.OK.getCode(), response.getStatus());
        String contentString = response.getContentAsString();
        AppService service = AppService.unmarshal(new StringReader(contentString));
        AppWorkspace workspace = service.getWorkspace().get(0);
        AppCollection collection = workspace.getCollection().get(0);
        String href = collection.getHref();
        assertTrue("ipv6 URI: " + href, href.startsWith(http));
    }

    public void testIPv6HttpsServiceDocument() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/",
                                                        MediaTypeUtils.ATOM_SERVICE_DOCUMENT_TYPE);
        request.setSecure(true);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", HttpStatus.OK.getCode(), response.getStatus());
        String contentString = response.getContentAsString();
        AppService service = AppService.unmarshal(new StringReader(contentString));
        AppWorkspace workspace = service.getWorkspace().get(0);
        AppCollection collection = workspace.getCollection().get(0);
        String href = collection.getHref();
        assertTrue("ipv6 URI: " + href, href.startsWith(https));
    }

    public void testIPv6HttpEntry() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/get/entry",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        request.addParameter(RestConstants.REST_PARAM_ABSOLUTE_URLS, "");
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", HttpStatus.OK.getCode(), response.getStatus());
        String contentString = response.getContentAsString();
        AtomEntry entry = AtomEntry.unmarshal(new StringReader(contentString));
        AtomLink link = entry.getLinks().get(0);
        assertTrue("ipv6 URI: " + link, link.getHref().startsWith(http));
    }

    public void testIPv6HttpsEntry() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/get/entry",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        request.setSecure(true);
        request.addParameter(RestConstants.REST_PARAM_ABSOLUTE_URLS, "");
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", HttpStatus.OK.getCode(), response.getStatus());
        String contentString = response.getContentAsString();
        AtomEntry entry = AtomEntry.unmarshal(new StringReader(contentString));
        AtomLink link = entry.getLinks().get(0);
        assertTrue("ipv6 URI: " + link, link.getHref().startsWith(https));
    }

    public void testIPv6HttpFeed() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/get/feed",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        request.addParameter(RestConstants.REST_PARAM_ABSOLUTE_URLS, "");
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", HttpStatus.OK.getCode(), response.getStatus());
        String contentString = response.getContentAsString();
        AtomFeed feed = AtomFeed.unmarshal(new StringReader(contentString));
        AtomLink link = feed.getLinks().get(0);
        assertTrue("ipv6 URI: " + link, link.getHref().startsWith(http));
    }

    public void testIPv6HttpsFeed() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/get/feed",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        request.setSecure(true);
        request.addParameter(RestConstants.REST_PARAM_ABSOLUTE_URLS, "");
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", HttpStatus.OK.getCode(), response.getStatus());
        String contentString = response.getContentAsString();
        AtomFeed feed = AtomFeed.unmarshal(new StringReader(contentString));
        AtomLink link = feed.getLinks().get(0);
        assertTrue("ipv6 URI: " + link, link.getHref().startsWith(https));
    }
}
