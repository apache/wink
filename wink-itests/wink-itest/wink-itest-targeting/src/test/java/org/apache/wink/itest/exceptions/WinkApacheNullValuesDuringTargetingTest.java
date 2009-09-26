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

import org.apache.wink.client.ApacheHttpClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;

public class WinkApacheNullValuesDuringTargetingTest extends WinkNullValuesDuringTargetingTest {

    @Override
    public void setUp() {
        client = new RestClient(new ApacheHttpClientConfig());
    }

    /**
     * Tests that a request to a method with no content type, a request entity,
     * but without a {@link Consumes} method results in 200 successful method
     * invocation.
     * 
     * @throws IOException
     */
    @Override
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
        assertEquals("userReadercalledWithString", response.getEntity(String.class));
        String contentType =
            (response.getHeaders().getFirst("Content-Type") == null) ? null : response.getHeaders()
                .getFirst("Content-Type");
        assertNotNull(contentType, contentType);
    }

}
