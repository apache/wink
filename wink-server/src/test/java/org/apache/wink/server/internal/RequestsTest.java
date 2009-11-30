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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class RequestsTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {RequestRootResource.class};
    }

    @Path("/root")
    public static class RequestRootResource {

        @GET
        public Response getOnlyMediaType(@Context Request request) {
            List<Variant> responseVariants =
                Variant.mediaTypes(MediaType.TEXT_PLAIN_TYPE,
                                   MediaType.APPLICATION_XML_TYPE,
                                   MediaType.APPLICATION_JSON_TYPE).add().build();
            Variant bestResponseVariant = request.selectVariant(responseVariants);
            if (bestResponseVariant != null) {
                return Response.ok("Hello world!").variant(request.selectVariant(responseVariants))
                    .build();
            }
            return Response.notAcceptable(responseVariants).build();
        }

        @GET
        @Path("language")
        public Response getOnlyLanguage(@Context Request request) {
            List<Variant> responseVariants =
                Variant.languages(Locale.ENGLISH, Locale.FRENCH, Locale.KOREAN).add().build();
            Variant bestResponseVariant = request.selectVariant(responseVariants);
            if (bestResponseVariant != null) {
                return Response.ok("Hello world!").variant(bestResponseVariant).build();
            }
            return Response.notAcceptable(responseVariants).build();
        }

        @GET
        @Path("encoding")
        public Response getOnlyEncoding(@Context Request request) {
            List<Variant> responseVariants =
                Collections.unmodifiableList(Variant.encodings("gzip", "identity", "deflate").add()
                    .build());
            Variant bestResponseVariant = request.selectVariant(responseVariants);
            if (bestResponseVariant != null) {
                return Response.ok("Hello world!").variant(bestResponseVariant).build();
            }
            return Response.notAcceptable(responseVariants).build();
        }

        @GET
        @Path("noidentityencoding")
        public Response getNoIdentityEncoding(@Context Request request) {
            List<Variant> responseVariants =
                Collections.unmodifiableList(Variant.encodings("gzip", "deflate").add().build());
            Variant bestResponseVariant = request.selectVariant(responseVariants);
            if (bestResponseVariant != null) {
                return Response.ok("Hello world!").variant(bestResponseVariant).build();
            }
            return Response.notAcceptable(responseVariants).build();
        }

        @GET
        @Path("charset")
        public Response getOnlyCharset(@Context Request request) {
            List<Variant> responseVariants =
                Variant.mediaTypes(MediaType.valueOf(MediaType.TEXT_PLAIN + ";charset=iso-8859-1"),
                                   MediaType.valueOf(MediaType.TEXT_PLAIN + ";charset=UTF-8"),
                                   MediaType.valueOf(MediaType.TEXT_PLAIN + ";charset=shift_jis"))
                    .add().build();
            Variant bestResponseVariant = request.selectVariant(responseVariants);
            if (bestResponseVariant != null) {
                return Response.ok("Hello world!").variant(bestResponseVariant).build();
            }
            return Response.notAcceptable(responseVariants).build();
        }

        @GET
        @Path("multipleheaders")
        public Response getMultipleAcceptHeaders(@Context Request request) {
            List<Variant> responseVariants =
                Collections.unmodifiableList(Variant.mediaTypes(MediaType.APPLICATION_XML_TYPE,
                                                                MediaType.APPLICATION_JSON_TYPE,
                                                                MediaType.TEXT_PLAIN_TYPE)
                    .encodings("gzip", "identity", "deflate").languages(Locale.ENGLISH,
                                                                        Locale.FRENCH,
                                                                        Locale.US).add().build());
            Variant bestResponseVariant = request.selectVariant(responseVariants);
            if (bestResponseVariant != null) {
                return Response.ok("Hello world!").variant(bestResponseVariant).build();
            }
            return Response.notAcceptable(responseVariants).build();
        }

        @GET
        @Path("moremultipleheaders")
        public Response getMoreMultipleAcceptHeaders(@Context Request request) {
            List<Variant> responseVariants =
                Collections.unmodifiableList(Variant
                    .mediaTypes(MediaType.valueOf(MediaType.APPLICATION_JSON + ";charset=utf-8"),
                                MediaType.valueOf(MediaType.TEXT_PLAIN + ";charset=shift_jis"),
                                MediaType.valueOf(MediaType.APPLICATION_XML),
                                MediaType
                                    .valueOf(MediaType.APPLICATION_JSON + ";charset=iso-8859-1"),
                                MediaType.valueOf(MediaType.TEXT_PLAIN + ";charset=iso-8859-1"))
                    .encodings("gzip", "identity", "deflate").languages(Locale.ENGLISH,
                                                                        Locale.FRENCH,
                                                                        Locale.US).add().build());
            Variant bestResponseVariant = request.selectVariant(responseVariants);
            if (bestResponseVariant != null) {
                return Response.ok("Hello world!").variant(bestResponseVariant).build();
            }
            return Response.notAcceptable(responseVariants).build();
        }
    }

    public void testSimpleMediaTypeSelect() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/root", MediaType.TEXT_PLAIN);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals(HttpHeaders.ACCEPT, response.getHeader(HttpHeaders.VARY));

        request = MockRequestConstructor.constructMockRequest("GET", "/root", "text/*");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals(HttpHeaders.ACCEPT, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET", "/root", MediaType.APPLICATION_XML);
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_XML, response.getContentType());
        assertEquals(HttpHeaders.ACCEPT, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET", "/root", MediaType.APPLICATION_JSON);
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getContentType());
        assertEquals(HttpHeaders.ACCEPT, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root",
                                                        MediaType.APPLICATION_ATOM_XML);
        response = invoke(request);
        assertEquals(406, response.getStatus());
        assertEquals(HttpHeaders.ACCEPT, response.getHeader(HttpHeaders.VARY));
    }

    public void testMultipleMediaTypeSelect() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root",
                                                        MediaType.TEXT_PLAIN + ";q=1.0,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=0.9");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(response.getContentType(), MediaType.TEXT_PLAIN);
        assertEquals(HttpHeaders.ACCEPT, response.getHeader(HttpHeaders.VARY));
        assertNull(response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertNull(response.getHeader(HttpHeaders.CONTENT_ENCODING));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root",
                                                        MediaType.APPLICATION_XML + ";q=0.9,"
                                                            + "text/*"
                                                            + ";q=1.0");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals(HttpHeaders.ACCEPT, response.getHeader(HttpHeaders.VARY));
        assertNull(response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertNull(response.getHeader(HttpHeaders.CONTENT_ENCODING));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root",
                                                        MediaType.TEXT_PLAIN + ";q=0.9,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=1.0");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_XML, response.getContentType());
        assertEquals(HttpHeaders.ACCEPT, response.getHeader(HttpHeaders.VARY));
        assertNull(response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertNull(response.getHeader(HttpHeaders.CONTENT_ENCODING));

        request =
            MockRequestConstructor.constructMockRequest("GET", "/root", MediaType.WILDCARD + ","
                + MediaType.TEXT_PLAIN
                + ";q=0.0,"
                + MediaType.APPLICATION_XML
                + ";q=0.0");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getContentType());
        assertEquals(HttpHeaders.ACCEPT, response.getHeader(HttpHeaders.VARY));
        assertNull(response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertNull(response.getHeader(HttpHeaders.CONTENT_ENCODING));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root",
                                                        MediaType.APPLICATION_ATOM_XML);

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root",
                                                        MediaType.APPLICATION_ATOM_XML + ";q=1.0,"
                                                            + MediaType.TEXT_PLAIN
                                                            + ";q=0.0,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=0.0");
        response = invoke(request);
        assertEquals(406, response.getStatus());
        assertEquals(HttpHeaders.ACCEPT, response.getHeader(HttpHeaders.VARY));
        assertNull(response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertNull(response.getHeader(HttpHeaders.CONTENT_ENCODING));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root",
                                                        MediaType.WILDCARD + ";q=0.1 ,"
                                                            + MediaType.APPLICATION_ATOM_XML
                                                            + ";q=1.0,"
                                                            + MediaType.TEXT_PLAIN
                                                            + ";q=0.0,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=0.0");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getContentType());
        assertEquals(HttpHeaders.ACCEPT, response.getHeader(HttpHeaders.VARY));
        assertNull(response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertNull(response.getHeader(HttpHeaders.CONTENT_ENCODING));
    }

    public void testSimpleLanguagesSelect() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/language",
                                                        MediaType.TEXT_PLAIN);
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals("en", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT_LANGUAGE, response.getHeader(HttpHeaders.VARY));
        assertNull(response.getHeader(HttpHeaders.CONTENT_ENCODING));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/language",
                                                        MediaType.TEXT_PLAIN);
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "fr");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals("fr", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT_LANGUAGE, response.getHeader(HttpHeaders.VARY));
        assertNull(response.getHeader(HttpHeaders.CONTENT_ENCODING));
    }

    public void testMultipleLanguagesSelect() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/language",
                                                        MediaType.TEXT_PLAIN);
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en;q=0.6,fr;q=0.5");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals("en", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT_LANGUAGE, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/language",
                                                        MediaType.TEXT_PLAIN);
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en;q=0.6,fr;q=0.7");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals("fr", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT_LANGUAGE, response.getHeader(HttpHeaders.VARY));
    }

    public void testSimpleEncoding() throws Exception {
        // test that a null Accept-Encoding means the only thing that can come
        // back is identity
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/encoding",
                                                        MediaType.TEXT_PLAIN);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals("identity", response.getHeader(HttpHeaders.CONTENT_ENCODING));
        assertEquals(HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/encoding",
                                                        MediaType.TEXT_PLAIN);
        request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals("gzip", response.getHeader(HttpHeaders.CONTENT_ENCODING));
        assertEquals(HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/noidentityencoding",
                                                        MediaType.TEXT_PLAIN);
        response = invoke(request);
        assertEquals(406, response.getStatus());
        assertEquals(HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/noidentityencoding",
                                                        MediaType.TEXT_PLAIN);
        request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals("gzip", response.getHeader(HttpHeaders.CONTENT_ENCODING));
        assertEquals(HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));
    }

    public void testMultipleEncoding() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/encoding",
                                                        MediaType.TEXT_PLAIN);
        request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip;q=0.8,deflate;q=0.7");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals("gzip", response.getHeader(HttpHeaders.CONTENT_ENCODING));
        assertEquals(HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/encoding",
                                                        MediaType.TEXT_PLAIN);
        request.addHeader(HttpHeaders.ACCEPT_ENCODING, "deflate;q=0.8,gzip;q=0.7");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals("deflate", response.getHeader(HttpHeaders.CONTENT_ENCODING));
        assertEquals(HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));
    }

    public void testCharset() throws Exception {
        // test that a null Accept-Charset means iso-8859-1 is automatically
        // chosen
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/charset",
                                                        MediaType.TEXT_PLAIN);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN + ";charset=iso-8859-1", response.getContentType());
        assertEquals(HttpHeaders.ACCEPT, response.getHeader(HttpHeaders.VARY));

        /*
         * due to not mentioning of iso-8859-1 and no wildcard, iso-8859-1 is
         * given a q-factor of 1.0 and since it is the first in the list of
         * variants, should get chosen
         */
        request =
            MockRequestConstructor.constructMockRequest("GET", "/root/charset", MediaType.WILDCARD);
        request.addHeader(HttpHeaders.ACCEPT_CHARSET, "shift_jis");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN + ";charset=iso-8859-1", response.getContentType());
        assertEquals("Accept, Accept-Charset", response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET", "/root/charset", MediaType.WILDCARD);
        request.addHeader(HttpHeaders.ACCEPT_CHARSET, "shift_jis, *;q=0.5");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN + ";charset=shift_jis", response.getContentType());
        assertEquals("Accept, Accept-Charset", response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET", "/root/charset", MediaType.WILDCARD);
        request.addHeader(HttpHeaders.ACCEPT_CHARSET, "SHIFT_jis, *;q=0.5");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN + ";charset=shift_jis", response.getContentType());
        assertEquals("Accept, Accept-Charset", response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET", "/root/charset", MediaType.WILDCARD);
        request.addHeader(HttpHeaders.ACCEPT_CHARSET, "shift_jis, iso-8859-1;q=0.5");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN + ";charset=shift_jis", response.getContentType());
        assertEquals("Accept, Accept-Charset", response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET", "/root/charset", MediaType.WILDCARD);
        request.addHeader(HttpHeaders.ACCEPT_CHARSET, "iso-8859-1");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN + ";charset=iso-8859-1", response.getContentType());
        assertEquals("Accept, Accept-Charset", response.getHeader(HttpHeaders.VARY));

        /*
         * due to not mentioning of iso-8859-1 and no wildcard, iso-8859-1 is
         * given a q-factor of 1.0
         */
        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/charset",
                                                        MediaType.TEXT_PLAIN);
        request.addHeader(HttpHeaders.ACCEPT_CHARSET, "abcd");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN + ";charset=iso-8859-1", response.getContentType());
        assertEquals("Accept, Accept-Charset", response.getHeader(HttpHeaders.VARY));
    }

    public void testSimpleMultipleAcceptHeaders() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/multipleheaders",
                                                        MediaType.TEXT_PLAIN + ";q=1.0,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=0.8");
        request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip;q=0.8,deflate;q=0.7");
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-us");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals("gzip", response.getHeader(HttpHeaders.CONTENT_ENCODING));
        assertEquals("en-US", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT + ", "
            + HttpHeaders.ACCEPT_LANGUAGE
            + ", "
            + HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));
        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/multipleheaders",
                                                        MediaType.TEXT_PLAIN + ";q=0.9,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=1.0");
        request.addHeader(HttpHeaders.ACCEPT_ENCODING, "deflate;q=0.8,gzip;q=0.7");
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-us;q=0.9,fr;q=1.0");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_XML, response.getContentType());
        assertEquals("deflate", response.getHeader(HttpHeaders.CONTENT_ENCODING));
        assertEquals("fr", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT + ", "
            + HttpHeaders.ACCEPT_LANGUAGE
            + ", "
            + HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/multipleheaders",
                                                        "text/*" + ";q=0.9,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=1.0");
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "fr;q=1.0, en-us;q=0.9");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_XML, response.getContentType());
        assertEquals("fr", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT + ", "
            + HttpHeaders.ACCEPT_LANGUAGE
            + ", "
            + HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));
    }

    public void testMoreSimpleMultipleAcceptHeaders() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/moremultipleheaders",
                                                        MediaType.TEXT_PLAIN + ";q=1.0,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=0.8");
        request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip;q=0.8,deflate;q=0.7");
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-us");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN + ";charset=shift_jis", response.getContentType());
        assertEquals("gzip", response.getHeader(HttpHeaders.CONTENT_ENCODING));
        assertEquals("en-US", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT + ", "
            + HttpHeaders.ACCEPT_LANGUAGE
            + ", "
            + HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/moremultipleheaders",
                                                        MediaType.TEXT_PLAIN + ";q=0.9,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=1.0");
        request.addHeader(HttpHeaders.ACCEPT_ENCODING, "deflate;q=0.8,gzip;q=0.7");
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-us;q=0.9,fr;q=1.0");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_XML, response.getContentType());
        assertEquals("deflate", response.getHeader(HttpHeaders.CONTENT_ENCODING));
        assertEquals("fr", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT + ", "
            + HttpHeaders.ACCEPT_LANGUAGE
            + ", "
            + HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/moremultipleheaders",
                                                        "text/*" + ";q=0.9,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=1.0");
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "fr;q=1.0, en-us;q=0.9");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_XML, response.getContentType());
        assertEquals("fr", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT + ", "
            + HttpHeaders.ACCEPT_LANGUAGE
            + ", "
            + HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/moremultipleheaders",
                                                        "text/*" + ";q=0.9,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=1.0");
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "fr;q=1.0, en-us;q=0.9");
        request.addHeader(HttpHeaders.ACCEPT_CHARSET, "shift_jis, *;q=0.8");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_XML, response.getContentType());
        assertEquals("fr", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT + ", "
            + HttpHeaders.ACCEPT_LANGUAGE
            + ", "
            + HttpHeaders.ACCEPT_ENCODING, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/moremultipleheaders",
                                                        "text/*" + ";q=0.9,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=0.8");
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "fr;q=1.0, en-us;q=0.9");
        request.addHeader(HttpHeaders.ACCEPT_CHARSET, "shift_jis, *;q=0.8");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN + ";charset=shift_jis", response.getContentType());
        assertEquals("fr", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT + ", "
            + HttpHeaders.ACCEPT_LANGUAGE
            + ", "
            + HttpHeaders.ACCEPT_ENCODING
            + ", "
            + HttpHeaders.ACCEPT_CHARSET, response.getHeader(HttpHeaders.VARY));

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/root/moremultipleheaders",
                                                        "text/*" + ";q=0.9,"
                                                            + MediaType.APPLICATION_XML
                                                            + ";q=0.8");
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "fr;q=1.0, en-us;q=0.9");
        request.addHeader(HttpHeaders.ACCEPT_CHARSET, "shift_jis;q=0.7");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN + ";charset=iso-8859-1", response.getContentType());
        assertEquals("fr", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(HttpHeaders.ACCEPT + ", "
            + HttpHeaders.ACCEPT_LANGUAGE
            + ", "
            + HttpHeaders.ACCEPT_ENCODING
            + ", "
            + HttpHeaders.ACCEPT_CHARSET, response.getHeader(HttpHeaders.VARY));
    }

}
