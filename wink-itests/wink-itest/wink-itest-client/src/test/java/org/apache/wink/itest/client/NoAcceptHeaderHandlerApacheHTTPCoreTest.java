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
package org.apache.wink.itest.client;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.wink.client.ApacheHttpClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.internal.handlers.AcceptHeaderHandler;
import org.apache.wink.providers.json.internal.JsonProvider;

public class NoAcceptHeaderHandlerApacheHTTPCoreTest extends NoAcceptHeaderHandlerTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client =
            new RestClient(new ApacheHttpClientConfig().acceptHeaderAutoSet(false)
                .applications(new Application() {

                    @Override
                    public Set<Class<?>> getClasses() {
                        Set<Class<?>> classes = new HashSet<Class<?>>();
                        classes.add(JsonProvider.class);
                        return classes;
                    }

                }));
    }

    /**
     * If the Accept header is not set, then let the client set a default to
     * send. In RestClient with Apache HTTP core, it is set to nothing.
     */
    @Override
    public void testAcceptHeaderNotSetString() {
        String s = client.resource(getBaseURI() + "/echoaccept").get(String.class);
        assertEquals("echo: ", s);
    }

    /**
     * If no entity class is specified in the initial GET, then the
     * {@link AcceptHeaderHandler} should not set anything. However, the
     * underlying client may set the header as a failsafe.
     */
    @Override
    public void testAcceptHeaderNoEntity() {
        ClientResponse resp = client.resource(getBaseURI() + "/echoaccept").get();
        /*
         * in this case the underlying client does not set an Accept header at
         * all
         */
        assertEquals("echo: ", resp.getEntity(String.class));
    }

}
