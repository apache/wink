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

package org.apache.wink.itest.pojo.lifecycle;

import static org.junit.Assert.assertEquals;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;
import org.junit.Before;
import org.junit.Test;

public class POJOLifecycleTest {

    private RestClient client;

    private String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI();
    }

    @Before
    public void setUp() {
        client = new RestClient();
        client.resource(getBaseURI() + "/messageaccess").post(null);
    }

    /**
     * Test that the post-construct method is called after the constructor but
     * before the GET method for a POJO resource. Also test that the pre-destroy
     * method is called after the request is processed, and before the next
     * request. Combining these into one test.
     */
    @Test
    public void testPostConstructAndPreDestroy() {
        Resource resource = client.resource(getBaseURI() + "/pojo/message");
        ClientResponse response = resource.get();
        assertEquals(204, response.getStatusCode());

        resource = client.resource(getBaseURI() + "/messageaccess");
        response = resource.get();
        assertEquals(200, response.getStatusCode());
        assertEquals("MyPOJO;myPostConstructMethod;message;myPreDestroyMethod;", response
            .getEntity(String.class));

        resource = client.resource(getBaseURI() + "/pojo/message");
        response = resource.get();
        assertEquals(204, response.getStatusCode());

        resource = client.resource(getBaseURI() + "/messageaccess");
        response = resource.get();
        assertEquals(200, response.getStatusCode());
        assertEquals("MyPOJO;myPostConstructMethod;message;myPreDestroyMethod;MyPOJO;myPostConstructMethod;message;myPreDestroyMethod;",
                     response.getEntity(String.class));
    }

    /**
     * Test things are called correctly in the Exception path.
     */
    @Test
    public void testPostConstructAndPreDestroyWithException() {
        Resource resource = client.resource(getBaseURI() + "/pojo/exception");
        ClientResponse response = resource.get();
        assertEquals(500, response.getStatusCode());

        resource = client.resource(getBaseURI() + "/messageaccess");
        response = resource.get();
        assertEquals(200, response.getStatusCode());
        assertEquals("MyPOJO;myPostConstructMethod;myPreDestroyMethod;", response
            .getEntity(String.class));
    }
}
