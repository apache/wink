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

package org.apache.wink.server.utils;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.synd.SyndLink;

/**
 * Base interface for the the two types of link builders.
 * 
 * @param <T> the derived type of the links builder
 * @see SingleLinkBuilder
 * @see SystemLinksBuilder
 */
public interface BaseLinksBuilder<T> {

    /**
     * Set the resource class that the generated links should link to. If no
     * resource is set then the active resource is used. Setting the resource
     * clears any sub-resource path previously set.
     * 
     * @param resource a resource class annotated with the
     *            {@link javax.ws.rs.Path} annotation
     * @return this links builder
     */
    public T resource(Class<?> resource);

    /**
     * Set the resource instance that the generated links should link to. If no
     * resource is set then the active resource is used. Setting the resource
     * clears any sub-resource path previously set.
     * 
     * @param resource a resource instance, or null to use the current resource
     * @return this links builder
     */
    public T resource(Object resource);

    /**
     * Set the sub-resource path that the generated links should link to,
     * relative to the resource set in the builder.
     * 
     * @param template the template path of the sub-resource
     * @return this links builder
     */
    public T subResource(String template);

    /**
     * Set the base uri for this builder. If not set, or if set to
     * <code>null</code>, then the base uri as obtained from
     * {@link UriInfo#getBaseUri()} is used.
     * 
     * @param base the base uri for this builder, or null
     * @return this links builder
     */
    public T baseUri(URI base);

    /**
     * Set the uri that the generated links will be relative to, if the
     * generation mode is relative uri's. The uri being set must be absolute and
     * must be prefixed with the base uri set on this links builder. If not set,
     * or if set to <code>null</code>, then the generated links will be relative
     * to the path obtained from {@link UriInfo#getPath()}.
     * 
     * @param relativeTo the relative uri for this builder, or null
     * @return this links builder
     */
    public T relativeTo(URI relativeTo);

    /**
     * Set the value of a template path parameter to be used during the building
     * of the links.
     * 
     * @param name the name of the path parameter
     * @param value the value of the path parameter
     * @return this links builder
     */
    public T pathParam(String name, String value);

    /**
     * Set the value of a query parameter to be added to the generated links
     * 
     * @param name the name of the query parameter to add to the generated links
     * @param value the value of the query parameter
     * @return this links builder
     */
    public T queryParam(String name, String value);

    /**
     * Set whether the links should be generated absolute or relative,
     * overriding any configuration and request settings. If this method is not
     * called, the behavior is taken from the value of the "relative-urls" query
     * parameter on the request. If the request does not have the query
     * parameter, the behavior is taken from the
     * <code>wink.defaultUrisRelative</code> configuration property.
     * 
     * @param relativize <code>true</code> to produce relative links,
     *            <code>false</code> to produce absolute links
     * @return this links builder
     */
    public T relativize(boolean relativize);

    /**
     * Set whether the to automatically add to the generated links the "alt"
     * query parameter indicating the link type. The "alt" query parameter will
     * never be added automatically in case if it was added manually. If this
     * method is not called, the default behavior is taken from the
     * <code>wink.addAltParam</code> configuration property.
     * 
     * @param addAltParam <code>true</code> to add the "alt" parameter
     * @return
     */
    public T addAltParam(boolean addAltParam);

    /**
     * Build the link(s) and add them to the provided list. The state of the
     * builder is not affected by this method.
     * 
     * @param out the output list of links to add the generated link(s) to. If
     *            the provided list is <code>null</code> then a new list is
     *            created.
     * @return the provided (or new) output list.
     */
    public List<SyndLink> build(List<SyndLink> out);

}
