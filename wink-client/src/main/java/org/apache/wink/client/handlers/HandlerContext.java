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

import java.util.List;

import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;

/**
 * The handler context is used by handlers to call the next handler on the
 * chain. It is also used by handlers to modify the input and output streams for
 * stream manipulations by using the {@link InputStreamAdapter} and the
 * {@link OutputStreamAdapter} classes.
 */
public interface HandlerContext {

    /**
     * Call the next handler on the chain. A handler is permitted to call this
     * method any number of times for the same request.
     * 
     * @param request the request context
     * @return the response context
     * @throws Exception
     */
    ClientResponse doChain(ClientRequest request) throws Exception;

    /**
     * Add an {@link OutputStreamAdapter} to adapt the request output stream
     * 
     * @param adapter the OutputStreamAdapeter to add
     */
    void addOutputStreamAdapter(OutputStreamAdapter adapter);

    /**
     * Add an {@link InputStreamAdapter} to adapt the response input stream
     * 
     * @param adapter the InputStreamAdapeter to add
     */
    void addInputStreamAdapter(InputStreamAdapter adapter);

    /**
     * Get an unmodifiable list of output stream adapters
     * 
     * @return unmodifiable list of output stream adapters
     */
    List<OutputStreamAdapter> getOutputStreamAdapters();

    /**
     * Get an unmodifiable list of input stream adapters
     * 
     * @return unmodifiable list of input stream adapters
     */
    List<InputStreamAdapter> getInputStreamAdapters();
}
