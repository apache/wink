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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.StringTokenizer;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.xml.parsers.DocumentBuilder;
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

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.common.internal.providers.entity.SourceProvider;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class SourceProviderTest extends MockServletInvocationTest {

    private static final String SOURCE       =
                                                 "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message>this is a test message</message>";
    private static final byte[] SOURCE_BYTES = SOURCE.getBytes();
    private String TEST_CLASSES_PATH = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Mockery mockery = new Mockery();
        final RuntimeContext context = mockery.mock(RuntimeContext.class);
        mockery.checking(new Expectations() {{
            allowing(context).getAttribute(WinkConfiguration.class); will(returnValue(null));
        }});
        
        RuntimeContextTLS.setRuntimeContext(context);
    }
    
    @Override
    public void tearDown() {
        RuntimeContextTLS.setRuntimeContext(null);
    }

    private String getPath() {
        if (TEST_CLASSES_PATH == null) {
	        String classpath = System.getProperty("java.class.path");
	        StringTokenizer tokenizer = new StringTokenizer(classpath, System.getProperty("path.separator"));
	        TEST_CLASSES_PATH = null;
	        while (tokenizer.hasMoreTokens()) {
	            TEST_CLASSES_PATH = tokenizer.nextToken();
	            if (TEST_CLASSES_PATH.endsWith("test-classes")) {
	                break;
	            }
	        }
	        // for windows:
	        int driveIndex = TEST_CLASSES_PATH.indexOf(":");
	        if(driveIndex != -1) {
	            TEST_CLASSES_PATH = TEST_CLASSES_PATH.substring(driveIndex + 1);
	        }
        }
        return TEST_CLASSES_PATH;
    }
    
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
        @Path("saxwithdtd")
        public String postSaxWithDTD(SAXSource source) throws Exception {
            /*
             * we don't want to trigger a parse in this resource method.  We're testing to see what happened
             * with the SAXSource on the way here.
             */
            StringBuilder sb = new StringBuilder();
            String line;
            InputStream is = source.getInputSource().getByteStream();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        }

        @POST
        @Path("dom")
        public String postDom(DOMSource source) throws Exception {
            return extractXml(source);
        }
        
        @POST
        @Path("domwithdtd")
        public String postDomWithDTD(DOMSource source) throws Exception {
            /*
             * we don't want to trigger a parse in this resource method.  We're testing to see what happened
             * with the SAXSource on the way here.
             */
            return source.getNode().getFirstChild().getFirstChild().getTextContent();
        }

        private String extractXml(Source source) throws TransformerFactoryConfigurationError,
            TransformerConfigurationException, TransformerException {
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(source, sr);
            return sw.toString();
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
    
    public void testSaxWithDTD() throws Exception {
        
        String path = getPath();
        
        final String SOURCE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE data [<!ENTITY file SYSTEM \""+ path +"/etc/SourceProviderTest.txt\">]>" +
            "<message>&file;</message>";
        
        final byte[] SOURCE_BYTES = SOURCE.getBytes();
        
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/source/saxwithdtd",
                                                        "application/xml",
                                                        "application/xml",
                                                        SOURCE_BYTES);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertFalse("File content is visible but should not be.",
                response.getContentAsString().contains("YOU SHOULD NOT BE ABLE TO SEE THIS"));
        assertEquals(SOURCE, response.getContentAsString().trim());
    }
    
    public void testDomWithDTD() throws Exception {
        
        String path = getPath();
        
        final String SOURCE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE data [<!ENTITY file SYSTEM \""+ path +"/etc/SourceProviderTest.txt\">]>" +
            "<message>&file;</message>";

        final byte[] SOURCE_BYTES = SOURCE.getBytes();
        
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/source/domwithdtd",
                                                        "application/xml",
                                                        "application/xml",
                                                        SOURCE_BYTES);
        MockHttpServletResponse response = invoke(request);
        assertEquals(400, response.getStatus());
        
        // as a sanity check, let's make sure our xml is good:
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource( new StringReader(SOURCE) );
        Document d = builder.parse( is );
        assertEquals("xml is bad", "YOU SHOULD NOT BE ABLE TO SEE THIS", d.getElementsByTagName("message").item(0).getTextContent().trim());
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
