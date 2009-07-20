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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;

/**
 * <p>
 * Default implementation for LifecycleManager according to JAX RS (JSR 311).
 * <p>
 * For <tt>createObjectFactory(T object)</tt> the factory will always return a
 * SingletonObjectFactory.
 * <p>
 * For <tt>createObjectFactory(final Class<T> cls)</tt> the factory will return:
 * <ul>
 * <li>SingletonObjectFactory - for Providers</li>
 * <li>ClassMetadataPrototypeOF - for Resources</li>
 * <li>SimplePrototypeOF - for Resources (marked with DispatchedPath annotation)
 * </li>
 * </ul>
 * and throw IllegalArgumentException otherwise.
 * 
 * @param <T>
 * @see SingletonObjectFactory
 * @see PrototypeObjectFactory
 * @see SimplePrototypeOF
 */
class DefaultLifecycleManager<T> implements LifecycleManager<T> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLifecycleManager.class);

    /**
     * The default implementation, returns a SingletonFactory for all objects.
     */
    public ObjectFactory<T> createObjectFactory(T object) {
        if (object == null) {
            throw new NullPointerException("object");
        }

        logger.debug("Creating a {} for {}", SingletonObjectFactory.class, object);

        return LifecycleManagerUtils.createSingletonObjectFactory(object);
    }

    public ObjectFactory<T> createObjectFactory(final Class<T> cls) {

        if (cls == null) {
            throw new NullPointerException("cls");
        }

        if (ResourceMetadataCollector.isDynamicResource(cls)) {
            // default factory cannot create instance of DynamicResource
            throw new IllegalArgumentException(String
                .format("Cannot create default factory for DynamicResource: %s", String
                    .valueOf(cls)));
        }

        if (ProviderMetadataCollector.isProvider(cls)) {
            // by default providers are singletons
            return LifecycleManagerUtils.createSingletonObjectFactory(cls);
        }

        if (ResourceMetadataCollector.isStaticResource(cls)) {
            // by default resources are prototypes (created per request)
            return LifecycleManagerUtils.createPrototypeObjectFactory(cls);
        }

        // unknown object, should never reach this code
        throw new IllegalArgumentException(String
            .format("Cannot create default factory for class: %s", String.valueOf(cls)));
    }

}
