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

package org.apache.wink.itest.exceptions;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.wink.test.integration.ServerContainerAssertions;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class NullValuesDuringTargettingTest extends TestCase {

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/exceptional";
    }

    /**
     * Tests that a request to a method with no content type, no request entity,
     * but with a {@link Consumes} method results in a 415 error.
     * 
     * @throws IOException
     */
    public void testNoContentTypeWithNoRequestEntityIncomingRequestWithConsumesMethod()
        throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/targeting/nullresource/withconsumes");
        try {
            client.executeMethod(postMethod);
            assertEquals(415, postMethod.getStatusCode());
            String responseBody = postMethod.getResponseBodyAsString();
            ServerContainerAssertions.assertExceptionBodyFromServer(415, responseBody);
            if (responseBody == null || "".equals(responseBody)) {
                assertNull(postMethod.getResponseHeader("Content-Type"));
            }
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a request to a method with no content type, a request entity,
     * but with a {@link Consumes} method results in a 415 error.
     * 
     * @throws IOException
     */
    public void testNoContentTypeWithRequestEntityIncomingRequestWithConsumesMethod()
        throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/targeting/nullresource/withconsumes");
        postMethod.setRequestEntity(new ByteArrayRequestEntity(new byte[] {0, 1, 2}));
        try {
            client.executeMethod(postMethod);
            assertEquals(415, postMethod.getStatusCode());
            String responseBody = postMethod.getResponseBodyAsString();
            ServerContainerAssertions.assertExceptionBodyFromServer(415, responseBody);
            if (responseBody == null || "".equals(responseBody)) {
                assertNull(postMethod.getResponseHeader("Content-Type"));
            }
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a request to a method with no content type, a request entity,
     * but without a {@link Consumes} method results in 200 successful method
     * invocation.
     * 
     * @throws IOException
     */
    public void testNoContentTypeWithRequestEntityIncomingRequestWithNoConsumesMethod()
        throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/targeting/nullresource/withoutconsumes");
        postMethod.setRequestEntity(new ByteArrayRequestEntity("calledWithString".getBytes()));
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("userReadercalledWithString", postMethod.getResponseBodyAsString());
            String contentType =
                (postMethod.getResponseHeader("Content-Type") == null) ? null : postMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a request to a method with a content type, a request entity,
     * but without a {@link Consumes} method results in 200 successful method
     * invocation.
     * 
     * @throws IOException
     */
    public void testContentTypeWithRequestEntityIncomingRequestWithNoConsumesMethod()
        throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/targeting/nullresource/withoutconsumes");
        postMethod
            .setRequestEntity(new ByteArrayRequestEntity("myString".getBytes(), "custom/type"));
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("myString", postMethod.getResponseBodyAsString());
            String contentType =
                (postMethod.getResponseHeader("Content-Type") == null) ? null : postMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a request to a method with a content type, no request entity,
     * but without a {@link Consumes} method results in 200 successful method
     * invocation.
     * 
     * @throws IOException
     */
    public void testContentTypeWithNoRequestEntityIncomingRequestWithNoConsumesMethod()
        throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/targeting/nullresource/withoutconsumes");
        postMethod.setRequestHeader("Content-Type", "text/plain");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("", postMethod.getResponseBodyAsString());
            String contentType =
                (postMethod.getResponseHeader("Content-Type") == null) ? null : postMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a request to a method with no content type, no request entity,
     * but without a {@link Consumes} method results in 200 successful method
     * invocation.
     * 
     * @throws IOException
     */
    public void testNoContentTypeWithNoRequestEntityIncomingRequestWithNoConsumesMethod()
        throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/targeting/nullresource/withoutconsumes");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("userReader", postMethod.getResponseBodyAsString());
            String contentType =
                (postMethod.getResponseHeader("Content-Type") == null) ? null : postMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a request to a method with a content type, no request entity,
     * but with a {@link Consumes} method results in 200 successful method
     * invocation.
     * 
     * @throws IOException
     */
    public void testContentTypeWithNoRequestEntityIncomingRequestWithConsumesMethod()
        throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/targeting/nullresource/withconsumes");
        postMethod.setRequestHeader("Content-Type", "text/plain");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("", postMethod.getResponseBodyAsString());
            String contentType =
                (postMethod.getResponseHeader("Content-Type") == null) ? null : postMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a request to a method with a content type, a request entity,
     * but with a {@link Consumes} method results in 200 successful method
     * invocation.
     * 
     * @throws IOException
     */
    public void testContentTypeWithRequestEntityIncomingRequestWithConsumesMethod()
        throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/targeting/nullresource/withconsumes");
        postMethod
            .setRequestEntity(new ByteArrayRequestEntity("mystring".getBytes(), "text/plain"));
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("mystring", postMethod.getResponseBodyAsString());
            String contentType =
                (postMethod.getResponseHeader("Content-Type") == null) ? null : postMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a request to a method with an Accept header with a
     * {@link Produces} method results in 200 successful method invocation.
     * 
     * @throws IOException
     */
    public void testAcceptHeaderIncomingRequestWithProducesMethod() throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/targeting/nullresource/withproduces");
        postMethod.setRequestHeader("Accept", "custom/type; q=0.8");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("calledWithProduces", postMethod.getResponseBodyAsString());
            assertEquals("custom/type;q=0.8", postMethod.getResponseHeader("Content-Type")
                .getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a request to a method with an Accept header with no
     * {@link Produces} method results in 200 successful method invocation.
     * 
     * @throws IOException
     */
    public void testAcceptHeaderIncomingRequestWithNoProducesMethod() throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/targeting/nullresource/withoutproduces");
        postMethod.setRequestHeader("Accept", "custom/type2; q=0.8");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("calledWithoutProduces", postMethod.getResponseBodyAsString());
            assertEquals("custom/type2;q=0.8", postMethod.getResponseHeader("Content-Type")
                .getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a request to a method with no Accept header with a
     * {@link Produces} method results in 200 successful method invocation.
     * 
     * @throws IOException
     */
    public void testNoAcceptHeaderIncomingRequestWithProducesMethod() throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/targeting/nullresource/withproduces");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("calledWithProduces", postMethod.getResponseBodyAsString());
            assertEquals("custom/type", postMethod.getResponseHeader("Content-Type").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a request to a method with no Accept header with no
     * {@link Produces} method results in 200 successful method invocation.
     * 
     * @throws IOException
     */
    public void testNoAcceptHeaderIncomingRequestWithNoProducesMethod() throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/targeting/nullresource/withoutproduces");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("calledWithoutProduces", postMethod.getResponseBodyAsString());
            String contentType =
                (postMethod.getResponseHeader("Content-Type") == null) ? null : postMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
        } finally {
            postMethod.releaseConnection();
        }
    }
}
