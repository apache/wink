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
package org.apache.wink.jcdi.server.internal.lifecycle;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.LifecycleManager;
import org.apache.wink.common.internal.lifecycle.ObjectCreationException;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.common.internal.registry.metadata.ApplicationMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCDILifecycleManager<T> implements LifecycleManager<T> {

    public JCDILifecycleManager() {
        logger.trace("JCDILifecycleManager created");
    }

    private static final Logger logger      = LoggerFactory.getLogger(JCDILifecycleManager.class);

    private BeanManager         beanManager = null;

    void setBeanManager(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    BeanManager getBeanManager() {
        if (beanManager == null) {
            try {
                InitialContext initialContext = new InitialContext();
                beanManager = (BeanManager)initialContext.lookup("java:comp/BeanManager");
            } catch (NamingException e) {
                logger.warn(Messages.getMessage("couldNotFindBeanManager"), e);
            }
        }
        return beanManager;
    }

    public ObjectFactory<T> createObjectFactory(T object) throws ObjectCreationException {
        logger.trace("createObjectFactory({}) entry", object);
        if (object == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "object")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        logger.trace("createObjectFactory() exit returning null");
        return null;
    }

    public ObjectFactory<T> createObjectFactory(Class<T> cls) throws ObjectCreationException {
        logger.trace("createObjectFactory({}) entry", cls);
        if (cls == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "cls")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        BeanManager beanManager = getBeanManager();
        if (isJCDIManagedBean(cls, beanManager)) {
            if (ProviderMetadataCollector.isProvider(cls) || ApplicationMetadataCollector
                .isApplication(cls)) {
                /*
                 * for providers and application sub-classes, they must be
                 * singletons, so use a special caching singleton object factory
                 * for them. also allows error messages to be handled.
                 */
                logger.trace("createObjectFactory() returning JCDISingletonObjectFactory");
                return new JCDISingletonObjectFactory<T>(cls, beanManager);
            }
            logger.trace("createObjectFactory() returning JCDIDefaultObjectFactory");
            return new JCDIDefaultObjectFactory<T>(cls, beanManager);
        }

        logger.trace("createObjectFactory() returning null");
        return null;
    }

    /**
     * Determines if a class is a JCDI managed bean or not.
     * 
     * @param <T>
     * @param cls
     * @return true if the class is a JCDI managed bean, false if not
     */
    static <T> boolean isJCDIManagedBean(Class<T> cls, BeanManager beanManager) {
        logger.trace("isJCDIManagedBean({}, {}) entry", cls, beanManager);
        Annotation[] annotations = cls.getAnnotations();
        List<Annotation> qualifierAnnotations = new ArrayList<Annotation>(1);
        for (Annotation a : annotations) {
            if (beanManager.isQualifier(a.annotationType())) {
                qualifierAnnotations.add(a);
            }
        }
        logger.trace("Qualifier annotations are {}", qualifierAnnotations);
        Set<Bean<?>> beans =
            beanManager.getBeans(cls, qualifierAnnotations.toArray(new Annotation[0]));
        logger.trace("Beans are {}", beans);
        if (beans == null || beans.isEmpty()) {
            logger
                .debug("{} is NOT a JCDI managed bean.",
                       cls);
            logger.trace("isJCDIManagedBean() returning false");
            return false;
        }
        logger.debug("{} is a JCDI managed bean.", cls);

        logger.trace("isJCDIManagedBean() returning true");
        return true;
    }

}
