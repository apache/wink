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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 */
package org.apache.wink.jcdi.server.internal.lifecycle;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.ObjectCreationException;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCDISingletonObjectFactory<T> implements ObjectFactory<T> {

    private final Logger               logger     =
                                                      LoggerFactory
                                                          .getLogger(JCDISingletonObjectFactory.class);

    final private T                    instance;

    final private CreationalContext<?> creationalContext;

    private boolean                    isReleased = false;

    @SuppressWarnings("unchecked")
    public JCDISingletonObjectFactory(Class<T> clazz, BeanManager beanManager) {
        Annotation[] annotations = clazz.getAnnotations();
        List<Annotation> qualifierAnnotations = new ArrayList<Annotation>(1);
        for (Annotation a : annotations) {
            if (beanManager.isQualifier(a.annotationType())) {
                qualifierAnnotations.add(a);
            }
        }
        Set<Bean<?>> beans =
            beanManager.getBeans(clazz, qualifierAnnotations.toArray(new Annotation[0]));
        Bean theBean = beans.iterator().next();

        creationalContext = beanManager.createCreationalContext(theBean);
        try {
            instance = (T)beanManager.getReference(theBean, clazz, creationalContext);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(Messages
                    .getMessage("jcdiSingletonObjectFactoryCannotInstantiateInstance", clazz
                        .getName()), e);
            }
            throw new ObjectCreationException(e);
        }
    }

    public T getInstance(RuntimeContext context) {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getInstanceClass() {
        return (Class<T>)instance.getClass();
    }

    public void releaseAll(RuntimeContext context) {
        if (isReleased) {
            /* already released so return */
            return;
        }
        isReleased = true;
        if (creationalContext != null) {
            creationalContext.release();

        }
    }

    public void releaseInstance(T instance, RuntimeContext context) {
        /* do nothing */
    }

}
