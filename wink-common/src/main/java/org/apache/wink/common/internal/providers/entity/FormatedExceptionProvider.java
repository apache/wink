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

package org.apache.wink.common.internal.providers.entity;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.apache.wink.common.internal.utils.ExceptionHelper;
import org.apache.wink.common.model.synd.SyndContent;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.common.utils.ProviderUtils;

@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class FormatedExceptionProvider implements MessageBodyWriter<Throwable> {

    private static final String ID_PREFIX = "urn:uuid:"; //$NON-NLS-1$

    @Context
    private Providers           providers;

    public long getSize(Throwable t,
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
        // do not check for non-null writer here; writeTo will handle this situation
        return Throwable.class.isAssignableFrom(type);
    }

    public void writeTo(Throwable t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

        SyndEntry se = new SyndEntry();
        Class<?> rawType = se.getClass();
        Type genType = rawType;
        String defaultErrorMessage = "An error has occurred while processing a request";

        // Check if SyndEntry supports response MediaType
        @SuppressWarnings("unchecked")
        MessageBodyWriter<Object> messageBodyWriter =
            (MessageBodyWriter<Object>)providers.getMessageBodyWriter(rawType,
                                                                      genType,
                                                                      null,
                                                                      mediaType);
        String localizedMessage = t.getLocalizedMessage();
        localizedMessage = localizedMessage == null ? defaultErrorMessage : localizedMessage;

        if (messageBodyWriter != null) {
            se.setId(ID_PREFIX + UUID.randomUUID());
            se.setUpdated(new Date(System.currentTimeMillis()));
            se.setPublished(new Date(System.currentTimeMillis()));
            se.setTitle(new SyndText(localizedMessage));
            SyndContent syndContent = new SyndContent();
            syndContent.setType(MediaType.TEXT_PLAIN);
            syndContent.setValue(ExceptionHelper.stackTraceToString(t));
            se.setContent(syndContent);
            se.setSummary(new SyndText(localizedMessage));
            messageBodyWriter.writeTo(se,
                                      rawType,
                                      genType,
                                      null,
                                      mediaType,
                                      httpHeaders,
                                      entityStream);
        } else {
            localizedMessage = "<error>" + localizedMessage + "</error>"; //$NON-NLS-1$ //$NON-NLS-2$
            entityStream.write(localizedMessage.getBytes(ProviderUtils.getCharset(mediaType)));
        }
    }

}
