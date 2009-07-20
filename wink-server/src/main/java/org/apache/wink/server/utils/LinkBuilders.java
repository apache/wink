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

package org.apache.wink.server.utils;

/**
 * Provides access to {@link SystemLinksBuilder} and to
 * {@link SingleLinkBuilder}
 * <p>
 * This interface can be injected to a resource method using the
 * {@link javax.ws.rs.core.Context} annotation
 */
public interface LinkBuilders {

    /**
     * Get a new instance of a {@link SingleLinkBuilder}. The state of the
     * builder is initialized to reflect the active resource. If a sub-resource
     * was invoked, the state of the builder is set accordingly.
     * 
     * @return a new instance of {@link SingleLinkBuilder} with a state
     *         reflecting the current active resource and sub-resource
     */
    public SingleLinkBuilder createSingleLinkBuilder();

    /**
     * Get a new instance of a {@link SystemLinksBuilder}. The state of the
     * builder is initialized to reflect the active resource. If a sub-resource
     * was invoked, the state of the builder is set accordingly.
     * 
     * @return a new instance of {@link SystemLinksBuilder} with a state
     *         reflecting the current active resource and sub-resource
     */
    public SystemLinksBuilder createSystemLinksBuilder();

}
