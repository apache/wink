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

package org.apache.wink.common.internal.providers.entity.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.annotations.Scope;
import org.apache.wink.common.annotations.Scope.ScopeType;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.json.JSONObject;

@Scope(ScopeType.PROTOTYPE)
@Provider
@Produces( {MediaType.APPLICATION_JSON, MediaTypeUtils.JAVASCRIPT})
public class JsonJAXBProvider extends AbstractJsonXmlProvider implements MessageBodyWriter<Object> {

    private static final Logger           logger = LoggerFactory.getLogger(JsonJAXBProvider.class);

    @Context
    private Providers                     providers;

    private MessageBodyWriter<JSONObject> bodyWriter;

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
        // we can handle only JAXB objects
        if (!isJAXBObject(type, genericType) && !isJAXBElement(type, genericType)) {
            return false;
        }
        // verify we have a writer for JSONObject
        bodyWriter =
            providers.getMessageBodyWriter(JSONObject.class,
                                           JSONObject.class,
                                           annotations,
                                           mediaType);
        if (bodyWriter == null) {
            return false;
        }
        return true;
    }

    public void writeTo(Object t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        Object jaxb = super.getEntityToMarshal(t, type);
        JSONObject json = jaxbToJson(jaxb, type, mediaType);
        bodyWriter.writeTo(json,
                           JSONObject.class,
                           JSONObject.class,
                           annotations,
                           mediaType,
                           httpHeaders,
                           entityStream);
    }

    private JSONObject jaxbToJson(Object jaxbObject, Class<?> type, MediaType mediaType) {
        try {
            if (type == JAXBElement.class) {
                type = ((JAXBElement<?>)jaxbObject).getDeclaredType();
            }
            Marshaller marshaller = super.getMarshaller(type, mediaType);
            JsonContentHandler handler = new JsonContentHandler();
            marshaller.setListener(handler);
            marshaller.marshal(jaxbObject, handler);
            return handler.getJsonResult();
        } catch (JAXBException e) {
            logger.error("Failed to convert JAXB object {} to JSONObject", type.getName());
            throw new WebApplicationException(e);
        }
    }

}
