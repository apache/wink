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
package org.apache.wink.providers.jettison;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.providers.entity.xml.AbstractJAXBProvider;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.utils.ProviderUtils;
import org.codehaus.jettison.badgerfish.BadgerFishXMLInputFactory;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedXMLInputFactory;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Jettison JAXBElement provider. By default, use the BadgerFishConvention.
 * Namespace mapping needs to be set if namespaces are used. In Application
 * sub-class, use {@link Application#getSingletons()} to add to application.
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JettisonJAXBElementProvider extends AbstractJAXBProvider implements
    MessageBodyReader<JAXBElement<?>>, MessageBodyWriter<JAXBElement<?>> {

    private static final Logger logger = LoggerFactory.getLogger(JettisonJAXBElementProvider.class);

    final private boolean       isBadgerFishConventionUsed;

    final private Configuration inputConfiguration;

    final private Configuration outputConfiguration;

    private boolean             isReadable;

    private boolean             isWritable;

    public JettisonJAXBElementProvider() {
        this(true, null, null);
    }

    public JettisonJAXBElementProvider(boolean isBadgerFishConventionUsed,
                                       Configuration reader,
                                       Configuration writer) {
        this.isBadgerFishConventionUsed = isBadgerFishConventionUsed;
        if (reader != null) {
            this.inputConfiguration = reader;
        } else {
            this.inputConfiguration = new Configuration(new HashMap<String, String>());
        }
        if (writer != null) {
            this.outputConfiguration = writer;
        } else {
            this.outputConfiguration = new Configuration(new HashMap<String, String>());
        }
        // see http://jira.codehaus.org/browse/JETTISON-74 . reading disabled
        // for now
        isReadable = false;
        isWritable = true;
    }

    public void setUseAsReader(boolean isReadable) {
        this.isReadable = isReadable;
    }

    public void setUseAsWriter(boolean isWritable) {
        this.isWritable = isWritable;
    }

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return isReadable && isJAXBElement(type, genericType);
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
            JAXBContext context = getContext(classToFill, mediaType);
            unmarshaller = getJAXBUnmarshaller(type, context, mediaType);

            XMLStreamReader xsr = null;
            if (isBadgerFishConventionUsed) {
                xsr = new BadgerFishXMLInputFactory().createXMLStreamReader(entityStream);
            } else {
                xsr =
                    new MappedXMLInputFactory(inputConfiguration)
                        .createXMLStreamReader(new StreamSource(entityStream));
            }

            unmarshaledResource = unmarshaller.unmarshal(xsr, classToFill);
        } catch (JAXBException e) {
            if (logger.isErrorEnabled()) {
                logger.error(Messages.getMessage("jaxbFailToUnmarshal", type.getName())); //$NON-NLS-1$
            }
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        } catch (XMLStreamException e) {
            if (logger.isErrorEnabled()) {
                logger.error(Messages.getMessage("jaxbFailToUnmarshal", type.getName())); //$NON-NLS-1$
            }
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
        return isWritable && isJAXBElement(type, genericType);
    }

    public void writeTo(JAXBElement<?> t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            mediaType = MediaTypeUtils.setDefaultCharsetOnMediaTypeHeader(httpHeaders, mediaType);

            Class<?> declaredType = t.getDeclaredType();
            JAXBContext context = getContext(declaredType, mediaType);
            Marshaller marshaller = getJAXBMarshaller(declaredType, context, mediaType);
            Charset charset = Charset.forName(ProviderUtils.getCharset(mediaType));
            OutputStreamWriter writer = new OutputStreamWriter(entityStream, charset);

            XMLStreamWriter xsw = null;
            if (isBadgerFishConventionUsed) {
                xsw = new BadgerFishXMLStreamWriter(writer);
            } else {
                try {
                    xsw =
                        new MappedXMLOutputFactory(outputConfiguration)
                            .createXMLStreamWriter(writer);
                } catch (XMLStreamException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error(Messages.getMessage("jaxbFailToMarshal", t.getName())); //$NON-NLS-1$
                    }
                    throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
                }
            }

            marshaller.marshal(t, xsw);
            writer.flush();
        } catch (JAXBException e) {
            if (logger.isErrorEnabled()) {
                logger.error(Messages.getMessage("jaxbFailToMarshal", t.getName())); //$NON-NLS-1$
            }
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

    }
}
