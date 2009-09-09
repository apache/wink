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
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.wink.common.annotations.Asset;
import org.apache.wink.common.internal.providers.entity.xml.JAXBElementXmlProvider;
import org.apache.wink.common.internal.providers.entity.xml.JAXBXmlProvider;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.atom.ObjectFactory;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JAXBElementProviderTest extends MockServletInvocationTest {

    private static final String SOURCE_REQUEST              =
                                                                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><feed xmlns:ns2=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns=\"http://www.w3.org/2005/Atom\" ><id>ID</id></feed>";
    private static final byte[] SOURCE_REQUEST_BYTES        = SOURCE_REQUEST.getBytes();
    private static final String SOURCE_RESPONSE             =
                                                                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><feed xmlns:ns2=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns=\"http://www.w3.org/2005/Atom\"><id>ID</id></feed>";

    private static final String JAXBXmlRootElement_REQUEST  =
                                                                "<JAXBXmlRootElement><id>ID</id><name>NAME</name></JAXBXmlRootElement>";
    private static final byte[] JAXBXmlRootElement_BYTES    = JAXBXmlRootElement_REQUEST.getBytes();
    private static final String JAXBXmlRootElement_RESPONSE =
                                                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + "<JAXBXmlRootElement>\r\n"
                                                                    + "    <id>ID</id>\r\n"
                                                                    + "    <name>NAME</name>\r\n"
                                                                    + "</JAXBXmlRootElement>";

    private static final String JAXBXmlType_REQUEST         =
                                                                "<JAXBXmlType><id>ID</id><name>NAME</name></JAXBXmlType>";
    private static final byte[] JAXBXmlType_REQUEST_BYTES   = JAXBXmlType_REQUEST.getBytes();
    private static final String JAXBXmlType_RESPONSE        =
                                                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + "<JAXBXmlType>\r\n"
                                                                    + "    <id>ID</id>\r\n"
                                                                    + "    <name>NAME</name>\r\n"
                                                                    + "</JAXBXmlType>";

    JAXBElement<AtomFeed>       atomfeed;

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {Resource.class};
    }

    @Path("jaxbresource")
    public static class Resource {

        @POST
        @Path("jaxbelement")
        public JAXBElement<AtomFeed> createWraped(JAXBElement<AtomFeed> feed) {
            AtomFeed atomFeed = new AtomFeed();
            atomFeed.setId(feed.getValue().getId());
            JAXBElement<AtomFeed> wrapedFeed = new ObjectFactory().createFeed(atomFeed);
            return wrapedFeed;
        }

        @POST
        @Path("xmlrootwithfactory")
        public AtomFeed createBare(AtomFeed feed) {
            AtomFeed atomFeed = new AtomFeed();
            atomFeed.setId(feed.getId());
            return atomFeed;
        }

        @POST
        @Path("xmltype")
        public JAXBElement<JAXBXmlType> createxmltype(JAXBXmlType resource) {
            JAXBXmlType s = new JAXBXmlType();
            s.setId(resource.getId());
            s.setName(resource.getName());
            JAXBElement<JAXBXmlType> element =
                new JAXBElement<JAXBXmlType>(new QName("JAXBXmlType"), JAXBXmlType.class, resource);
            return element;
        }

        @POST
        @Path("xmlrootnofactory")
        public JAXBXmlRootElement createXmlRoot(JAXBXmlRootElement resource) {
            JAXBXmlRootElement s = new JAXBXmlRootElement();
            s.setId(resource.getId());
            s.setName(resource.getName());
            return s;
        }

        @GET
        @Path("xmltypenofactory")
        public JAXBXmlType getXmltypeNoFactory() {
            JAXBXmlType s = new JAXBXmlType();
            s.setId("ID");
            s.setName("NAME");
            return s;
        }

        @POST
        @Path("xmlasset")
        public TestAsset getAsset(TestAsset asset) {
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

    public void testJAXBElementProviderReaderWriter() throws SecurityException,
        NoSuchFieldException {

        JAXBElementXmlProvider provider = new JAXBElementXmlProvider();
        AtomFeed af = new AtomFeed();
        org.apache.wink.common.model.atom.ObjectFactory objectFactory =
            new org.apache.wink.common.model.atom.ObjectFactory();
        JAXBElement<AtomFeed> feed = objectFactory.createFeed(af);
        Type genericType =
            JAXBElementProviderTest.class.getDeclaredField("atomfeed").getGenericType();

        assertTrue(provider.isWriteable(feed.getClass(), genericType, null, new MediaType("text",
                                                                                          "xml")));
        assertTrue(provider.isWriteable(feed.getClass(),
                                        genericType,
                                        null,
                                        new MediaType("application", "xml")));
        assertTrue(provider.isWriteable(feed.getClass(),
                                        genericType,
                                        null,
                                        new MediaType("application", "atom+xml")));
        assertTrue(provider.isWriteable(feed.getClass(),
                                        genericType,
                                        null,
                                        new MediaType("application", "atomsvc+xml")));
        assertFalse(provider
            .isWriteable(feed.getClass(), genericType, null, new MediaType("text", "plain")));

        assertTrue(provider.isReadable(feed.getClass(), genericType, null, new MediaType("text",
                                                                                         "xml")));
        assertTrue(provider.isReadable(feed.getClass(),
                                       genericType,
                                       null,
                                       new MediaType("application", "xml")));
        assertTrue(provider.isReadable(feed.getClass(),
                                       genericType,
                                       null,
                                       new MediaType("application", "atom+xml")));
        assertTrue(provider.isReadable(feed.getClass(),
                                       genericType,
                                       null,
                                       new MediaType("application", "atomsvc+xml")));
        assertFalse(provider.isReadable(feed.getClass(), genericType, null, new MediaType("text",
                                                                                          "plain")));
    }

    @SuppressWarnings("unchecked")
    public void testJAXBElementProviderInvocation() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/jaxbelement",
                                                        "text/xml",
                                                        "text/xml",
                                                        SOURCE_REQUEST_BYTES);
        MockHttpServletResponse invoke = invoke(request);
        assertEquals(200, invoke.getStatus());

        JAXBContext context = JAXBContext.newInstance(AtomFeed.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        JAXBElement<AtomFeed> expectedResponse =
            (JAXBElement<AtomFeed>)unmarshaller.unmarshal(new ByteArrayInputStream(SOURCE_RESPONSE
                .getBytes()));
        JAXBElement<AtomFeed> response =
            (JAXBElement<AtomFeed>)unmarshaller.unmarshal(new ByteArrayInputStream(invoke
                .getContentAsByteArray()));
        assertEquals(expectedResponse.getValue().getId(), response.getValue().getId());
    }

    @SuppressWarnings("unchecked")
    public void testJAXBXmlElementProviderInvocation() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/xmlrootwithfactory",
                                                        "text/xml",
                                                        "text/xml",
                                                        SOURCE_REQUEST_BYTES);
        MockHttpServletResponse invoke = invoke(request);
        assertEquals(200, invoke.getStatus());

        JAXBContext context = JAXBContext.newInstance(AtomFeed.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        JAXBElement<AtomFeed> expectedResponse =
            (JAXBElement<AtomFeed>)unmarshaller.unmarshal(new ByteArrayInputStream(SOURCE_RESPONSE
                .getBytes()));
        JAXBElement<AtomFeed> response =
            (JAXBElement<AtomFeed>)unmarshaller.unmarshal(new ByteArrayInputStream(invoke
                .getContentAsByteArray()));
        assertEquals(expectedResponse.getValue().getId(), response.getValue().getId());
    }

    public void testJAXBXmlElementProviderInvocationXmltypeNoFactory() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/jaxbresource/xmltypenofactory",
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

    public void testJAXBXmlElementProviderInvocationXmltype() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/xmltype",
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

    public void testJAXBXmlElementProviderInvocationXmlRoot() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/xmlrootnofactory",
                                                        "text/xml",
                                                        "text/xml",
                                                        JAXBXmlRootElement_BYTES);
        MockHttpServletResponse invoke = invoke(request);
        assertEquals(200, invoke.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(JAXBXmlRootElement_RESPONSE, invoke
                .getContentAsString());
        assertNull(msg, msg);

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/xmlrootnofactory",
                                                        "text/xml",
                                                        "text/xml",
                                                        "<BadRequest/>".getBytes());
        invoke = invoke(request);
        assertEquals(400, invoke.getStatus());
    }

    public void testJAXBXmlElementProviderInvocationXmlAsset() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jaxbresource/xmlasset",
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

    public void testJAXBXmlElementProviderReaderWriter() throws SecurityException,
        NoSuchFieldException {
        JAXBXmlProvider provider = new JAXBXmlProvider();
        AtomFeed af = new AtomFeed();

        assertTrue(provider.isWriteable(af.getClass(), null, null, new MediaType("text", "xml")));
        assertTrue(provider.isWriteable(af.getClass(), null, null, new MediaType("application",
                                                                                 "xml")));
        assertTrue(provider.isWriteable(af.getClass(), null, null, new MediaType("application",
                                                                                 "atom+xml")));
        assertTrue(provider.isWriteable(af.getClass(), null, null, new MediaType("application",
                                                                                 "atomsvc+xml")));
        assertFalse(provider.isWriteable(af.getClass(), null, null, new MediaType("text", "plain")));

        assertTrue(provider.isReadable(af.getClass(), null, null, new MediaType("text", "xml")));
        assertTrue(provider.isReadable(af.getClass(), null, null, new MediaType("application",
                                                                                "xml")));
        assertTrue(provider.isReadable(af.getClass(), null, null, new MediaType("application",
                                                                                "atom+xml")));
        assertTrue(provider.isReadable(af.getClass(), null, null, new MediaType("application",
                                                                                "atomsvc+xml")));
        assertFalse(provider.isReadable(af.getClass(), null, null, new MediaType("text", "plain")));
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

}
