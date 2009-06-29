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
import org.apache.wink.common.SymphonyApplication;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.apache.wink.server.internal.registry.ResourceRegistry;


/**
 * Processes the Application object. First singletons are processed and later
 * classes. If the provided Application extends SymphonyApplication, instances
 * are also processed AFTER the singletons and the classes.
 * <p>
 * Pay Attention that classes returned by getSingletons are ignored by both
 * getClasses and getInstances. And classes returned by getClasses are ignored
 * by getInstances.
 * 
 * @see Application
 * @see SymphonyApplication
 */
public class ApplicationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationProcessor.class);

    private final Application       application;
    private final ResourceRegistry  resourceRegistry;
    private final ProvidersRegistry providersRegistry;

    public ApplicationProcessor(Application application, ResourceRegistry resourceRegistry,
        ProvidersRegistry providersRegistry) {
        super();
        this.application = application;
        this.resourceRegistry = resourceRegistry;
        this.providersRegistry = providersRegistry;
    }

    public void process() {

        if (logger.isDebugEnabled()) {
            logger.debug("Processing Application:");
        }

        // process singletons
        Set<Object> singletons = application.getSingletons();
        if (singletons != null && singletons.size() > 0) {
            processSingletons(singletons);
        }

        // process classes
        Set<Class<?>> classes = application.getClasses();
        if (classes != null && classes.size() > 0) {
            processClasses(classes);
        }

        if (application instanceof SymphonyApplication) {
            processSymphonyApplication((SymphonyApplication) application);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Processing of Application completed.");
        }

    }

    private void processSymphonyApplication(SymphonyApplication sApplication) {
        Set<Object> instances = sApplication.getInstances();
        double priority = sApplication.getPriority();

        if (instances == null) {
            return;
        }

        for (Object obj : instances) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Processing instance: %s", String.valueOf(obj)));
            }

            Class<?> cls = obj.getClass();

            // the validations were moved to registry

            if (ResourceMetadataCollector.isDynamicResource(cls)) {
                resourceRegistry.addResource(obj, priority);
            } else if (ResourceMetadataCollector.isStaticResource(cls)) {
                resourceRegistry.addResource(obj, priority);
            } else if (ProviderMetadataCollector.isProvider(cls)) {
                providersRegistry.addProvider(obj, priority);
            } else {
                logger.warn(String.format("Cannot handle %s. Ignoring.", String.valueOf(obj)));
            }
        }
    }

    private void processClasses(Set<Class<?>> classes) {

        for (Class<?> cls : classes) {

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Processing class: %s", String.valueOf(cls)));
            }

            // the validations were moved to registry

            if (ResourceMetadataCollector.isStaticResource(cls)) {
                resourceRegistry.addResource(cls);
            } else if (ProviderMetadataCollector.isProvider(cls)) {
                providersRegistry.addProvider(cls);
            } else {
                logger.warn(String.format("%s is not a resource or a provider. Ignored.",
                    String.valueOf(cls)));
            }
        }
    }

    private void processSingletons(Set<Object> singletons) {

        // add singletons
        for (Object obj : singletons) {

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Processing singleton: %s", String.valueOf(obj)));
            }

            Class<?> cls = obj.getClass();

            if (ResourceMetadataCollector.isStaticResource(cls)) {
                resourceRegistry.addResource(obj);
            } else if (ProviderMetadataCollector.isProvider(cls)) {
                providersRegistry.addProvider(obj);
            } else {
                logger.warn(String.format("%s is not a resource or a provider. Ignoring.",
                    String.valueOf(obj)));
            }
        }
    }

}
