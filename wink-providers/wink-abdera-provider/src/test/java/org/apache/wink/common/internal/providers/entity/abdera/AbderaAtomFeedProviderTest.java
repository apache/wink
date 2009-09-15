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
package org.apache.wink.common.internal.providers.entity.abdera;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.providers.json.JSONUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests the Abdera Atom Feed provider.
 */
public class AbderaAtomFeedProviderTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class};
    }

    @Override
    protected Object[] getSingletons() {
        return new Object[] {new AbderaAtomFeedProvider()};
    }

    @Path("test")
    public static class TestResource {

        @GET
        @Path("atomfeed")
        @Produces( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
        public Feed getAtomFeed() throws IOException {
            Feed feed = new Abdera().newFeed();
            feed.setTitle("Report Definitions Collection");
            feed
                .setSubtitle("Collection of report definitions. Report definition is a XML document describing how to build the report. It describes data sources, data sets, business logic and rendering and report parameters. Report definitions may also use libraries.");
            feed
                .addLink("http://b216:8080/reporting/reports/?start-index=0&max-results=30&amp;alt=text/plain",
                         "first");
            feed.addLink("http://b216:8080/reporting/reports?alt=application/json", "alternate");
            feed.addLink("http://b216:8080/reporting/reports", "self");
            feed.addAuthor("admin");
            feed.addCategory("urn:com:systinet:reporting:kind",
                             "urn:com:systinet:reporting:kind:definitions:collection",
                             "report definitions");
            Entry e = feed.addEntry();
            e.setId("toptenvalidators");
            e.setTitle("top ten validators");
            e.addLink("http://b216:8080/reporting/reports/toptenvalidators?alt=application/json",
                      "alternate");
            e.addAuthor("admin");
            e.addCategory("urn:com:systinet:reporting:kind",
                          "urn:com:systinet:reporting:kind:definition",
                          "report definition");
            return feed;
        }

        @POST
        @Path("atomfeed")
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        @Consumes( {MediaType.APPLICATION_ATOM_XML})
        public Feed postAtomFeed(Feed feed) {
            return feed;
        }
    }

    @SuppressWarnings("unchecked")
    public void testGetAtomFeedInXML() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/atomfeed",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        Unmarshaller u =
            JAXBContext.newInstance(AtomEntry.class.getPackage().getName()).createUnmarshaller();
        AtomFeed feed =
            ((JAXBElement<AtomFeed>)u.unmarshal(new ByteArrayInputStream(response
                .getContentAsByteArray()))).getValue();
        assertEquals("Report Definitions Collection", feed.getTitle().getValue());
        assertEquals("Collection of report definitions. Report definition is a XML document describing how to build the report. It describes data sources, data sets, business logic and rendering and report parameters. Report definitions may also use libraries.",
                     feed.getSubtitle().getValue());
        assertEquals("http://b216:8080/reporting/reports/?start-index=0&max-results=30&amp;alt=text/plain",
                     feed.getLinks().get(0).getHref());
        assertEquals("first", feed.getLinks().get(0).getRel());
        assertEquals("http://b216:8080/reporting/reports?alt=application/json", feed.getLinks()
            .get(1).getHref());
        assertEquals("alternate", feed.getLinks().get(1).getRel());
        assertEquals("http://b216:8080/reporting/reports", feed.getLinks().get(2).getHref());
        assertEquals("self", feed.getLinks().get(2).getRel());
        assertEquals("admin", feed.getAuthors().get(0).getName());
        assertEquals("urn:com:systinet:reporting:kind", feed.getCategories().get(0).getScheme());
        assertEquals("urn:com:systinet:reporting:kind:definitions:collection", feed.getCategories()
            .get(0).getTerm());
        assertEquals("report definitions", feed.getCategories().get(0).getLabel());

        AtomEntry entry = feed.getEntries().get(0);
        assertEquals("toptenvalidators", entry.getId());
        assertEquals("top ten validators", entry.getTitle().getValue());
        assertEquals("http://b216:8080/reporting/reports/toptenvalidators?alt=application/json",
                     entry.getLinks().get(0).getHref());
        assertEquals("alternate", entry.getLinks().get(0).getRel());
        assertEquals("admin", entry.getAuthors().get(0).getName());
        assertEquals("urn:com:systinet:reporting:kind", entry.getCategories().get(0).getScheme());
        assertEquals("urn:com:systinet:reporting:kind:definition", entry.getCategories().get(0)
            .getTerm());
        assertEquals("report definition", entry.getCategories().get(0).getLabel());
    }

    public void testGetAtomEntryInJSON() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/atomfeed",
                                                        MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        System.out.println(response.getContentAsString());
        assertTrue(response.getContentAsString(), JSONUtils.equals(JSONUtils
            .objectForString(FEED_STR_JSON), JSONUtils.objectForString(response
            .getContentAsString())));
    }

    @SuppressWarnings("unchecked")
    public void testPostAtomEntry() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test/atomfeed",
                                                        MediaType.APPLICATION_ATOM_XML);
        request.setContentType(MediaType.APPLICATION_ATOM_XML);
        request.setContent(FEED_STR.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());

        Unmarshaller u =
            JAXBContext.newInstance(AtomEntry.class.getPackage().getName()).createUnmarshaller();
        AtomFeed feed =
            ((JAXBElement<AtomFeed>)u.unmarshal(new ByteArrayInputStream(response
                .getContentAsByteArray()))).getValue();
        assertEquals("Report Definitions Collection", feed.getTitle().getValue());
        assertEquals("Collection of report definitions. Report definition is a XML document describing how to build the report. It describes data sources, data sets, business logic and rendering and report parameters. Report definitions may also use libraries.",
                     feed.getSubtitle().getValue());
        assertEquals("http://b216:8080/reporting/reports/?start-index=0&max-results=30&alt=text/plain",
                     feed.getLinks().get(0).getHref());
        assertEquals("first", feed.getLinks().get(0).getRel());
        assertEquals("http://b216:8080/reporting/reports?alt=application/json", feed.getLinks()
            .get(1).getHref());
        assertEquals("alternate", feed.getLinks().get(1).getRel());
        assertEquals("http://b216:8080/reporting/reports", feed.getLinks().get(2).getHref());
        assertEquals("self", feed.getLinks().get(2).getRel());
        assertEquals("admin", feed.getAuthors().get(0).getName());
        assertEquals("urn:com:systinet:reporting:kind", feed.getCategories().get(0).getScheme());
        assertEquals("urn:com:systinet:reporting:kind:definitions:collection", feed.getCategories()
            .get(0).getTerm());
        assertEquals("report definitions", feed.getCategories().get(0).getLabel());

        AtomEntry entry = feed.getEntries().get(0);
        assertEquals("toptenvalidators", entry.getId());
        assertEquals("top ten validators", entry.getTitle().getValue());
        assertEquals("http://b216:8080/reporting/reports/toptenvalidators?alt=application/json",
                     entry.getLinks().get(0).getHref());
        assertEquals("alternate", entry.getLinks().get(0).getRel());
        assertEquals("admin", entry.getAuthors().get(0).getName());
        assertEquals("urn:com:systinet:reporting:kind", entry.getCategories().get(0).getScheme());
        assertEquals("urn:com:systinet:reporting:kind:definition", entry.getCategories().get(0)
            .getTerm());
        assertEquals("report definition", entry.getCategories().get(0).getLabel());
    }

    private static final String FEED_STR      =
                                                  "<feed xml:base=\"http://b216:8080/reporting/reports\" xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <id>urn:systinet2:reporting:collection:reportdefinition</id>\n"
                                                      + "    <title type=\"text\" xml:lang=\"en\">Report Definitions Collection</title>\n"
                                                      + "    <subtitle type=\"text\" xml:lang=\"en\">Collection of report definitions. Report definition is a XML document describing how to build the report. It describes data sources, data sets, business logic and rendering and report parameters. Report definitions may also use libraries.</subtitle>\n"
                                                      + "    <opensearch:itemsPerPage>1</opensearch:itemsPerPage>\n"
                                                      + "    <opensearch:startIndex>0</opensearch:startIndex>\n"
                                                      + "    <opensearch:totalResults>32</opensearch:totalResults>\n"
                                                      + "    <link href=\"http://b216:8080/reporting/reports/?start-index=0&amp;max-results=30&amp;alt=text/plain\" type=\"text/plain\" rel=\"first\"/>\n"
                                                      + "    <link href=\"http://b216:8080/reporting/reports?alt=application/json\" type=\"application/json\" rel=\"alternate\"/>\n"
                                                      + "    <link href=\"http://b216:8080/reporting/reports\" rel=\"self\"/>\n"
                                                      + "    <author>\n"
                                                      + "        <name>admin</name>\n"
                                                      + "    </author>\n"
                                                      + "    <category label=\"report definitions\" scheme=\"urn:com:systinet:reporting:kind\" term=\"urn:com:systinet:reporting:kind:definitions:collection\"/>\n"
                                                      + "    <entry>\n"
                                                      + "        <id>toptenvalidators</id>\n"
                                                      + "        <title type=\"text\" xml:lang=\"en\">top ten validators</title>\n"
                                                      + "        <published>@TIME@</published>\n"
                                                      + "        <link href=\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/json\" type=\"application/json\" rel=\"alternate\"/>\n"
                                                      + "        <author>\n"
                                                      + "            <name>admin</name>\n"
                                                      + "        </author>\n"
                                                      + "        <category label=\"report definition\" scheme=\"urn:com:systinet:reporting:kind\" term=\"urn:com:systinet:reporting:kind:definition\"/>\n"
                                                      + "    </entry>\n"
                                                      + "</feed>\n";
    private static String       FEED_STR_JSON =
                                                  "{ \"title\":\"Report Definitions Collection\",\"subtitle\":\"Collection of report definitions. Report definition is a XML document describing how to build the report. It describes data sources, data sets, business logic and rendering and report parameters. Report definitions may also use libraries.\",\"authors\":[{\"name\":\"admin\"}],\"links\":[{\"href\":\"http://b216:8080/reporting/reports/?start-index=0&max-results=30&amp;alt=text/plain\",\"rel\":\"first\"},{\"href\":\"http://b216:8080/reporting/reports?alt=application/json\",\"rel\":\"alternate\"},{\"href\":\"http://b216:8080/reporting/reports\",\"rel\":\"self\"}],\"categories\":[{\"term\":\"urn:com:systinet:reporting:kind:definitions:collection\",\"scheme\":\"urn:com:systinet:reporting:kind\",\"label\":\"report definitions\"}],\"entries\":[{\"id\":\"toptenvalidators\",\"title\":\"top ten validators\",\"authors\":[{\"name\":\"admin\"}],\"links\":[{\"href\":\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/json\",\"rel\":\"alternate\"}],\"categories\":[{\"term\":\"urn:com:systinet:reporting:kind:definition\",\"scheme\":\"urn:com:systinet:reporting:kind\",\"label\":\"report definition\"}]}]}";

}
