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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.apache.wink.common.internal.providers.entity.xml.AbstractJAXBCollectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JAXBArrayJSONProvider extends AbstractJAXBCollectionProvider implements
    MessageBodyReader<Object[]>, MessageBodyWriter<Object[]> {

    protected volatile MessageBodyReader<Object> readerProvider = null;
    protected volatile MessageBodyWriter<Object> writerProvider = null;

    private static final Logger                  logger         =
                                                                    LoggerFactory
                                                                        .getLogger(JAXBArrayJSONProvider.class);

    @Context
    Providers                                    injectedProviders;

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        Class<?> theType = getParameterizedTypeClassForRead(type, genericType, false);
        if (theType != null)
            return (type.isArray() && isJAXBObject(theType, genericType) && !isJAXBElement(theType,
                                                                                           genericType));
        return false;
    }

    public long getSize(Object[] t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    @SuppressWarnings("unchecked")
    public Object[] readFrom(Class<Object[]> type,
                             Type genericType,
                             Annotation[] annotations,
                             MediaType mediaType,
                             MultivaluedMap<String, String> httpHeaders,
                             InputStream entityStream) throws IOException, WebApplicationException {
        Class<?> theType = getParameterizedTypeClassForRead(type, genericType, false);
        if (this.readerProvider == null) {
            this.readerProvider =
                injectedProviders.getMessageBodyReader((Class<Object>)theType,
                                                       theType,
                                                       annotations,
                                                       mediaType);
            if (logger.isTraceEnabled()) {
                logger
                    .trace("readerProvider was {} of type {}", System.identityHashCode(readerProvider), readerProvider.getClass().getName()); //$NON-NLS-1$
            }
        }
        Queue<String> queue = new LinkedList<String>();
        List<Object> collection = new ArrayList<Object>();
        Pattern p = Pattern.compile("\\S"); //$NON-NLS-1$
        Matcher m = null;
        int next = entityStream.read();
        while (next != -1) {
            m = p.matcher("" + (char)next); //$NON-NLS-1$
            if (m.matches() && (char)next != '[')
                throw new WebApplicationException(500);
            else if (!m.matches())
                next = (char)entityStream.read();
            else {
                // we found the first non-whitespace character is '['. Read the
                // next character and begin parsing
                next = entityStream.read();
                break;
            }
        }

        // parse the content and deserialize the JSON Object one by one
        String objectString = ""; //$NON-NLS-1$
        while (next != -1) {
            if (((char)next != ',') || ((char)next == ',' && !queue.isEmpty()))
                objectString += (char)next;
            if ((char)next == '{')
                queue.offer("" + (char)next); //$NON-NLS-1$
            else if ((char)next == '}') {
                queue.poll();
                if (queue.isEmpty()) {
                    collection.add(this.readerProvider
                        .readFrom((Class<Object>)theType,
                                  theType,
                                  annotations,
                                  mediaType,
                                  httpHeaders,
                                  new ByteArrayInputStream(objectString.getBytes())));
                    objectString = ""; //$NON-NLS-1$
                }
            }
            next = entityStream.read();
        }
        return (Object[])getArray(theType, collection);
    }

    @SuppressWarnings("unchecked")
    protected static <T> Object getArray(Class<T> type, List<?> collection) {
        T[] ret = (T[])Array.newInstance(type, collection.size());
        int i = 0;
        for (Object o : collection) {
            ret[i++] = (T)o;
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public void writeTo(Object[] t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        Class<?> theType = getParameterizedTypeClassForWrite(type, genericType, false);
        if (this.writerProvider == null) {
            this.writerProvider =
                injectedProviders.getMessageBodyWriter((Class<Object>)theType,
                                                       theType,
                                                       annotations,
                                                       mediaType);
            if (logger.isTraceEnabled()) {
                logger
                    .trace("writerProvider was {} of type {}", System.identityHashCode(writerProvider), writerProvider.getClass().getName()); //$NON-NLS-1$
            }
        }
        entityStream.write("[".getBytes()); //$NON-NLS-1$
        int i = 0;
        for (Object o : t) {
            this.writerProvider.writeTo(o,
                                        theType,
                                        theType,
                                        annotations,
                                        mediaType,
                                        httpHeaders,
                                        entityStream);
            if ((++i) != t.length)
                entityStream.write(",".getBytes()); //$NON-NLS-1$
        }
        entityStream.write("]".getBytes()); //$NON-NLS-1$
    }

    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        Class<?> theType = getParameterizedTypeClassForWrite(type, genericType, false);

        if (theType != null)
            return (isJAXBObject(theType, genericType) && !isJAXBElement(theType, genericType));
        return false;
    }

}
