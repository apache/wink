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

package org.apache.wink.client.internal;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.ClientRuntimeException;
import org.apache.wink.client.ClientWebException;
import org.apache.wink.client.EntityType;
import org.apache.wink.client.Resource;
import org.apache.wink.client.handlers.HandlerContext;
import org.apache.wink.client.internal.handlers.ClientRequestImpl;
import org.apache.wink.client.internal.handlers.HandlerContextImpl;
import org.apache.wink.common.http.HttpMethodEx;
import org.apache.wink.common.internal.CaseInsensitiveMultivaluedMap;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.utils.HeaderUtils;

public class ResourceImpl implements Resource {

    private static final String            USER_AGENT = "Wink Client v0.1";

    private ProvidersRegistry              providersRegistry;
    private ClientConfig                   config;

    private MultivaluedMap<String, String> headers;
    private Map<String, Object>            attributes;
    private UriBuilder                     uriBuilder;

    public ResourceImpl(URI uri, ClientConfig config, ProvidersRegistry providersRegistry) {
        this.config = config;
        this.providersRegistry = providersRegistry;
        this.uriBuilder = UriBuilder.fromUri(uri);
        this.headers = new CaseInsensitiveMultivaluedMap<String>();
        this.attributes = new HashMap<String, Object>();
    }

    public Resource accept(String... values) {
        String header = headers.getFirst(HttpHeaders.ACCEPT);
        headers.putSingle(HttpHeaders.ACCEPT, appendHeaderValues(header, values));
        return this;
    }

    public Resource accept(MediaType... values) {
        String header = headers.getFirst(HttpHeaders.ACCEPT);
        headers.putSingle(HttpHeaders.ACCEPT, appendHeaderValues(header, values));
        return this;
    }

    public Resource acceptLanguage(String... values) {
        String header = headers.getFirst(HttpHeaders.ACCEPT_LANGUAGE);
        headers.putSingle(HttpHeaders.ACCEPT_LANGUAGE, appendHeaderValues(header, values));
        return this;
    }

