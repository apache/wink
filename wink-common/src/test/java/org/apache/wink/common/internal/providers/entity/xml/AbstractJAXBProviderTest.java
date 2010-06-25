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
package org.apache.wink.common.internal.providers.entity.xml;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.junit.After;
import org.junit.Before;

/**
 * this test class is relatively new compared to the various
 * XML provider tests here in wink-common and wink-server.
 * Hence there are not as many tests as you might expect.  I'm
 * confident in the amount of testing being done in other places.
 *
 */
public class AbstractJAXBProviderTest extends MockObjectTestCase {

    /*
     * this test class is relatively new compared to the various
     * XML provider tests here in wink-common and wink-server.
     * Hence there are not as many tests as you might expect.  I'm
     * confident in the amount of testing being done in other places.
     */
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "message")
    public static class Message {

        protected String arg0;

        /**
         * Gets the value of the arg0 property.
         * 
         */
        public String getArg0() {
            return arg0;
        }

        /**
         * Sets the value of the arg0 property.
         * 
         */
        public void setArg0(String value) {
            this.arg0 = value;
        }

    }
    
    static String TEST_CLASSES_PATH = null;
    static {
        String classpath = System.getProperty("java.class.path");
        StringTokenizer tokenizer = new StringTokenizer(classpath, System.getProperty("path.separator"));
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
    
    public WinkConfiguration winkConfiguration = null;
    
    @Before
    public void setUp() {
        winkConfiguration = mock(WinkConfiguration.class);
        final RuntimeContext runtimeContext = mock(RuntimeContext.class);
        checking(new Expectations() {{
            allowing(runtimeContext).getAttribute(WinkConfiguration.class); will(returnValue(winkConfiguration));
        }});
        RuntimeContextTLS.setRuntimeContext(runtimeContext);
    }
    
    @After
    public void tearDown() {
        // clean up the mess.  :)
        RuntimeContextTLS.setRuntimeContext(null);
    }
    
    public void testGetXMLStreamReader1() throws Exception {
        
        final Properties props = new Properties();
        checking(new Expectations() {{
            allowing(winkConfiguration).getProperties(); will(returnValue(props));
        }});
        
        // check for infinite loop, empty document
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        try {
            AbstractJAXBProvider.getXMLStreamReader(bais);
            fail("should have got an exception");
        } catch (XMLStreamException e) {
            // any other exception type will cause test failure
        }
    }
    
    public void testGetXMLStreamReader2() throws Exception {
        
        final Properties props = new Properties();
        checking(new Expectations() {{
            allowing(winkConfiguration).getProperties(); will(returnValue(props));
        }});
        
        // check for infinite loop, comment only
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><!-- comment -->";
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        try {
            AbstractJAXBProvider.getXMLStreamReader(bais);
            fail("should have got an exception");
        } catch (XMLStreamException e) {
            // any other exception type will cause test failure
        }
    }
    
    public void testGetXMLStreamReader3() throws Exception {
        
        final Properties props = new Properties();
        checking(new Expectations() {{
            allowing(winkConfiguration).getProperties(); will(returnValue(props));
        }});
        
        // check for infinite loop, whitespace and comment
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>  <!-- comment -->";
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        try {
            AbstractJAXBProvider.getXMLStreamReader(bais);
            fail("should have got an exception");
        } catch (XMLStreamException e) {
            // any other exception type will cause test failure
        }
    }
    
    public void testGetXMLStreamReader4() throws Exception {
        
        final Properties props = new Properties();
        checking(new Expectations() {{
            allowing(winkConfiguration).getProperties(); will(returnValue(props));
        }});
        
        // check for infinite loop, whitespace, comment, and whitespace
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>  <!-- comment -->   ";
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        try {
            AbstractJAXBProvider.getXMLStreamReader(bais);
            fail("should have got an exception");
        } catch (XMLStreamException e) {
            // any other exception type will cause test failure
        }
    }
    
    public void testGetXMLStreamReaderWithDTDUnsupported() throws Exception {
        
        final Properties props = new Properties();
        checking(new Expectations() {{
            allowing(winkConfiguration).getProperties(); will(returnValue(props));
        }});
        
        // check for infinite loop, whitespace, comment, whitespace, and DOCTYPE unsupported
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>  <!-- comment -->   " + 
        "<!DOCTYPE data [<!ENTITY file SYSTEM \"file:"+ TEST_CLASSES_PATH +"/etc/ProvidersJAXBTest.txt\">]>" +
        "<message>&file;</message>";
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        try {
            AbstractJAXBProvider.getXMLStreamReader(bais);
            fail("should have got an exception");
        } catch (XMLStreamException e) {
            // any other exception type will cause test failure
        }
    }
    
    public void testGetXMLStreamReaderWithDTDSupported() throws Exception {
        
        final Properties props = new Properties();
        props.put("wink.supportDTDEntityExpansion", "true");
        checking(new Expectations() {{
            allowing(winkConfiguration).getProperties(); will(returnValue(props));
        }});
        
        // check for infinite loop, whitespace, comment, whitespace, and DOCTYPE supported
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>  <!-- comment -->   " + 
        "<!DOCTYPE data [<!ENTITY file SYSTEM \"file:"+ TEST_CLASSES_PATH +"/etc/ProvidersJAXBTest.txt\">]>" +
        "<message><arg0>&file;</arg0></message>";
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = AbstractJAXBProvider.getXMLStreamReader(bais);
        assertEquals("reader event should be start element", XMLStreamReader.START_ELEMENT, reader.getEventType());
        
        JAXBContext context = JAXBContext.newInstance(Message.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Message message = (Message)unmarshaller.unmarshal(reader);
        
        assertEquals("message object should contain text from ProvidersJAXBTest.txt file", "99999999", message.getArg0().trim());
    }
    
}
