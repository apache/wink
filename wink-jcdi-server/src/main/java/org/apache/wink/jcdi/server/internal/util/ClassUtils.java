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
package org.apache.wink.jcdi.server.internal.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

//abstract class won't end up as cdi-bean
//see org.apache.deltaspike.core.util.ClassUtils
public abstract class ClassUtils {
    /**
     * Constructor which prevents the instantiation of this class
     */
    private ClassUtils() {
        // prevent instantiation
    }

    public static ClassLoader getClassLoader(Object o) {
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged(new GetClassLoaderAction(o));
        } else {
            return getClassLoaderInternal(o);
        }
    }

    static class GetClassLoaderAction implements PrivilegedAction<ClassLoader> {
        private Object object;

        GetClassLoaderAction(Object object) {
            this.object = object;
        }

        public ClassLoader run() {
            try {
                return getClassLoaderInternal(object);
            } catch (Exception e) {
                return null;
            }
        }
    }

    private static ClassLoader getClassLoaderInternal(Object o) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        if (loader == null && o != null) {
            loader = o.getClass().getClassLoader();
        }

        if (loader == null) {
            loader = ClassUtils.class.getClassLoader();
        }

        return loader;
    }
}
