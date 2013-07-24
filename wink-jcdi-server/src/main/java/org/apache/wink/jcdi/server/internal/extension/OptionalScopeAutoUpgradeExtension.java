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
import org.apache.wink.jcdi.server.internal.literal.ApplicationScopedLiteral;
import org.apache.wink.jcdi.server.internal.literal.RequestScopedLiteral;
import org.apache.wink.jcdi.server.internal.util.CdiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.annotation.Annotation;

/**
 * Adds scopes to CDI/JAX-RS beans, if they don't have one.
 * Resource -> @RequestScoped
 * Provider and Application -> @ApplicationScoped
 */
public class OptionalScopeAutoUpgradeExtension implements Extension {
    private static final Logger logger = LoggerFactory.getLogger(OptionalScopeAutoUpgradeExtension.class);

    @SuppressWarnings("UnusedDeclaration")
    public void findProviderAndApplicationBeans(@Observes ProcessAnnotatedType<?> pat, BeanManager beanManager) {
        Class<?> beanClass = pat.getAnnotatedType().getJavaClass();

        Annotation[] annotations = pat.getAnnotatedType().getAnnotations()
                .toArray(new Annotation[pat.getAnnotatedType().getAnnotations().size()]);

        if (ResourceMetadataCollector.isResource(beanClass) && !CdiUtils.isBeanWithScope(annotations, beanManager)) {
            logger.trace("{} upgraded to {} scoped bean.", beanClass.getName(), RequestScoped.class.getName());
            pat.setAnnotatedType(new WinkAnnotatedTypeWrapper(pat.getAnnotatedType(), new RequestScopedLiteral()));
        } else if ((ProviderMetadataCollector.isProvider(beanClass) ||
                ApplicationMetadataCollector.isApplication(beanClass)) &&
                !CdiUtils.isBeanWithScope(annotations, beanManager)) {
            logger.trace("{} upgraded to {} scoped bean.", beanClass.getName(), ApplicationScoped.class.getName());
            pat.setAnnotatedType(new WinkAnnotatedTypeWrapper(pat.getAnnotatedType(), new ApplicationScopedLiteral()));
        }
    }
}
