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

package org.apache.wink.client.internal;

import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

public interface BaseRequestResponse {

    /**
     * Get the live map of headers
     * 
     * @return the live map of headers
     */
    MultivaluedMap<String, String> getHeaders();

    /**
     * Get the live list of attributes
     * 
     * @return the live list of attributes
     */
    Map<String, Object> getAttributes();

    /**
     * Convenience method to set an attribute whose key is the fully qualified
     * name of attribute class
     * 
     * @param <T> type of attribute
     * @param key the class type whose fully qualified name will be used as the
     *            key
     * @param attribute the attribute to set
     */
    <T> void setAttribute(Class<T> key, T attribute);

    /**
     * Convenience method to retrieve an attribute whose key is the fully
     * qualified name of attribute class
     * 
     * @param <T> type of attribute
     * @param key the class type whose fully qualified name will be used as the
     *            key
     * @return the attribute value, or null if it does not exist
     */
    <T> T getAttribute(Class<T> key);
}
