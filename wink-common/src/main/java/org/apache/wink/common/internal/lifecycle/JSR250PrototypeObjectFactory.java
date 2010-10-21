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

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements ObjectFactory that creates a new object for each call based on its
 * ClassMetadata, with JSR250 PostConstruct and PreDestroy support.
 * 
 * @param <T>
 */
class JSR250PrototypeObjectFactory<T> extends PrototypeObjectFactory<T> {

    private static Logger logger = LoggerFactory.getLogger(JSR250PrototypeObjectFactory.class);
    
    public JSR250PrototypeObjectFactory(ClassMetadata metadata) {
        super(metadata);
    }

    @Override
    public T getInstance(RuntimeContext context) {
        T instance = super.getInstance(context);
        // TODO instead of below, get the method that has the postconstruct
        JSR250LifecycleManagerUtils.executePostConstructMethod(instance);
        return instance;
    }

    @Override
    public void releaseInstance(T instance, RuntimeContext context) {
        JSR250LifecycleManagerUtils.executePreDestroyMethod(instance);
    }

}
