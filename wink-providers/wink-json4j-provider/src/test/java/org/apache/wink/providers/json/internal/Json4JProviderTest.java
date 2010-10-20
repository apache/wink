/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.wink.providers.json.internal;

import java.io.StringReader;
import java.util.Collections;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.providers.json4j.JSON4JArrayProvider;
import org.apache.wink.providers.json4j.JSON4JObjectProvider;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class Json4JProviderTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class, JSON4JArrayProvider.class,
            JSON4JObjectProvider.class};
    }

    private static final String JSON       =
                                               "{\"entry\": {\n" + "  \"id\": \"entry:id\",\n"
                                                   + "  \"title\": {\n"
                                                   + "    \"content\": \"entry title\",\n"
                                                   + "    \"type\": \"text\"\n"
                                                   + "  }\n"
                                                   + "}}";

    private static final String JSON_ARRAY = "[" + JSON + ", {\"test\":\"ing\"}]";

    private void compairJsonContent(final String expected, final String actual)
        throws JSONException {
        JSONObject result = new JSONObject(actual);
        JSONObject want = new JSONObject(expected);
        assertEquals(result.toString(), want.toString());
    }

    @Path("test")
    public static class TestResource {

        @GET
        @Path("json")
        @Produces("application/json")
        public JSONObject getJson() throws Exception {
            return new JSONObject(JSON);
        }

        @POST
        @Path("json")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public JSONObject postJson(JSONObject object) throws Exception {
            return object.put("foo", "bar");
        }

        @GET
        @Path("jsonarray")
        @Produces(MediaType.APPLICATION_JSON)
        public JSONArray getJsonArray() throws Exception {
            return new JSONArray(JSON_ARRAY);
        }

        @POST
        @Path("jsonarray")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public JSONArray postJson(JSONArray array) throws Exception {
            return array.put(Collections.singletonMap("foo", "bar"));
        }
    }

    public void testGetJson() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test/json", "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        compairJsonContent(JSON, response.getContentAsString());
    }

    public void testPostJson() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test/json",
                                                        "application/json",
                                                        MediaType.APPLICATION_JSON,
                                                        JSON.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        JSONObject result = new JSONObject(response.getContentAsString());
        JSONObject want = new JSONObject(JSON);
        want.put("foo", "bar");
        assertEquals(want.toString(), result.toString());
    }

    public void testGetJsonArray() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/test/jsonarray",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        JSONArray result = new JSONArray(new StringReader(response.getContentAsString()));
        JSONArray want = new JSONArray(JSON_ARRAY);
        assertEquals(want.toString(), result.toString());
    }

    public void testPostJsonArray() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test/jsonarray",
                                                        "application/json",
                                                        MediaType.APPLICATION_JSON,
                                                        JSON_ARRAY.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        JSONArray result = new JSONArray(new StringReader(response.getContentAsString()));
        JSONArray want = new JSONArray(JSON_ARRAY).put(Collections.singletonMap("foo", "bar"));
        assertEquals(want.toString(), result.toString());
    }
}
