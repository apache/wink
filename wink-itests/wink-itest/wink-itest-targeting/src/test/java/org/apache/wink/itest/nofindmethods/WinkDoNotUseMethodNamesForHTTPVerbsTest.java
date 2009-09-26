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
package org.apache.wink.itest.nofindmethods;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpException;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkDoNotUseMethodNamesForHTTPVerbsTest extends TestCase {

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/nofindmethods";
    }

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Negative tests that method names that begin with HTTP verbs are not
     * invoked on a root resource.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testMethodsNotValid() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/nousemethodnamesforhttpverbs/someresource").post(null);
        assertEquals(405, response.getStatusCode());

        response =
            client.resource(getBaseURI() + "/nousemethodnamesforhttpverbs/someresource").get();
        assertEquals(405, response.getStatusCode());

        response =
            client.resource(getBaseURI() + "/nousemethodnamesforhttpverbs/someresource").put(null);
        assertEquals(405, response.getStatusCode());

        response =
            client.resource(getBaseURI() + "/nousemethodnamesforhttpverbs/someresource").delete();
        assertEquals(405, response.getStatusCode());

        response =
            client.resource(getBaseURI() + "/nousemethodnamesforhttpverbs/counter/root").get();
        assertEquals(200, response.getStatusCode());
        assertEquals("0", response.getEntity(String.class));
    }

    /**
     * Negative tests that method names that begin with HTTP verbs are not
     * invoked on a sublocator method.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testSublocatorMethodsNotValid() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/nousemethodnamesforhttpverbs/sublocatorresource/sub")
                .post(null);
        assertEquals(405, response.getStatusCode());

        response =
            client.resource(getBaseURI() + "/nousemethodnamesforhttpverbs/sublocatorresource/sub")
                .get();
        assertEquals(405, response.getStatusCode());

        response =
            client.resource(getBaseURI() + "/nousemethodnamesforhttpverbs/sublocatorresource/sub")
                .put(null);
        assertEquals(405, response.getStatusCode());

        response =
            client.resource(getBaseURI() + "/nousemethodnamesforhttpverbs/sublocatorresource/sub")
                .delete();
        assertEquals(405, response.getStatusCode());

        response =
            client.resource(getBaseURI() + "/nousemethodnamesforhttpverbs/counter/sublocator")
                .get();
        assertEquals(200, response.getStatusCode());
        assertEquals("0", response.getEntity(String.class));
    }
}
