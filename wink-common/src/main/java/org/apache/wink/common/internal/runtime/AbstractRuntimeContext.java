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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.apache.wink.common.RuntimeContext;

public abstract class AbstractRuntimeContext implements RuntimeContext {

    private HashMap<String, Object> attributes;

    public AbstractRuntimeContext() {
        this.attributes = new HashMap<String, Object>(32);
    }

    public final Map<String, Object> getAttributes() {
        return attributes;
    }

    public final <T> void setAttribute(Class<T> type, T object) {
        getAttributes().put(type.getName(), object);
    }

    @SuppressWarnings("unchecked")
    public final <T> T getAttribute(Class<T> type) {
        return (T)getAttributes().get(type.getName());
    }

    public Providers getProviders() {
        return getAttribute(Providers.class);
    }

    public HttpHeaders getHttpHeaders() {
        return getAttribute(HttpHeaders.class);
    }

    public UriInfo getUriInfo() {
        return getAttribute(UriInfo.class);
    }

    public SecurityContext getSecurityContext() {
        return getAttribute(SecurityContext.class);
    }

    public Request getRequest() {
        return getAttribute(Request.class);
    }

}
