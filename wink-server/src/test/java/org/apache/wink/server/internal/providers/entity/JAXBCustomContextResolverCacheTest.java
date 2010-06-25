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

import java.io.StringWriter;
import java.io.Writer;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.wink.common.internal.providers.entity.xml.JAXBElementXmlProvider;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Intent of this class is to exercise the JAXBContext cache in the ProvidersRegistry.
 */
public class JAXBCustomContextResolverCacheTest extends MockServletInvocationTest {

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
    
    public static class MyJAXBElementXmlProvider extends JAXBElementXmlProvider {
        public static void setContextCacheOff() {
            contextCacheOn = false;
        }
        public static void setContextCacheOn() {
            contextCacheOn = true;
        }
    }
    
    static int cacheMisses = 0;
    
    @Provider
    @Produces( MediaType.APPLICATION_XML)
    public static class XMLContextResolver implements ContextResolver<JAXBContext> {

        public JAXBContext getContext(Class arg0) {
            try {
                cacheMisses++;
                return JAXBContext.newInstance(arg0.getPackage().getName());
            } catch (JAXBException e) {
                return null;
            }
        }
    }
    
    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {Resource.class, XMLContextResolver.class, MyJAXBElementXmlProvider.class};
    }

    @Path("jaxbresource")
    public static class Resource {

        @POST
        @Path("jaxbelement")
        public String add(AddNumbers addNumbers) {
            return String.valueOf(addNumbers.getArg0() + addNumbers.getArg1());
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        cacheMisses = 0;
        super.setUp();
    }
    
    // CAUTION: don't use these methods as an accurate measure of performance.  That's not their purpose, and there's
    // too many other things going on underneath anyway, such as junit, logging, etc.
    
    public void testCustomResolverCacheOff() throws Exception {
        // users will set the "org.apache.wink.jaxbcontextcache" to "on" or "off"
        MyJAXBElementXmlProvider.setContextCacheOff();
        // must loop a few times to get an accurate count of cache misses with cache turned off
        int expectedCacheMisses = 100;
        for (int i = 0; i < expectedCacheMisses; i++) {
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
        assertEquals(expectedCacheMisses, cacheMisses);
    }
    
    public void testCustomResolverCacheOn() throws Exception {
     // users will set the "org.apache.wink.jaxbcontextcache" to "on" or "off"
        MyJAXBElementXmlProvider.setContextCacheOn();
        // must loop at least twice to initialize and exercise the underlying JAXBContext cache and get accurate count of cacheMisses
        int loop = 100;
        for (int i = 0; i < loop; i++) {
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
        // there will be some cache misses due to the garbage collector cleaning up the SoftConcurrentMap
        // cache, but certainly the number of misses will be lower than the number of times through the loop.
        System.out.println("loops = " + loop + ", cacheMisses = " + cacheMisses);
        
        /*
         * NOTE: original test used the following assert.  However, there was indeed aggressive garbage collection, and
         * unittests shouldn't be doing performance analysis anyway, so now we just make sure the cache was used.
         */
        // kind of a guess, but if we're getting 20% cache misses, then we have a VERY aggressive garbage collector
        //assertTrue("expected: " + (loop/5) + " > " + cacheMisses, (loop/5) > cacheMisses);
        
        // just make sure the cache was used:
        assertTrue("expected: " + (loop - 1) + " > " + cacheMisses, (loop - 1) > cacheMisses);
        
    }
}
