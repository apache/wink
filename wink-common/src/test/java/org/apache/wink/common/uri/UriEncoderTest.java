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

package org.apache.wink.common.uri;

import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.common.internal.utils.UriHelper;

import junit.framework.TestCase;

/**
 * Unit test of UriEncoder
 */
public class UriEncoderTest extends TestCase {

    public static final String TEST_STRING = "abcxyzABCXYZ0189-._~!$&'()*+,;=:/?#[]@{} %50";

    public void testEncodeString() {
        assertNull(UriEncoder.encodeString(null));
        assertEquals("", UriEncoder.encodeString(""));
        assertEquals("%2B%26", UriEncoder.encodeString("+&"));
        assertEquals("hell0world", UriEncoder.encodeString("hell0world"));
        assertEquals("%D7%90", UriEncoder.encodeString("\u05D0"));
        assertEquals("a%20b", UriEncoder.encodeString("a b"));
        assertEquals("abcxyzABCXYZ0189-._~%21%24%26%27%28%29%2A%2B%2C%3B%3D%3A%2F%3F%23%5B%5D%40%7B%7D%20%2550",
                     UriEncoder.encodeString(TEST_STRING));
    }

    public void testEncodeUserInfo() {
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:%2F%3F%23%5B%5D%40%7B%7D%20%50", UriEncoder
            .encodeUserInfo(TEST_STRING, true));
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:%2F%3F%23%5B%5D%40%7B%7D%20%2550", UriEncoder
            .encodeUserInfo(TEST_STRING, false));
    }

    public void testEncodeSegment() {
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:%2F%3F%23%5B%5D@%7B%7D%20%50", UriEncoder
            .encodePathSegment(TEST_STRING, true));
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:%2F%3F%23%5B%5D@%7B%7D%20%2550", UriEncoder
            .encodePathSegment(TEST_STRING, false));
    }

    public void testEncodeMatrix() {
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,%3B%3D:%2F%3F%23%5B%5D@%7B%7D%20%50", UriEncoder
            .encodeMatrix(TEST_STRING, true));
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,%3B%3D:%2F%3F%23%5B%5D@%7B%7D%20%2550",
                     UriEncoder.encodeMatrix(TEST_STRING, false));
    }

    public void testEncodePath() {
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:/%3F%23%5B%5D@%7B%7D%20%50", UriEncoder
            .encodePath(TEST_STRING, true));
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:/%3F%23%5B%5D@%7B%7D%20%2550", UriEncoder
            .encodePath(TEST_STRING, false));
    }

    public void testEncodeQueryParam() {
        assertEquals("abcxyzABCXYZ0189-._~!$%26'()*+,;%3D:/?%23%5B%5D@%7B%7D+%50", UriEncoder
            .encodeQueryParam(TEST_STRING, true));
        assertEquals("abcxyzABCXYZ0189-._~!$%26'()*+,;%3D:/?%23%5B%5D@%7B%7D+%2550", UriEncoder
            .encodeQueryParam(TEST_STRING, false));
    }

    public void testEncodeQuery() {
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:/?%23%5B%5D@%7B%7D%20%50", UriEncoder
            .encodeQuery(TEST_STRING, true));
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:/?%23%5B%5D@%7B%7D%20%2550", UriEncoder
            .encodeQuery(TEST_STRING, false));
    }

    public void testEncodeFragment() {
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:/?%23%5B%5D@%7B%7D%20%50", UriEncoder
            .encodeFragment(TEST_STRING, true));
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:/?%23%5B%5D@%7B%7D%20%2550", UriEncoder
            .encodeFragment(TEST_STRING, false));
    }

    public void testEncodeUri() {
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:/?#[]@%7B%7D%20%50", UriEncoder
            .encodeUri(TEST_STRING, true));
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:/?#[]@%7B%7D%20%2550", UriEncoder
            .encodeUri(TEST_STRING, false));
    }

    public void testEncodeUriTemplate() {
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:/?#[]@{}%20%50", UriEncoder
            .encodeUriTemplate(TEST_STRING, true));
        assertEquals("abcxyzABCXYZ0189-._~!$&'()*+,;=:/?#[]@{}%20%2550", UriEncoder
            .encodeUriTemplate(TEST_STRING, false));
    }

    public void testDecodeString() {
        assertEquals("+&", UriEncoder.decodeString(UriEncoder.encodeString("+&")));
        assertEquals("hell0world", UriEncoder.decodeString("hell0world"));
        assertEquals("\u05D0", UriEncoder.decodeString(UriEncoder.encodeString("\u05D0")));
        assertEquals("a b+c", UriEncoder.decodeString("a%20b+c"));
        assertEquals(TEST_STRING,
                     UriEncoder
                         .decodeString("abcxyzABCXYZ0189-._~%21%24%26%27%28%29%2A%2B%2C%3B%3D%3A%2F%3F%23%5B%5D%40%7B%7D%20%2550"));
        assertEquals("a b", UriEncoder.decodeString("a%2Kb"));
    }

    public void testDecodeQuery() {
        assertEquals("a b c", UriEncoder.decodeQuery("a+b%20c"));
    }

    public void testDecodeInternationalURI() {
        // some text editors do not like the UTF-8 encoding so just making
        // sure it doesn't throw an exception for now.
        // see [WINK-208]
        assertNotNull(UriHelper.normalize("http://l/%E3%81%82%E3%81%84"));
    }
}
