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

package org.apache.wink.common.internal.registry.metadata;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import junit.framework.TestCase;

public class ProviderMetadataCollectorTest extends TestCase {

    @Path("/")
    public interface Interface {

        @GET
        @Produces("text/plain")
        public String method();

    }
    
    @Path("/")
    public class Class {
        
        @GET
        @Produces("text/plain")
        public String method() {
            return "method";
        }
    }
    
    @Provider
    public interface ProviderInterface {
        
    }
    
    public class ProviderInterfaceImpl implements ProviderInterface {
        
    }
    
    @Provider
    public abstract class AbstractProvider {
        
    }
    
    public class ProviderBaseClass extends AbstractProvider {
        
    }
    
    @Provider
    public static class ProviderStandalone {
        
    }
    
    public void testIsProvider() {
        assertFalse(ProviderMetadataCollector.isProvider(Interface.class));
        assertFalse(ProviderMetadataCollector.isProvider(Class.class));
        assertTrue(ProviderMetadataCollector.isProvider(ProviderInterface.class));
        assertTrue(ProviderMetadataCollector.isProvider(ProviderInterfaceImpl.class));
        assertTrue(ProviderMetadataCollector.isProvider(AbstractProvider.class));
        assertTrue(ProviderMetadataCollector.isProvider(ProviderBaseClass.class));
        assertTrue(ProviderMetadataCollector.isProvider(ProviderStandalone.class));
    }
}
