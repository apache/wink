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
import org.apache.wink.common.model.synd.SyndContent;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndLink;
import org.apache.wink.common.model.synd.SyndPerson;
import org.apache.wink.common.model.synd.SyndText;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test default Html Representation for entry resource.
 */
public class HtmlRepresentationEntryDefaultTest extends HtmlMockServletInvocationTest {

    private static Date             CURRENT_DATE          = new Date();
    private static String           ID                    = "1";
    private static String           TITLE                 = "Entry 1";
    private static String           OWNER                 = "Tali";
    private static String           SUMMARY               = "summary for Entry 1";
    private static SimpleDateFormat simpleDateFormat      = new SimpleDateFormat("dd/MM/yyyy");                                                                                //$NON-NLS-1$
    private static final String     CATEGORY_SCHEME       = "urn:com:hp:categories:scheme";
    private static final String     CATEGORY_TERM         = "high";
    private static final String     LINK_REL              = "urn:com:hp:links:rel";
    private static final String     LINK_TYPE             = MediaType.APPLICATION_ATOM_XML;
    private static final String     LINK_HREF             = "www.google.com";
    private static final String     CONTENT_TEXT          = "<b>This is a test</b>";

    private static final String     HTML_HEADER_EXPANDED  =
                                                              "<script type='text/javascript' >\nvar collapseExpand1 = new CollapseExpand('1');\n</script>\n<table class='wide-table' margin-top='0' cellpadding='0' cellspacing='0'>\n<tr>\n<td >\n<table class='wide-table expandable-form-header'>\n<tr>\n<td style='{CURSOR: pointer};' class='portlet-expand-button minimum-icon-size'><div id='" + ID
                                                                  + "_div' onclick=\"collapseExpand"
                                                                  + ID
                                                                  + ".collapseExpand();\">-</div></td>\n<td>&nbsp;&nbsp;"
                                                                  + TITLE
                                                                  + "</td>\n</tr></table>\n</td>\n</tr>\n</table>\n<table id='"
                                                                  + ID
                                                                  + "' style='display: block' class='wide-table' cellpadding=0 cellspacing=0>\n<td class='form-area-width'>\n";
    private static final String     HTML_HEADER_COLLAPSED =
                                                              "<script type='text/javascript' >\nvar collapseExpand1 = new CollapseExpand('1');\n</script>\n<table class='wide-table' margin-top='0' cellpadding='0' cellspacing='0'>\n<tr>\n<td >\n<table class='wide-table expandable-form-header'>\n<tr>\n<td style='{CURSOR: pointer};' class='portlet-expand-button minimum-icon-size'><div id='" + ID
                                                                  + "_div' onclick=\"collapseExpand"
                                                                  + ID
                                                                  + ".collapseExpand();\">+</div></td>\n<td>&nbsp;&nbsp;"
                                                                  + TITLE
                                                                  + "</td>\n</tr></table>\n</td>\n</tr>\n</table>\n<table id='"
                                                                  + ID
                                                                  + "' style='display: none' class='wide-table' cellpadding=0 cellspacing=0>\n<td class='form-area-width'>\n";
    private static final String     HTML_FOOTER_COLLAPSED =
                                                              "</td>\n</table>\n<script type='text/javascript' >\ncollapseExpand" + ID
                                                                  + ".collapse();\n</script>\n";
    private static final String     HTML_FOOTER_EXPANDED  = "</td>\n</table>\n";

    @Path("/defectsDefault/htmlDefect")
    public static class DefectDefaultResource {

        @GET
        @Produces(MediaType.TEXT_HTML)
        public Object getSomeDefect(@Context HttpServletResponse httpServletResponse,
                                    @Context HttpServletRequest httpServletRequest) {
            // HtmlEntryResource resource = new
            // HtmlEntryResource(httpServletRequest,
            // httpServletResponse);
            //
            // return resource;
            return new HtmlDescriptor(createSyndEntry());
        }
    }// class defectDefaultResource

