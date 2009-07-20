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

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.annotations.Scope;
import org.apache.wink.common.annotations.Scope.ScopeType;
import org.apache.wink.common.internal.lifecycle.PrototypeObjectFactory;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.lifecycle.ScopeLifecycleManager;
import org.apache.wink.common.internal.lifecycle.SingletonObjectFactory;

import junit.framework.TestCase;

public class OFFactoryTest extends TestCase {

    @Provider
    public static class ProviderA {

    }

    @Provider
    @Scope(ScopeType.SINGLETON)
    public static class ProviderScopeableSingleton {

    }

    @Provider
    @Scope(ScopeType.PROTOTYPE)
    public static class ProviderScopeablePrototype {

    }

    @Path("/a")
    public static class ResourceA {

    }

    @Path("/a")
    @Scope(ScopeType.SINGLETON)
    public static class ResourceScopableSingleton {

    }

    @Path("/a")
    @Scope(ScopeType.PROTOTYPE)
    public static class ResourceScopablePrototype {

    }

    public void testDefault() {
        LifecycleManagersRegistry factoryRegistry = new LifecycleManagersRegistry();

        // by default providers are always singletons
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(ProviderA.class).getClass());
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(ProviderScopeablePrototype.class).getClass());
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(ProviderScopeableSingleton.class).getClass());

        // by default providers are always singletons
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(new ProviderA()).getClass());
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(new ProviderScopeablePrototype()).getClass());
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(new ProviderScopeableSingleton()).getClass());

        // by default resources are prototypes, when added as classes
        assertEquals(PrototypeObjectFactory.class, factoryRegistry
            .getObjectFactory(ResourceA.class).getClass());
        assertEquals(PrototypeObjectFactory.class, factoryRegistry
            .getObjectFactory(ResourceScopableSingleton.class).getClass());
        assertEquals(PrototypeObjectFactory.class, factoryRegistry
            .getObjectFactory(ResourceScopablePrototype.class).getClass());

        // by default resources are singletons, when added as instances
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(new ResourceA()).getClass());
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(new ResourceScopableSingleton()).getClass());
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(new ResourceScopablePrototype()).getClass());

    }

    @SuppressWarnings("unchecked")
    public void testScopeable() {
        LifecycleManagersRegistry factoryRegistry = new LifecycleManagersRegistry();
        factoryRegistry.addFactoryFactory(new ScopeLifecycleManager());

        // default
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(ProviderA.class).getClass());

        // prototype
        assertEquals(PrototypeObjectFactory.class, factoryRegistry
            .getObjectFactory(ProviderScopeablePrototype.class).getClass());

        // singleton
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(ProviderScopeableSingleton.class).getClass());

        // default
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(new ProviderA()).getClass());

        // prototype
        assertEquals(PrototypeObjectFactory.class, factoryRegistry
            .getObjectFactory(new ProviderScopeablePrototype()).getClass());

        // singleton
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(new ProviderScopeableSingleton()).getClass());

        // default
        assertEquals(PrototypeObjectFactory.class, factoryRegistry
            .getObjectFactory(ResourceA.class).getClass());

        // singleton
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(ResourceScopableSingleton.class).getClass());

        // prototype
        assertEquals(PrototypeObjectFactory.class, factoryRegistry
            .getObjectFactory(ResourceScopablePrototype.class).getClass());

        // default
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(new ResourceA()).getClass());

        // singleton
        assertEquals(SingletonObjectFactory.class, factoryRegistry
            .getObjectFactory(new ResourceScopableSingleton()).getClass());

        // prototype
        assertEquals(PrototypeObjectFactory.class, factoryRegistry
            .getObjectFactory(new ResourceScopablePrototype()).getClass());

    }
}
