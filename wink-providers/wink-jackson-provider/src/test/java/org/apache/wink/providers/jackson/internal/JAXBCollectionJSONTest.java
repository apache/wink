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

package org.apache.wink.providers.jackson.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.providers.jackson.internal.jaxb.Person;
import org.apache.wink.providers.json.JSONUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.json.JSONArray;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JAXBCollectionJSONTest extends MockServletInvocationTest {

    private static final String ENTRY_STR_1         =
                                                        "<entry xml:base=\"http://b216:8080/reporting/reports\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <id>toptenvalidators</id>\n"
                                                            + "    <updated>2009-08-31T18:30:02Z</updated>\n"
                                                            + "    <title type=\"text\" xml:lang=\"en\">top ten validators 1</title>\n"
                                                            + "    <published>2009-08-31T18:30:02Z</published>\n"
                                                            + "    <link href=\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/json\" type=\"application/json\" rel=\"alternate\"/>\n"
                                                            + "    <author>\n"
                                                            + "        <name>admin 1</name>\n"
                                                            + "    </author>\n"
                                                            + "    <category label=\"report definition\" scheme=\"urn:com:systinet:reporting:kind\" term=\"urn:com:systinet:reporting:kind:definition\"/>\n"
                                                            + "</entry>";
    private static final String ENTRY_STR_2         =
                                                        "<entry xml:base=\"http://b216:8080/reporting/reports\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <id>toptenvalidators</id>\n"
                                                            + "    <updated>2009-08-31T18:30:02Z</updated>\n"
                                                            + "    <title type=\"text\" xml:lang=\"en\">top ten validators 2</title>\n"
                                                            + "    <published>2009-08-31T18:30:02Z</published>\n"
                                                            + "    <link href=\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/json\" type=\"application/json\" rel=\"alternate\"/>\n"
                                                            + "    <author>\n"
                                                            + "        <name>admin 2</name>\n"
                                                            + "    </author>\n"
                                                            + "    <category label=\"report definition\" scheme=\"urn:com:systinet:reporting:kind\" term=\"urn:com:systinet:reporting:kind:definition\"/>\n"
                                                            + "</entry>";

    private static final String ENTRY_STR_JSON_1    =
                                                        "{\"base\":\"http://b216:8080/reporting/reports\"," + "\"otherAttributes\":{},"
                                                            + "\"id\":\"toptenvalidators\","
                                                            + "\"updated\":1251743402000,"
                                                            + "\"title\":{\"lang\":\"en\",\"otherAttributes\":{},\"type\":\"text\"},"
                                                            + "\"published\":1251743402000,"
                                                            + "\"link\":[{\"otherAttributes\":{},\"rel\":\"alternate\",\"type\":\"application/json\",\"href\":\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/json\"}],"
                                                            + "\"author\":[{\"name\":\"admin 1\"}],"
                                                            + "\"category\":[{\"otherAttributes\":{},\"term\":\"urn:com:systinet:reporting:kind:definition\",\"scheme\":\"urn:com:systinet:reporting:kind\",\"label\":\"report definition\"}]"
                                                            + "}";
    private static final String ENTRY_STR_JSON_2    =
                                                        "{\"base\":\"http://b216:8080/reporting/reports\"," + "\"otherAttributes\":{},"
                                                            + "\"id\":\"toptenvalidators\","
                                                            + "\"updated\":1251743402000,"
                                                            + "\"title\":{\"lang\":\"en\",\"otherAttributes\":{},\"type\":\"text\"},"
                                                            + "\"published\":1251743402000,"
                                                            + "\"link\":[{\"otherAttributes\":{},\"rel\":\"alternate\",\"type\":\"application/json\",\"href\":\"http://b216:8080/reporting/reports/toptenvalidators?alt=application/json\"}],"
                                                            + "\"author\":[{\"name\":\"admin 2\"}],"
                                                            + "\"category\":[{\"otherAttributes\":{},\"term\":\"urn:com:systinet:reporting:kind:definition\",\"scheme\":\"urn:com:systinet:reporting:kind\",\"label\":\"report definition\"}]"
                                                            + "}";

    private static final String ENTRY_STR_JSON_GET  =
                                                        "[{\"name\":\"{http://www.w3.org/2005/Atom}entry\"," + "\"declaredType\":\"org.apache.wink.common.model.atom.AtomEntry\","
                                                            + "\"scope\":\"javax.xml.bind.JAXBElement$GlobalScope\","
                                                            + "\"value\":"
                                                            + ENTRY_STR_JSON_1
                                                            + ",\"nil\":false, \"typeSubstituted\":false,\"globalScope\":true},"
                                                            + "{\"name\":\"{http://www.w3.org/2005/Atom}entry\"," + "\"declaredType\":\"org.apache.wink.common.model.atom.AtomEntry\","
                                                            + "\"scope\":\"javax.xml.bind.JAXBElement$GlobalScope\","
                                                            + "\"value\":"
                                                            + ENTRY_STR_JSON_2
                                                            + ",\"nil\":false, \"typeSubstituted\":false,\"globalScope\":true}]";

    private static final String ENTRY_JSON_1;
    private static final String ENTRY_JSON_2;

    static {

        GregorianCalendar gCal = new GregorianCalendar();
        XMLGregorianCalendar xmlGCal = null;
        try {
            xmlGCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);
        } catch (DatatypeConfigurationException e) {
            fail("could not construct XMLGregorianCalendar: " + e.getMessage());
            e.printStackTrace();
        }
        String jsonTimeStr1 = "{";
        jsonTimeStr1 += "\"eon\":" + xmlGCal.getEon() + ",";
        jsonTimeStr1 += "\"year\":" + xmlGCal.getYear() + ",";
        jsonTimeStr1 += "\"day\":" + xmlGCal.getDay() + ",";
        jsonTimeStr1 += "\"timezone\":" + xmlGCal.getTimezone() + ",";
        jsonTimeStr1 += "\"hour\":" + xmlGCal.getHour() + ",";
        jsonTimeStr1 += "\"minute\":" + xmlGCal.getMinute() + ",";
        jsonTimeStr1 += "\"second\":" + xmlGCal.getSecond() + ",";
        jsonTimeStr1 += "\"millisecond\":" + xmlGCal.getMillisecond();
        jsonTimeStr1 += "}";
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        String jsonTimeStr2 = "{";
        jsonTimeStr2 += "\"eon\":" + xmlGCal.getEon() + ",";
        jsonTimeStr2 += "\"year\":" + xmlGCal.getYear() + ",";
        jsonTimeStr2 += "\"day\":" + xmlGCal.getDay() + ",";
        jsonTimeStr2 += "\"timezone\":" + xmlGCal.getTimezone() + ",";
        jsonTimeStr2 += "\"hour\":" + xmlGCal.getHour() + ",";
        jsonTimeStr2 += "\"minute\":" + xmlGCal.getMinute() + ",";
        jsonTimeStr2 += "\"second\":" + xmlGCal.getSecond() + ",";
        jsonTimeStr2 += "\"millisecond\":" + xmlGCal.getMillisecond();
        jsonTimeStr2 += "}";

        ENTRY_JSON_1 = ENTRY_STR_JSON_1.replaceAll("@TIME_JSON@", jsonTimeStr1);
        ENTRY_JSON_2 = ENTRY_STR_JSON_2.replaceAll("@TIME_JSON@", jsonTimeStr2);
    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class, PersonResource.class};
    }

    @Override
    protected Object[] getSingletons() {
    	JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
    	jacksonProvider.configure(Feature.WRITE_NULL_MAP_VALUES, Boolean.FALSE);
    	jacksonProvider.configure(Feature.WRITE_NULL_PROPERTIES, Boolean.FALSE);
    	jacksonProvider.configure(Feature.WRITE_DATES_AS_TIMESTAMPS, Boolean.TRUE);
        return new Object[] {jacksonProvider};
    }

    @Path("/test/person")
    public static class PersonResource {

        @GET
        @Path("collection")
        public List<Person> getPeopleCollection() throws IOException {
            List<Person> people = new ArrayList<Person>();
            Person p = new Person();
            p.setName("My Name");
            p.setDesc("My desc");
            people.add(p);
            p = new Person();
            p.setName("My Name 2");
            p.setDesc("My desc 2");
            people.add(p);
            return people;
        }

        @GET
        @Path("array")
        public Person[] getPeopleArray() throws IOException {
            return getPeopleCollection().toArray(new Person[] {});
        }

        @GET
        @Path("jaxbelement")
        public List<JAXBElement<Person>> getPeopleJAXBElementCollection() throws IOException {
            List<Person> people = getPeopleCollection();
            List<JAXBElement<Person>> ret = new ArrayList<JAXBElement<Person>>();
            JAXBElement<Person> element = null;
            for (Person p : people) {
                element = new JAXBElement<Person>(new QName("", "person"), Person.class, p);
                ret.add(element);
            }
            return ret;
        }

        @POST
        @Path("collection")
        public List<Person> postPeopleCollection(List<Person> p) {
            return p;
        }

        @POST
        @Path("array")
        public Person[] postPeopleArray(Person[] p) {
            return p;
        }

    }

    @Path("test")
    public static class TestResource {

        @GET
        @Path("atomentries/collection")
        @Produces("application/json")
        public List<AtomEntry> getAtomEntriesCollection() throws IOException {
            List<AtomEntry> entries = new ArrayList<AtomEntry>();
            AtomEntry entry1 = AtomEntry.unmarshal(new StringReader(ENTRY_STR_1));
            AtomEntry entry2 = AtomEntry.unmarshal(new StringReader(ENTRY_STR_2));
            entries.add(entry1);
            entries.add(entry2);
            return entries;
        }

        @GET
        @Path("atomentries/array")
        @Produces("application/json")
        public AtomEntry[] getAtomEntriesArray() throws IOException {
            return getAtomEntriesCollection().toArray(new AtomEntry[] {});
        }

        @GET
        @Path("atomentryelements/collection")
        @Produces("application/json")
        public List<JAXBElement<AtomEntry>> getAtomEntryElementCollection() throws IOException {
            List<JAXBElement<AtomEntry>> entries = new ArrayList<JAXBElement<AtomEntry>>();
            AtomEntry entry1 = AtomEntry.unmarshal(new StringReader(ENTRY_STR_1));
            AtomEntry entry2 = AtomEntry.unmarshal(new StringReader(ENTRY_STR_2));
            org.apache.wink.common.model.atom.ObjectFactory of =
                new org.apache.wink.common.model.atom.ObjectFactory();
            entries.add(of.createEntry(entry1));
            entries.add(of.createEntry(entry2));
            return entries;
        }
    }

    public void testGetPeople() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/person/collection",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());

        assertTrue(JSONUtils
            .equals(new JSONArray(
                                  "[{\"desc\":\"My desc\",\"name\":\"My Name\"},{\"desc\":\"My desc 2\",\"name\":\"My Name 2\"}]"),
                    new JSONArray(response.getContentAsString())));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/person/array",
                                                        "application/json");
        response = invoke(request);
        assertEquals(200, response.getStatus());

        assertTrue(JSONUtils
            .equals(new JSONArray(
                                  "[{\"desc\":\"My desc\",\"name\":\"My Name\"},{\"desc\":\"My desc 2\",\"name\":\"My Name 2\"}]"),
                    new JSONArray(response.getContentAsString())));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/person/jaxbelement",
                                                        "application/json");
        response = invoke(request);
        assertEquals(200, response.getStatus());

        assertTrue(JSONUtils
            .equals(new JSONArray(
                                  "[{\"name\":\"person\",\"declaredType\":\"org.apache.wink.providers.jackson.internal.jaxb.Person\",\"scope\":\"javax.xml.bind.JAXBElement$GlobalScope\",\"value\":{\"name\":\"My Name\",\"desc\":\"My desc\"},\"nil\":false, \"typeSubstituted\":false,\"globalScope\":true},{\"name\":\"person\",\"declaredType\":\"org.apache.wink.providers.jackson.internal.jaxb.Person\",\"scope\":\"javax.xml.bind.JAXBElement$GlobalScope\",\"value\":{\"name\":\"My Name 2\",\"desc\":\"My desc 2\"},\"nil\":false, \"typeSubstituted\":false,\"globalScope\":true}]"),
                    new JSONArray(response.getContentAsString())));
    }

    public void testPostPeople() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test/person/collection",
                                                        "application/json");
        request.setContentType("application/json");
        request
            .setContent("[{\"desc\":\"My desc 1\",\"name\":\"My Name 1\"},{\"desc\":\"My desc 2\",\"name\":\"My Name 2\"}]"
                .getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils
            .equals(new JSONArray(
                                  "[{\"desc\":\"My desc 1\",\"name\":\"My Name 1\"},{\"desc\":\"My desc 2\",\"name\":\"My Name 2\"}]"),
                    new JSONArray(response.getContentAsString())));

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test/person/array",
                                                        "application/json");
        request.setContentType("application/json");
        request
            .setContent("[{\"desc\":\"My desc 1\",\"name\":\"My Name 1\"},{\"desc\":\"My desc 2\",\"name\":\"My Name 2\"}]"
                .getBytes());
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils
            .equals(new JSONArray(
                                  "[{\"desc\":\"My desc 1\",\"name\":\"My Name 1\"},{\"desc\":\"My desc 2\",\"name\":\"My Name 2\"}]"),
                    new JSONArray(response.getContentAsString())));

    }

    public void testGetAtomEntries() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/atomentries/collection",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(new JSONArray("[" + ENTRY_JSON_1 + "," + ENTRY_JSON_2 + "]"),
                                    new JSONArray(response.getContentAsString())));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/atomentries/array",
                                                        "application/json");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(new JSONArray("[" + ENTRY_JSON_1 + "," + ENTRY_JSON_2 + "]"),
                                    new JSONArray(response.getContentAsString())));
    }

    public void testGetAtomEntryElements() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/atomentryelements/collection",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(new JSONArray(ENTRY_STR_JSON_GET), new JSONArray(response
            .getContentAsString())));
    }
}
