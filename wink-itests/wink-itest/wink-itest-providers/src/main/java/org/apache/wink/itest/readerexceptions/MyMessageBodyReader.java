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

package org.apache.wink.itest.readerexceptions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

@Provider
public class MyMessageBodyReader implements MessageBodyReader<Object> {

    public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
        if (arg3.isCompatible(new MediaType("readable", "throwruntime"))) {
            throw new RuntimeException();
        } else if (arg3.isCompatible(new MediaType("readable", "thrownull"))) {
            throw new NullPointerException();
        } else if (arg3.isCompatible(new MediaType("readable", "throwwebapplicationexception"))) {
            throw new WebApplicationException(Response.status(499).entity("can not read type")
                .build());
        }

        return arg0.equals(String.class);
    }

    public Object readFrom(Class<Object> arg0,
                           Type arg1,
                           Annotation[] arg2,
                           MediaType arg3,
                           MultivaluedMap<String, String> arg4,
                           InputStream arg5) throws IOException, WebApplicationException {
        if (arg3.isCompatible(new MediaType("readfrom", "thrownull"))) {
            throw new NullPointerException();
        } else if (arg3.isCompatible(new MediaType("readfrom", "throwwebapplicationexception"))) {
            throw new WebApplicationException(Response.status(498)
                .entity("can not read type in readfrom").build());
        } else if (arg3.isCompatible(new MediaType("readfrom", "throwioexception"))) {
            throw new IOException();
        } else if (arg3.isCompatible(new MediaType("readfrom", "throwruntime"))) {
            throw new RuntimeException();
        }
        return "read";
    }

}
