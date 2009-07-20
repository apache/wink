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

package org.apache.wink.client;

import java.net.URI;
import java.util.Locale;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

/**
 * Represents a web resource, enabling the invocation of different http methods
 */
public interface Resource {

    /**
     * Add a request header to be used in every invocation
     * 
     * @param name name of the header to add. the name of the header is case
     *            insensitive.
     * @param values the values of the header. All of the values will be added
     *            to the same header, separated by a commas (,).
     * @return this resource instance
     */
    Resource header(String name, String... values);

    /**
     * Add values to the Accept header.
     * 
     * @param values accept header values to add
     * @return this resource instance
     */
    Resource accept(String... values);

    /**
     * Add values to the Accept header.
     * 
     * @param values accept header values to add
     * @return this resource instance
     */
    Resource accept(MediaType... values);

    /**
     * Add values to the Accept-Language header.
     * 
     * @param values accept-langage header values to add
     * @return this resource instance
     */
    Resource acceptLanguage(String... values);

    /**
     * Add values to the Accept-Language header.
     * 
     * @param values accept-langage header values to add
     * @return this resource instance
     */
    Resource acceptLanguage(Locale... values);

    /**
     * Add a Cookie value. Every call to this method will create a new Cookie
     * header.
     * 
     * @param value the cookie value to add
     * @return this resource instance
     */
    Resource cookie(String value);

    /**
     * Add a Cookie value. Every call to this method will create a new Cookie
     * header.
     * 
     * @param value the cookie value to add
     * @return this resource instance
     */
    Resource cookie(Cookie value);

    /**
     * Set the Content-Type header, overriding any previous value.
     * 
     * @param mediaType the content type to set
     * @return this resource instance
     */
    Resource contentType(String mediaType);

    /**
     * Set the Content-Type header, overriding any previous value.
     * 
     * @param mediaType the content type to set
     * @return this resource instance
     */
    Resource contentType(MediaType mediaType);

    /**
     * Add a query parameter to the uri
     * 
     * @param key the name of the query parameter
     * @param values values of the query parameters. A query parameter will be
     *            added for every value
     * @return this resource instance
     */
    Resource queryParam(String key, Object... values);

    /**
     * Add all the query parameters from the provided multivalued map.
     * 
     * @param params multivalued map of parameters.
     * @return this resource instance
     */
    Resource queryParams(MultivaluedMap<String, String> params);

    /**
     * Set an attribute on the resource. All the attributes are available to
     * handlers during a request via the {@link ClientRequest#getAttributes()}
     * and {@link ClientResponse#getAttributes()} methods.
     * 
     * @param key attribute key
     * @param value attribute value
     * @return this resource instance
     */
    Resource attribute(String key, Object value);

    /**
     * Get an attribute
     * 
     * @param key attribute key
     * @return attribute value, or null if the attribute is not set
     */
    Object attribute(String key);

    /**
     * Get the {@link UriBuilder} associated with this resource. All
     * modifications to the builder affect the uri of the resource
     * 
     * @return the {@link UriBuilder} associated with this resource
     */
    UriBuilder getUriBuilder();

    /**
     * Set the uri of this resource and create a new UriBuilder
     * 
     * @param uri the new uri for this resource
     * @return this resource instance
     */
    Resource uri(URI uri);

    /**
     * Set the uri of this resource and create a new UriBuilder
     * 
     * @param uri the new uri for this resource
     * @return this resource instance
     */
    Resource uri(String uri);

    /**
     * Invoke a request to the uri associated with the resource, and with any
     * headers and attributes set on the resource. If the response code
     * represents an error code, then a {@link ClientWebException} is thrown.
     * 
     * @param <T> the type of response entity to return
     * @param method the http request method
     * @param responseEntity the class of the response entity to return
     * @param requestEntity the request entity for methods that can send an
     *            entity (PUT, POST)
     * @return an instance of the response entity as specified by the response
     *         entity type
     * @throws ClientWebException if the response code represents an error code
     */
    <T> T invoke(String method, Class<T> responseEntity, Object requestEntity);

