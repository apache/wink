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
package org.apache.wink.common.internal.providers;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.RestException;
import org.apache.wink.common.annotations.Scope;
import org.apache.wink.common.annotations.Scope.ScopeType;
import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.registry.ProvidersRegistry;

import junit.framework.TestCase;

public class ProvidersErrorMapperTest extends TestCase {

    @Provider
    @Scope(ScopeType.PROTOTYPE)
    public static class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {

        public Response toResponse(Throwable exception) {
            return null;
        }

    }

    @Provider
    public static class BaseExceptionMapper implements ExceptionMapper<Exception> {

        public Response toResponse(Exception exception) {
            return null;
        }

    }

    @Provider
    public static class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

        public Response toResponse(RuntimeException exception) {
            return null;
        }
    }

    @Provider
    public static class RestExceptionMapper implements ExceptionMapper<RestException> {

        public Response toResponse(RestException exception) {
            return null;
        }
    }

    @Provider
    public static class RestExceptionMapper2 implements ExceptionMapper<RestException> {

        public Response toResponse(RestException exception) {
            return null;
        }
    }

    private ProvidersRegistry createProvidersRegistryImpl() {
        ProvidersRegistry providers =
            new ProvidersRegistry(new LifecycleManagersRegistry(), new ApplicationValidator());

        return providers;
    }

    public void testErrorMappers() {
        ProvidersRegistry providers = createProvidersRegistryImpl();

        providers.addProvider(new BaseExceptionMapper());
        providers.addProvider(new ThrowableExceptionMapper());
        providers.addProvider(new RuntimeExceptionMapper());
        providers.addProvider(new RestExceptionMapper());
        providers.addProvider(new RestExceptionMapper2());

        assertEquals(BaseExceptionMapper.class, providers.getExceptionMapper(IOException.class,
                                                                             null).getClass());
        assertEquals(ThrowableExceptionMapper.class, providers
            .getExceptionMapper(Error.class, null).getClass());
        assertEquals(RuntimeExceptionMapper.class, providers
            .getExceptionMapper(NullPointerException.class, null).getClass());
        assertEquals(RestExceptionMapper2.class, providers.getExceptionMapper(RestException.class,
                                                                              null).getClass());

        providers = createProvidersRegistryImpl();

        providers = createProvidersRegistryImpl();
        providers.addProvider(new RestExceptionMapper(), 0.7);
        providers.addProvider(new RestExceptionMapper2(), 0.5);
        assertEquals(RestExceptionMapper.class, providers.getExceptionMapper(RestException.class,
                                                                             null).getClass());

    }
}
