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

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.utils.JAXBUtils;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.internal.utils.SoftConcurrentMap;
import org.apache.wink.common.model.JAXBUnmarshalOptions;
import org.apache.wink.common.model.XmlFormattingOptions;
import org.apache.wink.common.utils.ProviderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJAXBProvider {

    private static final Logger                                   logger                    =
                                                                                                LoggerFactory
                                                                                                    .getLogger(AbstractJAXBProvider.class);
    private static final SoftConcurrentMap<Class<?>, JAXBContext> jaxbDefaultContexts       =
                                                                                                new SoftConcurrentMap<Class<?>, JAXBContext>();

    @Context
    protected Providers                                           providers;

    private static final SoftConcurrentMap<Class<?>, Boolean>     jaxbIsXMLRootElementCache =
                                                                                                new SoftConcurrentMap<Class<?>, Boolean>();

    private static final SoftConcurrentMap<Class<?>, Boolean>     jaxbIsXMLTypeCache        =
                                                                                                new SoftConcurrentMap<Class<?>, Boolean>();

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

    /**
     * This class is the key to the JAXBContext cache. It must be based on the
     * ContextResolver instance who created the JAXBContext and the type passed
     * to it. The only way this cache becomes invalid is if the ContextResolver
     * does something crazy like create JAXBContexts based on time -- it only
     * creates contexts between noon and 5:00pm. So, uhhh, don't do that.
     */
    private static class JAXBContextResolverKey {
        protected ContextResolver<JAXBContext> _resolver;
        protected Type                         _type;
        private int                            hashCode = -1;

        public JAXBContextResolverKey(ContextResolver<JAXBContext> resolver, Type type) {
            // resolver may be null, which is ok; we'll protect against NPEs in
            // equals and hashCode overrides
            _resolver = resolver;
            _type = type;
        }

        @Override
        public boolean equals(Object o) {
            if ((o == null) || (!(o instanceof JAXBContextResolverKey))) {
                return false;
            }
            JAXBContextResolverKey obj = (JAXBContextResolverKey)o;
            // check for both null or both NOT null
            boolean result =
                ((obj._resolver == null) && (_resolver == null)) || ((obj._resolver != null) && (_resolver != null));
            // we can use hashCode() to compare _resolver
            return result && (obj.hashCode() == hashCode()) && (obj._type.equals(_type));
        }

        @Override
        public int hashCode() {
            if (hashCode != -1) {
                return hashCode;
            }
            if (_resolver == null) {
                // allow the key to be based entirely on the _type object
                // equality from equals method. Only YOU can prevent NPEs.
                hashCode = 0; // don't use _type's hashCode, as the instances
                // may differ between JAXBContextResolverKey
                // instances
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
            byte[] bytes = resolverName.getBytes();
            for (int i = 0; i < bytes.length; i++) {
                hashCode += bytes[i];
            }
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
                                                                                                  .getProperty("org.apache.wink.jaxbcontextcache");
    // non-final, protected only to make it unittestable
    static protected boolean                                             contextCacheOn   =
                                                                                              !((propVal != null) && (propVal
                                                                                                  .equalsIgnoreCase("off")));

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
            if (logger.isDebugEnabled()) {
                logger.debug("Unmarshaller created [not in pool]"); //$NON-NLS-1$
            }
            unm = internalCreateUnmarshaller(context);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Unmarshaller obtained [from  pool]"); //$NON-NLS-1$
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
        if (logger.isDebugEnabled()) {
            logger.debug("Unmarshaller placed back into pool"); //$NON-NLS-1$
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
            if (logger.isDebugEnabled()) {
                logger.debug("Marshaller created [not in pool]"); //$NON-NLS-1$
            }
            m = internalCreateMarshaller(context);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Marshaller obtained [from  pool]"); //$NON-NLS-1$
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
        if (logger.isDebugEnabled()) {
            logger.debug("Marshaller placed back into pool"); //$NON-NLS-1$
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

        ContextResolver<JAXBContext> contextResolver =
            providers.getContextResolver(JAXBContext.class, mediaType);

        JAXBContext context = null;

        JAXBContextResolverKey key = null;
        if (contextCacheOn) {
            // it's ok and safe for contextResolver to be null at this point.
            // JAXBContextResolverKey can handle it
            key = new JAXBContextResolverKey(contextResolver, type);
            context = jaxbContextCache.get(key);
            if (context != null) {
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
            jaxbContextCache.put(key, context);
        }

        return context;
    }

    private JAXBContext getDefaultContext(Class<?> type, Type genericType) throws JAXBException {
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
                context = JAXBContext.newInstance((Class<?>)genericType);
            } else {
                context = JAXBContext.newInstance(type);
            }

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
                logger.error(Messages.getMessage("jaxbObjectFactoryNotFound", type.getName())); //$NON-NLS-1$
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
            logger.warn(Messages.getMessage("jaxbObjectFactoryInstantiate", type.getName())); //$NON-NLS-1$
            return defaultWrapInJAXBElement(jaxbObject, type);
        } catch (Exception e) {
            logger.error(Messages.getMessage("jaxbElementFailToBuild", type.getName())); //$NON-NLS-1$
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
        b.append(".ObjectFactory"); //$NON-NLS-1$
        Class<?> factoryClass = null;
        try {
            factoryClass = Thread.currentThread().getContextClassLoader().loadClass(b.toString());
        } catch (ClassNotFoundException e) {
            logger.error(Messages.getMessage("jaxbObjectFactoryNotFound", type.getName())); //$NON-NLS-1$
            return null;
        }

        if (!factoryClass.isAnnotationPresent(XmlRegistry.class)) {
            logger.error(Messages.getMessage("jaxbObjectFactoryNotAnnotatedXMLRegistry", type //$NON-NLS-1$
                .getName()));
            return null;
        }

        return factoryClass;
    }

    @SuppressWarnings("unchecked")
    private JAXBElement<?> defaultWrapInJAXBElement(Object jaxbObject, Class<?> type) {
        logger.info(Messages.getMessage("jaxbCreateDefaultJAXBElement", type.getName())); //$NON-NLS-1$
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