    /**
     * The method invokes the Resource and check the response.
     * 
     * @throws IOException
     */
    public void testGetEntryHtmlDefault() throws Exception {
        MockHttpServletResponse response =
            invoke(constructMockRequest("GET", "/defectsDefault/htmlDefect", MediaType.TEXT_HTML));
        assertEquals("HTTP status", 200, response.getStatus());
        String content = response.getContentAsString();
        assertEquals("body", HtmlConstants.DEFAULT_JSP_ENTRY_PATH, content);
    }

    /**
     * The method tests the HtmlRepresentationEntryAdapter that is used for
     * default case.
     */

    public void HtmlSyndEntryAdapter() {
        HtmlSyndEntryAdapter entryAdapter = new HtmlSyndEntryAdapter(createSyndEntry());
        assertEquals("id", ID, entryAdapter.getId());
        assertEquals("title", TITLE, entryAdapter.getTitle());
        assertEquals("owner", OWNER, entryAdapter.getAuthor());
        assertEquals("summary", SUMMARY, entryAdapter.getSummary());
        assertEquals("updated", simpleDateFormat.format(CURRENT_DATE), entryAdapter.getUpdated());
        assertEquals("published", simpleDateFormat.format(CURRENT_DATE), entryAdapter
            .getPublished());
        assertEquals("categories scheme", CATEGORY_SCHEME, entryAdapter.getCategories().get(0)
            .getScheme());
        assertEquals("categories term", CATEGORY_TERM, entryAdapter.getCategories().get(0)
            .getTerm());
        assertEquals("link rel", LINK_REL, entryAdapter.getLinks().get(0).getRel());
        assertEquals("link type", LINK_TYPE, entryAdapter.getLinks().get(0).getType());
        assertEquals("link href", LINK_HREF, entryAdapter.getLinks().get(0).getHref());
        try {
            assertEquals("content", CONTENT_TEXT, entryAdapter.getContent());
        } catch (IOException e) {
            e.printStackTrace();
            assertFalse(true);
        }
    }

    /**
     * The method checks the HTML header that is returned from
     * ExpandableSectionHelper.
     */

    public void testHtmlHeaderSection() {
        assertEquals(HTML_HEADER_EXPANDED, ExpandableSectionHelper.getFormHeaderHtml(TITLE,
                                                                                     ID,
                                                                                     false));
        assertEquals(HTML_HEADER_COLLAPSED, ExpandableSectionHelper.getFormHeaderHtml(TITLE,
                                                                                      ID,
                                                                                      true));
    }

    /**
     * The method checks the HTML footer that is returned from
     * ExpandableSectionHelper.
     */

    public void testHtmlFooterSection() {
        assertEquals(HTML_FOOTER_COLLAPSED, ExpandableSectionHelper.getFormFooterHtml(ID, true));
        assertEquals(HTML_FOOTER_EXPANDED, ExpandableSectionHelper.getFormFooterHtml(ID, false));
    }

    /**
     * The method creates DocumentResource with dummy data.
     * 
     * @return DocumentResource<Object>
     */
    public static SyndEntry createSyndEntry() {
        SyndEntry syndEntry = new SyndEntry();
        syndEntry.setTitle(new SyndText(TITLE));
        syndEntry.setId(ID);
        syndEntry.setUpdated(CURRENT_DATE);
        syndEntry.setPublished(CURRENT_DATE);
        SyndPerson person = new SyndPerson();
        person.setName(OWNER);
        syndEntry.addAuthor(person);
        syndEntry.setSummary(new SyndText(SUMMARY));
        SyndCategory syndCategory = new SyndCategory();
        syndCategory.setScheme(CATEGORY_SCHEME);
        syndCategory.setTerm(CATEGORY_TERM);
        syndEntry.addCategory(syndCategory);
        SyndLink syndLink = new SyndLink();
        syndLink.setType(LINK_TYPE);
        syndLink.setRel(LINK_REL);
        syndLink.setHref(LINK_HREF);
        syndEntry.addLink(syndLink);
        SyndContent content = new SyndContent();
        content.setValue(CONTENT_TEXT);
        syndEntry.setContent(content);

        return syndEntry;
    }
}
