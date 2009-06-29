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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.DynamicResource;
import org.apache.wink.common.internal.factory.OFFactoryRegistry;
import org.apache.wink.common.internal.factory.ObjectFactory;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor;


public class ResourceRecordFactory {

    private static final Logger logger = LoggerFactory.getLogger(ResourceRecordFactory.class);

    private final OFFactoryRegistry objectFactoryRegistry;
    private final Map<Class<?>,ResourceRecord> cacheByClass;

    private Lock readersLock;
    private Lock writersLock;

    public ResourceRecordFactory(OFFactoryRegistry objectFactoryRegistry) {
        if (objectFactoryRegistry == null) {
            throw new NullPointerException("objectFactoryRegistry");
        }
        this.objectFactoryRegistry = objectFactoryRegistry;
        this.cacheByClass = new HashMap<Class<?>,ResourceRecord>();
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readersLock = readWriteLock.readLock();
        writersLock = readWriteLock.writeLock();
    }

    /**
     * Gets a resource record from a cache of records for the specified resource class. If there is
     * no record in the cache, then a new record is created
     * 
     * @param cls
     *            the resource class to get the record for
     * @return ResourceRecord for the resource class
     */
    public ResourceRecord getResourceRecord(Class<?> cls) {
        readersLock.lock();
        try {
            ResourceRecord record = cacheByClass.get(cls);
            if (record == null) {
                ObjectFactory<?> of = objectFactoryRegistry.getObjectFactory(cls);
                readersLock.unlock();
                try {
                    record = createAndCacheResourceRecord(cls, of);
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
     * Gets a resource record from a cache of records for the specified resource instance. If there
     * is no record in the cache, or if the instance is a dynamic resource, then a new record is
     * created
     * 
     * @param Object
     *            the resource instance to get the record for
     * @return ResourceRecord for the resource instance
     */
    public ResourceRecord getResourceRecord(Object instance) {
        Class<? extends Object> cls = instance.getClass();
        ResourceRecord record = null;
        readersLock.lock();
        try {
            if (ResourceMetadataCollector.isStaticResource(cls)) {
                record = cacheByClass.get(cls);
                if (record == null) {
                    ObjectFactory<?> of = objectFactoryRegistry.getObjectFactory(instance);
                    readersLock.unlock();
                    try {
                        record = createAndCacheResourceRecord(cls, of);
                    } finally {
                        readersLock.lock();
                    }
                }
            } else {
                // don't use cache
                ObjectFactory<?> of = objectFactoryRegistry.getObjectFactory(instance);
                readersLock.unlock();
                try {
                    record = createResourceRecord(instance, of);
                } finally {
                    readersLock.lock();
                }
            }
            return record;
        } finally {
            readersLock.unlock();
        }
    }

    private ResourceRecord createAndCacheResourceRecord(Class<? extends Object> cls, ObjectFactory<?> of) {
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

    private ResourceRecord createResourceRecord(Object instance, ObjectFactory<?> of) {
        Class<? extends Object> cls = instance.getClass();
        ClassMetadata metadata = createMetadata(cls);
        metadata = fixInstanceMetadata(metadata, instance);
        UriTemplateProcessor processor = createUriTemplateProcessor(metadata);
        return new ResourceRecord(metadata, of, processor);
    }

    private ClassMetadata createMetadata(Class<? extends Object> cls) {
        return ResourceMetadataCollector.collectMetadata(cls);
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
        if (!path.endsWith("/")) {
            pathStr.append("/");
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
     * Fixed the metadata to reflect the information stored on the instance of the dynamic resource.
     * 
     * @param classMetadata
     * @param instance
     * @return
     */
    private ClassMetadata fixInstanceMetadata(ClassMetadata classMetadata, Object instance) {
        if (instance instanceof DynamicResource) {
            DynamicResource dynamicResource = (DynamicResource)instance;
            String[] dispatchedPath = dynamicResource.getDispatchedPath();
            if (dispatchedPath != null) {
                classMetadata.addPaths(Arrays.asList(dispatchedPath));
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Adding dispatched path from instance: %s", Arrays.toString(dispatchedPath)));
                }
            }

            Object[] parents = dynamicResource.getParents();
            if (parents != null) {
                classMetadata.getParentInstances().addAll(Arrays.asList(parents));
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Adding parent beans from instance: %s", Arrays.toString(parents)));
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
        }
        return classMetadata;
    }

}
