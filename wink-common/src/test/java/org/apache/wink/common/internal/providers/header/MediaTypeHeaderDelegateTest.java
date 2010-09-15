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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MediaTypeHeaderDelegateTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testParseSerializeMediaType() {

        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        HeaderDelegate<MediaType> mtd = rd.createHeaderDelegate(MediaType.class);
        if (mtd == null) {
            fail("MediaType header delegate is not regestered in RuntimeDelegateImpl");
        }

        MediaType mt = mtd.fromString("application/xml");
        assertEquals(mt.getType(), "application");
        assertEquals(mt.getSubtype(), "xml");
        assertEquals(mt.getParameters().size(), 0);
        assertFalse(mt.isWildcardType());
        assertFalse(mt.isWildcardSubtype());
        assertEquals("application/xml", mt.toString());

        mt = mtd.fromString("application/*");
        assertEquals(mt.getType(), "application");
        assertEquals(mt.getSubtype(), "*");
        assertFalse(mt.isWildcardType());
        assertTrue(mt.isWildcardSubtype());
        assertEquals(mt.getParameters().size(), 0);
        assertEquals("application/*", mt.toString());

        mt = mtd.fromString("application/xml;a=b;c=d;e=f");
        assertEquals(mt.getType(), "application");
        assertEquals(mt.getSubtype(), "xml");
        assertFalse(mt.isWildcardType());
        assertFalse(mt.isWildcardSubtype());
        Map<String, String> map = mt.getParameters();
        assertEquals(map.size(), 3);
        assertEquals(map.get("a"), "b");
        assertEquals(map.get("c"), "d");
        assertEquals(map.get("e"), "f");
        assertEquals("application/xml;a=b;c=d;e=f", mt.toString());

        // negative test
        boolean isException = false;
        try {
            mt = mtd.fromString("applicationxml");
        } catch (IllegalArgumentException e) {
            isException = true;
        }
        assertTrue(isException);

        try {
            mtd.toString(null);
            fail("MediaType is null- IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }

        try {
            mtd.fromString(null);
            fail("MediaType is null- IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testToleranceOfMalformedMediaTypes() {
        MediaType mt = MediaType.valueOf("text/html;;charset=utf-8");
        assertEquals("text", mt.getType());
        assertEquals("html", mt.getSubtype());
        assertEquals(1, mt.getParameters().size());
        assertEquals("utf-8", mt.getParameters().get("charset"));

        mt = MediaType.valueOf("text/html; charset: UTF-8");
        assertEquals("text", mt.getType());
        assertEquals("html", mt.getSubtype());
        assertEquals(0, mt.getParameters().size());

        mt = MediaType.valueOf("text/html; charset=");
        assertEquals("text", mt.getType());
        assertEquals("html", mt.getSubtype());
        assertEquals(0, mt.getParameters().size());

        mt = MediaType.valueOf("text/html; $str_charset; charset=ISO-8859-1");
        assertEquals("text", mt.getType());
        assertEquals("html", mt.getSubtype());
        assertEquals(1, mt.getParameters().size());
        assertEquals("ISO-8859-1", mt.getParameters().get("charset"));

        mt = MediaType.valueOf("text/html; UTF-8;charset=ISO-8859-1");
        assertEquals("text", mt.getType());
        assertEquals("html", mt.getSubtype());
        assertEquals(1, mt.getParameters().size());
        assertEquals("ISO-8859-1", mt.getParameters().get("charset"));

        mt = MediaType.valueOf("text/html; utf-8");
        assertEquals("text", mt.getType());
        assertEquals("html", mt.getSubtype());
        assertEquals(0, mt.getParameters().size());

        mt = MediaType.valueOf("text/html; UTF-8;charset=UTF-8");
        assertEquals("text", mt.getType());
        assertEquals("html", mt.getSubtype());
        assertEquals(1, mt.getParameters().size());
        assertEquals("UTF-8", mt.getParameters().get("charset"));
    }
}
