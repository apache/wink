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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.junit.Test;

public class CookieHeaderDelegateTest {

    @Test
    public void testParseNewCookie() {
        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        HeaderDelegate<Cookie> cookieHeaderDelegate = rd.createHeaderDelegate(Cookie.class);
        if (cookieHeaderDelegate == null) {
            fail("Cookie header delegate is not regestered in RuntimeDelegateImpl");
        }

        Cookie expectedNewCookie = new Cookie("MyCookie", "MyCookieValue", ".", "mydomain", 1);
        String cookieToParse =
            "$Version=1; MyCookie=MyCookieValue; $Path=.; $Domain=mydomain, SecondCookie=Value";

        // Test Parse Cookie
        Cookie parsedNewCookie = cookieHeaderDelegate.fromString(cookieToParse);
        assertEquals(expectedNewCookie, parsedNewCookie);

        expectedNewCookie = new Cookie("MyCookie", "", ".", "mydomain", 1);
        cookieToParse = "$Version=1; MyCookie=\"\"; $Path=.; $Domain=mydomain;";
        parsedNewCookie = cookieHeaderDelegate.fromString(cookieToParse);
        assertEquals(expectedNewCookie, parsedNewCookie);

        // Negative test - Invalid cookie
        try {
            cookieHeaderDelegate.fromString("Invalid Cookie");
            fail("Cookie is null - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }

        // Negative test - Invalid cookie
        try {
            cookieHeaderDelegate.fromString("MyCookieName;Version=1");
            fail("Cookie is invalid - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }

        // Negative test - Cookie in null
        try {
            cookieHeaderDelegate.fromString(null);
            fail("Cookie is null - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }

    }

    @Test
    public void testSerializeNewCookie() {
        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        HeaderDelegate<Cookie> cookieHeaderDelegate = rd.createHeaderDelegate(Cookie.class);
        if (cookieHeaderDelegate == null) {
            fail("Cookie header delegate is not regestered in RuntimeDelegateImpl");
        }

        String expectedCookieSerialization =
            "$Version=1;MyCookie=MyCookieValue;$Domain=mydomain;$Path=.";
        Cookie cookieToSerialize = new Cookie("MyCookie", "MyCookieValue", ".", "mydomain", 1);
        String serializedCookie = cookieToSerialize.toString();

        assertEquals(expectedCookieSerialization, serializedCookie);

        // Negative test - Cookie in null
        try {
            cookieHeaderDelegate.toString(null);
            fail("Cookie is null - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }

    }

}
