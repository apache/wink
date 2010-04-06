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

import javax.ws.rs.core.Response.StatusType;

import org.apache.wink.client.internal.BaseRequestResponse;

/**
 * Represents an http response that was received after invoking any one of the
 * invocation methods on a {@link Resource}. An instance of a ClientResponse is
 * created by the {@link ConnectionHandler} at the end of the handler chain, and
 * is returned from every handler on the chain.
 */
public interface ClientResponse extends BaseRequestResponse {

    /**
     * Gets the response status as a status type
     * 
     * @return the response status
     */
    public StatusType getStatusType();

    /**
     * Get the response status code
     * 
     * @return response status code
     */
    public int getStatusCode();

    /**
     * Set the response status code
     * 
     * @param code status code to set
     */
    public void setStatusCode(int code);

    /**
     * Get the response message
     * 
     * @return response message
     */
    public String getMessage();

    /**
     * Set the response message
     * 
     * @param message response message to set
     */
    public void setMessage(String message);

    /**
     * Get the response entity.
     * <p>
     * If the requested type to return is InputStream, then the input stream of
     * the response is returned, and the entity is not read using the providers.
     * The returned input stream is the adapted input stream as created by the
     * InputStream adapters. If the InputStream is read directly, then it will
     * not be possible to receive the entity as any other type other than
     * InputStream.
     * <p>
     * If the requested type to return is anything other than InputStream, then
     * the entity is read using the appropriate provider before returning it.
     * Subsequent calls to getEntity will return the same instance of the
     * entity.
     * 
     * @param <T> type of the response entity to get
     * @param cls class of the response entity to get
     * @return the response entity
     */
    public <T> T getEntity(Class<T> cls);

    /**
     * Get the response entity.
     * <p>
     * If the requested type to return is InputStream, then the input stream of
     * the response is returned, and the entity is not read using the providers.
     * The returned input stream is the adapted input stream as created by the
     * InputStream adapters. If the InputStream is read directly, then it will
     * not be possible to receive the entity as any other type other than
     * InputStream.
     * <p>
     * If the requested type to return is anything other than InputStream, then
     * the entity is read using the appropriate provider before returning it.
     * Subsequent calls to getEntity will return the same instance of the
     * entity.
     * 
     * @param <T> type of the response entity to get
     * @param entityType an instance of {@link EntityType} specifying the type
     *            of the entity
     * @return
     */
    public <T> T getEntity(EntityType<T> entityType);

    /**
     * Set the response entity
     * 
     * @param entity response entity to set
     */
    public void setEntity(Object entity);

    /**
     * Consumes entity content. The real behavior of this method depends on the
     * actual implementation. It's needed to call this method, if the calling
     * code decides not to handle content.
     * <p>
     * There is no need to call this method, if getEntity() was invoked.
     * <p>
     * Calling this method multiple times will not cause an error.
     */
    public void consumeContent();
}
