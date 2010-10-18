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
import java.util.List;

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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JAXBProvidersWithXmlAdapterTest extends MockServletInvocationTest {

    private static int adapterMarshalMethodCalled = 0;
    private static int adapterUnmarshalMethodCalled = 0;
    
    public static class MyRandomPojo {
        private int sum;
        public MyRandomPojo(int sum) {
            this.sum = sum;
        }
        public int getSum() {
            return sum;
        }
    }
    
    public static class MyRandomPojoAdapter extends XmlAdapter<AddNumbers,MyRandomPojo> {

        @Override
        public AddNumbers marshal(MyRandomPojo v) throws Exception {
            adapterMarshalMethodCalled++;
            AddNumbers addNumbers = new AddNumbers();
            addNumbers.setArg0(66);
            addNumbers.setArg1(99);
            return addNumbers;
        }

        @Override
        public MyRandomPojo unmarshal(AddNumbers v) throws Exception {
            adapterUnmarshalMethodCalled++;
            return new MyRandomPojo(v.getArg0() + v.getArg1());
        }
    }
    
    @XmlJavaTypeAdapter(AddNumbers.Adapter.class)
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
        
        public static class Adapter extends XmlAdapter<AddNumbers,IAddNumbers> {
            public IAddNumbers unmarshal(AddNumbers v) {
                adapterUnmarshalMethodCalled++;
                return v;
            }
            public AddNumbers marshal(IAddNumbers v) {
                adapterMarshalMethodCalled++;
                return (AddNumbers)v;
            }
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
        public IAddNumbers add(IAddNumbers addNumbers) {
            return addNumbers;
        }
        
        @POST
        @Path("jaxbelementarray")
        @Consumes(MediaType.APPLICATION_XML)
        @Produces(MediaType.APPLICATION_XML)
        public IAddNumbers[] addArray(IAddNumbers[] addNumbers) {
            return addNumbers;
        }
        
        @POST
        @Path("jaxbelementcollection")
        @Consumes(MediaType.APPLICATION_XML)
        @Produces(MediaType.APPLICATION_XML)
        public List<IAddNumbers> addCollection(List<IAddNumbers> addNumbers) {
            return addNumbers;
        }
        
        @POST
        @Path("jaxbtopojo")
        @Consumes(MediaType.APPLICATION_XML)
        @Produces(MediaType.APPLICATION_XML)
        public @XmlJavaTypeAdapter(MyRandomPojoAdapter.class) MyRandomPojo addCollection(@XmlJavaTypeAdapter(MyRandomPojoAdapter.class) MyRandomPojo myPojo) {
            return myPojo;
        }
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        adapterMarshalMethodCalled = 0;
        adapterUnmarshalMethodCalled = 0;
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
        
        // make sure the adapter was called the right number of times
        assertEquals(1, adapterMarshalMethodCalled);
        assertEquals(1, adapterUnmarshalMethodCalled);
    }
    
    public void testArray() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<addNumberss>"
            + "<addNumbers><arg0>2</arg0><arg1>3</arg1></addNumbers>"
            + "<addNumbers><arg0>4</arg0><arg1>5</arg1></addNumbers>"
            + "</addNumberss>";
        
        MockHttpServletRequest request = MockRequestConstructor
        .constructMockRequest("POST", "/jaxbresource/jaxbelementarray",
                MediaType.APPLICATION_XML, MediaType.APPLICATION_XML,
                xml.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        
        // make sure the round trip really was successful
        assertEquals(xml, response.getContentAsString());
        
        // make sure the adapter was called the right number of times
        assertEquals(2, adapterMarshalMethodCalled);
        assertEquals(2, adapterUnmarshalMethodCalled);
    }
    
    public void testCollection() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<addNumberss>"
            + "<addNumbers><arg0>2</arg0><arg1>3</arg1></addNumbers>"
            + "<addNumbers><arg0>4</arg0><arg1>5</arg1></addNumbers>"
            + "</addNumberss>";
        
        MockHttpServletRequest request = MockRequestConstructor
        .constructMockRequest("POST", "/jaxbresource/jaxbelementcollection",
                MediaType.APPLICATION_XML, MediaType.APPLICATION_XML,
                xml.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        
        // make sure the round trip really was successful
        assertEquals(xml, response.getContentAsString());
        
        // make sure the adapter was called the right number of times
        assertEquals(2, adapterMarshalMethodCalled);
        assertEquals(2, adapterUnmarshalMethodCalled);
    }
    
    public void testJAXBtoPojo() throws Exception {
        AddNumbers addNumbers = new AddNumbers();
        addNumbers.setArg0(2);
        addNumbers.setArg1(3);

        JAXBContext context = JAXBContext.newInstance(AddNumbers.class);
        Marshaller marshaller = context.createMarshaller();
        Writer writer = new StringWriter();
        
        marshaller.marshal(addNumbers, writer);

        MockHttpServletRequest request = MockRequestConstructor
        .constructMockRequest("POST", "/jaxbresource/jaxbtopojo",
                MediaType.APPLICATION_XML, MediaType.APPLICATION_XML,
                writer.toString().getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        
        // make sure the round trip really was successful
        context = JAXBContext.newInstance(AddNumbers.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        IAddNumbers ifaceAN = (IAddNumbers) unmarshaller.unmarshal(new ByteArrayInputStream(response.getContentAsByteArray()));
        assertEquals(66, ifaceAN.getArg0());
        assertEquals(99, ifaceAN.getArg1());
        
        // make sure the adapter was called the right number of times
        assertEquals(1, adapterMarshalMethodCalled);
        assertEquals(1, adapterUnmarshalMethodCalled);
    }

}
