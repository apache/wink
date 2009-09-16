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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;

/**
 * specifically testing JAX-RS 1.1
 */
public class ProvidersRegistry11Test extends TestCase {

    /**
     * JAX-RS 1.1 allows syntax such as:
     * 
     * @Consumes( { "abcd/efg, hijk/lmn", "opqr/stu" })
     * @throws Exception
     */
    public void testConsumesAnnotationParsing() throws Exception {
        ProvidersRegistry providersRegistry =
            new ProvidersRegistry(new LifecycleManagersRegistry(), new ApplicationValidator());
        providersRegistry.addProvider(MyProvider.class);
        Field field = providersRegistry.getClass().getDeclaredField("messageBodyReaders");
        field.setAccessible(true);
        Object messageBodyReaders = field.get(providersRegistry);
        Field field2 = messageBodyReaders.getClass().getSuperclass().getDeclaredField("data");
        field2.setAccessible(true);
        HashMap data = (HashMap)field2.get(messageBodyReaders);
        assertEquals(3, data.size());

    }

    /**
     * JAX-RS 1.1 allows syntax such as:
     * 
     * @Produces( { "abcd/efg, hijk/lmn", "opqr/stu" })
     * @throws Exception
     */
    public void testProvidesAnnotationParsing() throws Exception {
        ProvidersRegistry providersRegistry =
            new ProvidersRegistry(new LifecycleManagersRegistry(), new ApplicationValidator());
        providersRegistry.addProvider(MyProvider.class);
        Field field = providersRegistry.getClass().getDeclaredField("messageBodyWriters");
        field.setAccessible(true);
        Object messageBodyWriters = field.get(providersRegistry);
        Field field2 = messageBodyWriters.getClass().getSuperclass().getDeclaredField("data");
        field2.setAccessible(true);
        HashMap data = (HashMap)field2.get(messageBodyWriters);
        assertEquals(3, data.size());

    }

}
