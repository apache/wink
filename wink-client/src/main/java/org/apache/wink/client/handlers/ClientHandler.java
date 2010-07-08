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

import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.ClientRuntimeException;

/**
 * Interface that all client handlers must implement
 */
public interface ClientHandler {

    /**
     * This method is invoked for every request invocation to allow the handler
     * to perform custom actions during the invocation. This method may be
     * called more that once for a single request, so handlers are must be
     * prepared to handle such situations.
     * 
     * @param request a modifiable {@link ClientRequest} containing the request
     *            details
     * @param context the handler context
     * @return a {@link ClientResponse} instance containing the response details
     * @throws Exception any exception can be thrown by a handler and it will be
     *             caught by the underlying client implementation and wrapped in
     *             a {@link ClientRuntimeException}
     */
    ClientResponse handle(ClientRequest request, HandlerContext context) throws Exception;
}
