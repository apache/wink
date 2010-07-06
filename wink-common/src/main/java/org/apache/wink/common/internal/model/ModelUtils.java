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

package org.apache.wink.common.internal.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.wink.common.RestException;
import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.WinkApplication;
import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.application.ApplicationFileLoader;
import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.contexts.ProvidersImpl;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.lifecycle.ScopeLifecycleManager;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.runtime.AbstractRuntimeContext;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.internal.utils.UnmodifiableMultivaluedMap;
import org.apache.wink.common.model.atom.AtomContent;
import org.apache.wink.common.model.atom.AtomText;
import org.apache.wink.common.model.atom.AtomTextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

public class ModelUtils {

    public static final MultivaluedMap<String, Object> EMPTY_OBJECT_MAP =
                                                                            new UnmodifiableMultivaluedMap<String, Object>(
                                                                                                                           new MultivaluedMapImpl<String, Object>());
    public static final MultivaluedMap<String, String> EMPTY_STRING_MAP =
                                                                            new UnmodifiableMultivaluedMap<String, String>(
                                                                                                                           new MultivaluedMapImpl<String, String>());
    public static final Annotation[]                   EMPTY_ARRAY      = new Annotation[0];
    private final static SAXParserFactory              spf;
    private final static DatatypeFactory               datatypeFactory;
    private static final Logger                        logger           =
                                                                            LoggerFactory
                                                                                .getLogger(ModelUtils.class);

    static {
        try {
            spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(false);
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (Exception e) {
            throw new RestException(Messages.getMessage("errorSettingUpAtom", e)); //$NON-NLS-1$
        }
    }

    public static boolean isTypeXml(String type) {

        // remove parameters if they exist
        int index = type.indexOf(';');
        if (index > -1) {
            type = type.substring(0, index).trim();
        }

        // as per RFC3023 and Atom specification
        type = type.toLowerCase();
        if (type.endsWith("/xml") || type.endsWith("+xml") //$NON-NLS-1$ //$NON-NLS-2$
            || type.equals("xhtml") //$NON-NLS-1$
            || type.equals("text/xml-external-parsed-entity") //$NON-NLS-1$
            || type.equals("application/xml-external-parsed-entity") //$NON-NLS-1$
            || type.equals("application/xml-dtd")) { //$NON-NLS-1$
            return true;
        }

        return false;
    }

    public static boolean isValueActuallyXml(Object source) {
        if (source instanceof AtomContent) {
            AtomContent content = (AtomContent)source;
            String type = content.getType();
            if (ModelUtils.isTypeXml(type)) {
                return true;
            }
        } else if (source instanceof AtomText) {
            AtomText text = (AtomText)source;
            AtomTextType type = text.getType();
            if (type == AtomTextType.xhtml) {
                return true;
            }
        }
        return false;
    }

    public static void saxParse(Reader reader, ContentHandler handler, String errorMessage) {
        XMLReader xmlReader;
        try {
            xmlReader = spf.newSAXParser().getXMLReader();
            xmlReader.setContentHandler(handler);
            // setting this property will cause the handler to get lexical
            // events as well
            if (handler instanceof LexicalHandler) {
                xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler); //$NON-NLS-1$
            }
            xmlReader.parse(new InputSource(reader));
        } catch (SAXException e) {
            logger.error(errorMessage);
            throw new WebApplicationException(e);
        } catch (ParserConfigurationException e) {
            logger.error(errorMessage);
            throw new WebApplicationException(e);
        } catch (IOException e) {
            logger.error(errorMessage);
            throw new WebApplicationException(e);
        }
    }

    public static Object unmarshal(Unmarshaller unmarshaller, Reader reader) throws IOException {
        Object result = null;
        try {
            result = unmarshaller.unmarshal(reader);
            if (result instanceof JAXBElement<?>) {
                result = ((JAXBElement<?>)result).getValue();
            }
        } catch (IllegalStateException e) {
            throw new WebApplicationException(e);
        } catch (JAXBException e) {
            throw new WebApplicationException(e);
        }
        return result;
    }

