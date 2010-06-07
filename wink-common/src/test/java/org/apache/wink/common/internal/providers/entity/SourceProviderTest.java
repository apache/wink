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
package org.apache.wink.common.internal.providers.entity;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.common.internal.contexts.MediaTypeCharsetAdjuster;
import org.apache.wink.common.internal.providers.entity.SourceProvider.DOMSourceProvider;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.model.JAXBUnmarshalOptions;
import org.apache.wink.common.model.XmlFormattingOptions;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SourceProviderTest extends MockObjectTestCase {

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
        "<!DOCTYPE data [<!ENTITY file SYSTEM \"file:"+ path +"/etc/SourceProviderTest.txt\">]>" +
        "<ns2:messages xmlns:ns2=\"http://org/apache/wink/common/internal/providers/jaxb/jaxb1\">" +
        "<ns2:arg0>&file;</ns2:arg0>" +
        "</ns2:messages>";
    

    private WinkConfiguration winkConfiguration = null;
    private MessageBodyReader sourceProviderReader = null;
    private Providers providers;
    
    @Before
    public void setUp() {
        providers = mock(Providers.class);
        final RuntimeContext runtimeContext = mock(RuntimeContext.class);
        winkConfiguration = mock(WinkConfiguration.class);
        checking(new Expectations() {{
            allowing(providers).getContextResolver(XmlFormattingOptions.class, MediaType.TEXT_XML_TYPE); will(returnValue(null));
            allowing(providers).getContextResolver(JAXBUnmarshalOptions.class, MediaType.TEXT_XML_TYPE); will(returnValue(null));
            allowing(runtimeContext).getAttribute(MediaTypeCharsetAdjuster.class); will(returnValue(null));
            allowing(runtimeContext).getAttribute(WinkConfiguration.class); will(returnValue(winkConfiguration));
        }});
        RuntimeContextTLS.setRuntimeContext(runtimeContext);
        sourceProviderReader = new DOMSourceProvider();
        
    }
    
    @After
    public void tearDown() {
        RuntimeContextTLS.setRuntimeContext(null);
    }

    
    @Test
    /**
     * testing that supporting DTD expansion is configurable
     */
    public void testJAXBUnmarshallingWithDTDServerConfigurable() throws Exception {
        
        final Properties props = new Properties();
        props.put("wink.supportDTDEntityExpansion", "true");
        checking(new Expectations() {{
            allowing(winkConfiguration).getProperties(); will(returnValue(props));
        }});
        
        assertTrue(sourceProviderReader.isReadable(DOMSource.class, null, null, MediaType.TEXT_XML_TYPE));
        ByteArrayInputStream bais = new ByteArrayInputStream(xmlWithDTD.getBytes());
        Object obj = sourceProviderReader.readFrom(DOMSource.class, null, null, MediaType.TEXT_XML_TYPE, null, bais);
        assertTrue(obj instanceof DOMSource);
    }
    @Test
    public void testStreamSourceProviderIsReadable() {
        // Entity Stream to be read with SourceProvider
        SourceProvider.StreamSourceProvider sp = new SourceProvider.StreamSourceProvider();

        // Check if readable - assert true
        assertTrue(sp.isReadable(Source.class, null, null, MediaType.APPLICATION_XML_TYPE));
        assertTrue(sp.isReadable(StreamSource.class, null, null, MediaType.TEXT_XML_TYPE));
    }
}
