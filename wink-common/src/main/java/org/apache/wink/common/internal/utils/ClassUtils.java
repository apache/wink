/*******************************************************************************
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
 *******************************************************************************/
package org.apache.wink.common.internal.utils;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);

    // Class.forName does not support primitives, so we have to account for that
    private static HashMap<String, Class<?>> loadClassMap = new HashMap<String, Class<?>>();
    static {
        loadClassMap.put("byte", byte.class);
        loadClassMap.put("int", int.class);
        loadClassMap.put("short", short.class);
        loadClassMap.put("long", long.class);
        loadClassMap.put("float", float.class);
        loadClassMap.put("double", double.class);
        loadClassMap.put("boolean", boolean.class);
        loadClassMap.put("char", char.class);
        loadClassMap.put("void", void.class);
    }
    
    /**
     * Use of ClassUtils.loadClass may be necessary in J2EE environments to load classes dynamically
     * due to Classloader hierarchies configured by the specific J2EE runtime.  In such environments,
     * the thread context class loader is prioritized over the system classloader.  For example, when
     * loading a JAX-RS Application subclass, and WINK is running as a shared library, WINK runtime may
     * not have classloader visibility into the J2EE application.  Prioritizing the thread context
     * classloader restores visibility.
     *
     * @param _className Class name
     * @return java class
     * @throws ClassNotFoundException if the class is not found
     */
    public static Class loadClass(final String _className)
            throws ClassNotFoundException {

        final String className = _className;
        Object ret = null;
        try { 
            // use doPrivileged to handle java2security
            ret = AccessController.doPrivileged(
                    new PrivilegedAction<Object>() {
                        public Object run() {

                            try {
                                // try context class loader first
                                logger.debug("Loading class {} using thread context classloader.", className);
                                return Class.forName(className,
                                        true,
                                        Thread.currentThread().getContextClassLoader());
                            } catch (ClassNotFoundException cnfe) {
                                try {
                                    // fallback to current classloader
                                    logger.debug("Loading class {} using current classloader.", className);
                                    return Class.forName(className,
                                                         true,
                                                         ClassUtils.class.getClassLoader());
                                } catch (ClassNotFoundException cnfe2) {
                                    // fallback to system classloader
                                    logger.debug("Loading class {} using system classloader.", className);
                                    try {
                                        return Class.forName(className);
                                    } catch (ClassNotFoundException cnfe3) {
                                        return cnfe3;
                                    }
                                }
                            } 
                        }
                    });
        }
        catch (SecurityException se) {
            // SecurityException means java2security did not allow visibility to className
            throw new ClassNotFoundException(className);
        }

        // class was located, return it
        if (ret instanceof Class) {
            return (Class) ret;
        } else if (ret instanceof ClassNotFoundException) {
            // Class.forName does not support primitives, so do a last check:
            Class cls = (Class) loadClassMap.get(className);
            if (cls != null) {
                logger.debug("Returning class {}", className);
                return cls;
            }
            throw (ClassNotFoundException) ret;
        }
        throw new ClassNotFoundException(className);

    }
    
}
