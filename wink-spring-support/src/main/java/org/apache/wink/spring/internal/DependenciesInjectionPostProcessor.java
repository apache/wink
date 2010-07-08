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

import javax.ws.rs.WebApplicationException;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.CreationUtils;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class DependenciesInjectionPostProcessor implements BeanPostProcessor {

    private static final Logger            logger =
                                                      LoggerFactory
                                                          .getLogger(DependenciesInjectionPostProcessor.class);
    private SpringLifecycleManager<Object> springOFFactory;

    public Object postProcessAfterInitialization(Object bean, String beanName)
        throws BeansException {
        SpringObjectFactory springObjectFactory =
            springOFFactory.getSpringObjectFactory(bean, beanName);
        if (springObjectFactory != null) {
            ClassMetadata classMetadata = springObjectFactory.getClassMetadata();
            try {
                CreationUtils.injectFields(bean, classMetadata, RuntimeContextTLS
                    .getRuntimeContext());
            } catch (Exception e) {
                logger.error(Messages.getMessage("springExceptionOccurredDuringFieldInject"), //$NON-NLS-1$
                             e);
                throw new WebApplicationException(e);
            }
        }
        return bean;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName)
        throws BeansException {

        return bean;
    }

    public void setSpringOFFactory(SpringLifecycleManager<Object> springOFFactory) {
        this.springOFFactory = springOFFactory;
    }

}
