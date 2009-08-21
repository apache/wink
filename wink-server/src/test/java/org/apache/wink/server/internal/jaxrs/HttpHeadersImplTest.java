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
package org.apache.wink.server.internal.jaxrs;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class HttpHeadersImplTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {TestResource.class, MultipleHeaders.class};
    }

    @Path("/test")
    public static class TestResource {
        @GET
        @Produces("text/plain")
        public void getFoo(@Context HttpHeaders headers) {

            assertNotNull(headers);

            // acceptable languages
            List<Locale> locales = headers.getAcceptableLanguages();
            assertEquals(3, locales.size());
            assertEquals(new Locale("en", "us"), locales.get(0));
            assertEquals(new Locale("he"), locales.get(1));
            assertEquals(new Locale("en"), locales.get(2));

            // acceptable media-types
            List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
            assertEquals(3, mediaTypes.size());
            assertEquals(MediaType.valueOf("text/plain"), mediaTypes.get(0));
            assertEquals(MediaType.valueOf("text/html;q=0.5"), mediaTypes.get(1));
            assertEquals(MediaType.valueOf("application/xml;q=0.3"), mediaTypes.get(2));

            // Cookies
            Map<String, Cookie> cookies = headers.getCookies();
            assertEquals(2, cookies.size());
            assertEquals(new Cookie("cookie1_name", "cookie1_val"), cookies.get("cookie1_name"));
            assertEquals(new Cookie("cookie2_name", "cookie2_val"), cookies.get("cookie2_name"));

            // Language
            Locale language = headers.getLanguage();
            assertEquals(new Locale("en", "us"), language);

            // MediaType
            MediaType mediaType = headers.getMediaType();
            assertEquals("application/xml", mediaType.toString());

            // header
            List<String> header = headers.getRequestHeader("header1");
            List<String> headerUpper = headers.getRequestHeader("HeaDer1");
            assertEquals(2, header.size());
            assertEquals("value1", header.get(0));
            assertEquals("value2", header.get(1));
            assertEquals(header, headerUpper);

            header = headers.getRequestHeader("header2");
            assertEquals(1, header.size());
            assertEquals("value2", header.get(0));

            // all headers
            MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
            assertNotNull(requestHeaders);
            assertEquals(7, requestHeaders.size());
            assertEquals(1, requestHeaders.get("Accept").size());
            assertEquals("application/xml;q=0.3, text/plain, text/html;q=0.5", requestHeaders
                .get("Accept").get(0));
            assertEquals(1, requestHeaders.get("Content-Type").size());
            assertEquals("application/xml", requestHeaders.get("Content-Type").get(0));
            assertEquals(1, requestHeaders.get("Content-Language").size());
            assertEquals("en-us, he", requestHeaders.get("Content-Language").get(0));
            assertEquals(2, requestHeaders.get("Cookie").size());
            assertEquals("$Version=1; cookie1_name=cookie1_val", requestHeaders.get("Cookie")
                .get(0));
            assertEquals("$Version=1; cookie2_name=cookie2_val", requestHeaders.get("Cookie")
                .get(1));
            assertEquals(1, requestHeaders.get("Accept-Language").size());
            assertEquals("en;q=0.3, en-us, he;q=0.5", requestHeaders.get("Accept-Language").get(0));
            assertEquals(2, requestHeaders.get("Header1").size());
            assertEquals("value1", requestHeaders.get("Header1").get(0));
            assertEquals("value2", requestHeaders.get("Header1").get(1));
            assertEquals(1, requestHeaders.get("Header2").size());
            assertEquals("value2", requestHeaders.get("Header2").get(0));

            return;
        }

        @Path("/negative")
        @GET
        @Produces("text/plain")
        public void getNegative(@Context HttpHeaders headers) {
            assertNotNull(headers);

            List<Locale> locales = headers.getAcceptableLanguages();
            assertTrue(locales.isEmpty());

            List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
            assertEquals(1, mediaTypes.size());
            assertEquals("*/*", mediaTypes.get(0).toString());

            // Cookies
            Map<String, Cookie> cookies = headers.getCookies();
            assertTrue(cookies.isEmpty());

            Locale language = headers.getLanguage();
            assertNull(language);

            // MediaType
            MediaType mediaType = headers.getMediaType();
            assertNull(mediaType);

            // headers
            List<String> header = headers.getRequestHeader("header1");
            assertNull(header);

            MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
            assertNotNull(requestHeaders);
            assertEquals(1, requestHeaders.size());
            assertEquals(1, requestHeaders.get("Accept").size());
            assertEquals("*/*", requestHeaders.get("Accept").get(0));
            return;
        }
    }

    @Path("/multipleheaders")
    public static class MultipleHeaders {

        private @Context
        HttpHeaders httpHeaders;

        @GET
        @Produces("text/plain")
        public String getMultiple() {
            StringBuilder sb = new StringBuilder();
            for (Locale l : httpHeaders.getAcceptableLanguages()) {
                sb.append(l.getLanguage());
                sb.append(",");
            }
            return sb.toString();
        }
    }

    @Test
    public void testHttpHeaderContext() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/test",
                                      "application/xml;q=0.3, text/plain, text/html;q=0.5");
        servletRequest.addHeader("Content-Type", "application/xml");
        servletRequest.addHeader("Content-Language", "en-us, he");
        servletRequest.addHeader("Cookie", "$Version=1; cookie1_name=cookie1_val");
        servletRequest.addHeader("Cookie", "$Version=1; cookie2_name=cookie2_val");
        servletRequest.addHeader("Accept-Language", "en;q=0.3, en-us, he;q=0.5");
        servletRequest.addHeader("header1", "value1");
        servletRequest.addHeader("header1", "value2");
        servletRequest.addHeader("header2", "value2");
        invoke(servletRequest);
    }

    @Test
    public void testHttpHeaderContextNegative() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("GET", "/test/negative", "*/*");
        invoke(servletRequest);
    }

    @Test
    public void testMultipleHeaders() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/multipleheaders", "abcd/efgh");
        request.addHeader("Accept", "xyz/def");
        MockHttpServletResponse response = invoke(request);
        assertEquals(406, response.getStatus());

        request =
            MockRequestConstructor.constructMockRequest("GET", "/multipleheaders", "abcd/efgh");
        request.addHeader("Accept", "xyz/def");
        request.addHeader("Accept", "text/plain");
        request.addHeader("Accept-Language", Locale.JAPANESE.getLanguage() + ","
            + Locale.FRENCH.getLanguage());

        response = invoke(request);

        assertEquals(200, response.getStatus());
        assertEquals("ja,fr,", response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("GET", "/multipleheaders", "abcd/efgh");
        request.addHeader("Accept", "xyz/def, text/plain");
        request.addHeader("Accept-Language", Locale.JAPANESE.getLanguage());
        request.addHeader("Accept-Language", Locale.FRENCH.getLanguage());

        response = invoke(request);

        assertEquals(200, response.getStatus());
        assertEquals("ja,fr,", response.getContentAsString());
    }
}
