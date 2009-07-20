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

import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.common.internal.uritemplate.UriTemplateMatcher;

/**
 * Represents a sub-resource (method or locator) record paired with the uri
 * template matcher that was used to match the sub-resource record
 */
public class SubResourceInstance implements MethodRecord, Comparable<SubResourceInstance> {

    private SubResourceRecord  record;
    private UriTemplateMatcher matcher;

    public SubResourceInstance(SubResourceRecord record, UriTemplateMatcher matcher) {
        this.record = record;
        this.matcher = matcher;
    }

    public MethodMetadata getMetadata() {
        return record.getMetadata();
    }

    /**
     * Get the sub-resource record of this instance
     * 
     * @return {@link SubResourceRecord} of this instance
     */
    public SubResourceRecord getRecord() {
        return record;
    }

    /**
     * Get the uri template matcher of this instance
     * 
     * @return {@link UriTemplateMatcher} of this instance
     */
    public UriTemplateMatcher getMatcher() {
        return matcher;
    }

    public int compareTo(SubResourceInstance other) {
        return record.compareTo(other.record);
    }

}
