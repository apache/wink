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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.SymphonyApplication;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.MockServletInvocationTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


/**
 * Tests Response created from InputStream and Reader
 */
public class StreamResponseTest extends MockServletInvocationTest {

    private static byte[] BYTES  = new byte[] { 123, 23, 43, 54, 34, 66, 89, 77 };
    private static String STRING = createTestString();

    
    @Override
    protected Application getApplication() {
        return new SymphonyApplication() {
            @Override
            public Set<Object> getInstances() {
                Set<Object> set = new HashSet<Object>();
                set.add(new Resource());
                return set;
            }
        };
    }

    private static String createTestString() {
        StringBuilder sb = new StringBuilder();
        for (int cnt = 0; cnt < 1000; cnt++) {
            sb.append("abcdefghi+");
        }
        String v = sb.toString();
        return v;
    }

    @Path("/path")
    public static final class Resource {

        @GET
        @Produces(MediaTypeUtils.IMAGE_JPEG)
        public ByteArrayInputStream getBinary() {
            
            return new ByteArrayInputStream(BYTES);
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public StringReader getChar() {
            return new StringReader(STRING);
        }

    } // 

    public void testStreamResponse() throws IOException {
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "path",
            MediaTypeUtils.IMAGE_JPEG_TYPE);
        MockHttpServletResponse response = invoke(request);
        byte[] responseBytes = response.getContentAsByteArray();
        assertEquals("len", BYTES.length, responseBytes.length);
        for (int pos = 0; pos < BYTES.length; pos++) {
            assertEquals("byte " + pos, BYTES[pos], responseBytes[pos]);
        }
    }

    public void testReaderResponse() throws IOException {
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET", "path",
            MediaType.TEXT_PLAIN_TYPE);
        MockHttpServletResponse response = invoke(request);
        assertEquals("len", STRING, response.getContentAsString());
    }

}
