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
import java.lang.reflect.Type;

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

import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.synd.SyndContent;
import org.apache.wink.common.model.synd.SyndEntry;

@Provider
@Consumes( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON, MediaTypeUtils.JAVASCRIPT})
@Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON, MediaTypeUtils.JAVASCRIPT})
public class AtomEntrySyndEntryProvider implements MessageBodyReader<SyndEntry>,
    MessageBodyWriter<SyndEntry> {

    @Context
    private Providers providers;

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return type == SyndEntry.class;
    }

    public SyndEntry readFrom(Class<SyndEntry> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType,
                              MultivaluedMap<String, String> httpHeaders,
                              InputStream entityStream) throws IOException, WebApplicationException {
        MessageBodyReader<AtomEntry> reader =
            providers.getMessageBodyReader(AtomEntry.class, genericType, annotations, mediaType);
        AtomEntry entry =
            reader.readFrom(AtomEntry.class,
                            genericType,
                            annotations,
                            mediaType,
                            httpHeaders,
                            entityStream);
        
        /*
         * The value in the AtomContent object is arbitrary, set by the server.  We want to use the
         * built-in AND client application supplied providers.  Because these providers are collected
         * on the thread local store, and the unmarshalling of the AtomContent value is done lazily
         * (after the client-server context has expired), we need some way to hold onto the Providers
         * long enough for the client app to be able to seamlessly get the value off the AtomContent
         * object.  Thus the need to set the *real* providers list on the AtomContent object, so it
         * can use the list to pass to ModelUtils when it needs to retrieve and unmarshal the AtomContent
         * value.
         * 
         * We have to be careful to use the real Providers list instead of the one from the injected
         * local providers field in this class.  The injected object may be a "proxy" to the real
         * providers, and could cause an infinite loop when ModelUtils.readValue calls back through
         * the providers.
         */
        SyndEntry returnVal = entry.toSynd(new SyndEntry());
        SyndContent content = returnVal.getContent();
        if (content != null) {
            content.setProviders(RuntimeContextTLS.getRuntimeContext().getAttribute(Providers.class));
        }
        
        return returnVal;
    }

    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        MessageBodyWriter<AtomEntry> writer =
            providers.getMessageBodyWriter(AtomEntry.class, genericType, annotations, mediaType);
        return ((type.isAssignableFrom(SyndEntry.class)) && (writer != null));
    }

    public void writeTo(SyndEntry t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        AtomEntry entry = new AtomEntry(t);
        MessageBodyWriter<AtomEntry> writer =
            providers.getMessageBodyWriter(AtomEntry.class, genericType, annotations, mediaType);

        // already checked for non-null writer in isWriteable
        
        writer.writeTo(entry,
                       AtomEntry.class,
                       genericType,
                       annotations,
                       mediaType,
                       httpHeaders,
                       entityStream);
    }

    public long getSize(SyndEntry t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }
}
