/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.wink.itest.largeentity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkClientLargeEntityTest extends TestCase {

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/largeentity";
    }

    protected RestClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = new RestClient();
    }

    /**
     * Tests sending a large string. Possible failures including the servlet
     * request buffer being too small, so the status headers do not get set
     * correctly since the message body will have to be flushed out of the
     * servlet response buffer.
     * 
     * @throws Exception
     */
    public void testSendLargeString() throws Exception {
        ByteArrayOutputStream originalContent = new ByteArrayOutputStream();
        for (int c = 0; c < 5000000; ++c) {
            originalContent.write(c);
        }
        byte[] entity = originalContent.toByteArray();
        ClientResponse response =
            client.resource(new URI(getBaseURI() + "/large")).contentType(MediaType.TEXT_XML_TYPE)
                .post(entity);
        assertEquals(277, response.getStatusCode());

        InputStream respStream = response.getEntity(InputStream.class);
        for (int c = 0; c < entity.length; ++c) {
            int respByte = respStream.read();
            assertEquals(entity[c] % 256, (byte)respByte);
        }

        StringBuffer sb = new StringBuffer();
        for (int c = 0; c < 50; ++c) {
            sb.append("abcdefghijklmnopqrstuvwxyz");
        }
        assertEquals(sb.toString(), response.getHeaders().getFirst("appendStringsHeader"));
    }

    /**
     * Tests sending a JAR file.
     * 
     * @throws Exception
     */
    public void testSendJAR() throws Exception {
        ClientResponse response =
            client
                .resource(getBaseURI() + "/large/zip")
                .contentType("application/jar")
                .post(new File(
                               ServerEnvironmentInfo.getWorkDir() + "/wink-itest-targeting-1.1-incubating-SNAPSHOT.war"));
        assertEquals(290, response.getStatusCode());
        assertEquals("META-INF/DEPENDENCIES", response.getEntity(String.class));
    }
}
