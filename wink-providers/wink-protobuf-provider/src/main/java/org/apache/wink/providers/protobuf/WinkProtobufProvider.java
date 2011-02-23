/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.wink.providers.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.google.protobuf.Message;

/**
 * JAX-RS message provider for Google Protocol Buffer 
 */
@Provider
@Consumes("application/x-protobuf")
@Produces("application/x-protobuf")
public class WinkProtobufProvider implements MessageBodyReader<Message>, MessageBodyWriter<Message> {
    public static final String PROTOBUF = "application/x-protobuf";
    
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type != null && Message.class.isAssignableFrom(type);
    }

    public Message readFrom(Class<Message> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, String> httpHeaders,
                            InputStream entityStream) throws IOException, WebApplicationException {
        try {
            Method method = type.getMethod("parseFrom", InputStream.class);
            Object result = method.invoke(null, entityStream);
            return (Message)result;
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof IOException) {
                throw (IOException)t;
            } else {
                throw new WebApplicationException(t);
            }
        } catch (Throwable e) {
            throw new WebApplicationException(e);
        }
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type != null && Message.class.isAssignableFrom(type);
    }

    public long getSize(Message t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    public void writeTo(Message t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        if (t != null) {
            t.writeTo(entityStream);
        }
    }

}
