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

package org.apache.wink.server.internal.registry;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.PathSegmentImpl;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class InjectableDataTest extends MockServletInvocationTest {
    private static List<Class<?>> resources = new LinkedList<Class<?>>();

    static {
        for (Class<?> cls : InjectableDataTest.class.getClasses()) {
            if (cls.getSimpleName().endsWith("Resource")) {
                resources.add(cls);
            }
        }
    }

    @Override
    protected Class<?>[] getClasses() {
        return resources.toArray(new Class<?>[resources.size()]);
    }

    @Path("pathParam/{p}")
    public static class PathParamResource {

        @GET
        @Path("simple")
        @Produces
        public void getSimple(@PathParam("p") String p) {
            assertEquals("a b+c", p);
        }

        @GET
        @Path("encoded")
        @Produces
        public void getEncoded(@Encoded @PathParam("p") String p) {
            assertEquals("a%20b+c", p);
        }

        @GET
        @Path("default")
        @Produces
        public void getDefault(@DefaultValue("b") @PathParam("k") String p) {
            assertEquals("b", p);
        }

        @GET
        @Path("simpleList")
        @Produces
        public void getSimpleList(@PathParam("p") List<String> p) {
            assertEquals(1, p.size());
            assertEquals("a b+c", p.get(0));
        }

        @GET
        @Path("simpleListMulti/{p:.*/.*}")
        @Produces
        public void getSimpleListMulti(@PathParam("p") List<String> p) {
            assertEquals(1, p.size());
            assertEquals("a/b", p.get(0));
        }

        @GET
        @Path("encodedList")
        @Produces
        public void getEncodedList(@Encoded @PathParam("p") List<String> p) {
            assertEquals(1, p.size());
            assertEquals("a%20b+c", p.get(0));
        }

        @GET
        @Path("defaultList")
        @Produces
        public void getDefaultList(@DefaultValue("b/c") @PathParam("k") List<String> p) {
            assertEquals(1, p.size());
            assertEquals("b/c", p.get(0));
        }

        @GET
        @Path("simpleSet")
        @Produces
        public void getSimpleSet(@PathParam("p") Set<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("a b+c"));
        }

        @GET
        @Path("encodedSet")
        @Produces
        public void getEncodedSet(@Encoded @PathParam("p") Set<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("a%20b+c"));
        }

        @GET
        @Path("defaultSet")
        @Produces
        public void getDefaultSet(@DefaultValue("b/c") @PathParam("k") Set<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("b/c"));
        }

        @GET
        @Path("simpleSortedSet")
        @Produces
        public void getSimpleSortedSet(@PathParam("p") SortedSet<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("a b+c"));
        }

        @GET
        @Path("encodedSortedSet")
        @Produces
        public void getEncodedSortedSet(@Encoded @PathParam("p") SortedSet<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("a%20b+c"));
        }

        @GET
        @Path("defaultSortedSet")
        @Produces
        public void getDefaultSortedSet(@DefaultValue("b/c") @PathParam("k") SortedSet<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("b/c"));
        }

        @GET
        @Path("PathSegmentSimple")
        @Produces
        public void getPathSegment(@PathParam("p") PathSegment p) {
            PathSegment segment = new PathSegmentImpl("a b;m1=1");
            assertEquals(segment, p);
        }

        @GET
        @Path("PathSegmentEncoded")
        @Produces
        public void getPathSegmentEncoded(@Encoded @PathParam("p") PathSegment p) {
            assertEquals(new PathSegmentImpl("a%20b;m1=1"), p);
        }

        @GET
        @Path("PathSegmentDefault")
        @Produces
        public void getPathSegmentDefault(@DefaultValue("d;m=1") @PathParam("k") PathSegment p) {
            assertEquals(new PathSegmentImpl("d;m=1"), p);
        }

        @GET
        @Path("PathSegmentEmpty")
        @Produces
        public void getPathSegmentEmpty(@PathParam("k") PathSegment p) {
            assertEquals(null, p);
        }

        @GET
        @Path("PathSegmentLast/{p1:.*}")
        @Produces
        public void getPathSegmentLast(@PathParam("p1") PathSegment p) {
            PathSegment segment = new PathSegmentImpl("c");
            assertEquals(segment, p);
        }

        @GET
        @Path("PathSegmentMultiSimple/{p1:.*/.*}/end")
        @Produces
        public void getPathSegmentMultiSimple(@PathParam("p1") PathSegment p) {
            PathSegment segment = new PathSegmentImpl("c d");
            assertEquals(segment, p);
        }

        @GET
        @Path("PathSegmentMultiEncoded/{p1:.*/.*}/end")
        @Produces
        public void getPathSegmentMultiEncoded(@Encoded @PathParam("p1") PathSegment p) {
            PathSegment segment = new PathSegmentImpl("c%20d");
            assertEquals(segment, p);
        }

        @GET
        @Path("PathSegmentSimpleList/{p1:.*}")
        @Produces
        public void getPathSegmentList(@PathParam("p1") List<PathSegment> p) {
            PathSegment segment1 = new PathSegmentImpl("a b;m1=1");
            PathSegment segment2 = new PathSegmentImpl("c d;m2=2");
            assertEquals(2, p.size());
            assertEquals(segment1, p.get(0));
            assertEquals(segment2, p.get(1));
        }

        @GET
        @Path("PathSegmentEncodedList/{p1:.*}")
        @Produces
        public void getPathSegmentEncodedList(@Encoded @PathParam("p1") List<PathSegment> p) {
            PathSegment segment1 = new PathSegmentImpl("a%20b;m1=1");
            PathSegment segment2 = new PathSegmentImpl("c%20d;m2=2");
            assertEquals(2, p.size());
            assertEquals(segment1, p.get(0));
            assertEquals(segment2, p.get(1));
        }

        @GET
        @Path("PathSegmentDefaultList/{p1:.*}")
        @Produces
        public void getPathSegmentDefaultList(@DefaultValue("a;m1=1/b;m2=2") @PathParam("k") List<PathSegment> p) {
            PathSegment segment1 = new PathSegmentImpl("a;m1=1");
            PathSegment segment2 = new PathSegmentImpl("b;m2=2");
            assertEquals(2, p.size());
            assertEquals(segment1, p.get(0));
            assertEquals(segment2, p.get(1));
        }

        @GET
        @Path("PathSegmentEmptyList/{p1:.*}")
        @Produces
        public void getPathSegmentEmptyList(@PathParam("k") List<PathSegment> p) {
            assertEquals(0, p.size());
        }

        @GET
        @Path("PathSegmentSimpleSet/{p1:.*}")
        @Produces
        public void getPathSegmentSet(@PathParam("p1") Set<PathSegment> p) {
            PathSegment segment1 = new PathSegmentImpl("a b;m1=1");
            PathSegment segment2 = new PathSegmentImpl("c d;m2=2");
            assertEquals(2, p.size());
            assertTrue(p.contains(segment1));
            assertTrue(p.contains(segment2));
        }

        @GET
        @Path("PathSegmentEncodedSet/{p1:.*}")
        @Produces
        public void getPathSegmentEncodedSet(@Encoded @PathParam("p1") Set<PathSegment> p) {
            PathSegment segment1 = new PathSegmentImpl("a%20b;m1=1");
            PathSegment segment2 = new PathSegmentImpl("c%20d;m2=2");
            assertEquals(2, p.size());
            assertTrue(p.contains(segment1));
            assertTrue(p.contains(segment2));
        }

        @GET
        @Path("PathSegmentDefaultSet/{p1:.*}")
        @Produces
        public void getPathSegmentDefaultSet(@DefaultValue("a;m1=1/b;m2=2") @PathParam("k") Set<PathSegment> p) {
            PathSegment segment1 = new PathSegmentImpl("a;m1=1");
            PathSegment segment2 = new PathSegmentImpl("b;m2=2");
            assertEquals(2, p.size());
            assertTrue(p.contains(segment1));
            assertTrue(p.contains(segment2));
        }

        @GET
        @Path("PathSegmentSimpleSortedSet/{p1:.*}")
        @Produces
        public void getPathSegmentSortedSet(@PathParam("p1") SortedSet<PathSegment> p) {
            PathSegment segment1 = new PathSegmentImpl("a b;m1=1");
            PathSegment segment2 = new PathSegmentImpl("c d;m2=2");
            assertEquals(2, p.size());
            assertTrue(p.contains(segment1));
            assertTrue(p.contains(segment2));
        }

        @GET
        @Path("PathSegmentEncodedSortedSet/{p1:.*}")
        @Produces
        public void getPathSegmentEncodedSortedSet(@Encoded @PathParam("p1") SortedSet<PathSegment> p) {
            PathSegment segment1 = new PathSegmentImpl("a%20b;m1=1");
            PathSegment segment2 = new PathSegmentImpl("c%20d;m2=2");
            assertEquals(2, p.size());
            assertTrue(p.contains(segment1));
            assertTrue(p.contains(segment2));
        }

        @GET
        @Path("PathSegmentDefaultSortedSet/{p1:.*}")
        @Produces
        public void getPathSegmentDefaultSortedSet(@DefaultValue("a;m1=1/b;m2=2") @PathParam("k") SortedSet<PathSegment> p) {
            PathSegment segment1 = new PathSegmentImpl("a;m1=1");
            PathSegment segment2 = new PathSegmentImpl("b;m2=2");
            assertEquals(2, p.size());
            assertTrue(p.contains(segment1));
            assertTrue(p.contains(segment2));
        }

        @Path("subresourcelocator/{p1}")
        public PathParamResource.PathParamSubResource getSubResource(@PathParam("p1") String p1) {
            assertEquals("d e+f", p1);
            return new PathParamResource.PathParamSubResource();
        }

        public class PathParamSubResource {
            @GET
            @Path("subresourcestring/{p2}")
            public void getPathString(@PathParam("p2") String p2) {
                assertEquals("g h+i", p2);
            }

            @GET
            @Path("subresourcepathsegment/{p2}")
            public void getPathSegment(@PathParam("p2") PathSegment p2) {
                assertEquals(new PathSegmentImpl("j k+l"), p2);
            }
        }
    }

    @Path("queryParam")
    public static class QueryParamResource {

        @GET
        @Path("simple")
        @Produces
        public void getSimple(@QueryParam("q") String p, @QueryParam("m") String m) {
            assertEquals("a b c", p);
            assertEquals("1 2", m);
        }

        @GET
        @Path("encoded")
        @Produces
        public void getEncoded(@Encoded @QueryParam("q") String p,
                               @Encoded @QueryParam("m") String m) {
            assertEquals("a%20b+c", p);
            assertEquals("1+2", m);
        }

        @GET
        @Path("default")
        @Produces
        public void getDefault(@DefaultValue("b") @QueryParam("k") String p) {
            assertEquals("b", p);
        }

        @GET
        @Path("simpleList")
        @Produces
        public void getSimpleList(@QueryParam("q") List<String> p) {
            assertEquals(2, p.size());
            assertEquals("a b c", p.get(0));
            assertEquals("a c", p.get(1));
        }

        @GET
        @Path("encodedList")
        @Produces
        public void getEncodedList(@Encoded @QueryParam("q") List<String> p) {
            assertEquals(2, p.size());
            assertEquals("a%20b+c", p.get(0));
            assertEquals("a+c", p.get(1));
        }

        @GET
        @Path("defaultList")
        @Produces
        public void getDefaultList(@DefaultValue("a") @QueryParam("k") List<String> p) {
            assertEquals(1, p.size());
            assertEquals("a", p.get(0));
        }

        @GET
        @Path("simpleSet")
        @Produces
        public void getSimpleSet(@QueryParam("q") Set<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("a b c"));
            assertTrue(p.contains("a c"));
        }

        @GET
        @Path("encodedSet")
        @Produces
        public void getEncodedSet(@Encoded @QueryParam("q") Set<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("a%20b+c"));
            assertTrue(p.contains("a+c"));
        }

        @GET
        @Path("defaultSet")
        @Produces
        public void getDefaultSet(@DefaultValue("a") @QueryParam("k") Set<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("a"));
        }

        @GET
        @Path("simpleSortedSet")
        @Produces
        public void getSimpleSortedSet(@QueryParam("q") SortedSet<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("a b c"));
            assertTrue(p.contains("a c"));
        }

        @GET
        @Path("encodedSortedSet")
        @Produces
        public void getEncodedSortedSet(@Encoded @QueryParam("q") SortedSet<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("a%20b+c"));
            assertTrue(p.contains("a+c"));
        }

        @GET
        @Path("defaultSortedSet")
        @Produces
        public void getDefaultSortedSet(@DefaultValue("a") @QueryParam("k") SortedSet<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("a"));
        }
    }

    @Path("matrixParam")
    public static class MatrixParamResource {

        @GET
        @Path("simple")
        @Produces
        public void getSimple(@MatrixParam("m") String p) {
            assertEquals("a b+c", p);
        }

        @GET
        @Path("encoded")
        @Produces
        public void getEncoded(@Encoded @MatrixParam("m") String p) {
            assertEquals("a%20b+c", p);
        }

        @GET
        @Path("default")
        @Produces
        public void getDefault(@DefaultValue("b") @MatrixParam("k") String p) {
            assertEquals("b", p);
        }

        @GET
        @Path("none")
        @Produces
        public void getNone(@MatrixParam("m") String p) {
            assertNull(p);
        }

        @GET
        @Path("slash/")
        @Produces
        public void getSlash(@MatrixParam("m") String p) {
            assertEquals("a", p);
        }

        @GET
        @Path("slashNone/")
        @Produces
        public void getSlashNone(@MatrixParam("m") String p) {
            assertNull(p);
        }

        @GET
        @Path("simpleList1")
        @Produces
        public void getSimpleList1(@MatrixParam("m1") String p1, @MatrixParam("m2") String p2) {
            assertEquals("a", p1);
            assertEquals("b", p2);
        }

        @GET
        @Path("simpleList2")
        @Produces
        public void getSimpleList2(@MatrixParam("m") List<String> p) {
            assertEquals(2, p.size());
            assertEquals("a b", p.get(0));
            assertEquals("c", p.get(1));
        }

        @GET
        @Path("encodedList")
        @Produces
        public void getEncodedList(@Encoded @MatrixParam("m") List<String> p) {
            assertEquals(2, p.size());
            assertEquals("a%20b", p.get(0));
            assertEquals("c", p.get(1));
        }

        @GET
        @Path("defaultList")
        @Produces
        public void getDefaultList(@DefaultValue("a") @MatrixParam("k") List<String> p) {
            assertEquals(1, p.size());
            assertEquals("a", p.get(0));
        }

        @GET
        @Path("simpleSet")
        @Produces
        public void getSimpleSet(@MatrixParam("m") Set<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("a b"));
            assertTrue(p.contains("c"));
        }

        @GET
        @Path("encodedSet")
        @Produces
        public void getEncodedSet(@Encoded @MatrixParam("m") Set<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("a%20b"));
            assertTrue(p.contains("c"));
        }

        @GET
        @Path("defaultSet")
        @Produces
        public void getDefaultSet(@DefaultValue("a") @MatrixParam("k") Set<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("a"));
        }

        @GET
        @Path("simpleSortedSet")
        @Produces
        public void getSimpleSortedSet(@MatrixParam("m") SortedSet<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("a b"));
            assertTrue(p.contains("c"));
        }

        @GET
        @Path("encodedSortedSet")
        @Produces
        public void getEncodedSortedSet(@Encoded @MatrixParam("m") SortedSet<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("a%20b"));
            assertTrue(p.contains("c"));
        }

        @GET
        @Path("defaultSortedSet")
        @Produces
        public void getDefaultSortedSet(@DefaultValue("a") @MatrixParam("k") SortedSet<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("a"));
        }
    }

    @Path("formParam")
    public static class FormParamResource {

        @GET
        @Path("simple")
        @Produces
        public void getSimple(@FormParam("q") String p, @FormParam("m") String m) {
            assertEquals("a b c", p);
            assertEquals("1 2", m);
        }

        @GET
        @Path("encoded")
        @Produces
        public void getEncoded(@Encoded @FormParam("q") String p, @Encoded @FormParam("m") String m) {
            assertEquals("a%20b+c", p);
            assertEquals("1+2", m);
        }

        @GET
        @Path("default")
        @Produces
        public void getDefault(@DefaultValue("b") @FormParam("k") String p) {
            assertEquals("b", p);
        }

        @GET
        @Path("simpleList")
        @Produces
        public void getSimpleList(@FormParam("q") List<String> p) {
            assertEquals(2, p.size());
            assertEquals("a b c", p.get(0));
            assertEquals("a c", p.get(1));
        }

        @GET
        @Path("encodedList")
        @Produces
        public void getEncodedList(@Encoded @FormParam("q") List<String> p) {
            assertEquals(2, p.size());
            assertEquals("a%20b+c", p.get(0));
            assertEquals("a+c", p.get(1));
        }

        @GET
        @Path("defaultList")
        @Produces
        public void getDefaultList(@DefaultValue("a") @FormParam("k") List<String> p) {
            assertEquals(1, p.size());
            assertEquals("a", p.get(0));
        }

        @GET
        @Path("simpleSet")
        @Produces
        public void getSimpleSet(@FormParam("q") Set<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("a b c"));
            assertTrue(p.contains("a c"));
        }

        @GET
        @Path("encodedSet")
        @Produces
        public void getEncodedSet(@Encoded @FormParam("q") Set<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("a%20b+c"));
            assertTrue(p.contains("a+c"));
        }

        @GET
        @Path("defaultSet")
        @Produces
        public void getDefaultSet(@DefaultValue("a") @FormParam("k") Set<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("a"));
        }

        @GET
        @Path("simpleSortedSet")
        @Produces
        public void getSimpleSortedSet(@FormParam("q") SortedSet<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("a b c"));
            assertTrue(p.contains("a c"));
        }

        @GET
        @Path("encodedSortedSet")
        @Produces
        public void getEncodedSortedSet(@Encoded @FormParam("q") SortedSet<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("a%20b+c"));
            assertTrue(p.contains("a+c"));
        }

        @GET
        @Path("defaultSortedSet")
        @Produces
        public void getDefaultSortedSet(@DefaultValue("a") @FormParam("k") SortedSet<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("a"));
        }

        @GET
        @Path("entity1")
        @Produces
        public void getEntity1(@FormParam("q") String p, String e) {
            assertEquals("a b c", p);
            assertEquals("", e);
        }

        @GET
        @Path("entity2")
        @Produces
        public void getEntity2(String e, @FormParam("q") String p) {
            assertNull(p);
        }
    }

    @Path("cookieParam")
    public static class CookieParamResource {

        @GET
        @Path("simple")
        @Produces
        public void getSimple(@CookieParam("c") String p) {
            assertEquals("cookieVal", p);
        }

        @GET
        @Path("default")
        @Produces
        public void getDefault(@DefaultValue("b") @CookieParam("k") String p) {
            assertEquals("b", p);
        }

        @GET
        @Path("simpleList")
        @Produces
        public void getSimpleList(@CookieParam("c") List<String> p) {
            assertEquals(1, p.size());
            assertEquals("cookieVal", p.get(0));
        }

        @GET
        @Path("defaultList")
        @Produces
        public void getDefaultList(@DefaultValue("b") @CookieParam("k") List<String> p) {
            assertEquals(1, p.size());
            assertEquals("b", p.get(0));
        }

        @GET
        @Path("simpleSet")
        @Produces
        public void getSimpleSet(@CookieParam("c") Set<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("cookieVal"));
        }

        @GET
        @Path("defaultSet")
        @Produces
        public void getDefaultSet(@DefaultValue("b") @CookieParam("k") Set<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("b"));
        }

        @GET
        @Path("simpleSortedSet")
        @Produces
        public void getSimpleSortedSet(@CookieParam("c") SortedSet<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("cookieVal"));
        }

        @GET
        @Path("defaultSortedSet")
        @Produces
        public void getDefaultSortedSet(@DefaultValue("b") @CookieParam("k") SortedSet<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("b"));
        }

        @GET
        @Path("cookie")
        @Produces
        public void getCookie(@CookieParam("c") Cookie p) {
            assertEquals(Cookie.valueOf("$Version=1; c=cookieVal"), p);
        }

        @GET
        @Path("cookieDefault")
        @Produces
        public void getCookieDefault(@DefaultValue("b") @CookieParam("k") Cookie p) {
            assertEquals(Cookie.valueOf("$Version=1; k=b"), p);
        }

        @GET
        @Path("cookieEmpty")
        @Produces
        public void getCookieEmpty(@CookieParam("k") Cookie p) {
            assertNull(p);
        }

        @GET
        @Path("cookieSimpleList")
        @Produces
        public void getCookieSimpleList(@CookieParam("c") List<Cookie> p) {
            assertEquals(1, p.size());
            assertEquals(Cookie.valueOf("$Version=1; c=cookieVal"), p.get(0));
        }

        @GET
        @Path("cookieDefaultList")
        @Produces
        public void getCookieDefaultList(@DefaultValue("b") @CookieParam("k") List<Cookie> p) {
            assertEquals(1, p.size());
            assertEquals(Cookie.valueOf("$Version=1; k=b"), p.get(0));
        }

        @GET
        @Path("cookieEmptyList")
        @Produces
        public void getCookieEmptyList(@CookieParam("k") List<Cookie> p) {
            assertEquals(0, p.size());
        }

        @GET
        @Path("cookieSimpleSet")
        @Produces
        public void getCookieSimpleSet(@CookieParam("c") Set<Cookie> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains(Cookie.valueOf("$Version=1; c=cookieVal")));
        }

        @GET
        @Path("cookieDefaultSet")
        @Produces
        public void getCookieDefaultSet(@DefaultValue("b") @CookieParam("k") Set<Cookie> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains(Cookie.valueOf("$Version=1; k=b")));
        }

        @GET
        @Path("cookieSimpleSortedSet")
        @Produces
        public void getCookieSimpleSortedSet(@CookieParam("c") SortedSet<Cookie> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains(Cookie.valueOf("$Version=1; c=cookieVal")));
        }

        @GET
        @Path("cookieDefaultSortedSet")
        @Produces
        public void getCookieDefaultSortedSet(@DefaultValue("b") @CookieParam("k") SortedSet<Cookie> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains(Cookie.valueOf("$Version=1; k=b")));
        }

    }

    @Path("headerParam")
    public static class HeaderParamResource {

        @GET
        @Path("simple")
        @Produces
        public void getSimple(@HeaderParam("h") String p) {
            assertEquals("abc", p);
        }

        @GET
        @Path("default")
        @Produces
        public void getDefault(@DefaultValue("d") @HeaderParam("k") String p) {
            assertEquals("d", p);
        }

        @GET
        @Path("simpleList")
        @Produces
        public void getSimpleList(@HeaderParam("h") List<String> p) {
            assertEquals(2, p.size());
            assertEquals("abc", p.get(0));
            assertEquals("def", p.get(1));
        }

        @GET
        @Path("defaultList")
        @Produces
        public void getDefaultList(@DefaultValue("d") @HeaderParam("k") List<String> p) {
            assertEquals(1, p.size());
            assertEquals("d", p.get(0));
        }

        @GET
        @Path("emptyList")
        @Produces
        public void getEmptyList(@HeaderParam("k") List<String> p) {
            assertEquals(0, p.size());
        }

        @GET
        @Path("simpleSet")
        @Produces
        public void getSimpleSet(@HeaderParam("h") Set<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("abc"));
            assertTrue(p.contains("def"));
        }

        @GET
        @Path("defaultSet")
        @Produces
        public void getDefaultSet(@DefaultValue("d") @HeaderParam("k") Set<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("d"));
        }

        @GET
        @Path("simpleSortedSet")
        @Produces
        public void getSimpleSortedSet(@HeaderParam("h") SortedSet<String> p) {
            assertEquals(2, p.size());
            assertTrue(p.contains("abc"));
            assertTrue(p.contains("def"));
        }

        @GET
        @Path("defaultSortedSet")
        @Produces
        public void getDefaultSortedSet(@DefaultValue("d") @HeaderParam("k") SortedSet<String> p) {
            assertEquals(1, p.size());
            assertTrue(p.contains("d"));
        }

    }

    private void assertInvocation(String path) {
        assertInvocation(path, null, null, null, null);
    }

    private void assertInvocation(String path, MultivaluedMap<String, String> headers) {
        assertInvocation(path, null, null, null, headers);
    }

    private void assertInvocation(String path, String body, String type) {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl<String, String>();
        headers.add("Content-Type", type);
        assertInvocation(path, null, body, type, headers);
    }

    private void assertInvocation(String path, String queryString) {
        assertInvocation(path, queryString, null, null, null);
    }

    private void assertInvocation(String path,
                                  String queryString,
                                  String body,
                                  String type,
                                  MultivaluedMap<String, String> headers) {
        try {
            MockHttpServletRequest mockRequest =
                MockRequestConstructor.constructMockRequest("GET", path, MediaType.APPLICATION_XML);
            mockRequest.setQueryString(queryString);
            if (body != null) {
                mockRequest.setContent(body.getBytes());
                mockRequest.setContentType(type);
            }
            if (headers != null) {
                for (String name : headers.keySet()) {
                    mockRequest.addHeader(name, headers.get(name));
                }
            }
            MockHttpServletResponse mockResponse = invoke(mockRequest);
            assertEquals(204, mockResponse.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            fail("method invocation failed");
        }
    }

    public void testPathParam() {
        assertInvocation("pathParam/a%20b+c/simple");
        assertInvocation("pathParam;m1=1/a%20b+c/simple");
        assertInvocation("pathParam;m1=1;m2=2/a%20b+c;m3=3/simple");
        assertInvocation("pathParam/a%20b+c/encoded");
        assertInvocation("pathParam/a%20b+c/default");
        assertInvocation("pathParam/a%20b+c/simpleList");
        assertInvocation("pathParam/a%20b+c/simpleListMulti/a;m=1/b");
        assertInvocation("pathParam/a%20b+c/encodedList");
        assertInvocation("pathParam/a%20b+c/defaultList");
        assertInvocation("pathParam/a%20b+c/simpleSet");
        assertInvocation("pathParam/a%20b+c/encodedSet");
        assertInvocation("pathParam/a%20b+c/defaultSet");
        assertInvocation("pathParam/a%20b+c/simpleSortedSet");
        assertInvocation("pathParam/a%20b+c/encodedSortedSet");
        assertInvocation("pathParam/a%20b+c/defaultSortedSet");

        assertInvocation("pathParam/a%20b;m1=1/PathSegmentSimple");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentEncoded");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentDefault");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentEmpty");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentLast/a/b/c");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentMultiSimple/a%20b/c%20d/end");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentMultiEncoded/a%20b/c%20d/end");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentSimpleList/a%20b;m1=1/c%20d;m2=2");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentEncodedList/a%20b;m1=1/c%20d;m2=2");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentDefaultList/a%20b;m1=1/c%20d;m2=2");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentEmptyList/a%20b;m1=1/c%20d;m2=2");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentSimpleSet/a%20b;m1=1/c%20d;m2=2");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentEncodedSet/a%20b;m1=1/c%20d;m2=2");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentDefaultSet/a%20b;m1=1/c%20d;m2=2");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentSimpleSortedSet/a%20b;m1=1/c%20d;m2=2");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentEncodedSortedSet/a%20b;m1=1/c%20d;m2=2");
        assertInvocation("pathParam/a%20b;m1=1/PathSegmentDefaultSortedSet/a%20b;m1=1/c%20d;m2=2");
        assertInvocation("pathParam/a%20b+c/subresourcelocator/d%20e+f/subresourcestring/g%20h+i");
    }

    public void testQueryParam() {
        String query = "q=a%20b+c&q=a+c&m=1+2";
        assertInvocation("queryParam/simple", query);
        assertInvocation("queryParam/encoded", query);
        assertInvocation("queryParam/default", query);
        assertInvocation("queryParam/simpleList", query);
        assertInvocation("queryParam/encodedList", query);
        assertInvocation("queryParam/defaultList", query);
        assertInvocation("queryParam/simpleSet", query);
        assertInvocation("queryParam/encodedSet", query);
        assertInvocation("queryParam/defaultSet", query);
        assertInvocation("queryParam/simpleSortedSet", query);
        assertInvocation("queryParam/encodedSortedSet", query);
        assertInvocation("queryParam/defaultSortedSet", query);
    }

    public void testMatrixParam() {
        assertInvocation("matrixParam/simple;m=a%20b+c");
        assertInvocation("matrixParam/encoded;m=a%20b+c");
        assertInvocation("matrixParam/default;m=a%20b+c");
        assertInvocation("matrixParam/none");
        assertInvocation("matrixParam;m=1/none");
        assertInvocation("matrixParam/slash/;m=a");
        assertInvocation("matrixParam/slashNone;m=a/");
        assertInvocation("matrixParam/simpleList1;m1=a;m2=b");
        assertInvocation("matrixParam/simpleList2;m=a%20b;m=c");
        assertInvocation("matrixParam/encodedList;m=a%20b;m=c");
        assertInvocation("matrixParam/defaultList;m=a%20b;m=c");
        assertInvocation("matrixParam/simpleSet;m=a%20b;m=c");
        assertInvocation("matrixParam/encodedSet;m=a%20b;m=c");
        assertInvocation("matrixParam/defaultSet;m=a%20b;m=c");
        assertInvocation("matrixParam/simpleSortedSet;m=a%20b;m=c");
        assertInvocation("matrixParam/encodedSortedSet;m=a%20b;m=c");
        assertInvocation("matrixParam/defaultSortedSet;m=a%20b;m=c");
    }

    public void testFormParam() {
        String form = "q=a%20b+c&q=a+c&m=1+2";
        assertInvocation("formParam/simple", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/encoded", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/default", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/simpleList", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/encodedList", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/defaultList", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/simpleSet", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/encodedSet", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/defaultSet", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/simpleSortedSet", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/encodedSortedSet", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/defaultSortedSet", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/entity1", form, MediaType.APPLICATION_FORM_URLENCODED);
        assertInvocation("formParam/entity2", form, MediaType.APPLICATION_FORM_URLENCODED);
    }

    public void testCookieParam() {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl<String, String>();
        headers.add("Cookie", "$Version=1; c=cookieVal");
        assertInvocation("cookieParam/simple", headers);
        assertInvocation("cookieParam/default", headers);
        assertInvocation("cookieParam/simpleList", headers);
        assertInvocation("cookieParam/defaultList", headers);
        assertInvocation("cookieParam/simpleSet", headers);
        assertInvocation("cookieParam/defaultSet", headers);
        assertInvocation("cookieParam/simpleSortedSet", headers);
        assertInvocation("cookieParam/defaultSortedSet", headers);
        assertInvocation("cookieParam/cookie", headers);
        assertInvocation("cookieParam/cookieDefault", headers);
        assertInvocation("cookieParam/cookieEmpty", headers);
        assertInvocation("cookieParam/cookieSimpleList", headers);
        assertInvocation("cookieParam/cookieDefaultList", headers);
        assertInvocation("cookieParam/cookieSimpleSet", headers);
        assertInvocation("cookieParam/cookieDefaultSet", headers);
        assertInvocation("cookieParam/cookieSimpleSortedSet", headers);
        assertInvocation("cookieParam/cookieDefaultSortedSet", headers);
    }

    public void testHeaderParam() {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl<String, String>();
        headers.add("h", "abc");
        headers.add("h", "def");
        assertInvocation("headerParam/simple", headers);
        assertInvocation("headerParam/default", headers);
        assertInvocation("headerParam/simpleList", headers);
        assertInvocation("headerParam/defaultList", headers);
        assertInvocation("headerParam/emptyList", headers);
        assertInvocation("headerParam/simpleSet", headers);
        assertInvocation("headerParam/defaultSet", headers);
        assertInvocation("headerParam/simpleSortedSet", headers);
        assertInvocation("headerParam/defaultSortedSet", headers);
    }

}
