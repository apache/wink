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
package org.apache.wink.common.internal.providers.entity.xml;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.utils.JAXBUtils;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.internal.utils.SimpleMap;
import org.apache.wink.common.internal.utils.SoftConcurrentMap;
import org.apache.wink.common.model.XmlFormattingOptions;
import org.apache.wink.common.utils.ProviderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJAXBProvider {

    private static final Logger                           logger                    =
                                                                                        LoggerFactory
                                                                                            .getLogger(AbstractJAXBProvider.class);
    private static final SimpleMap<Class<?>, JAXBContext> jaxbDefaultContexts       =
                                                                                        new SoftConcurrentMap<Class<?>, JAXBContext>();

    @Context
    private Providers                                     providers;

    private static final ConcurrentMap<Class<?>, Boolean> jaxbIsXMLRootElementCache =
                                                                                        new ConcurrentHashMap<Class<?>, Boolean>();

    private static final ConcurrentMap<Class<?>, Boolean> jaxbIsXMLTypeCache        =
                                                                                        new ConcurrentHashMap<Class<?>, Boolean>();

    protected final Unmarshaller getUnmarshaller(Class<?> type, MediaType mediaType)
        throws JAXBException {
        JAXBContext context = getContext(type, mediaType);
        return context.createUnmarshaller();
    }

    protected final Marshaller getMarshaller(Class<?> type, MediaType mediaType)
        throws JAXBException {
        JAXBContext context = getContext(type, mediaType);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, ProviderUtils.getCharset(mediaType));

        ContextResolver<XmlFormattingOptions> contextResolver =
            providers.getContextResolver(XmlFormattingOptions.class, mediaType);
        XmlFormattingOptions formatingOptions = null;
        if (contextResolver != null) {
            formatingOptions = contextResolver.getContext(type);
        }
        if (formatingOptions != null) {
            JAXBUtils.setXmlFormattingOptions(marshaller, formatingOptions);
        }
        return marshaller;
    }

    protected boolean isSupportedMediaType(MediaType mediaType) {
        return MediaTypeUtils.isXmlType(mediaType);
    }

    public static boolean isJAXBObject(Class<?> type, Type genericType) {
        return isXMLRootElement(type) || isXMLType(type);
    }

    private static boolean isXMLRootElement(Class<?> type) {
        Boolean isJAXBObject = jaxbIsXMLRootElementCache.get(type);

        if (isJAXBObject == null) {
            boolean isXmlRootElement = type.getAnnotation(XmlRootElement.class) != null;
            isJAXBObject = Boolean.valueOf(isXmlRootElement);
            jaxbIsXMLRootElementCache.putIfAbsent(type, isJAXBObject);
        }

        return isJAXBObject.booleanValue();
    }

    private static boolean isXMLType(Class<?> type) {
        Boolean isJAXBObject = jaxbIsXMLTypeCache.get(type);

        if (isJAXBObject == null) {
            boolean isXmlTypeElement = type.getAnnotation(XmlType.class) != null;
            isJAXBObject = Boolean.valueOf(isXmlTypeElement);
            jaxbIsXMLTypeCache.putIfAbsent(type, isJAXBObject);
        }

        return isJAXBObject.booleanValue();
    }

    public static boolean isJAXBElement(Class<?> type, Type genericType) {
        return (type == JAXBElement.class && genericType instanceof ParameterizedType);
    }

    private JAXBContext getContext(Class<?> type, MediaType mediaType) throws JAXBException {
        ContextResolver<JAXBContext> contextResolver =
            providers.getContextResolver(JAXBContext.class, mediaType);
        JAXBContext context = null;

        if (contextResolver != null) {
            context = contextResolver.getContext(type);
        }

        if (context == null) {
            context = getDefaultContext(type);
        }
        return context;
    }

    private JAXBContext getDefaultContext(Class<?> type) throws JAXBException {
        JAXBContext context = jaxbDefaultContexts.get(type);
        if (context == null) {
            context = JAXBContext.newInstance(type);
            jaxbDefaultContexts.put(type, context);
        }
        return context;
    }

    /**
     * If the object is not a JAXBElement and is annotated with XmlType but not
     * with XmlRootElement, then it is automatically wrapped in a JAXBElement
     * 
     * @param t
     * @param type
     * @return
     */
    protected Object getEntityToMarshal(Object jaxbObject, Class<?> type) {
        // in case JAXB Objects is not annotated with XmlRootElement, Wrap JAXB
        // Objects with JAXBElement
        if (!isXMLRootElement(type) && isXMLType(type)) {
            JAXBElement<?> wrappedJAXBElement = wrapInJAXBElement(jaxbObject, type);
            if (wrappedJAXBElement == null) {
                logger.error(Messages.getMessage("jaxbObjectFactoryNotFound"), type.getName());
                throw new WebApplicationException();
            }
            return wrappedJAXBElement;
        }
        return jaxbObject;
    }

    /**
     * If this object is managed by an XmlRegistry, this method will invoke the
     * registry and wrap the object in a JAXBElement so that it can be
     * marshalled.
     */
    private JAXBElement<?> wrapInJAXBElement(Object jaxbObject, Class<?> type) {
        try {
            Object factory = null;
            Class<?> factoryClass = findDefaultObjectFactoryClass(type);
            if (factoryClass != null) {
                factory = factoryClass.newInstance();
                Method[] method = factory.getClass().getDeclaredMethods();
                for (int i = 0; i < method.length; i++) {
                    // Invoke method
                    Method current = method[i];
                    if (current.getParameterTypes().length == 1 && current.getParameterTypes()[0]
                        .equals(type)
                        && current.getName().startsWith("create")) {
                        Object result = current.invoke(factory, new Object[] {jaxbObject});
                        return JAXBElement.class.cast(result);
                    }
                }
                return null;
            }
            logger.warn(Messages.getMessage("jaxbObjectFactoryInstantiate"), type.getName());
            return defaultWrapInJAXBElement(jaxbObject, type);
        } catch (Exception e) {
            logger.error(Messages.getMessage("jaxbElementFailToBuild"), type.getName());
            return null;
        }

    }

    private Class<?> findDefaultObjectFactoryClass(Class<?> type) {
        // XmlType typeAnnotation = type.getAnnotation(XmlType.class);
        // // Check that class factory method uses
        // if (!typeAnnotation.factoryClass().equals(XmlType.DEFAULT.class)) {
        // logger.error("Failed to build JAXBElement for {}", type.getName());
        // return null;
        // }
        // Search for Factory
        StringBuilder b = new StringBuilder(type.getPackage().getName());
        b.append(".ObjectFactory");
        Class<?> factoryClass = null;
        try {
            factoryClass = Thread.currentThread().getContextClassLoader().loadClass(b.toString());
        } catch (ClassNotFoundException e) {
            logger.error(Messages.getMessage("jaxbObjectFactoryNotFound"), type.getName());
            return null;
        }

        if (!factoryClass.isAnnotationPresent(XmlRegistry.class)) {
            logger.error(Messages.getMessage("jaxbObjectFactoryNotAnnotatedXMLRegistry"), type
                .getName());
            return null;
        }

        return factoryClass;
    }

    @SuppressWarnings("unchecked")
    private JAXBElement<?> defaultWrapInJAXBElement(Object jaxbObject, Class<?> type) {
        logger.info(Messages.getMessage("jaxbCreateDefaultJAXBElement"), type.getName());
        String typeStr = type.getAnnotation(XmlType.class).name();
        return new JAXBElement(new QName(typeStr), type, jaxbObject);
    }

}
