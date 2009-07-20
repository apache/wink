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
import java.io.InputStream;

import org.apache.wink.client.ClientResponse;

/**
 * Interface for adapting the input stream
 */
public interface InputStreamAdapter {

    /**
     * This method is called in order to wrap the response input stream with
     * another input stream to allow the manipulation of the response entity
     * stream. This method is called after reading the response status code and
     * response headers, and before returning to the ClientResponse to the
     * handlers on the chain.
     * <p>
     * For example:
     * 
     * <pre>
     * public InputStream adapt(InputStream is, ClientResponse response) throws IOException {
     *     String header = response.getHeaders().getFirst(&quot;Content-Encoding&quot;);
     *     if (header != null &amp;&amp; header.equalsIgnoreCase(&quot;gzip&quot;)) {
     *         return new GZIPInputStream(is);
     *     }
     *     return is;
     * }
     * </pre>
     * 
     * @param is the current response input stream
     * @param response the response
     * @return the adapted response input stream
     * @throws IOException
     */
    InputStream adapt(InputStream is, ClientResponse response) throws IOException;
}
