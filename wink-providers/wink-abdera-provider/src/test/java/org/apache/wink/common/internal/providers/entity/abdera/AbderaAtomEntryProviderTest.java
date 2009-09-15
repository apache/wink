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
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.providers.json.JSONUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests the Abdera Atom Entry provider.
 */
public class AbderaAtomEntryProviderTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class};
    }

    @Override
    protected Object[] getSingletons() {
        return new Object[] {new AbderaAtomEntryProvider()};
    }

    @Path("test")
    public static class TestResource {

        @GET
        @Path("atomentry")
        @Produces( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
        public Entry getAtomEntry() throws IOException {
            Abdera abdera = new Abdera();
            Entry e = abdera.newEntry();
            e.setId("toptenvalidators");
            e.setTitle("top ten validators");
            e.addLink("http://b216:8080/reporting/reports/toptenvalidators?alt=application/json",
                      "alternate");
            e.addAuthor("admin");
            e.addCategory("urn:com:systinet:reporting:kind",
                          "urn:com:systinet:reporting:kind:definition",
                          "report definition");
            return e;
        }

        @POST
        @Path("atomentry")
        @Produces( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
        @Consumes( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
        public Entry postAtomEntry(Entry e) throws IOException {
            return e;
        }
    }

    @SuppressWarnings("unchecked")
    public void testGetAtomEntryInXML() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/atomentry",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        Unmarshaller u =
            JAXBContext.newInstance(AtomEntry.class.getPackage().getName()).createUnmarshaller();
        AtomEntry entry =
            ((JAXBElement<AtomEntry>)u.unmarshal(new ByteArrayInputStream(response
                .getContentAsByteArray()))).getValue();
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
                                                        "/test/atomentry",
                                                        MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString(), JSONUtils.equals(JSONUtils
            .objectForString(ENTRY_STR_JSON), JSONUtils.objectForString(response
            .getContentAsString())));
    }

    @SuppressWarnings("unchecked")
    public void testPostAtomEntry() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test/atomentry",
                                                        MediaType.APPLICATION_ATOM_XML);
        request.setContentType(MediaType.APPLICATION_ATOM_XML);
        request.setContent(ENTRY_STR.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());

        Unmarshaller u =
            JAXBContext.newInstance(AtomEntry.class.getPackage().getName()).createUnmarshaller();
        AtomEntry entry =
            ((JAXBElement<AtomEntry>)u.unmarshal(new ByteArrayInputStream(response
                .getContentAsByteArray()))).getValue();
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

    private static final String ENTRY_STR      =
                                                   "<entry xml:base=\"http://b216:8080/reporting/reports\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <id>toptenvalidators</id>\n"
                                                       + "    <updated>@TIME@</updated>\n"
                                                       + "    <title>top ten validators</title>\n"
                                                       + "    <link href=\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/json\" rel=\"alternate\"/>\n"
                                                       + "    <author>\n"
                                                       + "        <name>admin</name>\n"
                                                       + "    </author>\n"
                                                       + "    <category label=\"report definition\" scheme=\"urn:com:systinet:reporting:kind\" term=\"urn:com:systinet:reporting:kind:definition\"/>\n"
                                                       + "</entry>\n";

    private static String       ENTRY_STR_JSON =
                                                   "{\"id\":\"toptenvalidators\", \"title\":\"top ten validators\", \"authors\":[{\"name\":\"admin\"}], \"links\":[{\"href\":\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/json\",\"rel\":\"alternate\"}],\"categories\":[{\"term\":\"urn:com:systinet:reporting:kind:definition\",\"scheme\":\"urn:com:systinet:reporting:kind\",\"label\":\"report definition\"}]}";

}
