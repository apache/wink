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
package org.apache.wink.jcdi.server.internal;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.IdentityHashMap;
import java.util.Map;

public class CdiAwareObjectFactory<T> implements ObjectFactory<T> {
    final private Class<T> clazz;
    final private BeanManager beanManager;
    private Bean<?> theBean;
    private IdentityHashMap<T, CreationalContext<T>> dependentCreationalContextMap;

    public CdiAwareObjectFactory(Class<T> c, BeanManager beanManager, Bean<T> bean) {
        //TODO discuss the support of producer methods
        this.clazz = c;
        this.beanManager = beanManager;
        theBean = bean;
    }

    @SuppressWarnings("unchecked")
    public T getInstance(RuntimeContext context) {
        CreationalContext<?> creationalContext = beanManager.createCreationalContext(theBean);
        T instance = (T) beanManager.getReference(theBean, clazz, creationalContext);
        //it's only allowed to manage the lifecycle of dependent-scoped beans manually
        if (!theBean.getScope().equals(Dependent.class)) {
            return instance; //contextual-reference
        }

        /*
         * if there is a context, this is during a request. if context is null,
         * this is during application scoped. store the CreationalContext to
         * release it later.
         */
        if (context != null) {
            context.setAttribute(CreationalContext.class, creationalContext);
        } else {
            /*
             * be sure to synchronize on the off chance this is run concurrently
             * (in normal operation, the application scoped instances are
             * created sequentially but that could change
             */
            synchronized (this) {
                if (dependentCreationalContextMap == null) {
                    dependentCreationalContextMap = new IdentityHashMap<T, CreationalContext<T>>();
                }
                dependentCreationalContextMap.put(instance, (CreationalContext<T>) creationalContext);
            }
        }
        return instance;
    }

    public Class<T> getInstanceClass() {
        return clazz;
    }

    public void releaseAll(RuntimeContext context) {
        synchronized (this) {
            if (dependentCreationalContextMap != null) {
                for (Map.Entry<T, CreationalContext<T>> dependentBeanEntry : dependentCreationalContextMap.entrySet()) {
                    ((Bean<T>) theBean).destroy(dependentBeanEntry.getKey(), dependentBeanEntry.getValue());
                }
                dependentCreationalContextMap = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void releaseInstance(T instance, RuntimeContext context) {
        if (context != null) //mainly destroys dependent instances at the end of the request
        {
            CreationalContext<T> creationalContext =
                    (CreationalContext<T>) context.getAttributes().remove(CreationalContext.class.getName());
            if (creationalContext != null) {
                ((Bean<T>) theBean).destroy(instance, creationalContext);
            }
        }
    }
}
