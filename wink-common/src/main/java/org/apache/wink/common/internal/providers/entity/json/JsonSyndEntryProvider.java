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

package org.apache.wink.common.internal.providers.entity.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.synd.SyndEntry;

@Provider
@Produces( {MediaType.APPLICATION_JSON, MediaTypeUtils.JAVASCRIPT})
public class JsonSyndEntryProvider extends JsonJAXBProvider {

    @Override
    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        if (type != SyndEntry.class) {
            return false;
        }
        return super.isWriteable(AtomEntry.class, AtomEntry.class, annotations, mediaType);
    }

    @Override
    public void writeTo(Object t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        AtomEntry entry = new AtomEntry((SyndEntry)t);
        super.writeTo(entry,
                      AtomEntry.class,
                      AtomEntry.class,
                      annotations,
                      mediaType,
                      httpHeaders,
                      entityStream);
    }

}
