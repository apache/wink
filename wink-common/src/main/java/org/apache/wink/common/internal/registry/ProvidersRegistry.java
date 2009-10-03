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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

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
import org.apache.wink.common.internal.utils.AnnotationUtils;
import org.apache.wink.common.internal.utils.GenericsUtils;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.internal.utils.SoftConcurrentMap;
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

    private static final Logger                                 logger             =
                                                                                       LoggerFactory
                                                                                           .getLogger(ProvidersRegistry.class);

    private final ProducesMediaTypeMap<ContextResolver<?>>      contextResolvers   =
                                                                                       new ProducesMediaTypeMap<ContextResolver<?>>(
                                                                                                                                    ContextResolver.class);
    /*
     * need exception mappers to be volatile for publication purposes
     */
    private volatile TreeSet<ObjectFactory<ExceptionMapper<?>>> exceptionMappers   =
                                                                                       new TreeSet<ObjectFactory<ExceptionMapper<?>>>(
                                                                                                                                      Collections
                                                                                                                                          .reverseOrder());
    private final ConsumesMediaTypeMap<MessageBodyReader<?>>    messageBodyReaders =
                                                                                       new ConsumesMediaTypeMap<MessageBodyReader<?>>(
                                                                                                                                      MessageBodyReader.class);
    private final ProducesMediaTypeMap<MessageBodyWriter<?>>    messageBodyWriters =
                                                                                       new ProducesMediaTypeMap<MessageBodyWriter<?>>(
                                                                                                                                      MessageBodyWriter.class);
    private final ApplicationValidator                          applicationValidator;
    private final LifecycleManagersRegistry                     factoryFactoryRegistry;

    public ProvidersRegistry(LifecycleManagersRegistry factoryRegistry,
                             ApplicationValidator applicationValidator) {
        this.factoryFactoryRegistry = factoryRegistry;
        this.applicationValidator = applicationValidator;
    }

    @SuppressWarnings("unchecked")
    public boolean addProvider(Class<?> cls, double priority) {
        if (cls == null) {
            throw new NullPointerException("cls");
        }
        ObjectFactory<?> objectFactory = factoryFactoryRegistry.getObjectFactory(cls);
        return addProvider(new PriorityObjectFactory(objectFactory, priority));

    }

    @SuppressWarnings("unchecked")
    public boolean addProvider(Object provider, double priority) {
        if (provider == null) {
            throw new NullPointerException("provider");
        }
        ObjectFactory<?> objectFactory = factoryFactoryRegistry.getObjectFactory(provider);
        return addProvider(new PriorityObjectFactory(objectFactory, priority));
    }

    @SuppressWarnings("unchecked")
    private synchronized boolean addProvider(ObjectFactory<?> objectFactory) {
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
            logger.debug("Adding type {} to ExceptionMappers list", cls);
            TreeSet<ObjectFactory<ExceptionMapper<?>>> exceptionMappersCopy =
                new TreeSet<ObjectFactory<ExceptionMapper<?>>>(Collections.reverseOrder());
            exceptionMappersCopy.addAll(exceptionMappers);
            exceptionMappersCopy.add((ObjectFactory<ExceptionMapper<?>>)objectFactory);
            exceptionMappers = exceptionMappersCopy;
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
        logger.debug("Getting ContextResolver for {} which has @Produces compatible with {}",
                     contextType,
                     mediaType);
        if (mediaType == null) {
            // see https://issues.apache.org/jira/browse/WINK-153
            mediaType = MediaType.WILDCARD_TYPE;
        }

        /*
         * performance improvement
         */
        if (contextResolvers.isMapEmpty()) {
            logger.debug("ContextResolvers MediaTypeMap was empty so returning null");
            return null;
        }

        final List<ObjectFactory<ContextResolver<?>>> factories =
            contextResolvers.getProvidersByMediaType(mediaType, contextType);

        if (factories.isEmpty()) {
            logger
                .debug("Did not find a ContextResolver for {} which has @Produces compatible with {}",
                       contextType,
                       mediaType);
            return null;
        }

        if (factories.size() == 1) {
            ObjectFactory<ContextResolver<?>> factory = factories.get(0);
            logger
                .debug("Found ContextResolver ObjectFactory {} for {} which has @Produces compatible with {}",
                       new Object[] {factory, contextType, mediaType});
            return (ContextResolver<T>)factory.getInstance(runtimeContext);
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

        logger
            .debug("Found multiple ContextResolver ObjectFactories {} for {} which has @Produces compatible with {} .  Using Proxy object which will call all matching ContextResolvers to find correct context.",
                   new Object[] {providers, contextType, mediaType});
        final MediaType mt = mediaType;
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
                                                                          Class<?> arg0 =
                                                                              (Class<?>)args[0];
                                                                          if (logger
                                                                              .isDebugEnabled()) {
                                                                              logger
                                                                                  .debug("Calling {}.getContext({}) to find context for {} with @Produces media type compatible with {}",
                                                                                         new Object[] {
                                                                                             resolver,
                                                                                             arg0,
                                                                                             contextType,
                                                                                             mt});
                                                                          }
                                                                          Object context =
                                                                              resolver
                                                                                  .getContext(arg0);
                                                                          if (context != null) {
                                                                              if (logger
                                                                                  .isDebugEnabled()) {
                                                                                  logger
                                                                                      .debug("Returning {} from calling {}.getContext({}) to find context for {} with @Produces media type compatible with {}",
                                                                                             new Object[] {
                                                                                                 context,
                                                                                                 resolver,
                                                                                                 arg0,
                                                                                                 contextType,
                                                                                                 mt});
                                                                              }
                                                                              return context;
                                                                          }
                                                                      }
                                                                      if (logger.isDebugEnabled()) {
                                                                          logger
                                                                              .debug("Did not find context for {} with @Produces media type compatible with {}",
                                                                                     new Object[] {
                                                                                         contextType,
                                                                                         mt});
                                                                      }
                                                                      return null;
                                                                  } else {
                                                                      return method.invoke(proxy,
                                                                                           args);
                                                                  }
                                                              }
                                                          });
    }

    @SuppressWarnings("unchecked")
    public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type,
                                                                       RuntimeContext runtimeContext) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        logger.debug("Getting ExceptionMapper for {} ", type);
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
            logger.debug("Did not find an ExceptionMapper for {} ", type);
            return null;
        }

        logger.debug("Found matching ExceptionMappers {} for type {} ", matchingMappers, type);
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

        ExceptionMapper<T> mapper = (ExceptionMapper<T>)matchingMappers.get(0);
        logger.debug("Found best matching ExceptionMapper {} for type {} ", mapper, type);
        return mapper;
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
        if (logger.isDebugEnabled()) {
            List<Annotation> anns = (annotations == null) ? null : Arrays.asList(annotations);
            logger
                .debug("Getting MessageBodyReader for class type {}, genericType {}, annotations {}, and media type {}",
                       new Object[] {type, genericType, anns, mediaType});
        }
        List<ObjectFactory<MessageBodyReader<?>>> factories =
            messageBodyReaders.getProvidersByMediaType(mediaType, type);

        logger.debug("Found possible MessageBodyReader ObjectFactories {}", factories);
        for (ObjectFactory<MessageBodyReader<?>> factory : factories) {
            MessageBodyReader<?> reader = factory.getInstance(runtimeContext);
            if (logger.isDebugEnabled()) {
                List<Annotation> anns = (annotations == null) ? null : Arrays.asList(annotations);
                logger.debug("Calling {}.isReadable( {}, {}, {}, {} )", new Object[] {reader, type,
                    genericType, anns, mediaType});
            }
            if (reader.isReadable(type, genericType, annotations, mediaType)) {
                if (logger.isDebugEnabled()) {
                    List<Annotation> anns =
                        (annotations == null) ? null : Arrays.asList(annotations);
                    logger.debug("{}.isReadable( {}, {}, {}, {} ) returned true", new Object[] {
                        reader, type, genericType, anns, mediaType});
                }
                return (MessageBodyReader<T>)reader;
            }
        }
        return null;
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
        if (logger.isDebugEnabled()) {
            List<Annotation> anns = (annotations == null) ? null : Arrays.asList(annotations);
            logger
                .debug("Getting MessageBodyWriter for class type {}, genericType {}, annotations {}, and media type {}",
                       new Object[] {type, genericType, anns, mediaType});
        }
        List<ObjectFactory<MessageBodyWriter<?>>> writersFactories =
            messageBodyWriters.getProvidersByMediaType(mediaType, type);
        logger.debug("Found possible MessageBodyWriter ObjectFactories {}", writersFactories);
        for (ObjectFactory<MessageBodyWriter<?>> factory : writersFactories) {
            MessageBodyWriter<?> writer = factory.getInstance(runtimeContext);
            if (logger.isDebugEnabled()) {
                List<Annotation> anns = (annotations == null) ? null : Arrays.asList(annotations);
                logger.debug("Calling {}.isWritable( {}, {}, {}, {} )", new Object[] {writer, type,
                    genericType, anns, mediaType});
            }
            if (writer.isWriteable(type, genericType, annotations, mediaType)) {
                if (logger.isDebugEnabled()) {
                    List<Annotation> anns =
                        (annotations == null) ? null : Arrays.asList(annotations);
                    logger.debug("{}.isWritable( {}, {}, {}, {} ) returned true", new Object[] {
                        writer, type, genericType, anns, mediaType});
                }
                return (MessageBodyWriter<T>)writer;
            }
        }
        return null;
    }

    public Set<MediaType> getMessageBodyReaderMediaTypesLimitByIsReadable(Class<?> type,
                                                                          RuntimeContext runtimeContext) {
        Set<MediaType> mediaTypes = new HashSet<MediaType>();
        logger.debug("Searching MessageBodyReaders media types limited by class type {}", type);

        List<ObjectFactory<MessageBodyReader<?>>> readerFactories =
            messageBodyReaders.getProvidersByMediaType(MediaType.WILDCARD_TYPE, type);
        logger.debug("Found all MessageBodyReader ObjectFactories limited by class type {}",
                     readerFactories);
        Annotation[] ann = new Annotation[0];
        for (ObjectFactory<MessageBodyReader<?>> factory : readerFactories) {
            MessageBodyReader<?> reader = factory.getInstance(runtimeContext);
            Consumes consumes = factory.getInstanceClass().getAnnotation(Consumes.class);
            String[] values = null;
            if (consumes != null) {
                values = AnnotationUtils.parseConsumesProducesValues(consumes.value());
            } else {
                values = new String[] {MediaType.WILDCARD};
            }
            for (String v : values) {
                MediaType mt = MediaType.valueOf(v);
                if (logger.isDebugEnabled()) {
                    List<Annotation> anns = (ann == null) ? null : Arrays.asList(ann);
                    logger.debug("Calling {}.isReadable( {}, {}, {}, {} )", new Object[] {reader,
                        type, type, anns, mt});
                }
                if (reader.isReadable(type, type, ann, mt)) {
                    logger.debug("Adding {} to media type set", mt);
                    mediaTypes.add(mt);
                }
            }
        }
        logger
            .debug("Found {} from @Consumes values from all MessageBodyReader ObjectFactories compatible with Java type {}",
                   mediaTypes,
                   type);
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
                String[] values = AnnotationUtils.parseConsumesProducesValues(produces.value());
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
                String[] values = AnnotationUtils.parseConsumesProducesValues(consumes.value());
                for (String val : values) {
                    put(MediaType.valueOf(val), objectFactory);
                }
            }
        }
    }

    private abstract class MediaTypeMap<T> {

        private volatile HashMap<MediaType, HashSet<ObjectFactory<T>>>                                  data           =
                                                                                                                           new HashMap<MediaType, HashSet<ObjectFactory<T>>>();
        @SuppressWarnings("unchecked")
        private volatile Entry<MediaType, HashSet<ObjectFactory<T>>>[]                                  entrySet       =
                                                                                                                           data
                                                                                                                               .entrySet()
                                                                                                                               .toArray(new Entry[0]);
        private final Class<?>                                                                          rawType;

        private final SoftConcurrentMap<Class<?>, SoftConcurrentMap<MediaType, List<ObjectFactory<T>>>> providersCache =
                                                                                                                           new SoftConcurrentMap<Class<?>, SoftConcurrentMap<MediaType, List<ObjectFactory<T>>>>(); ;

        public MediaTypeMap(Class<?> rawType) {
            super();
            this.rawType = rawType;
        }

        boolean isMapEmpty() {
            return data.isEmpty();
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

            logger
                .debug("Getting providers by media type by calling getProvidersByMediaType({}, {})",
                       mediaType,
                       cls);
            SoftConcurrentMap<MediaType, List<ObjectFactory<T>>> mediaTypeToProvidersCache =
                providersCache.get(cls);
            if (mediaTypeToProvidersCache == null) {
                logger
                    .debug("MediaType to providers cache for class {} does not exist so creating",
                           cls);
                mediaTypeToProvidersCache =
                    new SoftConcurrentMap<MediaType, List<ObjectFactory<T>>>();
                providersCache.put(cls, mediaTypeToProvidersCache);
            }

            List<ObjectFactory<T>> list = mediaTypeToProvidersCache.get(mediaType);

            logger.debug("Get media type to providers cache for media type {} resulted in {}",
                         mediaType,
                         list);
            if (list == null) {
                list = internalGetProvidersByMediaType(mediaType, cls);
                mediaTypeToProvidersCache.put(mediaType, list);
            }

            return list;
        }

        private List<ObjectFactory<T>> internalGetProvidersByMediaType(MediaType mediaType,
                                                                       Class<?> cls) {
            Set<ObjectFactory<T>> compatible =
                new TreeSet<ObjectFactory<T>>(Collections.reverseOrder());
            for (Entry<MediaType, HashSet<ObjectFactory<T>>> entry : entrySet) {
                if (entry.getKey().isCompatible(mediaType)) {
                    // media type is compatible, check generic type of the
                    // subset
                    for (ObjectFactory<T> of : entry.getValue()) {
                        if (GenericsUtils.isGenericInterfaceAssignableFrom(cls, of
                            .getInstanceClass(), rawType)) {
                            // Both media type and generic types are compatible.
                            // The assumption here that more specific media
                            // types are added first so replacing the entity
                            // with the same object factory of the different
                            // media type, won't change the map.

                            // This is done via the equals() of the OFHolder
                            // which doesn't compare the MediaType
                            compatible.add(new OFHolder<T>(entry.getKey(), of));
                        }
                    }
                }
            }
            @SuppressWarnings("unchecked")
            ObjectFactory<T>[] tmp = compatible.toArray(new ObjectFactory[compatible.size()]);
            return Arrays.asList(tmp);
        }

        public Set<MediaType> getProvidersMediaTypes(Class<?> type) {
            Set<MediaType> mediaTypes = new HashSet<MediaType>();

            l1: for (Entry<MediaType, HashSet<ObjectFactory<T>>> entry : data.entrySet()) {
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

        @SuppressWarnings("unchecked")
        synchronized void put(MediaType key, ObjectFactory<T> objectFactory) {
            HashMap<MediaType, HashSet<ObjectFactory<T>>> copyOfMap =
                new HashMap<MediaType, HashSet<ObjectFactory<T>>>(data);
            if (!key.getParameters().isEmpty()) {
                key = new MediaType(key.getType(), key.getSubtype());
            }
            HashSet<ObjectFactory<T>> set = data.get(key);
            if (set == null) {
                set = new HashSet<ObjectFactory<T>>();
            } else {
                set = new HashSet<ObjectFactory<T>>(set);
            }
            copyOfMap.put(key, set);
            if (!set.add(objectFactory)) {
                logger.warn(Messages.getMessage("mediaTypeSetAlreadyContains"), objectFactory);
            } else {

                // need to resort the entry set
                Entry<MediaType, HashSet<ObjectFactory<T>>>[] newEntrySet =
                    copyOfMap.entrySet().toArray(new Entry[0]);
                // It's important to sort the media types here to ensure that
                // provider of the more dominant media type will precede, when
                // adding to the compatible set.
                Arrays.sort(newEntrySet, Collections
                    .reverseOrder(new Comparator<Entry<MediaType, HashSet<ObjectFactory<T>>>>() {

                        public int compare(Entry<MediaType, HashSet<ObjectFactory<T>>> o1,
                                           Entry<MediaType, HashSet<ObjectFactory<T>>> o2) {
                            return MediaTypeUtils.compareTo(o1.getKey(), o2.getKey());
                        }
                    }));

                if (logger.isDebugEnabled()) {
                    logger.debug("Added ObjectFactory {} with MediaType {} to MediaTypeMap {}",
                                 new Object[] {objectFactory, key, this});
                    logger.debug("EntrySet is {}", newEntrySet);
                }
                entrySet = newEntrySet;
                data = copyOfMap;

                // the set of providers has been changed so must clear the cache
                providersCache.clear();
                logger.debug("Cleared the providers cache");
            }
        }

        @Override
        public String toString() {
            return toString("  ");
        }

        protected String toString(String indent) {
            StringBuffer sb = new StringBuffer();

            sb.append("\nRawType: ");
            sb.append(String.valueOf(rawType));
            sb.append("\nData Map: ");
            if (data.isEmpty()) {
                sb.append("{empty}");
            } else {
                sb.append("\n");
            }

            // The data Map can be huge. Separate entries
            // to make it more understandable
            for (MediaType k : data.keySet()) {
                sb.append("MediaType key = ");
                sb.append(k);
                sb.append("\n");
                sb.append("ObjectFactory Set value = {\n");

                // Separate each ObjectFactory entry in the Set
                // into its own line
                for (ObjectFactory<T> of : data.get(k)) {
                    sb.append(indent);
                    sb.append(of);
                    sb.append("\n");
                }
                sb.append("}\n");
            }
            return sb.toString();
        }

        private class OFHolder<T> implements ObjectFactory<T>, Comparable<OFHolder<T>> {

            private final PriorityObjectFactory<T> of;
            private final MediaType                mediaType;
            private final Class<?>                 genericType;

            public OFHolder(MediaType mediaType, ObjectFactory<T> of) {
                super();
                this.of = (PriorityObjectFactory<T>)of;
                this.mediaType = mediaType;
                genericType =
                    GenericsUtils.getClassType(GenericsUtils.getGenericInterfaceParamType(of
                        .getInstanceClass(), rawType));
            }

            @Override
            public String toString() {
                return "OFHolder [" + (genericType != null ? "genericType=" + genericType + ", "
                    : "")
                    + (mediaType != null ? "mediaType=" + mediaType + ", " : "")
                    + (of != null ? "of=" + of : "")
                    + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((of == null) ? 0 : of.hashCode());
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                OFHolder<?> other = (OFHolder<?>)obj;
                if (of == null) {
                    if (other.of != null) {
                        return false;
                    }
                } else if (of != other.of) {
                    return false;
                }
                return true;
            }

            public T getInstance(RuntimeContext context) {
                return of.getInstance(context);
            }

            public Class<T> getInstanceClass() {
                return of.getInstanceClass();
            }

            private static final double MAX_SYSTEM_PRIORITY = WinkApplication.SYSTEM_PRIORITY + 0.1;

            public int compareTo(OFHolder<T> o) {
                // check if this is a system provider
                // system providers are less than
                // WinkApplication.SYSTEM_PRIORITY + 0.1 (they start at
                // WinkApplication.SYSTEM_PRIORITY and
                // unless there are 10000000000, this shouldn't matter)
                if (of.priority < MAX_SYSTEM_PRIORITY) {
                    // this is a system provider
                    if (o.of.priority > MAX_SYSTEM_PRIORITY) {
                        // the other is a user provider so this is > 0.2
                        return -1;
                    }
                } else if (o.of.priority < MAX_SYSTEM_PRIORITY) {
                    // the other is a system provider
                    if (of.priority > MAX_SYSTEM_PRIORITY) {
                        // this is a user provider
                        return 1;
                    }
                }

                // first compare by media type
                int compare = MediaTypeUtils.compareTo(mediaType, o.mediaType);
                if (compare != 0) {
                    return compare;
                }
                // second compare by generic type
                if (genericType != o.genericType) {
                    if (genericType.isAssignableFrom(o.genericType)) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
                // last compare by priority
                return Double.compare(of.priority, o.of.priority);
            }
        }
    }

    private static class PriorityObjectFactory<T> implements ObjectFactory<T>,
        Comparable<PriorityObjectFactory<T>> {

        private final ObjectFactory<T> of;
        private final double           priority;
        private static double          counter = 0.00000000001;
        private static final double    inc     = 0.00000000001;

        public PriorityObjectFactory(ObjectFactory<T> of, double priority) {
            super();
            this.of = of;
            this.priority = priority + (counter += inc);
        }

        public T getInstance(RuntimeContext context) {
            return of.getInstance(context);
        }

        public Class<T> getInstanceClass() {
            return of.getInstanceClass();
        }

        // this compare is used by exception mappers
        public int compareTo(PriorityObjectFactory<T> o) {
            return Double.compare(priority, o.priority);
        }

        @Override
        public String toString() {
            return String.format("Priority: %f, ObjectFactory: %s", priority, String.valueOf(of));
        }
    }

}
