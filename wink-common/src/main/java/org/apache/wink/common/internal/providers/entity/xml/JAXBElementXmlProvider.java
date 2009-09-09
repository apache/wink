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
import java.lang.reflect.ParameterizedType;
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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.utils.ProviderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Consumes( {MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
@Produces( {MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
public class JAXBElementXmlProvider extends AbstractJAXBProvider implements
    MessageBodyReader<JAXBElement<?>>, MessageBodyWriter<JAXBElement<?>> {

    private static final Logger logger = LoggerFactory.getLogger(JAXBElementXmlProvider.class);

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return isJAXBElement(type, genericType) && isSupportedMediaType(mediaType);
    }

    public JAXBElement<?> readFrom(Class<JAXBElement<?>> type,
                                   Type genericType,
                                   Annotation[] annotations,
                                   MediaType mediaType,
                                   MultivaluedMap<String, String> httpHeaders,
                                   InputStream entityStream) throws IOException,
        WebApplicationException {
        ParameterizedType parameterizedType = (ParameterizedType)genericType;
        Class<?> classToFill = (Class<?>)parameterizedType.getActualTypeArguments()[0];
        JAXBElement<?> unmarshaledResource = null;
        Unmarshaller unmarshaller = null;

        try {
            unmarshaller = getUnmarshaller(classToFill, mediaType);
            unmarshaledResource =
                unmarshaller.unmarshal(new StreamSource(entityStream), classToFill);
        } catch (JAXBException e) {
            logger.error(Messages.getMessage("jaxbFailToUnmarshal"), type.getName());
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        return unmarshaledResource;
    }

    public long getSize(JAXBElement<?> t,
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
        return isJAXBElement(type, genericType) && isSupportedMediaType(mediaType);
    }

    public void writeTo(JAXBElement<?> t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            Marshaller marshaller = getMarshaller(t.getDeclaredType(), mediaType);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, ProviderUtils.getCharset(mediaType));
            marshaller.marshal(t, entityStream);
        } catch (JAXBException e) {
            logger.error(Messages.getMessage("jaxbFailToMarshal"), t.getName());
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