    public static void marshal(Marshaller marshaller, Object jaxbObject, OutputStream os)
        throws IOException {
        try {
            marshaller.marshal(jaxbObject, os);
        } catch (JAXBException e) {
            throw new WebApplicationException(e);
        }
    }

    public static XMLGregorianCalendar timeToXmlGregorianCalendar(long time) {
        if (time == -1) {
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        XMLGregorianCalendar xmlGregCal = datatypeFactory.newXMLGregorianCalendar(calendar);
        return xmlGregCal;
    }

    public static long xmlGregorianCalendarToTime(XMLGregorianCalendar xmlGregCal) {
        if (xmlGregCal == null) {
            return -1;
        }
        Calendar calendar = xmlGregCal.toGregorianCalendar();
        long time = calendar.getTimeInMillis();
        return time;
    }

    @SuppressWarnings("unchecked")
    public static <T> T readValue(List<Object> list,
                                  Class<T> type,
                                  Providers providers,
                                  Type genericType,
                                  Annotation[] annotations,
                                  MultivaluedMap<String, String> httpHeaders,
                                  MediaType mediaType) throws IOException {

        if (list == null || list.isEmpty()) {
            return null;
        }

        Object value = list.get(0);

        if (value == null) {
            return null;
        }

        Class<? extends Object> cls = value.getClass();
        if (type.isAssignableFrom(cls)) {
            return (T)value;
        }

        if (value instanceof JAXBElement<?>) {
            value = ((JAXBElement<?>)value).getValue();
            return readValue(Arrays.asList(value),
                             type,
                             providers,
                             genericType,
                             annotations,
                             httpHeaders,
                             mediaType);
        }

        if (cls == AtomXhtml.class) {
            return readValue(((AtomXhtml)value).getAny(),
                             type,
                             providers,
                             genericType,
                             annotations,
                             httpHeaders,
                             mediaType);
        }

        if (cls == XmlWrapper.class) {
            value = ((XmlWrapper)value).getValue();
            return readValue(Arrays.asList(value),
                             type,
                             providers,
                             genericType,
                             annotations,
                             httpHeaders,
                             mediaType);
        }

        if (value instanceof byte[]) {
            if (providers == null) {
                // try to get Providers from the TLS
                RuntimeContext runtimeContext = RuntimeContextTLS.getRuntimeContext();
                if (runtimeContext != null) {
                    providers = runtimeContext.getProviders();
                }

                if (providers == null) {
                    LifecycleManagersRegistry ofFactoryRegistry = new LifecycleManagersRegistry();
                    ofFactoryRegistry.addFactoryFactory(new ScopeLifecycleManager<Object>());
                    ProvidersRegistry providersRegistry =
                        new ProvidersRegistry(ofFactoryRegistry, new ApplicationValidator());

                    final Set<Class<?>> classes = new ApplicationFileLoader(true).getClasses();

                    processApplication(providersRegistry, new WinkApplication() {
                        @Override
                        public Set<Class<?>> getClasses() {
                            return classes;
                        }

                        @Override
                        public double getPriority() {
                            return WinkApplication.SYSTEM_PRIORITY;
                        }
                    });

                    providers = new ProvidersImpl(providersRegistry, runtimeContext);
                }
            }

            /*
             * Need to set a temporary RuntimeContextTLS just in case we're
             * already outside of the runtime context. This may occur when a
             * client app is retrieving the AtomContent value, expecting it to
             * be unmarshalled automatically, but we are already outside of the
             * client-server thread, and thus no longer have a RuntimeContextTLS
             * from which to retrieve or inject providers.
             */

            RuntimeContext tempRuntimeContext = RuntimeContextTLS.getRuntimeContext();
            if (tempRuntimeContext == null) {
                final Providers p = providers;
                RuntimeContextTLS.setRuntimeContext(new AbstractRuntimeContext() {
                    {
                        setAttribute(Providers.class, p);
                    }

                    public OutputStream getOutputStream() throws IOException {
                        return null;
                    }

                    public InputStream getInputStream() throws IOException {
                        return null;
                    }
                });
            }

            MessageBodyReader<T> reader =
                providers.getMessageBodyReader(type, type, EMPTY_ARRAY, mediaType);
            if (reader == null)
                throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);

            T read =
                reader.readFrom(type,
                                type,
                                annotations,
                                mediaType,
                                httpHeaders,
                                new ByteArrayInputStream((byte[])value));

            // Reset RuntimeContext from temporary above. tempRuntimeContext may
            // be null here, which is ok.
            RuntimeContextTLS.setRuntimeContext(tempRuntimeContext);

            return read;
        }
        throw new ClassCastException(Messages.getMessage("cannotCastTo", //$NON-NLS-1$
                                                         value.getClass().getName(),
                                                         type.getName()));

    }

