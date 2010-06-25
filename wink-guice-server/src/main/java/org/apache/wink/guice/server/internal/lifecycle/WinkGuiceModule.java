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

package org.apache.wink.guice.server.internal.lifecycle;

import java.io.IOException;
import java.security.PrivilegedActionException;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.CreationUtils;
import org.apache.wink.common.internal.lifecycle.ObjectCreationException;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class WinkGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bindListener(Matchers.any(), new TypeListener() {

            public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> encounter) {
                ClassMetadata classMetaData = collectClassMetadata(typeLiteral.getRawType());
                if (classMetaData == null) {
                    return;
                }
                if (classMetaData.getInjectableFields().size() > 0) {
                    encounter.register(new JAXRSInjector<I>(classMetaData));
                }
            }
        });
    }

    static class JAXRSInjector<T> implements MembersInjector<T> {
        private final Logger        logger = LoggerFactory.getLogger(JAXRSInjector.class);

        private final ClassMetadata classMetaData;

        public JAXRSInjector(ClassMetadata classMetaData) {
            this.classMetaData = classMetaData;
        }

        public void injectMembers(T instance) {
            try {
                CreationUtils.injectFields(instance, classMetaData, RuntimeContextTLS
                    .getRuntimeContext());
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(Messages.getMessage("injectionFailureSingleton", instance
                        .getClass().getName()));
                }
                throw new ObjectCreationException(e);
            } catch (PrivilegedActionException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(Messages.getMessage("injectionFailureSingleton", instance
                        .getClass().getName()));
                }
                throw new ObjectCreationException(e);
            }
        }
    }

    private static <T> ClassMetadata collectClassMetadata(final Class<T> cls) {
        ClassMetadata classMetadata = null;
        if (ProviderMetadataCollector.isProvider(cls)) {
            classMetadata = ProviderMetadataCollector.collectMetadata(cls);
        } else if (ResourceMetadataCollector.isResource(cls)) {
            classMetadata = ResourceMetadataCollector.collectMetadata(cls);
        }

        return classMetadata;
    }

}
