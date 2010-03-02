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

import java.util.Random;

import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.wink.providers.json.JSONUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JacksonSerializationConfiguration2Test extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {Resource.class};
    }

    @Override
    protected Object[] getSingletons() {
        JacksonJsonProvider jacksonProvider = new JacksonJsonProvider();
        jacksonProvider
            .configure(org.codehaus.jackson.map.SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,
                       true);
        jacksonProvider
            .configure(org.codehaus.jackson.map.SerializationConfig.Feature.USE_STATIC_TYPING, true);
        return new Object[] {jacksonProvider};
    }

    @Path("/resource")
    public static class Resource {

        @GET
        @Path("empty")
        @Produces(MediaType.APPLICATION_JSON)
        public Empty getEmptyPOJO() {
            return new Empty();
        }

        @GET
        @Path("group")
        @Produces(MediaType.APPLICATION_JSON)
        public Group getMammalGroup(@QueryParam("num") int num) {
            Group group = new Group();
            Mammal leader;
            if (num == 0) {
                leader = new Dog();
                leader.setName("Fido");
                leader.setSpecies("Dog");
                ((Dog)leader).setBreed("Laborador");
            } else {
                leader = new Whale();
                leader.setName("Shamu");
                leader.setSpecies("Whale");
                ((Whale)leader).setToothed(true);

            }
            group.setLeader(leader);
            return group;
        }
    }

    public static class Empty {

    }

    public static class Group {
        public Mammal leader;

        public Mammal getLeader() {
            return leader;
        }

        public void setLeader(Mammal leader) {
            this.leader = leader;
        }

    }

    public static class Mammal {
        private String species;
        private String name;

        public String getSpecies() {
            return species;
        }

        public void setSpecies(String species) {
            this.species = species;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public static class Dog extends Mammal {
        public String breed;

        public String getBreed() {
            return breed;
        }

        public void setBreed(String breed) {
            this.breed = breed;
        }

    }

    public static class Whale extends Mammal {
        public boolean isToothed;

        public boolean isToothed() {
            return isToothed;
        }

        public void setToothed(boolean isToothed) {
            this.isToothed = isToothed;
        }

    }

    public void testGETEmpty() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/resource/empty",
                                                        MediaType.APPLICATION_JSON);
        try {
            invoke(request);
            fail("ServletException was not thrown for empty bean.");
        } catch (ServletException e) {
        }
    }

    public void testGETStaticTyping() throws Exception {
        Random r = new Random();
        int num = r.nextInt(2);
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/resource/group",
                                                        MediaType.APPLICATION_JSON);
        request.setQueryString("num=" + num);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        if (num == 0)
            assertTrue(JSONUtils
                .equals(new JSONObject("{\"leader\":{\"species\":\"Dog\", \"name\":\"Fido\"}}"),
                        new JSONObject(response.getContentAsString())));
        else
            assertTrue(JSONUtils
                .equals(new JSONObject("{\"leader\":{\"species\":\"Whale\", \"name\":\"Shamu\"}}"),
                        new JSONObject(response.getContentAsString())));
    }
}
