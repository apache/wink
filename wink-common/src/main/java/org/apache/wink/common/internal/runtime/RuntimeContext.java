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

package org.apache.wink.common.internal.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

public interface RuntimeContext {

    /**
     * Get the {@link Providers} context
     * 
     * @return the {@link Providers} context
     */
    public Providers getProviders();

    /**
     * Get the {@link HttpHeaders} context
     * 
     * @return the {@link HttpHeaders} context
     */
    public HttpHeaders getHttpHeaders();

    /**
     * Get the {@link UriInfo} context
     * 
     * @return the {@link UriInfo} context
     */
    public UriInfo getUriInfo();

    /**
     * Get the {@link SecurityContext} context
     * 
     * @return the {@link SecurityContext} context
     */
    public SecurityContext getSecurityContext();

    /**
     * Get the {@link Request} context
     * 
     * @return the {@link Request} context
     */
    public Request getRequest();

    /**
     * Get the input stream of the request
     * 
     * @return the request input stream
     */
    public InputStream getInputStream() throws IOException;

    /**
     * Get the output stream of the response
     * 
     * @return the response output stream
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * Get an attributes map to store objects in the context of a request.
     * 
     * @return a live map of attributes
     */
    public Map<String, Object> getAttributes();

    /**
     * Convenience method for setting an attribute whose key that is the fully
     * qualified name of a class
     * 
     * @param <T> attribute type
     * @param type the class of the key whose fully qualified name is used as
     *            the key
     * @param object the object to set
     */
    public <T> void setAttribute(Class<T> type, T object);

    /**
     * Convenience method for getting an attribute whose key that is the fully
     * qualified name of a class
     * 
     * @param <T> attribute type
     * @param type he class of the key whose fully qualified name is used as the
     *            key
     * @return the attribute or null
     */
    public <T> T getAttribute(Class<T> type);

}
