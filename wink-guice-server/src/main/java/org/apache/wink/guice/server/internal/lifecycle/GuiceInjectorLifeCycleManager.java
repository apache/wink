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

package org.apache.wink.guice.server.internal.lifecycle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.lifecycle.LifecycleManager;
import org.apache.wink.common.internal.lifecycle.ObjectCreationException;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.InjectableFactory;
import org.apache.wink.common.internal.registry.metadata.ApplicationMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.ConstructorMetadata;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

public class GuiceInjectorLifeCycleManager<T> implements LifecycleManager<T> {

    private final Injector injector;

    public GuiceInjectorLifeCycleManager(Injector injector) {
        this.injector = injector;
    }

    // private Logger logger =
    // LoggerFactory.getLogger(GuiceInjectorLifeCycleManager.class);

    public ObjectFactory<T> createObjectFactory(final T object) throws ObjectCreationException {
        injector.injectMembers(object);
        return new ObjectFactory<T>() {
            public T getInstance(RuntimeContext context) {
                return object;
            }

            @SuppressWarnings("unchecked")
            public Class<T> getInstanceClass() {
                return (Class<T>)object.getClass();
            }

            public void releaseInstance(T instance, RuntimeContext context) {
                /* do nothing */
            }

            public void releaseAll(RuntimeContext context) {
                /* do nothing */
            }
        };
    }

    public ObjectFactory<T> createObjectFactory(final Class<T> clazz)
        throws ObjectCreationException {
        if (clazz == null) {
            throw new NullPointerException("cls");
        }

        if (ResourceMetadataCollector.isDynamicResource(clazz)) {
            // default factory cannot create instance of DynamicResource
            throw new IllegalArgumentException(String
                .format("Cannot create default factory for DynamicResource: %s", clazz));
        }

        if (ApplicationMetadataCollector.isApplication(clazz)) {
            // by default application subclasses are singletons
            return new GuiceSingletonObjectFactory<T>(clazz, injector);
        }

        if (ProviderMetadataCollector.isProvider(clazz)) {
            // by default providers are singletons
            return new GuiceSingletonObjectFactory<T>(clazz, injector);
        }

        if (ResourceMetadataCollector.isStaticResource(clazz)) {
            // by default resources are prototypes (created per request)
            return new GuicePrototypeObjectFactory<T>(clazz, injector);
        }

        // unknown object, should never reach this code
        throw new IllegalArgumentException(String
            .format("Cannot create default factory for class: %s", clazz));
    }

    private static class GuiceSingletonObjectFactory<T> implements ObjectFactory<T> {

        private final ClassMetadata classMetadata;
        private final boolean       isConstructorNoArgumentOrInject;
        private final Injector      injector;
        private final Class<T>      clazz;
        private T                   instance;

        public GuiceSingletonObjectFactory(Class<T> clazz, Injector injector) {
            classMetadata = collectClassMetadata(clazz);
            ConstructorMetadata constructorMetadata = classMetadata.getConstructor();
            Constructor<?> constructor = constructorMetadata.getConstructor();
            List<Injectable> formalParameters = constructorMetadata.getFormalParameters();
            isConstructorNoArgumentOrInject =
                formalParameters.size() == 0 || constructor.getAnnotation(Inject.class) != null;
            this.injector = injector;
            this.clazz = clazz;
        }

        @SuppressWarnings("unchecked")
        public T getInstance(RuntimeContext context) {
            if (instance != null) {
                return instance;
            }
            try {
                if (isConstructorNoArgumentOrInject) {
                    try {
                        instance = (T)injector.getInstance(clazz);
                        return instance;
                    } catch (ProvisionException e) {
                        throw (Exception)e.getCause();
                    }
                }
                instance = (T)createObject(classMetadata, context);
                injector.injectMembers(instance);

                return instance;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new ObjectCreationException(e);
            }
        }

        public Class<T> getInstanceClass() {
            return clazz;
        }

        public void releaseInstance(T instance, RuntimeContext context) {
            /* do nothing */
        }

        public void releaseAll(RuntimeContext context) {
            /* do nothing */
        }
    }

    private static class GuicePrototypeObjectFactory<T> implements ObjectFactory<T> {

        private final ClassMetadata classMetadata;
        private final boolean       isConstructorNoArgumentOrInject;
        private final Injector      injector;
        private final Class<T>      clazz;

        public GuicePrototypeObjectFactory(Class<T> clazz, Injector injector) {
            classMetadata = collectClassMetadata(clazz);
            ConstructorMetadata constructorMetadata = classMetadata.getConstructor();
            Constructor<?> constructor = constructorMetadata.getConstructor();
            List<Injectable> formalParameters = constructorMetadata.getFormalParameters();
            isConstructorNoArgumentOrInject =
                formalParameters.size() == 0 || constructor.getAnnotation(Inject.class) != null;
            this.injector = injector;
            this.clazz = clazz;
        }

        @SuppressWarnings("unchecked")
        public T getInstance(RuntimeContext context) {
            try {
                if (isConstructorNoArgumentOrInject) {
                    try {
                        return (T)injector.getInstance(clazz);
                    } catch (ProvisionException e) {
                        throw (Exception)e.getCause();
                    }
                }
                T instance = (T)createObject(classMetadata, context);
                injector.injectMembers(instance);

                return instance;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new ObjectCreationException(e);
            }
        }

        public Class<T> getInstanceClass() {
            return clazz;
        }

        public void releaseInstance(T instance, RuntimeContext context) {
            /* do nothing */
        }

        public void releaseAll(RuntimeContext context) {
            /* do nothing */
        }
    }

    private static <T> ClassMetadata collectClassMetadata(final Class<T> cls) {
        ClassMetadata classMetadata = null;
        if (ProviderMetadataCollector.isProvider(cls)) {
            classMetadata = ProviderMetadataCollector.collectMetadata(cls);
        } else if (ResourceMetadataCollector.isResource(cls)) {
            classMetadata = ResourceMetadataCollector.collectMetadata(cls);
        } else if (ApplicationMetadataCollector.isApplication(cls)) {
            classMetadata = ApplicationMetadataCollector.collectMetadata(cls);
        }

        return classMetadata;
    }

    /**
     * creates object (StaticResource or Provider) based on its ClassMetadata
     * 
     * @param metadata
     * @param runtimeContext
     * @return created object
     */
    static Object createObject(ClassMetadata metadata, RuntimeContext runtimeContext) {
        try {
            // use constructor to create a prototype
            ConstructorMetadata constructorMetadata = metadata.getConstructor();
            Constructor<?> constructor = constructorMetadata.getConstructor();
            List<Injectable> formalParameters = constructorMetadata.getFormalParameters();
            Object[] params =
                InjectableFactory.getInstance().instantiate(formalParameters, runtimeContext);
            Object object = constructor.newInstance(params);
            return object;
        } catch (RuntimeException e) {
            throw e;
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException)targetException;
            }
            throw new ObjectCreationException(targetException);
        } catch (Exception e) {
            throw new ObjectCreationException(e);
        }
    }
}
