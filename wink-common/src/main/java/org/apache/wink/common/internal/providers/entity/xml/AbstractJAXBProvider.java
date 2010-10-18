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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.internal.utils.JAXBUtils;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.internal.utils.SoftConcurrentMap;
import org.apache.wink.common.model.JAXBUnmarshalOptions;
import org.apache.wink.common.model.XmlFormattingOptions;
import org.apache.wink.common.utils.ProviderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJAXBProvider {

    private static final Logger                                      logger                         =
                                                                                                        LoggerFactory
                                                                                                            .getLogger(AbstractJAXBProvider.class);
    private static final SoftConcurrentMap<Class<?>, JAXBContext>    jaxbDefaultContexts            =
                                                                                                        new SoftConcurrentMap<Class<?>, JAXBContext>();

    @Context
    protected Providers                                              providers;

    private static final SoftConcurrentMap<Class<?>, Boolean>        jaxbIsXMLRootElementCache      =
                                                                                                        new SoftConcurrentMap<Class<?>, Boolean>();

    private static final SoftConcurrentMap<Class<?>, Boolean>        jaxbIsXMLTypeCache             =
                                                                                                        new SoftConcurrentMap<Class<?>, Boolean>();

    private static final SoftConcurrentMap<Class<?>, Class<?>>       xmlElementConcreteClassCache   =
                                                                                                        new SoftConcurrentMap<Class<?>, Class<?>>();

    // if JAXB objects implement an interface where that interface has
    // @XmlJavaTypeAdapter annotation, or
    // in JAXB 2.2 if the @XMLElement annotation is on the 'type' of the
    // resource method parameter
    protected static final SoftConcurrentMap<Class<?>, Class<?>>     jaxbTypeMapCache               =
                                                                                                        new SoftConcurrentMap<Class<?>, Class<?>>();

    private static final SoftConcurrentMap<Type, XmlJavaTypeAdapter> xmlJavaTypeAdapterCache        =
                                                                                                        new SoftConcurrentMap<Type, XmlJavaTypeAdapter>();

    private static final SoftConcurrentMap<Type, Boolean>            xmlJavaTypeAdapterPresentCache =
                                                                                                        new SoftConcurrentMap<Type, Boolean>();

    // the Pool code for the pooling of unmarshallers is from Axis2 Java
    // http://svn.apache.org/repos/asf/webservices/axis2/trunk/java/modules/jaxws/src/org/apache/axis2/jaxws/message/databinding/JAXBUtils.java
    //
    // These pools should *not* be static in Wink, however, because the
    // (un)marshallers are unique per JAXBContext instance, each
    // of which is unique per class object being (un)marshalled. In Axis2, the
    // JAXBContext instances cover the entire application space, thus
    // it was safe to cache them in a static field.
    private Pool<JAXBContext, Marshaller>                         mpool                     =
                                                                                                new Pool<JAXBContext, Marshaller>();
    private Pool<JAXBContext, Unmarshaller>                       upool                     =
                                                                                                new Pool<JAXBContext, Unmarshaller>();
                                                                                                

    // For performance, it might seem advantageous to use a static XMLInputFactory instance.  However, this was shown to
    // be problematic on the Sun StAX parser (which is a fork of Apache Xerces) under load stress test.  So we use ThreadLocal.
    private static ThreadLocal<XMLInputFactory> xmlInputFactory = new ThreadLocal<XMLInputFactory>();
    
    /**
     * This class is the key to the JAXBContext cache. It must be based on the
     * ContextResolver instance who created the JAXBContext and the type passed
     * to it. The only way this cache becomes invalid is if the ContextResolver
     * does something crazy like create JAXBContexts based on time -- it only
     * creates contexts between noon and 5:00pm. So, uhhh, don't do that.
     */
    private static class JAXBContextResolverKey {
        private static final Logger logger = LoggerFactory.getLogger(JAXBContextResolverKey.class);
        
        protected ContextResolver<JAXBContext> _resolver;
        protected Type                         _type;
        private int                            hashCode = -1;

        public JAXBContextResolverKey(ContextResolver<JAXBContext> resolver, Type type) {
            logger.trace("Constructing JAXBContextResolverKey with {} and {}", resolver, type); //$NON-NLS-1$
            // resolver may be null, which is ok; we'll protect against NPEs in
            // equals and hashCode overrides
            _resolver = resolver;
            _type = type;
        }

        @Override
        public boolean equals(Object o) {
            logger.trace("equals({}) entry", o); //$NON-NLS-1$
            if ((o == null) || (!(o instanceof JAXBContextResolverKey))) {
                logger.trace("equals() exit due to null or not instance of JAXBContextResolverKey"); //$NON-NLS-1$
                return false;
            }
            JAXBContextResolverKey obj = (JAXBContextResolverKey)o;
            // check for both null or both NOT null
            boolean result =
                ((obj._resolver == null) && (_resolver == null)) || ((obj._resolver != null) && (_resolver != null));
            logger.trace("null check result is {}", result); //$NON-NLS-1$
            // we can use hashCode() to compare _resolver
            boolean finalResult = result && (obj.hashCode() == hashCode()) && (obj._type.equals(_type));
            logger.trace("final result is {}", finalResult); //$NON-NLS-1$
            return finalResult;
        }

        @Override
        public int hashCode() {
            logger.trace("hashCode() entry"); //$NON-NLS-1$
            if (hashCode != -1) {
                logger.trace("returning hashCode {}", hashCode); //$NON-NLS-1$
                return hashCode;
            }
            if (_resolver == null) {
                // allow the key to be based entirely on the _type object
                // equality from equals method. Only YOU can prevent NPEs.
                hashCode = 0; // don't use _type's hashCode, as the instances
                // may differ between JAXBContextResolverKey
                // instances
                logger.trace("resolver is null so returning hashCode {}", hashCode); //$NON-NLS-1$
                return hashCode;
            }
            // Resolver instances may be unique due to the way we proxy the call
            // to get the instances in the ProvidersRegistry.
            // Therefore, we'll get better performance if we calculate the
            // hashCode from the package.classname of the ContextResolver.
            // However, this means we need to make sure the map that uses this
            // key is non-static, so it remains scoped at the
            // transaction level, rather than at the application, or worse, JVM
            // level.
            String resolverName = _resolver.getClass().getName();
            logger.trace("resolverName is {}", resolverName); //$NON-NLS-1$
            byte[] bytes = resolverName.getBytes();
            for (int i = 0; i < bytes.length; i++) {
                hashCode += bytes[i];
            }
            logger.trace("returning hashCode to be {}", hashCode); //$NON-NLS-1$
            return hashCode;
        }

    }

    /*
     * TODO: in my small, uncontrolled test, the JVM (garbage collector?) was
     * cleaning about 10% of the time. It may be worth considering the use of
     * LRU cache or something more directly under our control to gain more of
     * that. To observe this behavior, set the "loop" int in
     * JAXBCustomContextResolverCacheTest.testCustomResolverCacheOn to a high
     * number, and see the System.out for cacheMisses. In my checking, it was
     * about 10% of "loop".
     */
    // do not make static, as the key is based on the classname of the
    // ContextResolver
    private final SoftConcurrentMap<JAXBContextResolverKey, JAXBContext> jaxbContextCache =
                                                                                              new SoftConcurrentMap<JAXBContextResolverKey, JAXBContext>();

    // JAXBContext cache can be turned off through system property
    static private final String                                          propVal          =
                                                                                              System
                                                                                                  .getProperty("org.apache.wink.jaxbcontextcache"); //$NON-NLS-1$
    // non-final, protected only to make it unittestable
    static protected boolean                                             contextCacheOn   =
                                                                                              !((propVal != null) && (propVal
                                                                                                  .equalsIgnoreCase("off"))); //$NON-NLS-1$

/**
     * Get the unmarshaller. You must call {@link #releaseJAXBUnmarshaller(JAXBContext, Unmarshaller) to put it back
     * into the pool.
     * 
     * @param context the current context
     * @return Unmarshaller an unmarshaller for the context
     * @throws JAXBException
     */
    protected final Unmarshaller getJAXBUnmarshaller(Class<?> type,
                                                     JAXBContext context,
                                                     MediaType mediaType) throws JAXBException {
        Unmarshaller unm = upool.get(context);
        if (unm == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Unmarshaller created [not in pool]"); //$NON-NLS-1$
            }
            unm = internalCreateUnmarshaller(context);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Unmarshaller obtained [from  pool]"); //$NON-NLS-1$
            }
        }

        if (providers != null) {
            ContextResolver<JAXBUnmarshalOptions> contextResolver =
                providers.getContextResolver(JAXBUnmarshalOptions.class, mediaType);
            JAXBUnmarshalOptions options = null;
            if (contextResolver != null) {
                options = contextResolver.getContext(type);
            }
            if (options != null) {
                JAXBUtils.setJAXBUnmarshalOptions(unm, options);
            }
        }

        return unm;
    }
    
    /**
     * skips START_DOCUMENT, COMMENTs, PIs, and checks for DTD
     * @param reader
     * @throws XMLStreamException
     */
    private static void checkForDTD(XMLStreamReader reader)
            throws XMLStreamException {
        boolean supportDTD = false;

        int event = reader.getEventType();
        if (event != XMLStreamReader.START_DOCUMENT) {
            // something went horribly wrong; the reader passed into us has
            // already been partially processed
            throw new XMLStreamException(Messages.getMessage("badXMLReaderInitialStart")); //$NON-NLS-1$
        }
        while (event != XMLStreamReader.START_ELEMENT) {  // all StAX parsers require a START_ELEMENT.  See AbstractJAXBProviderTest class
            event = reader.next();
            if (event == XMLStreamReader.DTD) {

                RuntimeContext runtimeContext = RuntimeContextTLS.getRuntimeContext();
                WinkConfiguration winkConfig = runtimeContext.getAttribute(WinkConfiguration.class);
                if (winkConfig != null) {
                    Properties props = winkConfig.getProperties();
                    if (props != null) {
                        // use valueOf method to require the word "true"
                        supportDTD = Boolean.valueOf(props.getProperty("wink.supportDTDEntityExpansion")); //$NON-NLS-1$
                    }
                }
                if (!supportDTD) {
                    throw new EntityReferenceXMLStreamException(Messages.getMessage("entityRefsNotSupported")); //$NON-NLS-1$
                } else {
                    logger.trace("DTD entity reference expansion is enabled.  This may present a security risk."); //$NON-NLS-1$
                }
            }
        }
    }
    
    private static XMLInputFactory getXMLInputFactory() {
        XMLInputFactory factory = xmlInputFactory.get();
        if (factory == null) {
            factory = XMLInputFactory.newInstance();
            xmlInputFactory.set(factory);
        }
        return factory;
    }
    
    /**
     * A consistent place to get a properly configured XMLStreamReader.
     * 
     * @param entityStream
     * @return
     * @throws XMLStreamException
     */
    protected static XMLStreamReader getXMLStreamReader(InputStream entityStream) throws XMLStreamException  {
        // NOTE: createFilteredReader may appear to be more convenient, but it comes at the cost of
        // performance.  This solution (to use checkForDTD) appears to be the best solution to preserve
        // performance, but still achieve what we need to do.
        XMLStreamReader reader = getXMLInputFactory().createXMLStreamReader(entityStream);
        checkForDTD(reader);
        return reader;
    }
    
    /**
     * A consistent place to get a properly configured XMLStreamReader.
     * 
     * @param entityStream
     * @return
     * @throws XMLStreamException
     */
    protected static XMLStreamReader getXMLStreamReader(InputStreamReader entityStreamReader) throws XMLStreamException  {
        // NOTE: createFilteredReader may appear to be more convenient, but it comes at the cost of
        // performance.  This solution (to use checkForDTD) appears to be the best solution to preserve
        // performance, but still achieve what we need to do.
        XMLStreamReader reader = getXMLInputFactory().createXMLStreamReader(entityStreamReader);
        checkForDTD(reader);
        return reader;
    }
    
    protected static void closeXMLStreamReader(XMLStreamReader xmlStreamReader) {
        if (xmlStreamReader != null) {
            try {
                xmlStreamReader.close();
            } catch (XMLStreamException e) {
                logger.trace("XMLStreamReader already closed.", e); //$NON-NLS-1$
            } catch (RuntimeException e) {
                logger.trace("RuntimeException occurred: ", e); //$NON-NLS-1$
            }
        }
    }

    private static Unmarshaller internalCreateUnmarshaller(final JAXBContext context)
        throws JAXBException {
        Unmarshaller unm;
        try {
            unm = AccessController.doPrivileged(new PrivilegedExceptionAction<Unmarshaller>() {
                public Unmarshaller run() throws JAXBException {
                    return context.createUnmarshaller();
                }
            });
        } catch (PrivilegedActionException e) {
            throw (JAXBException)e.getCause();
        }
        return unm;
    }

    /**
     * Release Unmarshaller. Do not call this method if an exception occurred
     * while using the Unmarshaller. The object may be in an invalid state.
     * 
     * @param context JAXBContext the context to key off from
     * @param unmarshaller the unmarshaller to put back in the pool
     */
    protected void releaseJAXBUnmarshaller(JAXBContext context, Unmarshaller unmarshaller) {
        if (logger.isTraceEnabled()) {
            logger.trace("Unmarshaller placed back into pool"); //$NON-NLS-1$
        }
        unmarshaller.setAttachmentUnmarshaller(null);
        upool.put(context, unmarshaller);
    }

    private static Marshaller internalCreateMarshaller(final JAXBContext context)
        throws JAXBException {
        Marshaller marshaller;
        try {
            marshaller = AccessController.doPrivileged(new PrivilegedExceptionAction<Marshaller>() {
                public Marshaller run() throws JAXBException {
                    return context.createMarshaller();
                }
            });
        } catch (PrivilegedActionException e) {
            throw (JAXBException)e.getCause();
        }
        return marshaller;
    }

    /**
     * Get JAXBMarshaller
     * 
     * @param context JAXBContext
     * @return Marshaller
     * @throws JAXBException
     */
    protected Marshaller getJAXBMarshaller(Class<?> type, JAXBContext context, MediaType mediaType)
        throws JAXBException {

        Marshaller m = mpool.get(context);

        if (m == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Marshaller created [not in pool]"); //$NON-NLS-1$
            }
            m = internalCreateMarshaller(context);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Marshaller obtained [from  pool]"); //$NON-NLS-1$
            }
        }

        // will set to UTF-8 if there isn't a charset
        m.setProperty(Marshaller.JAXB_ENCODING, ProviderUtils.getCharset(mediaType));

        ContextResolver<XmlFormattingOptions> contextResolver =
            providers.getContextResolver(XmlFormattingOptions.class, mediaType);
        XmlFormattingOptions formatingOptions = null;
        if (contextResolver != null) {
            formatingOptions = contextResolver.getContext(type);
        }
        if (formatingOptions != null) {
            JAXBUtils.setXmlFormattingOptions(m, formatingOptions);
        }

        return m;
    }

    /**
     * Do not call this method if an exception occurred while using the
     * Marshaller. The object may be in an invalid state.
     * 
     * @param context JAXBContext
     * @param marshaller Marshaller
     */
    protected void releaseJAXBMarshaller(JAXBContext context, Marshaller marshaller) {
        if (logger.isTraceEnabled()) {
            logger.trace("Marshaller placed back into pool"); //$NON-NLS-1$
        }

        marshaller.setAttachmentMarshaller(null);
        mpool.put(context, marshaller);
    }

    protected boolean isSupportedMediaType(MediaType mediaType) {
        return MediaTypeUtils.isXmlType(mediaType);
    }

    public static boolean isJAXBObject(Class<?> type, Type genericType) {
        if (isJAXBObject(type)) {
            return true;
        } else if (genericType instanceof Class<?>) {
            return isJAXBObject((Class<?>)genericType);
        }
        return false;
    }
    
    /**
     * Checks to see if type is marshallable.  One of two annotations must be present with the following conditions:
     * 1)  @XmlJavaTypeAdapter(type, SomeotherType), or
     * 2)  @XmlElement(type=SomeotherType.class) where SomeotherType is a JAXB object.
     * @param type
     * @param annotations
     * @return
     */
    public boolean isCompatible(Class<?> type, Annotation[] annotations) {
        return isJAXBObject(getConcreteTypeFromTypeMap(type, annotations));
    }

    private Class<?> getConcreteTypeFromAdapter(Class<?> type, Annotation[] annotations) {
        XmlJavaTypeAdapter adapter = getXmlJavaTypeAdapter(type, annotations);
        if (adapter != null) {
            Class<?> adapterClass = adapter.value();
            try {
                return (Class<?>) adapterClass.getMethod("marshal", type).getReturnType();
            } catch (NoSuchMethodException e) {
                // not possible to get here;
                // compiler would have prevented compilation of an application with an XmlJavaTypeAdapter that lacked a "marshal" method
            }
        }
        return type;
    }
    
    private Class<?> getConcreteTypeFromXmlElementAnno(Class<?> type, Annotation[] annotations) {
        Class<?> ret = xmlElementConcreteClassCache.get(type);
        if (ret == null) {
            XmlElement xmlElement = getXmlElementAnno(type, annotations);
            if (xmlElement != null) {
                Type xmlElementType = xmlElement.type();
                if (xmlElementType != null) {
                    ret = (Class<?>)xmlElementType;
                }
            }
            if (ret == null)
                ret = type;
            xmlElementConcreteClassCache.put(type, ret);
        }
        return ret;
    }

    public Class<?> getConcreteTypeFromTypeMap(Class<?> type, Annotation[] annotations) {
        Class<?> concreteType = jaxbTypeMapCache.get(type);
        if (concreteType == null) {
            concreteType = getConcreteTypeFromAdapter(type, annotations);
            // @XmlJavaTypeAdapter takes priority over XmlElement
            if (concreteType == type) {
                concreteType = getConcreteTypeFromXmlElementAnno(type, annotations);
            }
            jaxbTypeMapCache.put(type, concreteType);
        }
        return concreteType;
    }
    
    @SuppressWarnings("unchecked")
    protected Object marshalWithXmlAdapter(Object obj, Type type, Annotation[] annotations) {
        if ((type == null) || (annotations == null)) {
            return obj;
        }
        XmlJavaTypeAdapter xmlJavaTypeAdapter = getXmlJavaTypeAdapter(type,
                annotations);
        if (xmlJavaTypeAdapter != null) {
            try {
                XmlAdapter xmlAdapter = xmlJavaTypeAdapter.value().newInstance();
                return xmlAdapter.marshal(obj);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not marshal {} using {} due to exception:", new Object[]{obj, xmlJavaTypeAdapter.value().getName(), e});
                }
            }
        }
        return obj;
    }

    /**
     * @param type
     * @param annotations
     * @return
     */
    private XmlJavaTypeAdapter getXmlJavaTypeAdapter(Type type, Annotation[] annotations) {
        Boolean present = xmlJavaTypeAdapterPresentCache.get(type);
        if (Boolean.FALSE.equals(present)) {
            return null;
        }
        XmlJavaTypeAdapter xmlJavaTypeAdapter = xmlJavaTypeAdapterCache.get(type);
        if(xmlJavaTypeAdapter == null) {
            xmlJavaTypeAdapter = findXmlJavaTypeAdapter(type, annotations);
            xmlJavaTypeAdapterCache.put(type, xmlJavaTypeAdapter);
            xmlJavaTypeAdapterPresentCache.put(type, xmlJavaTypeAdapter != null);
        }
        return xmlJavaTypeAdapter;
    }

    private XmlJavaTypeAdapter findXmlJavaTypeAdapter(Type type, Annotation[] annotations) {
        XmlJavaTypeAdapter xmlJavaTypeAdapter = null;
        for (int i = 0; (annotations != null) && i < annotations.length; i++) {
            if (annotations[i].annotationType() == XmlJavaTypeAdapter.class) {
                xmlJavaTypeAdapter = (XmlJavaTypeAdapter)annotations[i];
                break;
            }
        }
        if ((xmlJavaTypeAdapter == null) && (type != null)) {
            // check the type itself
            xmlJavaTypeAdapter = ((Class<?>)type).getAnnotation(XmlJavaTypeAdapter.class);
        }
        return xmlJavaTypeAdapter;
    }
    
    /**
     * @param type
     * @param annotations
     * @return
     */
    private XmlElement getXmlElementAnno(Type type,
            Annotation[] annotations) {
        XmlElement xmlElement = null;
        for (int i = 0; (annotations != null) && i < annotations.length; i++) {
            if (annotations[i].annotationType() == XmlElement.class) {
                xmlElement = (XmlElement)annotations[i];
                break;
            }
        }
        return xmlElement;
    }
    
    @SuppressWarnings("unchecked")
    protected Object unmarshalWithXmlAdapter(Object obj, Type type, Annotation[] annotations) {
        if ((type == null) || (annotations == null)) {
            return obj;
        }
        XmlJavaTypeAdapter xmlJavaTypeAdapter = getXmlJavaTypeAdapter(type,
                annotations);
        if (xmlJavaTypeAdapter != null) {
            try {
                XmlAdapter xmlAdapter = xmlJavaTypeAdapter.value().newInstance();
                return xmlAdapter.unmarshal(obj);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not unmarshal {} using {} due to exception:", new Object[]{obj, xmlJavaTypeAdapter.value().getName(), e});
                }
            }
        }
        return obj;
    }
    
    public static boolean isJAXBObject(Class<?> type) {
        return isXMLRootElement(type) || isXMLType(type);
    }

    private static boolean isXMLRootElement(Class<?> type) {
        Boolean isJAXBObject = jaxbIsXMLRootElementCache.get(type);

        if (isJAXBObject == null) {
            boolean isXmlRootElement = type.getAnnotation(XmlRootElement.class) != null;
            isJAXBObject = Boolean.valueOf(isXmlRootElement);
            jaxbIsXMLRootElementCache.put(type, isJAXBObject);
        }

        return isJAXBObject.booleanValue();
    }

    private static boolean isXMLType(Class<?> type) {
        Boolean isJAXBObject = jaxbIsXMLTypeCache.get(type);

        if (isJAXBObject == null) {
            boolean isXmlTypeElement = type.getAnnotation(XmlType.class) != null;
            isJAXBObject = Boolean.valueOf(isXmlTypeElement);
            jaxbIsXMLTypeCache.put(type, isJAXBObject);
        }

        return isJAXBObject.booleanValue();
    }

    public static boolean isJAXBElement(Class<?> type, Type genericType) {
        return (type == JAXBElement.class);
    }

    protected JAXBContext getContext(Class<?> type, MediaType mediaType) throws JAXBException {
        return getContext(type, type, mediaType);
    }

    protected JAXBContext getContext(Class<?> type, Type genericType, MediaType mediaType)
        throws JAXBException {
        if (logger.isTraceEnabled()) {
            logger
                .trace("getContext({}, {}, {}) entry", new Object[] {type, genericType, mediaType}); //$NON-NLS-1$
        }
        ContextResolver<JAXBContext> contextResolver =
            providers.getContextResolver(JAXBContext.class, mediaType);

        JAXBContext context = null;

        JAXBContextResolverKey key = null;
        if (contextCacheOn) {
            logger.trace("contextCacheOn is true"); //$NON-NLS-1$
            // it's ok and safe for contextResolver to be null at this point.
            // JAXBContextResolverKey can handle it
            key = new JAXBContextResolverKey(contextResolver, type);
            if (logger.isTraceEnabled()) {
                logger
                    .trace("created JAXBContextResolverKey {} for ({}, {}, {})", new Object[] {key, type, genericType, mediaType}); //$NON-NLS-1$
            }
            context = jaxbContextCache.get(key);
            logger.trace("retrieved context {}", context); //$NON-NLS-1$
            if (context != null) {
                if (logger.isTraceEnabled()) {
                    logger
                        .trace("retrieved context {}@{}", context.getClass().getName(), System.identityHashCode(context)); //$NON-NLS-1$
                    logger.trace("returned context {}", context); //$NON-NLS-1$
                }
                return context;
            }
        }

        if (contextResolver != null) {
            context = contextResolver.getContext(type);
        }

        if (context == null) {
            context = getDefaultContext(type, genericType);
        }

        if (contextCacheOn) {
            logger.trace("put key {} and context {} into jaxbContextCache", key, context); //$NON-NLS-1$
            jaxbContextCache.put(key, context);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("returned context {}", context); //$NON-NLS-1$
            logger
                .trace("retrieved context {}@{}", context.getClass().getName(), System.identityHashCode(context)); //$NON-NLS-1$
        }
        return context;
    }

    private JAXBContext getDefaultContext(final Class<?> type, final Type genericType) throws JAXBException {
        logger.trace("getDefaultContext({}, {}) entry", type, genericType); //$NON-NLS-1$
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<JAXBContext>() {

                public JAXBContext run() throws Exception {
                    JAXBContext context = jaxbDefaultContexts.get(type);
                    if (context == null) {

                        // CAUTION: be careful with this. Adding a second or more classes to
                        // the JAXBContext has the side
                        // effect of putting a namespace prefix and the namespace decl on
                        // the subelements of the
                        // desired type, thus degrading performance.

                        if (!isXMLRootElement(type) && !isXMLType(type)) { // use
                            // genericType.
                            // If that fails,
                            // we'll know
                            // soon enough
                            logger.trace("Using genericType to create context"); //$NON-NLS-1$
                            context = JAXBContext.newInstance((Class<?>)genericType);
                        } else {
                            logger.trace("Using type to create context"); //$NON-NLS-1$
                            context = JAXBContext.newInstance(type);
                        }

                        jaxbDefaultContexts.put(type, context);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("getDefaultContext() exit returning", context); //$NON-NLS-1$
                        logger
                            .trace("returning context {}@{}", context.getClass().getName(), System.identityHashCode(context)); //$NON-NLS-1$
                    }
                    return context;
                }
                
            });
        } catch(PrivilegedActionException e) {
            throw (JAXBException)e.getException();
        }
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
                if (logger.isErrorEnabled()) {
                    logger.error(Messages.getMessage("jaxbObjectFactoryNotFound", type.getName())); //$NON-NLS-1$
                }
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
                        && current.getName().startsWith("create")) { //$NON-NLS-1$
                        Object result = current.invoke(factory, new Object[] {jaxbObject});
                        return JAXBElement.class.cast(result);
                    }
                }
                return null;
            }
            if (logger.isWarnEnabled()) {
                logger.warn(Messages.getMessage("jaxbObjectFactoryInstantiate", type.getName())); //$NON-NLS-1$
            }
            return defaultWrapInJAXBElement(jaxbObject, type);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(Messages.getMessage("jaxbElementFailToBuild", type.getName()), e); //$NON-NLS-1$
            }
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
        final StringBuilder b = new StringBuilder(type.getPackage().getName());
        b.append(".ObjectFactory"); //$NON-NLS-1$
        Class<?> factoryClass = null;
        try {
            factoryClass = AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
                public Class<?> run() throws ClassNotFoundException {
                    return Thread.currentThread().getContextClassLoader().loadClass(b.toString());
                }
            });
        } catch(PrivilegedActionException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(Messages.getMessage("jaxbObjectFactoryNotFound", type.getName()), e); //$NON-NLS-1$
            }
            return null;
        }

        if (!factoryClass.isAnnotationPresent(XmlRegistry.class)) {
            if (logger.isErrorEnabled()) {
                logger.error(Messages.getMessage("jaxbObjectFactoryNotAnnotatedXMLRegistry", type //$NON-NLS-1$
                    .getName()));
            }
            return null;
        }

        return factoryClass;
    }

    @SuppressWarnings("unchecked")
    private JAXBElement<?> defaultWrapInJAXBElement(Object jaxbObject, Class<?> type) {
        if (logger.isInfoEnabled()) {
            logger.info(Messages.getMessage("jaxbCreateDefaultJAXBElement", type.getName())); //$NON-NLS-1$
        }
        String typeStr = type.getAnnotation(XmlType.class).name();
        return new JAXBElement(new QName(typeStr), type, jaxbObject);
    }

    /**
     * Pool a list of items for a specific key
     * 
     * @param <K> Key
     * @param <V> Pooled object
     */
    private static class Pool<K, V> {
        private SoftReference<ConcurrentHashMap<K, ArrayList<V>>> softMap         =
                                                                                      new SoftReference<ConcurrentHashMap<K, ArrayList<V>>>(
                                                                                                                                            new ConcurrentHashMap<K, ArrayList<V>>());
        /**
         * Maximum number of JAXBContexts to store
         */
        private static int                                        MAX_LOAD_FACTOR = 32;

        /** The maps are freed up when a LOAD FACTOR is hit */
        private static int                                        MAX_LIST_FACTOR = 50;

        /**
         * @param key
         * @return removed item from pool or null.
         */
        public V get(K key) {
            List<V> values = getValues(key);
            synchronized (values) {
                if (values.size() > 0) {
                    V v = values.remove(values.size() - 1);
                    return v;
                }
            }
            return null;
        }

        /**
         * Add item back to pool
         * 
         * @param key
         * @param value
         */
        public void put(K key, V value) {
            adjustSize();
            List<V> values = getValues(key);
            synchronized (values) {
                if (values.size() < MAX_LIST_FACTOR) {
                    values.add(value);
                }
            }
        }

        /**
         * Get or create a list of the values for the key
         * 
         * @param key
         * @return list of values (never null)
         */
        private List<V> getValues(K key) {
            ConcurrentHashMap<K, ArrayList<V>> map = softMap.get();
            ArrayList<V> values = null;
            if (map != null) {
                values = map.get(key);
                if (values != null) {
                    return values;
                }
            }
            synchronized (this) {
                if (map != null) {
                    values = map.get(key);
                }
                if (values == null) {
                    if (map == null) {
                        map = new ConcurrentHashMap<K, ArrayList<V>>();
                        softMap = new SoftReference<ConcurrentHashMap<K, ArrayList<V>>>(map);
                    }
                    values = new ArrayList<V>();
                    map.put(key, values);
                }
                return values;
            }
        }

        /**
         * When the number of keys exceeds the maximum load, half of the entries
         * are deleted. The assumption is that the JAXBContexts, UnMarshallers,
         * Marshallers, etc. require a large footprint.
         */
        private void adjustSize() {
            ConcurrentHashMap<K, ArrayList<V>> map = softMap.get();
            if (map != null && map.size() > MAX_LOAD_FACTOR) {
                // Remove every other Entry in the map.
                Iterator it = map.entrySet().iterator();
                boolean removeIt = false;
                while (it.hasNext()) {
                    it.next();
                    if (removeIt) {
                        it.remove();
                    }
                    removeIt = !removeIt;
                }
            }
        }
    }

}
