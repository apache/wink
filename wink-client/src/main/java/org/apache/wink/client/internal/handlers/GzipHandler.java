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

package org.apache.wink.client.internal.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.handlers.HandlerContext;
import org.apache.wink.client.handlers.InputStreamAdapter;
import org.apache.wink.client.handlers.OutputStreamAdapter;

/**
 * Provides support for GZip encoding for requests and responses
 */
public class GzipHandler implements ClientHandler {

    public ClientResponse handle(ClientRequest request, HandlerContext context) throws Exception {
        request.getHeaders().add("Accept-Encoding", "gzip"); //$NON-NLS-1$ //$NON-NLS-2$
        context.addInputStreamAdapter(new GzipAdapter());
        context.addOutputStreamAdapter(new GzipAdapter());
        return context.doChain(request);
    }

    private static class GzipAdapter implements InputStreamAdapter, OutputStreamAdapter {

        public OutputStream adapt(OutputStream os, ClientRequest request) throws IOException {
            request.getHeaders().add("Content-Encoding", "gzip"); //$NON-NLS-1$ //$NON-NLS-2$
            return new GZIPOutputStream(os);
        }

        public InputStream adapt(InputStream is, ClientResponse response) throws IOException {
            String header = response.getHeaders().getFirst("Content-Encoding"); //$NON-NLS-1$
            if (header != null && header.equalsIgnoreCase("gzip")) { //$NON-NLS-1$
                return new GZIPInputStream(is);
            }
            return is;
        }

    }
}
