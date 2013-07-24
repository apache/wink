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

import org.apache.wink.jcdi.server.internal.util.ClassUtils;
import org.apache.wink.jcdi.server.spi.BeanManagerResolver;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultBeanManagerResolver implements BeanManagerResolver {
    private static Map<ClassLoader, BeanManager> beanManager = new ConcurrentHashMap<ClassLoader, BeanManager>();

    //useful for OpenWebBeans which just has one BM which isn't bound to jndi with the minimal module-config
    private static Map<ClassLoader, BeanManager> foundBeanManager = new ConcurrentHashMap<ClassLoader, BeanManager>();

    public BeanManager get() {
        BeanManager result = beanManager.get(ClassUtils.getClassLoader(null));
        if (result == null) {
            try {
                result = (BeanManager) new InitialContext().lookup("java:comp/BeanManager");
                beanManager.put(ClassUtils.getClassLoader(null), result);
            } catch (NamingException e) {
                result = foundBeanManager.get(ClassUtils.getClassLoader(null));

                if (result != null) {
                    beanManager.put(ClassUtils.getClassLoader(null), result);
                } else {
                    throw new IllegalStateException(e);
                }
            }
        }

        if (result == null) {
            throw new IllegalStateException("no bean-manager found");
        }

        return result;
    }

    public static void setBeanManager(BeanManager beanManager) {
        foundBeanManager.put(ClassUtils.getClassLoader(null), beanManager);
    }

    public static void reset() {
        foundBeanManager.clear();
    }
}
