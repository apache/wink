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

package org.apache.wink.providers.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.apache.wink.common.annotations.Scope;
import org.apache.wink.common.annotations.Scope.ScopeType;
import org.apache.wink.common.internal.providers.entity.xml.JAXBXmlProvider;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

@Scope(ScopeType.PROTOTYPE)
@Provider
@Produces( {MediaType.APPLICATION_JSON, MediaTypeUtils.JAVASCRIPT})
@Consumes( {MediaType.APPLICATION_JSON, MediaTypeUtils.JAVASCRIPT})
public class JsonJAXBProvider implements MessageBodyWriter<Object>, MessageBodyReader<Object> {

    @Context
    private Providers                     providers;

    private MessageBodyWriter<JSONObject> bodyWriter;
    private MessageBodyReader<JSONObject> bodyReader;

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
        if (!JAXBXmlProvider.isJAXBObject(type, genericType) && !JAXBXmlProvider
            .isJAXBElement(type, genericType)) {
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

        try {
            mediaType = MediaTypeUtils.setDefaultCharsetOnMediaTypeHeader(httpHeaders, mediaType);

            @SuppressWarnings("unchecked")
            MessageBodyWriter<Object> jaxbWriter =
                (MessageBodyWriter<Object>)providers
                    .getMessageBodyWriter(type,
                                          genericType,
                                          annotations,
                                          MediaType.APPLICATION_XML_TYPE);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            jaxbWriter.writeTo(t,
                               type,
                               genericType,
                               annotations,
                               MediaType.APPLICATION_XML_TYPE,
                               httpHeaders,
                               os);
            JSONObject json = XML.toJSONObject(os.toString());
            bodyWriter.writeTo(json,
                               JSONObject.class,
                               JSONObject.class,
                               annotations,
                               mediaType,
                               httpHeaders,
                               entityStream);
        } catch (JSONException e) {
            throw new WebApplicationException(e);
        }
    }

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        // we can handle only JAXB objects
        if (!JAXBXmlProvider.isJAXBObject(type, genericType) && !JAXBXmlProvider
            .isJAXBElement(type, genericType)) {
            return false;
        }
        // verify we have a reader for JSONObject
        bodyReader =
            providers.getMessageBodyReader(JSONObject.class,
                                           JSONObject.class,
                                           annotations,
                                           mediaType);
        if (bodyReader == null) {
            return false;
        }
        return true;
    }

    public Object readFrom(Class<Object> type,
                           Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {

        try {
            JSONObject json =
                bodyReader.readFrom(JSONObject.class,
                                    JSONObject.class,
                                    annotations,
                                    mediaType,
                                    httpHeaders,
                                    entityStream);
            String xml = XML.toString(json);
            MessageBodyReader<Object> jaxbReader =
                providers.getMessageBodyReader(type,
                                               genericType,
                                               annotations,
                                               MediaType.APPLICATION_XML_TYPE);
            return jaxbReader.readFrom(type,
                                       genericType,
                                       annotations,
                                       MediaType.APPLICATION_XML_TYPE,
                                       httpHeaders,
                                       new ByteArrayInputStream(xml.getBytes()));
        } catch (JSONException e) {
            throw new WebApplicationException(e, Status.BAD_REQUEST);
        }
    }
}
