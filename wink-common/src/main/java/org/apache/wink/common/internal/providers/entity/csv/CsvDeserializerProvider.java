/*******************************************************************************
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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/
package org.apache.wink.common.internal.providers.entity.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.model.csv.CsvDeserializer;
import org.apache.wink.common.utils.ProviderUtils;

@Provider
@Consumes("text/csv")
public class CsvDeserializerProvider<T extends CsvDeserializer> implements MessageBodyReader<T> {

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return CsvDeserializer.class.isAssignableFrom(type);
    }

    public T readFrom(Class<T> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, String> httpHeaders,
                      InputStream entityStream) throws IOException, WebApplicationException {
        T descriptor;
        try {
            descriptor = type.getConstructor().newInstance();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
        return fillDiscriptor(descriptor, mediaType, entityStream);
    }

    public static <T extends CsvDeserializer> T fillDiscriptor(T descriptor,
                                                               MediaType mediaType,
                                                               InputStream entityStream) {
        Charset charset = Charset.forName(ProviderUtils.getCharset(mediaType));
        Reader reader = new InputStreamReader(entityStream, charset);
        CsvReader csvReader = new CsvReader(reader);
        String[] line;
        while ((line = csvReader.readLine()) != null) {
            descriptor.addEntity(line);
        }
        return descriptor;
    }

}
