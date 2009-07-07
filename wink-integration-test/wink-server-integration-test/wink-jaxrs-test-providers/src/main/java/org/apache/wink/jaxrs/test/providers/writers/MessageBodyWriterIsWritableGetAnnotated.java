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

package org.apache.wink.jaxrs.test.providers.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
public class MessageBodyWriterIsWritableGetAnnotated implements MessageBodyWriter<Object> {

    public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
        return -1;
    }

    public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
        boolean isGetAnnotated = false;
        boolean isMyWriterAnnotated = false;
        if (arg2 != null) {
            for (Annotation ann : arg2) {
                if (MyWriterAnnotation.class.equals(ann.annotationType())) {
                    isMyWriterAnnotated = true;
                } else if (GET.class.equals(ann.annotationType())) {
                    isGetAnnotated = true;
                }
            }
            if (isMyWriterAnnotated && isGetAnnotated) {
                return true;
            }
        }
        return false;
    }

    public void writeTo(Object arg0,
                        Class<?> arg1,
                        Type arg2,
                        Annotation[] arg3,
                        MediaType arg4,
                        MultivaluedMap<String, Object> arg5,
                        OutputStream arg6) throws IOException, WebApplicationException {
        Writer writer = new OutputStreamWriter(arg6);
        writer.write("getannotation:");
        List list = (List)arg0;
        for (Object o : list) {
            writer.write(o.toString());
        }
        writer.flush();
    }
}
