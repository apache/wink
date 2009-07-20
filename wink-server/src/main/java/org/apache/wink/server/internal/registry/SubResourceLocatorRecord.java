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

/**
 * Record for a sub-resource locator
 */
public class SubResourceLocatorRecord extends SubResourceRecord {

    public SubResourceLocatorRecord(MethodMetadata metadata) {
        super(metadata);
    }

    @Override
    protected int compareToSubResource(SubResourceRecord o) {
        if (o instanceof SubResourceLocatorRecord) {
            // if this is also a sub-resource locator
            return 0;
        }
        // if the other record is not a sub-resource locator,
        // it's a sub-resource method, which comes ahead of a locator
        return -1;
    }
}
