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
package org.apache.wink.jcdi.server.internal.util;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.util.*;

//abstract class won't end up as cdi-bean
public abstract class CdiUtils {
    private CdiUtils() {
        // prevent instantiation
    }

    public static <T> Bean<T> getBeanFor(Class<T> beanClass, BeanManager beanManager) {
        Annotation[] qualifiers = getClassLevelQualifiers(beanClass, beanManager); //producers aren't supported currently
        Set<Bean<?>> beans = beanManager.getBeans(beanClass, qualifiers);

        if (beans == null || beans.isEmpty()) {
            return null;
        }

        Bean<?> result;

        try {
            result = beanManager.resolve(beans);
        } catch (AmbiguousResolutionException e) {
            //try it again with concrete class in case of resource-sub-classes
            beans = new HashSet<Bean<?>>(beans);
            Iterator<Bean<?>> beanIterator = beans.iterator();

            while (beanIterator.hasNext()) {
                Bean<?> bean = beanIterator.next();

                if (bean.isAlternative()) {
                    continue; //don't drop alternatives
                }

                if (bean.getBeanClass().isAnnotationPresent(Specializes.class)) {
                    continue; //don't drop specialized beans
                }

                if (beanClass.equals(bean.getBeanClass())) {
                    //in case of a prev. AmbiguousResolutionException we have to find the same class
                    beanIterator.remove();
                }
            }
            result = beanManager.resolve(beans);
        }
        return (Bean<T>) result;
    }

    public static <T> Annotation[] getClassLevelQualifiers(Class<T> beanClass, BeanManager beanManager) {
        List<Annotation> result = new ArrayList<Annotation>();
        for (Annotation annotation : beanClass.getAnnotations()) {
            if (beanManager.isQualifier(annotation.annotationType())) {
                result.add(annotation);
            }
        }
        return result.toArray(new Annotation[result.size()]);
    }

    public static boolean isBeanWithScope(Annotation[] annotations, BeanManager beanManager) {
        for (Annotation annotation : annotations) {
            if (beanManager.isScope(annotation.annotationType())) {
                return true;
            }

            if (beanManager.isStereotype(annotation.annotationType())) {
                if (isBeanWithScope(annotation.annotationType().getAnnotations(), beanManager)) {
                    return true;
                }
            }
        }
        return false;
    }
}
