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

package org.apache.wink.server.internal.registry;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.DynamicResource;
import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.Injectable.ParamType;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor;
import org.apache.wink.server.internal.ServerCustomProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceRecordFactory {

    private static final Logger                 logger =
                                                           LoggerFactory
                                                               .getLogger(ResourceRecordFactory.class);

    private final LifecycleManagersRegistry     lifecycleManagerRegistry;
    private final Map<Class<?>, ResourceRecord> cacheByClass;

    private Lock                                readersLock;
    private Lock                                writersLock;

    final private boolean                       isStrictConsumesProduces;

    public ResourceRecordFactory(LifecycleManagersRegistry lifecycleManagerRegistry) {
        this(lifecycleManagerRegistry, new Properties());
    }

    public ResourceRecordFactory(LifecycleManagersRegistry lifecycleManagerRegistry,
                                 Properties customProperties) {
        if (lifecycleManagerRegistry == null) {
            throw new NullPointerException("lifecycleManagerRegistry"); //$NON-NLS-1$
        }
        this.lifecycleManagerRegistry = lifecycleManagerRegistry;
        this.cacheByClass = new HashMap<Class<?>, ResourceRecord>();
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readersLock = readWriteLock.readLock();
        writersLock = readWriteLock.writeLock();

        if (customProperties == null) {
            customProperties = new Properties();
        }

        String value =
            customProperties
                .getProperty(ServerCustomProperties.STRICT_INTERPRET_CONSUMES_PRODUCES_SPEC_CUSTOM_PROPERTY
                                 .getPropertyName(),
                             ServerCustomProperties.STRICT_INTERPRET_CONSUMES_PRODUCES_SPEC_CUSTOM_PROPERTY
                                 .getDefaultValue());
        isStrictConsumesProduces = Boolean.valueOf(value);
    }

    /**
     * Gets a resource record from a cache of records for the specified resource
     * class. If there is no record in the cache, then a new record is created
     * 
     * @param cls the resource class to get the record for
     * @return ResourceRecord for the resource class
     */
    public ResourceRecord getResourceRecord(Class<?> cls) {
        readersLock.lock();
        try {
            ResourceRecord record = cacheByClass.get(cls);
            if (record == null) {
                ObjectFactory<?> of = lifecycleManagerRegistry.getObjectFactory(cls);
                readersLock.unlock();
                try {
                    record = createStaticResourceRecord(cls, of);
                } finally {
                    readersLock.lock();
                }
            }
            return record;
        } finally {
            readersLock.unlock();
        }
    }

    /**
     * Gets a root resource record from a cache of records for the specified
     * resource instance. This is a shortcut for {@code
     * getResourceRecord(instance, true)}
     * 
     * @param instance the resource instance to get the record for
     * @return ResourceRecord for the resource instance
     */
    public ResourceRecord getResourceRecord(Object instance) {
        return getResourceRecord(instance, true);
    }

    /**
     * Gets a resource record from a cache of records for the specified resource
     * instance. If there is no record in the cache, or if the instance is a
     * dynamic resource, then a new record is created
     * 
     * @param instance the resource instance to get the record for
     * @param isRootResource specifies whether the instance is a root resource
     *            (true) or sub-resource (false)
     * @return ResourceRecord for the resource instance
     */
    public ResourceRecord getResourceRecord(Object instance, boolean isRootResource) {
        Class<? extends Object> cls = instance.getClass();
        ResourceRecord record = null;
        readersLock.lock();
        try {
            // if this is a root resource
            if (isRootResource) {
                if (ResourceMetadataCollector.isStaticResource(cls)) {
                    // if this is a static resource, and use cache
                    record = cacheByClass.get(cls);
                    if (record == null) {
                        ObjectFactory<?> of = lifecycleManagerRegistry.getObjectFactory(instance);
                        readersLock.unlock();
                        try {
                            record = createStaticResourceRecord(cls, of);
                        } finally {
                            readersLock.lock();
                        }
                    }
                } else if (ResourceMetadataCollector.isDynamicResource(cls)) {
                    // if this is a dynamic resource, don't use cache
                    ObjectFactory<?> of = lifecycleManagerRegistry.getObjectFactory(instance);
                    readersLock.unlock();
                    try {
                        record = createDynamicResourceRecord((DynamicResource)instance, of);
                    } finally {
                        readersLock.lock();
                    }
                } else {
                    throw new IllegalArgumentException(Messages
                        .getMessage("rootResourceInstanceIsAnInvalidResource", instance.getClass() //$NON-NLS-1$
                            .getCanonicalName()));
                }
            } else {
                // if this is a sub-resource, don't use cache, and don't use the
                // life-cycle manager
                ObjectFactory<?> of = new InstanceObjectFactory<Object>(instance);
                readersLock.unlock();
                try {
                    record = createSubResourceRecord(instance, of);
                } finally {
                    readersLock.lock();
                }
            }
            return record;
        } finally {
            readersLock.unlock();
        }
    }

    private ResourceRecord createStaticResourceRecord(Class<? extends Object> cls,
                                                      ObjectFactory<?> of) {
        ClassMetadata metadata = createMetadata(cls);
        UriTemplateProcessor processor = createUriTemplateProcessor(metadata);
        ResourceRecord record = new ResourceRecord(metadata, of, processor);
        writersLock.lock();
        try {
            // double check so as not to put the same resource twice
            if (cacheByClass.get(cls) == null) {
                cacheByClass.put(cls, record);
            }
        } finally {
            writersLock.unlock();
        }
        return record;
    }

    private ResourceRecord createDynamicResourceRecord(DynamicResource instance, ObjectFactory<?> of) {
        Class<? extends Object> cls = instance.getClass();
        ClassMetadata metadata = createMetadata(cls);
        metadata = fixInstanceMetadata(metadata, instance);
        UriTemplateProcessor processor = createUriTemplateProcessor(metadata);
        return new ResourceRecord(metadata, of, processor);
    }

    private ResourceRecord createSubResourceRecord(Object instance, ObjectFactory<?> of) {
        Class<? extends Object> cls = instance.getClass();
        ClassMetadata metadata = createMetadata(cls);
        return new ResourceRecord(metadata, of, null);
    }

    private ClassMetadata createMetadata(Class<? extends Object> cls) {
        ClassMetadata md = ResourceMetadataCollector.collectMetadata(cls);
        md = fixConsumesAndProduces(md);
        return md;
    }

    private UriTemplateProcessor createUriTemplateProcessor(ClassMetadata metadata) {
        // create the resource path using the parents paths
        StringBuilder path = new StringBuilder();
        // Recursively append parent paths
        appendPathWithParent(metadata, path);
        // create the processor
        return UriTemplateProcessor.newNormalizedInstance(path.toString());
    }

    private void appendPathWithParent(ClassMetadata metadata, StringBuilder pathStr) {
        ResourceRecord parentRecord = getParent(metadata);
        if (parentRecord != null) {
            ClassMetadata parentMetadata = parentRecord.getMetadata();
            appendPathWithParent(parentMetadata, pathStr);
        }
        String path = UriTemplateProcessor.normalizeUri(metadata.getPath());
        if (!path.endsWith("/")) { //$NON-NLS-1$
            pathStr.append("/"); //$NON-NLS-1$
        }
        pathStr.append(path);
    }

    private ResourceRecord getParent(ClassMetadata metadata) {
        Class<?> parent = metadata.getParent();
        Object parentInstance = metadata.getParentInstance();
        ResourceRecord parentRecord = null;
        if (parent != null) {
            parentRecord = getResourceRecord(parent);
        } else if (parentInstance != null) {
            parentRecord = getResourceRecord(parentInstance);
        }
        return parentRecord;
    }

    /**
     * Fixed the metadata to reflect the information stored on the instance of
     * the dynamic resource.
     * 
     * @param classMetadata
     * @param instance
     * @return
     */
    private ClassMetadata fixInstanceMetadata(ClassMetadata classMetadata,
                                              DynamicResource dynamicResource) {
        String path = dynamicResource.getPath();
        if (path != null) {
            classMetadata.addPath(path);
            if (logger.isTraceEnabled()) {
                logger.trace("Adding dispatched path from instance: {}", path); //$NON-NLS-1$
            }
        }

        Object parent = dynamicResource.getParent();
        if (parent != null) {
            classMetadata.getParentInstances().add(parent);
            if (logger.isTraceEnabled()) {
                logger.trace("Adding parent beans from instance: {}", parent); //$NON-NLS-1$
            }
        }

        String workspaceTitle = dynamicResource.getWorkspaceTitle();
        if (workspaceTitle != null) {
            classMetadata.setWorkspaceName(workspaceTitle);
        }

        String collectionTitle = dynamicResource.getCollectionTitle();
        if (collectionTitle != null) {
            classMetadata.setCollectionTitle(collectionTitle);
        }
        return classMetadata;
    }

    /**
     * This method will go through each method and "fix" the method metadata to
     * "ignore" inherited {@link Consumes} and {@link Produces} annotations when
     * appropriate. For Produces, if the return type is void, then ignore the
     * Produces annotation. For Consumes, if there are no entity parameters,
     * ignore the Consumes annotation.
     * 
     * @param classMetadata
     * @return
     */
    ClassMetadata fixConsumesAndProduces(ClassMetadata classMetadata) {
        logger.trace("fixConsumesAndProduces({}) entry", classMetadata);
        if (isStrictConsumesProduces) {
            logger
                .trace("fixConsumesAndProduces() exit returning because custom property {} is set to true.",
                       ServerCustomProperties.STRICT_INTERPRET_CONSUMES_PRODUCES_SPEC_CUSTOM_PROPERTY
                           .getPropertyName());
            return classMetadata;
        }

        Set<MediaType> produces = classMetadata.getProduces();
        Set<MediaType> consumes = classMetadata.getConsumes();

        Set<MethodMetadata> allMethodMetadata = new HashSet<MethodMetadata>();
        allMethodMetadata.addAll(classMetadata.getResourceMethods());
        allMethodMetadata.addAll(classMetadata.getSubResourceMethods());

        /*
         * Ignore subresource locators because a) they have to return a non-void
         * and b) they aren't allowed to have an entity parameter.
         */

        /* fix the produces */
        for (MethodMetadata methodMetadata : allMethodMetadata) {
            Method method = methodMetadata.getReflectionMethod();
            if (Void.TYPE.equals(method.getReturnType())) {
                if (produces.size() > 0 && methodMetadata.getProduces().equals(produces)) {
                    /*
                     * let's assume this was inherited now. weird case would be
                     * they repeated the annotation values in both the class and
                     * method.
                     */
                    methodMetadata.addProduces(MediaType.WILDCARD_TYPE);
                    logger
                        .trace("Method has a @Produces value but also a void return type so adding a */* to allow any response: {} ",
                               methodMetadata);
                }
            }
        }

        /* fix the consumes */
        for (MethodMetadata methodMetadata : allMethodMetadata) {
            if (consumes.size() > 0 && methodMetadata.getConsumes().equals(consumes)) {
                List<Injectable> params = methodMetadata.getFormalParameters();
                boolean isEntityParamFound = false;
                for (Injectable p : params) {
                    if (ParamType.ENTITY.equals(p.getParamType())) {
                        isEntityParamFound = true;
                    }
                }
                /*
                 * let's assume this was inherited now. weird case would be they
                 * repeated the annotation values in both the class and method.
                 */
                if (!isEntityParamFound) {
                    methodMetadata.addConsumes(MediaType.WILDCARD_TYPE);
                    logger
                        .trace("Method has a @Consumes value but no entity parameter so adding a */* to allow any request: {} ",
                               methodMetadata);
                }
            }
        }

        logger.trace("fixConsumesAndProduces() exit returning {}", classMetadata);
        return classMetadata;
    }

    private static class InstanceObjectFactory<T> implements ObjectFactory<T> {

        private final T object;

        public InstanceObjectFactory(T object) {
            this.object = object;
        }

        public T getInstance(RuntimeContext context) {
            return object;
        }

        @SuppressWarnings("unchecked")
        public Class<T> getInstanceClass() {
            return (Class<T>)object.getClass();
        }

        @Override
        public String toString() {
            return String.format("InstanceObjectFactory: %s", getInstanceClass()); //$NON-NLS-1$
        }

        public void releaseInstance(T instance, RuntimeContext context) {
            /* do nothing */
        }

        public void releaseAll(RuntimeContext context) {
            /* do nothing */
        }
    }

}
