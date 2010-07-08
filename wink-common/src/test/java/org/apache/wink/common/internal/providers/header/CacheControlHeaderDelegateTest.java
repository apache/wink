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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.junit.Test;

public class CacheControlHeaderDelegateTest {

    @Test
    public void testSerializeCacheControlHeader() {
        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        HeaderDelegate<CacheControl> cacheControlHeaderDelegate =
            rd.createHeaderDelegate(CacheControl.class);
        if (cacheControlHeaderDelegate == null) {
            fail("CacheControl header delegate is not regestered in RuntimeDelegateImpl");
        }

        String expectedCacheControlHeader =
            "private=\"privateField1\", " + "no-cache=\"noCachefield1, noCachefield2\", "
                + "no-store, "
                + "no-transform, "
                + "must-revalidate, "
                + "proxy-revalidate, "
                + "max-age=21600, "
                + "s-maxage=3000, "
                + "extension=value, "
                + "extension3, "
                + "extension2=\"value with space\"";
        CacheControl cc = new CacheControl();
        cc.setMaxAge(21600);
        cc.setMustRevalidate(true);
        cc.setNoCache(true);
        cc.setNoStore(true);
        cc.setNoTransform(true);
        cc.setPrivate(true);
        cc.setProxyRevalidate(true);
        cc.setSMaxAge(3000);
        cc.getCacheExtension().put("extension", "value");
        cc.getCacheExtension().put("extension2", "value with space");
        cc.getCacheExtension().put("extension3", null);
        cc.getNoCacheFields().add("noCachefield1");
        cc.getNoCacheFields().add("noCachefield2");
        cc.getPrivateFields().add("privateField1");

        String[] cache_out = cacheControlHeaderDelegate.toString(cc).split(",");
        String[] cache_expected = expectedCacheControlHeader.split(",");
        Arrays.sort(cache_expected);
        Arrays.sort(cache_out);
        assertArrayEquals(cache_expected, cache_out);
        // assertEquals(expectedCacheControlHeader,
        // cacheControlHeaderDelegate.toString(cc));

        try {
            cacheControlHeaderDelegate.toString(null);
            fail("CacheControl Header is null - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testParseCacheControlHeader() {
        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        HeaderDelegate<CacheControl> cacheControlHeaderDelegate =
            rd.createHeaderDelegate(CacheControl.class);
        if (cacheControlHeaderDelegate == null) {
            fail("CacheControl header delegate is not regestered in RuntimeDelegateImpl");
        }

        try {
            cacheControlHeaderDelegate.fromString("no-cache");
            fail("JAX-RS CacheControl type is designed to support only cache-response-directives - UnsupportedOperationException must be thrown");
        } catch (UnsupportedOperationException e) {
            // success
        }

    }

}
