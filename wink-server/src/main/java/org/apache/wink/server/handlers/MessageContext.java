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

package org.apache.wink.server.handlers;

import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.runtime.RuntimeContext;
import org.apache.wink.server.utils.LinkBuilders;

/**
 * The MessageContext is used by Handlers to obtain and manipulate request
 * specific information.
 * 
 * @See {@link RequestHandler}, {@link ResponseHandler}
 */
public interface MessageContext extends RuntimeContext {

    /**
     * Get the initialization properties
     * 
     * @return initialization properties
     */
    public Properties getProperties();

    /**
     * Set the response status code
     * 
     * @param responseStatusCode response status code
     */
    public void setResponseStatusCode(int responseStatusCode);

    /**
     * Get the response status code that was previously set
     * 
     * @return response status code or -1 if it was not set
     */
    public int getResponseStatusCode();

    /**
     * Set the response entity.
     * <p>
     * This may be
     * <ul>
     * <li>the return value from the invoked resource method (including a
     * {@link javax.ws.rs.core.Response})</li>
     * <li>the thrown exception in case of the error handler chain being invoked
     * </li>
     * </ul>
     * 
     * @param entity the response entity
     */
    public void setResponseEntity(Object entity);

    /**
     * Get the response entity.
     * 
     * @return the response entity
     */
    public Object getResponseEntity();

    /**
     * Set the response media type
     * 
     * @param responseMediaType response media type to set
     */
    public void setResponseMediaType(MediaType responseMediaType);

    /**
     * Get the previously set response media type
     * 
     * @return previously set response media type
     */
    public MediaType getResponseMediaType();

    /**
     * Set the http method of the request.
     * <p>
     * Enables overriding of the actual http method that was used for the
     * request
     * 
     * @param method the request http method to set
     */
    public void setHttpMethod(String method);

    /**
     * Get the http method of the request.
     * <p>
     * Note that this may be different than the real http method on the
     * HttpServletRequest if one of <code>X-Method-Override</code> or
     * <code>X-Http-Method-Override</code> request headers was used to override
     * the actual http method.
     * 
     * @return the request http method
     */
    public String getHttpMethod();

    /**
     * Get the {@link LinkBuilders} context
     * 
     * @return the {@link LinkBuilders} context
     */
    public LinkBuilders getLinkBuilders();

}
