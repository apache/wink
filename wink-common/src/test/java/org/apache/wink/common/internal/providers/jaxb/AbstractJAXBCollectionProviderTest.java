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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.common.internal.contexts.MediaTypeCharsetAdjuster;
import org.apache.wink.common.internal.providers.entity.xml.JAXBCollectionXmlProvider;
import org.apache.wink.common.internal.providers.jaxb.jaxb1.AddNumbers;
import org.apache.wink.common.internal.providers.jaxb.jaxb1.MyPojo;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.model.JAXBUnmarshalOptions;
import org.apache.wink.common.model.XmlFormattingOptions;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.junit.After;
import org.junit.Before;
import org.w3c.dom.Document;


public class AbstractJAXBCollectionProviderTest extends MockObjectTestCase {
    
    public class MyJAXBXmlProvider extends JAXBCollectionXmlProvider {

        MyJAXBXmlProvider() {
            super();
            providers = AbstractJAXBCollectionProviderTest.this.providers;
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
    
    static final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<addNumberss>" +
        "<ns2:addNumbers xmlns:ns2=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<ns2:arg0>1</ns2:arg0>" +
        "<ns2:arg1>2</ns2:arg1>" +
        "</ns2:addNumbers>" +
        "<ns2:addNumbers xmlns:ns2=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<ns2:arg0>1</ns2:arg0>" +
        "<ns2:arg1>3</ns2:arg1>" +
        "</ns2:addNumbers>" +
        "</addNumberss>";

    static final String xmlMyPojo = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<myPojos>" +
        "<ns2:myPojo xmlns:ns2=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<ns2:stringdata>1</ns2:stringdata>" +
        "</ns2:myPojo>" +
        "<ns2:myPojo xmlns:ns2=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<ns2:stringdata>2</ns2:stringdata>" +
        "</ns2:myPojo>" +
        "</myPojos>";
    
    static final String xmlWithDTD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<!DOCTYPE data [<!ENTITY file SYSTEM \"file:"+ path +"/etc/ProvidersJAXBTest.txt\">]>" +
        "<addNumberss>" +
        "<ns2:addNumbers xmlns:ns2=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<ns2:arg0>&file;</ns2:arg0>" +
        "<ns2:arg1>2</ns2:arg1>" +
        "</ns2:addNumbers>" +
        "<ns2:addNumbers xmlns:ns2=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<ns2:arg0>&file;</ns2:arg0>" +
        "<ns2:arg1>3</ns2:arg1>" +
        "</ns2:addNumbers>" +
        "</addNumberss>";
    
    static final String xmlMyPojoWithDTD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<!DOCTYPE data [<!ENTITY file SYSTEM \"file:"+ path +"/etc/ProvidersJAXBTest.txt\">]>" +
        "<myPojos>" +
        "<ns2:myPojo xmlns:ns2=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<ns2:stringdata>&file;</ns2:stringdata>" +
        "</ns2:myPojo>" +
        "<ns2:myPojo xmlns:ns2=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<ns2:stringdata>&file;</ns2:stringdata>" +
        "</ns2:myPojo>" +
        "</myPojos>";

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
        final WinkConfiguration winkConfiguration = mock(WinkConfiguration.class);
        checking(new Expectations() {{
            allowing(providers).getContextResolver(JAXBContext.class, MediaType.TEXT_XML_TYPE); will(returnValue(new MyJAXBContextResolver()));
            allowing(providers).getContextResolver(XmlFormattingOptions.class, MediaType.TEXT_XML_TYPE); will(returnValue(null));
            allowing(providers).getContextResolver(JAXBUnmarshalOptions.class, MediaType.TEXT_XML_TYPE); will(returnValue(null));
            allowing(runtimeContext).getAttribute(MediaTypeCharsetAdjuster.class); will(returnValue(null));
            allowing(runtimeContext).getAttribute(WinkConfiguration.class); will(returnValue(winkConfiguration));
            allowing(winkConfiguration).getProperties(); will(returnValue(null));
        }});
        RuntimeContextTLS.setRuntimeContext(runtimeContext);
        jaxbProviderReader = new MyJAXBXmlProvider();
        jaxbProviderWriter = new MyJAXBXmlProvider();
        
    }
    
    @After
    public void tearDown() {
        // clean up the mess.
        RuntimeContextTLS.setRuntimeContext(null);
    }
    
    public void testXml() throws Exception {

        GenericEntity<List<AddNumbers>> type1 =
            new GenericEntity<List<AddNumbers>>(new ArrayList<AddNumbers>()) {
            };
            
        assertTrue(jaxbProviderReader.isReadable(type1.getRawType(), type1.getType(), null, MediaType.TEXT_XML_TYPE));
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        Object obj = jaxbProviderReader.readFrom(type1.getRawType(), type1.getType(), null, MediaType.TEXT_XML_TYPE, null, bais);
        assertTrue(obj instanceof ArrayList);
        ArrayList alist = (ArrayList)obj;
        // make sure the objects in the returned list are the actual jaxb objects, not JAXBElement, and the unmarshal worked:
        assertEquals(1, ((AddNumbers)alist.get(0)).getArg0());
        assertEquals(2, ((AddNumbers)alist.get(0)).getArg1());
        assertEquals(1, ((AddNumbers)alist.get(1)).getArg0());
        assertEquals(3, ((AddNumbers)alist.get(1)).getArg1());
    }
    
    public void testXmlWithDTD() throws Exception {

        GenericEntity<List<AddNumbers>> type1 =
            new GenericEntity<List<AddNumbers>>(new ArrayList<AddNumbers>()) {
            };
            
        assertTrue(jaxbProviderReader.isReadable(type1.getRawType(), type1.getType(), null, MediaType.TEXT_XML_TYPE));
        Exception ex = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlWithDTD.getBytes());
            Object obj = jaxbProviderReader.readFrom(type1.getRawType(), type1.getType(), null, MediaType.TEXT_XML_TYPE, null, bais);
            fail("should have got an exception");
        } catch (Exception e) {
            ex = e;
        }
        assertTrue("expected an XMLStreamException", ex.getCause() instanceof XMLStreamException);

        // parse it just as a sanity check to make sure our xml is good.  No exceptions means good xml!
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(new ByteArrayInputStream(xmlWithDTD.getBytes()));

    }
    
