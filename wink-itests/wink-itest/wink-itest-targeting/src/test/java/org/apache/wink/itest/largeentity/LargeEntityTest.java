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

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class LargeEntityTest extends TestCase {

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/largeentity";
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
        PostMethod postMethod = new PostMethod(getBaseURI() + "/large");
        HttpClient client = new HttpClient();
        try {

            ByteArrayOutputStream originalContent = new ByteArrayOutputStream();
            for (int c = 0; c < 5000000; ++c) {
                originalContent.write(c);
            }
            byte[] entity = originalContent.toByteArray();
            postMethod.setRequestEntity(new ByteArrayRequestEntity(entity, "text/xml"));
            client.executeMethod(postMethod);
            assertEquals(277, postMethod.getStatusCode());

            InputStream respStream = postMethod.getResponseBodyAsStream();
            for (int c = 0; c < entity.length; ++c) {
                int respByte = respStream.read();
                assertEquals(entity[c] % 256, (byte)respByte);
            }

            // final int maxHeaderLength = 100;
            // int headerLength = (entity.length < maxHeaderLength) ?
            // entity.length : maxHeaderLength;
            // byte[] headerBytes = new byte[headerLength];
            // for (int c = 0; c < headerLength; ++c) {
            // headerBytes[c] = entity[c];
            // }
            //

            StringBuffer sb = new StringBuffer();
            for (int c = 0; c < 50; ++c) {
                sb.append("abcdefghijklmnopqrstuvwxyz");
            }
            // String expectedHeaderValue = new String(headerBytes, "UTF-8");
            Header header = postMethod.getResponseHeader("appendStringsHeader");
            assertNotNull(header);
            assertEquals(sb.toString(), header.getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests sending a large string. Possible failures including the servlet
     * request buffer being too small, so the status headers do not get set
     * correctly since the message body will have to be flushed out of the
     * servlet response buffer.
     * 
     * @throws Exception
     */
    public void testSendLargeStringChunked() throws Exception {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/large");
        postMethod.setContentChunked(true);
        HttpClient client = new HttpClient();
        try {

            ByteArrayOutputStream originalContent = new ByteArrayOutputStream();
            for (int c = 0; c < 5000000; ++c) {
                originalContent.write(c);
            }
            byte[] entity = originalContent.toByteArray();
            postMethod.setRequestEntity(new ByteArrayRequestEntity(entity, "text/xml"));
            client.executeMethod(postMethod);
            assertEquals(277, postMethod.getStatusCode());

            InputStream respStream = postMethod.getResponseBodyAsStream();
            for (int c = 0; c < entity.length; ++c) {
                int respByte = respStream.read();
                assertEquals(entity[c] % 256, (byte)respByte);
            }

            // int headerLength = (entity.length < 2048) ? entity.length : 2048;
            // byte[] headerBytes = new byte[headerLength];
            // for (int c = 0; c < headerLength; ++c) {
            // headerBytes[c] = entity[c];
            // }

            StringBuffer sb = new StringBuffer();
            for (int c = 0; c < 50; ++c) {
                sb.append("abcdefghijklmnopqrstuvwxyz");
            }
            // String expectedHeaderValue = new String(headerBytes, "UTF-8");
            Header header = postMethod.getResponseHeader("appendStringsHeader");
            assertNotNull(header);
            assertEquals(sb.toString(), header.getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests sending a JAR file.
     * 
     * @throws Exception
     */
    public void testSendJAR() throws Exception {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/large/zip");
        HttpClient client = new HttpClient();
        try {
            System.out
                .println(new File(
                                  ServerEnvironmentInfo.getWorkDir() + "/wink-itest-targeting-0.2-incubating-SNAPSHOT.war")
                    .getAbsoluteFile().getAbsolutePath());
            postMethod.setRequestEntity(new FileRequestEntity(new File(ServerEnvironmentInfo
                .getWorkDir() + "/wink-itest-targeting-0.2-incubating-SNAPSHOT.war"),
                                                              "application/jar"));
            client.executeMethod(postMethod);
            assertEquals(290, postMethod.getStatusCode());
            String resp = postMethod.getResponseBodyAsString();
            assertEquals("META-INF/DEPENDENCIES", resp);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests sending a JAR file in chunked transfer format.
     * 
     * @throws Exception
     */
    public void testSendJARChunked() throws Exception {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/large/zip");
        postMethod.setContentChunked(true);
        HttpClient client = new HttpClient();
        try {
            System.out
                .println(new File(
                                  ServerEnvironmentInfo.getWorkDir() + "/wink-itest-targeting-0.2-incubating-SNAPSHOT.war")
                    .getAbsoluteFile().getAbsolutePath());
            postMethod.setRequestEntity(new FileRequestEntity(new File(ServerEnvironmentInfo
                .getWorkDir() + "/wink-itest-targeting-0.2-incubating-SNAPSHOT.war"),
                                                              "application/jar"));
            client.executeMethod(postMethod);
            assertEquals(290, postMethod.getStatusCode());
            String resp = postMethod.getResponseBodyAsString();
            assertEquals("META-INF/DEPENDENCIES", resp);
        } finally {
            postMethod.releaseConnection();
        }
    }
}
