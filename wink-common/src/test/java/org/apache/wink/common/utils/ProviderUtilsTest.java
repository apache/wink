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
package org.apache.wink.common.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

public class ProviderUtilsTest {

    @Test
    public void testGetCharset() {
        MediaType mediaType = null;
        String charset = ProviderUtils.getCharset(mediaType);
        assertEquals(charset, "UTF-8");

        mediaType = new MediaType("application", "atom+xml");
        charset = ProviderUtils.getCharset(mediaType);
        assertEquals(charset, "UTF-8");

        Map<String, String> parameters = new LinkedHashMap<String, String>();
        parameters.put("charset", "UTF-16");
        mediaType = new MediaType("application", "atom+xml", parameters);
        charset = ProviderUtils.getCharset(mediaType);
        assertEquals(charset, "UTF-16");

        // check case insensitive
        parameters = new LinkedHashMap<String, String>();
        parameters.put("CharSet", "UTF-16");
        mediaType = new MediaType("application", "atom+xml", parameters);
        charset = ProviderUtils.getCharset(mediaType);
        assertEquals(charset, "UTF-16");

    }

    @Test
    public void testCreateReader() throws Exception {
        String s = "sssssssssssssss";
        String cs = "UTF-8";
        char[] ca = new char[100];

        InputStream is = new ByteArrayInputStream(s.getBytes(cs));
        Map<String, String> parameters = new LinkedHashMap<String, String>();
        parameters.put("CharSet", cs);
        MediaType mediaType = new MediaType("application", "atom+xml", parameters);
        Reader r = ProviderUtils.createReader(is, mediaType);
        int size = r.read(ca);
        String s2 = new String(ca, 0, size);
        assertEquals(s2, s);

        is = new ByteArrayInputStream(s.getBytes("UTF-16"));
        parameters = new LinkedHashMap<String, String>();
        parameters.put("CharSet", cs);
        mediaType = new MediaType("application", "atom+xml", parameters);
        r = ProviderUtils.createReader(is, mediaType);
        size = r.read(ca);
        s2 = new String(ca, 0, size);
        assertNotSame(s, s2);

    }
    /*
     * @Test public void testWriteToStringProvidersObjectMediaType() {
     * fail("Not yet implemented"); }
     * @Test public void testWriteToStringProvidersObjectClassOfQMediaType() {
     * fail("Not yet implemented"); }
     * @Test public void testWriteToStringProvidersObjectClassOfQTypeMediaType()
     * { fail("Not yet implemented"); }
     * @Test public void
     * testWriteToStringProvidersObjectClassOfQTypeMultivaluedMapOfStringObjectMediaType
     * () { fail("Not yet implemented"); }
     * @Test public void testReadFromStringProvidersStringClassOfTMediaType() {
     * fail("Not yet implemented"); }
     * @Test public void
     * testReadFromStringProvidersStringClassOfTTypeMediaType() {
     * fail("Not yet implemented"); }
     * @Test public void
     * testReadFromStringProvidersStringClassOfTTypeMultivaluedMapOfStringStringMediaType
     * () { fail("Not yet implemented"); }
     */
}
