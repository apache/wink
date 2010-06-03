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
package org.apache.wink.client.internal.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.handlers.HandlerContext;
import org.apache.wink.client.handlers.InputStreamAdapter;
import org.apache.wink.client.handlers.OutputStreamAdapter;

public class DeflateHandler implements ClientHandler {

    public ClientResponse handle(ClientRequest request, HandlerContext context) throws Exception {
        request.getHeaders().add("Accept-Encoding", "deflate"); //$NON-NLS-1$ //$NON-NLS-2$
        if (request.getEntity() != null) {
            request.getHeaders().add("Content-Encoding", "deflate"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        context.addInputStreamAdapter(new DeflateAdapter());
        context.addOutputStreamAdapter(new DeflateAdapter());
        return context.doChain(request);
    }

    private static class DeflateAdapter implements InputStreamAdapter, OutputStreamAdapter {

        public OutputStream adapt(OutputStream os, ClientRequest request) throws IOException {
            return new DeflaterOutputStream(os);
        }

        public InputStream adapt(InputStream is, ClientResponse response) throws IOException {
            String header = response.getHeaders().getFirst("Content-Encoding"); //$NON-NLS-1$
            if (header != null && header.equalsIgnoreCase("deflate")) { //$NON-NLS-1$
                return new InflaterInputStream(is);
            }
            return is;
        }
    }
}
