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
package org.apache.wink.common.internal.registry;

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.WinkApplication;
import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.common.internal.utils.GenericsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps the registry of providers.
 * <p>
 * The order of the providers is important. The later provider was added, the
 * higher priority it has. Thus, the default providers should be always added
 * before the custom or with lower priority.
 */
public class ProvidersRegistry {

    private static Logger                                    logger             =
                                                                                    LoggerFactory
                                                                                        .getLogger(ProvidersRegistry.class);

    private final ProducesMediaTypeMap<ContextResolver<?>>   contextResolvers   =
                                                                                    new ProducesMediaTypeMap<ContextResolver<?>>(
                                                                                                                                 ContextResolver.class);
    private final Set<ObjectFactory<ExceptionMapper<?>>>     exceptionMappers   =
                                                                                    new TreeSet<ObjectFactory<ExceptionMapper<?>>>(
                                                                                                                                   Collections
                                                                                                                                       .reverseOrder());
    private final ConsumesMediaTypeMap<MessageBodyReader<?>> messageBodyReaders =
                                                                                    new ConsumesMediaTypeMap<MessageBodyReader<?>>(
                                                                                                                                   MessageBodyReader.class);
    private final ProducesMediaTypeMap<MessageBodyWriter<?>> messageBodyWriters =
                                                                                    new ProducesMediaTypeMap<MessageBodyWriter<?>>(
                                                                                                                                   MessageBodyWriter.class);
    private final ApplicationValidator                       applicationValidator;
    private final LifecycleManagersRegistry                  factoryFactoryRegistry;
    private final Lock                                       readersLock;
    private final Lock                                       writersLock;

    public ProvidersRegistry(LifecycleManagersRegistry factoryRegistry,
                             ApplicationValidator applicationValidator) {
        this.factoryFactoryRegistry = factoryRegistry;
        this.applicationValidator = applicationValidator;
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readersLock = readWriteLock.readLock();
        writersLock = readWriteLock.writeLock();
    }

