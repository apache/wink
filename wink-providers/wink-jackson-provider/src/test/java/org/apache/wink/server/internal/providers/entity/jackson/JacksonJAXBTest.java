package org.apache.wink.server.internal.providers.entity.jackson;

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

import java.io.IOException;
import java.io.StringReader;
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

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.json.JSONUtils;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.server.internal.providers.entity.jackson.jaxb.Person;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests the Jackson provider.
 */
public class JacksonJAXBTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class, PersonResource.class};
    }

    @Override
    protected Object[] getSingletons() {
        ObjectMapper mapper = new ObjectMapper();
        JaxbAnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector();
        mapper.getSerializationConfig().setAnnotationIntrospector(jaxbIntrospector);
        mapper.getSerializationConfig().set(Feature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.getDeserializationConfig().setAnnotationIntrospector(jaxbIntrospector);
        JacksonJsonProvider jacksonProvider = new JacksonJsonProvider(mapper);
        return new Object[] {jacksonProvider};
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
            AtomEntry entry = AtomEntry.unmarshal(new StringReader(ENTRY_STR));
            return entry;
        }

        @GET
        @Path("atomentryelement")
        @Produces("application/json")
        public JAXBElement<AtomEntry> getAtomEntryElement() throws IOException {
            AtomEntry entry = AtomEntry.unmarshal(new StringReader(ENTRY_STR));
            org.apache.wink.common.model.atom.ObjectFactory of =
                new org.apache.wink.common.model.atom.ObjectFactory();
            return of.createEntry(entry);
        }

        @GET
        @Path("atomsyndentry")
        @Produces("application/json")
        public SyndEntry getSyndEntry() throws IOException {
            AtomEntry entry = AtomEntry.unmarshal(new StringReader(ENTRY_STR));
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

        assertTrue(JSONUtils.equals(new JSONObject("{\"desc\":\"My desc\",\"name\":\"My Name\"}"),
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
        request.setContent("{\"desc\":\"My desc\",\"name\":\"My Name\"}".getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(new JSONObject("{\"desc\":\"My desc\",\"name\":\"My Name\"}"),
                                    new JSONObject(response.getContentAsString())));
    }

    public void testGetAtomEntry() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/atomentry",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(JSONUtils.objectForString(ENTRY_JSON), JSONUtils
            .objectForString(response.getContentAsString())));
    }

    public void testGetAtomEntryElement() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/atomentryelement",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(JSONUtils.objectForString(ENTRY_STR_JSON_GET), JSONUtils
            .objectForString(response.getContentAsString())));

    }

    /*
     * NOTE: this test is currently disabled, as the Jackson-supplied provider
     * cannot tolerate org.w3c.dom.Element fields in the various Atom* objects
     */
    public void _testPostAtomEntry() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test/atomentry",
                                                        "application/json");
        request.setContentType("application/json");
        request.setContent(ENTRY_JSON_POST.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());

        assertTrue(JSONUtils.equals(JSONUtils.objectForString(ENTRY_JSON_POST), JSONUtils
            .objectForString(response.getContentAsString())));
    }

    private static final String ENTRY_STR           =
                                                        "<entry xml:base=\"http://b216:8080/reporting/reports\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <id>toptenvalidators</id>\n"
                                                            + "    <updated>2009-08-31T18:30:02Z</updated>\n"
                                                            + "    <title type=\"text\" xml:lang=\"en\">top ten validators</title>\n"
                                                            + "    <published>2009-08-31T18:30:02Z</published>\n"
                                                            + "    <link href=\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/json\" type=\"application/json\" rel=\"alternate\"/>\n"
                                                            + "    <author>\n"
                                                            + "        <name>admin</name>\n"
                                                            + "    </author>\n"
                                                            + "    <category label=\"report definition\" scheme=\"urn:com:systinet:reporting:kind\" term=\"urn:com:systinet:reporting:kind:definition\"/>\n"
                                                            + "</entry>\n";

    private static final String ENTRY_STR_JSON_POST =
                                                        "{" + "\"id\":\"top ten validators\",\"updated\":@TIME_JSON@,\"title\":{\"lang\":\"en\",\"otherAttributes\":{},\"any\":[\"top ten validators\"],\"type\":\"text\"}"
                                                            + "}";

    private static final String ENTRY_STR_JSON      =
                                                        "{\"base\":\"http://b216:8080/reporting/reports\"," + "\"otherAttributes\":{},"
                                                            + "\"id\":\"toptenvalidators\","
                                                            + "\"updated\":{\"year\":2009,\"month\":8,\"day\":31,\"timezone\":0,\"hour\":18,\"minute\":30,\"second\":2,\"millisecond\":-2147483648},"
                                                            + "\"title\":{\"lang\":\"en\",\"otherAttributes\":{},\"type\":\"text\"},"
                                                            + "\"published\":{\"year\":2009,\"month\":8,\"day\":31,\"timezone\":0,\"hour\":18,\"minute\":30,\"second\":2,\"millisecond\":-2147483648},"
                                                            + "\"link\":[{\"otherAttributes\":{},\"rel\":\"alternate\",\"type\":\"application/json\",\"href\":\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/json\"}],"
                                                            + "\"author\":[{\"name\":\"admin\"}],"
                                                            + "\"category\":[{\"otherAttributes\":{},\"term\":\"urn:com:systinet:reporting:kind:definition\",\"scheme\":\"urn:com:systinet:reporting:kind\",\"label\":\"report definition\"}]"
                                                            + "}";

    private static final String ENTRY_STR_JSON_GET  =
                                                        "{\"name\":{\"namespaceURI\":\"http://www.w3.org/2005/Atom\",\"localPart\":\"entry\",\"prefix\":\"\"}," + "\"declaredType\":\"org.apache.wink.common.model.atom.AtomEntry\","
                                                            + "\"scope\":\"javax.xml.bind.JAXBElement$GlobalScope\","
                                                            + "\"value\":"
                                                            + ENTRY_STR_JSON
                                                            + ",\"nil\":false}";

    private static final String ENTRY_JSON;

    private static final String ENTRY_JSON_POST;

    static {

        GregorianCalendar gCal = new GregorianCalendar();
        XMLGregorianCalendar xmlGCal = null;
        try {
            xmlGCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);
        } catch (DatatypeConfigurationException e) {
            fail("could not construct XMLGregorianCalendar: " + e.getMessage());
            e.printStackTrace();
        }
        String jsonTimeStr = "{";
        jsonTimeStr += "\"eon\":" + xmlGCal.getEon() + ",";
        jsonTimeStr += "\"year\":" + xmlGCal.getYear() + ",";
        jsonTimeStr += "\"day\":" + xmlGCal.getDay() + ",";
        jsonTimeStr += "\"timezone\":" + xmlGCal.getTimezone() + ",";
        jsonTimeStr += "\"hour\":" + xmlGCal.getHour() + ",";
        jsonTimeStr += "\"minute\":" + xmlGCal.getMinute() + ",";
        jsonTimeStr += "\"second\":" + xmlGCal.getSecond() + ",";
        jsonTimeStr += "\"millisecond\":" + xmlGCal.getMillisecond();
        jsonTimeStr += "}";

        ENTRY_JSON = ENTRY_STR_JSON.replaceAll("@TIME_JSON@", jsonTimeStr);
        ENTRY_JSON_POST = ENTRY_STR_JSON_POST.replaceAll("@TIME_JSON@", jsonTimeStr);

    }

}
