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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class GenericInheritanceProviderTest extends MockServletInvocationTest {

    private static boolean reachedNirvana = false;
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name="MyJAXBObject", propOrder = {"stringdata"})
    @XmlRootElement(name = "myJAXBObject")
    public static class MyJAXBObject {
        private String stringdata;
        public MyJAXBObject() {}
        public void setStringdata(String stringdata) {
            this.stringdata = stringdata;
        }
        public String getStringdata() {
            return stringdata;
        }
    }
    
    public static class Foo {
        String x;
        
        public Foo(String x) {
            this.x = x;
        }

        public String getValue() {
            return x + "foo";
        }
    }
    
    public static class Bar extends Foo {
        
        public Bar(String x) {
            super(x);
        }
        
        public String getValue() {
            return x + "bar";
        }
    }
    
    public static interface GenericService<T, X extends Foo> {
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Path("{id1}/{id2}")
        public void doSomething(@PathParam("id1") int id1, @PathParam("id2") X id2, T obj);
    }

    @Path("/impl")
    public static class GenericServiceImpl implements GenericService<MyJAXBObject, Bar> {
        public void doSomething(int id, Bar id2, MyJAXBObject obj) {
            reachedNirvana = true;
        }
    }
    
    @Provider
    @Consumes(MediaType.APPLICATION_JSON)
    public static class JSONtoJAXBProvider implements MessageBodyReader {

        public boolean isReadable(Class type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return mediaType.equals(MediaType.APPLICATION_JSON_TYPE);
        }

        public Object readFrom(Class type, Type genericType,
                Annotation[] annotations, MediaType mediaType,
                MultivaluedMap httpHeaders, InputStream entityStream)
                throws IOException {
            // for test purposes, I don't really care about how this conversion is actually done, just
            // that Wink can navigate its way through the right paths and providers
            return new MyJAXBObject();
        }
        
    }
    
    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {GenericServiceImpl.class, JSONtoJAXBProvider.class};
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        reachedNirvana = false;
    }
    
    @Test
    public void test() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/impl/5/6",
                                                        MediaType.TEXT_PLAIN,
                                                        MediaType.APPLICATION_JSON,
                                                        "{stringdata: \"hi\"}".getBytes());
        invoke(request);
        assertTrue(reachedNirvana);
    }

}
