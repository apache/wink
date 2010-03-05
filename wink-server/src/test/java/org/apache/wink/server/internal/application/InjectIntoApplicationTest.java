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
package org.apache.wink.server.internal.application;

import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;


public class InjectIntoApplicationTest extends MockServletInvocationTest {
    
    public static class MyApp extends Application {
        
        // you wouldn't make this public static in the real world...but for test, sure
        @Context
        public static UriInfo uriInfo;

        @Override
        public Set<Class<?>> getClasses() {
            return null;
        }
        
    }
    
    
    @Override
    protected String getApplicationClassName() {
        return MyApp.class.getName();
    }
    
    public void test() throws Exception {
        // Make sure injection works for Application subclass, which is what InjectIntoApplicationTest is.
        // Will be a proxy object if injection worked, but an attempt to use will result in NPE due to proxy's
        // inability to get RuntimeContext.  This is ok for this test as there is no RuntimeContext at Application
        // creation time.
        assertNotNull(MyApp.uriInfo);
    }
    
}
