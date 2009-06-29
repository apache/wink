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
package org.apache.wink.common.internal.factory;


import org.apache.wink.common.annotations.Scope;
import org.apache.wink.common.annotations.Scope.ScopeType;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;

/**
 * Implements a OFFactory that creates objects based on the Scope annotation.
 * 
 * @param <T>
 * @see Scope
 */
public class ScopeOFFactory<T> implements OFFactory<T> {

    public ObjectFactory<T> createObjectFactory(T object) {

        if (object == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        final Class<T> cls = (Class<T>) object.getClass();
        Scope scope = cls.getAnnotation(Scope.class);

        if (scope != null) {

            if (scope.value() == ScopeType.SINGLETON) {
                return new SingletonObjectFactory<T>(object);
            } else if (scope.value() == ScopeType.PROTOTYPE) {
                // It's a prototype
                return createPrototype(cls);
            }
        }
        // has no Scope annotation, do nothing
        return null;
    }

    public ObjectFactory<T> createObjectFactory(Class<T> cls) {

        if (cls == null) {
            return null;
        }

        Scope scope = cls.getAnnotation(Scope.class);
        if (scope != null) {
            if (scope.value() == ScopeType.SINGLETON) {
                T object;
                if (ProviderMetadataCollector.isProvider(cls)) {
                    object = CreationUtils.createProvider(cls, null);
                } else if (ResourceMetadataCollector.isStaticResource(cls)) {
                    object = CreationUtils.createResource(cls, null);
                } else {
                    // unknown object, should never reach this code
                    throw new IllegalArgumentException(String.format(
                        "Cannot create factory for a class: %s", String.valueOf(cls)));
                }
                return new SingletonObjectFactory<T>(object);
            } else if (scope.value() == ScopeType.PROTOTYPE) {
                return createPrototype(cls);
            }
        }
        // has no Scope annotation, do nothing
        return null;
    }

    private ObjectFactory<T> createPrototype(final Class<T> cls) {
        if (ResourceMetadataCollector.isStaticResource(cls)) {
            return new ClassMetadataPrototypeOF<T>(ResourceMetadataCollector.collectMetadata(cls));
        }

        if (ProviderMetadataCollector.isProvider(cls)) {
            return new ClassMetadataPrototypeOF<T>(ProviderMetadataCollector.collectMetadata(cls));
        }
        // unknown object, should never reach this code
        throw new IllegalArgumentException(String.format("Cannot create factory for a class: %s",
            String.valueOf(cls)));
    }
}
