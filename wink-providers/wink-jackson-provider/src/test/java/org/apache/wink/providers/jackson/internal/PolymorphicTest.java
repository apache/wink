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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.providers.jackson.WinkJacksonJaxbJsonProvider;
import org.apache.wink.providers.jackson.internal.jaxb.polymorphic.MyJAXBObject;
import org.apache.wink.providers.jackson.internal.jaxb.polymorphic.MyProperties;
import org.apache.wink.providers.jackson.internal.pojo.polymorphic.Animal;
import org.apache.wink.providers.jackson.internal.pojo.polymorphic.Dog;
import org.apache.wink.providers.json.JSONUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class PolymorphicTest extends MockServletInvocationTest {

    WinkJacksonJaxbJsonProvider winkJacksonProvider = new WinkJacksonJaxbJsonProvider();
    
    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyJAXBObjectResource.class, AnimalResource.class};
    }

    @Override
    protected Object[] getSingletons() {
        return new Object[] {winkJacksonProvider};
    }

    @Path("/test/myproperties")
    public static class MyJAXBObjectResource {

        @GET
        public MyJAXBObject getMyJAXBObject() throws IOException {
            MyJAXBObject p = new MyJAXBObject();
            MyProperties myProps = new MyProperties();
            myProps.addProperty("I rock?", "Yes, yes I do.");
            p.setConfiguration(myProps);
            return p;
        }

    }
    
    @Path("/test/animal")
    public static class AnimalResource {

        @GET
        @Produces("application/json")
        public Animal getDog() throws IOException {
            Animal animal = new Dog();
            return animal;
        }

    }
    
    @Test
    public void testGetMyProperties() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test/myproperties", "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());

        String expectedResponseString = "{\"config\":{\"properties\":{\"I rock?\":\"Yes, yes I do.\"}}}";
                
        assertTrue(JSONUtils.equals(new JSONObject(expectedResponseString),
                                    new JSONObject(response.getContentAsString())));
        assertTrue(JSONUtils.equals(JSONUtils.objectForString(expectedResponseString), JSONUtils
                .objectForString(response.getContentAsString())));
        
        // call the provider as though the wink-client was in use on the client side
        InputStream is = new ByteArrayInputStream(response.getContentAsByteArray());
        MyJAXBObject myJAXBObject = (MyJAXBObject)winkJacksonProvider.readFrom(Object.class, MyJAXBObject.class, null, MediaType.APPLICATION_JSON_TYPE, null, is);

        // make sure the Jackson deserializer is using the 'type' property on the XmlElement annotation in MyJAXBObject
        // confirm Jackson deserialized to expected object type -- support for this was added in Jackson 1.4
        assertTrue(myJAXBObject.getConfiguration() instanceof MyProperties);
    }
    
    @Test
    public void testGetMyPropertiesJaxbProvider() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test/myproperties", "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());

        String expectedResponseString = "{\"config\":{\"properties\":{\"I rock?\":\"Yes, yes I do.\"}}}";
                
        assertTrue(JSONUtils.equals(new JSONObject(expectedResponseString),
                                    new JSONObject(response.getContentAsString())));
        assertTrue(JSONUtils.equals(JSONUtils.objectForString(expectedResponseString), JSONUtils
                .objectForString(response.getContentAsString())));
        
        // call the provider as though the wink-client was in use on the client side
        InputStream is = new ByteArrayInputStream(response.getContentAsByteArray());
        // use JacksonJaxbJsonProvider with default configuration instead of the old JacksonJsonProvider
        JacksonJaxbJsonProvider jacksonJAXBProvider = new JacksonJaxbJsonProvider();
        MyJAXBObject myJAXBObject = (MyJAXBObject)jacksonJAXBProvider.readFrom(Object.class, MyJAXBObject.class, null, MediaType.APPLICATION_JSON_TYPE, null, is);

        // make sure the Jackson deserializer is using the 'type' property on the XmlElement annotation in MyJAXBObject
        // confirm Jackson deserialized to expected object type -- support for this was added in Jackson 1.4
        assertTrue(myJAXBObject.getConfiguration() instanceof MyProperties);
    }
    
    @Test
    public void testGetAnimal() throws Exception {
        
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test/animal", "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        
        // make sure @JsonIgnore is honored
        String expectedResponseString = "{\"type\":\"dog\"}";
        assertTrue(JSONUtils.equals(new JSONObject(expectedResponseString),
                new JSONObject(response.getContentAsString())));
        
        // call the provider as though the wink-client was in use on the client side
        InputStream is = new ByteArrayInputStream(response.getContentAsByteArray());
        JacksonJsonProvider jacksonProvider = new JacksonJsonProvider();
        Animal animal = (Animal)jacksonProvider.readFrom(Object.class, Animal.class, null, MediaType.APPLICATION_JSON_TYPE, null, is);
        
        // make sure pseudo polymorphism support works.  See Animal class with @JsonCreator and @JsonProperty annotations
        assertEquals(Dog.class, animal.getClass());

    }
}
