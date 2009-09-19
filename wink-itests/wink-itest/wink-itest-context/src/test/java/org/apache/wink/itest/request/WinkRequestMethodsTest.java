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

package org.apache.wink.itest.request;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpException;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkRequestMethodsTest extends TestCase {

    final private static SimpleDateFormat rfc1123Format                 =
                                                                            new SimpleDateFormat(
                                                                                                 "EEE, dd MMM yyyy HH:mm:ss zzz",
                                                                                                 Locale.ENGLISH);

    final private static SimpleDateFormat rfc850Format                  =
                                                                            new SimpleDateFormat(
                                                                                                 "EEEE, dd-MMM-yy HH:mm:ss zzz",
                                                                                                 Locale.ENGLISH);

    final private static SimpleDateFormat asctimeDateFormat             =
                                                                            new SimpleDateFormat(
                                                                                                 "EEE MMM dd HH:mm:ss yyyy",
                                                                                                 Locale.ENGLISH);

    final private static SimpleDateFormat asctimeDateFormatWithOneDigit =
                                                                            new SimpleDateFormat(
                                                                                                 "EEE MMM  d HH:mm:ss yyyy",
                                                                                                 Locale.ENGLISH);

    {
        /*
         * the implementation allows you to set a different time zone on the
         * requests for If-Modified-Since headers and it will do the
         * "right thing" (this is more leniant). However, asctime does not have
         * a time zone specified so it is assumed that all datetimes are in
         * GMT/UTC so the tests have to assume that the GMT time zone is used.
         */
        asctimeDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        asctimeDateFormatWithOneDigit.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/request";
    }

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Tests the {@link Request#evaluatePreconditions(Date)} that uses the
     * <code>If-Modified-Since</code> header and the RFC 1123 date format.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testEvaluateDateIfModifiedSinceUsingRFC1123Format() throws HttpException,
        IOException {
        checkIfModifiedSinceUsingSuppliedDateFormat(rfc1123Format);
    }

    /**
     * Tests the {@link Request#evaluatePreconditions(Date)} that uses the
     * <code>If-Modified-Since</code> header and the RFC 850 date format.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testEvaluateDateIfModifiedSinceUsingRFC850Format() throws HttpException,
        IOException {
        checkIfModifiedSinceUsingSuppliedDateFormat(rfc850Format);
    }

    /**
     * Tests the {@link Request#evaluatePreconditions(Date)} that uses the
     * <code>If-Modified-Since</code> header and the Asctime date format.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testEvaluateDateIfModifiedSinceUsingAscTimeFormat() throws HttpException,
        IOException {
        SimpleDateFormat formatter =
            (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) < 10)
                ? asctimeDateFormatWithOneDigit : asctimeDateFormat;
        checkIfModifiedSinceUsingSuppliedDateFormat(formatter);
    }

    private void checkIfModifiedSinceUsingSuppliedDateFormat(SimpleDateFormat formatter)
        throws IOException, HttpException {
        Date d2 = new Date(System.currentTimeMillis() - 120000);
        Date d = new Date(System.currentTimeMillis() - 60000);
        String date = DateFormat.getDateTimeInstance().format(d);
        /*
         * sets a last modified date
         */
        Resource dateResource = client.resource(getBaseURI() + "/context/request/date");
        ClientResponse response = dateResource.contentType("text/string").put(date);
        assertEquals(204, response.getStatusCode());

        response = dateResource.header(HttpHeaders.IF_MODIFIED_SINCE, formatter.format(d)).get();
        /*
         * verifies that if the exact date is sent in and used in
         * If-Modified-Since header, then the server will be ok and that it will
         * return 304
         */
        assertEquals(304, response.getStatusCode());

        /*
         * verifies that if no If-Modified-Since header is sent, then the server
         * will be ok and the Request instance won't build a response.
         */
        dateResource = client.resource(getBaseURI() + "/context/request/date");
        response = dateResource.get();
        assertEquals(200, response.getStatusCode());
        assertEquals("the date: " + rfc1123Format.format(d), response.getEntity(String.class));

        rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(rfc1123Format.format(d), response.getHeaders()
            .getFirst(HttpHeaders.LAST_MODIFIED));
        rfc1123Format.setTimeZone(TimeZone.getDefault());

        /*
         * verifies that using Last-Modified response header sent by server as
         * If-Modified-Since request header, then the server will return a 304
         */
        String lastModified = response.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED);
        dateResource = client.resource(getBaseURI() + "/context/request/date");
        response = dateResource.header(HttpHeaders.IF_MODIFIED_SINCE, lastModified).get();
        assertEquals(304, response.getStatusCode());

        /*
         * verifies that using a If-Modified-Since earlier than the
         * Last-Modified response header sent by server then the server will
         * return a 200 with entity
         */
        dateResource = client.resource(getBaseURI() + "/context/request/date");
        response = dateResource.header(HttpHeaders.IF_MODIFIED_SINCE, formatter.format(d2)).get();
        assertEquals(200, response.getStatusCode());
        assertEquals("the date: " + rfc1123Format.format(d), response.getEntity(String.class));

        rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(rfc1123Format.format(d), response.getHeaders()
            .getFirst(HttpHeaders.LAST_MODIFIED));
        rfc1123Format.setTimeZone(TimeZone.getDefault());

        /*
         * verifies that using a If-Modified-Since later than the Last-Modified
         * response header sent by server, then the server will return a 304
         */
        dateResource = client.resource(getBaseURI() + "/context/request/date");
        response =
            dateResource.header(HttpHeaders.IF_MODIFIED_SINCE, formatter.format(new Date())).get();
        assertEquals(304, response.getStatusCode());
    }

    /**
     * Tests the {@link Request#evaluatePreconditions(Date)} that uses the
     * <code>If-Unmodified-Since</code> header using RFC 1123.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testEvaluateDateIfUnmodifiedSinceUsingRFC1123() throws HttpException, IOException {
        checkIfUnmodifiedSinceUsingSuppliedDateFormat(rfc1123Format);
    }

    /**
     * Tests the {@link Request#evaluatePreconditions(Date)} that uses the
     * <code>If-Unmodified-Since</code> header using RFC 850.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testEvaluateDateIfUnmodifiedSinceUsingRFC850() throws HttpException, IOException {
        checkIfUnmodifiedSinceUsingSuppliedDateFormat(rfc850Format);
    }

    /**
     * Tests the {@link Request#evaluatePreconditions(Date)} that uses the
     * <code>If-Unmodified-Since</code> header using Asctime.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testEvaluateDateIfUnmodifiedSinceUsingAscTime() throws HttpException, IOException {
        SimpleDateFormat dateFormat =
            (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) < 10)
                ? asctimeDateFormatWithOneDigit : asctimeDateFormat;
        checkIfUnmodifiedSinceUsingSuppliedDateFormat(dateFormat);
    }

    private void checkIfUnmodifiedSinceUsingSuppliedDateFormat(SimpleDateFormat formatter)
        throws IOException, HttpException {
        Date d2 = new Date(System.currentTimeMillis() - 120000);
        Date d = new Date(System.currentTimeMillis() - 60000);
        String date = DateFormat.getDateTimeInstance().format(d);
        ClientResponse response =
            client.resource(getBaseURI() + "/context/request/date").contentType("text/string")
                .put(date);
        assertEquals(204, response.getStatusCode());

        /*
         * verifies that if the exact date is sent in and used in
         * If-Unmodified-Since header, then the server will be ok and that it
         * will return 200
         */
        response =
            client.resource(getBaseURI() + "/context/request/date")
                .header(HttpHeaders.IF_UNMODIFIED_SINCE, formatter.format(d)).get();
        assertEquals(200, response.getStatusCode());
        assertEquals("the date: " + rfc1123Format.format(d), response.getEntity(String.class));

        rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(rfc1123Format.format(d), response.getHeaders().getFirst("Last-Modified"));
        rfc1123Format.setTimeZone(TimeZone.getDefault());

        /*
         * verifies that if no If-Unmodified-Since header is sent, then the
         * server will be ok and the Request instance won't build a response.
         */
        response = client.resource(getBaseURI() + "/context/request/date").get();

        assertEquals(200, response.getStatusCode());
        assertEquals("the date: " + rfc1123Format.format(d), response.getEntity(String.class));

        rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(rfc1123Format.format(d), response.getHeaders()
            .getFirst(HttpHeaders.LAST_MODIFIED));
        rfc1123Format.setTimeZone(TimeZone.getDefault());

        /*
         * verifies that using Last-Modified response header sent by server as
         * If-Unmodified-Since request header, then the server will return the
         * entity
         */
        String lastModified = response.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED);
        response =
            client.resource(getBaseURI() + "/context/request/date")
                .header(HttpHeaders.IF_UNMODIFIED_SINCE, lastModified).get();

        assertEquals(200, response.getStatusCode());
        assertEquals("the date: " + rfc1123Format.format(d), response.getEntity(String.class));

        rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(rfc1123Format.format(d), response.getHeaders()
            .getFirst(HttpHeaders.LAST_MODIFIED));
        rfc1123Format.setTimeZone(TimeZone.getDefault());

        /*
         * verifies that using a If-Unmodified-Since earlier than the
         * Last-Modified response header sent by server then the server will
         * return a 412
         */
        response =
            client.resource(getBaseURI() + "/context/request/date")
                .header(HttpHeaders.IF_UNMODIFIED_SINCE, formatter.format(d2)).get();
        assertEquals(412, response.getStatusCode());

        response =
            client.resource(getBaseURI() + "/context/request/date")
                .header(HttpHeaders.IF_UNMODIFIED_SINCE, formatter.format(new Date())).get();
        /*
         * verifies that using a If-Unmodified-Since later than the
         * Last-Modified response header sent by server, then the server will
         * return 200 and the entity
         */
        assertEquals(200, response.getStatusCode());
        assertEquals("the date: " + rfc1123Format.format(d), response.getEntity(String.class));

        rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(rfc1123Format.format(d), response.getHeaders()
            .getFirst(HttpHeaders.LAST_MODIFIED));
        rfc1123Format.setTimeZone(TimeZone.getDefault());
    }

    /**
     * Tests the
     * {@link Request#evaluatePreconditions(javax.ws.rs.core.EntityTag)} that
     * uses the <code>If-Match</code> header and a strong ETag.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testEvaluateEtagIfMatchStrong() throws HttpException, IOException {
        try {
            checkETagIfMatch("\"myentitytagABCXYZ\"", false);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests the
     * {@link Request#evaluatePreconditions(javax.ws.rs.core.EntityTag)} that
     * uses the <code>If-Match</code> header and a weak ETag.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testEvaluateEtagIfMatchWeak() throws HttpException, IOException {
        try {
            checkETagIfMatch("\"myentitytagABCXYZ\"", true);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests the
     * {@link Request#evaluatePreconditions(javax.ws.rs.core.EntityTag)} that
     * uses the <code>If-Match</code> header.
     * 
     * @throws HttpException
     * @throws IOException
     */
    private void checkETagIfMatch(String etag, boolean isEntityTagWeak) throws HttpException,
        IOException {
        final String justTheTag = etag;
        final String setETag = isEntityTagWeak ? "W/" + justTheTag : justTheTag;
        String isWeak = isEntityTagWeak ? "true" : "false";

        ClientResponse response =
            client.resource(getBaseURI() + "/context/request/etag").contentType("text/string")
                .put(setETag);
        assertEquals(204, response.getStatusCode());

        response =
            client.resource(getBaseURI() + "/context/request/etag").header(HttpHeaders.IF_MATCH,
                                                                           setETag).get();
        assertEquals(200, response.getStatusCode());
        assertEquals("the etag: " + justTheTag + isWeak, response.getEntity(String.class));

        /*
         * verifies that a request without an If-Match header will still proceed
         */
        response = client.resource(getBaseURI() + "/context/request/etag").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("the etag: " + justTheTag + isWeak, response.getEntity(String.class));

        /*
         * verifies that a misquoted entity tag is not a valid entity tag
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_MATCH, setETag.substring(1, setETag.length() - 1)).get();
        assertEquals(400, response.getStatusCode());

        /*
         * verifies that a misquoted entity tag is not a valid entity tag
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_MATCH, setETag.substring(0, setETag.length() - 1)).get();
        assertEquals(400, response.getStatusCode());

        /*
         * verifies that a misquoted entity tag is not a valid entity tag
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_MATCH, setETag.substring(1, setETag.length())).get();
        assertEquals(400, response.getStatusCode());

        /*
         * verifies that if an etag is sent that does not match the server etag,
         * that a 412 is returned
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag").header(HttpHeaders.IF_MATCH,
                                                                           "\"someothervalue\"")
                .get();
        assertEquals(412, response.getStatusCode());

        /*
         * verifies that if multiple etags are sent that do not match the server
         * etag, that a 412 is returned
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_MATCH, "\"austin\", \"powers\"").get();
        assertEquals(412, response.getStatusCode());

        /*
         * verifies that if multiple etags are sent that do not match the server
         * etag, that a 412 is returned
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag").header(HttpHeaders.IF_MATCH,
                                                                           "\"austin\", " + setETag
                                                                               + " , \"powers\"")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("the etag: " + justTheTag + isWeak, response.getEntity(String.class));
    }

    /**
     * Tests the
     * {@link Request#evaluatePreconditions(javax.ws.rs.core.EntityTag)} that
     * uses the <code>If-None-Match</code> header with strong entity tag.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testEvaluateEtagIfNoneMatchStrong() throws HttpException, IOException {
        checkETagIfNoneMatch("\"myentitytagABCXYZ\"", false);
    }

    /**
     * Tests the
     * {@link Request#evaluatePreconditions(javax.ws.rs.core.EntityTag)} that
     * uses the <code>If-None-Match</code> header with weak entity tag.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testEvaluateEtagIfNoneMatchWeak() throws HttpException, IOException {
        checkETagIfNoneMatch("\"myentitytagABCXYZ\"", true);
    }

    /**
     * Tests the
     * {@link Request#evaluatePreconditions(javax.ws.rs.core.EntityTag)} that
     * uses the <code>If-None-Match</code> header.
     * 
     * @throws HttpException
     * @throws IOException
     */
    private void checkETagIfNoneMatch(String etag, boolean isEntityTagWeak) throws HttpException,
        IOException {
        final String justTheTag = etag;
        final String setETag = isEntityTagWeak ? "W/" + justTheTag : justTheTag;
        String isWeak = isEntityTagWeak ? "true" : "false";

        /*
         * sets an entity tag
         */
        ClientResponse response =
            client.resource(getBaseURI() + "/context/request/etag").contentType("text/string")
                .put(setETag);
        assertEquals(204, response.getStatusCode());

        /*
         * verifies that if the exact etag is sent in, then the response is a
         * 304
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_NONE_MATCH, setETag).get();
        assertEquals(304, response.getStatusCode());
        assertEquals(setETag, response.getHeaders().getFirst(HttpHeaders.ETAG));

        /*
         * verifies that if a "*" etag is sent in, then the response returns a
         * 304
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_NONE_MATCH, "\"*\"").get();
        assertEquals(304, response.getStatusCode());
        assertEquals(setETag, response.getHeaders().getFirst(HttpHeaders.ETAG));

        /*
         * verifies that if a "*" etag is sent in, then the response returns a
         * 412
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_NONE_MATCH, "\"*\"").post(null);
        assertEquals(412, response.getStatusCode());
        assertEquals(setETag, response.getHeaders().getFirst(HttpHeaders.ETAG));

        /*
         * verifies that if the set etag is sent in, then the response returns a
         * 412
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_NONE_MATCH, setETag).post(null);
        assertEquals(412, response.getStatusCode());
        assertEquals(setETag, response.getHeaders().getFirst(HttpHeaders.ETAG));

        /*
         * verifies that a request without an If-None-Match header will still
         * proceed
         */
        response = client.resource(getBaseURI() + "/context/request/etag").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("the etag: " + justTheTag + isWeak, response.getEntity(String.class));

        /*
         * verifies that a request without an If-None-Match header will still
         * proceed
         */
        response = client.resource(getBaseURI() + "/context/request/etag").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("the etag: " + justTheTag + isWeak, response.getEntity(String.class));

        /*
         * verifies that an unquoted entity tag is invalid
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_NONE_MATCH, setETag.substring(1, setETag.length() - 1))
                .get();
        assertEquals(400, response.getStatusCode());

        /*
         * verifies that a misquoted entity tag is invalid
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_NONE_MATCH, setETag.substring(0, setETag.length() - 1))
                .get();
        assertEquals(400, response.getStatusCode());

        /*
         * verifies that a misquoted entity tag is invalid
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_NONE_MATCH, setETag.substring(1, setETag.length())).get();
        assertEquals(400, response.getStatusCode());

        /*
         * verifies that if an etag is sent that does not match the server etag,
         * that request is allowed to proceed
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_NONE_MATCH, "\"someothervalue\"").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("the etag: " + justTheTag + isWeak, response.getEntity(String.class));

        /*
         * verifies that if multiple etags are sent that do not match the server
         * etag, that the request is allowed to proceed
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_NONE_MATCH, "\"austin\", \"powers\"").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("the etag: " + justTheTag + isWeak, response.getEntity(String.class));

        /*
         * verifies that if multiple etags are sent that do not match the server
         * etag, then a 200 and the request entity is returned
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_NONE_MATCH, "\"austin\", \"powers\"").post(null);
        assertEquals(200, response.getStatusCode());
        assertEquals("the etag: " + justTheTag + isWeak, response.getEntity(String.class));

        /*
         * verifies that if multiple etags are sent that do match the server
         * etag, that a 304 is returned
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_NONE_MATCH, "\"austin\", " + setETag + " , \"powers\"")
                .get();
        assertEquals(304, response.getStatusCode());
        assertEquals(setETag, response.getHeaders().getFirst(HttpHeaders.ETAG));

        /*
         * verifies that a request with an If-None-Match header will fail
         */
        response =
            client.resource(getBaseURI() + "/context/request/etag")
                .header(HttpHeaders.IF_NONE_MATCH, "\"austin\", " + setETag + " , \"powers\"")
                .post(null);
        assertEquals(412, response.getStatusCode());
        assertEquals(setETag, response.getHeaders().getFirst(HttpHeaders.ETAG));
    }

    // TODO: add selectVariant tests by querying the various
    // /context/request/variant/* paths

}
