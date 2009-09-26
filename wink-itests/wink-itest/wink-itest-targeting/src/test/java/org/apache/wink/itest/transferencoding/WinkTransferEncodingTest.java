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
package org.apache.wink.itest.transferencoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpException;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkTransferEncodingTest extends TestCase {

    protected RestClient        client;

    final private static String BASE_URI =
                                             ServerEnvironmentInfo.getBaseURI() + ((ServerEnvironmentInfo
                                                 .isRestFilterUsed()) ? "" : "/transferencoding");

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Tests sending in small bits of chunked content.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testSendSmallGzipContentEncoded() throws HttpException, IOException {
        ByteArrayOutputStream originalContent = new ByteArrayOutputStream();
        originalContent.write("Hello world".getBytes("UTF-8"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        originalContent.writeTo(baos);
        byte[] content = baos.toByteArray();

        ClientResponse response =
            client.resource(BASE_URI + "/chunkedbook").accept(MediaType.TEXT_PLAIN_TYPE)
                .contentType("text/plain; charset=utf-8").post(content);
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("Hello world", responseBody);
    }
}
