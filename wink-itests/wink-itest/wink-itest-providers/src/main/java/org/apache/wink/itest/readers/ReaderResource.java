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

package org.apache.wink.itest.readers;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("jaxrs/tests/providers/messagebodyreader/reader")
public class ReaderResource {

    private static int counter = 0;

    @Path("requestcontenttype")
    @POST
    @Consumes(MediaType.WILDCARD)
    public String postReaderNoGenericEntityWildcard(byte[] barr) {
        return "hello world";
    }

    @Path("requestcontenttype")
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public String postReaderNoGenericEntityOctetStream(byte[] barr) {
        return "invoked octet-stream method";
    }

    @Path("unexpectedclasstype")
    @POST
    public String postReaderNoGenericEntity(HashMap<String, String> entity) {
        return "hello";
    }

    @Path("classtype")
    @POST
    public String postReaderNoGenericEntity(Deque<String> entity) {
        StringBuilder sb = new StringBuilder();
        for (String s : entity) {
            sb.append("echo:" + s);
        }
        return sb.toString();
    }

    @Path("nogenericentity")
    @POST
    public String postReaderNoGenericEntity(String str) {
        return "echo:" + str;
    }

    @Path("genericentityempty")
    @POST
    public String postReaderGenericEntityEmpty(Queue entity) {
        StringBuilder sb = new StringBuilder();
        for (Object s : entity) {
            sb.append(s.toString() + " there");
        }
        return sb.toString();
    }

    @Path("genericentityqueuestring")
    @POST
    public String postReaderGenericEntityListString(Queue<String> entity) {
        StringBuilder sb = new StringBuilder();
        for (String s : entity) {
            sb.append(s + " there");
        }
        return sb.toString();
    }

    @Path("genericentityqueueobject")
    @POST
    public String postReaderGenericEntityListObject(Queue<Object> entity) {
        StringBuilder sb = new StringBuilder();
        for (Object o : entity) {
            sb.append(o + " there");
        }
        return sb.toString();
    }

    @Path("notannotatedentity")
    @POST
    public String postResponseReaderNotAnnotated(List<String> entity) {
        StringBuilder sb = new StringBuilder();
        for (String s : entity) {
            sb.append(s + " there");
        }
        return sb.toString();
    }

    @Path("annotatedentity")
    @POST
    public String postResponseReaderAnnotated(@MyReaderAnnotation List<String> entity) {
        StringBuilder sb = new StringBuilder();
        for (String s : entity) {
            sb.append(s + " there");
        }
        return sb.toString();
    }

    @Path("multipleannotatedentity")
    @POST
    public String postResponseReaderAnnotatedMultipleTimes(@CustomAnnotation @MyReaderAnnotation List<String> entity) {
        StringBuilder sb = new StringBuilder();
        for (String s : entity) {
            sb.append(s + " there");
        }
        return sb.toString();
    }

    @Path("mediatype")
    @POST
    public String postReaderMediaType(Set<String> entity) {
        StringBuilder sb = new StringBuilder();
        for (String s : entity) {
            sb.append(s + " there");
        }
        return sb.toString();
    }

    @Path("readfrom")
    @POST
    public String postReaderReadFrom(String str) {
        if ("clear".equals(str)) {
            counter = -1;
        }
        ++counter;
        return counter + "postReaderReadFrom:" + str;
    }

    @Path("readdifferentlyinteger")
    @POST
    public String postReaderReadFromClass(Integer value) {
        return "" + value;
    }

    @Path("readdifferentlylong")
    @POST
    public String postReaderReadFromClass(Long value) {
        return "" + value;
    }

    @Path("readdifferentlyshort")
    @POST
    public String postReaderReadFromAnnotation(@CustomAnnotation Short value) {
        return "" + value;
    }

    @Path("readdifferentlyshortnoannotation")
    @POST
    public String postReaderReadFromNoAnnotation(Short value) {
        return "" + value;
    }

    @Path("readdifferentlybytemediatype")
    @POST
    public String postReaderReadFromMediaType(Byte value) {
        return "" + value;
    }

    @Path("readdifferentlygenericlist")
    @POST
    public String postReaderReadFromGenericType(List value) {
        StringBuilder sb = new StringBuilder();
        sb.append("listnonspecified:");
        for (Object o : value) {
            sb.append(o.toString());
        }
        return sb.toString();
    }

    @Path("readdifferentlygenericliststring")
    @POST
    public String postReaderReadFromGenericTypeListString(List<String> value) {
        StringBuilder sb = new StringBuilder();
        sb.append("liststring:");
        for (String s : value) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Path("readdifferentlygenericlistinteger")
    @POST
    public String postReaderReadFromGenericTypeListInteger(List<Integer> value) {
        StringBuilder sb = new StringBuilder();
        sb.append("listinteger:");
        for (Integer i : value) {
            sb.append(i.toString());
        }
        return sb.toString();
    }

    @Path("readdifferentlygenericinteger")
    @POST
    public String postReaderReadFromGenericTypeInteger(Integer value) {
        return "integer:" + value;
    }
}
