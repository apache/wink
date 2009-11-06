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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.wink.common.internal.providers.entity.xml.JAXBXmlProvider;
import org.apache.wink.common.internal.providers.jaxb.jaxb1.AddNumbers;
import org.junit.Before;
import org.junit.Test;

public class ProvidersJAXBTest {
    
    public class MyJAXBXmlProvider extends JAXBXmlProvider {

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
        "<arg0>1</arg0>" +
        "<arg1>2</arg1>" +
        "</ns2:addNumbers>";
    
    private MessageBodyReader jaxbProvider = null;
    
    @Before
    public void setUp() {
        jaxbProvider = new MyJAXBXmlProvider();
    }
    
    @Test
    public void testJAXBUnmarshallingWithAlternateContext1() throws Exception {
        assertTrue(jaxbProvider.isReadable(AddNumbers.class, null, null, MediaType.TEXT_XML_TYPE));
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        Object obj = jaxbProvider.readFrom(AddNumbers.class, null, null, MediaType.TEXT_XML_TYPE, null, bais);
        assertTrue(obj instanceof AddNumbers);
    }
    
}
