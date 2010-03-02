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

import javax.servlet.ServletException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.springframework.mock.web.MockHttpServletRequest;

public class JacksonDeserializationConfigurationTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {Resource.class};
    }

    @Override
    protected Object[] getSingletons() {
        JacksonJsonProvider jacksonProvider = new JacksonJsonProvider();
        jacksonProvider
            .configure(org.codehaus.jackson.map.DeserializationConfig.Feature.AUTO_DETECT_SETTERS,
                       false);
        jacksonProvider
            .configure(org.codehaus.jackson.map.DeserializationConfig.Feature.AUTO_DETECT_FIELDS,
                       false);
        return new Object[] {jacksonProvider};
    }

    @Path("/resource")
    public static class Resource {

        @POST
        @Path("personsetters")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public PersonSetters postPersonSetters(PersonSetters person) {
            return person;
        }

        @POST
        @Path("personfields")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public PersonFields postPersonFields(PersonFields person) {
            return person;
        }

        @POST
        @Path("personchildren")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public PersonSetters postChildren(PersonSetters person) {
            return person;
        }
    }

    public static class PersonSetters {
        private String  first;
        private String  last;
        private boolean awake = false;

        public boolean isAwake() {
            return awake;
        }

        public void setAwake(boolean awake) {
            this.awake = awake;
        }

        public String nickname;

        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        public String getLast() {
            return last;
        }

        public void setLast(String last) {
            this.last = last;
        }
    }

    public static class PersonFields {
        public String  first;
        public String  last;
        public boolean awake = false;
        public String  nickname;
    }

    public void testPOSTPersonSetters() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/resource/personsetters",
                                                        MediaType.APPLICATION_JSON);
        request.setContentType(MediaType.APPLICATION_JSON);
        request
            .setContent("{\"first\":\"firstName\", \"last\":\"lastName\", \"awake\":true, \"nickname\":\"Bill\"}"
                .getBytes());
        try {
            invoke(request);
            fail("ServletException was not thrown when specifying fields that are not detected.");
        } catch (ServletException e) {
        }
    }

    public void testPOSTPersonFields() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/resource/personfields",
                                                        MediaType.APPLICATION_JSON);
        request.setContentType(MediaType.APPLICATION_JSON);
        request
            .setContent("{\"first\":\"firstName\", \"last\":\"lastName\", \"awake\":true, \"nickname\":\"Bill\"}"
                .getBytes());
        try {
            invoke(request);
            fail("ServletException was not thrown when specifying fields that are not detected.");
        } catch (ServletException e) {
        }
    }

}
