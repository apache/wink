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

package org.apache.wink.common.internal.providers.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.annotations.Asset;
import org.apache.wink.common.annotations.Scope;
import org.apache.wink.common.annotations.Scope.ScopeType;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.InjectableFactory;
import org.apache.wink.common.internal.registry.Injectable.ParamType;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.internal.utils.AnnotationUtils;
import org.apache.wink.common.internal.utils.GenericsUtils;
import org.apache.wink.common.internal.utils.MediaTypeUtils;

@Scope(ScopeType.PROTOTYPE)
@Provider
public class AssetProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

    private static final Logger       logger = LoggerFactory.getLogger(AssetProvider.class);

    @Context
    private Providers                 providers;

    private MessageBodyWriter<Object> writer;
    private BaseAssetMethod           method;

    public long getSize(Object t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        // must be annotated with @Asset
        if (type.getAnnotation(Asset.class) == null) {
            return false;
        }

        return findProducesMethod(type, annotations, mediaType);
    }

    public void writeTo(Object t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        RuntimeContext context = getRuntimeContext();
        // instantiate parameters of the asset method
        Object[] args =
            InjectableFactory.getInstance().instantiate(method.getFormalParameters(), context);
        try {
            // invoke the asset method to produce the object to write
            Object object = method.getMethod().invoke(t, args);
            // write the object
            writer.writeTo(object,
                           method.getTypeClass(),
                           method.getType(),
                           annotations,
                           mediaType,
                           httpHeaders,
                           entityStream);
        } catch (IllegalArgumentException e) {
            logger.error(Messages.getMessage("assetMethodInvokeError"), method.getMethod()
                .getName());
            throw new WebApplicationException(e);
        } catch (IllegalAccessException e) {
            logger.error(Messages.getMessage("assetMethodInvokeError"), method.getMethod()
                .getName());
            throw new WebApplicationException(e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException)targetException;
            }
            logger.error(Messages.getMessage("assetMethodInvokeError"), method.getMethod()
                .getName());
            throw new WebApplicationException(targetException);
        }
    }

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        // must be annotated with @Asset
        if (type.getAnnotation(Asset.class) == null) {
            return false;
        }

        return findConsumesMethod(type, annotations, mediaType);
    }

    public Object readFrom(Class<Object> type,
                           Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {
        RuntimeContext context = getRuntimeContext();
        // instantiate parameters of the asset method.
        // since the formal parameters contain an Entity parameter, it is
        // populated
        // during the call to the instantiate method. there is no need to
        // manually call
        // the reader for getting the entity
        Object[] args =
            InjectableFactory.getInstance().instantiate(method.getFormalParameters(), context);
        try {
            // create the asset
            Object asset = type.newInstance();
            // invoke the asset method to consume the entity that was read
            method.getMethod().invoke(asset, args);
            return asset;
        } catch (RuntimeException e) {
            throw e;
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException)targetException;
            }
            logger.error(Messages.getMessage("assetMethodInvokeError"), method.getMethod()
                .getName());
            throw new WebApplicationException(e);
        } catch (InstantiationException e) {
            logger.error(Messages.getMessage("assetMustHavePublicConstructor"), type.getName());
            throw new WebApplicationException(e);

        } catch (Exception e) {
            logger.error(Messages.getMessage("assetMethodInvokeError"), method.getMethod()
                .getName());
            throw new WebApplicationException(e);
        }
    }

    private RuntimeContext getRuntimeContext() {
        return RuntimeContextTLS.getRuntimeContext();
    }

    @SuppressWarnings("unchecked")
    private boolean findProducesMethod(Class<?> assetType,
                                       Annotation[] annotations,
                                       MediaType mediaType) {

        // get all writable locator methods
        List<ProducesMethod> methods = getProducingMethods(assetType, mediaType);
        // sort the methods according to media types in descending order
        Collections.sort(methods, new Comparator<ProducesMethod>() {

            public int compare(ProducesMethod o1, ProducesMethod o2) {
                return o2.compareTo(o1);
            }
        });
        // find a method that can be handled
        // use the return type of the method to find the actual provider
        for (ProducesMethod method : methods) {
            MessageBodyWriter<?> writer =
                providers.getMessageBodyWriter(method.getTypeClass(),
                                               method.getType(),
                                               annotations,
                                               mediaType);
            if (writer != null) {
                this.writer = (MessageBodyWriter<Object>)writer;
                this.method = method;
                return true;
            }
        }
        // couldn't find any method in the asset that we can use
        return false;
    }

    private boolean findConsumesMethod(Class<?> assetType,
                                       Annotation[] annotations,
                                       MediaType mediaType) {

        // verify that the asset has a default public constructor
        try {
            if (assetType.getConstructor() == null) {
                logger.info(Messages.getMessage("assetCannotInstantiate"), assetType.getName());
                return false;
            }
        } catch (SecurityException e) {
            logger.info(Messages.getMessage("assetCannotInstantiate"), assetType.getName());
            return false;
        } catch (NoSuchMethodException e) {
            logger.info(Messages.getMessage("assetCannotInstantiate"), assetType.getName());
            return false;
        }

        // get all writable locator methods
        List<ConsumesMethod> methods = getConsumingMethods(assetType, mediaType);
        // sort the methods according to media types in descending order
        Collections.sort(methods, new Comparator<ConsumesMethod>() {

            public int compare(ConsumesMethod o1, ConsumesMethod o2) {
                return o2.compareTo(o1);
            }
        });
        // find a consuming method that has a message body reader
        for (ConsumesMethod method : methods) {
            MessageBodyReader<?> reader =
                providers.getMessageBodyReader(method.getTypeClass(),
                                               method.getType(),
                                               annotations,
                                               mediaType);
            if (reader != null) {
                // we don't need to save the reader because when we prepare the
                // parameters to inject to the locator method, the entity
                // parameter will also be
                // populated
                this.method = method;
                return true;
            }
        }
        // couldn't find any method in the asset that we can use
        return false;
    }

    private List<ProducesMethod> getProducingMethods(Class<?> assetType, MediaType mediaType) {
        // collect all the methods that are annotated with @Produces
        List<ProducesMethod> locators = new LinkedList<ProducesMethod>();
        Method[] methods = assetType.getMethods();
        for (Method method : methods) {
            Produces annotation = method.getAnnotation(Produces.class);
            if (annotation != null) {
                String[] producesArray =
                    AnnotationUtils.parseConsumesProducesValues(annotation.value());
                List<MediaType> produces = toSortedMediaTypes(producesArray);
                for (MediaType mt : produces) {
                    if (mt.isCompatible(mediaType)) {
                        ProducesMethod prodcuesMethod = new ProducesMethod(method, mt);
                        if (prodcuesMethod.getType() != null) {
                            locators.add(prodcuesMethod);
                        }
                    }
                }
            }
        }
        return locators;
    }

    private List<ConsumesMethod> getConsumingMethods(Class<?> assetType, MediaType mediaType) {
        // collect all the methods that are annotated with @Consumes
        List<ConsumesMethod> locators = new LinkedList<ConsumesMethod>();
        Method[] methods = assetType.getMethods();
        for (Method method : methods) {
            Consumes annotation = method.getAnnotation(Consumes.class);
            if (annotation != null) {
                String[] producesArray =
                    AnnotationUtils.parseConsumesProducesValues(annotation.value());
                List<MediaType> produces = toSortedMediaTypes(producesArray);
                for (MediaType mt : produces) {
                    if (mt.isCompatible(mediaType)) {
                        ConsumesMethod consumesMethod = new ConsumesMethod(method, mt);
                        if (consumesMethod.getType() != null) {
                            locators.add(consumesMethod);
                        }
                    }
                }
            }
        }
        return locators;
    }

    private List<MediaType> toSortedMediaTypes(String[] array) {
        List<MediaType> list = new LinkedList<MediaType>();
        for (String mt : array) {
            list.add(MediaType.valueOf(mt));
        }

        // sort the list of media types
        Collections.sort(list, new Comparator<MediaType>() {

            public int compare(MediaType o1, MediaType o2) {
                // compare in descending order
                return MediaTypeUtils.compareTo(o2, o1);
            }
        });
        return list;
    }

    private static abstract class BaseAssetMethod implements Comparable<BaseAssetMethod> {

        private Method           method;
        private List<Injectable> formalParameters;
        private MediaType        mediaType;
        protected Type           type;
        protected Class<?>       typeClass;

        public BaseAssetMethod(Method method, MediaType mediaType) {
            this.method = method;
            this.mediaType = mediaType;
            createFormalParameters();
        }

        private void createFormalParameters() {
            formalParameters = new LinkedList<Injectable>();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Type[] paramTypes = method.getGenericParameterTypes();
            for (int pos = 0, limit = paramTypes.length; pos < limit; pos++) {
                Injectable fp =
                    InjectableFactory.getInstance().create(paramTypes[pos],
                                                           parameterAnnotations[pos],
                                                           method,
                                                           false,
                                                           null);
                formalParameters.add(fp);
            }
        }

        public List<Injectable> getFormalParameters() {
            return formalParameters;
        }

        public Method getMethod() {
            return method;
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        public Type getType() {
            return type;
        }

        public Class<?> getTypeClass() {
            return typeClass;
        }

        @Override
        public boolean equals(Object obj) {
            return method.equals(obj);
        }

        @Override
        public int hashCode() {
            return method.hashCode();
        }

        public int compareTo(BaseAssetMethod o) {
            return MediaTypeUtils.compareTo(getMediaType(), o.getMediaType());
        }

    }

    private static class ProducesMethod extends BaseAssetMethod {

        public ProducesMethod(Method method, MediaType mediaType) {
            super(method, mediaType);
            this.type = method.getGenericReturnType();
            this.typeClass = GenericsUtils.getClassType(type);
        }
    }

    private static class ConsumesMethod extends BaseAssetMethod {

        public ConsumesMethod(Method method, MediaType mediaType) {
            super(method, mediaType);
            for (Injectable fp : getFormalParameters()) {
                if (fp.getParamType() == ParamType.ENTITY) {
                    if (type != null) {
                        // we allow to have only one entity parameter
                        String methodName =
                            method.getDeclaringClass().getName() + "." + method.getName();
                        logger.error(Messages
                            .getMessage("assetLocatorMethodMoreThanOneEntityParam"), methodName);
                        throw new WebApplicationException();
                    }
                    type = fp.getGenericType();
                    typeClass = fp.getType();
                }
            }
        }
    }

}
