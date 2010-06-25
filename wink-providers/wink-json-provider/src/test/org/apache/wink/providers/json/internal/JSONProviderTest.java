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

import java.io.StringReader;
import java.util.Collections;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

import org.apache.wink.common.annotations.Asset;
import org.apache.wink.common.model.json.JSONUtils;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JsonProviderTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class};
    }

    private static final SyndFeed  SYND_FEED          = new SyndFeed(new SyndText("title"), "id");

    private static final SyndEntry SYND_ENTRY         =
                                                          new SyndEntry(
                                                                        new SyndText("entry title"),
                                                                        "entry:id");

    private static final String    JSON_FEED          =
                                                          "{\"feed\": {\n" + "  \"@xmlns\": {\"$\": \"http:\\/\\/www.w3.org\\/2005\\/Atom\"},\n"
                                                              + "  \"id\": {\n"
                                                              + "    \"@xmlns\": {\"$\": \"http:\\/\\/www.w3.org\\/2005\\/Atom\"},\n"
                                                              + "    \"$\": \"id\"\n"
                                                              + "  },\n"
                                                              + "  \"title\": {\n"
                                                              + "    \"@type\": \"text\",\n"
                                                              + "    \"@xmlns\": {\"$\": \"http:\\/\\/www.w3.org\\/2005\\/Atom\"},\n"
                                                              + "    \"$\": \"title\"\n"
                                                              + "  }\n"
                                                              + "}}";

    private static final String    JSON               =
                                                          "{\"entry\": {\n" + "  \"id\": {\"$\": \"entry:id\"},\n"
                                                              + "  \"title\": {\n"
                                                              + "    \"@type\": \"text\",\n"
                                                              + "    \"$\": \"entry title\"\n"
                                                              + "  }\n"
                                                              + "}}";

    private static final String    JSON_ARRAY         = "[" + JSON + ", {\"test\":\"ing\"}]";

    private static final String    JSON_AS_ATOM_ENTRY =
                                                          "{\"entry\": {\n" + "  \"@xmlns\": {\"$\": \"http:\\/\\/www.w3.org\\/2005\\/Atom\"},\n"
                                                              + "  \"id\": {\n"
                                                              + "    \"@xmlns\": {\"$\": \"http:\\/\\/www.w3.org\\/2005\\/Atom\"},\n"
                                                              + "    \"$\": \"entry:id\"\n"
                                                              + "  },\n"
                                                              + "  \"title\": {\n"
                                                              + "    \"@type\": \"text\",\n"
                                                              + "    \"@xmlns\": {\"$\": \"http:\\/\\/www.w3.org\\/2005\\/Atom\"},\n"
                                                              + "    \"$\": \"entry title\"\n"
                                                              + "  }\n"
                                                              + "}}";

    private void compairJsonContent(final String expected, final String actual)
        throws JSONException {
        JSONObject result = JSONUtils.objectForString(actual);
        JSONObject want = JSONUtils.objectForString(expected);
        assertTrue(JSONUtils.equals(want, result));
    }

    @Path("test")
    public static class TestResource {

        @GET
        @Path("json")
        @Produces("application/json")
        public JSONObject getJson() throws Exception {
            return new JSONObject(JSON);
        }

        @POST
        @Path("json")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public JSONObject postJson(JSONObject object) throws Exception {
            return object.put("foo", "bar");
        }

        @GET
        @Path("jsonarray")
        @Produces(MediaType.APPLICATION_JSON)
        public JSONArray getJsonArray() throws Exception {
            return new JSONArray(JSON_ARRAY);
        }

        @POST
        @Path("jsonarray")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public JSONArray postJson(JSONArray array) throws Exception {
            return array.put(Collections.singletonMap("foo", "bar"));
        }

        @GET
        @Path("jsonfeed")
        @Produces("application/json")
        public SyndFeed getJsonFeed() throws Exception {
            return SYND_FEED;
        }

        @GET
        @Path("jaxb")
        @Produces("application/json")
        public Entry getJAXB() throws Exception {
            Entry entry = TestJAXBAsset.getJAXBEntry();
            return entry;
        }

        @GET
        @Path("jaxbelement")
        @Produces("application/json")
        public JAXBElement<Entry> getJAXBElement() throws Exception {
            Entry entry = TestJAXBAsset.getJAXBEntry();
            return new JAXBElement<Entry>(new QName("entry"), Entry.class, entry);
        }

        @GET
        @Path("atom")
        @Produces("application/json")
        public SyndEntry getAtom() throws Exception {
            return SYND_ENTRY;
        }

        @GET
        @Path("jsonasset")
        @Produces("application/json")
        public TestJsonAsset getJsonAsset() throws Exception {
            return new TestJsonAsset();
        }

        @GET
        @Path("atomasset")
        @Produces("application/json")
        public TestOtherMediaTypeAsset getAtomAsset() throws Exception {
            return new TestOtherMediaTypeAsset();
        }

        @GET
        @Path("jaxbasset")
        @Produces("application/json")
        public TestJAXBAsset getJAXBAsset() throws Exception {
            return new TestJAXBAsset();
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "Entry", propOrder = {"id", "title"})
    @XmlRootElement(name = "entry")
    public static class Entry {

        @XmlElement(name = "id")
        public String id;
        @XmlElementRef
        public Title  title;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "Title", propOrder = {"type"})
    @XmlRootElement(name = "title")
    public static class Title {

        @XmlAttribute(name = "type")
        public String type;
        @XmlValue
        public String value;
    }

    @Asset
    public static class TestJsonAsset {

        @Produces(MediaType.APPLICATION_JSON)
        public JSONObject getJSONObject() {
            try {
                return new JSONObject(JSON);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Asset
    public static class TestOtherMediaTypeAsset {

        public MediaType getJsonXmlMediaType() {
            return MediaType.APPLICATION_XML_TYPE;
        }

        @Produces(MediaType.APPLICATION_JSON)
        public Entry getJAXB() {
            return TestJAXBAsset.getJAXBEntry();
        }

        @Consumes(MediaType.APPLICATION_JSON)
        public void setJAXB(Entry jaxbObject) {
            fail("json does not support read");
        }
    }

    @Asset
    public static class TestJAXBAsset {

        public static Entry getJAXBEntry() {
            Title title = new Title();
            title.type = "text";
            title.value = "entry title";
            Entry entry = new Entry();
            entry.id = "entry:id";
            entry.title = title;
            return entry;
        }

        @Produces(MediaType.APPLICATION_JSON)
        public Entry getJAXB() {
            return getJAXBEntry();
        }

        @Consumes(MediaType.APPLICATION_JSON)
        public void setJAXB(Entry entry) {
            fail("setJAXB shouldn't be called for Json");
        }
    }

    public void testGetJson() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test/json", "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        compairJsonContent(JSON, response.getContentAsString());
    }

    public void testPostJson() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test/json",
                                                        "application/json",
                                                        MediaType.APPLICATION_JSON,
                                                        JSON.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        JSONObject result = JSONUtils.objectForString(response.getContentAsString());
        JSONObject want = JSONUtils.objectForString(JSON).put("foo", "bar");
        assertTrue(JSONUtils.equals(want, result));
    }

    public void testGetJsonArray() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/jsonarray",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        JSONArray result =
            new JSONArray(new JSONTokener(new StringReader(response.getContentAsString())));
        JSONArray want = new JSONArray(JSON_ARRAY);
        assertTrue(JSONUtils.equals(want, result));
    }

    public void testPostJsonArray() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test/jsonarray",
                                                        "application/json",
                                                        MediaType.APPLICATION_JSON,
                                                        JSON_ARRAY.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        JSONArray result =
            new JSONArray(new JSONTokener(new StringReader(response.getContentAsString())));
        JSONArray want = new JSONArray(JSON_ARRAY).put(Collections.singletonMap("foo", "bar"));
        assertTrue(JSONUtils.equals(want, result));
    }

    public void testGetJsonFeed() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor
                .constructMockRequest("GET", "/test/jsonfeed", "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        compairJsonContent(JSON_FEED, response.getContentAsString());
    }

    public void testGetJsonFromJAXB() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test/jaxb", "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        compairJsonContent(JSON, response.getContentAsString());
    }

    public void testGetJsonFromJAXBElement() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/jaxbelement",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        compairJsonContent(JSON, response.getContentAsString());
    }

    public void testGetJsonFromAtom() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test/atom", "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        compairJsonContent(JSON_AS_ATOM_ENTRY, response.getContentAsString());
    }

    public void testGetJsonAsset() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/jsonasset",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        compairJsonContent(JSON, response.getContentAsString());
    }

    public void testGetJAXBAsset() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/jaxbasset",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        compairJsonContent(JSON, response.getContentAsString());
    }

    public void testGetAtomAsset() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/atomasset",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        compairJsonContent(JSON, response.getContentAsString());
    }

}
