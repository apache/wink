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
 * A builder for generating the system links of a resource. The links are
 * created absolute or relative to the base uri according to the configuration
 * or request settings.
 */
public interface SystemLinksBuilder extends BaseLinksBuilder<SystemLinksBuilder> {

    /**
     * Types of system links
     */
    public enum LinkType {
        SELF, ALTERNATE, EDIT, OPENSEARCH;
    }

    /**
     * Set the types of system links to generate. If this method is not called
     * or if no types are specified, then all the possible types will be
     * generated.
     * 
     * @param types the types of system links to generate
     * @return this links builder
     */
    public SystemLinksBuilder types(LinkType... types);

    /**
     * Determines if this system links builder will generate links for all the
     * root resources that are reachable when the search mode is
     * "Continued Search" or just the current active resource. If this method is
     * not called, then the builder will use the value defined by the
     * wink.searchPolicyContinuedSearch property.
     * 
     * @param all If set to <code>true</code> then this system links builder
     *            will generate links for all the root resources that are
     *            reachable when the search mode is "Continued Search".
     * @return this links builder
     */
    public SystemLinksBuilder allResources(boolean all);

}
