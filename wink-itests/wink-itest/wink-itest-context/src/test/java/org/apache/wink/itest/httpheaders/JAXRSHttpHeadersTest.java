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

import javax.ws.rs.core.HttpHeaders;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests the {@link HttpHeaders} methods.
 */
public class JAXRSHttpHeadersTest extends TestCase {

    private static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/httpheaders";
    }

    private HttpClient client;

    public void setUp() {
        client = new HttpClient();
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableLanguages()} that if given no
     * acceptable languages, that it will return the server default locale back.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testAcceptableLanguagesNoneGiven() throws HttpException, IOException {
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/httpheaders/acceptablelanguages");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("acceptablelanguages:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableLanguages()} that if given a
     * language, it will be the only language in the list.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testAcceptableLanguagesOneGiven() throws HttpException, IOException {
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/httpheaders/acceptablelanguages");
        getMethod.setRequestHeader("Accept-Language", "de");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("acceptablelanguages:de:", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableLanguages()} that if given multiple
     * languages, all will be returned in the list.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testAcceptableLanguagesManyGiven() throws HttpException, IOException {
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/httpheaders/acceptablelanguages");
        getMethod.setRequestHeader("Accept-Language", "de, en, zh");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertTrue(responseBody, responseBody.startsWith("acceptablelanguages:"));
            assertTrue(responseBody, responseBody.contains(":de:"));
            assertTrue(responseBody, responseBody.contains(":en:"));
            assertTrue(responseBody, responseBody.contains(":zh:"));
        } finally {
            getMethod.releaseConnection();
        }
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
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/httpheaders/acceptablelanguages");
        getMethod.setRequestHeader("Accept-Language", "de;q=0.6, en;q=0.8, zh;q=0.7");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals("acceptablelanguages:en:zh:de:", responseBody);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableMediaTypes()} that if given no
     * Accept header, wildcard/wildcard is returned.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testAcceptableMediaTypesNoneGiven() throws HttpException, IOException {
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/httpheaders/acceptablemediatypes");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals("acceptablemediatypes:*/*:", responseBody);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableMediaTypes()} that if given a
     * single Accept header value, it is returned.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testAcceptableMediaTypesOneGiven() throws HttpException, IOException {
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/httpheaders/acceptablemediatypes");
        getMethod.setRequestHeader("Accept", "text/plain");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals("acceptablemediatypes:text/plain:", responseBody);
            assertEquals("text/plain" + ";charset=UTF-8", getMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getAcceptableMediaTypes()} that if given
     * multiple Accept header values, the values are sorted by q-value.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testAcceptableMediaTypesManyGiven() throws HttpException, IOException {
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/httpheaders/acceptablemediatypes");
        getMethod
            .addRequestHeader("Accept",
                              "text/plain;q=1.0,*/*;q=0.6, application/json;q=0.7,text/xml;q=0.8");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals("acceptablemediatypes:text/plain:text/xml:application/json:*/*:",
                         responseBody);
            assertEquals("text/plain;q=1.0" + ";charset=UTF-8", getMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getMediaType()} that if given a text/plain, the
     * method will return text/plain.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testMediaTypesRequestTextPlain() throws HttpException, IOException {
        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/context/httpheaders/requestmediatype");
        postMethod.setRequestEntity(new StringRequestEntity("Hello world!", "text/plain", "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            String responseBody = postMethod.getResponseBodyAsString();
            assertEquals("mediatype:text/plain:", responseBody);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getMediaType()} when a non-standard content type
     * is sent in.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testMediaTypesRequestCustomContentType() throws HttpException, IOException {
        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/context/httpheaders/requestmediatype");
        postMethod.setRequestEntity(new StringRequestEntity("Hello world!", "defg/abcd", "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            String responseBody = postMethod.getResponseBodyAsString();
            assertEquals("mediatype:defg/abcd:", responseBody);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getMediaType()} when no request entity is given.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testMediaTypesRequestNoRequestEntity() throws HttpException, IOException {
        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/context/httpheaders/requestmediatype");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            String responseBody = postMethod.getResponseBodyAsString();
            assertEquals("mediatype:null:", responseBody);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getLanguage()} when no language is given in the
     * request.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testLanguageNoneGiven() throws HttpException, IOException {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/context/httpheaders/language");
        postMethod.setRequestEntity(new StringRequestEntity("Hello world!", "text/plain", "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            String responseBody = postMethod.getResponseBodyAsString();
            assertEquals("language:null:", responseBody);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getLanguage()} when English language is given in
     * the request.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testLanguageEnglishGiven() throws HttpException, IOException {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/context/httpheaders/language");
        postMethod.setRequestEntity(new StringRequestEntity("Hello world!", "text/plain", "UTF-8"));
        postMethod.setRequestHeader("Content-Language", "en");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            String responseBody = postMethod.getResponseBodyAsString();
            assertEquals("language:en:", responseBody);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getLanguage()} when Chinese language is given in
     * the request.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testLanguageChineseGiven() throws HttpException, IOException {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/context/httpheaders/language");
        postMethod.setRequestEntity(new StringRequestEntity("Hello world!", "text/plain", "UTF-8"));
        postMethod.setRequestHeader("Content-Language", "zh");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            String responseBody = postMethod.getResponseBodyAsString();
            assertEquals("language:zh:", responseBody);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getCookies()} when no cookies are given.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testCookiesNone() throws HttpException, IOException {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/context/httpheaders/cookies");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            String responseBody = postMethod.getResponseBodyAsString();
            assertEquals("cookies:", responseBody);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getCookies()} when given a single cookie.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testCookiesOneGiven() throws HttpException, IOException {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/context/httpheaders/cookies");
        postMethod.addRequestHeader("Cookie", "foo=bar");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            String responseBody = postMethod.getResponseBodyAsString();
            assertEquals("cookies:foo=bar:", responseBody);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getCookies()} when given multiple cookies.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testCookiesManyGiven() throws HttpException, IOException {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/context/httpheaders/cookies");
        postMethod.addRequestHeader("Cookie", "foo=bar");
        postMethod.addRequestHeader("Cookie", "foo2=bar2");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            String responseBody = postMethod.getResponseBodyAsString();
            assertEquals("cookies:foo=bar:foo2=bar2:", responseBody);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeader(String)} when given a null
     * value.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeaderNoneGivenIllegalArgument() throws HttpException, IOException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/httpheaders/");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals("requestheader:null:", responseBody);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeader(String)} when requesting header
     * values for a non-existent header name.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeaderNonexistentHeader() throws HttpException, IOException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/httpheaders/?name=foo");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals("requestheader:null:", responseBody);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeader(String)} when requesting header
     * value for a single header name.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeaderSingleValue() throws HttpException, IOException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/httpheaders/?name=foo");
        getMethod.setRequestHeader("foo", "bar");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals("requestheader:[bar]", responseBody);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeader(String)} when requesting
     * multiple header value for a single header name.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeaderMultipleValue() throws HttpException, IOException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/httpheaders/?name=foo");
        getMethod.addRequestHeader("foo", "bar");
        getMethod.addRequestHeader("foo", "bar2");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals("requestheader:[bar, bar2]", responseBody);
        } finally {
            getMethod.releaseConnection();
        }
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
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/httpheaders/?name=foo");
        getMethod.addRequestHeader("FOO", "bar");
        getMethod.addRequestHeader("FoO", "bar2");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertEquals("requestheader:[bar, bar2]", responseBody);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeaders()} when making a basic
     * HttpClient request.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeadersBasicHeader() throws HttpException, IOException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/httpheaders/requestheaders");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertTrue(responseBody, responseBody.contains("requestheaders:"));
            assertTrue(responseBody, responseBody.contains(":host=") || responseBody
                .contains(":Host="));
            assertTrue(responseBody, responseBody.contains(":user-agent=") || responseBody
                .contains(":User-Agent="));
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeaders()} when having a custom
     * header.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeadersSingleValue() throws HttpException, IOException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/httpheaders/requestheaders");
        getMethod.addRequestHeader("fOo", "bAr");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertTrue(responseBody, responseBody.contains("requestheaders:"));
            assertTrue(responseBody, responseBody.contains(":fOo=[bAr]") || responseBody
                .contains(":foo=[bAr]"));
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests {@link HttpHeaders#getRequestHeaders()} when having multiple values
     * and multiple custom headers.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testRequestHeadersMultipleValues() throws HttpException, IOException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/httpheaders/requestheaders");
        getMethod.addRequestHeader("fOo", "bAr");
        getMethod.addRequestHeader("abc", "xyz");
        getMethod.addRequestHeader("fOo", "2bAr");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertTrue(responseBody, responseBody.contains("requestheaders:"));
            assertTrue(responseBody, responseBody.contains(":fOo=[2bAr, bAr]") || responseBody
                .contains(":foo=[2bAr, bAr]"));
            assertTrue(responseBody, responseBody.contains(":abc=[xyz]"));
        } finally {
            getMethod.releaseConnection();
        }
    }
}
