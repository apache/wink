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
import java.lang.reflect.Type;
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.providers.entity.xml.AbstractJAXBProvider;
import org.codehaus.jettison.badgerfish.BadgerFishXMLInputFactory;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLInputFactory;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Jettison JAXB provider. By default, use the MappedNamespace convention.
 * Namespace mapping needs to be set if namespaces are used. In Application
 * sub-class, use {@link Application#getSingletons()} to add to application.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JettisonJAXBProvider extends AbstractJAXBProvider implements
    MessageBodyReader<Object>, MessageBodyWriter<Object> {

    private static final Logger logger = LoggerFactory.getLogger(JettisonJAXBProvider.class);

    final private boolean       isBadgerFishConventionUsed;

    final private Configuration inputConfiguration;

    final private Configuration outputConfiguration;

    private boolean             isReadable;

    private boolean             isWritable;

    public JettisonJAXBProvider() {
        this(true, null, null);
    }

    public JettisonJAXBProvider(boolean isBadgerFishConventionUsed,
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

        // see http://jira.codehaus.org/browse/JETTISON-74 . reading disabled for now
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
        return isReadable && isJAXBObject(type, genericType);
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
            unmarshaller = getJAXBUnmarshaller(context);

            XMLStreamReader xsr = null;

            if (isBadgerFishConventionUsed) {
                xsr = new BadgerFishXMLInputFactory().createXMLStreamReader(entityStream);
            } else {
                xsr =
                    new MappedXMLInputFactory(inputConfiguration)
                .createXMLStreamReader(entityStream);
            }

            if (type.isAnnotationPresent(XmlRootElement.class)) {
                unmarshaledResource = unmarshaller.unmarshal(xsr);
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
                unmarshaledResource = unmarshaller.unmarshal(xsr, type).getValue();
            }
        } catch (JAXBException e) {
            logger.error(Messages.getMessage("jaxbFailToUnmarshal"), type.getName());
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        } catch (XMLStreamException e) {
            logger.error(Messages.getMessage("jaxbFailToUnmarshal"), type.getName());
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
        return isWritable && isJAXBObject(type, genericType);
    }

    public void writeTo(Object t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            JAXBContext context = getContext(type, mediaType);
            Marshaller marshaller = getJAXBMarshaller(type, context, mediaType);
            Object entityToMarshal = getEntityToMarshal(t, type);

            // Use an OutputStream directly instead of a Writer for performance.
            XMLStreamWriter xsw = null;
            if (isBadgerFishConventionUsed) {
                xsw = new BadgerFishXMLStreamWriter(new OutputStreamWriter(entityStream));
            } else {
                MappedNamespaceConvention con = new MappedNamespaceConvention(outputConfiguration);
                xsw = new MappedXMLStreamWriter(con, new OutputStreamWriter(entityStream));
            }
            marshaller.marshal(entityToMarshal, xsw);
        } catch (JAXBException e) {
            logger.error(Messages.getMessage("jaxbFailToMarshal"), type.getName());
            throw new WebApplicationException(e);
        }
    }
}
