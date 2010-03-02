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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.providers.json.JSONUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JacksonDeserializationConfiguration3Test extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {Resource.class};
    }

    @Override
    protected Object[] getSingletons() {
        JacksonJsonProvider jacksonProvider = new JacksonJsonProvider();
        jacksonProvider
            .configure(org.codehaus.jackson.map.SerializationConfig.Feature.USE_ANNOTATIONS, false);
        jacksonProvider
            .configure(org.codehaus.jackson.map.DeserializationConfig.Feature.USE_ANNOTATIONS,
                       false);
        return new Object[] {jacksonProvider};
    }

    @Path("resource")
    public static class Resource {

        @POST
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        public Shape postShape(Shape shape) {
            return shape;
        }
    }

    public static class Shape {
        private int    numSides;
        private String name;

        public int getNumSides() {
            return numSides;
        }

        @JsonIgnore
        public void setNumSides(int numSides) {
            this.numSides = numSides;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public void testPOSTShape() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/resource",
                                                        MediaType.APPLICATION_JSON);
        request.setContentType(MediaType.APPLICATION_JSON);
        request.setContent("{\"numSides\":4, \"name\":\"square\"}".getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        System.out.println(response.getContentAsString());
        assertTrue(JSONUtils.equals(new JSONObject("{\"numSides\":4, \"name\":\"square\"}"),
                                    new JSONObject(response.getContentAsString())));
    }
}
