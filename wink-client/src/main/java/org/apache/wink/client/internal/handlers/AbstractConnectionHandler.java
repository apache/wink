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

package org.apache.wink.client.internal.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;

import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.handlers.ConnectionHandler;
import org.apache.wink.client.handlers.InputStreamAdapter;
import org.apache.wink.client.handlers.OutputStreamAdapter;
import org.apache.wink.client.internal.ClientRuntimeContext;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.runtime.RuntimeContext;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;

public abstract class AbstractConnectionHandler implements ConnectionHandler {

    protected OutputStream adaptOutputStream(OutputStream os,
                                             ClientRequest request,
                                             List<OutputStreamAdapter> outputStreamAdapters)
        throws IOException {
        for (OutputStreamAdapter adapter : outputStreamAdapters) {
            os = adapter.adapt(os, request);
        }
        return os;
    }

    protected InputStream adaptInputStream(InputStream is,
                                           ClientResponse response,
                                           List<InputStreamAdapter> inputStreamAdapters)
        throws IOException {
        for (InputStreamAdapter adapter : inputStreamAdapters) {
            is = adapter.adapt(is, response);
        }
        return is;
    }

    @SuppressWarnings("unchecked")
    protected void writeEntity(ClientRequest request, OutputStream os)
        throws WebApplicationException, IOException {
        Object entity = request.getEntity();
        if (entity == null) {
            return;
        }

        ProvidersRegistry providersRegistry = request.getAttribute(ProvidersRegistry.class);
        ClientRuntimeContext runtimeContext = new ClientRuntimeContext(providersRegistry);
        RuntimeContext saved = RuntimeContextTLS.getRuntimeContext();
        RuntimeContextTLS.setRuntimeContext(runtimeContext);
        try {
            Class<?> type = entity.getClass();
            Type genericType = type;
            if (entity instanceof GenericEntity) {
                GenericEntity<?> genericEntity = (GenericEntity<?>)entity;
                type = genericEntity.getRawType();
                genericType = genericEntity.getType();
                entity = genericEntity.getEntity();
            }
            String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            MediaType contentMediaType = MediaType.valueOf(contentType);
            MessageBodyWriter writer =
                providersRegistry.getMessageBodyWriter(type,
                                                       genericType,
                                                       null,
                                                       contentMediaType,
                                                       runtimeContext);
            if (writer == null) {
                throw new RuntimeException(String.format("No writer for type %s and media type %s",
                                                         String.valueOf(type),
                                                         contentType));
            }
            writer.writeTo(entity,
                           type,
                           genericType,
                           null,
                           contentMediaType,
                           request.getHeaders(),
                           os);
            os.flush();
            os.close();
        } finally {
            RuntimeContextTLS.setRuntimeContext(saved);
        }
    }

}
