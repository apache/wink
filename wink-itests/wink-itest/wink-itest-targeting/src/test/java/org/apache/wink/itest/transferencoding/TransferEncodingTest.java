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

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class TransferEncodingTest extends TestCase {

    private HttpClient          client;

    final private static String BASE_URI = ServerEnvironmentInfo.getBaseURI() + "/transferencoding";

    @Override
    public void setUp() {
        client = new HttpClient();
    }

    /**
     * Tests sending in small bits of chunked content.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testSendSmallGzipContentEncoded() throws HttpException, IOException {
        PostMethod postMethod = new PostMethod(BASE_URI + "/chunkedbook");
        postMethod.setContentChunked(true);

        postMethod.addRequestHeader("Accept", "text/plain");
        ByteArrayOutputStream originalContent = new ByteArrayOutputStream();
        originalContent.write("Hello world".getBytes("UTF-8"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        originalContent.writeTo(baos);
        byte[] content = baos.toByteArray();

        postMethod
            .setRequestEntity(new ByteArrayRequestEntity(content, "text/plain; charset=utf-8"));
        try {
            int result = client.executeMethod(postMethod);
            assertEquals(200, result);
            String response = postMethod.getResponseBodyAsString();
            assertEquals("Hello world", response);
        } finally {
            postMethod.releaseConnection();
        }
    }
}
