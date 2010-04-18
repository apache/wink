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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 */
package org.apache.wink.common.internal.providers.header;

import static org.junit.Assert.fail;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.http.AcceptCharset;
import org.junit.Before;
import org.junit.Test;

public class AcceptCharsetHeaderDelegateTest {
    private HeaderDelegate<AcceptCharset> acceptCharsetDelegate;

    @Before
    public void setup() {
        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        acceptCharsetDelegate = rd.createHeaderDelegate(AcceptCharset.class);
    }

    @Test
    public void testHeaderDelegateExists() {
        if (acceptCharsetDelegate == null) {
            fail("AcceptCharsetDelegate header delegate is not registered in RuntimeDelegateImpl");
        }
    }

    @Test
    public void testFromStringNull() {
        AcceptCharset charset = acceptCharsetDelegate.fromString(null);
        assertTrue(charset.isAnyCharsetAllowed());
        assertEquals(Collections.EMPTY_LIST, charset.getAcceptableCharsets());
        assertEquals(Collections.EMPTY_LIST, charset.getBannedCharsets());
    }

    @Test
    public void testFromStringEmpty() {
        AcceptCharset charset = acceptCharsetDelegate.fromString("");
        assertFalse(charset.isAnyCharsetAllowed());
        assertEquals(Collections.singletonList("ISO-8859-1"), charset.getAcceptableCharsets());
        assertEquals(Collections.EMPTY_LIST, charset.getBannedCharsets());
    }

    @Test
    public void testFromStringUTF8Only() {
        AcceptCharset charset = acceptCharsetDelegate.fromString("utf-8");
        assertFalse(charset.isAnyCharsetAllowed());
        // see HTTP Accept-Char set
        assertEquals(Arrays.asList(new String[] {"ISO-8859-1", "utf-8"}), charset
            .getAcceptableCharsets());
        assertEquals(Collections.EMPTY_LIST, charset.getBannedCharsets());
    }

    @Test
    public void testFromStringUTF8AndWildcard() {
        AcceptCharset charset = acceptCharsetDelegate.fromString("utf-8,*");
        assertTrue(charset.isAnyCharsetAllowed());
        assertEquals(Arrays.asList(new String[] {"utf-8"}), charset.getAcceptableCharsets());
        // note that any charset allowed means ISO-8859-1 is part of wildcard,
        // see HTTP spec
        assertEquals(Collections.EMPTY_LIST, charset.getBannedCharsets());
    }

    @Test
    public void testFromStringUTF8WithQuality() {
        AcceptCharset charset = acceptCharsetDelegate.fromString("utf-8;q=0.8");
        assertFalse(charset.isAnyCharsetAllowed());
        assertEquals(Arrays.asList(new String[] {"ISO-8859-1", "utf-8"}), charset
            .getAcceptableCharsets());
        assertEquals(Arrays.asList(new AcceptCharset.ValuedCharset[] {
            new AcceptCharset.ValuedCharset(1.0, "ISO-8859-1"),
            new AcceptCharset.ValuedCharset(0.8, "utf-8")}), charset.getValuedCharsets());
        assertEquals(Collections.EMPTY_LIST, charset.getBannedCharsets());
    }

    @Test
    public void testFromStringUTF8WithQualityAndWildcardWithQuality() {
        AcceptCharset charset = acceptCharsetDelegate.fromString("utf-8;q=0.8, *;q=0.4");
        assertTrue(charset.isAnyCharsetAllowed());
        assertEquals(Arrays.asList(new String[] {"utf-8"}), charset.getAcceptableCharsets());
        assertEquals(Arrays.asList(new AcceptCharset.ValuedCharset[] {
            new AcceptCharset.ValuedCharset(0.8, "utf-8"),
            new AcceptCharset.ValuedCharset(0.4, null)}), charset.getValuedCharsets());
        // note that any charset allowed means ISO-8859-1 is part of wildcard,
        // see HTTP spec
        assertEquals(Collections.EMPTY_LIST, charset.getBannedCharsets());
    }

}
