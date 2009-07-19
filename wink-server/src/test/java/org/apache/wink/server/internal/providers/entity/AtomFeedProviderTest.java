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

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


public class AtomFeedProviderTest extends MockServletInvocationTest {
    
    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[]{TestResource.class};
    }

    @Path("test")
    public static class TestResource {
        
        @GET
        @Path("atomfeed")
        @Produces("application/atom+xml")
        public AtomFeed getAtomFeed() throws IOException {
            AtomFeed feed = AtomFeed.unmarshal(new StringReader(FEED));
            return feed;
        }
        
        @GET
        @Path("atomfeedelement")
        @Produces("application/atom+xml")
        public JAXBElement<AtomFeed> getAtomFeedElement() throws IOException {
            AtomFeed feed = AtomFeed.unmarshal(new StringReader(FEED));
            org.apache.wink.common.model.atom.ObjectFactory of = new org.apache.wink.common.model.atom.ObjectFactory();
            return of.createFeed(feed);
        }

        @GET
        @Path("atomsyndfeed")
        @Produces("application/atom+xml")
        public SyndFeed getSyndFeed() throws IOException {
            AtomFeed feed = AtomFeed.unmarshal(new StringReader(FEED));
            return feed.toSynd(new SyndFeed());
        }
        
        @POST
        @Path("atomfeed")
        @Produces("application/atom+xml")
        @Consumes("application/atom+xml")
        public AtomFeed postAtomFeed(AtomFeed feed) {
            return feed;
        }

        @POST
        @Path("atomfeedelement")
        @Produces("application/atom+xml")
        @Consumes("application/atom+xml")
        public JAXBElement<AtomFeed> postAtomFeedElement(JAXBElement<AtomFeed> feed) {
            return feed;
        }

        @POST
        @Path("atomsyndfeed")
        @Produces("application/atom+xml")
        @Consumes("application/atom+xml")
        public SyndFeed postAtomSyndFeed(SyndFeed feed) {
            return feed;
        }

    }

    public void testGetAtomFeed() throws Exception {
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "/test/atomfeed", "application/atom+xml");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(FEED, response.getContentAsString());
    }
    
    public void testGetAtomFeedElement() throws Exception {
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "/test/atomfeedelement", "application/atom+xml");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(FEED, response.getContentAsString());
    }

    public void testGetAtomSyndFeed() throws Exception {
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "/test/atomsyndfeed", "application/atom+xml");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(FEED, response.getContentAsString());
    }
    
    public void testPostAtomFeed() throws Exception {
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("POST", "/test/atomfeed", "application/atom+xml");
        request.setContentType("application/atom+xml");
        request.setContent(FEED.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(FEED, response.getContentAsString());
    }

    public void testPostAtomFeedElement() throws Exception {
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("POST", "/test/atomfeedelement", "application/atom+xml");
        request.setContentType("application/atom+xml");
        request.setContent(FEED.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(FEED, response.getContentAsString());
    }

    public void testPostAtomSyndFeed() throws Exception {
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("POST", "/test/atomsyndfeed", "application/atom+xml");
        request.setContentType("application/atom+xml");
        request.setContent(FEED.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(FEED, response.getContentAsString());
    }

    private static final String FEED_STR =
    		"<feed xml:base=\"http://b216:8080/reporting/reports\" xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + 
    		"    <id>urn:systinet2:reporting:collection:reportdefinition</id>\n" + 
    		"    <updated>@TIME@</updated>\n" + 
    		"    <title type=\"text\" xml:lang=\"en\">Report Definitions Collection</title>\n" + 
    		"    <subtitle type=\"text\" xml:lang=\"en\">Collection of report definitions. Report definition is a XML document describing how to build the report. It describes data sources, data sets, business logic and rendering and report parameters. Report definitions may also use libraries.</subtitle>\n" + 
            "    <opensearch:itemsPerPage>1</opensearch:itemsPerPage>\n" + 
            "    <opensearch:startIndex>0</opensearch:startIndex>\n" + 
            "    <opensearch:totalResults>32</opensearch:totalResults>\n" + 
    		"    <link href=\"http://b216:8080/reporting/reports/?start-index=0&amp;max-results=30&amp;alt=text/plain\" type=\"text/plain\" rel=\"first\"/>\n" + 
    		"    <link href=\"http://b216:8080/reporting/reports?alt=application/json\" type=\"application/json\" rel=\"alternate\"/>\n" + 
    		"    <link href=\"http://b216:8080/reporting/reports\" rel=\"self\"/>\n" + 
    		"    <link href=\"http://b216:8080/reporting/reports?alt=text/xml\" type=\"text/xml\" rel=\"alternate\"/>\n" + 
    		"    <link href=\"http://b216:8080/reporting/reports\" type=\"application/atom+xml\" rel=\"edit\"/>\n" + 
    		"    <author>\n" + 
    		"        <name>admin</name>\n" + 
    		"    </author>\n" + 
    		"    <category label=\"report definitions\" scheme=\"urn:com:systinet:reporting:kind\" term=\"urn:com:systinet:reporting:kind:definitions:collection\"/>\n" + 
    		"    <entry>\n" + 
    		"        <id>toptenvalidators</id>\n" + 
    		"        <updated>@TIME@</updated>\n" + 
    		"        <title type=\"text\" xml:lang=\"en\">top ten validators</title>\n" + 
    		"        <published>@TIME@</published>\n" + 
    		"        <link href=\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/json\" type=\"application/json\" rel=\"alternate\"/>\n" + 
            "        <link href=\"http://b216:8080/reporting/reports/toptenvalidators?alt=text/plain\" type=\"text/plain\" rel=\"alternate\"/>\n" + 
    		"        <link href=\"http://b216:8080/reporting/reports/toptenvalidators\" rel=\"self\"/>\n" + 
    		"        <link href=\"http://b216:8080/reporting/reports/toptenvalidators?alt=text/xml\" type=\"text/xml\" rel=\"alternate\"/>\n" + 
    		"        <link href=\"http://b216:8080/reporting/reports/toptenvalidators/documents/\" type=\"application/atom+xml\" rel=\"execute\"/>\n" + 
    		"        <link href=\"http://b216:8080/reporting/reports/toptenvalidators\" type=\"application/atom+xml\" rel=\"edit\"/>\n" + 
    		"        <link href=\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/xml\" type=\"application/xml\" rel=\"alternate\"/>\n" + 
    		"        <link href=\"http://b216:8080/reporting/reports/toptenvalidators\" type=\"application/xml\" rel=\"edit-media\"/>\n" + 
    		"        <author>\n" + 
    		"            <name>admin</name>\n" + 
    		"        </author>\n" + 
    		"        <category label=\"report definition\" scheme=\"urn:com:systinet:reporting:kind\" term=\"urn:com:systinet:reporting:kind:definition\"/>\n" + 
    		"        <category label=\"Policy Manager - Homepage Report\" scheme=\"urn:com:systinet:policymgr:report:type\" term=\"aoi\"/>\n" + 
    		"    </entry>\n" + 
    		"</feed>\n";
    
    private static final String FEED;
    
    static {
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis((new Date()).getTime());
            XMLGregorianCalendar xmlGregCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
            FEED = FEED_STR.replace("@TIME@", xmlGregCal.toXMLFormat());
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
