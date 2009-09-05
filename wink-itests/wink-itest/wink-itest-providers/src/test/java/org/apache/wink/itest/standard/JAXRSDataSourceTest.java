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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.activation.DataSource;
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.wink.common.internal.providers.entity.DataSourceProvider;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class JAXRSDataSourceTest extends TestCase {

    public String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/standard";
    }

    /**
     * Tests posting to a DataSource entity parameter.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPostDataSource() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/providers/standard/datasource");
        byte[] barr = new byte[1000];
        Random r = new Random();
        r.nextBytes(barr);
        postMethod.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(barr),
                                                                 "text/plain"));
        postMethod.addRequestHeader("Accept", "text/plain");
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            InputStream is = postMethod.getResponseBodyAsStream();

            byte[] receivedBArr = new byte[1000];
            DataInputStream dis = new DataInputStream(is);
            dis.readFully(receivedBArr);

            int checkEOF = dis.read();
            assertEquals(-1, checkEOF);
            for (int c = 0; c < barr.length; ++c) {
                assertEquals(barr[c], receivedBArr[c]);
            }
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
            assertNull((postMethod.getResponseHeader("Content-Length") == null) ? "" : postMethod
                .getResponseHeader("Content-Length").getValue(), postMethod
                .getResponseHeader("Content-Length"));
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests putting and then getting a DataSource entity.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPutDataSource() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PutMethod putMethod = new PutMethod(getBaseURI() + "/providers/standard/datasource");
        byte[] barr = new byte[1000];
        Random r = new Random();
        r.nextBytes(barr);
        putMethod.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(barr),
                                                                "bytes/array"));
        try {
            client.executeMethod(putMethod);
            assertEquals(204, putMethod.getStatusCode());
        } finally {
            putMethod.releaseConnection();
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/providers/standard/datasource");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            InputStream is = getMethod.getResponseBodyAsStream();

            byte[] receivedBArr = new byte[1000];
            DataInputStream dis = new DataInputStream(is);
            dis.readFully(receivedBArr);

            int checkEOF = dis.read();
            assertEquals(-1, checkEOF);
            for (int c = 0; c < barr.length; ++c) {
                assertEquals(barr[c], receivedBArr[c]);
            }

            String contentType =
                (getMethod.getResponseHeader("Content-Type") == null) ? null : getMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
            assertNull((getMethod.getResponseHeader("Content-Length") == null) ? "" : getMethod
                .getResponseHeader("Content-Length").getValue(), getMethod
                .getResponseHeader("Content-Length"));
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests receiving a DataSource with any media type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWithRequestAcceptHeaderWillReturnRequestedContentType() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PutMethod putMethod = new PutMethod(getBaseURI() + "/providers/standard/datasource");
        byte[] barr = new byte[1000];
        Random r = new Random();
        r.nextBytes(barr);
        putMethod.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(barr),
                                                                "any/type"));
        try {
            client.executeMethod(putMethod);
            assertEquals(204, putMethod.getStatusCode());
        } finally {
            putMethod.releaseConnection();
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/providers/standard/datasource");
        getMethod.addRequestHeader("Accept", "mytype/subtype");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            InputStream is = getMethod.getResponseBodyAsStream();

            byte[] receivedBArr = new byte[1000];
            DataInputStream dis = new DataInputStream(is);
            dis.readFully(receivedBArr);

            int checkEOF = dis.read();
            assertEquals(-1, checkEOF);
            for (int c = 0; c < barr.length; ++c) {
                assertEquals(barr[c], receivedBArr[c]);
            }
            assertEquals("mytype/subtype", getMethod.getResponseHeader("Content-Type").getValue());
            assertNull((getMethod.getResponseHeader("Content-Length") == null) ? "" : getMethod
                .getResponseHeader("Content-Length").getValue(), getMethod
                .getResponseHeader("Content-Length"));
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests posting to a DataSource subclass. This should result in a 415
     * error.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPostDataSourceSubclass() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/providers/standard/datasource/subclass/should/fail");
        byte[] barr = new byte[1000];
        Random r = new Random();
        r.nextBytes(barr);
        postMethod.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(barr),
                                                                 "text/plain"));
        postMethod.addRequestHeader("Accept", "text/plain");
        try {
            client.executeMethod(postMethod);

            assertEquals(415, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Verify that we can send a DataSource and receive a DataSource. The 'POST'
     * method on the resource we are calling is a simple echo.
     */
    public void testPOSTDataSource() throws Exception {
        PostMethod postMethod = null;
        try {
            postMethod = new PostMethod(getBaseURI() + "/dstest");
            String input = "This is some test input";
            RequestEntity requestEntity =
                new ByteArrayRequestEntity(input.getBytes(), "application/datasource");
            postMethod.setRequestEntity(requestEntity);
            HttpClient client = new HttpClient();
            client.executeMethod(postMethod);

            // just use our provider to read the response
            DataSourceProvider provider = new DataSourceProvider();
            DataSource returnedData =
                provider.readFrom(DataSource.class,
                                  null,
                                  null,
                                  new MediaType("application", "datasource"),
                                  null,
                                  postMethod.getResponseBodyAsStream());
            assertNotNull(returnedData);
            assertNotNull(returnedData.getInputStream());
            byte[] responseBytes = new byte[input.getBytes().length];
            returnedData.getInputStream().read(responseBytes);
            assertNotNull(responseBytes);
            String response = new String(responseBytes);
            assertEquals("This is some test input", response);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }
}
