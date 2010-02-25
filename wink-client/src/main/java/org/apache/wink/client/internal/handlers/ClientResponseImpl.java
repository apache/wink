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
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.ClientRuntimeException;
import org.apache.wink.client.EntityType;
import org.apache.wink.client.internal.ClientRuntimeContext;
import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;

public class ClientResponseImpl extends BaseRequestResponseImpl implements ClientResponse {

    private Object   entity;
    private String   message;
    private int      status;
    private Runnable contentConsumer;

    public <T> T getEntity(Class<T> type) {
        return getEntity(type, type);
    }

    public <T> T getEntity(EntityType<T> entityType) {
        return getEntity(entityType.getRawClass(), entityType.getType());
    }

    @SuppressWarnings("unchecked")
    private <T> T getEntity(Class<T> type, Type genericType) {
        if (type.isInstance(entity)) {
            return (T)entity;
        }
        if (entity instanceof InputStream) {
            T t = readEntity(type, genericType, (InputStream)entity);
            setEntity(t);
            return t;
        }
        if (entity == null) {
            return null;
        }
        throw new ClassCastException(String
            .format(Messages.getMessage("clientCannotConvertEntity"),
                    entity.getClass().getName(),
                    type.getName()));
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return status;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusCode(int code) {
        this.status = code;
    }

    @SuppressWarnings("unchecked")
    private <T> T readEntity(Class<T> type, Type genericType, InputStream is) {
        if (type == null) {
            return null;
        }
        if (ClientResponse.class.equals(type)) {
            return (T)this;
        }
        ProvidersRegistry providersRegistry = getAttribute(ProvidersRegistry.class);
        RuntimeContext saved = RuntimeContextTLS.getRuntimeContext();
        ClientRuntimeContext runtimeContext = new ClientRuntimeContext(providersRegistry);
        RuntimeContextTLS.setRuntimeContext(runtimeContext);
        try {
            String contentType = getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentType == null || contentType.length() == 0) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }
            MediaType contentMediaType = MediaType.valueOf(contentType);
            MessageBodyReader<T> reader =
                providersRegistry.getMessageBodyReader(type,
                                                       genericType,
                                                       null,
                                                       contentMediaType,
                                                       runtimeContext);
            if (reader == null) {
                throw new RuntimeException(String.format(Messages.getMessage("clientNoReaderForTypeAndMediaType"),
                                                         String.valueOf(type),
                                                         contentType));
            }
            T entity = reader.readFrom(type, genericType, null, contentMediaType, getHeaders(), is);
            return entity;
        } catch (WebApplicationException e) {
            throw new ClientRuntimeException(e);
        } catch (IOException e) {
            throw new ClientRuntimeException(e);
        } finally {
            RuntimeContextTLS.setRuntimeContext(saved);
            consumeContent();
        }
    }

    public void consumeContent() {
        if (contentConsumer != null) {
            contentConsumer.run();
        }
    }

    public void setContentConsumer(Runnable contentConsumer) {
        this.contentConsumer = contentConsumer;
    }

    public Runnable getContentConsumer() {
        return contentConsumer;
    }

}
