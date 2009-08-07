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
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
public class MessageBodyWriterIsWritableMediaTypeHashMap implements MessageBodyWriter<Object> {

    public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
        return -1;
    }

    public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
        if (arg3.equals(new MediaType("custom", "correct"))) {
            return true;
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
        writer.write("mediatype:");
        Map map = (Map)arg0;
        for (Object k : map.keySet()) {
            writer.write(k.toString() + "=" + map.get(k));
        }
        writer.flush();
    }

}
