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
package org.apache.wink.common.internal.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * The only reason to extend LifecycleManagerUtils is to get easy access to collectClassMetadata method
 *
 */
public class JSR250LifecycleManagerUtils extends LifecycleManagerUtils {

    private static Logger logger = LoggerFactory.getLogger(JSR250LifecycleManagerUtils.class);
    
    // cache the @PostConstruct and @PreDestroy annotated methods so we don't have to do reflective search each time
    private static class PreDestroyMethod {
        private Method m;
        public PreDestroyMethod(Method _m) {
            m = _m;
        }
        public Method getMethod() {
            return m;
        }
    }
    private static class PostConstructMethod {
        private Method m;
        public PostConstructMethod(Method _m) {
            m = _m;
        }
        public Method getMethod() {
            return m;
        }
    }
    @SuppressWarnings("unchecked")
    private static Map<Class, PreDestroyMethod> preDestroyMethodCache = new HashMap<Class, PreDestroyMethod>();
    @SuppressWarnings("unchecked")
    private static Map<Class, PostConstructMethod> postConstructMethodCache = new HashMap<Class, PostConstructMethod>();

    
    public static <T> ObjectFactory<T> createSingletonObjectFactory(T object) {
        ObjectFactory<T> factory = LifecycleManagerUtils.createSingletonObjectFactory(object);
        executePostConstructMethod(factory.getInstance(null));
        return new JSR250SingletonObjectFactory<T>(factory.getInstance(null));
    }

    public static <T> ObjectFactory<T> createSingletonObjectFactory(final Class<T> cls) {
        ObjectFactory<T> factory = LifecycleManagerUtils.createSingletonObjectFactory(cls);
        executePostConstructMethod(factory.getInstance(null));
        return new JSR250SingletonObjectFactory<T>(factory.getInstance(null));
    }
    
    public static <T> ObjectFactory<T> createPrototypeObjectFactory(final Class<T> cls) {
        ClassMetadata classMetadata = collectClassMetadata(cls, true);
        return new JSR250PrototypeObjectFactory<T>(classMetadata);
    }
    
    @SuppressWarnings("unchecked")
    protected static <T> void executePostConstructMethod(T object) {
        if (object != null) {
            // check the cache
            if (postConstructMethodCache.containsKey((Class<T>)object.getClass())) {
                Method m = postConstructMethodCache.get((Class<T>)object.getClass()).getMethod();
                if (m != null) {
                    invokeMethod(object, m, null);
                    if(logger.isTraceEnabled()) {
                        logger.trace("Invoked PostConstruct annotated method {} for class {}", new Object[]{m, (Class<T>)object.getClass()});
                    }
                } else {
                    if(logger.isTraceEnabled()) {
                        logger.trace("No PostConstruct annotated method found for class {}", new Object[]{(Class<T>)object.getClass()});
                    }
                }
                return;
            }
            // no cache entry; do the search and populate the cache
            postConstructMethodCache.put((Class<T>)object.getClass(), new PostConstructMethod(null));
            List<Method> methods = getMethods(object.getClass());
            for (Method method : methods) {
                if (isPostConstruct(method)) {
                    invokeMethod(object, method, null);
                    // cache it
                    postConstructMethodCache.put((Class<T>)object.getClass(), new PostConstructMethod(method));
                    // don't invoke all the @PostConstruct methods
                    break;
                }
            }
            // logging:
            Method m = postConstructMethodCache.get((Class<T>)object.getClass()).getMethod();
            if (m != null) {
                if(logger.isTraceEnabled()) {
                    logger.trace("Invoked and cached PostConstruct annotated method {} for class {}", new Object[]{m, (Class<T>)object.getClass()});
                }
            } else {
                if(logger.isTraceEnabled()) {
                    logger.trace("No PostConstruct annotated method found for class {}, null value cached", new Object[]{(Class<T>)object.getClass()});
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected static <T> void executePreDestroyMethod(T object) {
        if (object != null) {
            // check the cache
            if (preDestroyMethodCache.containsKey((Class<T>)object.getClass())) {
                Method m = preDestroyMethodCache.get((Class<T>)object.getClass()).getMethod();
                if (m != null) {
                    invokeMethod(object, m, null);
                    if(logger.isTraceEnabled()) {
                        logger.trace("Invoked PreDestroy annotated method {} for class {}", new Object[]{m, (Class<T>)object.getClass()});
                    }
                } else {
                    if(logger.isTraceEnabled()) {
                        logger.trace("No PreDestroy annotated method found for class {}", new Object[]{(Class<T>)object.getClass()});
                    }
                }
                return;
            }
            // no cache entry; do the search and populate the cache
            preDestroyMethodCache.put((Class<T>)object.getClass(), new PreDestroyMethod(null));
            List<Method> methods = getMethods(object.getClass());
            for (Method method : methods) {
                if (isPreDestroy(method)) {
                    invokeMethod(object, method, null);
                    // cache it
                    preDestroyMethodCache.put((Class<T>)object.getClass(), new PreDestroyMethod(method));
                    // don't invoke all the @PreDestroy methods
                    break;
                }
            }
            // logging:
            Method m = preDestroyMethodCache.get((Class<T>)object.getClass()).getMethod();
            if (m != null) {
                if(logger.isTraceEnabled()) {
                    logger.trace("Invoked and cached PreDestroy annotated method {} for class {}", new Object[]{m, (Class<T>)object.getClass()});
                }
            } else {
                if(logger.isTraceEnabled()) {
                    logger.trace("No PreDestroy annotated method found for class {}, null value cached", new Object[]{(Class<T>)object.getClass()});
                }
            }
        }
    }
    
    // utility to detect existence of any method annotated with either @PostConstruct or @PreDestroy
    @SuppressWarnings("unchecked")
    protected static boolean hasJSR250AnnotatedMethod(Class clazz) {
        List<Method> methods = getMethods(clazz);
        for (Method method : methods) {
            if (isPreDestroy(method) || isPostConstruct(method)) {
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private static void invokeMethod(final Object object, final Method m, final Object[] params) throws ObjectCreationException {
        try {
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws InvocationTargetException, IllegalAccessException {
                            return m.invoke(object, params);
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            throw new ObjectCreationException(e.getException());
        }
    }

    
    /**
     * Gets all of the methods in this class and the super classes
     *
     * @param beanClass
     * @return
     */
    @SuppressWarnings("unchecked")
    private static List<Method> getMethods(final Class beanClass) {
        // This class must remain private due to Java 2 Security concerns
        List<Method> methods;
        methods = (List<Method>)AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        List<Method> methods = new ArrayList<Method>();
                        Class cls = beanClass;
                        while (cls != null) {
                            Method[] methodArray = cls.getDeclaredMethods();
                            for (Method method : methodArray) {
                                methods.add(method);
                            }
                            cls = cls.getSuperclass();
                        }
                        return methods;
                    }
                }
        );

        return methods;
    }

    @SuppressWarnings("unchecked")
    private static boolean isPostConstruct(final Method method) {
        Annotation[] annotations = (Annotation[]) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return method.getDeclaredAnnotations();
                    }
                }
        );
        for (Annotation annotation : annotations) {
            return PostConstruct.class.isAssignableFrom(annotation.annotationType());
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static boolean isPreDestroy(final Method method) {
        Annotation[] annotations = (Annotation[]) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return method.getDeclaredAnnotations();
                    }
                }
        );
        for (Annotation annotation : annotations) {
            return PreDestroy.class.isAssignableFrom(annotation.annotationType());
        }
        return false;
    }

}
