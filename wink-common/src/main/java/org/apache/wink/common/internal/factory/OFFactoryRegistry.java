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
package org.apache.wink.common.internal.factory;

import java.util.LinkedList;

/**
 * Registry of OFFactories.
 */
public class OFFactoryRegistry {

    private LinkedList<OFFactory<?>> store                 = new LinkedList<OFFactory<?>>();

    @SuppressWarnings("unchecked")
    private OFFactory                defaultFactoryFactory = new DefaultOFFactory();

    public void setFactoryFactoryArray(OFFactory<?>[] factories) {
        for (OFFactory<?> factory : factories) {
            addFactoryFactory(factory);
        }
    }

    /**
     * Adds a OFFactory to the registry. The factory will be added with the
     * highest priority, meaning it will be invoked <tt>before</tt> the factory
     * that were already added.
     * 
     * @param factory
     */
    public void addFactoryFactory(OFFactory<?> factory) {
        store.addFirst(factory);
    }

    /**
     * Returns an ObjectFactory based on the object's instance.
     * 
     * @param object
     *            - object for which a ObjectFactory will be returned.
     * @return ObjectFactory
     */
    @SuppressWarnings("unchecked")
    public <T> ObjectFactory<T> getObjectFactory(T object) {
        for (OFFactory factoryFactory : store) {
            ObjectFactory objectFactory = factoryFactory.createObjectFactory(object);
            if (objectFactory != null) {
                return objectFactory;
            }
        }
        return defaultFactoryFactory.createObjectFactory(object);
    }

    /**
     * Returns an ObjectFactory based on the object's class.
     * 
     * @param cls
     *            - class for which a ObjectFactory will be returned.
     * @return ObjectFactory
     */
    @SuppressWarnings("unchecked")
    public <T> ObjectFactory<T> getObjectFactory(Class<T> cls) {
        for (OFFactory factoryFactory : store) {
            ObjectFactory objectFactory = factoryFactory.createObjectFactory(cls);
            if (objectFactory != null) {
                return objectFactory;
            }
        }

        return defaultFactoryFactory.createObjectFactory(cls);
    }
}
