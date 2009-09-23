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
package org.apache.wink.common;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * Enhances javax.ws.rs.core.Application with <tt>getPriority</tt> and
 * <tt>getInstances</tt> methods.
 */
public class WinkApplication extends Application {

    public static final double DEFAULT_PRIORITY = 0.5;

    public static final double SYSTEM_PRIORITY  = 0.1;

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.emptySet();
    }

    /**
     * Returns the Application priority.
     * 
     * @return priority
     */
    public double getPriority() {
        return DEFAULT_PRIORITY;
    }

    /**
     * Returns instances of resources or providers. The implementation should
     * ignore and warn about the classes returned by getClasses() or by
     * getSingletons() methods.
     * 
     * @return a set of instances of resources or providers.
     */
    public Set<Object> getInstances() {
        return Collections.emptySet();
    }

}
