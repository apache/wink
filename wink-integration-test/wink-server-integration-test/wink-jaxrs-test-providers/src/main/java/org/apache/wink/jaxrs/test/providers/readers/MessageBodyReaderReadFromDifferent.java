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
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

@Provider
@Consumes( {"custom/long", "custom/int", "custom/byte", "custom/short"})
public class MessageBodyReaderReadFromDifferent implements MessageBodyReader<Number> {

    public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
        if (Integer.class.isAssignableFrom(arg0) || Long.class.isAssignableFrom(arg0)
            || Byte.class.isAssignableFrom(arg0)
            || Short.class.isAssignableFrom(arg0)) {
            return true;
        }
        return false;
    }

    public Number readFrom(Class<Number> arg0,
                           Type arg1,
                           Annotation[] arg2,
                           MediaType arg3,
                           MultivaluedMap<String, String> arg4,
                           InputStream arg5) throws IOException, WebApplicationException {
        if (arg0.isAssignableFrom(Long.class)) {
            return Long.valueOf(Long.MAX_VALUE);
        }
        if (arg0.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(Integer.MAX_VALUE);
        }

        for (Annotation ann : arg2) {
            if (CustomAnnotation.class.equals(ann.annotationType())) {
                return Short.valueOf(Short.MAX_VALUE);
            }
        }

        if (arg3.isCompatible(MediaType.valueOf("custom/byte"))) {
            return Byte.valueOf(Byte.MAX_VALUE);
        }

        return null;
    }

}
