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
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

public class ReaderProviderTest {

    private static String message =
                                      "The two most common elements in the world are hydrogen and stupidity";

    @Test
    public void testMessageReader() {

        // Entity Input Stream
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());

        // Entity Stream to be read with ReaderProvider
        ReaderProvider rp = new ReaderProvider();

        // Check if readable - assert true
        assertTrue(rp.isReadable(Reader.class, null, null, null));
        assertTrue(rp.isReadable(Object.class, null, null, null));
        // Check if readable - assert false
        assertFalse(rp.isReadable(StringReader.class, null, null, null));
        assertFalse(" Reading from String.class is not supported", rp.isReadable(String.class,
                                                                                 null,
                                                                                 null,
                                                                                 null));

        Reader reader = null;
        try {
            // Read Entity
            reader = rp.readFrom(null, null, null, MediaType.WILDCARD_TYPE, null, bais);
        } catch (IOException e) {
            assertFalse(" Failed to read Entity", true);
        }

        BufferedReader sr = new BufferedReader(reader);
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
        ReaderProvider rp = new ReaderProvider();

        // Check if writable
        assertTrue(rp.isWriteable(StringReader.class, null, null, null));
        // Check if readable - assert false
        assertFalse(" Writting from String.class is not supported", rp.isWriteable(String.class,
                                                                                   null,
                                                                                   null,
                                                                                   null));

        StringReader reader = new StringReader(message);

        try {
            rp.writeTo(reader, null, null, null, MediaType.WILDCARD_TYPE, null, baos);
        } catch (IOException e) {
            assertFalse(" Failed to write Entity", true);
        }

        assertEquals(message, new String(baos.toByteArray()));
    }

    public static class MyReader extends Reader {

        public MyReader(StringReader reader) {
            this.reader = reader;
        }

        final private Reader reader;
        private boolean      closed = false;

        @Override
        public void close() throws IOException {
            closed = true;
            reader.close();
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            return reader.read(cbuf, off, len);
        }

        public boolean calledClose() {
            return closed;
        }
    }

    @Test
    public void testMessageWriterClose() throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Entity Stream to be read with ReaderProvider
        ReaderProvider rp = new ReaderProvider();

        // Check if writable
        assertTrue(rp.isWriteable(MyReader.class, null, null, null));
        // Check if readable - assert false
        assertFalse(" Writting from String.class is not supported", rp.isWriteable(String.class,
                                                                                   null,
                                                                                   null,
                                                                                   null));

        MyReader reader = new MyReader(new StringReader(message));

        try {
            rp.writeTo(reader, null, null, null, MediaType.WILDCARD_TYPE, null, baos);
        } catch (IOException e) {
            assertFalse(" Failed to write Entity", true);
        }

        assertEquals(message, new String(baos.toByteArray()));
        assertTrue(reader.calledClose());
    }

}
