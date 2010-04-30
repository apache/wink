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
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.ClientWebException;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerContainerAssertions;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkNullValuesDuringTargetingTest extends TestCase {

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/exceptional";
    }

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Tests that a request to a method with no content type, no request entity,
     * but with a {@link Consumes} method results in a 415 error.
     * 
     * @throws IOException
     */
    public void testNoContentTypeWithNoRequestEntityIncomingRequestWithConsumesMethod()
        throws IOException {
        try {
            client.resource(getBaseURI() + "/targeting/nullresource/withconsumes")
                .post(String.class, null);
            fail();
        } catch (ClientWebException e) {
            assertEquals(415, e.getResponse().getStatusCode());
            String responseBody = e.getResponse().getEntity(String.class);
            ServerContainerAssertions.assertExceptionBodyFromServer(415, responseBody);
            if (responseBody == null || "".equals(responseBody)) {
                assertNull(e.getResponse().getHeaders().getFirst("Content-Type"));
            }
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
        try {
            client.resource(getBaseURI() + "/targeting/nullresource/withconsumes")
                .post(String.class, new byte[] {0, 1, 2});
            fail();
        } catch (ClientWebException e) {
            assertEquals(415, e.getResponse().getStatusCode());
            String responseBody = e.getResponse().getEntity(String.class);
            ServerContainerAssertions.assertExceptionBodyFromServer(415, responseBody);
            if (responseBody == null || "".equals(responseBody)) {
                assertNull(e.getResponse().getHeaders().getFirst("Content-Type"));
            }
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
        /*
         * with Wink client, content type is set to applcation/octet-stream if
         * no content type specified
         */
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/nullresource/withoutconsumes")
                .post("calledWithString");
        assertEquals(200, response.getStatusCode());
        assertEquals("calledWithString", response.getEntity(String.class));
        String contentType =
            (response.getHeaders().getFirst("Content-Type") == null) ? null : response.getHeaders()
                .getFirst("Content-Type");
        assertNotNull(contentType, contentType);
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
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/nullresource/withoutconsumes")
                .contentType("custom/type").post("myString");
        assertEquals(200, response.getStatusCode());
        assertEquals("myString", response.getEntity(String.class));
        String contentType =
            (response.getHeaders().getFirst("Content-Type") == null) ? null : response.getHeaders()
                .getFirst("Content-Type");
        assertNotNull(contentType, contentType);
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
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/nullresource/withoutconsumes")
                .contentType("text/plain").post(null);
        assertEquals(200, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
        String contentType =
            (response.getHeaders().getFirst("Content-Type") == null) ? null : response.getHeaders()
                .getFirst("Content-Type");
        assertNotNull(contentType, contentType);
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
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/nullresource/withoutconsumes")
                .contentType(MediaType.APPLICATION_OCTET_STREAM).post(new String());

        assertEquals(200, response.getStatusCode());
        assertEquals("userReader", response.getEntity(String.class));
        String contentType =
            (response.getHeaders().getFirst("Content-Type") == null) ? null : response.getHeaders()
                .getFirst("Content-Type");
        assertNotNull(contentType, contentType);
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
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/nullresource/withconsumes")
                .contentType("text/plain").post(null);
        assertEquals(200, response.getStatusCode());
        assertEquals("", response.getEntity(String.class));
        String contentType =
            (response.getHeaders().getFirst("Content-Type") == null) ? null : response.getHeaders()
                .getFirst("Content-Type");
        assertNotNull(contentType, contentType);
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
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/nullresource/withconsumes")
                .contentType("text/plain").post("mystring");
        assertEquals(200, response.getStatusCode());
        assertEquals("mystring", response.getEntity(String.class));
        String contentType =
            (response.getHeaders().getFirst("Content-Type") == null) ? null : response.getHeaders()
                .getFirst("Content-Type");
        assertNotNull(contentType, contentType);
    }

    /**
     * Tests that a request to a method with an Accept header with a
     * {@link Produces} method results in 200 successful method invocation.
     * 
     * @throws IOException
     */
    public void testAcceptHeaderIncomingRequestWithProducesMethod() throws IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/nullresource/withproduces")
                .accept("custom/type; q=0.8").post(null);
        assertEquals(200, response.getStatusCode());
        assertEquals("calledWithProduces", response.getEntity(String.class));
        assertEquals(MediaType.valueOf("custom/type;q=0.8"), MediaType
            .valueOf(response.getHeaders().getFirst("Content-Type")));
    }

    /**
     * Tests that a request to a method with an Accept header with no
     * {@link Produces} method results in 200 successful method invocation.
     * 
     * @throws IOException
     */
    public void testAcceptHeaderIncomingRequestWithNoProducesMethod() throws IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/nullresource/withoutproduces")
                .accept("custom/type2; q=0.8").post(null);
        assertEquals(200, response.getStatusCode());
        assertEquals("calledWithoutProduces", response.getEntity(String.class));
        assertEquals(MediaType.valueOf("custom/type2;q=0.8"), MediaType
            .valueOf(response.getHeaders().getFirst("Content-Type")));
    }

    /**
     * Tests that a request to a method with no Accept header with a
     * {@link Produces} method results in 200 successful method invocation.
     * 
     * @throws IOException
     */
    public void testNoAcceptHeaderIncomingRequestWithProducesMethod() throws IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/nullresource/withproduces").post(null);
        assertEquals(200, response.getStatusCode());
        assertEquals("calledWithProduces", response.getEntity(String.class));
        assertEquals("custom/type", response.getHeaders()
            .getFirst("Content-Type"));
    }

    /**
     * Tests that a request to a method with no Accept header with no
     * {@link Produces} method results in 200 successful method invocation.
     * 
     * @throws IOException
     */
    public void testNoAcceptHeaderIncomingRequestWithNoProducesMethod() throws IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/targeting/nullresource/withoutproduces").post(null);
        assertEquals(200, response.getStatusCode());
        assertEquals("calledWithoutProduces", response.getEntity(String.class));
        String contentType =
            (response.getHeaders().getFirst("Content-Type") == null) ? null : response.getHeaders()
                .getFirst("Content-Type");
        assertNotNull(contentType, contentType);
    }
}