    /**
     * Invoke a request to the uri associated with the resource, and with any
     * headers and attributes set on the resource. If the response code
     * represents an error code, then a {@link ClientWebException} is thrown.
     * 
     * @param <T> the type of response entity to return
     * @param method the http request method
     * @param responseEntity an instance of {@link EntityType} specifying the
     *            response entity to return
     * @param requestEntity the request entity for methods that can send an
     *            entity (PUT, POST)
     * @return an instance of the response entity as specified by the response
     *         entity type
     * @throws ClientWebException if the response code represents an error code
     */
    <T> T invoke(String method, EntityType<T> responseEntity, Object requestEntity);

    /**
     * Invoke the HEAD method
     * 
     * @return the ClientResponse for the invocation
     */
    ClientResponse head();

    /**
     * Invoke the OPTIONS method
     * 
     * @return the ClientResponse for the invocation
     */
    ClientResponse options();

    /**
     * Invoke the GET method
     * 
     * @param <T> type of response entity
     * @param responseEntity response entity class
     * @return an instance of the response entity as specified by the response
     *         entity type
     * @throws ClientWebException if the response code represents an error code
     */
    <T> T get(Class<T> responseEntity);

    /**
     * Invoke the GET method
     * 
     * @param <T> type of response entity
     * @param responseEntity an instance of {@link EntityType} specifying the
     *            response entity to return
     * @return an instance of the response entity as specified by the response
     *         entity type
     * @throws ClientWebException if the response code represents an error code
     */
    <T> T get(EntityType<T> responseEntity);

    /**
     * Invoke the GET method
     * 
     * @return the ClientResponse for the invocation
     */
    ClientResponse get();

    /**
     * Invoke the POST method
     * 
     * @param <T> type of response entity
     * @param responseEntity response entity class
     * @param requestEntity request entity to send
     * @return an instance of the response entity as specified by the response
     *         entity type
     * @throws ClientWebException if the response code represents an error code
     */
    <T> T post(Class<T> responseEntity, Object requestEntity);

    /**
     * Invoke the POST method
     * 
     * @param <T> type of response entity
     * @param responseEntity an instance of {@link EntityType} specifying the
     *            response entity to return
     * @param requestEntity request entity to send
     * @return an instance of the response entity as specified by the response
     *         entity type
     * @throws ClientWebException if the response code represents an error code
     */
    <T> T post(EntityType<T> responseEntity, Object requestEntity);

    /**
     * Invoke the POST method
     * 
     * @return the ClientResponse for the invocation
     */
    ClientResponse post(Object requestEntity);

    /**
     * Invoke the PUT method
     * 
     * @param <T> type of response entity
     * @param responseEntity response entity class
     * @param requestEntity request entity to send
     * @return an instance of the response entity as specified by the response
     *         entity type
     * @throws ClientWebException if the response code represents an error code
     */
    <T> T put(Class<T> responseEntity, Object requestEntity);

    /**
     * Invoke the PUT method
     * 
     * @param <T> type of response entity
     * @param responseEntity an instance of {@link EntityType} specifying the
     *            response entity to return
     * @param requestEntity request entity to send
     * @return an instance of the response entity as specified by the response
     *         entity type
     * @throws ClientWebException if the response code represents an error code
     */
    <T> T put(EntityType<T> responseEntity, Object requestEntity);

    /**
     * Invoke the PUT method
     * 
     * @return the ClientResponse for the invocation
     */
    ClientResponse put(Object requestEntity);

    /**
     * Invoke the DELETE method
     * 
     * @param <T> type of response entity
     * @param responseEntity response entity class
     * @return an instance of the response entity as specified by the response
     *         entity type
     * @throws ClientWebException if the response code represents an error code
     */
    <T> T delete(Class<T> responseEntity);

    /**
     * Invoke the DELETE method
     * 
     * @param <T> type of response entity
     * @param responseEntity an instance of {@link EntityType} specifying the
     *            response entity to return
     * @return an instance of the response entity as specified by the response
     *         entity type
     * @throws ClientWebException if the response code represents an error code
     */
    <T> T delete(EntityType<T> responseEntity);

    /**
     * Invoke the DELETE method
     * 
     * @return the ClientResponse for the invocation
     */
    ClientResponse delete();
}
