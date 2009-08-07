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

package org.apache.wink.itest.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
public class MessageBodyWriterContentLength implements MessageBodyWriter<Object> {

    public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {

        if (arg1.equals(Vector.class) && arg2.equals(Vector.class)) {
            return 17;
        }

        if (arg1.equals(ArrayList.class)) {
            if (arg2 instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)arg2;
                if (pt.getRawType().equals(List.class)) {
                    if (pt.getActualTypeArguments().length == 1 && pt.getActualTypeArguments()[0]
                        .equals(Integer.class)) {
                        return 14;
                        // return true;
                    }
                }
            }

            if (arg1.equals(ArrayList.class)) {
                // return true;
            }
        }

        if (arg1.equals(String.class)) {
            boolean isGetFound = false;
            for (Annotation ann : arg3) {
                if (ann.annotationType().equals(CustomAnnotation.class)) {
                    isGetFound = true;
                }
            }

            if (isGetFound) {
                return 18;
            }
        }
        return -1;
    }

    public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
        if (arg0.equals(Vector.class)) {
            return true;
        }

        if (arg0.equals(ArrayList.class)) {
            if (arg1 instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)arg1;
                if (pt.getRawType().equals(List.class)) {
                    if (pt.getActualTypeArguments().length == 1 && pt.getActualTypeArguments()[0]
                        .equals(Integer.class)) {
                        return true;
                    }
                }
            }

            if (arg1.equals(ArrayList.class)) {
                return true;
            }
        }

        if (arg1.equals(String.class)) {
            if (arg2 != null) {
                for (Annotation ann : arg2) {
                    if (ann.annotationType().equals(CustomAnnotation.class)) {
                        return true;
                    }
                }
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
        if (arg1.equals(Vector.class)) {
            Writer writer = new OutputStreamWriter(arg6);
            writer.write("vector:");
            Vector vec = (Vector)arg0;
            for (Object o : vec) {
                writer.write(o.toString());
            }
            writer.flush();
        } else if (arg1.equals(ArrayList.class)) {
            if (arg2 instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)arg2;
                if (pt.getRawType().equals(List.class)) {
                    if (pt.getActualTypeArguments().length == 1 && pt.getActualTypeArguments()[0]
                        .equals(Integer.class)) {
                        Writer writer = new OutputStreamWriter(arg6);
                        writer.write("listinteger:");
                        List list = (List)arg0;
                        for (Object o : list) {
                            writer.write(o.toString());
                        }
                        writer.flush();
                    }
                }
            }
        } else if (arg1.equals(String.class)) {
            boolean isCustomAnnotationFound = false;
            for (Annotation ann : arg3) {
                if (ann.annotationType().equals(CustomAnnotation.class)) {
                    isCustomAnnotationFound = true;
                }
            }

            if (isCustomAnnotationFound) {
                arg6.write("string:".getBytes());
                arg6.write(((String)arg0).getBytes());
            }
        } else {
            Writer writer = new OutputStreamWriter(arg6);
            writer.write("getannotation:");
            List list = (List)arg0;
            for (Object o : list) {
                writer.write(o.toString());
            }
            writer.flush();
        }
    }

}
