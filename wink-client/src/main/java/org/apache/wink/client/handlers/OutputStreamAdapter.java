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

package org.apache.wink.client.handlers;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.wink.client.ClientRequest;

/**
 * Interface for adapting the output stream
 */
public interface OutputStreamAdapter {

    /**
     * This method is called in order to wrap the request output stream with
     * another output stream to allow the manipulation of the request entity
     * stream. This method is called before writing the request headers to allow
     * the adapter to manipulate the request.
     * <p>
     * For example:
     * 
     * <pre>
     * public OutputStream adapt(OutputStream os, ClientRequest request) throws IOException {
     *     request.getHeaders().add(&quot;Content-Encoding&quot;, &quot;gzip&quot;);
     *     return new GZIPOutputStream(os);
     * }
     * </pre>
     * 
     * @param os the current request output stream
     * @param request the request
     * @return the adapted request output stream
     * @throws IOException
     */
    OutputStream adapt(OutputStream os, ClientRequest request) throws IOException;
}
