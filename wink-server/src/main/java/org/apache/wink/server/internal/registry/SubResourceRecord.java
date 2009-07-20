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
import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor;

/**
 * Abstract base class for sub-resource methods records
 */
public abstract class SubResourceRecord extends TemplatedRecord implements MethodRecord {

    private MethodMetadata metadata;

    public SubResourceRecord(MethodMetadata metadata) {
        super(UriTemplateProcessor.newNormalizedInstance(metadata.getPath()));
        this.metadata = metadata;
    }

    public MethodMetadata getMetadata() {
        return metadata;
    }

    @Override
    public int compareTo(TemplatedRecord o) {
        int compared = super.compareTo(o);
        if (compared != 0) {
            return compared;
        }
        return compareToSubResource((SubResourceRecord)o);
    }

    /**
     * Compare the type of this sub-resource with another sub-resource;
     * sub-resource methods come ahead of sub-resource locators
     * 
     * @param other the sub-resource to compare to
     * @return 0 is the same, -1 if compared to a sub-resource locator and 1 if
     *         compared to a sub-resource method
     */
    protected abstract int compareToSubResource(SubResourceRecord other);
}
