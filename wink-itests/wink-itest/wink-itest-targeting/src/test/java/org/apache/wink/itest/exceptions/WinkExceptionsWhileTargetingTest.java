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

import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.wink.client.ClientWebException;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerContainerAssertions;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkExceptionsWhileTargetingTest extends TestCase {

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
     * Tests that a 404 error is thrown when no resource can be found for a
     * path.
     * 
     * @throws Exception
     */
    public void test404WhenNoResourceExists() throws Exception {
        try {
            String response = client.resource(getBaseURI() + "/doesnotexist").get(String.class);
            fail(response);
        } catch (ClientWebException e) {
            assertEquals(404, e.getResponse().getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, e.getResponse()
                .getEntity(String.class));
        }
    }

    /**
     * Tests that a 405 error is thrown when no subresource can be found for a
     * path.
     * 
     * @throws Exception
     */
    public void test405WhenNoMethodExistsOnExistingResource() throws Exception {
        try {
            String response =
                client.resource(getBaseURI() + "/existingresource/").get(String.class);
            fail(response);
        } catch (ClientWebException e) {
            assertEquals(405, e.getResponse().getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(405, e.getResponse()
                .getEntity(String.class));
        }
    }

    /**
     * Tests that a 404 error is thrown when no subresource can be found for a
     * path.
     * 
     * @throws Exception
     */
    public void test404WhenNoSubResourceExists() throws Exception {
        try {
            String response =
                client.resource(getBaseURI() + "/existingresource/noexistsub").get(String.class);
            fail(response);
        } catch (ClientWebException e) {
            assertEquals(404, e.getResponse().getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, e.getResponse()
                .getEntity(String.class));
        }

        String response =
            client.resource(getBaseURI() + "/targeting/resourcewithmethod").get(String.class);
        assertEquals("Hello", response);

        try {
            response =
                client.resource(getBaseURI() + "/targeting/resourcewithmethod/noexistsub")
                    .get(String.class);
            fail(response);
        } catch (ClientWebException e) {
            assertEquals(404, e.getResponse().getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, e.getResponse()
                .getEntity(String.class));
        }
    }

    /**
     * Tests that a 405 error is thrown when other http methods exist on a
     * resource but not the one looking for.
     * 
     * @throws Exception
     */
    public void test405WhenResourceMethodDoesNotExistButOthersDo() throws Exception {
        try {
            String response =
                client.resource(getBaseURI() + "/existingresource/noexistsub").get(String.class);
            fail(response);
        } catch (ClientWebException e) {
            assertEquals(404, e.getResponse().getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(404, e.getResponse()
                .getEntity(String.class));
        }
        try {
            String response =
                client.resource(getBaseURI() + "/targeting/resourcewithmethod").post(String.class, null);
            fail(response);
        } catch (ClientWebException e) {
            assertEquals(405, e.getResponse().getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(405, e.getResponse()
                .getEntity(String.class));
        }
    }

    /**
     * Tests that a 415 error is thrown when request entity data sent is not
     * acceptable by the resource.
     * 
     * @throws Exception
     */
    public void test415WhenResourceMethodDoesNotAcceptRequestEntity() throws Exception {
        String response =
            client.resource(getBaseURI() + "/targeting/resourcewithmethod")
                .contentType(MediaType.TEXT_PLAIN).put(String.class, "some content");
        assertEquals("some content", response);

        try {
            response =
                client.resource(getBaseURI() + "/targeting/resourcewithmethod")
                    .contentType("customplain/something").put(String.class, "some content");
            fail(response);
        } catch (ClientWebException e) {
            assertEquals(415, e.getResponse().getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(415, e.getResponse()
                .getEntity(String.class));
        }
    }

    /**
     * Tests that a 406 error is produced if server side cannot produce any
     * acceptable content type.
     * 
     * @throws Exception
     */
    public void test406WhenResourceMethodDoesNotProduceResponseEntityType() throws Exception {
        String response =
            client.resource(getBaseURI() + "/targeting/resourcewithmethod")
                .contentType(MediaType.TEXT_PLAIN).accept(MediaType.TEXT_PLAIN).put(String.class,
                                                                                    "some content");
        assertEquals("some content", response);

        try {
            response =
                client.resource(getBaseURI() + "/targeting/resourcewithmethod")
                    .contentType("text/plain").accept("text/customplain").put(String.class,
                                                                              "some content");
            fail(response);
        } catch (ClientWebException e) {
            assertEquals(406, e.getResponse().getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(406, e.getResponse()
                .getEntity(String.class));
        }
    }
}
