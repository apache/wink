package org.apache.wink.providers.thrift;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.example.tutorial.Person;

/**
 * Tests the Apache Thrift provider.
 */
public class ThriftTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {PersonResource.class};
    }

    @Override
    protected Object[] getSingletons() {
        return new Object[] {
        // new WinkThriftProvider()
        };
    }

    private static Person createPerson() {
        return new Person().setId(1).setEmail("abc@example.com").setName("John Smith");
    }

    @Path("/test/person")
    public static class PersonResource {

        @GET
        public Person getPerson() throws IOException {
            return createPerson();
        }

        @POST
        public Person postPerson(Person p) {
            return p;
        }
    }

    /**
     * Tests a simple single JAXB Object to write.
     * 
     * @throws Exception
     */
    public void testGetPerson() throws Exception {
        testGet(WinkThriftProvider.THRIFT_TYPE);
    }

    private void assertEquals(Person p) {
        Assert.assertEquals("abc@example.com", p.getEmail());
        Assert.assertEquals(1, p.getId());
        Assert.assertEquals("John Smith", p.getName());
    }

    /**
     * Tests a simple single JAXB Object to both read and write.
     * 
     * @throws Exception
     */
    public void testPostPerson() throws Exception {
        testPost(WinkThriftProvider.THRIFT_TYPE);
    }

    /**
     * Tests a simple single JAXB Object to write.
     * 
     * @throws Exception
     */
    public void testGetPersonJSON() throws Exception {
        MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
        testGet(mediaType);
    }

    private void testGet(MediaType mediaType) throws ServletException, IOException, Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test/person", mediaType.toString());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        byte[] data = response.getContentAsByteArray();
        Person p = WinkThriftProvider.unmarshal(Person.class, mediaType, new ByteArrayInputStream(data));
        assertEquals(p);
    }

    /**
     * Tests a simple single JAXB Object to both read and write.
     * 
     * @throws Exception
     */
    public void testPostPersonJSON() throws Exception {
        MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
        testPost(mediaType);
    }

    private void testPost(MediaType mediaType) throws IOException, ServletException, Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST", "/test/person", mediaType.toString());
        request.setContentType(mediaType.toString());
        Person p = createPerson();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        WinkThriftProvider.marshal(p, mediaType, bos);
        request.setContent(bos.toByteArray());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        byte[] data = response.getContentAsByteArray();
        Person p1 = WinkThriftProvider.unmarshal(Person.class, mediaType, new ByteArrayInputStream(data));
        assertEquals(p1);
    }

}
