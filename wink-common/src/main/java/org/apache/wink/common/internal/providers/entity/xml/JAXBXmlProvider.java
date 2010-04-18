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
import javax.xml.transform.stream.StreamSource;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.utils.ProviderUtils;
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
        return isJAXBObject(type, genericType) && isSupportedMediaType(mediaType);
    }

    public Object readFrom(Class<Object> type,
                           Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {
        Unmarshaller unmarshaller = null;
        Object unmarshaledResource = null;
        try {
            JAXBContext context = getContext(type, mediaType);
            unmarshaller = getJAXBUnmarshaller(type, context, mediaType);
            if (type.isAnnotationPresent(XmlRootElement.class)) {
                unmarshaledResource = unmarshaller.unmarshal(entityStream);
                if (unmarshaledResource instanceof JAXBElement) {
                    // this can happen if the JAXBContext object used to create the unmarshaller
                    // was created using the package name string instead of a class object and the
                    // ObjectFactory has a creator method for the desired object that returns
                    // JAXBElement.  But we know better; the 'type' param passed in here had the
                    // XmlRootElement on it, so we know the desired return object type is NOT
                    // JAXBElement, thus:
                    unmarshaledResource = ((JAXBElement)unmarshaledResource).getValue();
                }
            } else {
                unmarshaledResource =
                    unmarshaller.unmarshal(new StreamSource(entityStream), type).getValue();
            }

            releaseJAXBUnmarshaller(context, unmarshaller);
        } catch (JAXBException e) {
            logger.error(Messages.getMessage("jaxbFailToUnmarshal", type.getName()), e); //$NON-NLS-1$
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        return unmarshaledResource;
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
        return isJAXBObject(type, genericType) && isSupportedMediaType(mediaType);
    }

    public void writeTo(Object t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        
        ProviderUtils.setDefaultCharsetOnMediaTypeHeader(httpHeaders, mediaType);

        try {
            if (isJAXBObject(type)) {
                JAXBContext context = getContext(type, genericType, mediaType);
                Marshaller marshaller = getJAXBMarshaller(type, context, mediaType);
                Object entityToMarshal = getEntityToMarshal(t, type);

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
            logger.error(Messages.getMessage("jaxbFailToMarshal", type.getName()), e); //$NON-NLS-1$
            throw new WebApplicationException(e);
        }
    }

}