    public void testXmlMyPojo() throws Exception {

        GenericEntity<List<MyPojo>> type1 =
            new GenericEntity<List<MyPojo>>(new ArrayList<MyPojo>()) {
            };
            
        assertTrue(jaxbProviderReader.isReadable(type1.getRawType(), type1.getType(), null, MediaType.TEXT_XML_TYPE));
        ByteArrayInputStream bais = new ByteArrayInputStream(xmlMyPojo.getBytes());
        Object obj = jaxbProviderReader.readFrom(type1.getRawType(), type1.getType(), null, MediaType.TEXT_XML_TYPE, null, bais);
        assertTrue(obj instanceof ArrayList);
        ArrayList alist = (ArrayList)obj;
        // make sure the objects in the returned list are the actual jaxb objects, not JAXBElement, and the unmarshal worked:
        assertEquals("1", ((MyPojo)alist.get(0)).getStringdata());
        assertEquals("2", ((MyPojo)alist.get(1)).getStringdata());
    }
    
    public void testXmlMyPojoWithDTD() throws Exception {

        GenericEntity<List<MyPojo>> type1 =
            new GenericEntity<List<MyPojo>>(new ArrayList<MyPojo>()) {
            };
            
        assertTrue(jaxbProviderReader.isReadable(type1.getRawType(), type1.getType(), null, MediaType.TEXT_XML_TYPE));
        Exception ex = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlMyPojoWithDTD.getBytes());
            Object obj = jaxbProviderReader.readFrom(type1.getRawType(), type1.getType(), null, MediaType.TEXT_XML_TYPE, null, bais);
            fail("should have got an exception");
        } catch (Exception e) {
            ex = e;
        }
        assertTrue("expected an XMLStreamException", ex.getCause() instanceof XMLStreamException);

        // parse it just as a sanity check to make sure our xml is good.  No exceptions means good xml!
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(new ByteArrayInputStream(xmlMyPojoWithDTD.getBytes()));

    }
}
