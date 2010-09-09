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
package org.apache.wink.jcdi.server.internal.extension;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import org.apache.wink.common.internal.registry.metadata.ApplicationMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCDIExtension implements Extension {

    private static final Logger logger = LoggerFactory.getLogger(JCDIExtension.class);

    public <T> void observeProcessInjectionTarget(@Observes ProcessInjectionTarget<T> pij) {
        logger.trace("observeProcessInjectionTarget({}) entry", pij);
        if (isJAXRSBean(pij.getAnnotatedType().getJavaClass())) {
            logger.trace("Was JAX-RS annotated class so changing the injection target");
            pij.setInjectionTarget(new JAXRSJCDICustomInjectionTarget<T>(pij.getInjectionTarget()));
        }
        logger.trace("observeProcessInjectionTarget() exit");
    }

    static boolean isJAXRSBean(final Class<?> cls) {
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
