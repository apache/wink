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
package org.apache.wink.common.internal.runtime;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.wink.common.internal.ResponseImpl;
import org.apache.wink.common.internal.UriBuilderImpl;
import org.apache.wink.common.internal.VariantListBuilderImpl;
import org.apache.wink.common.internal.http.Accept;
import org.apache.wink.common.internal.http.AcceptLanguage;
import org.apache.wink.common.internal.http.ContentDispositionHeader;
import org.apache.wink.common.internal.http.EntityTagMatchHeader;
import org.apache.wink.common.internal.providers.header.AcceptHeaderDelegate;
import org.apache.wink.common.internal.providers.header.AcceptLanguageHeaderDelegate;
import org.apache.wink.common.internal.providers.header.CacheControlHeaderDelegate;
import org.apache.wink.common.internal.providers.header.ContentDispositionHeaderDelegate;
import org.apache.wink.common.internal.providers.header.CookieHeaderDelegate;
import org.apache.wink.common.internal.providers.header.DateHeaderDelegate;
import org.apache.wink.common.internal.providers.header.EntityTagHeaderDelegate;
import org.apache.wink.common.internal.providers.header.EntityTagMatchHeaderDelegate;
import org.apache.wink.common.internal.providers.header.MediaTypeHeaderDelegate;
import org.apache.wink.common.internal.providers.header.NewCookieHeaderDelegate;

public class RuntimeDelegateImpl extends RuntimeDelegate {

    private Map<Class<?>, HeaderDelegate<?>> headerDelegates =
                                                                 new HashMap<Class<?>, HeaderDelegate<?>>();

    public RuntimeDelegateImpl() {
        headerDelegates.put(MediaType.class, new MediaTypeHeaderDelegate());
        headerDelegates.put(NewCookie.class, new NewCookieHeaderDelegate());
        headerDelegates.put(Date.class, new DateHeaderDelegate());
        headerDelegates.put(CacheControl.class, new CacheControlHeaderDelegate());
        headerDelegates.put(Cookie.class, new CookieHeaderDelegate());
        headerDelegates.put(EntityTag.class, new EntityTagHeaderDelegate());
        headerDelegates.put(EntityTagMatchHeader.class, new EntityTagMatchHeaderDelegate());
        headerDelegates.put(Accept.class, new AcceptHeaderDelegate());
        headerDelegates.put(AcceptLanguage.class, new AcceptLanguageHeaderDelegate());
        headerDelegates.put(ContentDispositionHeader.class, new ContentDispositionHeaderDelegate());
    }

    @Override
    public <T> T createEndpoint(Application application, Class<T> endpointType)
        throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }
        return (HeaderDelegate<T>)headerDelegates.get(type);
    }

    @Override
    public ResponseBuilder createResponseBuilder() {
        return new ResponseImpl.ResponseBuilderImpl();
    }

    @Override
    public UriBuilder createUriBuilder() {
        return new UriBuilderImpl();
    }

    @Override
    public VariantListBuilder createVariantListBuilder() {
        return new VariantListBuilderImpl();
    }

}
