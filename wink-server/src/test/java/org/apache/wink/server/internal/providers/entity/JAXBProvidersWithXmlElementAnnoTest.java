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
import java.io.StringWriter;
import java.io.Writer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JAXBProvidersWithXmlElementAnnoTest extends MockServletInvocationTest {

    public static interface IAddNumbers {
        public int getArg0();
        public void setArg0(int value);
        public int getArg1();
        public void setArg1(int value);
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "arg0",
        "arg1"
    })
    @XmlRootElement(name = "addNumbers")
    public static class AddNumbers implements IAddNumbers {

        protected int arg0;
        protected int arg1;

        /**
         * Gets the value of the arg0 property.
         * 
         */
        public int getArg0() {
            return arg0;
        }

        /**
         * Sets the value of the arg0 property.
         * 
         */
        public void setArg0(int value) {
            this.arg0 = value;
        }

        /**
         * Gets the value of the arg1 property.
         * 
         */
        public int getArg1() {
            return arg1;
        }

        /**
         * Sets the value of the arg1 property.
         * 
         */
        public void setArg1(int value) {
            this.arg1 = value;
        }
        
    }
    

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {Resource.class};
    }

    @Path("jaxbresource")
    public static class Resource {

        @POST
        @Path("jaxbelement")
        @Consumes(MediaType.APPLICATION_XML)
        @Produces(MediaType.APPLICATION_XML)
        public @XmlElement(type=AddNumbers.class) IAddNumbers add(@XmlElement(type=AddNumbers.class) IAddNumbers addNumbers) {
            return addNumbers;
        }
        
    }
    
    public void test1() throws Exception {
        AddNumbers addNumbers = new AddNumbers();
        addNumbers.setArg0(2);
        addNumbers.setArg1(3);

        JAXBContext context = JAXBContext.newInstance(AddNumbers.class);
        Marshaller marshaller = context.createMarshaller();
        Writer writer = new StringWriter();
        
        marshaller.marshal(addNumbers, writer);

        MockHttpServletRequest request = MockRequestConstructor
        .constructMockRequest("POST", "/jaxbresource/jaxbelement",
                MediaType.APPLICATION_XML, MediaType.APPLICATION_XML,
                writer.toString().getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        
        // make sure the round trip really was successful
        context = JAXBContext.newInstance(AddNumbers.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        IAddNumbers ifaceAN = (IAddNumbers) unmarshaller.unmarshal(new ByteArrayInputStream(response.getContentAsByteArray()));
        assertEquals(2, ifaceAN.getArg0());
        assertEquals(3, ifaceAN.getArg1());
        
    }
    
}
