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

import org.apache.wink.common.internal.registry.metadata.ApplicationMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;

/**
 * <p>
 * implementation for LifecycleManager according to JAX RS (JSR 311) that supports
 * JSR250 PostConstruct and PreDestroy annotations
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
public class JSR250LifecycleManager<T> implements LifecycleManager<T> {

    public ObjectFactory<T> createObjectFactory(T object) {

        if (object == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        final Class<T> cls = (Class<T>)object.getClass();

        if (JSR250LifecycleManagerUtils.hasJSR250AnnotatedMethod(cls)) {
            return JSR250LifecycleManagerUtils.createSingletonObjectFactory(cls);
        }
        
        // has no JSR250 annotations, do nothing
        return null;
    }

    public ObjectFactory<T> createObjectFactory(Class<T> cls) {

        if (cls == null) {
            return null;
        }

        if (JSR250LifecycleManagerUtils.hasJSR250AnnotatedMethod(cls)) {
            ObjectFactory<T> ret = null;
            if (ApplicationMetadataCollector.isApplication(cls)) {
                // by default application subclasses are singletons
                ret = JSR250LifecycleManagerUtils.createSingletonObjectFactory(cls);
            } else if (ProviderMetadataCollector.isProvider(cls)) {
                // by default providers are singletons
                ret = JSR250LifecycleManagerUtils.createSingletonObjectFactory(cls);
            } else if (ResourceMetadataCollector.isStaticResource(cls)) {
                // by default resources are prototypes (created per request)
                ret = JSR250LifecycleManagerUtils.createPrototypeObjectFactory(cls);
            }

            return ret;
        }
        
        // has no JSR250 annotations, do nothing
        return null;
    }

}
