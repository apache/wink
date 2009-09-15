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
package org.apache.wink.server.internal.providers.entity.jettison;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.providers.json.JSONUtils;
import org.apache.wink.server.internal.providers.entity.jettison.jaxb.Person;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.codehaus.jettison.mapped.Configuration;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JettisonJAXBMappedTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class, PersonResource.class,
            FormattingOptionsContextResolver.class};
    }

    @Override
    protected Object[] getSingletons() {
        Map<String, String> namespaceMap = new HashMap<String, String>();
        namespaceMap.put("http://www.w3.org/2005/Atom", "http://www.w3.org/2005/Atom");
        namespaceMap.put("http://b216:8080/reporting/reports/toptenvalidators",
                         "http://b216:8080/reporting/reports/toptenvalidators");
        namespaceMap.put("http://www.w3.org/XML/1998/namespace",
                         "http://www.w3.org/XML/1998/namespace");
        namespaceMap = Collections.unmodifiableMap(namespaceMap);

        Map<String, String> outputNamespaceMap = new HashMap<String, String>();
        outputNamespaceMap.put("http://www.w3.org/2005/Atom", "http://www.w3.org/2005/Atom");
        outputNamespaceMap.put("http://b216:8080/reporting/reports/toptenvalidators",
                               "http://b216:8080/reporting/reports/toptenvalidators");
        outputNamespaceMap.put("http://www.w3.org/XML/1998/namespace",
                               "http://www.w3.org/XML/1998/namespace");
        outputNamespaceMap = Collections.unmodifiableMap(outputNamespaceMap);

        JettisonJAXBProvider jaxbProvider =
            new JettisonJAXBProvider(false, new Configuration(namespaceMap),
                                     new Configuration(outputNamespaceMap));

        JettisonJAXBElementProvider jaxbElementProvider =
            new JettisonJAXBElementProvider(false, new Configuration(namespaceMap),
                                            new Configuration(outputNamespaceMap));
        return new Object[] {jaxbProvider, jaxbElementProvider};
    }

    @Path("/test/person")
    public static class PersonResource {

        @GET
        public Person getPerson() throws IOException {
            Person p = new Person();
            p.setName("My Name");
            p.setDesc("My desc");
            return p;
        }

        @POST
        public Person postPerson(Person p) {
            return p;
        }
    }

    @Path("test")
    public static class TestResource {

        @GET
        @Path("atomentry")
        @Produces("application/json")
        public AtomEntry getAtomEntry() throws IOException {
            AtomEntry entry = AtomEntry.unmarshal(new StringReader(ENTRY));
            return entry;
        }

        @GET
        @Path("atomentryelement")
        @Produces("application/json")
        public JAXBElement<AtomEntry> getAtomEntryElement() throws IOException {
            AtomEntry entry = AtomEntry.unmarshal(new StringReader(ENTRY));
            org.apache.wink.common.model.atom.ObjectFactory of =
                new org.apache.wink.common.model.atom.ObjectFactory();
            return of.createEntry(entry);
        }

        @GET
        @Path("atomsyndentry")
        @Produces("application/json")
        public SyndEntry getSyndEntry() throws IOException {
            AtomEntry entry = AtomEntry.unmarshal(new StringReader(ENTRY));
            return entry.toSynd(new SyndEntry());
        }

        @POST
        @Path("atomentry")
        @Produces("application/json")
        @Consumes("application/json")
        public AtomEntry postAtomEntry(AtomEntry entry) throws IOException {
            return entry;
        }

        @POST
        @Path("atomentryelement")
        @Produces("application/json")
        @Consumes("application/json")
        public JAXBElement<AtomEntry> postAtomEntryElement(JAXBElement<AtomEntry> entry) {
            return entry;
        }

        @POST
        @Path("atomsyndentry")
        @Produces("application/json")
        @Consumes("application/json")
        public SyndEntry postAtomSyndEntry(SyndEntry entry) {
            return entry;
        }

    }

    /**
     * Tests a simple single JAXB Object to write.
     * 
     * @throws Exception
     */
    public void testGetPerson() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test/person", "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());

        assertTrue(JSONUtils
            .equals(new JSONObject(
                                   " { \"person\" : { \"desc\" : \"My desc\", \"name\" : \"My Name\" } } "),
                    new JSONObject(response.getContentAsString())));
    }

    /**
     * Tests a simple single JAXB Object to both read and write.
     * 
     * @throws Exception
     */
    public void testPostPerson() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST", "/test/person", "application/json");
        request.setContentType("application/json");
        request.setContent(" { \"person\" : { \"desc\" : \"My desc\", \"name\" : \"My Name\" } } "
            .getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils
            .equals(new JSONObject("{\"person\":{\"desc\":\"My desc\",\"name\":\"My Name\"}}"),
                    new JSONObject(response.getContentAsString())));
    }

    public void testGetAtomEntry() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/atomentry",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());

        assertTrue(JSONUtils.equals(new JSONObject(ENTRY_JSON), new JSONObject(response
            .getContentAsString())));
    }

    public void testGetAtomEntryElement() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/atomentryelement",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(new JSONObject(ENTRY_JSON), new JSONObject(response
            .getContentAsString())));
    }

    public void testPostAtomEntry() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test/atomentry",
                                                        "application/json");
        request.setContentType("application/json");

        request.setContent(ENTRY_JSON.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(JSONUtils.objectForString(ENTRY_JSON), JSONUtils
            .objectForString(response.getContentAsString())));
    }

    public void testPostAtomEntryElement() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test/atomentryelement",
                                                        "application/json");
        request.setContentType("application/json");
        request.setContent(ENTRY_JSON.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(JSONUtils.objectForString(ENTRY_JSON), JSONUtils
            .objectForString(response.getContentAsString())));
    }

    private static final String ENTRY_STR      =
                                                   "<entry xml:base=\"http://b216:8080/reporting/reports\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <id>toptenvalidators</id>\n"
                                                       + "    <updated>@TIME@</updated>\n"
                                                       + "    <title type=\"text\" xml:lang=\"en\">top ten validators</title>\n"
                                                       + "    <published>@TIME@</published>\n"
                                                       + "    <link href=\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/json\" type=\"application/json\" rel=\"alternate\"/>\n"
                                                       + "    <author>\n"
                                                       + "        <name>admin</name>\n"
                                                       + "    </author>\n"
                                                       + "    <category label=\"report definition\" scheme=\"urn:com:systinet:reporting:kind\" term=\"urn:com:systinet:reporting:kind:definition\"/>\n"
                                                       + "</entry>\n";

    private static String       ENTRY_STR_JSON =

                                                   "{" + "\"http:\\/\\/www.w3.org\\/2005\\/Atom.entry\":"
                                                       + "{"
                                                       + "\"@http:\\/\\/www.w3.org\\/XML\\/1998\\/namespace.base\":\"http:\\/\\/b216:8080\\/reporting\\/reports\","
                                                       + "\"http:\\/\\/www.w3.org\\/2005\\/Atom.id\":\"toptenvalidators\","
                                                       + "\"http:\\/\\/www.w3.org\\/2005\\/Atom.updated\":\"@TIME@\","
                                                       + "\"http:\\/\\/www.w3.org\\/2005\\/Atom.title\":{"
                                                       + "\"@type\":\"text\","
                                                       + "\"@http:\\/\\/www.w3.org\\/XML\\/1998\\/namespace.lang\":\"en\","
                                                       + "\"$\":\"top ten validators\"},"
                                                       + "\"http:\\/\\/www.w3.org\\/2005\\/Atom.published\":\"@TIME@\","
                                                       + "\"http:\\/\\/www.w3.org\\/2005\\/Atom.link\":"
                                                       + "{\"@href\":\"http:\\/\\/b216:8080\\/reporting\\/reports\\/toptenvalidators?alt=application\\/json\","
                                                       + "\"@type\":\"application\\/json\","
                                                       + "\"@rel\":\"alternate\"},"
                                                       + "\"http:\\/\\/www.w3.org\\/2005\\/Atom.author\":"
                                                       + "{\"http:\\/\\/www.w3.org\\/2005\\/Atom.name\":\"admin\"},"
                                                       + "\"http:\\/\\/www.w3.org\\/2005\\/Atom.category\":"
                                                       + "{\"@label\":\"report definition\","
                                                       + "\"@scheme\":\"urn:com:systinet:reporting:kind\","
                                                       + "\"@term\":\"urn:com:systinet:reporting:kind:definition\"},"
                                                       + "}"
                                                       + "}";

    private static final String ENTRY;

    private static final String ENTRY_JSON;

    static {
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis((new Date()).getTime());
            XMLGregorianCalendar xmlGregCal =
                DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
            ENTRY = ENTRY_STR.replaceAll("@TIME@", xmlGregCal.toString());
            ENTRY_JSON = ENTRY_STR_JSON.replaceAll("@TIME@", xmlGregCal.toString());
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
