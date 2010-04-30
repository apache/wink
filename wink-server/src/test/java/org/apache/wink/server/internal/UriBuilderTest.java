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

package org.apache.wink.server.internal;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import junit.framework.TestCase;

public class UriBuilderTest extends TestCase {

    /**
     * Tests instances where {@link UriBuilder#build(Object...)} should throw an
     * IllegalArgumentException.
     * 
     * @throws Exception
     */
    public void testIllegalArgumentExceptionFromBuild() throws Exception {
        URI uri = UriBuilder.fromUri("http://www.example.com").build();
        assertEquals(uri.toASCIIString(), "http://www.example.com");

        uri = UriBuilder.fromUri("http://www.example.com/").path("{arg1}").build("someValue");
        assertEquals(uri.toASCIIString(), "http://www.example.com/someValue");

        uri =
            UriBuilder.fromUri("http://www.example.com/").path("{arg1}").build("someValue",
                                                                               "otherValue");
        assertEquals(uri.toASCIIString(), "http://www.example.com/someValue");

        try {
            UriBuilder.fromUri("http://www.example.com/").path("{arg1}").build();
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            /* good catch */
        }

        try {
            UriBuilder.fromUri("http://www.example.com/").path("{arg1}").build((Object)null);
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            /* good catch */
        }

        try {
            UriBuilder.fromUri("http://www.example.com/").build((Object)null);
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            /* good catch */
        }

        try {
            UriBuilder.fromUri("http://www.example.com/").path("{arg1}").build("value",
                                                                               (Object)null);
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            /* good catch */
        }

        uri = UriBuilder.fromPath("somePath").build();
        assertEquals(uri.toASCIIString(), "somePath");

        uri = UriBuilder.fromPath("somePath/{arg1}").build("value");
        assertEquals(uri.toASCIIString(), "somePath/value");

        uri = UriBuilder.fromPath("somePath/{arg1}").build("value", "otherValue");
        assertEquals(uri.toASCIIString(), "somePath/value");

        try {
            UriBuilder.fromPath("somePath/{arg1}").build();
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            /* good catch */
        }

        try {
            UriBuilder.fromPath("somePath/{arg1}").build((Object)null);
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            /* good catch */
        }

        try {
            UriBuilder.fromPath("somePath/").build((Object)null);
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            /* good catch */
        }

        try {
            UriBuilder.fromPath("somePath/{arg1}").build("value", (Object)null);
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            /* good catch */
        }
    }

    /**
     * Tests instances where {@link UriBuilder#buildFromEncodedMap(Map)} should
     * throw an IllegalArgumentException.
     * 
     * @throws Exception
     */
    public void testBuiltFromEncoded() throws Exception {
        Map<String, String> valueMap = Collections.singletonMap("arg1", "Hello");
        URI uri = UriBuilder.fromUri("http://www.example.com").buildFromEncodedMap(valueMap);
        assertEquals(uri.toASCIIString(), "http://www.example.com");

        uri =
            UriBuilder.fromUri("http://www.example.com").path("{arg1}")
                .buildFromEncodedMap(valueMap);
        assertEquals(uri.toASCIIString(), "http://www.example.com/Hello");

        valueMap = Collections.singletonMap("arg1", null);

        try {
            uri =
                UriBuilder.fromUri("http://www.example.com").path("{arg1}")
                    .buildFromEncodedMap(valueMap);
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            /* good catch */
        }

        try {
            uri =
                UriBuilder.fromUri("http://www.example.com").path("{arg1}")
                    .buildFromEncodedMap(null);
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            /* good catch */
        }
    }

    public void testIllegalHostnameReplacement() throws Exception {
        try {
            UriBuilder.fromUri("http://www.ibm.com/").host("").build();
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            /* good catch */
        }

        /* this is valid */
        URI uri = UriBuilder.fromUri("http://www.ibm.com/").build();
        assertEquals("www.ibm.com", uri.getHost());

        uri = UriBuilder.fromUri("http://www.ibm.com/").host(null).build();
        assertEquals(null, uri.getHost());
    }

    public void testIllegalPortReplacement() throws Exception {
        try {
            UriBuilder.fromUri("http://www.ibm.com:9080/").port(-100).build();
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            /* good catch */
        }

        /* this is valid */
        URI uri = UriBuilder.fromUri("http://www.ibm.com/").port(9080).build();
        assertEquals(9080, uri.getPort());

        uri = UriBuilder.fromUri("http://www.ibm.com:9080/").build();
        assertEquals(9080, uri.getPort());

        uri = UriBuilder.fromUri("http://www.ibm.com/").port(-1).build();
        assertEquals(-1, uri.getPort());

        uri = UriBuilder.fromUri("http://www.ibm.com:9080/").port(-1).build();
        assertEquals(-1, uri.getPort());

    }
}
