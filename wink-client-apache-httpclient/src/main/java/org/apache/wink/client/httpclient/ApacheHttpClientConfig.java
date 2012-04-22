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

package org.apache.wink.client.httpclient;

import org.apache.http.client.HttpClient;
import org.apache.wink.client.ClientConfig;
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
 */
public class ApacheHttpClientConfig extends ClientConfig {

    protected HttpClient client;
    protected int maxPooledConnections;
    protected boolean chunked = true;

    public ApacheHttpClientConfig() {
        client = null;
    }

    public ApacheHttpClientConfig(HttpClient client) {
        this.client = client;
    }

    @Override
    protected ConnectionHandler getConnectionHandler() {
        return new ApacheHttpClientConnectionHandler(client);
    }

    public void setMaxPooledConnections(int maxPooledConnections) {
        this.maxPooledConnections = maxPooledConnections;
    }

    public int getMaxPooledConnections() {
        return maxPooledConnections;
    }

    public boolean isChunked() {
        return chunked;
    }

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
    }
}
