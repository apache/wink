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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.wink.common.annotations.Asset;
import org.apache.wink.common.internal.providers.entity.xml.JAXBArrayXmlProvider;
import org.apache.wink.common.internal.providers.entity.xml.JAXBCollectionXmlProvider;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.atom.ObjectFactory;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JAXBCollectionXMLProviderTest extends MockServletInvocationTest {

    public static final String SOURCE_REQUEST              =
                                                               "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><feeds><feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:ns2=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns3=\"http://www.w3.org/1999/xhtml\"><id>0</id></feed><feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:ns2=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns3=\"http://www.w3.org/1999/xhtml\"><id>1</id></feed><feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:ns2=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns3=\"http://www.w3.org/1999/xhtml\"><id>2</id></feed></feeds>";
    public static final byte[] SOURCE_REQUEST_BYTES        = SOURCE_REQUEST.getBytes();
    public static final String SOURCE_RESPONSE             =
                                                               "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><feeds><feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:ns2=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns3=\"http://www.w3.org/1999/xhtml\"><id>010</id></feed><feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:ns2=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns3=\"http://www.w3.org/1999/xhtml\"><id>110</id></feed><feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:ns2=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns3=\"http://www.w3.org/1999/xhtml\"><id>210</id></feed></feeds>";
    public static final String JAXBXmlType_REQUEST         =
                                                               "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><JAXBXmlTypes><JAXBXmlType><id>0</id><name>0</name></JAXBXmlType><JAXBXmlType><id>1</id><name>1</name></JAXBXmlType><JAXBXmlType><id>2</id><name>2</name></JAXBXmlType></JAXBXmlTypes>";
    public static final byte[] JAXBXmlType_REQUEST_BYTES   = JAXBXmlType_REQUEST.getBytes();
    public static final String JAXBXmlType_RESPONSE        =
                                                               "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><JAXBXmlTypes><JAXBXmlType><id>ID0</id><name>NAME0</name></JAXBXmlType><JAXBXmlType><id>ID1</id><name>NAME1</name></JAXBXmlType><JAXBXmlType><id>ID2</id><name>NAME2</name></JAXBXmlType></JAXBXmlTypes>";
    public static final String JAXBXmlRootElement_REQUEST  =
                                                               "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><JAXBXmlRootElements><JAXBXmlRootElement><id>0</id><name>0</name></JAXBXmlRootElement><JAXBXmlRootElement><id>1</id><name>1</name></JAXBXmlRootElement><JAXBXmlRootElement><id>2</id><name>2</name></JAXBXmlRootElement></JAXBXmlRootElements>";
    public static final byte[] JAXBXmlRootElement_BYTES    = JAXBXmlRootElement_REQUEST.getBytes();
    public static final String JAXBXmlRootElement_RESPONSE =
                                                               "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><JAXBXmlRootElements><JAXBXmlRootElement><id>ID0</id><name>NAME0</name></JAXBXmlRootElement><JAXBXmlRootElement><id>ID1</id><name>NAME1</name></JAXBXmlRootElement><JAXBXmlRootElement><id>ID2</id><name>NAME2</name></JAXBXmlRootElement></JAXBXmlRootElements>";

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {Resource.class};
    }

    @Path("jaxbresource")
    public static class Resource {

        @POST
        @Path("jaxbelement")
        public List<JAXBElement<AtomFeed>> createWrapped(List<JAXBElement<AtomFeed>> feedList) {
            List<JAXBElement<AtomFeed>> wrappedFeeds = new ArrayList<JAXBElement<AtomFeed>>();
            for (JAXBElement<AtomFeed> feed : feedList) {
                AtomFeed atomFeed = new AtomFeed();
                atomFeed.setId(feed.getValue().getId() + 10);
                JAXBElement<AtomFeed> wrappedFeed = new ObjectFactory().createFeed(atomFeed);
                wrappedFeeds.add(wrappedFeed);
            }
            return wrappedFeeds;
        }

        @POST
        @Path("xmlrootwithfactorycollection")
        public List<AtomFeed> createBareCollection(List<AtomFeed> feedList) {
            List<AtomFeed> feeds = new ArrayList<AtomFeed>();
            for (AtomFeed feed : feedList) {
                AtomFeed atomFeed = new AtomFeed();
                atomFeed.setId(feed.getId() + 10);
                feeds.add(atomFeed);
            }
            return feeds;
        }

        @POST
        @Path("xmlrootwithfactoryarray")
        public AtomFeed[] createBareArray(AtomFeed[] feedList) {
            List<AtomFeed> feeds = new ArrayList<AtomFeed>();
            for (AtomFeed feed : feedList) {
                AtomFeed atomFeed = new AtomFeed();
                atomFeed.setId(feed.getId() + 10);
                feeds.add(atomFeed);
            }
            return feeds.toArray(new AtomFeed[] {});
        }

        @POST
        @Path("xmltypecollection")
        public List<JAXBElement<JAXBXmlType>> createxmltypeCollection(List<JAXBXmlType> resource) {
            List<JAXBElement<JAXBXmlType>> ret = new ArrayList<JAXBElement<JAXBXmlType>>();
            JAXBXmlType s = null;
            for (JAXBXmlType type : resource) {
                s = new JAXBXmlType();
                s.setId("ID" + type.getId());
                s.setName("NAME" + type.getName());
                JAXBElement<JAXBXmlType> element =
                    new JAXBElement<JAXBXmlType>(new QName("JAXBXmlType"), JAXBXmlType.class, s);
                ret.add(element);
            }
            return ret;
        }

        @POST
        @Path("xmltypearray")
        public JAXBXmlType[] createxmltypeArray(JAXBXmlType[] resource) {
            JAXBXmlType[] ret = new JAXBXmlType[resource.length];
            JAXBXmlType s = null;
            int i = 0;
            for (JAXBXmlType type : resource) {
                s = new JAXBXmlType();
                s.setId("ID" + type.getId());
                s.setName("NAME" + type.getId());
                ret[i++] = s;
            }
            return ret;
        }

        @POST
        @Path("xmlrootnofactorycollection")
        public List<JAXBXmlRootElement> createXmlRoot(List<JAXBXmlRootElement> resource) {
            List<JAXBXmlRootElement> ret = new ArrayList<JAXBXmlRootElement>();
            JAXBXmlRootElement s = null;
            for (int i = 0; i < resource.size(); ++i) {
                s = new JAXBXmlRootElement();
                s.setId("ID" + resource.get(i).getId());
                s.setName("NAME" + resource.get(i).getName());
                ret.add(s);
            }
            return ret;
        }

        @POST
        @Path("xmlrootnofactoryarray")
        public JAXBXmlRootElement[] createXmlRoot(JAXBXmlRootElement[] resource) {
            JAXBXmlRootElement[] ret = new JAXBXmlRootElement[resource.length];
            JAXBXmlRootElement s = null;
            for (int i = 0; i < resource.length; ++i) {
                s = new JAXBXmlRootElement();
                s.setId("ID" + resource[i].getId());
                s.setName("NAME" + resource[i].getName());
                ret[i] = s;
            }
            return ret;
        }

        @GET
        @Path("xmltypenofactorycollection")
        public List<JAXBXmlType> getXmltypeNoFactoryCollection() {
            List<JAXBXmlType> ret = new ArrayList<JAXBXmlType>();
            JAXBXmlType s = null;
            for (int i = 0; i < 3; ++i) {
                s = new JAXBXmlType();
                s.setId("ID" + i);
                s.setName("NAME" + i);
                ret.add(s);
            }
            return ret;
        }

        @GET
        @Path("xmltypenofactoryarray")
        public JAXBXmlType[] getXmltypeNoFactoryArray() {
            List<JAXBXmlType> ret = new ArrayList<JAXBXmlType>();
            JAXBXmlType s = null;
            for (int i = 0; i < 3; ++i) {
                s = new JAXBXmlType();
                s.setId("ID" + i);
                s.setName("NAME" + i);
                ret.add(s);
            }
            return ret.toArray(new JAXBXmlType[] {});
        }

        @POST
        @Path("xmlassetcollection")
        public List<TestAsset> getAsset(List<TestAsset> asset) {
            return asset;
        }
    }

    @Asset
    public static class TestAsset {

        JAXBXmlType xml;

        @Produces(MediaType.TEXT_XML)
        public JAXBXmlType getJAXB() {
            return xml;
        }

        @Consumes(MediaType.TEXT_XML)
        public void setJAXB(JAXBXmlType jaxbObject) {
            xml = jaxbObject;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "JAXBXmlType", propOrder = {"id", "name"})
    public static class JAXBXmlType {

        @XmlElement(required = true)
        private String id;
        @XmlElement(required = true)
        private String name;

        public JAXBXmlType() {
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "JAXBXmlRootElement", propOrder = {"id", "name"})
    @XmlRootElement(name = "JAXBXmlRootElement")
    public static class JAXBXmlRootElement {

        @XmlElement(required = true)
        private String id;
        @XmlElement(required = true)
        private String name;

        public JAXBXmlRootElement() {
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    public void testJAXBCollectionXMLProviderIsReadableCollection() throws Exception {
        JAXBCollectionXmlProvider provider = new JAXBCollectionXmlProvider();

        GenericEntity<List<AtomFeed>> type1 =
            new GenericEntity<List<AtomFeed>>(new ArrayList<AtomFeed>()) {
            };
        assertTrue(provider.isReadable(type1.getRawType(),
                                       type1.getType(),
                                       null,
                                       new MediaType("text", "xml")));
        assertTrue(provider.isReadable(type1.getRawType(),
                                       type1.getType(),
                                       null,
                                       new MediaType("application", "xml")));
        assertTrue(provider.isReadable(type1.getRawType(),
                                       type1.getType(),
                                       null,
                                       new MediaType("application", "atom+xml")));
        assertTrue(provider.isReadable(type1.getRawType(),
                                       type1.getType(),
                                       null,
                                       new MediaType("application", "atomsvc+xml")));

        GenericEntity<List<JAXBElement<AtomFeed>>> type2 =
            new GenericEntity<List<JAXBElement<AtomFeed>>>(new ArrayList<JAXBElement<AtomFeed>>()) {
            };
        assertTrue(provider.isReadable(type2.getRawType(),
                                       type2.getType(),
                                       null,
                                       new MediaType("text", "xml")));
        assertTrue(provider.isReadable(type2.getRawType(),
                                       type2.getType(),
                                       null,
                                       new MediaType("application", "xml")));
        assertTrue(provider.isReadable(type2.getRawType(),
                                       type2.getType(),
                                       null,
                                       new MediaType("application", "atom+xml")));
        assertTrue(provider.isReadable(type2.getRawType(),
                                       type2.getType(),
                                       null,
                                       new MediaType("application", "atomsvc+xml")));

        GenericEntity<List<String>> type3 =
            new GenericEntity<List<String>>(new ArrayList<String>()) {
            };
        assertFalse(provider.isReadable(type3.getRawType(),
                                        type3.getType(),
                                        null,
                                        new MediaType("text", "xml")));
        assertFalse(provider.isReadable(type3.getRawType(),
                                        type3.getType(),
                                        null,
                                        new MediaType("application", "xml")));
        assertFalse(provider.isReadable(type3.getRawType(),
                                        type3.getType(),
                                        null,
                                        new MediaType("application", "atom+xml")));
        assertFalse(provider.isReadable(type3.getRawType(),
                                        type3.getType(),
                                        null,
                                        new MediaType("application", "atomsvc+xml")));
    }

    public void testJAXBCollectionXMLProviderIsReadableArray() throws Exception {
        JAXBArrayXmlProvider provider = new JAXBArrayXmlProvider();

        assertTrue(provider.isReadable(AtomFeed[].class,
                                       AtomFeed[].class,
                                       null,
                                       new MediaType("text", "xml")));
        assertTrue(provider.isReadable(AtomFeed[].class,
                                       AtomFeed[].class,
                                       null,
                                       new MediaType("application", "xml")));
        assertTrue(provider.isReadable(AtomFeed[].class,
                                       AtomFeed[].class,
                                       null,
                                       new MediaType("application", "atom+xml")));
        assertTrue(provider.isReadable(AtomFeed[].class,
                                       AtomFeed[].class,
                                       null,
                                       new MediaType("application", "atomsvc+xml")));

        assertFalse(provider.isReadable(JAXBElement[].class,
                                        JAXBElement[].class,
                                        null,
                                        new MediaType("text", "xml")));
        assertFalse(provider.isReadable(JAXBElement[].class,
                                        JAXBElement[].class,
                                        null,
                                        new MediaType("application", "xml")));
        assertFalse(provider.isReadable(JAXBElement[].class,
                                        JAXBElement[].class,
                                        null,
                                        new MediaType("application", "atom+xml")));
        assertFalse(provider.isReadable(JAXBElement[].class,
                                        JAXBElement[].class,
                                        null,
                                        new MediaType("application", "atomsvc+xml")));

        assertFalse(provider.isReadable(String[].class, String[].class, null, new MediaType("text",
                                                                                            "xml")));
        assertFalse(provider.isReadable(String[].class,
                                        String[].class,
                                        null,
                                        new MediaType("application", "xml")));
        assertFalse(provider.isReadable(String[].class,
                                        String[].class,
                                        null,
                                        new MediaType("application", "atom+xml")));
        assertFalse(provider.isReadable(String[].class,
                                        String[].class,
                                        null,
                                        new MediaType("application", "atomsvc+xml")));
    }

    public void testJAXBCollectionXMLProviderIsWritableCollection() throws Exception {
        JAXBCollectionXmlProvider provider = new JAXBCollectionXmlProvider();

        GenericEntity<List<AtomFeed>> type1 =
            new GenericEntity<List<AtomFeed>>(new ArrayList<AtomFeed>()) {
            };
        assertTrue(provider.isWriteable(type1.getRawType(),
                                        type1.getType(),
                                        null,
                                        new MediaType("text", "xml")));
        assertTrue(provider.isWriteable(type1.getRawType(),
                                        type1.getType(),
                                        null,
                                        new MediaType("application", "xml")));
        assertTrue(provider.isWriteable(type1.getRawType(),
                                        type1.getType(),
                                        null,
                                        new MediaType("application", "atom+xml")));
        assertTrue(provider.isWriteable(type1.getRawType(),
                                        type1.getType(),
                                        null,
                                        new MediaType("application", "atomsvc+xml")));

        GenericEntity<List<JAXBElement<AtomFeed>>> type2 =
            new GenericEntity<List<JAXBElement<AtomFeed>>>(new ArrayList<JAXBElement<AtomFeed>>()) {
            };
        assertTrue(provider.isWriteable(type2.getRawType(),
                                        type2.getType(),
                                        null,
                                        new MediaType("text", "xml")));
        assertTrue(provider.isWriteable(type2.getRawType(),
                                        type2.getType(),
                                        null,
                                        new MediaType("application", "xml")));
        assertTrue(provider.isWriteable(type2.getRawType(),
                                        type2.getType(),
                                        null,
                                        new MediaType("application", "atom+xml")));
        assertTrue(provider.isWriteable(type2.getRawType(),
                                        type2.getType(),
                                        null,
                                        new MediaType("application", "atomsvc+xml")));

        GenericEntity<List<String>> type3 =
            new GenericEntity<List<String>>(new ArrayList<String>()) {
            };
        assertFalse(provider.isWriteable(type3.getRawType(),
                                         type3.getType(),
                                         null,
                                         new MediaType("text", "xml")));
        assertFalse(provider.isWriteable(type3.getRawType(),
                                         type3.getType(),
                                         null,
                                         new MediaType("application", "xml")));
        assertFalse(provider.isWriteable(type3.getRawType(),
                                         type3.getType(),
                                         null,
                                         new MediaType("application", "atom+xml")));
        assertFalse(provider.isWriteable(type3.getRawType(),
                                         type3.getType(),
                                         null,
                                         new MediaType("application", "atomsvc+xml")));
    }

    public void testJAXBCollectionXMLProviderIsWritableArray() throws Exception {
        JAXBArrayXmlProvider provider = new JAXBArrayXmlProvider();

        assertTrue(provider.isWriteable(AtomFeed[].class,
                                        AtomFeed[].class,
                                        null,
                                        new MediaType("text", "xml")));
        assertTrue(provider.isWriteable(AtomFeed[].class,
                                        AtomFeed[].class,
                                        null,
                                        new MediaType("application", "xml")));
        assertTrue(provider.isWriteable(AtomFeed[].class,
                                        AtomFeed[].class,
                                        null,
                                        new MediaType("application", "atom+xml")));
        assertTrue(provider.isWriteable(AtomFeed[].class,
                                        AtomFeed[].class,
                                        null,
                                        new MediaType("application", "atomsvc+xml")));

        assertFalse(provider.isWriteable(JAXBElement[].class,
                                         JAXBElement[].class,
                                         null,
                                         new MediaType("text", "xml")));
        assertFalse(provider.isWriteable(JAXBElement[].class,
                                         JAXBElement[].class,
                                         null,
                                         new MediaType("application", "xml")));
        assertFalse(provider.isWriteable(JAXBElement[].class,
                                         JAXBElement[].class,
                                         null,
                                         new MediaType("application", "atom+xml")));
        assertFalse(provider.isWriteable(JAXBElement[].class,
                                         JAXBElement[].class,
                                         null,
                                         new MediaType("application", "atomsvc+xml")));

        assertFalse(provider.isWriteable(String[].class,
                                         String[].class,
                                         null,
                                         new MediaType("text", "xml")));
        assertFalse(provider.isWriteable(String[].class,
                                         String[].class,
                                         null,
                                         new MediaType("application", "xml")));
        assertFalse(provider.isWriteable(String[].class,
                                         String[].class,
                                         null,
                                         new MediaType("application", "atom+xml")));
        assertFalse(provider.isWriteable(String[].class,
                                         String[].class,
                                         null,
                                         new MediaType("application", "atomsvc+xml")));
    }

    public void testJAXBElementCollection() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/jaxbelement",
                                                        "text/xml",
                                                        "text/xml",
                                                        SOURCE_REQUEST_BYTES);
        MockHttpServletResponse invoke = invoke(request);
        assertEquals(200, invoke.getStatus());
        
        // nested elements may or may not have a namespace prefix, so...
        String xml = invoke.getContentAsString();
        Pattern myPattern = Pattern.compile("feeds><\\w*?:?feed");
        Matcher matcher = myPattern.matcher(xml);
        matcher.find();
        // +6 to skip the "feeds>"
        String feedString = xml.substring(matcher.start() + 6, matcher.end());
        
        String[] elements =
            getElementsFromList("<feeds>", "</feeds>", feedString, invoke.getContentAsString());
        assertEquals(4, elements.length);

        AtomFeed feed = null;
        for (int i = 1; i < elements.length; ++i) {
            feed =
                (AtomFeed)((JAXBElement<?>)unmarshallElement(AtomFeed.class, feedString + elements[i]))
                    .getValue();
            assertEquals("" + (i - 1) + "10", feed.getId());
        }
    }

    public void testJAXBObjectCollection() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor
                .constructMockRequest("POST",
                                      "/jaxbresource/xmlrootwithfactorycollection",
                                      "text/xml",
                                      "text/xml",
                                      SOURCE_REQUEST_BYTES);
        MockHttpServletResponse invoke = invoke(request);
        assertEquals(200, invoke.getStatus());
        
        // nested elements may or may not have a namespace prefix, so...
        String xml = invoke.getContentAsString();
        Pattern myPattern = Pattern.compile("feeds><\\w*?:?feed");
        Matcher matcher = myPattern.matcher(xml);
        matcher.find();
        // +6 to skip the "feeds>"
        String feedString = xml.substring(matcher.start() + 6, matcher.end());

        String[] elements =
            getElementsFromList("<feeds>", "</feeds>", feedString, invoke.getContentAsString());
        assertEquals(4, elements.length);

        AtomFeed feed = null;
        for (int i = 1; i < elements.length; ++i) {
            feed =
                (AtomFeed)((JAXBElement<?>)unmarshallElement(AtomFeed.class, feedString + elements[i]))
                    .getValue();
            assertEquals("" + (i - 1) + "10", feed.getId());
        }
    }

    public void testJAXBObjectArray() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/xmlrootwithfactoryarray",
                                                        "text/xml",
                                                        "text/xml",
                                                        SOURCE_REQUEST_BYTES);
        MockHttpServletResponse invoke = invoke(request);
        assertEquals(200, invoke.getStatus());

        // nested elements may or may not have a namespace prefix, so...
        String xml = invoke.getContentAsString();
        Pattern myPattern = Pattern.compile("feeds><\\w*?:?feed");
        Matcher matcher = myPattern.matcher(xml);
        matcher.find();
        // +6 to skip the "feeds>"
        String feedString = xml.substring(matcher.start() + 6, matcher.end());
        
        String[] elements =
            getElementsFromList("<feeds>", "</feeds>", feedString, invoke.getContentAsString());
        assertEquals(4, elements.length);

        AtomFeed feed = null;
        for (int i = 1; i < elements.length; ++i) {
            feed =
                (AtomFeed)((JAXBElement<?>)unmarshallElement(AtomFeed.class, feedString + elements[i]))
                    .getValue();
            assertEquals("" + (i - 1) + "10", feed.getId());
        }
    }

    public void testXMLTypeNoFactoryCollection() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/jaxbresource/xmltypenofactorycollection",
                                                        "text/xml",
                                                        "text/xml",
                                                        null);
        MockHttpServletResponse invoke = invoke(request);
        assertEquals(200, invoke.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(JAXBXmlType_RESPONSE, invoke
                .getContentAsString());
        assertNull(msg, msg);
    }

    public void testXMLTypeNoFactoryArray() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/jaxbresource/xmltypenofactoryarray",
                                                        "text/xml",
                                                        "text/xml",
                                                        null);
        MockHttpServletResponse invoke = invoke(request);
        assertEquals(200, invoke.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(JAXBXmlType_RESPONSE, invoke
                .getContentAsString());
        assertNull(msg, msg);
    }

    public void testXMLTypeCollection() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/xmltypecollection",
                                                        "text/xml",
                                                        "text/xml",
                                                        JAXBXmlType_REQUEST_BYTES);
        MockHttpServletResponse invoke = invoke(request);
        assertEquals(200, invoke.getStatus());
        System.out.println(invoke.getContentAsString());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(JAXBXmlType_RESPONSE, invoke
                .getContentAsString());
        assertNull(msg, msg);
    }

    public void testXMLTypeArray() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/xmltypearray",
                                                        "text/xml",
                                                        "text/xml",
                                                        JAXBXmlType_REQUEST_BYTES);
        MockHttpServletResponse invoke = invoke(request);
        assertEquals(200, invoke.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(JAXBXmlType_RESPONSE, invoke
                .getContentAsString());
        assertNull(msg, msg);
    }

    public void testXmlRootCollection() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/xmlrootnofactorycollection",
                                                        "text/xml",
                                                        "text/xml",
                                                        JAXBXmlRootElement_BYTES);
        MockHttpServletResponse invoke = invoke(request);
        assertEquals(200, invoke.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(JAXBXmlRootElement_RESPONSE, invoke
                .getContentAsString());
        assertNull(msg, msg);
    }

    public void testXmlRootArray() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/xmlrootnofactoryarray",
                                                        "text/xml",
                                                        "text/xml",
                                                        JAXBXmlRootElement_BYTES);
        MockHttpServletResponse invoke = invoke(request);
        assertEquals(200, invoke.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(JAXBXmlRootElement_RESPONSE, invoke
                .getContentAsString());
        assertNull(msg, msg);
    }

    private static String[] getElementsFromList(String rootStartTag,
                                                String rootEndTag,
                                                String elementStart,
                                                String content) {
        assertTrue(content.indexOf(rootStartTag) != -1);
        assertTrue(content.indexOf(rootEndTag) != -1);
        content =
            content.substring(content.indexOf(rootStartTag) + rootStartTag.length(), content
                .indexOf(rootEndTag));
        return content.split(elementStart);
    }

    private static Object unmarshallElement(Class<?> type, String response) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(type);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return unmarshaller.unmarshal(new ByteArrayInputStream(response.getBytes()));
    }
}
