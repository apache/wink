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
 * Record for a sub-resource method
 */
public class SubResourceMethodRecord extends SubResourceRecord {

    public SubResourceMethodRecord(MethodMetadata metadata) {
        super(metadata);
    }

    @Override
    protected int compareToSubResource(SubResourceRecord o) {
        if (o instanceof SubResourceMethodRecord) {
            // if this is also a sub-resource method
            return 0;
        }
        // if the other record is not a sub-resource method,
        // it's a sub-resource locator, which comes after of a method
        return 1;
    }

}
