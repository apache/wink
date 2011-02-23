package org.apache.wink.providers.protobuf;

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

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import junit.framework.Assert;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.example.tutorial.AddressBookProtos.Person;

/**
 * Tests the ProtocolBuffer provider.
 */
public class ProtobufTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {PersonResource.class};
    }

    @Override
    protected Object[] getSingletons() {
        return new Object[] {new WinkProtobufProvider()};
    }

    private static Person createPerson() {
        return Person.newBuilder().setId(1).setEmail("abc@example.com").setName("John Smith").build();
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
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test/person", "application/x-protobuf");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        byte[] data = response.getContentAsByteArray();
        Person p = Person.parseFrom(data);
        assertEquals(p);
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
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST", "/test/person", "application/x-protobuf");
        request.setContentType("application/x-protobuf");
        Person p = createPerson();
        request.setContent(p.toByteArray());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        byte[] data = response.getContentAsByteArray();
        Person p1 = Person.parseFrom(data);
        assertEquals(p1);
    }

}
