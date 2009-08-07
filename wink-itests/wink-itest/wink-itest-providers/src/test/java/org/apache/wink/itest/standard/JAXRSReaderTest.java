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

package org.apache.wink.itest.standard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.itest.standard.ArrayUtils;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class JAXRSReaderTest extends TestCase {

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/standard";
    }

    /**
     * Tests posting to a Reader parameter.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPostReader() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/providers/standard/reader");
        postMethod.setRequestEntity(new StringRequestEntity("abcd", "text/plain", "UTF-8"));
        postMethod.addRequestHeader("Accept", "text/plain");
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            InputStream is = postMethod.getResponseBodyAsStream();

            InputStreamReader isr = new InputStreamReader(is);
            char[] buffer = new char[1];
            int read = 0;
            int offset = 0;
            while ((read = isr.read(buffer, offset, buffer.length - offset)) != -1) {
                offset += read;
                if (offset >= buffer.length) {
                    buffer = ArrayUtils.copyOf(buffer, buffer.length * 2);
                }
            }
            char[] carr = ArrayUtils.copyOf(buffer, offset);

            int checkEOF = is.read();
            assertEquals(-1, checkEOF);
            String str = new String(carr);

            assertEquals("abcd", str);
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
            Header contentLengthHeader = postMethod.getResponseHeader("Content-Length");
            assertNull(contentLengthHeader == null ? "null" : contentLengthHeader.getValue(),
                       contentLengthHeader);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests putting and then getting a Reader.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPutReader() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PutMethod putMethod = new PutMethod(getBaseURI() + "/providers/standard/reader");
        putMethod.setRequestEntity(new StringRequestEntity("wxyz", "char/array", "UTF-8"));
        try {
            client.executeMethod(putMethod);
            assertEquals(204, putMethod.getStatusCode());
        } finally {
            putMethod.releaseConnection();
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/providers/standard/reader");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            InputStream is = getMethod.getResponseBodyAsStream();

            InputStreamReader isr = new InputStreamReader(is);
            char[] buffer = new char[1];
            int read = 0;
            int offset = 0;
            while ((read = isr.read(buffer, offset, buffer.length - offset)) != -1) {
                offset += read;
                if (offset >= buffer.length) {
                    buffer = ArrayUtils.copyOf(buffer, buffer.length * 2);
                }
            }
            char[] carr = ArrayUtils.copyOf(buffer, offset);

            int checkEOF = is.read();
            assertEquals(-1, checkEOF);
            String str = new String(carr);

            assertEquals("wxyz", str);

            String contentType =
                (getMethod.getResponseHeader("Content-Type") == null) ? null : getMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);

            Header contentLengthHeader = getMethod.getResponseHeader("Content-Length");
            assertNull(contentLengthHeader == null ? "null" : contentLengthHeader.getValue(),
                       contentLengthHeader);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * @throws HttpException
     * @throws IOException
     */
    public void testWithRequestAcceptHeaderWillReturnRequestedContentType() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PutMethod putMethod = new PutMethod(getBaseURI() + "/providers/standard/reader");
        putMethod.setRequestEntity(new StringRequestEntity("wxyz", "char/array", "UTF-8"));
        try {
            client.executeMethod(putMethod);
            assertEquals(204, putMethod.getStatusCode());
        } finally {
            putMethod.releaseConnection();
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/providers/standard/reader");
        getMethod.addRequestHeader("Accept", "mytype/subtype");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            InputStream is = getMethod.getResponseBodyAsStream();

            InputStreamReader isr = new InputStreamReader(is);
            char[] buffer = new char[1];
            int read = 0;
            int offset = 0;
            while ((read = isr.read(buffer, offset, buffer.length - offset)) != -1) {
                offset += read;
                if (offset >= buffer.length) {
                    buffer = ArrayUtils.copyOf(buffer, buffer.length * 2);
                }
            }
            char[] carr = ArrayUtils.copyOf(buffer, offset);

            int checkEOF = is.read();
            assertEquals(-1, checkEOF);
            String str = new String(carr);

            assertEquals("wxyz", str);
            assertEquals("mytype/subtype", getMethod.getResponseHeader("Content-Type").getValue());

            Header contentLengthHeader = getMethod.getResponseHeader("Content-Length");
            assertNull(contentLengthHeader == null ? "null" : contentLengthHeader.getValue(),
                       contentLengthHeader);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests a resource method invoked with a BufferedReader as a parameter.
     * This should fail with a 415 since the reader has no way to necessarily
     * wrap it to the type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testInputStreamImplementation() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/providers/standard/reader/subclasses/shouldfail");
        byte[] barr = new byte[1000];
        Random r = new Random();
        r.nextBytes(barr);
        postMethod.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(barr),
                                                                 "any/type"));
        try {
            client.executeMethod(postMethod);
            assertEquals(415, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }
}
