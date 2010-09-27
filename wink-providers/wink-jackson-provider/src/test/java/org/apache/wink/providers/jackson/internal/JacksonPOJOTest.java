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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.wink.providers.jackson.WinkJacksonJaxbJsonProvider;
import org.apache.wink.providers.json.JSONUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JacksonPOJOTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {POJOResource.class};
    }

    @Override
    protected Object[] getSingletons() {
        return new Object[] {new WinkJacksonJaxbJsonProvider()};
    }

    @Path("/jackson/pojo")
    public static class POJOResource {

        @GET
        @Produces("application/json")
        @Path("person")
        public Person getPerson() {
            Person p = new Person();
            p.setFirst("first");
            p.setLast("last");
            return p;
        }

        @POST
        @Produces("application/json")
        @Consumes("application/json")
        @Path("person")
        public Person postPerson(Person p) {
            return p;
        }

        @GET
        @Produces("application/json")
        @Path("string")
        public List<String> getCollection() {
            List<String> list = new ArrayList<String>();
            list.add("string1");
            list.add("");
            list.add("string3");
            return list;
        }

        @POST
        @Produces("application/json")
        @Consumes("application/json")
        @Path("string")
        public List<String> postCollection(List<String> list) {
            return list;
        }

        @GET
        @Produces("application/json")
        @Path("personcollect")
        public List<Person> getPersonCollection() {
            List<Person> people = new ArrayList<Person>();
            Person p = new Person();
            p.setFirst("first1");
            p.setLast("last1");
            people.add(p);
            p = new Person();
            p.setFirst("first2");
            p.setLast("last2");
            people.add(p);
            p = new Person();
            p.setFirst("first3");
            p.setLast("last3");
            people.add(p);
            return people;
        }

        @POST
        @Produces("application/json")
        @Consumes("application/json")
        @Path("personcollect")
        public List<Person> postPeopleCollection(List<Person> people) {
            return people;
        }

        @GET
        @Produces("application/json")
        @Path("stringarray")
        public String[] getArray() {
            String[] list = new String[4];
            list[0] = "string1";
            list[1] = "";
            list[2] = null;
            list[3] = "string4";
            return list;
        }

        @POST
        @Produces("application/json")
        @Consumes("application/json")
        @Path("stringarray")
        public String[] postArray(String[] list) {
            return list;
        }

        @GET
        @Produces("application/json")
        @Path("personarray")
        public Person[] getPeopleArray() {
            Person[] people = new Person[3];
            Person p = new Person();
            p.setFirst("first1");
            p.setLast("last1");
            people[0] = p;
            p = new Person();
            p.setFirst("first2");
            p.setLast("last2");
            people[1] = p;
            p = new Person();
            p.setFirst("first3");
            p.setLast("last3");
            people[2] = p;
            return people;
        }

        @POST
        @Produces("application/json")
        @Consumes("application/json")
        @Path("personarray")
        public Person[] postPeopleArray(Person[] people) {
            return people;
        }

        @GET
        @Produces("application/json")
        @Path("collectionofcollection")
        public List<List<Person>> getCollectionofCollection() {
            List<List<Person>> peopleCollection = new ArrayList<List<Person>>();

            List<Person> people = new ArrayList<Person>();
            Person p = new Person();
            p.setFirst("first1");
            p.setLast("last1");
            people.add(p);
            p = new Person();
            p.setFirst("first2");
            p.setLast("last2");
            people.add(p);
            p = new Person();
            p.setFirst("first3");
            p.setLast("last3");
            people.add(p);
            peopleCollection.add(people);

            people = new ArrayList<Person>();
            p = new Person();
            p.setFirst("first4");
            p.setLast("last4");
            people.add(p);
            people.add(null);
            p = new Person();
            p.setFirst("first6");
            p.setLast("last6");
            people.add(p);
            peopleCollection.add(people);

            return peopleCollection;
        }

        @POST
        @Produces("application/json")
        @Consumes("application/json")
        @Path("collectionofcollection")
        public List<List<Person>> postCollectionofCollection(List<List<Person>> peopleCollection) {
            return peopleCollection;
        }

        @GET
        @Produces("application/json")
        @Path("collectionofarray")
        public List<Person[]> getCollectionofArray() {
            List<Person[]> peopleCollection = new ArrayList<Person[]>();

            List<Person> people = new ArrayList<Person>();
            Person p = new Person();
            p.setFirst("first1");
            p.setLast("last1");
            people.add(p);
            p = new Person();
            p.setFirst("first2");
            p.setLast("last2");
            people.add(p);
            p = new Person();
            p.setFirst("first3");
            p.setLast("last3");
            people.add(p);
            peopleCollection.add(people.toArray(new Person[] {}));

            people = new ArrayList<Person>();
            p = new Person();
            p.setFirst("first4");
            p.setLast("last4");
            people.add(p);
            people.add(null);
            p = new Person();
            p.setFirst("first6");
            p.setLast("last6");
            people.add(p);
            peopleCollection.add(people.toArray(new Person[] {}));

            return peopleCollection;
        }

        @POST
        @Produces("application/json")
        @Consumes("application/json")
        @Path("collectionofarray")
        public List<Person[]> postCollectionofArray(List<Person[]> peopleCollection) {
            return peopleCollection;
        }
    }

    public static class Person {
        String first;
        String last;

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

        public boolean equals(Object o) {
            if (!(o instanceof Person))
                return false;
            Person other = (Person)o;
            return this.first.equals(other.first) && this.last.equals(other.last);
        }
    }

    public void testGETPerson() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/jackson/pojo/person",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(new JSONObject("{\"first\":\"first\", \"last\":\"last\"}"),
                                    new JSONObject(response.getContentAsString())));
    }

    public void testPOSTPerson() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jackson/pojo/person",
                                                        "application/json");
        request.setContentType("application/json");
        request.setContent("{\"first\":\"first\", \"last\":\"last\"}".getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(new JSONObject("{\"first\":\"first\", \"last\":\"last\"}"),
                                    new JSONObject(response.getContentAsString())));
    }

    public void testGETCollection() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/jackson/pojo/string",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(new JSONArray("[\"string1\", \"\", \"string3\"]"),
                                    new JSONArray(response.getContentAsString())));
    }

    public void testPOSTCollection() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jackson/pojo/string",
                                                        "application/json");
        request.setContentType("application/json");
        request.setContent("[\"string1\", \"\", \"string3\"]".getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils.equals(new JSONArray("[\"string1\", \"\", \"string3\"]"),
                                    new JSONArray(response.getContentAsString())));
    }

    public void testGETCollectionWithObject() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/jackson/pojo/personcollect",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils
            .equals(new JSONArray(
                                  "[{\"first\":\"first1\",\"last\":\"last1\"}," + "{\"first\":\"first2\",\"last\":\"last2\"},"
                                      + "{\"first\":\"first3\",\"last\":\"last3\"}]"),
                    new JSONArray(response.getContentAsString())));
    }

    public void testPOSTCollectionWithObject() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jackson/pojo/personcollect",
                                                        "application/json");
        request.setContentType("application/json");
        request
            .setContent(("[{\"first\":\"first1\",\"last\":\"last1\"}," + "{\"first\":\"first2\",\"last\":\"last2\"},"
                + "{\"first\":\"first3\",\"last\":\"last3\"}]").getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils
            .equals(new JSONArray(
                                  "[{\"first\":\"first1\",\"last\":\"last1\"}," + "{\"first\":\"first2\",\"last\":\"last2\"},"
                                      + "{\"first\":\"first3\",\"last\":\"last3\"}]"),
                    new JSONArray(response.getContentAsString())));
    }

    public void testGETCollectionWithCollection() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/jackson/pojo/collectionofcollection",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils
            .equals(new JSONArray(
                                  "[[{\"first\":\"first1\",\"last\":\"last1\"}," + "{\"first\":\"first2\",\"last\":\"last2\"},"
                                      + "{\"first\":\"first3\",\"last\":\"last3\"}],"
                                      + "[{\"first\":\"first4\",\"last\":\"last4\"},"
                                      + "null,"
                                      + "{\"first\":\"first6\",\"last\":\"last6\"}]]"),
                    new JSONArray(response.getContentAsString())));
    }

    public void testPOSTCollectionWithCollection() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jackson/pojo/collectionofcollection",
                                                        "application/json");
        request.setContentType("application/json");
        request
            .setContent(("[[{\"first\":\"first1\",\"last\":\"last1\"}," + "{\"first\":\"first2\",\"last\":\"last2\"},"
                + "{\"first\":\"first3\",\"last\":\"last3\"}],"
                + "[{\"first\":\"first4\",\"last\":\"last4\"},"
                + "null,"
                + "{\"first\":\"first6\",\"last\":\"last6\"}]]").getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils
            .equals(new JSONArray(
                                  "[[{\"first\":\"first1\",\"last\":\"last1\"}," + "{\"first\":\"first2\",\"last\":\"last2\"},"
                                      + "{\"first\":\"first3\",\"last\":\"last3\"}],"
                                      + "[{\"first\":\"first4\",\"last\":\"last4\"},"
                                      + "null,"
                                      + "{\"first\":\"first6\",\"last\":\"last6\"}]]"),
                    new JSONArray(response.getContentAsString())));
    }

    public void testGETCollectionWithArray() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/jackson/pojo/collectionofarray",
                                                        "application/json");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils
            .equals(new JSONArray(
                                  "[[{\"first\":\"first1\",\"last\":\"last1\"}," + "{\"first\":\"first2\",\"last\":\"last2\"},"
                                      + "{\"first\":\"first3\",\"last\":\"last3\"}],"
                                      + "[{\"first\":\"first4\",\"last\":\"last4\"},"
                                      + "null,"
                                      + "{\"first\":\"first6\",\"last\":\"last6\"}]]"),
                    new JSONArray(response.getContentAsString())));
    }

    public void testPOSTCollectionWithArray() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/jackson/pojo/collectionofarray",
                                                        "application/json");
        request.setContentType("application/json");
        request
            .setContent(("[[{\"first\":\"first1\",\"last\":\"last1\"}," + "{\"first\":\"first2\",\"last\":\"last2\"},"
                + "{\"first\":\"first3\",\"last\":\"last3\"}],"
                + "[{\"first\":\"first4\",\"last\":\"last4\"},"
                + "null,"
                + "{\"first\":\"first6\",\"last\":\"last6\"}]]").getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils
            .equals(new JSONArray(
                                  "[[{\"first\":\"first1\",\"last\":\"last1\"}," + "{\"first\":\"first2\",\"last\":\"last2\"},"
                                      + "{\"first\":\"first3\",\"last\":\"last3\"}],"
                                      + "[{\"first\":\"first4\",\"last\":\"last4\"},"
                                      + "null,"
                                      + "{\"first\":\"first6\",\"last\":\"last6\"}]]"),
                    new JSONArray(response.getContentAsString())));
    }
}
