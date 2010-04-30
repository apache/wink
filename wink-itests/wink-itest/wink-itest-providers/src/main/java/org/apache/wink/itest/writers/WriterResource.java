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

package org.apache.wink.itest.writers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.dom.DOMSource;

@Path("jaxrs/tests/providers/messagebodywriter")
public class WriterResource {

    private static int counter = 0;

    @GET
    @Path("concretetype")
    public String getConcretTypeBack() {
        return "Hello there";
    }

    @GET
    @Path("contentlength")
    @CustomAnnotation
    public Response getContentLength(@QueryParam("mt") String mt, @QueryParam("class") String clazz) {
        if (clazz == null) {
            byte[] barr = new byte[100000];
            for (int c = 0; c < barr.length; ++c) {
                barr[c] = (byte)c;
            }
            return Response.ok(barr).type(mt).build();
        } else if ("Vector".equals(clazz)) {
            Vector v = new Vector(2);
            v.add("Hello");
            v.add("There");
            return Response.ok(v).type(mt).build();
        } else if ("ListInteger".equals(clazz)) {
            List<Integer> v = new ArrayList<Integer>(2);
            v.add(1);
            v.add(2);
            return Response.ok(new GenericEntity<List<Integer>>(v) {
            }).build();
        } else if ("String".equals(clazz)) {
            return Response.ok("hello there").build();
        }
        return null;
    }

    private static class MyType {

    }

    @Path("classtype")
    @GET
    public Object getWriterClassType(@QueryParam("type") String type) {
        if ("deque".equals(type)) {
            ArrayDeque<String> d = new ArrayDeque<String>();
            d.add("str:foo");
            d.add("str:bar");
            return d;
        } else if ("mytype".equals(type)) {
            return new MyType();
        } else if ("string".equals(type)) {
            return "str:foobar";
        } else if ("stringcontenttype".equals(type)) {
            return Response.ok("str:foobarcontenttype").type(MediaType.APPLICATION_XML_TYPE)
                .build();
        } else if ("sourcecontenttype".equals(type)) {
            return Response.ok(new DOMSource()).type(MediaType.APPLICATION_JSON).build();
        } else if ("source".equals(type)) {
            return Response.ok(new DOMSource()).type(MediaType.TEXT_XML).build();
        }
        return null;
    }

    @Path("notannotated")
    @GET
    public Object getWriterNotAnnotatedMethod() {
        List<String> l = new ArrayList<String>();
        l.add("foo");
        l.add("bar");
        return l;
    }

    @Path("annotated")
    @GET
    @MyWriterAnnotation
    public Object getWriterAnnotatedMethod() {
        List<String> l = new ArrayList<String>();
        l.add("foo");
        l.add("bar");
        return l;
    }

    @Path("annotated")
    @POST
    @MyWriterAnnotation
    public Object postWriterAnnotatedMethod() {
        List<String> l = new ArrayList<String>();
        l.add("foo");
        l.add("bar");
        return l;
    }

    @Path("genericentity")
    @POST
    public Response getWriterResponse(@QueryParam("query") String q) {
        if ("setstring".equals(q)) {
            HashSet<String> s = new HashSet<String>();
            s.add("hello");
            s.add("world");
            return Response.ok(new GenericEntity<Set<String>>(s) {
            }).build();
        } else if ("setinteger".equals(q)) {
            HashSet<Integer> s = new HashSet<Integer>();
            s.add(Integer.valueOf(1));
            s.add(Integer.valueOf(2));
            return Response.ok(new GenericEntity<Set<Integer>>(s) {
            }).build();
        } else if ("setshort".equals(q)) {
            HashSet<Short> s = new HashSet<Short>();
            s.add(Short.valueOf((short)1));
            s.add(Short.valueOf((short)2));
            return Response.ok(new GenericEntity<Set<Short>>(s) {
            }).build();
        }
        return null;
    }

    @Path("nogenericentity")
    @POST
    public Response getNoWriterResponse(@QueryParam("query") String q) {
        if ("setstring".equals(q)) {
            HashSet<String> s = new HashSet<String>();
            s.add("hello");
            s.add("world");
            return Response.ok(s).build();
        } else if ("setinteger".equals(q)) {
            HashSet<Integer> s = new HashSet<Integer>();
            s.add(Integer.valueOf(1));
            s.add(Integer.valueOf(2));
            return Response.ok(s).build();
        } else if ("setshort".equals(q)) {
            HashSet<Short> s = new HashSet<Short>();
            s.add(Short.valueOf((short)1));
            s.add(Short.valueOf((short)2));
            return Response.ok(s).build();
        }
        return null;
    }

    @Path("mediatype")
    @POST
    public Response getMediaType(@QueryParam("mt") String mt) {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("foo", "bar");
        return Response.ok(hm).type(mt).build();
    }

    @Path("throwsexception")
    @POST
    public Response throwsException(@QueryParam("mt") String mt) {
        return Response.ok("something").type(mt).build();
    }
}
