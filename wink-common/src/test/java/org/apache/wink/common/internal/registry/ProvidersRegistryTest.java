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
package org.apache.wink.common.internal.registry;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;

public class ProvidersRegistryTest extends TestCase {
    
    /**
     * Tests that the providersCache object is and remains instanceof ConcurrentHashMap.  
     * 
     * ProvidersRegistry.MediaTypeMap uses type ConcurrentHashMap on the providersCache object to provide some lock protection on
     * the map when providers are dynamically added.  However, lock protection is already built into the ProvidersRegistry methods:
     * getContextResolver(), getMessageBodyReader(), and getMessageBodyWriter().
     * 
     * However, the second protection (in the ProvidersRegistry methods) is for the cache itself which could be written to by two
     * different threads even if they both were getting a single MessageBodyReader (i.e. a cache value may be dropped and then two
     * threads come back later and try to write a new cache value).  Due to some weird HashMap properties, this can blow up in
     * weird ways.
     * 
     * Thus, we need to ensure the providersCache continues to be instantiated with ConcurrentHashMap.
     */
    public void testProtectionModel() throws Exception {

        // I need the instantiated object providersCache in the abstract private nested class MediaTypeMap, so here we go!
        ProvidersRegistry providersRegistry = new ProvidersRegistry(new LifecycleManagersRegistry(), new ApplicationValidator());
        Field field = providersRegistry.getClass().getDeclaredField("messageBodyReaders");
        field.setAccessible(true);
        Object messageBodyReaders = field.get(providersRegistry);
        System.out.println(messageBodyReaders.getClass().getSuperclass().getName());
        Field field2 = messageBodyReaders.getClass().getSuperclass().getDeclaredField("providersCache");
        field2.setAccessible(true);
        Object providersCache = field2.get(messageBodyReaders);
        
        assertTrue(providersCache instanceof ConcurrentHashMap);    
    }
    
    // TODO:  perhaps future tests should be added to actually exercise the providersCache code, but it would be an involved,
    // multi-threaded test that dynamically adds providers at just the right time to ensure no problems with
    // concurrent writes.
    
}
