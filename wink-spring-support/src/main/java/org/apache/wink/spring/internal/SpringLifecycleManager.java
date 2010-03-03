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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.DynamicResource;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.LifecycleManager;
import org.apache.wink.common.internal.lifecycle.ObjectCreationException;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;

public class SpringLifecycleManager<T> implements LifecycleManager<T> {

    private static final Logger                logger        =
                                                                 LoggerFactory
                                                                     .getLogger(SpringLifecycleManager.class);

    /**
     * holds Static Resources and Providers
     */
    private Map<Class<?>, SpringObjectFactory> class2factory =
                                                                 new HashMap<Class<?>, SpringObjectFactory>();

    /**
     * holds Dynamic Resources
     */
    private Map<String, SpringObjectFactory>   id2factory    =
                                                                 new HashMap<String, SpringObjectFactory>();

    @SuppressWarnings("unchecked")
    public ObjectFactory<T> createObjectFactory(T object) throws ObjectCreationException {
        Class<? extends Object> cls = object.getClass();
        if (ResourceMetadataCollector.isStaticResource(cls) || ProviderMetadataCollector
            .isProvider(cls)) {
            return (ObjectFactory<T>)class2factory.get(cls);
        }
        if (ResourceMetadataCollector.isDynamicResource(cls)) {
            DynamicResource dynResource = (DynamicResource)object;
            String beanName = dynResource.getBeanName();
            return (ObjectFactory<T>)id2factory.get(beanName);
        }
        return null;
    }

    public SpringObjectFactory getSpringObjectFactory(T object, String beanName) {
        Class<? extends Object> cls = object.getClass();
        if (ResourceMetadataCollector.isStaticResource(cls) || ProviderMetadataCollector
            .isProvider(cls)) {
            return class2factory.get(cls);
        }
        if (ResourceMetadataCollector.isDynamicResource(cls)) {
            return id2factory.get(beanName);
        }
        return null;
    }

    public ObjectFactory<T> createObjectFactory(Class<T> object) throws ObjectCreationException {
        // Spring will cannot find Object Factory based on class
        return null;
    }

    public void addResourceOrProvider(Object bean,
                                      String beanName,
                                      SpringObjectFactory objectFactory) {
        Class<? extends Object> cls = bean.getClass();
        if (ResourceMetadataCollector.isStaticResource(cls) || ProviderMetadataCollector
            .isProvider(cls)) {
            SpringObjectFactory old = class2factory.put(cls, objectFactory);
            if (old != null) {
                logger.warn(Messages.getMessage("springClassReplaceNewerObjectFactory", cls)); //$NON-NLS-1$
            }
        } else {
            logger.warn(Messages.getMessage("springBeanNotResourceNorProvider", beanName, cls)); //$NON-NLS-1$
        }
    }

    public void addDynamicResource(Object bean, String beanName, SpringObjectFactory objectFactory) {
        Class<? extends Object> cls = bean.getClass();
        if (ResourceMetadataCollector.isDynamicResource(cls)) {
            DynamicResource dynResource = (DynamicResource)bean;
            dynResource.setBeanName(beanName);
            SpringObjectFactory old = id2factory.put(beanName, objectFactory);
            if (old != null) {
                logger.warn(Messages.getMessage("springClassReplaceNewerObjectFactory", beanName)); //$NON-NLS-1$
            }
        } else {
            logger.warn(Messages.getMessage("springBeanClassNotDynamicResource", beanName, cls)); //$NON-NLS-1$
        }
    }

}
