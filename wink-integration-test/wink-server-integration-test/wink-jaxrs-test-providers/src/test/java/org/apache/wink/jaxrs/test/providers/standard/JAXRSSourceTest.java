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

package org.apache.wink.jaxrs.test.providers.standard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class JAXRSSourceTest extends TestCase {

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/standard";
    }

    /**
     * Tests posting to a Source entity parameter with text/xml
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPostSourceWithTextXMLMediaType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/providers/standard/source");
        postMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "<message><user>user1</user><password>user1pwd</password></message>",
                                                      "text/xml", "UTF-8"));
        postMethod.addRequestHeader("Accept", "text/xml");
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            String str = postMethod.getResponseBodyAsString();
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><message><user>user1</user><password>user1pwd</password></message>",
                         str);
            assertEquals("text/xml", postMethod.getResponseHeader("Content-Type").getValue());
            Header contentLengthHeader = postMethod.getResponseHeader("Content-Length");
            assertNull(contentLengthHeader == null ? "null" : contentLengthHeader.getValue(),
                       contentLengthHeader);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests posting to a Source entity parameter with application/xml as the
     * media type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPostSourceWithApplicationXMLMediaType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/providers/standard/source");
        postMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "<message><user>user1</user><password>user1pwd</password></message>",
                                                      "application/xml", "UTF-8"));
        postMethod.addRequestHeader("Accept", "application/xml");
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            String str = postMethod.getResponseBodyAsString();
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><message><user>user1</user><password>user1pwd</password></message>",
                         str);
            assertEquals("application/xml", postMethod.getResponseHeader("Content-Type").getValue());
            Header contentLengthHeader = postMethod.getResponseHeader("Content-Length");
            assertNull(contentLengthHeader == null ? "null" : contentLengthHeader.getValue(),
                       contentLengthHeader);
        } finally {
            postMethod.releaseConnection();
        }
    }

    // public void testPostSourceWithApplicationWildcardXMLSubtypeMediaType() {
    // fail();
    // }

    /**
     * Tests posting to a Source entity parameter and returning Source entity
     * response with an unacceptable response media type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPostSourceWithNonExpectedAcceptType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/providers/standard/source");
        postMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "<message><user>user1</user><password>user1pwd</password></message>",
                                                      "application/xml", "UTF-8"));
        postMethod.addRequestHeader("Accept", "not/expected");
        try {
            client.executeMethod(postMethod);

            assertEquals(500, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests posting to a Source entity parameter and returning Source entity
     * response with an unacceptable request content-type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPostSourceWithNonExpectedRequestContentType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/providers/standard/source");
        postMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "<message><user>user1</user><password>user1pwd</password></message>",
                                                      "text/plain", "UTF-8"));
        postMethod.addRequestHeader("Accept", "application/xml");
        try {
            client.executeMethod(postMethod);

            assertEquals(415, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests putting and then getting a source.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPutSource() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PutMethod putMethod = new PutMethod(getBaseURI() + "/providers/standard/source");
        putMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message><user>user1</user><password>user1pwd</password></message>",
                                                      "application/xml", "UTF-8"));
        try {
            client.executeMethod(putMethod);
            assertEquals(204, putMethod.getStatusCode());
        } finally {
            putMethod.releaseConnection();
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/providers/standard/source");
        try {
            client.executeMethod(getMethod);

            String str = getMethod.getResponseBodyAsString();
            assertEquals(str, 200, getMethod.getStatusCode());

            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><message><user>user1</user><password>user1pwd</password></message>",
                         str);

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
     * Tests a resource method invoked with a SAXSource as a parameter. This
     * should fail with a 415 since the reader has no way to necessarily wrap it
     * to the type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testSourceSubclassImplementation() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/providers/standard/source/subclasses/shouldfail");
        byte[] barr = new byte[1000];
        Random r = new Random();
        r.nextBytes(barr);
        postMethod.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(barr),
                                                                 "application/xml"));
        try {
            client.executeMethod(postMethod);
            assertEquals(415, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }
}
