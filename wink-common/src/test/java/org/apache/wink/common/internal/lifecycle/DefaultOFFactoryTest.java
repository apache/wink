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


import org.apache.wink.common.DynamicResource;
import org.apache.wink.common.internal.lifecycle.DefaultLifecycleManager;
import org.apache.wink.common.internal.lifecycle.SingletonObjectFactory;

import junit.framework.TestCase;

/**
 * test scenarios not covered by OFFactoryTest
 */
public class DefaultOFFactoryTest extends TestCase {

    private static class Dummy {
    }

    @SuppressWarnings("unchecked")
    public void testNull() {
        DefaultLifecycleManager defaultOFFactory = new DefaultLifecycleManager();

        try {
            defaultOFFactory.createObjectFactory(null);
        } catch (NullPointerException e) {
            return;
        }
        fail("Null pointet exception should be thrown.");
    }

    @SuppressWarnings("unchecked")
    public void testResourceBean() {
        DefaultLifecycleManager defaultOFFactory = new DefaultLifecycleManager();

        try {
            defaultOFFactory.createObjectFactory(DynamicResource.class);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("IllegalArgumentException should be thrown.");
    }

    @SuppressWarnings("unchecked")
    public void testDummy() {
        DefaultLifecycleManager defaultOFFactory = new DefaultLifecycleManager();
        
        try {
            defaultOFFactory.createObjectFactory(new Dummy());
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }

        try {
            defaultOFFactory.createObjectFactory(Dummy.class);
            fail("IllegalArgumentException should be thrown.");
        } catch (IllegalArgumentException e) {
        }
        
    }
}
