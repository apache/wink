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
import java.io.ByteArrayOutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.wink.common.internal.providers.entity.SourceProvider;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.xml.sax.InputSource;

public class SourceProviderTest extends MockServletInvocationTest {

    private static final String SOURCE       =
                                                 "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message>this is a test message</message>";
    private static final byte[] SOURCE_BYTES = SOURCE.getBytes();

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {SourceResource.class};
    }

    @Path("source")
    public static class SourceResource {

        private static final DocumentBuilderFactory documentBuilderFactory =
                                                                               DocumentBuilderFactory
                                                                                   .newInstance();
        private static final TransformerFactory     transformerFactory     =
                                                                               TransformerFactory
                                                                                   .newInstance();

        @GET
        @Path("stream")
        public Source getStream() {
            return new StreamSource(new ByteArrayInputStream(SOURCE_BYTES));
        }

        @GET
        @Path("sax")
        public Source getSax() {
            return new SAXSource(new InputSource(new ByteArrayInputStream(SOURCE_BYTES)));
        }

        @GET
        @Path("dom")
        public Source getDom() throws Exception {
            return new DOMSource(documentBuilderFactory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(SOURCE_BYTES)));
        }

        @POST
        @Path("stream")
        public String postStream(StreamSource source) throws Exception {
            return extractXml(source);
        }

        @POST
        @Path("sax")
        public String postSax(SAXSource source) throws Exception {
            return extractXml(source);
        }

        @POST
        @Path("dom")
        public String postDom(DOMSource source) throws Exception {
            return extractXml(source);
        }

        private String extractXml(Source source) throws TransformerFactoryConfigurationError,
            TransformerConfigurationException, TransformerException {
            Transformer transformer = transformerFactory.newTransformer();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StreamResult sr = new StreamResult(outputStream);
            transformer.transform(source, sr);
            return outputStream.toString();
        }

    }

    public void testSourceProvider() throws Exception {
        // stream source
        SourceProvider provider = new SourceProvider.StreamSourceProvider();
        Source source = assertSourceReader(provider, StreamSource.class);
        assertSourceWriter(provider, source);

        // sax source
        provider = new SourceProvider.SAXSourceProvider();
        source = assertSourceReader(provider, SAXSource.class);
        assertSourceWriter(provider, source);

        // dom source
        provider = new SourceProvider.DOMSourceProvider();
        source = assertSourceReader(provider, DOMSource.class);
        assertSourceWriter(provider, source);
    }

    public void testSourceProviderInvocation() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/source/stream", "text/xml");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(SOURCE, response.getContentAsString());

        request = MockRequestConstructor.constructMockRequest("GET", "/source/sax", "text/xml");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(SOURCE, response.getContentAsString());

        request = MockRequestConstructor.constructMockRequest("GET", "/source/dom", "text/xml");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        // Ignore Xml declaration, since in 1.6 Transformer always generates xml
        // with "standalone" attribute
        // when DOMSource is serialized
        assertEqualsEgnoreXmlDecl(SOURCE, response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/source/stream",
                                                        "text/xml",
                                                        "text/xml",
                                                        SOURCE_BYTES);
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(SOURCE, response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/source/sax",
                                                        "text/xml",
                                                        "text/xml",
                                                        SOURCE_BYTES);
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(SOURCE, response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/source/dom",
                                                        "text/xml",
                                                        "text/xml",
                                                        SOURCE_BYTES);
        response = invoke(request);
        assertEquals(200, response.getStatus());
        // Ignore Xml declaration, since in 1.6 Transformer always generates xml
        // with "standalone" attribute
        // when DOMSource is serialized
        assertEqualsEgnoreXmlDecl(SOURCE, response.getContentAsString());

    }

    // -- Helpers

    private void assertSourceWriter(SourceProvider provider, Source source) throws Exception {
        assertTrue(provider
            .isWriteable(source.getClass(), null, null, new MediaType("text", "xml")));
        assertTrue(provider.isWriteable(source.getClass(), null, null, new MediaType("application",
                                                                                     "xml")));
        assertTrue(provider.isWriteable(source.getClass(), null, null, new MediaType("application",
                                                                                     "atom+xml")));
        assertTrue(provider
            .isWriteable(source.getClass(), null, null, new MediaType("application", "atomsvc+xml")));
        assertFalse(provider.isWriteable(source.getClass(), null, null, new MediaType("text",
                                                                                      "plain")));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        provider.writeTo(source, null, null, null, new MediaType("text", "xml"), null, os);
        // Ignore Xml declaration, since in 1.6 Transformer always generates xml
        // with "standalone" attribute
        // when DOMSource is serialized
        assertEqualsEgnoreXmlDecl(SOURCE, os.toString());
    }

    @SuppressWarnings("unchecked")
    private Source assertSourceReader(SourceProvider provider, Class<?> sourceClass)
        throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(SOURCE.getBytes("UTF-8"));
        MessageBodyReader<Source> bodyReader = (MessageBodyReader<Source>)provider;
        assertTrue(bodyReader.isReadable(sourceClass, null, null, new MediaType("text", "xml")));
        assertTrue(bodyReader.isReadable(sourceClass, null, null, new MediaType("application",
                                                                                "xml")));
        assertTrue(bodyReader.isReadable(sourceClass, null, null, new MediaType("application",
                                                                                "atom+xml")));
        assertTrue(bodyReader.isReadable(sourceClass, null, null, new MediaType("application",
                                                                                "atomsvc+xml")));
        assertFalse(bodyReader.isReadable(sourceClass, null, null, new MediaType("text", "plain")));

        Source source =
            bodyReader.readFrom((Class<Source>)sourceClass,
                                null,
                                null,
                                new MediaType("text", "xml"),
                                null,
                                inputStream);
        assertNotNull(source);
        assertEquals(sourceClass, source.getClass());
        return source;
    }

    private void assertEqualsEgnoreXmlDecl(String expected, String actual) {
        expected = removeXmlDecl(expected);
        actual = removeXmlDecl(actual);
        assertEquals(expected, actual);
    }

    private String removeXmlDecl(String expected) {
        if (expected.indexOf("<?xml") >= 0) {
            expected = expected.substring(expected.indexOf("?>"));
        }
        return expected;
    }

}
