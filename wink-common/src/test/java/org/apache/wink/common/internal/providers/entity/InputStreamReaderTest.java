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

package org.apache.wink.common.internal.providers.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

public class InputStreamReaderTest {

    private static String message =
                                      "The two most common elements in the world are hydrogen and stupidity";

    @Test
    public void testMessageInputStream() {

        // Entity Input Stream
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());

        // Entity Stream to be read with ReaderProvider
        InputStreamProvider ip = new InputStreamProvider();

        // Check if readable - assert true
        assertTrue(ip.isReadable(InputStream.class, null, null, null));
        assertTrue(ip.isReadable(Object.class, null, null, null));
        // Check if readable - assert false
        assertFalse(ip.isReadable(ByteArrayInputStream.class, null, null, null));
        assertFalse(" Reading from String.class is not supported", ip.isReadable(String.class,
                                                                                 null,
                                                                                 null,
                                                                                 null));

        InputStream istream = null;
        try {
            // Read Entity
            istream = ip.readFrom(null, null, null, MediaType.WILDCARD_TYPE, null, bais);
        } catch (IOException e) {
            assertFalse(" Failed to read Entity", true);
        }

        BufferedReader sr = new BufferedReader(new InputStreamReader(istream));
        char[] cbuf = new char[message.length()];
        try {
            sr.read(cbuf);
        } catch (IOException e) {
            assertFalse(" Failed to read Entity", true);
        }

        assertEquals(message, new String(cbuf));

    }

    @Test
    public void testMessageWriter() throws UnsupportedEncodingException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Entity Stream to be read with ReaderProvider
        InputStreamProvider ip = new InputStreamProvider();

        // Check if writable
        assertTrue(ip.isWriteable(ByteArrayInputStream.class, null, null, null));
        // Check if readable - assert false
        assertFalse(" Writting from String.class is not supported", ip.isWriteable(String.class,
                                                                                   null,
                                                                                   null,
                                                                                   null));

        try {
            ip.writeTo(new ByteArrayInputStream(message.getBytes()),
                       null,
                       null,
                       null,
                       MediaType.WILDCARD_TYPE,
                       null,
                       baos);
        } catch (IOException e) {
            assertFalse(" Failed to write Entity", true);
        }

        assertEquals(message, new String(baos.toByteArray()));
    }

    public static class MyStream extends InputStream {

        public MyStream(ByteArrayInputStream istream) {
            this.istream = istream;
        }

        final private InputStream istream;
        private boolean           closed = false;

        @Override
        public void close() throws IOException {
            closed = true;
            istream.close();
        }

        public boolean calledClose() {
            return closed;
        }

        @Override
        public int read() throws IOException {
            return istream.read();
        }
    }

    @Test
    public void testMessageWriterClose() throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Entity Stream to be read with ReaderProvider
        InputStreamProvider ip = new InputStreamProvider();

        // Check if writable
        assertTrue(ip.isWriteable(MyStream.class, null, null, null));
        // Check if readable - assert false
        assertFalse(" Writting from String.class is not supported", ip.isWriteable(String.class,
                                                                                   null,
                                                                                   null,
                                                                                   null));

        MyStream istream = new MyStream(new ByteArrayInputStream(message.getBytes()));

        try {
            ip.writeTo(istream, null, null, null, MediaType.WILDCARD_TYPE, null, baos);
        } catch (IOException e) {
            assertFalse(" Failed to write Entity", true);
        }

        assertEquals(message, new String(baos.toByteArray()));
        assertTrue(istream.calledClose());
    }

}