    public static MediaType determineMediaType(String type) {
        MediaType mediaType;
        if (type == null || type.equals("text") || type.equals("html")) { //$NON-NLS-1$ //$NON-NLS-2$
            mediaType = MediaType.TEXT_PLAIN_TYPE;
        } else if (type.equals("xhtml")) { //$NON-NLS-1$
            mediaType = MediaType.APPLICATION_XML_TYPE;
        } else {
            mediaType = MediaType.valueOf(type);
        }
        return mediaType;
    }

    /**
     * Fixes content of any list.
     * <p>
     * This method provides the solution of wrapping the necessary elements with
     * XmlWrapper in order to invoke AnyContentHandler later.
     * 
     * @param any
     * @param type
     */
    public static void fixAnyContent(List<Object> any, String type) {
        if (any == null || any.isEmpty()) {
            // nothing to handle for null or empty objects
            return;
        }

        // retrieve the value to handle
        Object value = any.get(0);

        if (type == null) {
            // if type not set, use AtomTextType.text
            type = AtomTextType.text.name();
        }

        if (value instanceof XmlWrapper) {
            XmlWrapper xmlWrapper = (XmlWrapper)value;
            if (xmlWrapper.getType() == null) {
                // fixes type on the XmlWrapper in the case it was not set, it
                // happens if the same object was unmarsheled, and now is going
                // to be marsheled back to xml
                xmlWrapper.setType(type);
            }
        } else if (value.getClass() == String.class && !isTypeXml(type)) {
            // Non xml strings should be escaped
            // nothing to do
        } else {
            // wrapping with XmlWrapper will cause the Providers code to run
            // xml content won't be escaped
            any.set(0, new XmlWrapper(value, type));
        }
    }

    private static void processApplication(ProvidersRegistry providersRegistry,
                                           Application application) {
        if (application == null) {
            return;
        }

        // process singletons
        Set<Object> singletons = application.getSingletons();
        if (singletons != null && singletons.size() > 0) {
            processSingletons(providersRegistry, singletons);
        }

        // process classes
        Set<Class<?>> classes = application.getClasses();
        if (classes != null && classes.size() > 0) {
            processClasses(providersRegistry, classes);
        }

        if (application instanceof WinkApplication) {
            processWinkApplication(providersRegistry, (WinkApplication)application);
        }
    }

    private static void processClasses(ProvidersRegistry providersRegistry, Set<Class<?>> classes) {
        for (Class<?> cls : classes) {
            if (ProviderMetadataCollector.isProvider(cls)) {
                providersRegistry.addProvider(cls);
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn(Messages.getMessage("classNotAProvider", cls)); //$NON-NLS-1$
                }
            }
        }
    }

    private static void processSingletons(ProvidersRegistry providersRegistry,
                                          Set<Object> singletons) {
        for (Object obj : singletons) {
            Class<?> cls = obj.getClass();
            if (ProviderMetadataCollector.isProvider(cls)) {
                providersRegistry.addProvider(obj);
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn(Messages.getMessage("classNotAProvider", obj.getClass())); //$NON-NLS-1$
                }
            }
        }
    }

    private static void processWinkApplication(ProvidersRegistry providersRegistry,
                                               WinkApplication sApplication) {
        Set<Object> instances = sApplication.getInstances();
        double priority = sApplication.getPriority();
        if (instances == null) {
            return;
        }

        for (Object obj : instances) {
            Class<?> cls = obj.getClass();
            if (ProviderMetadataCollector.isProvider(cls)) {
                providersRegistry.addProvider(obj, priority);
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn(Messages.getMessage("classNotAProvider", obj.getClass())); //$NON-NLS-1$
                }
            }
        }
    }
}
