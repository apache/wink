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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Consumes( {MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
@Produces( {MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
public class JAXBXmlProvider extends AbstractJAXBProvider implements MessageBodyReader<Object>,
    MessageBodyWriter<Object> {

    private static final Logger logger = LoggerFactory.getLogger(JAXBXmlProvider.class);

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return (isJAXBObject(type, genericType) || isCompatible(type, annotations)) && isSupportedMediaType(mediaType);
    }

    public Object readFrom(final Class<Object> type,
                           Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders,
                           final InputStream entityStream) throws IOException,
        WebApplicationException {

        Class<?> concreteType = getConcreteTypeFromTypeMap(type, annotations);
        
        Unmarshaller unmarshaller = null;
        Object unmarshaledResource = null;
        XMLStreamReader xmlStreamReader = null;
        try {
            JAXBContext context = getContext(concreteType, mediaType);
            unmarshaller = getJAXBUnmarshaller(concreteType, context, mediaType);
            xmlStreamReader = getXMLStreamReader(entityStream);
            if (concreteType.isAnnotationPresent(XmlRootElement.class)) {
                unmarshaledResource = unmarshaller.unmarshal(xmlStreamReader);
                closeXMLStreamReader(xmlStreamReader);
                if (unmarshaledResource instanceof JAXBElement) {
                    // this can happen if the JAXBContext object used to create
                    // the unmarshaller
                    // was created using the package name string instead of a
                    // class object and the
                    // ObjectFactory has a creator method for the desired object
                    // that returns
                    // JAXBElement. But we know better; the 'type' param passed
                    // in here had the
                    // XmlRootElement on it, so we know the desired return
                    // object type is NOT
                    // JAXBElement, thus:
                    unmarshaledResource = ((JAXBElement)unmarshaledResource).getValue();
                }
            } else {
                try {
                    final Unmarshaller _unmarshaller = unmarshaller;
                    final XMLStreamReader _xmlStreamReader = xmlStreamReader;
                    final Class<?> _concreteType = concreteType;
                    unmarshaledResource =
                        AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                            public Object run() throws PrivilegedActionException {
                                try {
                                    Object obj = _unmarshaller.unmarshal(_xmlStreamReader, _concreteType).getValue();
                                    closeXMLStreamReader(_xmlStreamReader);
                                    return obj;
                                } catch (JAXBException e) {
                                    throw new PrivilegedActionException(e);
                                }
                            }
                        });
                } catch (PrivilegedActionException e) {
                    closeXMLStreamReader(xmlStreamReader);
                    if (logger.isErrorEnabled()) {
                        logger
                            .error(Messages.getMessage("jaxbFailToUnmarshal", concreteType.getName()), e.getException()); //$NON-NLS-1$
                    }
                    throw new WebApplicationException(e.getException(), Response.Status.BAD_REQUEST);
                }
            }

            releaseJAXBUnmarshaller(context, unmarshaller);
        } catch (JAXBException e) {
            closeXMLStreamReader(xmlStreamReader);
            if (logger.isErrorEnabled()) {
                logger.error(Messages.getMessage("jaxbFailToUnmarshal", concreteType.getName()), e); //$NON-NLS-1$
            }
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        } catch (EntityReferenceXMLStreamException e) {
            closeXMLStreamReader(xmlStreamReader);
            if (logger.isErrorEnabled()) {
                logger.error(Messages.getMessage("entityRefsNotSupported")); //$NON-NLS-1$
            }
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        } catch (XMLStreamException e) {
            closeXMLStreamReader(xmlStreamReader);
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        } catch (RuntimeException e) {
            closeXMLStreamReader(xmlStreamReader);
            throw e;
        }
        return unmarshalWithXmlAdapter(unmarshaledResource, type, type, annotations);
    }

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
        return (isJAXBObject(type, genericType) || isCompatible(type, annotations)) && isSupportedMediaType(mediaType);
    }

    public void writeTo(Object t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

        t = marshalWithXmlAdapter(t, type, genericType, annotations);
        Class<?> concreteType = getConcreteTypeFromTypeMap(type, annotations);
        mediaType = MediaTypeUtils.setDefaultCharsetOnMediaTypeHeader(httpHeaders, mediaType);

        try {
            if (isJAXBObject(concreteType)) {
                JAXBContext context = getContext(concreteType, genericType, mediaType);
                if(logger.isTraceEnabled()) {
                    logger.trace("using context {}@{} to get marshaller", context.getClass().getName(), System.identityHashCode(context)); //$NON-NLS-1$
                }
                Marshaller marshaller = getJAXBMarshaller(concreteType, context, mediaType);
                Object entityToMarshal = getEntityToMarshal(t, concreteType);

                // Use an OutputStream directly instead of a Writer for
                // performance.
                marshaller.marshal(entityToMarshal, entityStream);

                releaseJAXBMarshaller(context, marshaller);
            } else if (genericType instanceof Class<?>) {
                JAXBContext context = getContext((Class<?>)genericType, genericType, mediaType);
                Marshaller marshaller =
                    getJAXBMarshaller((Class<?>)genericType, context, mediaType);
                Object entityToMarshal = getEntityToMarshal(t, (Class<?>)genericType);

                // Use an OutputStream directly instead of a Writer for
                // performance.
                marshaller.marshal(entityToMarshal, entityStream);

                releaseJAXBMarshaller(context, marshaller);
            }
        } catch (JAXBException e) {
            if (logger.isErrorEnabled()) {
                logger.error(Messages.getMessage("jaxbFailToMarshal", concreteType.getName()), e); //$NON-NLS-1$
            }
            throw new WebApplicationException(e);
        }
    }

}
