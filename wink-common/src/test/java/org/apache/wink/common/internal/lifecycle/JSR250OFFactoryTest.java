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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import junit.framework.TestCase;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;

public class JSR250OFFactoryTest extends TestCase {

    protected static String called;
    LifecycleManagersRegistry factoryRegistry;
    
    @Provider
    public static class ProviderPostConstructSingleton {

        @PostConstruct
        public void postConstruct() {
            called = this.getClass().getSimpleName() + ".postConstruct()";
        }
        
    }
    
    @Provider
    public static class ProviderPreDestroySingleton {

        @PreDestroy
        public void preDestroy() {
            called = this.getClass().getSimpleName() + ".preDestroy()";
        }
        
    }
    
    @Provider
    public static class ProviderBothSingleton {

        @PostConstruct
        public void postConstruct() {
            called = this.getClass().getSimpleName() + ".postConstruct()";
        }
        
        @PreDestroy
        public void preDestroy() {
            called = this.getClass().getSimpleName() + ".preDestroy()";
        }
        
    }
    
    @Provider
    public static class ProviderPostConstructDoubleSingleton {
        @PostConstruct
        public void postConstruct1() {
            called = this.getClass().getSimpleName() + ".postConstruct1()";
        }
        @PostConstruct
        public void postConstruct2() {
            called = this.getClass().getSimpleName() + ".postConstruct2()";
        }
    }
    
    @Path("")
    public static class MyResource {
        @PostConstruct
        public void postConstruct() {
            called = this.getClass().getSimpleName() + ".postConstruct()";
        }
        
        @PreDestroy
        public void preDestroy() {
            called = this.getClass().getSimpleName() + ".preDestroy()";
        }
    }
    
    @SuppressWarnings("unchecked")
    public void setUp() {
        called = null;
        factoryRegistry = new LifecycleManagersRegistry();
        factoryRegistry.addFactoryFactory(new JSR250LifecycleManager());
    }
    
    @SuppressWarnings("unchecked")
    public void testCorrectOF() {
        ObjectFactory of = factoryRegistry.getObjectFactory(ProviderPostConstructSingleton.class);
        assertEquals(JSR250SingletonObjectFactory.class, of.getClass());
    }
    
    @SuppressWarnings("unchecked")
    public void testCorrectOF2() {
        ObjectFactory of = factoryRegistry.getObjectFactory(ProviderPreDestroySingleton.class);
        assertEquals(JSR250SingletonObjectFactory.class, of.getClass());
    }
    
    @SuppressWarnings("unchecked")
    public void testCorrectOF3() {
        ObjectFactory of = factoryRegistry.getObjectFactory(ProviderBothSingleton.class);
        assertEquals(JSR250SingletonObjectFactory.class, of.getClass());
    }
    
    @SuppressWarnings("unchecked")
    public void testPostConstructCalled() {
        ObjectFactory of = factoryRegistry.getObjectFactory(ProviderPostConstructSingleton.class);
        assertEquals(JSR250SingletonObjectFactory.class, of.getClass());
        // due to behavior of singleton instances, the object will have already been created without 
        // calling of.getInstance(RuntimeContext)
        assertEquals(called, ProviderPostConstructSingleton.class.getSimpleName() + ".postConstruct()");
    }
    
    @SuppressWarnings("unchecked")
    public void testFirstPostConstructCalled() {
        ObjectFactory of = factoryRegistry.getObjectFactory(ProviderPostConstructDoubleSingleton.class);
        assertEquals(JSR250SingletonObjectFactory.class, of.getClass());
        // due to behavior of singleton instances, the object will have already been created without 
        // calling of.getInstance(RuntimeContext)
        assertEquals(called, ProviderPostConstructDoubleSingleton.class.getSimpleName() + ".postConstruct1()");
    }
    
    @SuppressWarnings("unchecked")
    public void testPreDestroyCalled() {
        ObjectFactory of = factoryRegistry.getObjectFactory(ProviderPreDestroySingleton.class);
        assertEquals(JSR250SingletonObjectFactory.class, of.getClass());
        // no PostConstruct in ProviderPreDestroySingleton
        assertEquals(called, null);
        RuntimeContext context = RuntimeContextTLS.getRuntimeContext();
        of.releaseInstance(of.getInstance(context), context);
        assertEquals(called, null);  // we don't want releaseInstance to call @PreDestroy annotated method for singletons
        of.releaseAll(context);
        assertEquals(called, ProviderPreDestroySingleton.class.getSimpleName() + ".preDestroy()");
    }
    
    @SuppressWarnings("unchecked")
    public void testResource() {
        ObjectFactory of = factoryRegistry.getObjectFactory(MyResource.class);
        assertEquals(JSR250PrototypeObjectFactory.class, of.getClass());
        // due to being a "Prototype" object factory, instances are only created upon request
        assertEquals(called, null);
        RuntimeContext context = RuntimeContextTLS.getRuntimeContext();
        of.getInstance(null);
        assertEquals(called, MyResource.class.getSimpleName() + ".postConstruct()");
        called = null;
        of.releaseAll(context);
        assertEquals(called, null);  // instances created by "Prototype" object factories are per-request instances, not per-app or per-jvm
        of.releaseInstance(of.getInstance(context), context);
        assertEquals(called, MyResource.class.getSimpleName() + ".preDestroy()");  // we *do* want releaseInstance to call @PreDestroy annotated method for per-request instances
    }
}
