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

import javax.ws.rs.core.Request;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests the {@link Request} implementation.
 */
public class RequestMethodsTest extends TestCase {

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
        if(ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/request";
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
        HttpClient client = new HttpClient();

        PutMethod putMethod = new PutMethod(getBaseURI() + "/context/request/date");
        Date d2 = new Date(System.currentTimeMillis() - 120000);
        Date d = new Date(System.currentTimeMillis() - 60000);
        String date = DateFormat.getDateTimeInstance().format(d);
        putMethod.setRequestEntity(new StringRequestEntity(date, "text/string", "UTF-8"));
        try {
            /*
             * sets a last modified date
             */
            client.executeMethod(putMethod);
            assertEquals(204, putMethod.getStatusCode());
        } finally {
            putMethod.releaseConnection();
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/request/date");
        getMethod.setRequestHeader("If-Modified-Since", formatter.format(d));
        try {
            /*
             * verifies that if the exact date is sent in and used in
             * If-Modified-Since header, then the server will be ok and that it
             * will return 304
             */
            client.executeMethod(getMethod);
            assertEquals(304, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/date");
        try {
            /*
             * verifies that if no If-Modified-Since header is sent, then the
             * server will be ok and the Request instance won't build a
             * response.
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the date: " + rfc1123Format.format(d), getMethod
                .getResponseBodyAsString());

            rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
            assertEquals(rfc1123Format.format(d), getMethod.getResponseHeader("Last-Modified")
                .getValue());
            rfc1123Format.setTimeZone(TimeZone.getDefault());
        } finally {
            getMethod.releaseConnection();
        }

        String lastModified = getMethod.getResponseHeader("Last-Modified").getValue();
        getMethod = new GetMethod(getBaseURI() + "/context/request/date");
        getMethod.setRequestHeader("If-Modified-Since", lastModified);
        try {
            /*
             * verifies that using Last-Modified response header sent by server
             * as If-Modified-Since request header, then the server will return
             * a 304
             */
            client.executeMethod(getMethod);
            assertEquals(304, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/date");
        getMethod.setRequestHeader("If-Modified-Since", formatter.format(d2));
        try {
            /*
             * verifies that using a If-Modified-Since earlier than the
             * Last-Modified response header sent by server then the server will
             * return a 200 with entity
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the date: " + rfc1123Format.format(d), getMethod
                .getResponseBodyAsString());

            rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
            assertEquals(rfc1123Format.format(d), getMethod.getResponseHeader("Last-Modified")
                .getValue());
            rfc1123Format.setTimeZone(TimeZone.getDefault());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/date");
        getMethod.setRequestHeader("If-Modified-Since", formatter.format(new Date()));
        try {
            /*
             * verifies that using a If-Modified-Since later than the
             * Last-Modified response header sent by server, then the server
             * will return a 304
             */
            client.executeMethod(getMethod);
            assertEquals(304, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }
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
        HttpClient client = new HttpClient();

        PutMethod putMethod = new PutMethod(getBaseURI() + "/context/request/date");
        Date d2 = new Date(System.currentTimeMillis() - 120000);
        Date d = new Date(System.currentTimeMillis() - 60000);
        String date = DateFormat.getDateTimeInstance().format(d);
        putMethod.setRequestEntity(new StringRequestEntity(date, "text/string", "UTF-8"));
        try {
            /*
             * sets a last modified date
             */
            client.executeMethod(putMethod);
            assertEquals(204, putMethod.getStatusCode());
        } finally {
            putMethod.releaseConnection();
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/request/date");
        getMethod.setRequestHeader("If-Unmodified-Since", formatter.format(d));
        try {
            /*
             * verifies that if the exact date is sent in and used in
             * If-Unmodified-Since header, then the server will be ok and that
             * it will return 200
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the date: " + rfc1123Format.format(d), getMethod
                .getResponseBodyAsString());

            rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
            assertEquals(rfc1123Format.format(d), getMethod.getResponseHeader("Last-Modified")
                .getValue());
            rfc1123Format.setTimeZone(TimeZone.getDefault());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/date");
        try {
            /*
             * verifies that if no If-Unmodified-Since header is sent, then the
             * server will be ok and the Request instance won't build a
             * response.
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the date: " + rfc1123Format.format(d), getMethod
                .getResponseBodyAsString());

            rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
            assertEquals(rfc1123Format.format(d), getMethod.getResponseHeader("Last-Modified")
                .getValue());
            rfc1123Format.setTimeZone(TimeZone.getDefault());
        } finally {
            getMethod.releaseConnection();
        }

        String lastModified = getMethod.getResponseHeader("Last-Modified").getValue();

        getMethod = new GetMethod(getBaseURI() + "/context/request/date");
        getMethod.setRequestHeader("If-Unmodified-Since", lastModified);
        try {
            /*
             * verifies that using Last-Modified response header sent by server
             * as If-Unmodified-Since request header, then the server will
             * return the entity
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the date: " + rfc1123Format.format(d), getMethod
                .getResponseBodyAsString());

            rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
            assertEquals(rfc1123Format.format(d), getMethod.getResponseHeader("Last-Modified")
                .getValue());
            rfc1123Format.setTimeZone(TimeZone.getDefault());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/date");
        getMethod.setRequestHeader("If-Unmodified-Since", formatter.format(d2));
        try {
            /*
             * verifies that using a If-Unmodified-Since earlier than the
             * Last-Modified response header sent by server then the server will
             * return a 412
             */
            client.executeMethod(getMethod);
            assertEquals(412, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/date");
        getMethod.setRequestHeader("If-Unmodified-Since", formatter.format(new Date()));
        try {
            /*
             * verifies that using a If-Unmodified-Since later than the
             * Last-Modified response header sent by server, then the server
             * will return 200 and the entity
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the date: " + rfc1123Format.format(d), getMethod
                .getResponseBodyAsString());

            rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
            assertEquals(rfc1123Format.format(d), getMethod.getResponseHeader("Last-Modified")
                .getValue());
            rfc1123Format.setTimeZone(TimeZone.getDefault());
        } finally {
            getMethod.releaseConnection();
        }
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
        HttpClient client = new HttpClient();

        final String justTheTag = etag;
        final String setETag = isEntityTagWeak ? "W/" + justTheTag : justTheTag;
        String isWeak = isEntityTagWeak ? "true" : "false";

        PutMethod putMethod = new PutMethod(getBaseURI() + "/context/request/etag");
        putMethod.setRequestEntity(new StringRequestEntity(setETag, "text/string", "UTF-8"));
        try {
            /*
             * sets an entity tag
             */
            client.executeMethod(putMethod);
            assertEquals(204, putMethod.getStatusCode());
        } finally {
            putMethod.releaseConnection();
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-Match", setETag);
        try {
            /*
             * verifies that if the exact etag is sent in, then the request is
             * allowed to proceed
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the etag: " + justTheTag + isWeak, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        try {
            /*
             * verifies that a request without an If-Match header will still
             * proceed
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the etag: " + justTheTag + isWeak, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-Match", setETag.substring(1, setETag.length() - 1));
        try {
            /*
             * verifies that an unquoted entity tag is not a valid entity tag
             */
            client.executeMethod(getMethod);
            assertEquals(400, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-Match", setETag.substring(0, setETag.length() - 1));
        try {
            /*
             * verifies that a misquoted entity tag is not a valid entity tag
             */
            client.executeMethod(getMethod);
            assertEquals(400, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-Match", setETag.substring(1, setETag.length()));
        try {
            /*
             * verifies that a misquoted entity tag is not a valid entity tag
             */
            client.executeMethod(getMethod);
            assertEquals(400, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-Match", "\"someothervalue\"");
        try {
            /*
             * verifies that if an etag is sent that does not match the server
             * etag, that a 412 is returned
             */
            client.executeMethod(getMethod);
            assertEquals(412, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-Match", "\"austin\", \"powers\"");
        try {
            /*
             * verifies that if multiple etags are sent that do not match the
             * server etag, that a 412 is returned
             */
            client.executeMethod(getMethod);
            assertEquals(412, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.addRequestHeader("If-Match", "\"austin\", \"powers\"");
        try {
            /*
             * verifies that if multiple etags are sent that do not match the
             * server etag, that a 412 is returned
             */
            client.executeMethod(getMethod);
            assertEquals(412, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.addRequestHeader("If-Match", "\"austin\", " + setETag + " , \"powers\"");
        try {
            /*
             * verifies that if multiple etags are sent that do match the server
             * etag, that a 200 and entity body is returned
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the etag: " + justTheTag + isWeak, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
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
        HttpClient client = new HttpClient();
        final String justTheTag = etag;
        final String setETag = isEntityTagWeak ? "W/" + justTheTag : justTheTag;
        String isWeak = isEntityTagWeak ? "true" : "false";

        PutMethod putMethod = new PutMethod(getBaseURI() + "/context/request/etag");
        putMethod.setRequestEntity(new StringRequestEntity(setETag, "text/string", "UTF-8"));
        try {
            /*
             * sets an entity tag
             */
            client.executeMethod(putMethod);
            assertEquals(204, putMethod.getStatusCode());
        } finally {
            putMethod.releaseConnection();
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-None-Match", setETag);
        try {
            /*
             * verifies that if the exact etag is sent in, then the response is
             * a 304
             */
            client.executeMethod(getMethod);
            assertEquals(304, getMethod.getStatusCode());
            assertEquals(setETag, getMethod.getResponseHeader("ETag").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-None-Match", "\"*\"");
        try {
            /*
             * verifies that if a "*" etag is sent in, then the response returns
             * a 304
             */
            client.executeMethod(getMethod);
            assertEquals(304, getMethod.getStatusCode());
            assertEquals(setETag, getMethod.getResponseHeader("ETag").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        PostMethod postMethod = new PostMethod(getBaseURI() + "/context/request/etag");
        postMethod.setRequestHeader("If-None-Match", setETag);
        try {
            /*
             * verifies that if a matching etag is sent in, then the response
             * returns a 412
             */
            client.executeMethod(postMethod);
            assertEquals(412, postMethod.getStatusCode());
            assertEquals(setETag, postMethod.getResponseHeader("ETag").getValue());
        } finally {
            postMethod.releaseConnection();
        }

        postMethod = new PostMethod(getBaseURI() + "/context/request/etag");
        postMethod.setRequestHeader("If-None-Match", "\"*\"");
        try {
            /*
             * verifies that if a "*" etag is sent in, then the response returns
             * a 412
             */
            client.executeMethod(postMethod);
            assertEquals(412, postMethod.getStatusCode());
            assertEquals(setETag, postMethod.getResponseHeader("ETag").getValue());
        } finally {
            postMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        try {
            /*
             * verifies that a request without an If-None-Match header will
             * still proceed
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the etag: " + justTheTag + isWeak, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        postMethod = new PostMethod(getBaseURI() + "/context/request/etag");
        try {
            /*
             * verifies that a request without an If-None-Match header will
             * still proceed
             */
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("the etag: " + justTheTag + isWeak, postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-None-Match", setETag.substring(1, setETag.length() - 1));
        try {
            /*
             * verifies that an unquoted entity tag is invalid
             */
            client.executeMethod(getMethod);
            assertEquals(400, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-None-Match", setETag.substring(0, setETag.length() - 1));
        try {
            /*
             * verifies that a misquoted entity tag is invalid
             */
            client.executeMethod(getMethod);
            assertEquals(400, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-None-Match", setETag.substring(1, setETag.length()));
        try {
            /*
             * verifies that a misquoted entity tag is invalid
             */
            client.executeMethod(getMethod);
            assertEquals(400, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-None-Match", "\"someothervalue\"");
        try {
            /*
             * verifies that if an etag is sent that does not match the server
             * etag, that request is allowed to proceed
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the etag: " + justTheTag + isWeak, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.setRequestHeader("If-None-Match", "\"austin\", \"powers\"");
        try {
            /*
             * verifies that if multiple etags are sent that do not match the
             * server etag, that the request is allowed to proceed
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the etag: " + justTheTag + isWeak, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.addRequestHeader("If-None-Match", "\"austin\", \"powers\"");
        try {
            /*
             * verifies that if multiple etags are sent that do not match the
             * server etag, then a 200 and the request entity is returned
             */
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("the etag: " + justTheTag + isWeak, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        postMethod = new PostMethod(getBaseURI() + "/context/request/etag");
        postMethod.addRequestHeader("If-None-Match", "\"austin\", \"powers\"");
        try {
            /*
             * verifies that a request without an If-None-Match header will
             * still proceed
             */
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("the etag: " + justTheTag + isWeak, postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/context/request/etag");
        getMethod.addRequestHeader("If-None-Match", "\"austin\", " + setETag + " , \"powers\"");
        try {
            /*
             * verifies that if multiple etags are sent that do match the server
             * etag, that a 304 is returned
             */
            client.executeMethod(getMethod);
            assertEquals(304, getMethod.getStatusCode());
            assertEquals(setETag, getMethod.getResponseHeader("ETag").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        postMethod = new PostMethod(getBaseURI() + "/context/request/etag");
        postMethod.addRequestHeader("If-None-Match", "\"austin\", " + setETag + " , \"powers\"");
        try {
            /*
             * verifies that a request with an If-None-Match header will fail
             */
            client.executeMethod(postMethod);
            assertEquals(412, postMethod.getStatusCode());
            assertEquals(setETag, getMethod.getResponseHeader("ETag").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    // TODO: add selectVariant tests by querying the various
    // /context/request/variant/* paths

}
