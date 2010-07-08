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
package org.apache.wink.common.internal.application;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.DynamicResource;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to validate classes and objects returned by the Application.
 */
public class ApplicationValidator {

    /**
     * This maps should keep all classes that were already added to any of
     * registries.
     */
    private final Set<Class<?>> singletonClasses = new LinkedHashSet<Class<?>>();

    private static final Logger logger           =
                                                     LoggerFactory
                                                         .getLogger(ApplicationValidator.class);

    /**
     * Returns true if the resource is valid resource and false otherwise.
     * Notice that a class with Path annotation can be validated only once,
     * since it must be unique. Therefore, calling this method for the same
     * class can return true only for the first time.
     * 
     * @param cls
     * @return true if the resource is valid resource and false otherwise.
     */
    public boolean isValidResource(Class<?> cls) {
        return isValidClass(cls) && ((ResourceMetadataCollector.isStaticResource(cls) && classUnique(cls)) || ResourceMetadataCollector
            .isDynamicResource(cls));
    }

    /**
     * Returns true if the resource is valid provider and false otherwise.
     * Notice that a class with Provider annotation can be validated only once,
     * since it must be unique. Therefore, calling this method for the same
     * class can return true only for the first time.
     * 
     * @param cls
     * @return true if the resource is valid provider and false otherwise.
     */
    public boolean isValidProvider(Class<?> cls) {
        return isValidClass(cls) && ProviderMetadataCollector.isProvider(cls) && classUnique(cls);
    }

    /**
     * returns true if called first time for the given class. The second call
     * with the same class to this method will return false.
     */
    private boolean classUnique(Class<? extends Object> cls) {
        if (!singletonClasses.add(cls)) {
            // the singleton of this class already exists
            if (logger.isWarnEnabled()) {
                logger.warn(Messages.getMessage("classAlreadyAdded", cls.getName())); //$NON-NLS-1$
            }
            return false;
        }
        return true;
    }

    /**
     * Validates that class has only one of the following: Path, Provider,
     * DispatchedPath, DynamicResource
     * 
     * @param cls
     * @return
     */
    private static boolean isValidClass(Class<?> cls) {
        int counter = 0;
        if (ResourceMetadataCollector.isStaticResource(cls)) {
            ++counter;
        }
        if (ProviderMetadataCollector.isProvider(cls)) {
            ++counter;
        }
        if (ResourceMetadataCollector.isDynamicResource(cls)) {
            ++counter;
        }
        boolean valid = counter <= 1;

        if (!valid) {
            if (logger.isWarnEnabled()) {
                logger.warn(Messages.getMessage("classNotValid", //$NON-NLS-1$
                                                cls.getName(), Path.class.getName(),
                                                    DynamicResource.class.getName(),
                                                    Provider.class.getName()));
            }
        }

        return valid;
    }
}
