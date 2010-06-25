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
package org.apache.wink.common.internal.providers.jaxb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.CaseInsensitiveMultivaluedMap;
import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.common.internal.contexts.MediaTypeCharsetAdjuster;
import org.apache.wink.common.internal.providers.entity.xml.JAXBXmlProvider;
import org.apache.wink.common.internal.providers.jaxb.jaxb1.AddNumbers;
import org.apache.wink.common.internal.providers.jaxb.jaxb1.MyPojo;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.model.JAXBUnmarshalOptions;
import org.apache.wink.common.model.XmlFormattingOptions;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProvidersJAXBTest extends MockObjectTestCase {
    
    public class MyJAXBXmlProvider extends JAXBXmlProvider {

        MyJAXBXmlProvider() {
            super();
            providers = ProvidersJAXBTest.this.providers;
        }
        
        /* 
         * simulate what would happen if application had supplied a JAXBContext provider
         */
        @Override
        protected JAXBContext getContext(Class<?> type, MediaType mediaType)
                throws JAXBException {
            // use JAXBContext.newInstance(String).  The default in AbstractJAXBProvider is JAXBContext.newInstance(Class)
            return JAXBContext.newInstance(type.getPackage().getName());
        }
        
    }
    
    static final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<ns2:addNumbers xmlns:ns2=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<ns2:arg0>1</ns2:arg0>" +
        "<ns2:arg1>2</ns2:arg1>" +
        "</ns2:addNumbers>";
    
    static final String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + 
        "<addNumbers xmlns=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<arg0>0</arg0>" +
        "<arg1>0</arg1>" +
        "</addNumbers>";

    static String path = null;
    static {
        String classpath = System.getProperty("java.class.path");
        StringTokenizer tokenizer = new StringTokenizer(classpath, System.getProperty("path.separator"));
        while (tokenizer.hasMoreTokens()) {
            path = tokenizer.nextToken();
            if (path.endsWith("test-classes")) {
                break;
            }
        }
        // for windows:
        int driveIndex = path.indexOf(":");
        if(driveIndex != -1) {
            path = path.substring(driveIndex + 1);
        }

    }
    
    static final String xmlWithDTD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<!DOCTYPE data [<!ENTITY file SYSTEM \"file:"+ path + "/etc/ProvidersJAXBTest.txt\">]>" +
        "<ns2:addNumbers xmlns:ns2=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<ns2:arg0>&file;</ns2:arg0>" +
        "<ns2:arg1>2</ns2:arg1>" +
        "</ns2:addNumbers>";
    
    static final String xmlMyPojoWithDTD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<!DOCTYPE data [<!ENTITY file SYSTEM \"file:"+ path +"/etc/ProvidersJAXBTest.txt\">]>" +
        "<ns2:myPojo xmlns:ns2=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<ns2:stringdata>&file;</ns2:stringdata>" +
        "</ns2:myPojo>";

    private WinkConfiguration winkConfiguration = null;
    private MessageBodyReader jaxbProviderReader = null;
    private MessageBodyWriter jaxbProviderWriter = null;
    private Providers providers;
    
    public class MyJAXBContextResolver implements ContextResolver<JAXBContext> {

        public JAXBContext getContext(Class<?> arg0) {
            try {
                return JAXBContext.newInstance(arg0);
            } catch (JAXBException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
    
    @Before
    public void setUp() {
        providers = mock(Providers.class);
        final RuntimeContext runtimeContext = mock(RuntimeContext.class);
        winkConfiguration = mock(WinkConfiguration.class);
        checking(new Expectations() {{
            allowing(providers).getContextResolver(JAXBContext.class, MediaType.TEXT_XML_TYPE); will(returnValue(new MyJAXBContextResolver()));
            allowing(providers).getContextResolver(XmlFormattingOptions.class, MediaType.TEXT_XML_TYPE); will(returnValue(null));
            allowing(providers).getContextResolver(JAXBUnmarshalOptions.class, MediaType.TEXT_XML_TYPE); will(returnValue(null));
            allowing(runtimeContext).getAttribute(MediaTypeCharsetAdjuster.class); will(returnValue(null));
            allowing(runtimeContext).getAttribute(WinkConfiguration.class); will(returnValue(winkConfiguration));
        }});
        RuntimeContextTLS.setRuntimeContext(runtimeContext);
        jaxbProviderReader = new MyJAXBXmlProvider();
        jaxbProviderWriter = new MyJAXBXmlProvider();
        
    }
    
    @After
    public void tearDown() {
        // clean up the mess.  :)
        RuntimeContextTLS.setRuntimeContext(null);
    }
    
    @Test
    public void testJAXBUnmarshallingWithAlternateContext1() throws Exception {
        
        final Properties props = new Properties();
        checking(new Expectations() {{
            allowing(winkConfiguration).getProperties(); will(returnValue(props));
        }});
        
        // let's make sure our test string is unmarshallable, just for a good sanity check
        JAXBContext testcontext = JAXBContext.newInstance(AddNumbers.class);
        Unmarshaller testunmarshaller = testcontext.createUnmarshaller();
        AddNumbers testresponse =
            (AddNumbers)testunmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
        assertEquals("we could not unmarshal the test xml", 1, testresponse.getArg0());
        
        assertTrue(jaxbProviderReader.isReadable(AddNumbers.class, null, null, MediaType.TEXT_XML_TYPE));
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        Object obj = jaxbProviderReader.readFrom(AddNumbers.class, null, null, MediaType.TEXT_XML_TYPE, null, bais);
        assertTrue(obj instanceof AddNumbers);
        assertEquals(1, ((AddNumbers)obj).getArg0());
        assertEquals(2, ((AddNumbers)obj).getArg1());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testJAXBMarshalling() throws WebApplicationException, IOException {
        assertTrue(jaxbProviderWriter.isWriteable(AddNumbers.class, null, null, MediaType.TEXT_XML_TYPE));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jaxbProviderWriter.writeTo(new AddNumbers(), AddNumbers.class, null, null, MediaType.TEXT_XML_TYPE, null, baos);
        assertEquals(expectedXml, baos.toString());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testJAXBMarshallingWithMap() throws WebApplicationException, IOException {
        assertTrue(jaxbProviderWriter.isWriteable(AddNumbers.class, null, null, MediaType.TEXT_XML_TYPE));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MultivaluedMap map = new CaseInsensitiveMultivaluedMap<Object>();
        jaxbProviderWriter.writeTo(new AddNumbers(), AddNumbers.class, null, null, MediaType.TEXT_XML_TYPE, map, baos);
        assertEquals(expectedXml, baos.toString());
    }
    
    @Test
    public void testJAXBUnmarshallingWithDTD() throws Exception {
        
        final Properties props = new Properties();
        checking(new Expectations() {{
            allowing(winkConfiguration).getProperties(); will(returnValue(props));
        }});
        
        Exception ex = null;
        assertTrue(jaxbProviderReader.isReadable(AddNumbers.class, null, null, MediaType.TEXT_XML_TYPE));
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlWithDTD.getBytes());
            Object obj = jaxbProviderReader.readFrom(AddNumbers.class, null, null, MediaType.TEXT_XML_TYPE, null, bais);
            fail("should have got an exception");
        } catch (Exception e) {
            ex = e;
        }
        assertTrue("expected an XMLStreamException", ex.getCause() instanceof XMLStreamException);
        
        // let's make sure our test string is unmarshallable, just for a good sanity check
        JAXBContext testcontext = JAXBContext.newInstance(AddNumbers.class);
        Unmarshaller testunmarshaller = testcontext.createUnmarshaller();
        AddNumbers testresponse =
            (AddNumbers)testunmarshaller.unmarshal(new ByteArrayInputStream(xmlWithDTD.getBytes()));
        assertEquals("we could not unmarshal the test xml", 99999999, testresponse.getArg0());
    }
    
    @Test
    /**
     * testing that supporting DTD expansion is configurable
     */
    public void testJAXBUnmarshallingWithDTDServerConfigurable() throws Exception {
        
        // pretend we're on the server:
        final Properties props = new Properties();
        props.put("wink.supportDTDEntityExpansion", "true");
        checking(new Expectations() {{
            allowing(winkConfiguration).getProperties(); will(returnValue(props));
        }});
        
        assertTrue(jaxbProviderReader.isReadable(AddNumbers.class, null, null, MediaType.TEXT_XML_TYPE));
        ByteArrayInputStream bais = new ByteArrayInputStream(xmlWithDTD.getBytes());
        Object obj = jaxbProviderReader.readFrom(AddNumbers.class, null, null, MediaType.TEXT_XML_TYPE, null, bais);
        assertTrue(obj instanceof AddNumbers);
        assertEquals(99999999, ((AddNumbers)obj).getArg0());
        assertEquals(2, ((AddNumbers)obj).getArg1());
        
        // let's make sure our test string is unmarshallable, just for a good sanity check
        JAXBContext testcontext = JAXBContext.newInstance(AddNumbers.class);
        Unmarshaller testunmarshaller = testcontext.createUnmarshaller();
        AddNumbers testresponse =
            (AddNumbers)testunmarshaller.unmarshal(new ByteArrayInputStream(xmlWithDTD.getBytes()));
        assertEquals("we could not unmarshal the test xml", 99999999, testresponse.getArg0());
    }
    
    @Test
    public void testJAXBUnmarshallingMyPojoWithDTD() throws Exception {

        final Properties props = new Properties();
        checking(new Expectations() {{
            allowing(winkConfiguration).getProperties(); will(returnValue(props));
        }});
        
        Exception ex = null;
        try {
            assertTrue(jaxbProviderReader.isReadable(MyPojo.class, null, null, MediaType.TEXT_XML_TYPE));
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlMyPojoWithDTD.getBytes());
            Object obj = jaxbProviderReader.readFrom(MyPojo.class, null, null, MediaType.TEXT_XML_TYPE, null, bais);
            fail("should have got an exception");
        } catch (Exception e) {
            ex = e;
        }
        assertTrue("expected an XMLStreamException", ex.getCause() instanceof XMLStreamException);
        
        // let's make sure our test string is unmarshallable, just for a good sanity check
        JAXBContext testcontext = JAXBContext.newInstance(MyPojo.class);
        Unmarshaller testunmarshaller = testcontext.createUnmarshaller();
        JAXBElement<MyPojo> testresponse =
            (JAXBElement<MyPojo>)testunmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(xmlMyPojoWithDTD.getBytes())), MyPojo.class);
        MyPojo myPojo = testresponse.getValue();
        assertEquals("we could not unmarshal the test xml", "99999999", myPojo.getStringdata().trim());
    }

}
