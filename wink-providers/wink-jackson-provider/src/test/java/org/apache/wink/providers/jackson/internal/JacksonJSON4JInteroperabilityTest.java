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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.providers.jackson.WinkJacksonJaxbJsonProvider;
import org.apache.wink.providers.jackson.internal.jaxb.Person;
import org.apache.wink.providers.json.JSONUtils;
import org.apache.wink.providers.json4j.JSON4JArrayProvider;
import org.apache.wink.providers.json4j.JSON4JObjectProvider;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JacksonJSON4JInteroperabilityTest extends MockServletInvocationTest {

    private WinkJacksonJaxbJsonProvider jacksonProvider      = new WinkJacksonJaxbJsonProvider();
    private JSON4JObjectProvider        json4jObjectProvider = new JSON4JObjectProvider();
    private JSON4JArrayProvider         json4jArrayProvider  = new JSON4JArrayProvider();

    public static class PersonPOJO {
        String first;
        String last;
        int    age;

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

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {PersonResource.class};
    }

    @Path("/person")
    public static class PersonResource {

        @GET
        public Person getPerson() {
            Person p = new Person();
            p.setName("myName");
            p.setDesc("myDescription");
            p.setAge(100);
            return p;
        }
    }

    public void testPOJOInteroperabilityJacksonToJSON4J() throws Exception {
        PersonPOJO p = new PersonPOJO();
        p.setFirst("Joe");
        p.setLast("Shmoe");
        p.setAge(100);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        jacksonProvider.writeTo(p,
                                p.getClass(),
                                p.getClass(),
                                null,
                                MediaType.APPLICATION_JSON_TYPE,
                                null,
                                os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        JSONObject jsonObject =
            json4jObjectProvider.readFrom(JSONObject.class,
                                          JSONObject.class,
                                          null,
                                          MediaType.APPLICATION_JSON_TYPE,
                                          null,
                                          is);
        assertEquals("Joe", jsonObject.get("first"));
        assertEquals("Shmoe", jsonObject.get("last"));
        assertEquals(100, jsonObject.get("age"));
    }

    public void testPOJOInteroperabilityJSON4JToJackson() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("first", "Joe");
        jsonObject.put("last", "Shmoe");
        jsonObject.put("age", 100);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        json4jObjectProvider.writeTo(jsonObject,
                                     JSONObject.class,
                                     JSONObject.class,
                                     null,
                                     MediaType.APPLICATION_JSON_TYPE,
                                     null,
                                     os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        PersonPOJO p =
            (PersonPOJO)jacksonProvider.readFrom(Object.class,
                                                 PersonPOJO.class,
                                                 null,
                                                 MediaType.APPLICATION_JSON_TYPE,
                                                 null,
                                                 is);
        assertEquals("Joe", p.getFirst());
        assertEquals("Shmoe", p.getLast());
        assertEquals(100, p.getAge());

        jsonObject = new JSONObject();
        jsonObject.put("first", "Joe");
        jsonObject.put("last", "Shmoe");
        jsonObject.put("age", "100"); // this is the only change to see if an
        // integer as a String works
        os = new ByteArrayOutputStream();
        json4jObjectProvider.writeTo(jsonObject,
                                     JSONObject.class,
                                     JSONObject.class,
                                     null,
                                     MediaType.APPLICATION_JSON_TYPE,
                                     null,
                                     os);
        is = new ByteArrayInputStream(os.toByteArray());
        p =
            (PersonPOJO)jacksonProvider.readFrom(Object.class,
                                                 PersonPOJO.class,
                                                 null,
                                                 MediaType.APPLICATION_JSON_TYPE,
                                                 null,
                                                 is);
        assertEquals("Joe", p.getFirst());
        assertEquals("Shmoe", p.getLast());
        assertEquals(100, p.getAge());
    }

    public void testJAXBInteroperabilityJSON4JToJackson() throws Exception {
        // we need to use the mock servlet for this one test because the
        // JSON4JJAXBProvider uses @Context injection
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/person", "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils
            .equals(new org.json.JSONObject(
                                            "{\"person\":{\"name\":\"myName\", \"desc\":\"myDescription\", \"age\":\"100\"}}"),
                    new org.json.JSONObject(response.getContentAsString())));
        JSONObject result = new JSONObject(new ByteArrayInputStream(response.getContentAsByteArray()));
        JSONObject person = (JSONObject)result.get("person");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", person.get("name"));
        jsonObject.put("desc", person.get("desc"));
        jsonObject.put("age", person.get("age"));
        Person p =
            (Person)jacksonProvider.readFrom(Object.class,
                                             Person.class,
                                             null,
                                             MediaType.APPLICATION_JSON_TYPE,
                                             null,
                                             new ByteArrayInputStream(jsonObject.toString()
                                                 .getBytes()));
        assertEquals("myName", p.getName());
        assertEquals("myDescription", p.getDesc());
        assertEquals(100, p.getAge());
    }

    public void testArrayInteroperabilityJacksonToJSON4J() throws Exception {
        PersonPOJO p1 = new PersonPOJO();
        p1.setFirst("firstName");
        p1.setLast("lastName");
        p1.setAge(45);
        PersonPOJO p2 = new PersonPOJO();
        p2.setFirst("firstName2");
        p2.setLast("lastName2");
        p2.setAge(46);
        PersonPOJO[] people = new PersonPOJO[] {p1, p2};
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        jacksonProvider.writeTo(people,
                                PersonPOJO[].class,
                                PersonPOJO[].class,
                                null,
                                MediaType.APPLICATION_JSON_TYPE,
                                null,
                                os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        JSONArray jsonArray =
            (JSONArray)json4jArrayProvider.readFrom(JSONArray.class,
                                                    JSONArray.class,
                                                    null,
                                                    MediaType.APPLICATION_JSON_TYPE,
                                                    null,
                                                    is);
        assertEquals(2, jsonArray.size());
        assertEquals("firstName", ((JSONObject)jsonArray.get(0)).get("first"));
        assertEquals("lastName", ((JSONObject)jsonArray.get(0)).get("last"));
        assertEquals(45, ((JSONObject)jsonArray.get(0)).get("age"));
        assertEquals("firstName2", ((JSONObject)jsonArray.get(1)).get("first"));
        assertEquals("lastName2", ((JSONObject)jsonArray.get(1)).get("last"));
        assertEquals(46, ((JSONObject)jsonArray.get(1)).get("age"));
    }

    public void testArrayInteroperabilityJSON4JToJackson() throws Exception {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        JSONObject jsonObject2 = new JSONObject();
        jsonObject1.put("first", "firstName");
        jsonObject1.put("last", "lastName");
        jsonObject1.put("age", 45);
        jsonObject2.put("first", "firstName2");
        jsonObject2.put("last", "lastName2");
        jsonObject2.put("age", 46);
        jsonArray.add(jsonObject1);
        jsonArray.add(jsonObject2);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        json4jArrayProvider.writeTo(jsonArray,
                                    JSONArray.class,
                                    JSONArray.class,
                                    null,
                                    MediaType.APPLICATION_JSON_TYPE,
                                    null,
                                    os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        PersonPOJO[] people =
            (PersonPOJO[])jacksonProvider.readFrom(Object.class,
                                                   PersonPOJO[].class,
                                                   null,
                                                   MediaType.APPLICATION_JSON_TYPE,
                                                   null,
                                                   is);
        PersonPOJO p = people[0];
        assertEquals(2, people.length);
        assertEquals("firstName", p.getFirst());
        assertEquals("lastName", p.getLast());
        assertEquals(45, p.getAge());
        p = people[1];
        assertEquals("firstName2", p.getFirst());
        assertEquals("lastName2", p.getLast());
        assertEquals(46, p.getAge());
    }
}
