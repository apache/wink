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
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.RestClient;
import org.apache.wink.providers.json.JsonProvider;

/**
 * Tests the Accept Header Handler. Repeats the {@link AcceptHeaderHandlerTest}
 * but with the Apache HTTP core client instead.
 */
public class AcceptHeaderHandlerApacheHTTPCoreTest extends AcceptHeaderHandlerTest {

    public void setUp() throws Exception {
        super.setUp();
        ClientConfig config = new ApacheHttpClientConfig();
//        config.setLoadWinkApplications(false);
        config.applications(new Application() {

            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> classes = new HashSet<Class<?>>();
                classes.add(JsonProvider.class);
                return classes;
            }

        });
        client = new RestClient(config);
    }

}
