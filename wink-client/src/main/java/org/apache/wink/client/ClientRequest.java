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

import java.net.URI;

import org.apache.wink.client.internal.BaseRequestResponse;

/**
 * Represents a request issued by invoking any one of the invocation methods on
 * a {@link Resource}. An instance of a ClientRequest is created at the
 * beginning of an invocation and passed to all the client handlers defined on
 * the client that was used for the invocation.
 */
public interface ClientRequest extends BaseRequestResponse {
    /**
     * Get the http method
     * 
     * @return http method
     */
    String getMethod();

    /**
     * Set the http method
     * 
     * @param method http method to set
     */
    void setMethod(String method);

    /**
     * Get the request entity to send with the request.
     * 
     * @return the request entity instance
     */
    Object getEntity();

    /**
     * Set the request entity to send with the request. The entity may be any
     * object that has a corresponding Provider that can handle it with the
     * content type specified in the Content-Type header.
     * 
     * @return the request entity instance
     */
    void setEntity(Object entity);

    /**
     * Get the uri of the request
     * 
     * @return the uri of the request
     */
    URI getURI();

    /**
     * Set the uri of the request
     * 
     * @param uri uri to set
     */
    void setURI(URI uri);
}
