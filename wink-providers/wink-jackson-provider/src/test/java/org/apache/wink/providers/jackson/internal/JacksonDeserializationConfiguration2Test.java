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

package org.apache.wink.providers.jackson.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JacksonDeserializationConfiguration2Test extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {Resource.class};
    }
    
    @Override
    protected Object[] getSingletons() {
        JacksonJsonProvider jacksonProvider = new JacksonJsonProvider();
        jacksonProvider.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS, false);
        jacksonProvider.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        jacksonProvider.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS, true);
        return new Object[] {jacksonProvider};
    }
    
    @Path("/resource")
    public static class Resource {
        
        @POST
        @Path("personwithchildren")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public List<String> postPersonWithChildren(Person person) {
            return person.getChildren();
        }
        
        @POST
        @Path("personwithweight")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public void postPersonWithAgeWeight(Person person) {
            Object weight = person.getWeight();
            if(!(weight instanceof BigDecimal))
                throw new WebApplicationException();
            Object age = person.getAge();
            if(!(age instanceof BigInteger))
                throw new WebApplicationException();
        }
    }
    
    public static class Person {
        private List<String> children = new ArrayList<String>();
        private Object weight;
        private Object age;

        public Object getAge() {
            return age;
        }

        public void setAge(Object age) {
            this.age = age;
        }

        public List<String> getChildren() {
            return children;
        }

        public Object getWeight() {
            return weight;
        }

        public void setWeight(Object weight) {
            this.weight = weight;
        }
    }
    
    public void testPOSTPersonWithChildren() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/resource/personwithchildren",
                                                        MediaType.APPLICATION_JSON);
        request.setContentType(MediaType.APPLICATION_JSON);
        request.setContent("{\"children\":[\"Joe\",\"Sally\",\"Steve\"]}".getBytes());
        // No jackson 1.9.x support the List without setter
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }
    
    public void testPOSTPersonWithAgeWeight() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/resource/personwithweight",
                                                        MediaType.APPLICATION_JSON);
        request.setContentType(MediaType.APPLICATION_JSON);
        request.setContent("{\"weight\":160.333, \"age\":27}".getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(204, response.getStatus());
    }
}
