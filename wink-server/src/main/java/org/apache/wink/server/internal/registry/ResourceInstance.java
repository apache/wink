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

package org.apache.wink.server.internal.registry;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.common.internal.uritemplate.UriTemplateMatcher;

/**
 * Represents a concrete resource instance and is used during the search of a
 * resource method to be invoked. It holds the resource record of a resource,
 * the uri template matcher that was used to match the resource, and possibly
 * the actual instance of the resource
 */
public class ResourceInstance {

    private ResourceRecord     record;
    private UriTemplateMatcher matcher;
    private Object             instance;

    public ResourceInstance(ResourceRecord record, UriTemplateMatcher matcher) {
        this(null, record, matcher);
    }

    public ResourceInstance(Object instance, ResourceRecord record, UriTemplateMatcher matcher) {
        this.instance = instance;
        this.record = record;
        this.matcher = matcher;
    }

    /**
     * Get the record associated with this resource
     * 
     * @return the {@link ResourceRecord}
     */
    public ResourceRecord getRecord() {
        return record;
    }

    /**
     * Get the uri template matcher that was used to match this resource
     * 
     * @return the uri template matcher
     */
    public UriTemplateMatcher getMatcher() {
        return matcher;
    }

    /**
     * Get the concrete instance. If this ResourceInstance was initialized with
     * an object instance, then it is returned. Otherwise, the
     * {@link ObjectFactory} associated with the resource record is invoked to
     * create a concrete instance
     * 
     * @param context the request context
     * @return the resource concrete instance
     */
    public Object getInstance(RuntimeContext context) {
        if (instance != null) {
            return instance;
        }
        instance = record.getObjectFactory().getInstance(context);
        return instance;
    }

    /**
     * Returns whether the matcher used to match this resource was an exact
     * match
     * 
     * @return true if exact match, false otherwise
     */
    public boolean isExactMatch() {
        return matcher.isExactMatch();
    }

    @Override
    public String toString() {
        return String.format("ResourceRecord: %s; UriTemplateMatcher: %s", //$NON-NLS-1$
                             record,
                             matcher);
    }

    public Class<?> getResourceClass() {
        return record.getMetadata().getResourceClass();
    }

}
