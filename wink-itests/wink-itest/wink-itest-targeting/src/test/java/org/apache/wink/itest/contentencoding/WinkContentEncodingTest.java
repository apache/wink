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
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.commons.httpclient.ChunkedOutputStream;
import org.apache.commons.httpclient.HttpException;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkContentEncodingTest extends TestCase {

    protected RestClient          client;

    final protected static String BASE_URI =
                                             ServerEnvironmentInfo.getBaseURI() + ((ServerEnvironmentInfo
                                                 .isRestFilterUsed()) ? "" : "/contentencoding");

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Tests sending in small bits of gzip encoded content.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testSendSmallGzipContentEncoded() throws HttpException, IOException {
        ByteArrayOutputStream originalContent = new ByteArrayOutputStream();
        originalContent.write("Hello world".getBytes("UTF-8"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChunkedOutputStream chunkedOut = new ChunkedOutputStream(baos);
        GZIPOutputStream gzipOut = new GZIPOutputStream(chunkedOut);

        originalContent.writeTo(gzipOut);

        gzipOut.finish();
        chunkedOut.finish();
        byte[] content = baos.toByteArray();

        ClientResponse response =
            client.resource(BASE_URI + "/bigbook").accept(MediaType.TEXT_PLAIN)
                .header("Transfer-Encoding", "chunked").header("Content-Encoding", "gzip")
                .contentType("text/plain; charset=utf-8").post(content);
        assertEquals(200, response.getStatusCode());
        String responseBody = response.getEntity(String.class);
        assertEquals("Hello world" + "helloworld", responseBody);
    }

    /**
     * Tests sending in small bits of gzip encoded content.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testSendLargeGzipContentEncoded() throws HttpException, IOException {
        ByteArrayOutputStream originalContent = new ByteArrayOutputStream();
        for (int c = 0; c < 5000000; ++c) {
            originalContent.write(c);
        }

        /*
         * gzip the contents
         */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
        originalContent.writeTo(gzipOut);
        gzipOut.finish();
        byte[] content = baos.toByteArray();

        ClientResponse response =
            client.resource(BASE_URI + "/bigbook/mirror").accept(MediaType.TEXT_PLAIN)
                .header("Content-Encoding", "gzip").contentType("text/plain; charset=utf-8")
                .post(content);

        assertEquals(200, response.getStatusCode());
        InputStream responseStream = response.getEntity(InputStream.class);
        for (int c = 0; c < 5000000; ++c) {
            assertEquals(c % 256, responseStream.read());
        }
    }

    /**
     * Tests sending in small bits of gzip encoded content.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testSendLargeGzipContentEncodedAndReceiveContentEncoded() throws HttpException,
        IOException {
        ByteArrayOutputStream originalContent = new ByteArrayOutputStream();
        for (int c = 0; c < 5000000; ++c) {
            originalContent.write(c);
        }

        /*
         * gzip the contents
         */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
        originalContent.writeTo(gzipOut);
        gzipOut.finish();
        byte[] content = baos.toByteArray();

        ClientResponse response =
            client.resource(BASE_URI + "/bigbook/mirror").header("Accept-Encoding", "gzip")
                .accept(MediaType.TEXT_PLAIN).header("Content-Encoding", "gzip")
                .contentType("text/plain; charset=utf-8").post(content);

        assertEquals(200, response.getStatusCode());
        InputStream responseStream = new GZIPInputStream(response.getEntity(InputStream.class));
        for (int c = 0; c < 5000000; ++c) {
            assertEquals(c % 256, responseStream.read());
        }
    }

}
