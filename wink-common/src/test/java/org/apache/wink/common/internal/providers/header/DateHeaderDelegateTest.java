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
package org.apache.wink.common.internal.providers.header;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import static org.junit.Assert.*;

import org.junit.Test;

public class DateHeaderDelegateTest {

    /**
     * The date format pattern for RFC 1123.
     */
    private static final String RFC1123_DATE_FORMAT_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String RFC1123_DATE                = "Sun, 06 Nov 1994 08:49:37 GMT";

    /**
     * The date example for format pattern for RFC 1036 - <EEEE, dd-MMM-yy
     * HH:mm:ss zzz>
     */
    private static final String RFC1036_DATE                = "Sunday, 06-Nov-94 08:49:37 GMT";

    /**
     * The date example for format pattern for ANSI C asctime() - <EEE MMM d
     * HH:mm:ss yyyy>
     */
    private static final String ANSI_C_ASCTIME_DATE         = "Sun Nov 6 08:49:37 1994";

    @Test
    public void testParseHeaderDelegate() throws ParseException {

        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        HeaderDelegate<Date> dateHeaderDelegate = rd.createHeaderDelegate(Date.class);
        if (dateHeaderDelegate == null) {
            fail("Date header delegate is not regestered in RuntimeDelegateImpl");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(RFC1123_DATE_FORMAT_PATTERN, Locale.US);
        Date expectedDate = dateFormat.parse(RFC1123_DATE);

        Date parsedDate = null;

        // HTTP1.1 clients and servers that parse the date value MUST accept all
        // three formats (for compatibility with HTTP/1.0)
        // Date Foramts:RFC1123, ANSI C asctime() and RFC 1036

        // parse Date in format defined by RFC 1123
        try {
            parsedDate = dateHeaderDelegate.fromString(RFC1123_DATE);
            assertEquals(expectedDate, parsedDate);
        } catch (Exception e) {
            fail("Failed to parse date " + RFC1123_DATE);
        }

        // parse Date in format defined by RFC 1036
        try {
            dateHeaderDelegate.fromString(RFC1036_DATE);
            assertEquals(expectedDate, parsedDate);
        } catch (Exception e) {
            fail("Failed to parse date " + RFC1036_DATE);
        }

        // parse Date in format defined by ANSI C asctime
        try {
            dateHeaderDelegate.fromString(ANSI_C_ASCTIME_DATE);
            assertEquals(expectedDate, parsedDate);
        } catch (Exception e) {
            fail("Failed to parse date " + ANSI_C_ASCTIME_DATE);

        }

        // Negative Tests
        try {
            dateHeaderDelegate.fromString("Sundayy Nov 6 08:49:37 1994");
            fail("Invalid date - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }

        try {
            dateHeaderDelegate.fromString(null);
            fail("Date is null- IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testSerializeHeaderDelegate() throws ParseException {

        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        HeaderDelegate<Date> dateHeaderDelegate = rd.createHeaderDelegate(Date.class);
        if (dateHeaderDelegate == null) {
            fail("Date header delegate is not regestered in RuntimeDelegateImpl");
        }

        // HTTP1.1 clients and servers MUST only generate the RFC 1123 format
        // for representing HTTP-date values in header fields
        String expectedDate = RFC1123_DATE;

        try {
            SimpleDateFormat dateFormat =
                new SimpleDateFormat(RFC1123_DATE_FORMAT_PATTERN, Locale.US);
            Date expectedDate_RFC1123 = dateFormat.parse(RFC1123_DATE);
            assertEquals(expectedDate, dateHeaderDelegate.toString(expectedDate_RFC1123));
        } catch (Exception e) {
            fail("Failed to write date " + RFC1123_DATE);
        }

        try {
            dateHeaderDelegate.toString(null);
            fail("Date is null- IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }
    }
}
