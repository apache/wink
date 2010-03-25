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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Intent of this class is to exercise the JAXBContext cache in the ProvidersRegistry.
 */
public class JAXBProviderInheritanceTest extends MockServletInvocationTest {

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "arg0",
        "arg1"
    })
    @XmlRootElement(name = "addNumbers")
    public static class AddNumbers {

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
    
    @XmlRootElement
    public static class AddIntsAnnotated extends AddNumbers {}
    
    public static class AddIntsNotAnnotated extends AddNumbers {}
    
    
    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {Resource.class, ResourceInherited.class};
    }

    @Path("jaxbresource")
    public static class Resource {

        @POST
        @Path("jaxbelement")
        public String add(AddNumbers addNumbers) {
            return String.valueOf(addNumbers.getArg0() + addNumbers.getArg1());
        }
    }
    
    @Path("jaxbresourceinherited")
    public static class ResourceInherited {

        @POST
        @Path("jaxbelement")
        public String add(AddIntsAnnotated addInts) {
            return String.valueOf(addInts.getArg0() + addInts.getArg1());
        }
        
        @GET
        @Path("getannotated")
        public Response getAnnotated() {
            AddIntsAnnotated addInts = new AddIntsAnnotated();
            addInts.setArg0(11);
            addInts.setArg1(12);
           return Response.ok(new GenericEntity<AddNumbers>(addInts) {}).build();
        }
        
        @GET
        @Path("getnotannotated")
        public Response getNotAnnotated() {
            AddIntsNotAnnotated addInts = new AddIntsNotAnnotated();
            addInts.setArg0(15);
            addInts.setArg1(16);
           return Response.ok(new GenericEntity<AddNumbers>(addInts) {}).build();
        }
    }
    

    public void testNormal() throws Exception {
        AddNumbers addNumbers = new AddNumbers();
        addNumbers.setArg0(2);
        addNumbers.setArg1(3);

        JAXBContext context = JAXBContext.newInstance(AddNumbers.class);
        Marshaller marshaller = context.createMarshaller();
        Writer writer = new StringWriter();
        marshaller.marshal(addNumbers, writer);

        MockHttpServletRequest request = MockRequestConstructor
        .constructMockRequest("POST", "/jaxbresource/jaxbelement",
                MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML,
                writer.toString().getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("5", response.getContentAsString());
    }
    
    public void testInheritance() throws Exception {
        AddIntsAnnotated addInts = new AddIntsAnnotated();
        addInts.setArg0(2);
        addInts.setArg1(3);

        // JAXBContext can handle inherited types
        JAXBContext context = JAXBContext.newInstance(AddIntsAnnotated.class);
        Marshaller marshaller = context.createMarshaller();
        Writer writer = new StringWriter();
        marshaller.marshal(addInts, writer);

        MockHttpServletRequest request = MockRequestConstructor
        .constructMockRequest("POST", "/jaxbresourceinherited/jaxbelement",
                MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML,
                writer.toString().getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("5", response.getContentAsString());
    }
    
    public void testGenericEntityAnnotatedJAXB() throws Exception {
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "/jaxbresourceinherited/getannotated", MediaType.APPLICATION_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        ByteArrayInputStream bais = new ByteArrayInputStream(response.getContentAsByteArray());
        
        JAXBContext context = JAXBContext.newInstance(AddIntsAnnotated.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        AddIntsAnnotated addInts = (AddIntsAnnotated)unmarshaller.unmarshal(bais);
        
        assertEquals(11, addInts.getArg0());
        assertEquals(12, addInts.getArg1());
    }
    
    public void testGenericEntityNotAnnotatedJAXB() throws Exception {
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "/jaxbresourceinherited/getnotannotated", MediaType.APPLICATION_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        ByteArrayInputStream bais = new ByteArrayInputStream(response.getContentAsByteArray());
        
        JAXBContext context = JAXBContext.newInstance(new Class[]{AddIntsAnnotated.class, AddNumbers.class});
        Unmarshaller unmarshaller = context.createUnmarshaller();
        // JAXB cannot unmarshal to AddIntsNotAnnotated because there is no @XmlRootElement annotation on it, so...
        AddNumbers addNumbers = (AddNumbers)unmarshaller.unmarshal(bais);
        
        assertEquals(15, addNumbers.getArg0());
        assertEquals(16, addNumbers.getArg1());
    }
    
}
