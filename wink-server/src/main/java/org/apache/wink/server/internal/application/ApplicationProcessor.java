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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.WinkApplication;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.apache.wink.server.internal.registry.ResourceRegistry;

/**
 * Processes the Application object. First singletons are processed and later
 * classes. If the provided Application extends WinkApplication, instances are
 * also processed AFTER the singletons and the classes.
 * <p>
 * Pay Attention that classes returned by getSingletons are ignored by both
 * getClasses and getInstances. And classes returned by getClasses are ignored
 * by getInstances.
 * 
 * @see Application
 * @see WinkApplication
 */
public class ApplicationProcessor {

    private static final Logger     logger = LoggerFactory.getLogger(ApplicationProcessor.class);

    private final Application       application;
    private final ResourceRegistry  resourceRegistry;
    private final ProvidersRegistry providersRegistry;

    public ApplicationProcessor(Application application,
                                ResourceRegistry resourceRegistry,
                                ProvidersRegistry providersRegistry) {
        super();
        this.application = application;
        this.resourceRegistry = resourceRegistry;
        this.providersRegistry = providersRegistry;
    }

    public void process() {
        logger.debug("Processing Application: {}", application);

        double priority = WinkApplication.DEFAULT_PRIORITY;
        if (application instanceof WinkApplication) {
            priority = ((WinkApplication)application).getPriority();
            logger.debug("WinkApplication priority is set to: {}", priority);
        }

        // process singletons
        Set<Object> singletons = application.getSingletons();
        if (singletons != null && singletons.size() > 0) {
            processSingletons(singletons, priority);
        }

        // process classes
        Set<Class<?>> classes = application.getClasses();
        if (classes != null && classes.size() > 0) {
            processClasses(classes, priority);
        }

        if (application instanceof WinkApplication) {
            processWinkApplication((WinkApplication)application);
        }

        logger.debug("Processing of Application completed.");
    }

    private void processWinkApplication(WinkApplication sApplication) {
        Set<Object> instances = sApplication.getInstances();
        double priority = sApplication.getPriority();

        if (instances == null) {
            logger.debug("WinkApplication.getInstances() returned null");
            return;
        }

        for (Object obj : instances) {
            try {
                logger.debug("Processing instance: {}", obj);

                Class<?> cls = obj.getClass();

                // the validations were moved to registry

                if (ResourceMetadataCollector.isDynamicResource(cls)) {
                    resourceRegistry.addResource(obj, priority);
                } else if (ResourceMetadataCollector.isStaticResource(cls)) {
                    resourceRegistry.addResource(obj, priority);
                } else if (ProviderMetadataCollector.isProvider(cls)) {
                    providersRegistry.addProvider(obj, priority);
                } else {
                    logger.warn(Messages
                        .getMessage("classNotADynamicResourceNorResourceNorProvider"), obj);
                }
            } catch (Exception e) {
                logger.warn(Messages.getMessage("exceptionOccurredDuringInstanceProcessing"), obj
                    .getClass().getCanonicalName());
                logger.warn(Messages.getMessage("listExceptionDuringInstanceProcessing"), e);
            }
        }
    }

    private void processClasses(Set<Class<?>> classes, double priority) {

        for (Class<?> cls : classes) {
            try {
                logger.debug("Processing class: {}", cls);

                // the validations were moved to registry

                if (ResourceMetadataCollector.isStaticResource(cls)) {
                    resourceRegistry.addResource(cls, priority);
                } else if (ProviderMetadataCollector.isProvider(cls)) {
                    providersRegistry.addProvider(cls, priority);
                } else {
                    logger.warn(Messages.getMessage("classNotAResourceNorProvider"), cls);
                }
            } catch (Exception e) {
                logger.warn(Messages.getMessage("exceptionOccurredDuringClassProcessing"), cls);
                logger.warn(Messages.getMessage("listExceptionDuringClassProcessing"), e);
            }
        }
    }

    private void processSingletons(Set<Object> singletons, double priority) {

        // add singletons
        for (Object obj : singletons) {
            try {
                logger.debug("Processing singleton: {}", obj);

                Class<?> cls = obj.getClass();

                if (ResourceMetadataCollector.isStaticResource(cls)) {
                    resourceRegistry.addResource(obj, priority);
                } else if (ProviderMetadataCollector.isProvider(cls)) {
                    providersRegistry.addProvider(obj, priority);
                } else {
                    logger.warn(Messages.getMessage("classNotAResourceNorProvider"), obj);
                }
            } catch (Exception e) {
                logger.warn(Messages.getMessage("exceptionOccurredDuringSingletonProcessing"), obj
                    .getClass().getCanonicalName());
                logger.warn(Messages.getMessage("listExceptionDuringSingletonProcessing"), e);
            }
        }
    }

}
