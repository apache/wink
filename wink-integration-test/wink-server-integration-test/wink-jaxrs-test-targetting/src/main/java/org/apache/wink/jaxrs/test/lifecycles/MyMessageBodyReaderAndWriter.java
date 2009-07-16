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

package org.apache.wink.jaxrs.test.lifecycles;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

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

@Provider
@Produces("text/plain")
@Consumes("text/plain")
public class MyMessageBodyReaderAndWriter implements MessageBodyWriter<Object>, MessageBodyReader<Object> {

    public static AtomicInteger readFromCounter    = new AtomicInteger(0);

    public static AtomicInteger writeToCounter     = new AtomicInteger(0);

    public static AtomicInteger constructorCounter = new AtomicInteger(0);

    @Context
    Providers                   providers;

    public MyMessageBodyReaderAndWriter() {
        constructorCounter.incrementAndGet();
    }

    public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
        return ((String)arg0).getBytes().length;
    }

    public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {

        return String.class.equals(arg0);
    }

    public void writeTo(Object arg0,
                        Class<?> arg1,
                        Type arg2,
                        Annotation[] arg3,
                        MediaType arg4,
                        MultivaluedMap<String, Object> arg5,
                        OutputStream arg6) throws IOException, WebApplicationException {
        writeToCounter.incrementAndGet();
        arg6.write(((String)arg0).getBytes());
    }

    public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
        return String.class.equals(arg0);
    }

    public Object readFrom(Class<Object> arg0,
                           Type arg1,
                           Annotation[] arg2,
                           MediaType arg3,
                           MultivaluedMap<String, String> arg4,
                           InputStream arg5) throws IOException, WebApplicationException {
        readFromCounter.incrementAndGet();

        MessageBodyReader<String> strReader =
            providers.getMessageBodyReader(String.class,
                                           String.class,
                                           arg2,
                                           MediaType.APPLICATION_OCTET_STREAM_TYPE);
        return strReader.readFrom(String.class, String.class, arg2, arg3, arg4, arg5);
    }

}
