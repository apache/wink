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
package org.apache.wink.common.internal.lifecycle;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;

/**
 * Implements ObjectFactory that creates a new object for each call based on its
 * ClassMetadata.
 * 
 * @param <T>
 */
class PrototypeObjectFactory<T> implements ObjectFactory<T> {

    private final ClassMetadata metadata;

    public PrototypeObjectFactory(ClassMetadata metadata) {
        this.metadata = metadata;
        if (metadata == null) {
            throw new NullPointerException("metadata");
        }
    }

    @SuppressWarnings("unchecked")
    public T getInstance(RuntimeContext context) {
        return (T)CreationUtils.createObject(metadata, context);
    }

    @SuppressWarnings("unchecked")
    public Class<T> getInstanceClass() {
        return (Class<T>)metadata.getResourceClass();
    }

    @Override
    public String toString() {
        return String.format("ClassMetadataPrototypeOF %s", String.valueOf(metadata));
    }
}
