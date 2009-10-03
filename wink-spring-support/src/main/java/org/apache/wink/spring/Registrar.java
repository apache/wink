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

package org.apache.wink.spring;

import java.util.Set;

import org.apache.wink.common.WinkApplication;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.server.internal.application.ApplicationProcessor;
import org.apache.wink.server.internal.registry.ResourceRegistry;

public class Registrar extends WinkApplication {

    private double        priority  = WinkApplication.DEFAULT_PRIORITY;

    private Set<Object>   instances = null;
    private Set<Class<?>> classes   = null;

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    public void setClasses(Set<Class<?>> classes) {
        this.classes = classes;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public double getPriority() {
        return priority;
    }

    public void register(ResourceRegistry resourceRegistry, ProvidersRegistry providersRegistry) {
        new ApplicationProcessor(this, resourceRegistry, providersRegistry, false).process();
    }

    public void setInstances(Set<Object> instances) {
        this.instances = instances;
    }

    public Set<Object> getInstances() {
        return instances;
    }

}
