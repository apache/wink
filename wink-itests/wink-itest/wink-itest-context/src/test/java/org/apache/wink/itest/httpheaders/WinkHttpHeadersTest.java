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

package org.apache.wink.itest.httpheaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpException;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.EntityType;
import org.apache.wink.client.RestClient;
import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkHttpHeadersTest extends TestCase {

    private static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/httpheaders";
    }

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableLanguages()} that if given no
     * acceptable languages, that it will return the server default locale back.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testAcceptableLanguagesNoneGiven() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/acceptablelanguages").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("acceptablelanguages:", response.getEntity(new EntityType<String>() {
        }));
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableLanguages()} that if given a
     * language, it will be the only language in the list.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testAcceptableLanguagesOneGiven() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders//acceptablelanguages")
                .header("Accept-Language", "de").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("acceptablelanguages:de:", response.getEntity(new EntityType<String>() {
        }));
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableLanguages()} that if given multiple
     * languages, all will be returned in the list.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testAcceptableLanguagesManyGiven() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders//acceptablelanguages")
                .header("Accept-Language", "de", "en", "zh").get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(new EntityType<String>() {
        });
        assertTrue(responseBody, responseBody.startsWith("acceptablelanguages:"));
        assertTrue(responseBody, responseBody.contains(":de:"));
        assertTrue(responseBody, responseBody.contains(":en:"));
        assertTrue(responseBody, responseBody.contains(":zh:"));
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableLanguages()} that if given multiple
     * languages, all will be returned in the list sorted by their quality
     * value.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testAcceptableLanguagesManyGivenQSort() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders//acceptablelanguages")
                .header("Accept-Language", "de;q=0.6", "en;q=0.8", "zh;q=0.7").get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(new EntityType<String>() {
        });
        assertEquals("acceptablelanguages:en:zh:de:", responseBody);

        response =
            client.resource(getBaseURI() + "/context/httpheaders//acceptablelanguages")
                .header("Accept-Language", "de;q=0.6").header("Accept-Language", "en;q=0.8")
                .header("Accept-Language", "zh;q=0.7").get();
        assertEquals(200, response.getStatusCode());
        responseBody = response.getEntity(new EntityType<String>() {
        });
        assertEquals("acceptablelanguages:en:zh:de:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableMediaTypes()} that if given no
     * Accept header, wildcard/wildcard is returned.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testAcceptableMediaTypesNoneGiven() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders//acceptablemediatypes").get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(new EntityType<String>() {
        });
        assertEquals("acceptablemediatypes:*/*:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableMediaTypes()} that if given a
     * single Accept header value, it is returned.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testAcceptableMediaTypesOneGiven() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders//acceptablemediatypes")
                .accept(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("acceptablemediatypes:text/plain:", responseBody);
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getFirst("Content-Type"));

        response =
            client.resource(getBaseURI() + "/context/httpheaders/acceptablemediatypes")
                .accept(MediaType.TEXT_PLAIN_TYPE).get();
        assertEquals(200, response.getStatusCode());
        responseBody = response.getEntity(String.class);
        assertEquals("acceptablemediatypes:text/plain:", responseBody);
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getFirst("Content-Type"));

        response =
            client.resource(getBaseURI() + "/context/httpheaders//acceptablemediatypes")
                .header("accept", MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatusCode());
        responseBody = response.getEntity(String.class);
        assertEquals("acceptablemediatypes:text/plain:", responseBody);
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getFirst("Content-Type"));
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableMediaTypes()} that if given
     * multiple Accept header values, the values are sorted by q-value.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testAcceptableMediaTypesManyGiven() throws HttpException, IOException {
        Map<String, String> qMap1 = new HashMap<String, String>();
        qMap1.put("q", "1.0");
        MediaType textPlainTypeQ1 = new MediaType("text", "plain", qMap1);

        Map<String, String> qMap06 = new HashMap<String, String>();
        qMap06.put("q", "0.6");
        MediaType wildCardQ06 = new MediaType("*", "*", qMap06);

        Map<String, String> qMap07 = new HashMap<String, String>();
        qMap07.put("q", "0.7");
        MediaType jsonQ07 = new MediaType("application", "json", qMap07);

        Map<String, String> qMap08 = new HashMap<String, String>();
        qMap08.put("q", "0.8");
        MediaType textXMLQ08 = new MediaType("text", "xml", qMap08);

        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/acceptablemediatypes")
                .accept(textPlainTypeQ1, wildCardQ06, jsonQ07, textXMLQ08).get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("acceptablemediatypes:text/plain:text/xml:application/json:*/*:", responseBody);
        assertEquals("text/plain;q=1.0", response.getHeaders().getFirst("Content-Type"));

        response =
            client.resource(getBaseURI() + "/context/httpheaders/acceptablemediatypes")
                .accept(textPlainTypeQ1).accept(wildCardQ06).accept(jsonQ07).accept(textXMLQ08)
                .get();
        assertEquals(200, response.getStatusCode());
        responseBody = response.getEntity(String.class);
        assertEquals("acceptablemediatypes:text/plain:text/xml:application/json:*/*:", responseBody);
        assertEquals("text/plain;q=1.0", response.getHeaders().getFirst("Content-Type"));

        response =
            client.resource(getBaseURI() + "/context/httpheaders/acceptablemediatypes")
                .header("Accept",
                        "text/plain;q=1.0,*/*;q=0.6, application/json;q=0.7,text/xml;q=0.8").get();
        assertEquals(200, response.getStatusCode());
        responseBody = response.getEntity(String.class);
        assertEquals("acceptablemediatypes:text/plain:text/xml:application/json:*/*:", responseBody);
        assertEquals("text/plain;q=1.0", response.getHeaders().getFirst("Content-Type"));

        response =
            client.resource(getBaseURI() + "/context/httpheaders/acceptablemediatypes")
                .header("Accept", "text/plain;q=1.0").header("Accept", "*/*;q=0.6")
                .header("Accept", "application/json;q=0.7").header("Accept", "text/xml;q=0.8")
                .get();
        assertEquals(200, response.getStatusCode());
        responseBody = response.getEntity(String.class);
        assertEquals("acceptablemediatypes:text/plain:text/xml:application/json:*/*:", responseBody);
        assertEquals("text/plain;q=1.0", response.getHeaders().getFirst("Content-Type"));
    }

    /**
     * Tests {@link HttpHeaders#getMediaType()} that if given a text/plain, the
     * method will return text/plain.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testMediaTypesRequestTextPlain() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/requestmediatype")
                .contentType(MediaType.TEXT_PLAIN).post("Hello world!");
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("mediatype:text/plain:", responseBody);

        response =
            client.resource(getBaseURI() + "/context/httpheaders/requestmediatype")
                .contentType("text/plain").post("Hello world!");
        assertEquals(200, response.getStatusCode());
        responseBody = response.getEntity(String.class);
        assertEquals("mediatype:text/plain:", responseBody);

        response =
            client.resource(getBaseURI() + "/context/httpheaders/requestmediatype")
                .header("Content-Type", "text/plain").post("Hello world!");
        assertEquals(200, response.getStatusCode());
        responseBody = response.getEntity(String.class);
        assertEquals("mediatype:text/plain:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getMediaType()} when a non-standard content type
     * is sent in.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testMediaTypesRequestCustomContentType() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/requestmediatype")
                .contentType("defg/abcd").post("Hello world!");
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("mediatype:defg/abcd:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getMediaType()} when no request entity is given.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testMediaTypesRequestNoRequestEntity() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/requestmediatype").post(null);
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("mediatype:null:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getLanguage()} when no language is given in the
     * request.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testLanguageNoneGiven() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/language").post(null);
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("language:null:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getLanguage()} when English language is given in
     * the request.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testLanguageEnglishGiven() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/language")
                .contentType("text/plain").header(HttpHeaders.CONTENT_LANGUAGE, "en")
                .post("Hello world!");
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("language:en:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getLanguage()} when Chinese language is given in
     * the request.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testLanguageChineseGiven() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/language")
                .contentType("text/plain").header(HttpHeaders.CONTENT_LANGUAGE, "zh")
                .post("Hello world!");
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("language:zh:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getCookies()} when no cookies are given.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testCookiesNone() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/cookies")
                .contentType("text/plain").header(HttpHeaders.CONTENT_LANGUAGE, "zh")
                .post("Hello world!");
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("cookies:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getCookies()} when given a single cookie.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testCookiesOneGiven() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/cookies")
                .cookie(new Cookie("foo", "bar")).post(null);
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("cookies:foo=bar:", responseBody);

        response =
            client.resource(getBaseURI() + "/context/httpheaders/cookies").cookie("foo=bar")
                .post(null);
        assertEquals(200, response.getStatusCode());
        responseBody = response.getEntity(String.class);
        assertEquals("cookies:foo=bar:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getCookies()} when given multiple cookies.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testCookiesManyGiven() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/cookies")
                .cookie(new Cookie("foo", "bar")).cookie(new Cookie("foo2", "bar2")).post(null);
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("cookies:foo=bar:foo2=bar2:", responseBody);

        response =
            client.resource(getBaseURI() + "/context/httpheaders/cookies").cookie("foo=bar")
                .cookie("foo2=bar2").post(null);
        assertEquals(200, response.getStatusCode());
        responseBody = response.getEntity(String.class);
        assertEquals("cookies:foo=bar:foo2=bar2:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeader(String)} when given a null
     * value.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeaderNoneGivenIllegalArgument() throws HttpException, IOException {
        ClientResponse response = client.resource(getBaseURI() + "/context/httpheaders/").get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("requestheader:null:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeader(String)} when requesting header
     * values for a non-existent header name.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeaderNonexistentHeader() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/" + "?name=foo").get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("requestheader:null:", responseBody);

        response =
            client.resource(getBaseURI() + "/context/httpheaders/").queryParam("name", "foo").get();
        assertEquals(200, response.getStatusCode());
        responseBody = response.getEntity(String.class);
        assertEquals("requestheader:null:", responseBody);

        MultivaluedMap<String, String> map = new MultivaluedMapImpl<String, String>();
        map.put("name", new ArrayList<String>());
        response = client.resource(getBaseURI() + "/context/httpheaders/").queryParams(map).get();
        assertEquals(200, response.getStatusCode());
        responseBody = response.getEntity(String.class);
        assertEquals("requestheader:null:", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeader(String)} when requesting header
     * value for a single header name.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeaderSingleValue() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/" + "?name=foo").header("foo",
                                                                                         "bar")
                .get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("requestheader:[bar]", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeader(String)} when requesting
     * multiple header value for a single header name.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeaderMultipleValue() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/" + "?name=foo").header("foo",
                                                                                         "bar")
                .header("foo", "bar2").get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("requestheader:[bar, bar2]", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeader(String)} when requesting
     * multiple header value for a single header name when using
     * case-insensitive names.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeaderCaseInsensitive() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/" + "?name=foo").header("FOO",
                                                                                         "bar")
                .header("FoO", "bar2").get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("requestheader:[bar, bar2]", responseBody);
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeaders()} when making a basic
     * HttpClient request.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeadersBasicHeader() throws HttpException, IOException {

        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/requestheaders").get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);

        assertTrue(responseBody, responseBody.contains("requestheaders:"));
        assertTrue(responseBody, responseBody.contains(":host=") || responseBody.contains(":Host="));
        assertTrue(responseBody, responseBody.contains(":user-agent=") || responseBody
            .contains(":User-Agent="));
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeaders()} when having a custom
     * header.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeadersSingleValue() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/requestheaders").header("fOo",
                                                                                         "bAr")
                .get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);

        assertTrue(responseBody, responseBody.contains("requestheaders:"));
        assertTrue(responseBody, responseBody.contains(":fOo=[bAr]") || responseBody
            .contains(":foo=[bAr]"));
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeaders()} when having multiple values
     * and multiple custom headers.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeadersMultipleValues() throws HttpException, IOException {

        ClientResponse response =
            client.resource(getBaseURI() + "/context/httpheaders/requestheaders").header("fOo",
                                                                                         "bAr")
                .header("fOo", "2bAr").header("abc", "xyz").get();
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);

        assertTrue(responseBody, responseBody.contains("requestheaders:"));
        assertTrue(responseBody, responseBody.contains(":fOo=[2bAr, bAr]") || responseBody
            .contains(":foo=[2bAr, bAr]"));
        assertTrue(responseBody, responseBody.contains(":abc=[xyz]"));
    }
}