    public Resource acceptLanguage(Locale... values) {
        String[] types = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            types[i] = HeaderUtils.localeToLanguage(values[i]);
        }
        return this;
    }

    public Resource contentType(String mediaType) {
        headers.putSingle(HttpHeaders.CONTENT_TYPE, mediaType);
        return this;
    }

    public Resource contentType(MediaType mediaType) {
        return contentType(mediaType.toString());
    }

    public Resource cookie(String value) {
        headers.add(HttpHeaders.COOKIE, value);
        return this;
    }

    public Resource cookie(Cookie value) {
        return cookie(value.toString());
    }

    public Resource header(String name, String... values) {
        if (name == null) {
            return this;
        }
        for (String value : values) {
            if (value != null) {
                headers.add(name, value);
            }
        }
        return this;
    }

    public Resource queryParam(String key, Object... values) {
        uriBuilder.queryParam(key, values);
        return this;
    }

    public Resource queryParams(MultivaluedMap<String, String> params) {
        for (String query : params.keySet()) {
            queryParam(query, params.get(query).toArray());
        }
        return this;
    }

    public Resource attribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public Object attribute(String key) {
        return attributes.get(key);
    }

    public UriBuilder getUriBuilder() {
        return uriBuilder;
    }

    public Resource uri(URI uri) {
        uriBuilder = UriBuilder.fromUri(uri);
        return this;
    }

    public Resource uri(String uri) {
        uriBuilder = UriBuilder.fromUri(uri);
        return this;
    }

    private ClientResponse invokeNoException(String method, Object requestEntity) {
        try {
            return invoke(method, ClientResponse.class, requestEntity);
        } catch (ClientWebException e) {
            return e.getResponse();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T invoke(String method, Class<T> responseEntity, Object requestEntity) {
        ClientResponse response = invoke(method, responseEntity, responseEntity, requestEntity);
        if (responseEntity == null) {
            return null;
        }
        if (ClientResponse.class.equals(responseEntity)) {
            return (T)response;
        }
        return response.getEntity(responseEntity);
    }

    @SuppressWarnings("unchecked")
    public <T> T invoke(String method, EntityType<T> responseEntity, Object requestEntity) {
        ClientResponse response =
            invoke(method, responseEntity.getRawClass(), responseEntity.getType(), requestEntity);
        if (responseEntity == null) {
            return null;
        }
        if (ClientResponse.class.equals(responseEntity.getRawClass())) {
            return (T)response;
        }
        return response.getEntity(responseEntity);
    }

    private ClientResponse invoke(String method,
                                  Class<?> responseEntity,
                                  Type responseEntityType,
                                  Object requestEntity) {
        ClientRequest request =
            createClientRequest(method, responseEntity, responseEntityType, requestEntity);
        HandlerContext context = createHandlerContext();
        try {
            ClientResponse response = context.doChain(request);
            if (ClientUtils.isErrorCode(response.getStatusCode())) {
                throw new ClientWebException(request, response);
            }
            return response;
        } catch (ClientWebException e) {
            throw e;
        } catch (ClientRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }
    }

    private <T> ClientRequest createClientRequest(String method,
                                                  Class<T> responseEntity,
                                                  Type responseEntityType,
                                                  Object requestEntity) {
        ClientRequest request = new ClientRequestImpl();
        request.setEntity(requestEntity);
        request.setURI(uriBuilder.build());
        request.setMethod(method);
        request.getHeaders().putAll(headers);
        if (headers.getFirst(HttpHeaders.ACCEPT) == null) {
            request.getHeaders().add(HttpHeaders.ACCEPT, MediaType.WILDCARD);
        }
        if (headers.getFirst(HttpHeaders.USER_AGENT) == null) {
            request.getHeaders().add(HttpHeaders.USER_AGENT, USER_AGENT);
        }

        request.getAttributes().putAll(attributes);
        request.setAttribute(ProvidersRegistry.class, providersRegistry);
        request.setAttribute(ClientConfig.class, config);
        request.getAttributes().put(ClientRequestImpl.RESPONSE_ENTITY_GENERIC_TYPE,
                                    responseEntityType);
        request.getAttributes().put(ClientRequestImpl.RESPONSE_ENTITY_CLASS_TYPE, responseEntity);
        return request;
    }

    private HandlerContext createHandlerContext() {
        HandlerContext context = new HandlerContextImpl(config.getHandlers());
        return context;
    }

    public ClientResponse head() {
        return invokeNoException(HttpMethod.HEAD, null);
    }

    public ClientResponse options() {
        return invokeNoException(HttpMethodEx.OPTIONS, null);
    }

    public <T> T delete(Class<T> responseEntity) {
        return invoke(HttpMethod.DELETE, responseEntity, null);
    }

    public <T> T delete(EntityType<T> responseEntity) {
        return invoke(HttpMethod.DELETE, responseEntity, null);
    }

    public ClientResponse delete() {
        return invokeNoException(HttpMethod.DELETE, null);
    }

    public <T> T get(Class<T> responseEntity) {
        return invoke(HttpMethod.GET, responseEntity, null);
    }

    public <T> T get(EntityType<T> responseEntity) {
        return invoke(HttpMethod.DELETE, responseEntity, null);
    }

    public ClientResponse get() {
        return invokeNoException(HttpMethod.GET, null);
    }

    public <T> T post(Class<T> responseEntity, Object requestEntity) {
        return invoke(HttpMethod.POST, responseEntity, requestEntity);
    }

    public <T> T post(EntityType<T> responseEntity, Object requestEntity) {
        return invoke(HttpMethod.POST, responseEntity, requestEntity);
    }

    public ClientResponse post(Object requestEntity) {
        return invokeNoException(HttpMethod.POST, requestEntity);
    }

    public <T> T put(Class<T> responseEntity, Object requestEntity) {
        return invoke(HttpMethod.PUT, responseEntity, requestEntity);
    }

    public <T> T put(EntityType<T> responseEntity, Object requestEntity) {
        return invoke(HttpMethod.POST, responseEntity, requestEntity);
    }

    public ClientResponse put(Object requestEntity) {
        return invokeNoException(HttpMethod.PUT, requestEntity);
    }

    private <T> String toHeaderString(T[] objects) {
        String delim = "";
        StringBuilder sb = new StringBuilder();
        for (T t : objects) {
            sb.append(delim);
            sb.append(t.toString());
            delim = ", ";
        }
        return sb.toString();
    }

    private <T> String appendHeaderValues(String value, T[] objects) {
        StringBuilder builder = new StringBuilder(value != null ? value : "");
        builder.append(value != null ? ", " : "");
        builder.append(toHeaderString(objects));
        return builder.toString();
    }

}
