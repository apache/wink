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

import java.io.IOException;
import java.security.PrivilegedActionException;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.metadata.ApplicationMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifecycleManagerUtils {

    private static Logger logger = LoggerFactory.getLogger(LifecycleManagerUtils.class);

    @SuppressWarnings("unchecked")
    public static <T> ObjectFactory<T> createSingletonObjectFactory(T object) {
        Class<T> cls = (Class<T>)object.getClass();
        ClassMetadata classMetadata = collectClassMetadata(cls, false);
        try {
            CreationUtils.injectFields(object, classMetadata, null);
            return new SingletonObjectFactory<T>(object);
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error(Messages.getMessage("injectionFailureSingleton", cls.getName())); //$NON-NLS-1$
            }
            throw new ObjectCreationException(e);
        } catch (PrivilegedActionException e) {
            if (logger.isErrorEnabled()) {
                logger.error(Messages.getMessage("injectionFailureSingleton", cls.getName())); //$NON-NLS-1$
            }
            throw new ObjectCreationException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ObjectFactory<T> createSingletonObjectFactory(final Class<T> cls) {
        ClassMetadata classMetadata = collectClassMetadata(cls, true);
        T object = (T)CreationUtils.createObject(classMetadata, null);
        return new SingletonObjectFactory<T>(object);
    }

    public static <T> ObjectFactory<T> createPrototypeObjectFactory(final Class<T> cls) {
        ClassMetadata classMetadata = collectClassMetadata(cls, true);
        return new PrototypeObjectFactory<T>(classMetadata);
    }

    private static <T> ClassMetadata collectClassMetadata(final Class<T> cls,
                                                          boolean validateConstructor) {
        ClassMetadata classMetadata = null;
        if (ProviderMetadataCollector.isProvider(cls)) {
            classMetadata = ProviderMetadataCollector.collectMetadata(cls);
        } else if (ResourceMetadataCollector.isResource(cls)) {
            classMetadata = ResourceMetadataCollector.collectMetadata(cls);
        } else if (ApplicationMetadataCollector.isApplication(cls)) {
            classMetadata = ApplicationMetadataCollector.collectMetadata(cls);
        } else {
            throw new IllegalArgumentException("Cannot create factory for class " + cls);
        }

        // validate that there is a valid constructor if needed
        if (validateConstructor && classMetadata.getConstructor().getConstructor() == null) {
            throw new IllegalStateException("No valid constructor found for " + cls
                .getCanonicalName());
        }
        return classMetadata;
    }

}
