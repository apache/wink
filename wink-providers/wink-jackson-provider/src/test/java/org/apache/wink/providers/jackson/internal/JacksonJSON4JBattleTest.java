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

import org.apache.wink.providers.jackson.WinkJacksonJaxbJsonProvider;
import org.apache.wink.providers.json.JSONUtils;
import org.apache.wink.providers.json.JsonProvider;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JacksonJSON4JBattleTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {JSON4JResource.class};
    }

    @Override
    protected Object[] getSingletons() {
        // this test checks to make sure we can override system providers (JsonProvider) to establish a new priority
        // last one listed takes priority
        // (note: this test relies on consistent behavior in MockServletInvocationTest.getSingletons() -- if this
        //  method re-orders the data return from here, test results may change)
        return new Object[] { new WinkJacksonJaxbJsonProvider(), new JsonProvider(),};
    }

    @Path("/json4j")
    public static class JSON4JResource {

        @POST
        @Consumes("application/json")
        @Produces("application/json")
        @Path("object")
        public JSONObject echoObject(JSONObject object) {
            return object;
        }
    }
    
    private static final String JSON =
        "{\"entry\": {\n" + "  \"id\": \"entry:id\",\n"
            + "  \"title\": {\n"
            + "    \"content\": \"entry title\",\n"
            + "    \"type\": \"text\"\n"
            + "  }\n"
            + "}}";
    
    public void testPost() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/json4j/object",
                                                        MediaType.APPLICATION_JSON,
                                                        MediaType.APPLICATION_JSON,
                                                        JSON.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(JSONUtils.objectForString(JSON),
                JSONUtils.objectForString(response.getContentAsString())));
    }
    
}
