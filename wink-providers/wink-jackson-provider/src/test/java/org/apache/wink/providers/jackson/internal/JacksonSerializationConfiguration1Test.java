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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.providers.json.JSONUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JacksonSerializationConfiguration1Test extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {PersonResource.class};
    }

    @Override
    protected Object[] getSingletons() {
        JacksonJsonProvider jacksonProvider = new JacksonJsonProvider();
        jacksonProvider
            .configure(org.codehaus.jackson.map.SerializationConfig.Feature.AUTO_DETECT_FIELDS,
                       false);
        jacksonProvider
            .configure(org.codehaus.jackson.map.SerializationConfig.Feature.AUTO_DETECT_GETTERS,
                       false);
        jacksonProvider
            .configure(org.codehaus.jackson.map.SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS,
                       false);
        jacksonProvider
            .configure(org.codehaus.jackson.map.SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,
                       false);
        return new Object[] {jacksonProvider};
    }

    @Path("/person")
    public static class PersonResource {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Person getPerson() {
            Person p = new Person();
            p.setFirst("firstName");
            p.setLast("lastName");
            p.nickname = "nickName";
            return p;
        }
    }

    public static class Person {
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

    public void testGETPerson() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/person",
                                                        MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(new JSONObject("{}"), new JSONObject(response
            .getContentAsString())));
    }
}
