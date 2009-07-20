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

import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Each time a new bean is created, it's checked if it's Resource, Provider or
 * Dynamic Resource. If this is the case, it's registered in
 * SpringLifecycleManager. This bean applies only during the context loading and
 * doesn't effects the beans created in runtime.
 */
public class LifecycleManagerPostProcessor implements BeanPostProcessor, ApplicationContextAware,
    ApplicationListener {

    private ApplicationContext        applicationContext;
    private SpringLifecycleManager<?> springOFFactory;
    private boolean                   loadingOfContextCompleted = false;

    public Object postProcessAfterInitialization(final Object bean, final String beanName)
        throws BeansException {

        if (!loadingOfContextCompleted && applicationContext.containsBean(beanName)) {
            // during the context loading, process providers and resources beans
            // in order to set their scope

            @SuppressWarnings("unchecked")
            final Class<Object> cls = (Class<Object>)bean.getClass();

            if (ResourceMetadataCollector.isStaticResource(cls)) {
                springOFFactory
                    .addResourceOrProvider(bean,
                                           beanName,
                                           new SpringObjectFactory(applicationContext, beanName,
                                                                   ResourceMetadataCollector
                                                                       .collectMetadata(cls)));
            } else if (ProviderMetadataCollector.isProvider(cls)) {
                springOFFactory
                    .addResourceOrProvider(bean,
                                           beanName,
                                           new SpringObjectFactory(applicationContext, beanName,
                                                                   ProviderMetadataCollector
                                                                       .collectMetadata(cls)));
            } else if (ResourceMetadataCollector.isDynamicResource(cls)) {
                springOFFactory
                    .addDynamicResource(bean,
                                        beanName,
                                        new SpringObjectFactory(applicationContext, beanName,
                                                                ResourceMetadataCollector
                                                                    .collectMetadata(cls)));
            }
        }

        return bean;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName)
        throws BeansException {
        return bean;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setSpringOFFactory(SpringLifecycleManager<?> springOFFactory) {
        this.springOFFactory = springOFFactory;
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            loadingOfContextCompleted = true;
        }
    }

}
