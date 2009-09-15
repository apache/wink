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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.apache.wink.common.annotations.Asset;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class AssetProviderTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class};
    }

    private static final String STRING = "hello message";
    private static final String XML    =
                                           "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + "<entry>\r\n"
                                               + "    <id>entry:id</id>\r\n"
                                               + "    <title type=\"text\">entry title</title>\r\n"
                                               + "</entry>\r\n";
    private static final String JSON   =
                                           "{\"entry\": {\n" + "  \"id\": {\"$\": \"entry:id\"},\n"
                                               + "  \"title\": {\n"
                                               + "    \"@type\": \"text\",\n"
                                               + "    \"$\": \"entry title\"\n"
                                               + "  }\n"
                                               + "}}";

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
    public static class TestAsset {


        @Produces(MediaType.APPLICATION_XML)
        public Entry getJAXBEntry() {
            Title title = new Title();
            title.type = "text";
            title.value = "entry title";
            Entry entry = new Entry();
            entry.id = "entry:id";
            entry.title = title;
            return entry;
        }

        @Produces(MediaType.TEXT_PLAIN)
        public String getString() {
            return STRING;
        }

        @Consumes(MediaType.APPLICATION_XML)
        public void setJAXBEntry(Entry entry, @Context Providers providers) {
            assertNotNull(providers);
            assertNotNull(entry);
            assertNotNull(entry.title);
            assertEquals("entry:id", entry.id);
            assertEquals("text", entry.title.type);
            assertEquals("entry title", entry.title.value);
        }

        @Consumes(MediaType.TEXT_PLAIN)
        public void setString(String string) {
            assertNotNull(string);
            assertEquals(STRING, string);
        }

    }

    @Path("test")
    public static class TestResource {
        @GET
        @Produces( {"application/xml", "text/plain"})
        public TestAsset getAsset() {
            return new TestAsset();
        }

        @POST
        @Produces( {"application/xml", "text/plain"})
        @Consumes( {"application/xml", "text/plain"})
        public TestAsset postAsset(TestAsset asset) {
            return asset;
        }
    }

    public void testAssetGetXml() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test", "application/xml");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(XML, response.getContentAsString());
        assertNull(msg, msg);
    }

    public void testAssetGetString() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test", "text/plain");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(STRING, response.getContentAsString());
    }

    public void testAssetPostXmlGetXml() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test",
                                                        "application/xml",
                                                        "application/xml",
                                                        XML.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(XML, response.getContentAsString());
        assertNull(msg, msg);
    }

    public void testAssetPostStringGetString() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test",
                                                        "text/plain",
                                                        "text/plain",
                                                        STRING.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(STRING, response.getContentAsString());
    }

    public void testAssetPostXmlGetString() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test",
                                                        "text/plain",
                                                        "application/xml",
                                                        XML.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(STRING, response.getContentAsString());
    }

    public void testAssetPostStringGetXml() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test",
                                                        "application/xml",
                                                        "text/plain",
                                                        STRING.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(XML, response.getContentAsString());
        assertNull(msg, msg);
    }
}
