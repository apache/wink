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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

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
import org.apache.wink.common.internal.log.Providers;
import org.apache.wink.common.internal.utils.AnnotationUtils;
import org.apache.wink.common.internal.utils.GenericsUtils;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.internal.utils.SoftConcurrentMap;
import org.apache.wink.common.utils.ProviderUtils;
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

    private static final Logger                                         logger             =
                                                                                               LoggerFactory
                                                                                                   .getLogger(ProvidersRegistry.class);

    private final ProducesMediaTypeMap<ContextResolver<?>>              contextResolvers   =
                                                                                               new ProducesMediaTypeMap<ContextResolver<?>>(
                                                                                                                                            ContextResolver.class);
    /*
     * need exception mappers to be volatile for publication purposes
     */
    private volatile TreeSet<PriorityObjectFactory<ExceptionMapper<?>>> exceptionMappers   =
                                                                                               new TreeSet<PriorityObjectFactory<ExceptionMapper<?>>>(
                                                                                                                                                      Collections
                                                                                                                                                          .reverseOrder());
    private final ConsumesMediaTypeMap<MessageBodyReader<?>>            messageBodyReaders =
                                                                                               new ConsumesMediaTypeMap<MessageBodyReader<?>>(
                                                                                                                                              MessageBodyReader.class);
    private final ProducesMediaTypeMap<MessageBodyWriter<?>>            messageBodyWriters =
                                                                                               new ProducesMediaTypeMap<MessageBodyWriter<?>>(
                                                                                                                                              MessageBodyWriter.class);
    private final ApplicationValidator                                  applicationValidator;
    private final LifecycleManagersRegistry                             factoryFactoryRegistry;

    public ProvidersRegistry(LifecycleManagersRegistry factoryRegistry,
                             ApplicationValidator applicationValidator) {
        this.factoryFactoryRegistry = factoryRegistry;
        this.applicationValidator = applicationValidator;
    }

    public boolean addProvider(Class<?> cls, double priority) {
        return addProvider(cls, priority, false);
    }

    @SuppressWarnings("unchecked")
    public boolean addProvider(Class<?> cls, double priority, boolean isSystemProvider) {
        if (cls == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "cls")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        ObjectFactory<?> objectFactory = factoryFactoryRegistry.getObjectFactory(cls);
        return addProvider(new PriorityObjectFactory(objectFactory, priority, isSystemProvider));

    }

    public boolean addProvider(Object provider, double priority) {
        return addProvider(provider, priority, false);
    }

    @SuppressWarnings("unchecked")
    public boolean addProvider(Object provider, double priority, boolean isSystemProvider) {
        if (provider == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "provider")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        ObjectFactory<?> objectFactory = factoryFactoryRegistry.getObjectFactory(provider);
        return addProvider(new PriorityObjectFactory(objectFactory, priority, isSystemProvider));
    }

    @SuppressWarnings("unchecked")
    private synchronized boolean addProvider(PriorityObjectFactory<?> objectFactory) {
        Class<? extends Object> cls = objectFactory.getInstanceClass();

        logger.trace("Processing provider of type {}", cls); //$NON-NLS-1$

        boolean retValue = false;

        if (!applicationValidator.isValidProvider(cls)) {
            return retValue;
        }

        if (ContextResolver.class.isAssignableFrom(cls)) {
            contextResolvers.putProvider((PriorityObjectFactory<ContextResolver<?>>)objectFactory);
            retValue = true;
        }
        if (ExceptionMapper.class.isAssignableFrom(cls)) {
            logger.trace("Adding type {} to ExceptionMappers list", cls); //$NON-NLS-1$
            TreeSet<PriorityObjectFactory<ExceptionMapper<?>>> exceptionMappersCopy =
                new TreeSet<PriorityObjectFactory<ExceptionMapper<?>>>(Collections.reverseOrder());
            exceptionMappersCopy.addAll(exceptionMappers);
            exceptionMappersCopy.add((PriorityObjectFactory<ExceptionMapper<?>>)objectFactory);
            exceptionMappers = exceptionMappersCopy;
            retValue = true;
        }
        if (MessageBodyReader.class.isAssignableFrom(cls)) {
            messageBodyReaders
                .putProvider((PriorityObjectFactory<MessageBodyReader<?>>)objectFactory);
            retValue = true;
        }
        if (MessageBodyWriter.class.isAssignableFrom(cls)) {
            messageBodyWriters
                .putProvider((PriorityObjectFactory<MessageBodyWriter<?>>)objectFactory);
            retValue = true;
        }
        if (retValue == false) {
            if (logger.isWarnEnabled()) {
                logger.warn(Messages.getMessage("classIsUnknownProvider", cls)); //$NON-NLS-1$
            }
        }
        return retValue;

    }

    public boolean addProvider(Class<?> cls) {
        return addProvider(cls, WinkApplication.DEFAULT_PRIORITY);
    }

    public boolean addProvider(Object provider) {
        return addProvider(provider, WinkApplication.DEFAULT_PRIORITY);
    }

    public List<ProviderRecord<?>> getMessageBodyWriterRecords() {
        return new ArrayList<ProviderRecord<?>>(messageBodyWriters.getProviderRecords());
    }

    public List<ProviderRecord<?>> getMessageBodyReaderRecords() {
        return new ArrayList<ProviderRecord<?>>(messageBodyReaders.getProviderRecords());
    }

    public List<ProviderRecord<?>> getExceptionMapperRecords() {
        ArrayList<ProviderRecord<?>> recordList = new ArrayList<ProviderRecord<?>>();

        for (PriorityObjectFactory<ExceptionMapper<?>> factory : exceptionMappers) {
            ProviderRecord<?> record =
                new ProviderRecord<ExceptionMapper<?>>(factory.getInstanceClass(), null,
                                                       ExceptionMapper.class,
                                                       factory.isSystemProvider);
            recordList.add(record);
        }

        return recordList;
    }

    public List<ProviderRecord<?>> getContextResolverRecords() {
        return new ArrayList<ProviderRecord<?>>(contextResolvers.getProviderRecords());
    }

    /**
     * Removes all providers in the registry.
     */
    public void removeAllProviders() {
        contextResolvers.removeAll();
        messageBodyReaders.removeAll();
        messageBodyWriters.removeAll();

        for (ObjectFactory<?> of : exceptionMappers) {
            of.releaseAll(null);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ContextResolver<T> getContextResolver(final Class<T> contextType,
                                                     MediaType mediaType,
                                                     RuntimeContext runtimeContext) {
        if (contextType == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "contextType")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        logger.trace("Getting ContextResolver for {} which has @Produces compatible with {}", //$NON-NLS-1$
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
            logger.trace("ContextResolvers MediaTypeMap was empty so returning null"); //$NON-NLS-1$
            return null;
        }

        final List<MediaTypeMap<ContextResolver<?>>.OFHolder<ContextResolver<?>>> factories =
            contextResolvers.getProvidersByMediaType(mediaType, contextType);

        if (factories.isEmpty()) {
            logger
                .trace("Did not find a ContextResolver for {} which has @Produces compatible with {}", //$NON-NLS-1$
                       contextType,
                       mediaType);
            return null;
        }

        if (factories.size() == 1) {
            ObjectFactory<ContextResolver<?>> factory = factories.get(0);
            logger
                .trace("Found ContextResolver ObjectFactory {} for {} which has @Produces compatible with {}", //$NON-NLS-1$
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
            .trace("Found multiple ContextResolver ObjectFactories {} for {} which has @Produces compatible with {} .  Using Proxy object which will call all matching ContextResolvers to find correct context.", //$NON-NLS-1$
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
                                                                      .equals("getContext") && args != null //$NON-NLS-1$
                                                                      && args.length == 1
                                                                      && (args[0] == null || args[0]
                                                                          .getClass()
                                                                          .equals(Class.class))) {
                                                                      for (ContextResolver<?> resolver : providers) {
                                                                          Class<?> arg0 =
                                                                              (Class<?>)args[0];
                                                                          if (logger
                                                                              .isTraceEnabled()) {
                                                                              logger
                                                                                  .trace("Calling {}.getContext({}) to find context for {} with @Produces media type compatible with {}", //$NON-NLS-1$
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
                                                                                  .isTraceEnabled()) {
                                                                                  logger
                                                                                      .trace("Returning {} from calling {}.getContext({}) to find context for {} with @Produces media type compatible with {}", //$NON-NLS-1$
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
                                                                      if (logger.isTraceEnabled()) {
                                                                          logger
                                                                              .trace("Did not find context for {} with @Produces media type compatible with {}", //$NON-NLS-1$
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
            throw new NullPointerException(Messages.getMessage("variableIsNull", "type")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        logger.trace("Getting ExceptionMapper for {} ", type); //$NON-NLS-1$
        List<ExceptionMapper<?>> matchingMappers = new ArrayList<ExceptionMapper<?>>();

        for (ObjectFactory<ExceptionMapper<?>> factory : exceptionMappers) {
            ExceptionMapper<?> exceptionMapper = factory.getInstance(runtimeContext);
            Type genericType =
                GenericsUtils.getGenericInterfaceParamType(exceptionMapper.getClass(),
                                                           ExceptionMapper.class);
            Class<?> classType = GenericsUtils.getClassType(genericType, null);
            if (classType.isAssignableFrom(type)) {
                matchingMappers.add(exceptionMapper);
            }
        }

        if (matchingMappers.isEmpty()) {
            logger.trace("Did not find an ExceptionMapper for {} ", type); //$NON-NLS-1$
            return null;
        }

        logger.trace("Found matching ExceptionMappers {} for type {} ", matchingMappers, type); //$NON-NLS-1$
        while (matchingMappers.size() > 1) {
            Type first =
                GenericsUtils.getGenericInterfaceParamType(matchingMappers.get(0).getClass(),
                                                           ExceptionMapper.class);
            Type second =
                GenericsUtils.getGenericInterfaceParamType(matchingMappers.get(1).getClass(),
                                                           ExceptionMapper.class);
            Class<?> firstClass = GenericsUtils.getClassType(first, null);
            Class<?> secondClass = GenericsUtils.getClassType(second, null);
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
        logger.trace("Found best matching ExceptionMapper {} for type {} ", mapper, type); //$NON-NLS-1$
        return mapper;
    }

    @SuppressWarnings("unchecked")
    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type,
                                                         Type genericType,
                                                         Annotation[] annotations,
                                                         MediaType mediaType,
                                                         RuntimeContext runtimeContext) {
        if (type == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "type")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (mediaType == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "mediaType")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (logger.isTraceEnabled()) {
            List<Annotation> anns = (annotations == null) ? null : Arrays.asList(annotations);
            logger
                .trace("Getting MessageBodyReader for class type {}, genericType {}, annotations {}, and media type {}", //$NON-NLS-1$
                       new Object[] {type, genericType, anns, mediaType});
        }
        List<MediaTypeMap<MessageBodyReader<?>>.OFHolder<MessageBodyReader<?>>> factories =
            messageBodyReaders.getProvidersByMediaType(mediaType, type);

        logger.trace("Found possible MessageBodyReader ObjectFactories {}", factories); //$NON-NLS-1$
        Providers providersLogger = new Providers();
        MessageBodyReader<T> ret = null;
        for (MediaTypeMap<MessageBodyReader<?>>.OFHolder<MessageBodyReader<?>> factory : factories) {
            MessageBodyReader<?> reader = factory.getInstance(runtimeContext);
            if (isReadable(reader, type, genericType, annotations, mediaType, runtimeContext, factory.isSystemProvider)) {
                ret = (MessageBodyReader<T>)reader;
                providersLogger.addMessageBodyReader(reader, true);
                break;
            } else {
                providersLogger.addMessageBodyReader(reader, false);
            }
        }
        providersLogger.log();
        return ret;
    }

    @SuppressWarnings("unchecked")
    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type,
                                                         Type genericType,
                                                         Annotation[] annotations,
                                                         MediaType mediaType,
                                                         RuntimeContext runtimeContext) {
        if (type == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "type")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (mediaType == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "mediaType")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (logger.isTraceEnabled()) {
            List<Annotation> anns = (annotations == null) ? null : Arrays.asList(annotations);
            logger
                .trace("Getting MessageBodyWriter for class type {}, genericType {}, annotations {}, and media type {}", //$NON-NLS-1$
                       new Object[] {type, genericType, anns, mediaType});
        }
        List<MediaTypeMap<MessageBodyWriter<?>>.OFHolder<MessageBodyWriter<?>>> writersFactories =
            messageBodyWriters.getProvidersByMediaType(mediaType, type);
        logger.trace("Found possible MessageBodyWriter ObjectFactories {}", writersFactories); //$NON-NLS-1$
        Providers providersLogger = new Providers();
        MessageBodyWriter<T> ret = null;
        for (MediaTypeMap<MessageBodyWriter<?>>.OFHolder<MessageBodyWriter<?>> factory : writersFactories) {
            MessageBodyWriter<?> writer = factory.getInstance(runtimeContext);
            if (isWriteable(writer, type, genericType, annotations, mediaType, runtimeContext, factory.isSystemProvider)) {
                if (logger.isTraceEnabled()) {
                    List<Annotation> anns =
                        (annotations == null) ? null : Arrays.asList(annotations);
                    logger.trace("{}.isWriteable( {}, {}, {}, {} ) returned true", new Object[] { //$NON-NLS-1$
                                 writer, type, genericType, anns, mediaType});
                }
                ret = (MessageBodyWriter<T>)writer;
                providersLogger.addMessageBodyWriter(writer, true);
                break;
            } else {
                providersLogger.addMessageBodyWriter(writer, false);
            }
        }
        if (ret == null && logger.isTraceEnabled()) {
            List<Annotation> anns = (annotations == null) ? null : Arrays.asList(annotations);
            logger
                .trace("No MessageBodyWriter returned true for isWriteable( {}, {}, {}, {} )", new Object[] { //$NON-NLS-1$
                       type, genericType, anns, mediaType});
        }
        providersLogger.log();
        return ret;
    }

    public Set<MediaType> getMessageBodyReaderMediaTypesLimitByIsReadable(Class<?> type,
                                                                          RuntimeContext runtimeContext) {
        Set<MediaType> mediaTypes = new HashSet<MediaType>();
        logger.trace("Searching MessageBodyReaders media types limited by class type {}", type); //$NON-NLS-1$

        List<MediaTypeMap<MessageBodyReader<?>>.OFHolder<MessageBodyReader<?>>> readerFactories =
            messageBodyReaders.getProvidersByMediaType(MediaType.WILDCARD_TYPE, type);
        logger.trace("Found all MessageBodyReader ObjectFactories limited by class type {}", //$NON-NLS-1$
                     readerFactories);
        Annotation[] ann = new Annotation[0];
        for (MediaTypeMap<MessageBodyReader<?>>.OFHolder<MessageBodyReader<?>> factory : readerFactories) {
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
                if (isReadable(reader, type, type, ann, mt, runtimeContext, factory.isSystemProvider)) {
                    logger.trace("Adding {} to media type set", mt); //$NON-NLS-1$
                    mediaTypes.add(mt);
                }
            }
        }
        logger
            .trace("Found {} from @Consumes values from all MessageBodyReader ObjectFactories compatible with Java type {}", //$NON-NLS-1$
                   mediaTypes,
                   type);
        return mediaTypes;
    }

    public MediaType getMessageBodyWriterMediaTypeLimitByIsWritable(Class<?> type,
                                                                    RuntimeContext runtimeContext) {
        logger.trace("Searching MessageBodyWriters media types limited by class type {}", type); //$NON-NLS-1$

        List<MediaTypeMap<MessageBodyWriter<?>>.OFHolder<MessageBodyWriter<?>>> writerFactories =
            messageBodyWriters.getProvidersByMediaType(MediaType.WILDCARD_TYPE, type);
        logger.trace("Found all MessageBodyWriter ObjectFactories limited by class type {}", //$NON-NLS-1$
                     writerFactories);
        Annotation[] ann = new Annotation[0];
        for (MediaTypeMap<MessageBodyWriter<?>>.OFHolder<MessageBodyWriter<?>> factory : writerFactories) {
            MessageBodyWriter<?> writer = factory.getInstance(runtimeContext);
            Produces produces = factory.getInstanceClass().getAnnotation(Produces.class);
            String[] values = null;
            if (produces != null) {
                values = AnnotationUtils.parseConsumesProducesValues(produces.value());
            } else {
                values = new String[] {MediaType.WILDCARD};
            }
            for (String v : values) {
                MediaType mt = MediaType.valueOf(v);
                if (isWriteable(writer, type, type, ann, mt, runtimeContext, factory.isSystemProvider)) {
                    logger.trace("Returning media type {}", mt); //$NON-NLS-1$
                    return mt;
                }
            }
        }
        return null;
    }

    /**
     * @param factory
     * @param type
     * @param ann
     * @param reader
     * @param mt
     * @param runtimeContext
     * @return
     */
    private boolean isReadable(
            MessageBodyReader<?> reader,
            Class<?> type, Type genericType, Annotation[] ann,
            MediaType mt, RuntimeContext runtimeContext, boolean isSystemProvider) {
        if (logger.isTraceEnabled()) {
            List<Annotation> anns = (ann == null) ? null : Arrays.asList(ann);
            logger.trace("Calling {}.isReadable( {}, {}, {}, {} )", new Object[] {reader, //$NON-NLS-1$
                type, genericType, anns, mt});
        }
        try {
            return reader.isReadable(type, genericType, ann, mt);
        } catch (RuntimeException ex) {
            ProviderUtils.logUserProviderException(ex, reader, ProviderUtils.PROVIDER_EXCEPTION_ORIGINATOR.isReadable, new Object[]{type, genericType, ann, mt}, runtimeContext);
            throw ex;
        }
    }


    /**
     * @param factory
     * @param type
     * @param genericType
     * @param ann
     * @param mt
     * @param runtimeContext
     * @return
     */
    private boolean isWriteable(
            MessageBodyWriter<?> writer,
            Class<?> type, Type genericType, Annotation[] ann, 
            MediaType mt, RuntimeContext runtimeContext, boolean isSystemProvider) {
        if (logger.isTraceEnabled()) {
            List<Annotation> anns = (ann == null) ? null : Arrays.asList(ann);
            logger.trace("Calling {}.isWritable( {}, {}, {}, {} )", new Object[] {writer, //$NON-NLS-1$
                type, genericType, anns, mt});
        }
        try {
            return writer.isWriteable(type, genericType, ann, mt);
        } catch (RuntimeException ex) {
            ProviderUtils.logUserProviderException(ex, writer, ProviderUtils.PROVIDER_EXCEPTION_ORIGINATOR.isWriteable, new Object[]{type, genericType, ann, mt}, runtimeContext);
            throw ex;
        }
    }

    public Set<MediaType> getMessageBodyWriterMediaTypes(Class<?> type) {
        if (type == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "type")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        Set<MediaType> mediaTypes = messageBodyWriters.getProvidersMediaTypes(type);
        return mediaTypes;
    }

    private class ProducesMediaTypeMap<T> extends MediaTypeMap<T> {

        public ProducesMediaTypeMap(Class<?> rawType) {
            super(rawType);
        }

        public void putProvider(PriorityObjectFactory<T> objectFactory) {
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

        public void putProvider(PriorityObjectFactory<T> objectFactory) {
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

        private volatile HashMap<MediaType, HashSet<PriorityObjectFactory<T>>>                     data           =
                                                                                                                      new HashMap<MediaType, HashSet<PriorityObjectFactory<T>>>();
        @SuppressWarnings("unchecked")
        private volatile Entry<MediaType, HashSet<PriorityObjectFactory<T>>>[]                     entrySet       =
                                                                                                                      data
                                                                                                                          .entrySet()
                                                                                                                          .toArray(new Entry[0]);
        private final Class<?>                                                                     rawType;

        private final SoftConcurrentMap<Class<?>, SoftConcurrentMap<MediaType, List<OFHolder<T>>>> providersCache =
                                                                                                                      new SoftConcurrentMap<Class<?>, SoftConcurrentMap<MediaType, List<OFHolder<T>>>>(); ;

        public MediaTypeMap(Class<?> rawType) {
            super();
            this.rawType = rawType;
        }

        boolean isMapEmpty() {
            return data.isEmpty();
        }

        @SuppressWarnings("unchecked")
        synchronized void removeAll() {
            // order of operations for the next 4 lines matter
            Entry<MediaType, HashSet<PriorityObjectFactory<T>>>[] oldEntrySet = entrySet;
            entrySet = data.entrySet().toArray(new Entry[0]);
            data = new HashMap<MediaType, HashSet<PriorityObjectFactory<T>>>();
            providersCache.clear();

            for (Entry<MediaType, HashSet<PriorityObjectFactory<T>>> entry : oldEntrySet) {
                HashSet<PriorityObjectFactory<T>> set = entry.getValue();
                for (PriorityObjectFactory<T> of : set) {
                    of.releaseAll(null);
                }
            }
        }

        /**
         * returns providers by mediaType and by type
         * 
         * @param mediaType
         * @param cls
         * @return
         */
        public List<OFHolder<T>> getProvidersByMediaType(MediaType mediaType, Class<?> cls) {
            String subtype = mediaType.getSubtype();
            String type = mediaType.getType();
            if (!mediaType.getParameters().isEmpty()) {
                mediaType = new MediaType(type, subtype);
            }

            logger
                .trace("Getting providers by media type by calling getProvidersByMediaType({}, {})", //$NON-NLS-1$
                       mediaType,
                       cls);
            SoftConcurrentMap<MediaType, List<OFHolder<T>>> mediaTypeToProvidersCache =
                providersCache.get(cls);
            if (mediaTypeToProvidersCache == null) {
                logger
                    .trace("MediaType to providers cache for class {} does not exist so creating", //$NON-NLS-1$
                           cls);
                mediaTypeToProvidersCache = new SoftConcurrentMap<MediaType, List<OFHolder<T>>>();
                providersCache.put(cls, mediaTypeToProvidersCache);
            }

            List<OFHolder<T>> list = mediaTypeToProvidersCache.get(mediaType);

            logger.trace("Get media type to providers cache for media type {} resulted in {}", //$NON-NLS-1$
                         mediaType,
                         list);
            if (list == null) {
                list = internalGetProvidersByMediaType(mediaType, cls);
                mediaTypeToProvidersCache.put(mediaType, list);
            }

            return list;
        }

        public Collection<ProviderRecord<T>> getProviderRecords() {
            List<ProviderRecord<T>> compatible = new ArrayList<ProviderRecord<T>>();

            Entry<MediaType, HashSet<PriorityObjectFactory<T>>>[] registryEntrySet = entrySet;
            for (Entry<MediaType, HashSet<PriorityObjectFactory<T>>> entry : registryEntrySet) {
                TreeSet<PriorityObjectFactory<T>> entries =
                    new TreeSet<PriorityObjectFactory<T>>(Collections.reverseOrder());
                entries.addAll(entry.getValue());

                for (PriorityObjectFactory<T> of : entries) {
                    compatible.add(new ProviderRecord<T>(of.getInstanceClass(), entry.getKey(),
                                                         rawType, of.isSystemProvider));
                }
            }
            return compatible;
        }

        private List<OFHolder<T>> internalGetProvidersByMediaType(MediaType mediaType, Class<?> cls) {
            Set<OFHolder<T>> compatible = new TreeSet<OFHolder<T>>(Collections.reverseOrder());
            for (Entry<MediaType, HashSet<PriorityObjectFactory<T>>> entry : entrySet) {
                if (entry.getKey().isCompatible(mediaType)) {
                    // media type is compatible, check generic type of the
                    // subset
                    for (PriorityObjectFactory<T> of : entry.getValue()) {
                        if (GenericsUtils.isGenericInterfaceAssignableFrom(cls, of
                            .getInstanceClass(), rawType)) {
                            // Both media type and generic types are compatible.
                            // The assumption here that more specific media
                            // types are added first so replacing the entity
                            // with the same object factory of the different
                            // media type, won't change the map.

                            // This is done via the equals() of the OFHolder
                            // which doesn't compare the MediaType
                            compatible
                                .add(new OFHolder<T>(entry.getKey(), of, of.isSystemProvider));
                        }
                    }
                }
            }
            @SuppressWarnings("unchecked")
            OFHolder<T>[] tmp = compatible.toArray(new OFHolder[compatible.size()]);
            return Arrays.asList(tmp);
        }

        public Set<MediaType> getProvidersMediaTypes(Class<?> type) {
            Set<MediaType> mediaTypes = new LinkedHashSet<MediaType>();

            l1: for (Entry<MediaType, HashSet<PriorityObjectFactory<T>>> entry : entrySet) {
                MediaType mediaType = entry.getKey();
                Set<PriorityObjectFactory<T>> set = entry.getValue();
                for (PriorityObjectFactory<T> t : set) {
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
        synchronized void put(MediaType key, PriorityObjectFactory<T> objectFactory) {
            HashMap<MediaType, HashSet<PriorityObjectFactory<T>>> copyOfMap =
                new HashMap<MediaType, HashSet<PriorityObjectFactory<T>>>(data);
            if (!key.getParameters().isEmpty()) {
                key = new MediaType(key.getType(), key.getSubtype());
            }
            HashSet<PriorityObjectFactory<T>> set = data.get(key);
            if (set == null) {
                set = new HashSet<PriorityObjectFactory<T>>();
            } else {
                set = new HashSet<PriorityObjectFactory<T>>(set);
            }
            copyOfMap.put(key, set);
            if (!set.add(objectFactory)) {
                if (logger.isTraceEnabled()) {
                    logger.trace(Messages.getMessage("mediaTypeSetAlreadyContains", objectFactory)); //$NON-NLS-1$
                }
            } else {

                // need to resort the entry set
                Entry<MediaType, HashSet<PriorityObjectFactory<T>>>[] newEntrySet =
                    copyOfMap.entrySet().toArray(new Entry[0]);
                // It's important to sort the media types here to ensure that
                // provider of the more dominant media type will precede, when
                // adding to the compatible set.
                Arrays
                    .sort(newEntrySet,
                          Collections
                              .reverseOrder(new Comparator<Entry<MediaType, HashSet<PriorityObjectFactory<T>>>>() {

                                  public int compare(Entry<MediaType, HashSet<PriorityObjectFactory<T>>> o1,
                                                     Entry<MediaType, HashSet<PriorityObjectFactory<T>>> o2) {
                                      return MediaTypeUtils.compareTo(o1.getKey(), o2.getKey());
                                  }
                              }));

                if (logger.isTraceEnabled()) {
                    logger.trace("Added ObjectFactory {} with MediaType {} to MediaTypeMap {}", //$NON-NLS-1$
                                 new Object[] {objectFactory, key, this});
                    logger.trace("EntrySet is {}", newEntrySet); //$NON-NLS-1$
                }
                entrySet = newEntrySet;
                data = copyOfMap;

                // the set of providers has been changed so must clear the cache
                providersCache.clear();
                logger.trace("Cleared the providers cache"); //$NON-NLS-1$
            }
        }

        @Override
        public String toString() {
            return toString("  ", false, true); //$NON-NLS-1$
        }

        /**
         * @param userOnly only print user-defined entities
         * @param trace if calling toString as part of debugging, use
         *            trace=false, if as part of trace or any other reason, use
         *            trace=true
         * @return
         */
        public String toString(boolean userOnly, boolean trace) {
            return toString("  ", userOnly, trace);
        }

        /**
         * @param indent how far to indent output
         * @param userOnly only log user-defined entities
         * @param trace if calling toString as part of debugging, use
         *            trace=false, if as part of trace or any other reason, use
         *            trace=true (debug prints slightly less verbose)
         * @return
         */
        protected String toString(String indent, boolean userOnly, boolean trace) {
            StringBuffer sb = new StringBuffer();

            sb.append("\nRawType: "); //$NON-NLS-1$
            sb.append(String.valueOf(rawType));
            sb.append("\nData Map: "); //$NON-NLS-1$
            if (data.isEmpty()) {
                sb.append("{empty}"); //$NON-NLS-1$
            } else {
                StringBuffer sb_map = new StringBuffer();
                boolean userItemFound = !userOnly;
                // The data Map can be huge. Separate entries
                // to make it more understandable
                for (MediaType k : data.keySet()) {
                    sb_map.append("MediaType key = "); //$NON-NLS-1$
                    sb_map.append(k);
                    sb_map.append("\n"); //$NON-NLS-1$
                    sb_map.append("ObjectFactory Set value = {\n"); //$NON-NLS-1$

                    // Separate each ObjectFactory entry in the Set
                    // into its own line
                    for (ObjectFactory<T> of : data.get(k)) {
                        // assuming everything in the org.apache.wink.* package
                        // space with "internal" in package name is system, not
                        // user
                        String instanceClassName = of.getInstanceClass().getName();
                        if ((userOnly && !(instanceClassName
                            .startsWith("org.apache.wink.common.internal.") || instanceClassName.startsWith("org.apache.wink.server.internal."))) || !userOnly) { //$NON-NLS-1$ $NON-NLS-2$
                            userItemFound = true;
                            sb_map.append(indent);
                            if (trace) { // trace, print full
                                // ObjectFactory.toString()
                                sb_map.append(of);
                            } else { // debug, print slightly less information
                                sb_map.append(of.getInstanceClass());
                            }
                            sb_map.append("\n"); //$NON-NLS-1$
                        }
                    }
                    sb_map.append("}\n"); //$NON-NLS-1$
                }
                if ((sb_map.length() > 0) && userItemFound) {
                    sb.append("\n" + sb_map.toString()); //$NON-NLS-1$
                } else {
                    sb.append("{empty}"); //$NON-NLS-1$
                }
            }
            return sb.toString();
        }

        @SuppressWarnings("hiding")
        class OFHolder<T> implements ObjectFactory<T>, Comparable<OFHolder<T>> {

            private final PriorityObjectFactory<T> of;
            private final MediaType                mediaType;
            private final Class<?>                 genericType;
            private final boolean                  isSystemProvider;

            public OFHolder(MediaType mediaType,
                            PriorityObjectFactory<T> of,
                            boolean isSystemProvider) {
                super();
                this.of = of;
                this.mediaType = mediaType;
                this.isSystemProvider = isSystemProvider;
                genericType =
                    GenericsUtils.getClassType(GenericsUtils.getGenericInterfaceParamType(of
                        .getInstanceClass(), rawType), rawType);
            }

            @Override
            public String toString() {
                return "OFHolder [" + (genericType != null ? "genericType=" + genericType + ", " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                : "") //$NON-NLS-1$
                    + (mediaType != null ? "mediaType=" + mediaType + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + (of != null ? "of=" + of : "") //$NON-NLS-1$ //$NON-NLS-2$
                    + "]"; //$NON-NLS-1$
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

            public void releaseInstance(T instance, RuntimeContext context) {
                of.releaseInstance(instance, context);
            }

            public void releaseAll(RuntimeContext context) {
                of.releaseAll(context);
            }

            public int compareTo(OFHolder<T> o) {
                // check if this is a system provider
                // system providers are less than
                // WinkApplication.SYSTEM_PRIORITY + 0.1 (they start at
                // WinkApplication.SYSTEM_PRIORITY and
                // unless there are 10000000000, this shouldn't matter)
                if (isSystemProvider) {
                    // this is a system provider
                    if (!o.isSystemProvider) {
                        // the other is a user provider so this is > 0.2
                        return -1;
                    }
                } else if (o.isSystemProvider) {
                    // the other is a system provider
                    if (!isSystemProvider) {
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
        final boolean                  isSystemProvider;
        private static double          counter = 0.00000000001;
        private static final double    inc     = 0.00000000001;

        public PriorityObjectFactory(ObjectFactory<T> of, double priority, boolean isSystemProvider) {
            super();
            this.of = of;
            this.priority = priority + (counter += inc);
            this.isSystemProvider = isSystemProvider;
        }

        public T getInstance(RuntimeContext context) {
            return of.getInstance(context);
        }

        public Class<T> getInstanceClass() {
            return of.getInstanceClass();
        }

        public void releaseInstance(T instance, RuntimeContext context) {
            of.releaseInstance(instance, context);
        }

        public void releaseAll(RuntimeContext context) {
            of.releaseAll(context);
        }

        // this compare is used by exception mappers
        public int compareTo(PriorityObjectFactory<T> o) {
            return Double.compare(priority, o.priority);
        }

        @Override
        public String toString() {
            return String
                .format("Priority: %f, ObjectFactory: %s", priority, of.toString().replace("class ", "")); //$NON-NLS-1$
        }
    }

    public static class ProviderRecord<T> {

        private final MediaType mediaType;
        private final Class<?>  genericType;
        private final boolean   isSystemProvider;
        private final Class<T>  providerClass;

        public ProviderRecord(Class<T> providerClass,
                              MediaType mediaType,
                              Class<?> rawType,
                              boolean isSystemProvider) {
            super();
            this.mediaType = mediaType;
            this.isSystemProvider = isSystemProvider;
            this.providerClass = providerClass;
            Type t = GenericsUtils.getGenericInterfaceParamType(providerClass, rawType);
            if (t == null) {
                this.genericType = Object.class;
            } else {
                this.genericType = GenericsUtils.getClassType(t, providerClass);
            }
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        public Class<?> getGenericType() {
            return genericType;
        }

        public boolean isSystemProvider() {
            return isSystemProvider;
        }

        public Class<T> getProviderClass() {
            return providerClass;
        }
    }

    /**
     * @param userOnly true = log user providers only, false = log all providers
     */
    public String getLogFormattedProvidersList(boolean userOnly) {
        StringBuffer sb = new StringBuffer();
        sb.append(this.messageBodyReaders.toString(userOnly, false));
        sb.append(this.messageBodyWriters.toString(userOnly, false));
        sb.append(this.contextResolvers.toString(userOnly, false));
        if (userOnly) {
            return Messages.getMessage("followingProvidersUserDefined", sb.toString());
        } else {
            return Messages.getMessage("followingProviders", sb.toString());
        }
    }
    
}
