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

package org.apache.wink.common.internal.providers.entity.atom;

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
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBElement;

import org.apache.wink.common.model.atom.AtomFeed;

@Provider
@Consumes(MediaType.APPLICATION_ATOM_XML)
@Produces(MediaType.APPLICATION_ATOM_XML)
public class AtomFeedJAXBElementProvider extends AbstractAtomFeedProvider<JAXBElement<AtomFeed>>
    implements MessageBodyReader<JAXBElement<AtomFeed>>, MessageBodyWriter<JAXBElement<AtomFeed>> {

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return type == JAXBElement.class && isAtomFeedJAXBElement(genericType);
    }

    public JAXBElement<AtomFeed> readFrom(Class<JAXBElement<AtomFeed>> type,
                                          Type genericType,
                                          Annotation[] annotations,
                                          MediaType mediaType,
                                          MultivaluedMap<String, String> httpHeaders,
                                          InputStream entityStream) throws IOException,
        WebApplicationException {
        AtomFeed feed =
            readFeed(AtomFeed.class, genericType, annotations, mediaType, httpHeaders, entityStream);
        return atomObjectFactory.createFeed(feed);
    }

    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return type == JAXBElement.class && isAtomFeedJAXBElement(genericType);
    }

    public void writeTo(JAXBElement<AtomFeed> t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        AtomFeed feed = t.getValue();
        writeFeed(feed,
                  AtomFeed.class,
                  genericType,
                  annotations,
                  mediaType,
                  httpHeaders,
                  entityStream);
    }

    private boolean isAtomFeedJAXBElement(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType)genericType;
            if (pType.getActualTypeArguments()[0] == AtomFeed.class) {
                return true;
            }
        }
        return false;
    }
}
