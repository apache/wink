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

package org.apache.wink.itest.providers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

@Path("context/providers")
public class MyResource {

    @Context
    Providers provider;

    @GET
    @Path("messagebodyreader")
    public String getMessageBodyReader(@QueryParam("className") String className,
                                       @QueryParam("mediaType") String mediaType) {
        Class<?> c = null;
        try {
            c = (className == null) ? null : Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        MediaType mt = (mediaType == null) ? null : MediaType.valueOf(mediaType);

        MessageBodyReader<?> reader = provider.getMessageBodyReader(c, null, null, mt);
        if (reader == null) {
            return "nullreader";
        }
        return reader.getClass().getName();
    }

    @GET
    @Path("messagebodywriter")
    public String getMessageBodyWriter(@QueryParam("className") String className,
                                       @QueryParam("mediaType") String mediaType) {
        Class<?> c = null;
        try {
            c = (className == null) ? null : Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        MediaType mt = (mediaType == null) ? null : MediaType.valueOf(mediaType);

        MessageBodyWriter<?> writer = provider.getMessageBodyWriter(c, null, null, mt);
        if (writer == null) {
            return "nullwriter";
        }
        return writer.getClass().getName();
    }

    @GET
    @Path("contextresolver")
    public String getContextResolver(@QueryParam("className") String className,
                                     @QueryParam("mediaType") String mediaType,
                                     @QueryParam("invokeWithClassName") String classNameToInvokeWith,
                                     @QueryParam("returnToStringValue") boolean shouldReturnToStringValue) {

        Class<?> c = null;
        try {
            c = (className == null) ? null : Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        MediaType mt = (mediaType == null) ? new MediaType("*", "*") : MediaType.valueOf(mediaType);
        ContextResolver<?> contextResolver = provider.getContextResolver(c, mt);
        if (contextResolver == null) {
            return "nullcontextresolver";
        }
        if (classNameToInvokeWith == null) {
            return contextResolver.getClass().getName();
        }
        c = null;
        try {
            c = (className == null) ? null : Class.forName(classNameToInvokeWith);
        } catch (ClassNotFoundException e) {
            // TODO: also throw? Return some nonsense string to make testcase
            // failure more obvious?
            e.printStackTrace();
        }
        Object o = contextResolver.getContext(c);
        if (o == null) {
            return "null";
        }
        if (shouldReturnToStringValue) {
            return (String)o;
        }
        return o.getClass().getName();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("exception")
    public String getException(@QueryParam("className") String className) {
        Class<? extends Throwable> c = null;
        try {
            c = (className == null) ? null : (Class<? extends Throwable>)Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        ExceptionMapper<?> em = provider.getExceptionMapper(c);
        if (em == null) {
            return "null";
        }
        return em.getClass().getName();
    }
}
