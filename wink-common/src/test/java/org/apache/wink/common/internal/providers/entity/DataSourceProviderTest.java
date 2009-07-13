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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.providers.entity.DataSourceProvider;
import org.junit.Test;


public class DataSourceProviderTest {

    private static String message = "If you are not part of the cure, then you are part of the problem";

    @Test
    public void testMessageReader() throws IOException {

        // Entity Input Stream
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());

        // Entity Stream to be read with DataSourceProvider
        DataSourceProvider dsp = new DataSourceProvider();
        DataSourceProvider.ByteArrayDataSource bads = new DataSourceProvider.ByteArrayDataSource(bais, "text/plain");

        // Check if readable - assert true
        assertTrue(dsp.isReadable(DataSource.class, null, null, null));
        assertTrue(dsp.isReadable(Object.class, null, null, null));
        // Check if readable - assert false
        assertFalse(dsp.isReadable(DataSourceProvider.ByteArrayDataSource.class, null, null, null));
        assertFalse(" Reading to String.class is not supported", dsp.isReadable(String.class, null, null, null));

        DataSource ds = null;
        try {
            // Read Entity
            ds = dsp.readFrom(null, null, null, MediaType.WILDCARD_TYPE, null, bads.getInputStream());
        } catch (IOException e) {
            assertFalse(" Failed to read Entity", true);
        }

        InputStream inputStream = null;
        byte[] buf = new byte[1024];
        int read = 0;
        try {
            inputStream = ds.getInputStream();
            read = inputStream.read(buf);
        } catch (IOException e) {
            assertFalse(" Failed to read Entity", true);
        }
        assertEquals(message, new String(buf, 0, read));

    }

    @Test
    public void testMessageWriter() throws IOException {

        // Entity Data Source
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
        DataSourceProvider.ByteArrayDataSource bads = new DataSourceProvider.ByteArrayDataSource(bais, "text/plain");

        // Entity Stream to be read with DataSourceProvider
        DataSourceProvider dsp = new DataSourceProvider();

        // Check if writable - assert true
        assertTrue(dsp.isWriteable(DataSourceProvider.ByteArrayDataSource.class, null, null, null));
        
        // Check if writable - assert false
        assertFalse(" Writting from String.class is not supported", dsp.isWriteable(String.class, null, null, null));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            dsp.writeTo(bads, null, null, null, MediaType.WILDCARD_TYPE, null, baos);
        } catch (IOException e) {
            assertFalse(" Failed to write Entity", true);
        }

        assertEquals(message, new String(baos.toByteArray()));
    }
}
