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
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.apache.wink.server.internal.registry.ResourceRegistry;


/**
 * Processes the Application object. First singletons are processed and later
 * classes. If the provided Application extends WinkApplication, instances
 * are also processed AFTER the singletons and the classes.
 * <p>
 * Pay Attention that classes returned by getSingletons are ignored by both
 * getClasses and getInstances. And classes returned by getClasses are ignored
 * by getInstances.
 * 
 * @see Application
 * @see WinkApplication
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
        logger.debug("Processing Application:");

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

        if (application instanceof WinkApplication) {
            processWinkApplication((WinkApplication) application);
        }

        logger.debug("Processing of Application completed.");
    }

    private void processWinkApplication(WinkApplication sApplication) {
        Set<Object> instances = sApplication.getInstances();
        double priority = sApplication.getPriority();

        if (instances == null) {
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
                    logger.warn("Cannot handle {}. Ignoring.", obj);
                }
            } catch (Exception e) {
                logger.warn("The following exception occured during processing of instance {}. Ignoring.", obj.getClass().getCanonicalName());
                e.printStackTrace();
            }
        }
    }

    private void processClasses(Set<Class<?>> classes) {

        for (Class<?> cls : classes) {
            try {
                logger.debug("Processing class: {}", cls);
    
                // the validations were moved to registry
    
                if (ResourceMetadataCollector.isStaticResource(cls)) {
                    resourceRegistry.addResource(cls);
                } else if (ProviderMetadataCollector.isProvider(cls)) {
                    providersRegistry.addProvider(cls);
                } else {
                    logger.warn("{} is not a resource or a provider. Ignored.", cls);
                }
            } catch (Exception e) {
                logger.warn("The following exception occured during processing of class {}. Ignoring.", cls);
                e.printStackTrace();
            }
        }
    }

    private void processSingletons(Set<Object> singletons) {

        // add singletons
        for (Object obj : singletons) {
            try {
                logger.debug("Processing singleton: {}", obj);
    
                Class<?> cls = obj.getClass();
    
                if (ResourceMetadataCollector.isStaticResource(cls)) {
                    resourceRegistry.addResource(obj);
                } else if (ProviderMetadataCollector.isProvider(cls)) {
                    providersRegistry.addProvider(obj);
                } else {
                    logger.warn("{} is not a resource or a provider. Ignoring.", obj);
                }
            } catch (Exception e) {
                logger.warn("The following exception occured during processing of singleton {}. Ignoring.", obj.getClass().getCanonicalName());
                e.printStackTrace();
            }
        }
    }

}
