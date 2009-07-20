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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.client.internal.BaseRequestResponse;
import org.apache.wink.common.internal.CaseInsensitiveMultivaluedMap;

public class BaseRequestResponseImpl implements BaseRequestResponse {

    MultivaluedMap<String, String>  headers;
    private HashMap<String, Object> attributes;

    public BaseRequestResponseImpl() {
        this.attributes = new HashMap<String, Object>();
        this.headers = new CaseInsensitiveMultivaluedMap<String>();
    }

    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public <T> void setAttribute(Class<T> type, T object) {
        getAttributes().put(type.getName(), object);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(Class<T> type) {
        return (T)getAttributes().get(type.getName());
    }

}
