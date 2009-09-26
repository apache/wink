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
package org.apache.wink.itest.contentencoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpException;
import org.apache.wink.client.ApacheHttpClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;

public class WinkApacheContentEncodingTest extends WinkContentEncodingTest {

    @Override
    public void setUp() {
        client = new RestClient(new ApacheHttpClientConfig());
    }

    /**
     * Tests sending in small bits of gzip encoded content.
     * 
     * @throws HttpException
     * @throws IOException
     */
    @Override
    public void testSendSmallGzipContentEncoded() throws HttpException, IOException {
        ByteArrayOutputStream originalContent = new ByteArrayOutputStream();
        originalContent.write("Hello world".getBytes("UTF-8"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOut = new GZIPOutputStream(baos);

        originalContent.writeTo(gzipOut);

        gzipOut.finish();
        byte[] content = baos.toByteArray();

        ClientResponse response =
            client.resource(BASE_URI + "/bigbook").accept(MediaType.TEXT_PLAIN)
                .header("Content-Encoding", "gzip").contentType("text/plain; charset=utf-8")
                .post(content);
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("Hello world" + "helloworld", responseBody);
    }
}
