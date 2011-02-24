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

package org.apache.wink.providers.thrift;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;

/**
 * JAX-RS message provider for Apache Thrift 
 */
@SuppressWarnings("rawtypes")
@Provider
@Consumes({"application/x-thrift", MediaType.APPLICATION_JSON})
@Produces({"application/x-thrift", MediaType.APPLICATION_JSON})
public class WinkThriftProvider implements MessageBodyReader<TBase>, MessageBodyWriter<TBase> {
    public static final String THRIFT = "application/x-thrift";
    public static final MediaType THRIFT_TYPE = new MediaType("application", "x-thrift");

    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type != null && TBase.class.isAssignableFrom(type);
    }

    public TBase readFrom(Class<TBase> type,
                          Type genericType,
                          Annotation[] annotations,
                          MediaType mediaType,
                          MultivaluedMap<String, String> httpHeaders,
                          InputStream entityStream) throws IOException, WebApplicationException {
        try {
            return unmarshal(type, mediaType, entityStream);
        } catch (Throwable e) {
            throw new WebApplicationException(e);
        }
    }

    static <T extends TBase> T unmarshal(Class<T> type, MediaType mediaType, InputStream entityStream) throws Exception {
        TIOStreamTransport transport = new TIOStreamTransport(entityStream);
        TProtocol protocol = null;
        if (THRIFT_TYPE.isCompatible(mediaType)) {
            protocol = new TBinaryProtocol.Factory().getProtocol(transport);
        } else if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
            protocol = new TJSONProtocol.Factory().getProtocol(transport);
        }
        T t = type.newInstance();
        t.read(protocol);
        return t;
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type != null && TBase.class.isAssignableFrom(type);
    }

    public long getSize(TBase t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    public void writeTo(TBase t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        marshal(t, mediaType, entityStream);
    }

    static <T extends TBase> void marshal(T t, MediaType mediaType, OutputStream entityStream) throws IOException {
        TIOStreamTransport transport = new TIOStreamTransport(entityStream);
        TProtocol protocol = null;
        if (THRIFT_TYPE.isCompatible(mediaType)) {
            protocol = new TBinaryProtocol.Factory().getProtocol(transport);
        } else if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
            protocol = new TJSONProtocol.Factory().getProtocol(transport);
        }
        if (t != null) {
            try {
                t.write(protocol);
            } catch (TException e) {
                throw new IOException(e);
            }
        }
    }

}