    @SuppressWarnings("unchecked")
    public boolean addProvider(Class<?> cls, double priority) {
        if (cls == null) {
            throw new NullPointerException("cls");
        }
        writersLock.lock();
        try {
            ObjectFactory<?> objectFactory = factoryFactoryRegistry.getObjectFactory(cls);
            return addProvider(new PriorityObjectFactory(objectFactory, priority));
        } finally {
            writersLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean addProvider(Object provider, double priority) {
        if (provider == null) {
            throw new NullPointerException("provider");
        }
        writersLock.lock();
        try {
            ObjectFactory<?> objectFactory = factoryFactoryRegistry.getObjectFactory(provider);
            return addProvider(new PriorityObjectFactory(objectFactory, priority));
        } finally {
            writersLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private boolean addProvider(ObjectFactory<?> objectFactory) {
        Class<? extends Object> cls = objectFactory.getInstanceClass();

        logger.debug("Processing provider of type {}", cls);

        boolean retValue = false;

        if (!applicationValidator.isValidProvider(cls)) {
            return retValue;
        }

        if (ContextResolver.class.isAssignableFrom(cls)) {
            contextResolvers.putProvider((ObjectFactory<ContextResolver<?>>)objectFactory);
            retValue = true;
        }
        if (ExceptionMapper.class.isAssignableFrom(cls)) {
            exceptionMappers.add((ObjectFactory<ExceptionMapper<?>>)objectFactory);
            retValue = true;
        }
        if (MessageBodyReader.class.isAssignableFrom(cls)) {
            messageBodyReaders.putProvider((ObjectFactory<MessageBodyReader<?>>)objectFactory);
            retValue = true;
        }
        if (MessageBodyWriter.class.isAssignableFrom(cls)) {
            messageBodyWriters.putProvider((ObjectFactory<MessageBodyWriter<?>>)objectFactory);
            retValue = true;
        }
        if (retValue == false) {
            logger.warn(Messages.getMessage("classIsUnknownProvider"), cls);
        }
        return retValue;

    }

    public boolean addProvider(Class<?> cls) {
        return addProvider(cls, WinkApplication.DEFAULT_PRIORITY);
    }

    public boolean addProvider(Object provider) {
        return addProvider(provider, WinkApplication.DEFAULT_PRIORITY);
    }

    @SuppressWarnings("unchecked")
    public <T> ContextResolver<T> getContextResolver(final Class<T> contextType,
                                                     MediaType mediaType,
                                                     RuntimeContext runtimeContext) {
        if (contextType == null) {
            throw new NullPointerException("contextType");
        }
        if (mediaType == null) {
            // see https://issues.apache.org/jira/browse/WINK-153
            mediaType = MediaType.WILDCARD_TYPE;
        }
        readersLock.lock();
        try {
            final List<ObjectFactory<ContextResolver<?>>> factories =
                contextResolvers.getProvidersByMediaType(mediaType, contextType);

            if (factories.isEmpty()) {
                return null;
            }

            if (factories.size() == 1) {
                return (ContextResolver<T>)factories.get(0).getInstance(runtimeContext);
            }

            // creates list of providers that is used by the proxy
            // this solution can be improved by creating providers inside the
            // proxy
            // one-by-one and keeping them on the proxy
            // so a new provider will be created only when all the old providers
            // will return null
            final List<ContextResolver<?>> providers =
                new ArrayList<ContextResolver<?>>(factories.size());
            for (ObjectFactory<ContextResolver<?>> factory : factories) {
                providers.add(factory.getInstance(runtimeContext));
            }

            return (ContextResolver<T>)Proxy.newProxyInstance(getClass().getClassLoader(),
                                                              new Class[] {ContextResolver.class},
                                                              new InvocationHandler() {

                                                                  public Object invoke(Object proxy,
                                                                                       Method method,
                                                                                       Object[] args)
                                                                      throws Throwable {
                                                                      if (method.getName()
                                                                          .equals("getContext") && args != null
                                                                          && args.length == 1
                                                                          && (args[0] == null || args[0]
                                                                              .getClass()
                                                                              .equals(Class.class))) {
                                                                          for (ContextResolver<?> resolver : providers) {
                                                                              Object context =
                                                                                  resolver
                                                                                      .getContext((Class<?>)args[0]);
                                                                              if (context != null) {
                                                                                  return context;
                                                                              }
                                                                          }
                                                                          return null;
                                                                      } else {
                                                                          return method
                                                                              .invoke(proxy, args);
                                                                      }
                                                                  }
                                                              });
        } finally {
            readersLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type,
                                                                       RuntimeContext runtimeContext) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        readersLock.lock();
        try {
            List<ExceptionMapper<?>> matchingMappers = new ArrayList<ExceptionMapper<?>>();

            for (ObjectFactory<ExceptionMapper<?>> factory : exceptionMappers) {
                ExceptionMapper<?> exceptionMapper = factory.getInstance(runtimeContext);
                Type genericType =
                    GenericsUtils.getGenericInterfaceParamType(exceptionMapper.getClass(),
                                                               ExceptionMapper.class);
                Class<?> classType = GenericsUtils.getClassType(genericType);
                if (classType.isAssignableFrom(type)) {
                    matchingMappers.add(exceptionMapper);
                }
            }

            if (matchingMappers.isEmpty()) {
                return null;
            }

            while (matchingMappers.size() > 1) {
                Type first =
                    GenericsUtils.getGenericInterfaceParamType(matchingMappers.get(0).getClass(),
                                                               ExceptionMapper.class);
                Type second =
                    GenericsUtils.getGenericInterfaceParamType(matchingMappers.get(1).getClass(),
                                                               ExceptionMapper.class);
                Class<?> firstClass = GenericsUtils.getClassType(first);
                Class<?> secondClass = GenericsUtils.getClassType(second);
                if (firstClass == secondClass) {
                    // the first one has higher priority, so remove the second
                    // one for the same classes!
                    matchingMappers.remove(1);
                } else if (firstClass.isAssignableFrom(secondClass)) {
                    matchingMappers.remove(0);
                } else {
                    matchingMappers.remove(1);
                }
            }

            return (ExceptionMapper<T>)matchingMappers.get(0);
        } finally {
            readersLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type,
                                                         Type genericType,
                                                         Annotation[] annotations,
                                                         MediaType mediaType,
                                                         RuntimeContext runtimeContext) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (mediaType == null) {
            throw new NullPointerException("mediaType");
        }
        readersLock.lock();
        try {
            List<ObjectFactory<MessageBodyReader<?>>> factories =
                messageBodyReaders.getProvidersByMediaType(mediaType, type);
            for (ObjectFactory<MessageBodyReader<?>> factory : factories) {
                MessageBodyReader<?> reader = factory.getInstance(runtimeContext);
                if (reader.isReadable(type, genericType, annotations, mediaType)) {
                    return (MessageBodyReader<T>)reader;
                }
            }
            return null;
        } finally {
            readersLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type,
                                                         Type genericType,
                                                         Annotation[] annotations,
                                                         MediaType mediaType,
                                                         RuntimeContext runtimeContext) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (mediaType == null) {
            throw new NullPointerException("mediaType");
        }
        readersLock.lock();
        try {
            List<ObjectFactory<MessageBodyWriter<?>>> writersFactories =
                messageBodyWriters.getProvidersByMediaType(mediaType, type);
            for (ObjectFactory<MessageBodyWriter<?>> factory : writersFactories) {
                MessageBodyWriter<?> writer = factory.getInstance(runtimeContext);
                if (writer.isWriteable(type, genericType, annotations, mediaType)) {
                    return (MessageBodyWriter<T>)writer;
                }
            }
            return null;
        } finally {
            readersLock.unlock();
        }
    }

    public Set<MediaType> getMessageBodyReaderMediaTypesLimitByIsReadable(Class<?> type,
                                                                          RuntimeContext runtimeContext) {
        Set<MediaType> mediaTypes = new HashSet<MediaType>();
        readersLock.lock();
        try {
            List<ObjectFactory<MessageBodyReader<?>>> readerFactories =
                messageBodyReaders.getProvidersByMediaType(MediaType.WILDCARD_TYPE, type);

            Annotation[] ann = new Annotation[0];
            for (ObjectFactory<MessageBodyReader<?>> factory : readerFactories) {
                MessageBodyReader<?> reader = factory.getInstance(runtimeContext);
                Consumes consumes = factory.getInstanceClass().getAnnotation(Consumes.class);
                String[] values = null;
                if (consumes != null) {
                    values = consumes.value();
                } else {
                    values = new String[] {MediaType.WILDCARD};
                }
                for (String v : values) {
                    MediaType mt = MediaType.valueOf(v);
                    if (reader.isReadable(type, type, ann, mt)) {
                        mediaTypes.add(mt);
                    }
                }
            }
        } finally {
            readersLock.unlock();
        }
        return mediaTypes;
    }

    public Set<MediaType> getMessageBodyWriterMediaTypes(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        Set<MediaType> mediaTypes = messageBodyWriters.getProvidersMediaTypes(type);
        return mediaTypes;
    }

    private class ProducesMediaTypeMap<T> extends MediaTypeMap<T> {

        public ProducesMediaTypeMap(Class<?> rawType) {
            super(rawType);
        }

        public void putProvider(ObjectFactory<T> objectFactory) {
            Produces produces = objectFactory.getInstanceClass().getAnnotation(Produces.class);
            if (produces == null) {
                put(MediaType.WILDCARD_TYPE, objectFactory);
            } else {
                String[] values = produces.value();
                for (String val : values) {
                    put(MediaType.valueOf(val), objectFactory);
                }
            }
        }
    }

    private class ConsumesMediaTypeMap<T> extends MediaTypeMap<T> {

        public ConsumesMediaTypeMap(Class<?> rawType) {
            super(rawType);
        }

        public void putProvider(ObjectFactory<T> objectFactory) {
            Consumes consumes = objectFactory.getInstanceClass().getAnnotation(Consumes.class);
            if (consumes == null) {
                put(MediaType.WILDCARD_TYPE, objectFactory);
            } else {
                String[] values = consumes.value();
                for (String val : values) {
                    put(MediaType.valueOf(val), objectFactory);
                }
            }
        }
    }

    private abstract class MediaTypeMap<T> {

        private final Map<MediaType, Set<ObjectFactory<T>>>                                          data           =
                                                                                                                        new LinkedHashMap<MediaType, Set<ObjectFactory<T>>>();
        private final Class<?>                                                                       rawType;

        private final Map<Class<?>, SoftReference<ConcurrentMap<MediaType, List<ObjectFactory<T>>>>> providersCache =
                                                                                                                        new ConcurrentHashMap<Class<?>, SoftReference<ConcurrentMap<MediaType, List<ObjectFactory<T>>>>>(); ;

        public MediaTypeMap(Class<?> rawType) {
            super();
            this.rawType = rawType;
        }

        /**
         * returns providers by mediaType and by type
         * 
         * @param mediaType
         * @param cls
         * @return
         */
        public List<ObjectFactory<T>> getProvidersByMediaType(MediaType mediaType, Class<?> cls) {
            String subtype = mediaType.getSubtype();
            String type = mediaType.getType();
            if (!mediaType.getParameters().isEmpty()) {
                mediaType = new MediaType(type, subtype);
            }

            SoftReference<ConcurrentMap<MediaType, List<ObjectFactory<T>>>> mediaTypeToProvidersCacheRef =
                providersCache.get(cls);
            ConcurrentMap<MediaType, List<ObjectFactory<T>>> mediaTypeToProvidersCache = null;
            if (mediaTypeToProvidersCacheRef != null) {
                mediaTypeToProvidersCache = mediaTypeToProvidersCacheRef.get();
            }
            if (mediaTypeToProvidersCache == null) {
                mediaTypeToProvidersCache =
                    new ConcurrentHashMap<MediaType, List<ObjectFactory<T>>>();
                providersCache
                    .put(cls,
                         new SoftReference<ConcurrentMap<MediaType, List<ObjectFactory<T>>>>(
                                                                                             mediaTypeToProvidersCache));
            }

            List<ObjectFactory<T>> list = mediaTypeToProvidersCache.get(mediaType);

            if (list == null) {
                if (subtype.equals(MediaType.MEDIA_TYPE_WILDCARD) || type
                    .equals(MediaType.MEDIA_TYPE_WILDCARD)) {
                    list = getProvidersByWildcardMediaType(mediaType, cls);
                    mediaTypeToProvidersCache.put(mediaType, list);
                    return list;
                }
                list = new ArrayList<ObjectFactory<T>>();
                if (!mediaType.getParameters().isEmpty()) {
                    mediaType = new MediaType(type, subtype);
                }
                Set<ObjectFactory<T>> set = data.get(mediaType);
                limitByType(list, set, cls);
                set = data.get(new MediaType(type, MediaType.MEDIA_TYPE_WILDCARD));
                limitByType(list, set, cls);
                set = data.get(MediaType.WILDCARD_TYPE);
                limitByType(list, set, cls);

                mediaTypeToProvidersCache.put(mediaType, list);
            }

            return list;
        }

        private List<ObjectFactory<T>> getProvidersByWildcardMediaType(MediaType mediaType,
                                                                       Class<?> cls) {

            // according to JSR311 3.8, the providers must be searched
            // using a concrete type
            // if the providers are searched using a wildcard, it means
            // that the call is done
            // from the Providers interface, therefore isCompatible method
            // should be used
            // the search here is less efficient that the regular search
            // see https://issues.apache.org/jira/browse/WINK-47

            List<ObjectFactory<T>> list = new ArrayList<ObjectFactory<T>>();

            ArrayList<Entry<MediaType, Set<ObjectFactory<T>>>> compatibleList =
                new ArrayList<Entry<MediaType, Set<ObjectFactory<T>>>>();
            for (Entry<MediaType, Set<ObjectFactory<T>>> entry : data.entrySet()) {
                if (entry.getKey().isCompatible(mediaType)) {
                    compatibleList.add(entry);
                }
            }

            // sorts according to the following algorithm: n / m > n / * > * / *
            // in descending order
            // see https://issues.apache.org/jira/browse/WINK-82
            Collections.sort(compatibleList, Collections
                .reverseOrder(new Comparator<Entry<MediaType, Set<ObjectFactory<T>>>>() {

                    public int compare(Entry<MediaType, Set<ObjectFactory<T>>> o1,
                                       Entry<MediaType, Set<ObjectFactory<T>>> o2) {
                        MediaType m1 = o1.getKey();
                        MediaType m2 = o2.getKey();
                        int compareTypes = compareTypes(m1.getType(), m2.getType());
                        if (compareTypes == 0) {
                            return compareTypes(m1.getSubtype(), m2.getSubtype());
                        }
                        return compareTypes;
                    }

                    private int compareTypes(String type1, String type2) {
                        if (type1.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
                            if (type2.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
                                // both types are wildcards
                                return 0;
                            }
                            // only type2 is concrete
                            // type2 > type1
                            return -1;
                        }
                        if (type2.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
                            // only type1 is concrete
                            return 1;
                        }
                        // both types are concrete
                        return 0;
                    }
                }));

            for (Entry<MediaType, Set<ObjectFactory<T>>> entry : compatibleList) {
                limitByType(list, entry.getValue(), cls);
            }
            return list;
        }

        public Set<MediaType> getProvidersMediaTypes(Class<?> type) {
            Set<MediaType> mediaTypes = new HashSet<MediaType>();

            l1: for (Entry<MediaType, Set<ObjectFactory<T>>> entry : data.entrySet()) {
                MediaType mediaType = entry.getKey();
                Set<ObjectFactory<T>> set = entry.getValue();
                for (ObjectFactory<T> t : set) {
                    if (GenericsUtils.isGenericInterfaceAssignableFrom(type,
                                                                       t.getInstanceClass(),
                                                                       rawType)) {
                        mediaTypes.add(mediaType);
                        continue l1;
                    }
                }
            }
            return mediaTypes;
        }

        private void limitByType(List<ObjectFactory<T>> list,
                                 Set<ObjectFactory<T>> set,
                                 Class<?> type) {
            if (set != null) {
                for (ObjectFactory<T> t : set) {
                    if (GenericsUtils.isGenericInterfaceAssignableFrom(type,
                                                                       t.getInstanceClass(),
                                                                       rawType)) {
                        list.add(t);
                    }
                }
            }
        }

        void put(MediaType key, ObjectFactory<T> objectFactory) {
            if (!key.getParameters().isEmpty()) {
                key = new MediaType(key.getType(), key.getSubtype());
            }
            Set<ObjectFactory<T>> set = data.get(key);
            if (set == null) {
                set = new TreeSet<ObjectFactory<T>>(Collections.reverseOrder());
                data.put(key, set);
            }
            if (!set.add(objectFactory)) {
                logger.warn(Messages.getMessage("mediaTypeSetAlreadyContains"), objectFactory);
            } else {
                // the set of providers has been changed so must clear the cache
                providersCache.clear();
            }
        }

        @Override
        public String toString() {
            return String.format("RawType: %s, Data: %s", String.valueOf(rawType), data.toString());
        }

    }

    private static class PriorityObjectFactory<T> implements ObjectFactory<T>,
        Comparable<PriorityObjectFactory<T>> {

        private final ObjectFactory<T> of;
        private final double           priority;

        public PriorityObjectFactory(ObjectFactory<T> of, double priority) {
            super();
            this.of = of;
            this.priority = priority;
        }

        public T getInstance(RuntimeContext context) {
            return of.getInstance(context);
        }

        public Class<T> getInstanceClass() {
            return of.getInstanceClass();
        }

        public int compareTo(PriorityObjectFactory<T> o) {
            int compare = Double.compare(priority, o.priority);
            // if the compare equals, the latest has the priority
            return compare == 0 ? -1 : compare;
        }

        @Override
        public String toString() {
            return String.format("Priority: %f, ObjectFactory: %s", priority, String.valueOf(of));
        }
    }

}
