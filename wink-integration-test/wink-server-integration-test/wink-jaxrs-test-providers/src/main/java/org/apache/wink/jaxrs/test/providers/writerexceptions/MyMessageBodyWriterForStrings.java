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

package org.apache.wink.jaxrs.test.providers.writerexceptions;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(value = {"getsize/*", "writable/*", "writeto/*", "writetoafterwritten/*"})
public class MyMessageBodyWriterForStrings implements MessageBodyWriter<String> {

    public long getSize(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
        if (arg4.isCompatible(new MediaType("getsize", "throwruntime"))) {
            throw new RuntimeException();
        } else if (arg4.isCompatible(new MediaType("getsize", "thrownull"))) {
            throw new NullPointerException();
        } else if (arg4.isCompatible(new MediaType("getsize", "throwwebapplicationexception"))) {
            throw new WebApplicationException(Response.status(499).entity("can not write type")
                .type("text/plain").build());
        }

        return -1;
    }

    public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
        if (arg3.isCompatible(new MediaType("writable", "throwruntime"))) {
            throw new RuntimeException();
        } else if (arg3.isCompatible(new MediaType("writable", "thrownull"))) {
            throw new NullPointerException();
        } else if (arg3.isCompatible(new MediaType("writable", "throwwebapplicationexception"))) {
            throw new WebApplicationException(Response.status(499).entity("can not write type")
                .type("text/plain").build());
        }

        if (arg3.getType().equals("writeto") || arg3.getType().equals("getsize")
            || arg3.getType().equals("writetoafterwritten")) {
            return true;
        }

        return false;
    }

    public void writeTo(String arg0,
                        Class<?> arg1,
                        Type arg2,
                        Annotation[] arg3,
                        MediaType arg4,
                        MultivaluedMap<String, Object> arg5,
                        OutputStream arg6) throws IOException, WebApplicationException {
        if (arg4.isCompatible(new MediaType("writeto", "thrownull"))) {
            throw new NullPointerException();
        } else if (arg4.isCompatible(new MediaType("writeto", "throwwebapplicationexception"))) {
            throw new WebApplicationException(Response.status(498)
                .entity("can not write type in writeto").type("text/plain").build());
        } else if (arg4.isCompatible(new MediaType("writeto", "throwioexception"))) {
            throw new IOException();
        } else if (arg4.isCompatible(new MediaType("writeto", "throwruntime"))) {
            throw new RuntimeException();
        }

        arg6.write("written".getBytes());
        arg6.flush();
        arg6.flush();
        arg6.flush();

        if (arg4.isCompatible(new MediaType("writetoafterwritten", "thrownull"))) {
            throw new NullPointerException();
        } else if (arg4.isCompatible(new MediaType("writetoafterwritten",
                                                   "throwwebapplicationexception"))) {
            throw new WebApplicationException(Response.status(498)
                .entity("can not write type in writeto").type("text/plain").build());
        } else if (arg4.isCompatible(new MediaType("writetoafterwritten", "throwioexception"))) {
            throw new IOException();
        } else if (arg4.isCompatible(new MediaType("writetoafterwritten", "throwruntime"))) {
            throw new RuntimeException();
        }

        arg6.write("writtensomemore".getBytes());
        arg6.flush();

    }

}
