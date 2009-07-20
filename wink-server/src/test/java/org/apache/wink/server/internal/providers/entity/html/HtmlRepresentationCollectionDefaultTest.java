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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.model.synd.SyndCategory;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.common.model.synd.SyndLink;
import org.apache.wink.common.model.synd.SyndPerson;
import org.apache.wink.common.model.synd.SyndText;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test default Html Representation for collection resource.
 */
public class HtmlRepresentationCollectionDefaultTest extends HtmlMockServletInvocationTest {

    private static Date             CURRENT_DATE        = new Date();
    private static SimpleDateFormat simpleDateFormat    = new SimpleDateFormat("dd/MM/yyyy");       //$NON-NLS-1$
    private static final String     COL_ID              = "10";
    private static final String     COL_TITLE           = "Collection";
    private static final String     COL_OWNER           = "Tali Col";
    private static final String     COL_SUB_TITLE       = "sub title of Collection";
    private static final String     COL_CATEGORY_SCHEME = "urn:com:hp:categories:collection:scheme";
    private static final String     COL_CATEGORY_TERM   = "low";
    private static final String     COL_LINK_REL        = "urn:com:hp:links:collection:rel";
    private static final String     COL_LINK_TYPE       = MediaType.TEXT_HTML;
    private static final String     COL_LINK_HREF       = "www.google.com";

    @Path("/defectsDefault")
    public static class DefectsDefaultResource {

        @GET
        @Produces(MediaType.TEXT_HTML)
        public Object getSomeDefects(@Context HttpServletResponse httpServletResponse,
                                     @Context HttpServletRequest httpServletRequest) {

            return new HtmlDescriptor(createSyndFeed());
        }
    } // class DefectsDefaultResource

    /**
     * The method invokes the Resource and check the response.
     * 
     * @throws IOException
     */
    @Test
    public void testGetCollectionHtmlDefault() throws Exception {
        MockHttpServletResponse response =
            invoke(constructMockRequest("GET", "/defectsDefault", MediaType.TEXT_HTML));
        assertEquals("HTTP status", 200, response.getStatus());
        String content = response.getContentAsString();
        assertEquals("body", HtmlConstants.DEFAULT_JSP_COLLECTION_PATH, content);
    }

    /**
     * The method tests the CollectionHtmlDefaultAdapter that is used for
     * default case.
     */
    @Test
    public void testCollectionHtmlDefaultAdapter() {
        SyndFeed syndFeed = createSyndFeed();
        HtmlSyndFeedAdapter collectionAdapter = new HtmlSyndFeedAdapter(syndFeed);
        assertEquals("id", COL_ID, collectionAdapter.getId());
        assertEquals("updated", simpleDateFormat.format(CURRENT_DATE), collectionAdapter
            .getUpdated());
        assertEquals("title", COL_TITLE, collectionAdapter.getTitle());
        assertEquals("link rel", COL_LINK_REL, collectionAdapter.getLinks().get(0).getRel());
        assertEquals("link type", COL_LINK_TYPE, collectionAdapter.getLinks().get(0).getType());
        assertEquals("link href", COL_LINK_HREF, collectionAdapter.getLinks().get(0).getHref());
        assertEquals("categories scheme", COL_CATEGORY_SCHEME, collectionAdapter.getCategories()
            .get(0).getScheme());
        assertEquals("categories term", COL_CATEGORY_TERM, collectionAdapter.getCategories().get(0)
            .getTerm());
        assertEquals("owner", COL_OWNER, collectionAdapter.getAuthor());
        assertEquals("summary", COL_SUB_TITLE, collectionAdapter.getSubTitle());
        // assertEquals("id", ENTRY_NAME, new HtmlSyndEntryAdapter(
        // collectionAdapter.getEntryResource(0)).getId());
    }

    /**
     * The method creates CollectionResource with dummy data.
     * 
     * @return CollectionResource<Object>
     */
    public static SyndFeed createSyndFeed() {
        SyndFeed feed = new SyndFeed();
        feed.setId(COL_ID);
        feed.setUpdated(CURRENT_DATE);
        feed.setTitle(new SyndText(COL_TITLE));
        SyndLink syndLink = new SyndLink();
        syndLink.setHref(COL_LINK_HREF);
        syndLink.setType(COL_LINK_TYPE);
        syndLink.setRel(COL_LINK_REL);
        feed.addLink(syndLink);
        SyndCategory category = new SyndCategory();
        category.setScheme(COL_CATEGORY_SCHEME);
        category.setTerm(COL_CATEGORY_TERM);
        feed.addCategory(category);
        SyndPerson person = new SyndPerson();
        person.setName(COL_OWNER);
        feed.addAuthor(person);
        feed.setSubtitle(new SyndText(COL_SUB_TITLE));
        return feed;
    }

}
