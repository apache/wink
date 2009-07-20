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

package org.apache.wink.spring.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.apache.wink.spring.Registrar;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Registers the registrars. First registras are added to the registrar' list,
 * and on ContextRefreshedEvent they all are registered.
 */
public class RegistryPostProcessor implements BeanPostProcessor, ApplicationListener {

    private ProvidersRegistry providersRegistry;
    private ResourceRegistry  resourceRegistry;

    private List<Registrar>   registrars = new ArrayList<Registrar>();

    /*
     * (non-Javadoc)
     * @seeorg.springframework.beans.factory.config.BeanPostProcessor#
     * postProcessAfterInitialization(java.lang.Object, java.lang.String)
     */
    public Object postProcessAfterInitialization(Object bean, String beanName)
        throws BeansException {
        if (bean instanceof Registrar) {
            // save registrars to register them at the end of context loading
            Registrar registrar = (Registrar)bean;
            registrars.add(registrar);
        }
        return bean;
    }

    /*
     * (non-Javadoc)
     * @seeorg.springframework.beans.factory.config.BeanPostProcessor#
     * postProcessBeforeInitialization(java.lang.Object, java.lang.String)
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName)
        throws BeansException {
        return bean;
    }

    /**
     * invoked on events
     */
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            // register all registrars after the application context was loaded
            for (Registrar registrar : registrars) {
                registrar.register(resourceRegistry, providersRegistry);
            }
        }
    }

    public void setResourceRegistry(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    public void setProvidersRegistry(ProvidersRegistry providersStore) {
        this.providersRegistry = providersStore;
    }
}
