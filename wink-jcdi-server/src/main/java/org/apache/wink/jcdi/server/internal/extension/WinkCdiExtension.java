/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.wink.jcdi.server.internal.extension;

import org.apache.wink.common.internal.registry.metadata.ApplicationMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.apache.wink.jcdi.server.internal.DefaultBeanManagerResolver;
import org.apache.wink.jcdi.server.internal.util.CdiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.inject.Singleton;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class WinkCdiExtension implements Extension {
    private static final Logger logger = LoggerFactory.getLogger(WinkCdiExtension.class);

    //don't use a static list - an extension instance gets created per application
    private List<Class> beanClassesToCheck = new ArrayList<Class>();

    @SuppressWarnings("UnusedDeclaration")
    public <T> void observeProcessInjectionTarget(@Observes ProcessInjectionTarget<T> pij) {
        logger.trace("observeProcessInjectionTarget({}) entry", pij);

        Annotation[] annotations = pij.getAnnotatedType().getAnnotations()
                .toArray(new Annotation[pij.getAnnotatedType().getAnnotations().size()]);

        if (isJAXRSBean(pij.getAnnotatedType().getJavaClass())) {
            logger.trace("Was JAX-RS annotated class so changing the injection target");
            pij.setInjectionTarget(new WinkInjectionTarget<T>(pij.getInjectionTarget()));
        }
        logger.trace("observeProcessInjectionTarget() exit");
    }

    @SuppressWarnings("UnusedDeclaration")
    public void findJaxRsClasses(@Observes ProcessAnnotatedType<?> processAnnotatedType) {
        Class<?> beanClass = processAnnotatedType.getAnnotatedType().getJavaClass();

        if (ProviderMetadataCollector.isProvider(beanClass) || ApplicationMetadataCollector.isApplication(beanClass)
                || ResourceMetadataCollector.isResource(beanClass)) {
            beanClassesToCheck.add(beanClass);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void validateJaxRsCdiBeans(
            @Observes AfterDeploymentValidation afterDeploymentValidation, BeanManager beanManager) {
        DefaultBeanManagerResolver.setBeanManager(beanManager);

        for (Class beanClass : beanClassesToCheck) {
            Bean<?> bean = CdiUtils.getBeanFor(beanClass, beanManager);

            if (bean == null) {
                continue; //e.g. a vetoed bean
            }

            if (ResourceMetadataCollector.isResource(beanClass) && Dependent.class.equals(bean.getScope())) {
                StringBuilder warning = new StringBuilder("{} gets processed as CDI bean, but uses {} ");

                if (CdiUtils.isBeanWithScope(beanClass.getAnnotations(), beanManager)) {
                    warning.append("explicitly. ");
                } else {
                    warning.append("implicitly. ");
                }
                warning.append("The suggested scope for this bean is {}. Alternatively use ");
                warning.append(OptionalScopeAutoUpgradeExtension.class.getName());
                Object[] parameters = new Object[]{
                        beanClass.getName(), Dependent.class.getName(), RequestScoped.class.getName()};
                logger.warn(warning.toString(), parameters);
                continue;
            }

            //an extension can change the scope dynamically during bootstrapping -> check it at the end
            if (!bean.getScope().equals(Singleton.class) && !beanManager.isNormalScope(bean.getScope())) {
                StringBuilder warning = new StringBuilder();
                if (ApplicationMetadataCollector.isApplication(beanClass)) {
                    warning.append("Implementations of ").append(Application.class.getName());
                } else {
                    warning.append(Provider.class.getName()).append(" beans");
                }
                warning.append(" are only compatible with singletons. Please add ");
                warning.append(OptionalScopeAutoUpgradeExtension.class.getName());
                warning.append(" Or use ");
                warning.append(ApplicationScoped.class.getName());
                warning.append(" or ");
                warning.append(Singleton.class.getName());
                warning.append(" or a custom scope which keeps only one instance per class for the whole application.");
                logger.warn(warning.toString());
            }
        }

        beanClassesToCheck.clear();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onShutdown(@Observes BeforeShutdown beforeShutdown) {
        DefaultBeanManagerResolver.reset();
    }

    private static boolean isJAXRSBean(final Class<?> cls) {
        if (logger.isTraceEnabled()) {
            logger.trace("isJAXRSBean({}) entry", cls.getName());
        }
        boolean result = false;
        if (ProviderMetadataCollector.isProvider(cls)) {
            result = true;
        } else if (ResourceMetadataCollector.isResource(cls)) {
            result = true;
        } else if (ApplicationMetadataCollector.isApplication(cls)) {
            result = true;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("isJAXRSBean({}) exit", result);
        }
        return result;
    }
}
