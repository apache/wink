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
package org.apache.wink.common.internal.properties;

import java.util.Properties;

import junit.framework.TestCase;

import org.junit.Test;

public class WinkSystemPropertiesTest extends TestCase {

    private static final String PROP_KEY = "someSysPropKey";
    
    @Override
    public void setUp() {
        System.clearProperty(PROP_KEY);
    }
    
    @Test
    public void testNullToleration() {
        System.setProperty(PROP_KEY, "false");
        // loadSystemProperties can only override an existing key in the properties bag.  We should get an empty bag back
        Properties props = WinkSystemProperties.loadSystemProperties(null);
        assertTrue("expected empty properties bag", props.isEmpty());
    }

    
    @Test
    public void testEmptyValue() {
        Properties startProps = new Properties();
        startProps.put(PROP_KEY, "");
        System.setProperty(PROP_KEY, "false");
        // loadSystemProperties can override an existing key in the properties bag when the value is empty
        Properties props = WinkSystemProperties.loadSystemProperties(startProps);
        assertEquals("expected non-null \""+PROP_KEY+"\" property to be \"false\"", "false", props.getProperty(PROP_KEY));
    }
    
    @Test
    public void testGetSystemProperties() throws Exception {
        Properties startProps = new Properties();  // empty
        System.setProperty(PROP_KEY, "false");
        // loadSystemProperties can only override an existing key in the properties bag.  We should get an empty bag back
        Properties props = WinkSystemProperties.loadSystemProperties(startProps);
        assertTrue("expected empty properties bag", props.isEmpty());
    }
    
    @Test
    public void testPropsOverride() throws Exception {
        Properties startProps = new Properties();
        startProps.setProperty(PROP_KEY, "true");
        System.setProperty(PROP_KEY, "false");
        // make sure the system property does NOT override what's already in the bag.  We should get an empty bag back
        Properties props = WinkSystemProperties.loadSystemProperties(startProps);
        assertTrue("expected empty properties bag", props.isEmpty());
    }

}
