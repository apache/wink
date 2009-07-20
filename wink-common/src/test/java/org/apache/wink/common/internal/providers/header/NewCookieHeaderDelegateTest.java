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

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.junit.Test;

public class NewCookieHeaderDelegateTest {

    @Test
    public void testParseNewCookie() {
        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        HeaderDelegate<NewCookie> newCookieHeaderDelegate =
            rd.createHeaderDelegate(NewCookie.class);
        if (newCookieHeaderDelegate == null) {
            fail("NewCookie header delegate is not regestered in RuntimeDelegateImpl");
        }

        NewCookie expectedNewCookie =
            new NewCookie("MyCookie", "MyCookieValue", ".", "mydomain", 1, "Comment", 21600, true);
        String cookieToParse =
            "MyCookie=MyCookieValue;Version=1; Path=.; Domain=mydomain; Comment=Comment; Max-Age=21600; Secure";

        // Test Parse NewCookie
        NewCookie parsedNewCookie = newCookieHeaderDelegate.fromString(cookieToParse);
        assertEquals(expectedNewCookie, parsedNewCookie);

        expectedNewCookie =
            new NewCookie("MyCookie", "", ".", "mydomain", 1, "Comment", 21600, true);
        cookieToParse =
            "MyCookie=\"\";Version=1; Path=.; Domain=mydomain; Comment=Comment; Max-Age=21600; Secure";
        parsedNewCookie = newCookieHeaderDelegate.fromString(cookieToParse);
        assertEquals(expectedNewCookie, parsedNewCookie);

        // Negative test - NewCookie in null
        try {
            newCookieHeaderDelegate.fromString(null);
            fail("NewCookie is null - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }

        // Negative test - Invalid cookie
        try {
            newCookieHeaderDelegate.fromString("Invalid Cookie");
            fail("Invalid  NewCookie - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }

        // Negative test - Invalid cookie
        try {
            newCookieHeaderDelegate
                .fromString("MyCookieName;Version=1; Path=.; Domain=mydomain; Comment=Comment; Max-Age=21600; Secure");
            fail("Invalid NewCookie - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testSerializeNewCookie() {
        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        HeaderDelegate<NewCookie> newCookieHeaderDelegate =
            rd.createHeaderDelegate(NewCookie.class);
        if (newCookieHeaderDelegate == null) {
            fail("NewCookie header delegate is not regestered in RuntimeDelegateImpl");
        }

        String expectedCookieSerialization =
            "MyCookie=MyCookieValue;Version=1;Path=.;Domain=mydomain;Comment=Comment;Max-Age=21600;Secure";
        NewCookie cookieToSerialize =
            new NewCookie("MyCookie", "MyCookieValue", ".", "mydomain", 1, "Comment", 21600, true);
        String serializedCookie = cookieToSerialize.toString();
        assertEquals(expectedCookieSerialization, serializedCookie);

        // Negative test - Invalid cookie
        try {
            newCookieHeaderDelegate.toString(null);
            fail("NewCookie is null - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }

    }

}
