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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;

public class JCDIDefaultObjectFactory<T> implements ObjectFactory<T> {

    final private Class<T>                           clazz;
    final private BeanManager                        beanManager;
    private Bean<?>                                  theBean;
    private IdentityHashMap<T, CreationalContext<T>> creationalContextMap;

    public JCDIDefaultObjectFactory(Class<T> c, BeanManager beanManager) {
        this.clazz = c;
        this.beanManager = beanManager;
        // this.creationalContextMap = new IdentityHashMap<T,
        // CreationalContext<T>>();
        theBean = null;
    }

    /* package */IdentityHashMap<T, CreationalContext<T>> getCreationalContextMap() {
        return creationalContextMap;
    }

    @SuppressWarnings("unchecked")
    public T getInstance(RuntimeContext context) {
        if (theBean == null) {
            // cache the Bean object
            Annotation[] annotations = clazz.getAnnotations();
            List<Annotation> qualifierAnnotations = new ArrayList<Annotation>(1);
            for (Annotation a : annotations) {
                if (beanManager.isQualifier(a.annotationType())) {
                    qualifierAnnotations.add(a);
                }
            }
            Set<Bean<?>> beans =
                beanManager.getBeans(clazz, qualifierAnnotations.toArray(new Annotation[0]));
            theBean = beans.iterator().next();
        }

        /*
         * need a new CreationalContext every "new" instance and use it to get
         * the bean instance
         */
        CreationalContext<?> creationalContext = beanManager.createCreationalContext(theBean);
        T instance = (T)beanManager.getReference(theBean, clazz, creationalContext);

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
                if (creationalContextMap == null) {
                    creationalContextMap = new IdentityHashMap<T, CreationalContext<T>>();
                }
                creationalContextMap.put(instance, (CreationalContext<T>)creationalContext);
            }
        }
        return instance;
    }

    public Class<T> getInstanceClass() {
        return clazz;
    }

    public void releaseAll(RuntimeContext context) {
        synchronized (this) {
            if (creationalContextMap != null) {
                for (CreationalContext<T> c : creationalContextMap.values()) {
                    c.release();
                }
                creationalContextMap = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void releaseInstance(T instance, RuntimeContext context) {
        if (context != null) {
            CreationalContext<T> creationalContext =
                (CreationalContext<T>)context.getAttributes().remove(CreationalContext.class
                    .getName());
            if (creationalContext != null) {
                creationalContext.release();
            }
        }
// /* will this code ever run? */
//        if (creationalContextMap != null) {
//            synchronized (creationalContextMap) {
//                CreationalContext<T> creationalContext =
//                    (CreationalContext<T>)creationalContextMap.remove(instance);
//                if (creationalContext != null) {
//                    creationalContext.release();
//                }
//            }
//        }
    }

}
