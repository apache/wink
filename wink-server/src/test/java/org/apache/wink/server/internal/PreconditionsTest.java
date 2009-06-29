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
 
package org.apache.wink.server.internal;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.utils.HttpDateParser;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.MockServletInvocationTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


public class PreconditionsTest extends MockServletInvocationTest {

    private static final String                    THE_CONTENT        = ">>>The Content<<<";
    private final static HeaderDelegate<EntityTag> etagHeaderDelegate = RuntimeDelegate.getInstance().createHeaderDelegate(
                                                                          EntityTag.class);

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] { ConditionalGetResource.class };
    }

    @Path("get/{variable}")
    public static class ConditionalGetResource {

        @GET
        @Produces("text/plain")
        public Object get(@Context Request request, @PathParam("variable") String variable) {

            Date lastModified = null;
            try {
                lastModified = new Date(Long.parseLong(variable));
            } catch (NumberFormatException e) {
            }
            ResponseBuilder evaluatePreconditions = null;
            if (lastModified != null) {
                evaluatePreconditions = request.evaluatePreconditions(lastModified);
            } else {
                evaluatePreconditions = request.evaluatePreconditions(etagHeaderDelegate.fromString("\""
                    + variable + "\""));
            }
            if (evaluatePreconditions != null) {
                return evaluatePreconditions.build();
            }
            return THE_CONTENT;
        }

        @GET
        @Path("null")
        @Produces("text/plain")
        public Object getNull(@Context Request request) {

            ResponseBuilder evaluatePreconditions = request.evaluatePreconditions(new Date());
            if (evaluatePreconditions != null) {
                return evaluatePreconditions.build();
            }
            return THE_CONTENT;
        }

        @GET
        @Produces("text/plain")
        @Path("etag/{etag}")
        public Object get2(@Context Request request, @PathParam("variable") String date,
            @PathParam("etag") String etag) {

            ResponseBuilder evaluatePreconditions = request.evaluatePreconditions(new Date(
                Long.parseLong(date)), etagHeaderDelegate.fromString("\"" + etag + "\""));

            if (evaluatePreconditions != null) {
                return evaluatePreconditions.build();
            }
            return THE_CONTENT;
        }

        @PUT
        @Produces("text/plain")
        @Path("etag/{etag}")
        public Object put2(@Context Request request, @PathParam("variable") String date,
            @PathParam("etag") String etag) {

            ResponseBuilder evaluatePreconditions = request.evaluatePreconditions(new Date(
                Long.parseLong(date)), etagHeaderDelegate.fromString("\"" + etag + "\""));

            if (evaluatePreconditions != null) {
                return evaluatePreconditions.build();
            }
            return THE_CONTENT;
        }

        @PUT
        @Produces("text/plain")
        public Object put(@Context Request request, @PathParam("variable") String variable) {

            Date lastModified = null;
            try {
                lastModified = new Date(Long.parseLong(variable));
            } catch (NumberFormatException e) {
            }
            ResponseBuilder evaluatePreconditions = null;
            if (lastModified != null) {
                evaluatePreconditions = request.evaluatePreconditions(lastModified);
            } else {
                evaluatePreconditions = request.evaluatePreconditions(etagHeaderDelegate.fromString("\""
                    + variable + "\""));
            }
            if (evaluatePreconditions != null) {
                return evaluatePreconditions.build();
            }
            return THE_CONTENT;
        }

    } // 

    public void testNormalGet() throws IOException {
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET",
            "get/0/null", "*/*");
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertEquals("content", THE_CONTENT, response.getContentAsString());
    }

    public void testConditionModified() throws IOException {
        Date date_to_return = new GregorianCalendar(2007, 11, 06, 10, 0, 0).getTime();
        Date modified_since = new GregorianCalendar(2007, 11, 07, 10, 0, 0).getTime();

        // GET
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "get/"
            + String.valueOf(date_to_return.getTime()), "*/*");
        request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, HttpDateParser.toHttpDate(modified_since));
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 304, response.getStatus());

        // PUT
        request = MockRequestConstructor.constructMockRequest("PUT", "get/"
            + String.valueOf(date_to_return.getTime()), "*/*");
        request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, HttpDateParser.toHttpDate(modified_since));
        response = invoke(request);
        assertEquals("status", 304, response.getStatus());
    }

    public void testConditionNotModified() throws IOException {
        Date date_to_return = new GregorianCalendar(2007, 11, 07, 10, 0, 0).getTime();
        Date modified_since = new GregorianCalendar(2007, 11, 06, 10, 0, 0).getTime();

        // GET
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "get/"
            + String.valueOf(date_to_return.getTime()), "*/*");
        request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, HttpDateParser.toHttpDate(modified_since));
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());

        // PUT
        request = MockRequestConstructor.constructMockRequest("PUT", "get/"
            + String.valueOf(date_to_return.getTime()), "*/*");
        request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, HttpDateParser.toHttpDate(modified_since));
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
    }

    public void testConditionModifiedAndMatches() throws IOException {
        Date date_to_return = new GregorianCalendar(2007, 11, 07, 10, 0, 0).getTime();
        Date modified_since = new GregorianCalendar(2007, 11, 06, 10, 0, 0).getTime();
        String etag = "blablabla";

        // GET
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "get/"
            + String.valueOf(date_to_return.getTime()) + "/etag/" + etag, "*/*");
        request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, HttpDateParser.toHttpDate(modified_since));
        request.addHeader(HttpHeaders.IF_NONE_MATCH, "\"notmatch\"");
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());

        // PUT
        request = MockRequestConstructor.constructMockRequest("PUT", "get/"
            + String.valueOf(date_to_return.getTime()) + "/etag/" + etag, "*/*");
        request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, HttpDateParser.toHttpDate(modified_since));
        request.addHeader(HttpHeaders.IF_NONE_MATCH, "\"notmatch\"");
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
    }

    public void testConditionUnModifiedAndMatches() throws IOException {
        Date date_to_return = new GregorianCalendar(2007, 11, 07, 10, 0, 0).getTime();
        Date modified_since = new GregorianCalendar(2007, 11, 06, 10, 0, 0).getTime();
        String etag = "blablabla";

        // GET
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "get/"
            + String.valueOf(date_to_return.getTime()) + "/etag/" + etag, "*/*");
        request.addHeader(HttpHeaders.IF_UNMODIFIED_SINCE,
            HttpDateParser.toHttpDate(modified_since));
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 412, response.getStatus());

        // PUT
        request = MockRequestConstructor.constructMockRequest("PUT", "get/"
            + String.valueOf(date_to_return.getTime()) + "/etag/" + etag, "*/*");
        request.addHeader(HttpHeaders.IF_MATCH, "\"" + etag + "\"");
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());

    }

    public void testConditionIfMatches() throws IOException {
        String etag = "blablabla";

        // GET
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "get/"
            + etag, "*/*");
        request.addHeader(HttpHeaders.IF_MATCH, "\"" + etag + "\"");
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());

        // PUT
        request = MockRequestConstructor.constructMockRequest("PUT", "get/" + etag, "*/*");
        request.addHeader(HttpHeaders.IF_MATCH, "\"" + etag + "\"");
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());

    }

    public void testConditionGetNotIfMatches() throws IOException {
        String etag = "blablabla";

        // GET
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "get/"
            + etag, "*/*");
        request.addHeader(HttpHeaders.IF_MATCH, "\"notmatch\"");
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 412, response.getStatus());

        // PUT
        request = MockRequestConstructor.constructMockRequest("PUT", "get/" + etag, "*/*");
        request.addHeader(HttpHeaders.IF_MATCH, "\"notmatch\"");
        response = invoke(request);
        assertEquals("status", 412, response.getStatus());

    }

    public void testConditionIfNoneMatches() throws IOException {
        String etag = "blablabla";

        // GET
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "get/"
            + etag, "*/*");
        request.addHeader(HttpHeaders.IF_NONE_MATCH, "\"notmatch\"");
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());

        // PUT
        request = MockRequestConstructor.constructMockRequest("PUT", "get/" + etag, "*/*");
        request.addHeader(HttpHeaders.IF_NONE_MATCH, "\"notmatch\"");
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());

    }

    public void testConditionNoteIfNoneMatches() throws IOException {
        String etag = "blablabla";

        // GET
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "get/"
            + etag, "*/*");
        request.addHeader(HttpHeaders.IF_NONE_MATCH, "\"" + etag + "\"");
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 304, response.getStatus());

        // PUT
        request = MockRequestConstructor.constructMockRequest("PUT", "get/" + etag, "*/*");
        request.addHeader(HttpHeaders.IF_NONE_MATCH, "\"" + etag + "\"");
        response = invoke(request);
        assertEquals("status", 412, response.getStatus());

    }

    public void testConditionalIfUnModified() throws IOException {
        Date date_to_return = new GregorianCalendar(2007, 11, 06, 10, 0, 0).getTime();
        Date modified_since = new GregorianCalendar(2007, 11, 07, 10, 0, 0).getTime();

        // GET
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "get/"
            + String.valueOf(date_to_return.getTime()), "*/*");
        request.addHeader(HttpHeaders.IF_UNMODIFIED_SINCE,
            HttpDateParser.toHttpDate(modified_since));
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertEquals("content", THE_CONTENT, response.getContentAsString());

        // PUT
        request = MockRequestConstructor.constructMockRequest("PUT", "get/"
            + String.valueOf(date_to_return.getTime()), "*/*");
        request.addHeader(HttpHeaders.IF_UNMODIFIED_SINCE,
            HttpDateParser.toHttpDate(modified_since));
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertEquals("content", THE_CONTENT, response.getContentAsString());

    }

    public void testConditionalIfNotUnModified() throws IOException {
        Date date_to_return = new GregorianCalendar(2007, 11, 07, 10, 0, 0).getTime();
        Date modified_since = new GregorianCalendar(2007, 11, 06, 10, 0, 0).getTime();

        // GET
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "get/"
            + String.valueOf(date_to_return.getTime()), "*/*");
        request.addHeader(HttpHeaders.IF_UNMODIFIED_SINCE,
            HttpDateParser.toHttpDate(modified_since));
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 412, response.getStatus());

        // PUT
        request = MockRequestConstructor.constructMockRequest("PUT", "get/"
            + String.valueOf(date_to_return.getTime()), "*/*");
        request.addHeader(HttpHeaders.IF_UNMODIFIED_SINCE,
            HttpDateParser.toHttpDate(modified_since));
        response = invoke(request);
        assertEquals("status", 412, response.getStatus());
    }

}
