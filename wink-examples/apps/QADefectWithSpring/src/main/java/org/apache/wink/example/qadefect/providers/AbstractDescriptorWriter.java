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
package org.apache.wink.example.qadefect.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

public abstract class AbstractDescriptorWriter<T, DESCRIPTOR> implements MessageBodyWriter<T> {

    @Context
    private Providers providers;

    public long getSize(T t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    @SuppressWarnings("unchecked")
    public void writeTo(T bean,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

        DESCRIPTOR descriptor = getDescriptor(bean);
        MessageBodyWriter<DESCRIPTOR> syndEntryWriter =
            (MessageBodyWriter<DESCRIPTOR>)providers.getMessageBodyWriter(descriptor.getClass(),
                                                                          descriptor.getClass(),
                                                                          annotations,
                                                                          mediaType);
        syndEntryWriter.writeTo(descriptor,
                                descriptor.getClass(),
                                descriptor.getClass(),
                                annotations,
                                mediaType,
                                httpHeaders,
                                entityStream);
    }

    public abstract DESCRIPTOR getDescriptor(T instance);

}
