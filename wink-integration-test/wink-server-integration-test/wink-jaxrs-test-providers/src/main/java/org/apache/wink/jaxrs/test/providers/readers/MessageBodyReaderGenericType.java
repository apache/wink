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

package org.apache.wink.jaxrs.test.providers.readers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

@Provider
@Consumes("custom/generic")
public class MessageBodyReaderGenericType implements MessageBodyReader<Object> {

    @Context
    Providers providers;

    public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
        if (List.class.isAssignableFrom(arg0)) {
            if (arg1 instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) arg1;
                Type rawType = pt.getRawType();
                if ((rawType instanceof Class)
                        && (List.class.isAssignableFrom((Class) rawType))) {
                    Type[] genericTypeArguments = pt.getActualTypeArguments();
                    if (genericTypeArguments.length == 1) {
                        Class argType = (Class) genericTypeArguments[0];
                        if (Integer.class.isAssignableFrom(argType)
                                || String.class.isAssignableFrom(argType)) {
                            return true;
                        }
                    }
                }
            } else if (arg1 instanceof Class) {
                return true;
            }
        }

        if (arg1 instanceof Class) {
            if (Integer.class.isAssignableFrom((Class) arg1)) {
                return true;
            }
        }
        return false;
    }

    public Object readFrom(Class<Object> arg0, Type arg1, Annotation[] arg2, MediaType arg3, MultivaluedMap<String, String> arg4, InputStream arg5)
            throws IOException, WebApplicationException {
        if (List.class.isAssignableFrom(arg0)) {
            if (arg1 instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) arg1;
                Type rawType = pt.getRawType();
                if ((rawType instanceof Class)
                        && (List.class.isAssignableFrom((Class) rawType))) {
                    Type[] genericTypeArguments = pt.getActualTypeArguments();
                    if (genericTypeArguments.length == 1) {
                        Class argType = (Class) genericTypeArguments[0];
                        if (Integer.class.isAssignableFrom(argType)) {
                            MessageBodyReader<String> strReader =
                                providers.getMessageBodyReader(String.class,
                                                               String.class,
                                                               arg2,
                                                               MediaType.TEXT_PLAIN_TYPE);
                            String str = strReader.readFrom(String.class, String.class, arg2, arg3, arg4, arg5);
                            String[] splitlines = str.split("\r\n");
                            List<Integer> ret = new ArrayList<Integer>();
                            for (String s : splitlines) {
                                ret.add(Integer.valueOf(s));
                            }
                            return ret;
                        } else if (String.class.isAssignableFrom(argType)) {
                            MessageBodyReader<String> strReader =
                                providers.getMessageBodyReader(String.class,
                                                               String.class,
                                                               arg2,
                                                               MediaType.TEXT_PLAIN_TYPE);
                            String str = strReader.readFrom(String.class, String.class, arg2, arg3, arg4, arg5);
                            String[] splitlines = str.split("\r\n");
                            List<String> ret = new ArrayList<String>();
                            for (String s : splitlines) {
                                ret.add("str:" + s);
                            }
                            return ret;
                        }
                    }
                }
            } else if (arg1 instanceof Class) {
                MessageBodyReader<String> strReader =
                    providers.getMessageBodyReader(String.class,
                                                   String.class,
                                                   arg2,
                                                   MediaType.TEXT_PLAIN_TYPE);
                String str = strReader.readFrom(String.class, String.class, arg2, arg3, arg4, arg5);
                String[] splitlines = str.split("\r\n");
                List<Object> ret = new ArrayList<Object>();
                for (String s : splitlines) {
                    ret.add("obj:" + s);
                }
                return ret;
            }
        }

        if (arg1 instanceof Class) {
            if (Integer.class.isAssignableFrom((Class) arg1)) {
                MessageBodyReader<String> strReader =
                    providers.getMessageBodyReader(String.class,
                                                   String.class,
                                                   arg2,
                                                   MediaType.TEXT_PLAIN_TYPE);
                String str = strReader.readFrom(String.class, String.class, arg2, arg3, arg4, arg5);
                String[] splitlines = str.split("\r\n");
                int sum = 0;
                for (String s : splitlines) {
                    sum += Integer.valueOf(s).intValue();
                }
                return Integer.valueOf(sum);
            }
        }
        return null;
    }

}
