/*******************************************************************************
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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.wink.client;

import org.apache.http.client.HttpClient;
import org.apache.wink.client.handlers.ConnectionHandler;
import org.apache.wink.client.internal.handlers.httpclient.ApacheHttpClientConnectionHandler;

/**
 * Configuration object that wraps Apache HttpClient as underling Http client.
 * The following code snippet, demonstrates the typical usage:
 * <p>
 * 
 * <pre>
 *      // create the client that uses Apache DefaultHttpClient as underling Http client. 
 *      RestClient client = new RestClient(new ApacheHttpClientConfig(new DefaultHttpClient()));
 *      
 *      // create the resource to make invocations on
 *      Resource resource = client.resource(&quot;http://myhost:80/my/service&quot;);
 *      
 *      // invoke GET on the resource and receive the response entity as a string
 *      String entity = resource.get(String.class);
 *      ...
 * </pre>
 * 
 * </p>
 * Move to org.apache.wink.client.httpclient to avoid OSGi split package issues
 * @deprecated
 * 
 */
@Deprecated
public class ApacheHttpClientConfig extends org.apache.wink.client.httpclient.ApacheHttpClientConfig {

    public ApacheHttpClientConfig() {
        super();
    }

    public ApacheHttpClientConfig(HttpClient client) {
        super(client);
    }
}
