/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.wink.jcdi.server.internal.extension;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.CreationUtils;
import org.apache.wink.common.internal.registry.metadata.ApplicationMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JAXRSJCDICustomInjectionTarget<T> implements InjectionTarget<T> {

    private static final Logger      logger =
                                                LoggerFactory
                                                    .getLogger(JAXRSJCDICustomInjectionTarget.class);

    private ClassMetadata            classMetadata;

    final private InjectionTarget<T> delegate;

    static <T> ClassMetadata collectClassMetadata(final Class<T> cls) {
        ClassMetadata classMetadata = null;
        if (ProviderMetadataCollector.isProvider(cls)) {
            classMetadata = ProviderMetadataCollector.collectMetadata(cls);
        } else if (ResourceMetadataCollector.isResource(cls)) {
            classMetadata = ResourceMetadataCollector.collectMetadata(cls);
        } else if (ApplicationMetadataCollector.isApplication(cls)) {
            classMetadata = ApplicationMetadataCollector.collectMetadata(cls);
        }
        return classMetadata;
    }

    public JAXRSJCDICustomInjectionTarget(InjectionTarget<T> delegate) {
        logger.trace("constructor({}) entry", delegate);
        this.delegate = delegate;
        logger.trace("constructor() exit");
    }

    public void inject(final T instance, final CreationalContext<T> creationalContext) {
        logger.trace("inject({}, {}) entry", instance, creationalContext);
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

                public Object run() throws PrivilegedActionException {
                    if (classMetadata == null) {
                        logger.trace("Collecting classMetadata for {}", this);
                        classMetadata = collectClassMetadata(instance.getClass());
                    }
                    logger.trace("Calling CreationUtils.injectFields for instance");
                    try {
                        CreationUtils.injectFields(instance, classMetadata, RuntimeContextTLS
                            .getRuntimeContext());
                    } catch (IOException e) {
                        throw new PrivilegedActionException(e);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            logger.warn(Messages.getMessage("exceptionDuringInjection"), e);
        }

        logger.trace("calling delegate.inject(instance, creationalContext)");
        delegate.inject(instance, creationalContext);
        logger.trace("inject() exit");
    }

    public void postConstruct(T instance) {
        delegate.postConstruct(instance);
    }

    public void preDestroy(T instance) {
        delegate.preDestroy(instance);
    }

    public void dispose(T instance) {
        delegate.dispose(instance);
    }

    public Set<InjectionPoint> getInjectionPoints() {
        return delegate.getInjectionPoints();
    }

    public T produce(CreationalContext<T> creationalContext) {
        return delegate.produce(creationalContext);
    }
}
