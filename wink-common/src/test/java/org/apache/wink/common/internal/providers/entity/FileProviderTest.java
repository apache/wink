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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;

public class FileProviderTest {
    FileProvider fileProvider = null;

    @Before
    public void setUp() throws Exception {
        fileProvider = new FileProvider();

    }

    @Test
    public void testFileProvider() throws Exception {

        String s = "tttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt";
        byte[] ba = s.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // write Buffer 2 file
        assertTrue(fileProvider.isWriteable(File.class,
                                            null,
                                            null,
                                            MediaType.MULTIPART_FORM_DATA_TYPE));
        File f =
            fileProvider.readFrom(File.class,
                                  null,
                                  null,
                                  MediaType.APPLICATION_SVG_XML_TYPE,
                                  null,
                                  bais);

        // read File 2 Buffer
        assertTrue(fileProvider.isReadable(File.class, null, null, null));
        fileProvider.writeTo(f, null, null, null, null, null, baos);
        byte[] after = baos.toByteArray();

        // check the same
        assertEquals(ba.length, after.length);

        // check Size
        long size = fileProvider.getSize(f, null, null, null, null);
        assertEquals(size, after.length);

        // try to return serialize Dir
        File dir = File.createTempFile("aaa", "bbb");
        dir.delete();
        dir.mkdir();
        boolean exceptionWasThrown = false;
        try {
            fileProvider.writeTo(dir, null, null, null, null, null, baos);
        } catch (WebApplicationException e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);

        int i = 0;
        i++;

    }

}
