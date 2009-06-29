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

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.annotations.Scope;
import org.apache.wink.common.annotations.Scope.ScopeType;
import org.apache.wink.common.internal.factory.ClassMetadataPrototypeOF;
import org.apache.wink.common.internal.factory.OFFactoryRegistry;
import org.apache.wink.common.internal.factory.ScopeOFFactory;
import org.apache.wink.common.internal.factory.SingletonObjectFactory;

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
        OFFactoryRegistry factoryRegistry = new OFFactoryRegistry();

        // by default providers are always singletons
        assertEquals(SingletonObjectFactory.class,
            factoryRegistry.getObjectFactory(ProviderA.class).getClass());
        assertEquals(SingletonObjectFactory.class, factoryRegistry.getObjectFactory(
            ProviderScopeablePrototype.class).getClass());
        assertEquals(SingletonObjectFactory.class, factoryRegistry.getObjectFactory(
            ProviderScopeableSingleton.class).getClass());

        // by default providers are always singletons
        assertEquals(SingletonObjectFactory.class,
            factoryRegistry.getObjectFactory(new ProviderA()).getClass());
        assertEquals(SingletonObjectFactory.class, factoryRegistry.getObjectFactory(
            new ProviderScopeablePrototype()).getClass());
        assertEquals(SingletonObjectFactory.class, factoryRegistry.getObjectFactory(
            new ProviderScopeableSingleton()).getClass());

        // by default resources are prototypes, when added as classes
        assertEquals(ClassMetadataPrototypeOF.class, factoryRegistry.getObjectFactory(
            ResourceA.class).getClass());
        assertEquals(ClassMetadataPrototypeOF.class, factoryRegistry.getObjectFactory(
            ResourceScopableSingleton.class).getClass());
        assertEquals(ClassMetadataPrototypeOF.class, factoryRegistry.getObjectFactory(
            ResourceScopablePrototype.class).getClass());

        // by default resources are singletons, when added as instances
        assertEquals(SingletonObjectFactory.class,
            factoryRegistry.getObjectFactory(new ResourceA()).getClass());
        assertEquals(SingletonObjectFactory.class, factoryRegistry.getObjectFactory(
            new ResourceScopableSingleton()).getClass());
        assertEquals(SingletonObjectFactory.class, factoryRegistry.getObjectFactory(
            new ResourceScopablePrototype()).getClass());

    }

    @SuppressWarnings("unchecked")
    public void testScopeable() {
        OFFactoryRegistry factoryRegistry = new OFFactoryRegistry();
        factoryRegistry.addFactoryFactory(new ScopeOFFactory());

        // default
        assertEquals(SingletonObjectFactory.class,
            factoryRegistry.getObjectFactory(ProviderA.class).getClass());

        // prototype
        assertEquals(ClassMetadataPrototypeOF.class, factoryRegistry.getObjectFactory(
            ProviderScopeablePrototype.class).getClass());

        // singleton
        assertEquals(SingletonObjectFactory.class, factoryRegistry.getObjectFactory(
            ProviderScopeableSingleton.class).getClass());

        // default
        assertEquals(SingletonObjectFactory.class,
            factoryRegistry.getObjectFactory(new ProviderA()).getClass());

        // prototype
        assertEquals(ClassMetadataPrototypeOF.class, factoryRegistry.getObjectFactory(
            new ProviderScopeablePrototype()).getClass());

        // singleton
        assertEquals(SingletonObjectFactory.class, factoryRegistry.getObjectFactory(
            new ProviderScopeableSingleton()).getClass());

        // default
        assertEquals(ClassMetadataPrototypeOF.class, factoryRegistry.getObjectFactory(
            ResourceA.class).getClass());

        // singleton
        assertEquals(SingletonObjectFactory.class, factoryRegistry.getObjectFactory(
            ResourceScopableSingleton.class).getClass());

        // prototype
        assertEquals(ClassMetadataPrototypeOF.class, factoryRegistry.getObjectFactory(
            ResourceScopablePrototype.class).getClass());

        // default
        assertEquals(SingletonObjectFactory.class,
            factoryRegistry.getObjectFactory(new ResourceA()).getClass());

        // singleton
        assertEquals(SingletonObjectFactory.class, factoryRegistry.getObjectFactory(
            new ResourceScopableSingleton()).getClass());

        // prototype
        assertEquals(ClassMetadataPrototypeOF.class, factoryRegistry.getObjectFactory(
            new ResourceScopablePrototype()).getClass());

    }
}
