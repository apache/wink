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
package org.apache.wink.jcdi.server.internal.extension;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;

@SuppressWarnings("unchecked")
public class JAXRSJCDICustomInjectionTargetTest extends TestCase {

    private Mockery                                mockContext       = new Mockery();
    private InjectionTarget<Object>                it                =
                                                                         mockContext
                                                                             .mock(InjectionTarget.class);
    private JAXRSJCDICustomInjectionTarget<Object> jaxrsIT;

    private CreationalContext<Object>              creationalContext =
                                                                         mockContext
                                                                             .mock(CreationalContext.class);

    static class JAXRSApplicationClass extends Application {
        @Context
        public Request      request;

        private HttpHeaders http;

        @Context
        public void setHeaders(HttpHeaders headers) {
            http = headers;
        }

        public HttpHeaders getHttp() {
            return http;
        }
    }

    @Provider
    static class JAXRSProviderClass implements MessageBodyReader<Object> {

        @Context
        public Request      request;

        private HttpHeaders http;

        @Context
        public void setHeaders(HttpHeaders headers) {
            http = headers;
        }

        public HttpHeaders getHttp() {
            return http;
        }

        public boolean isReadable(Class<?> type,
                                  Type genericType,
                                  Annotation[] annotations,
                                  MediaType mediaType) {
            return false;
        }

        public Object readFrom(Class<Object> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders,
                               InputStream entityStream) throws IOException {
            return null;
        }
    }

    @Path("/hello")
    static class JAXRSResourceClass {

        @Context
        public Request      request;

        private HttpHeaders http;

        @Context
        public void setHeaders(HttpHeaders headers) {
            http = headers;
        }

        public HttpHeaders getHttp() {
            return http;
        }
    }

    public void setUp() {
        jaxrsIT = new JAXRSJCDICustomInjectionTarget<Object>(it);
    }

    public void testInjectJAXRSApplication() {
        final JAXRSApplicationClass jaxrsObj = new JAXRSApplicationClass();
        mockContext.checking(new Expectations() {
            {
                oneOf(it).inject(jaxrsObj, creationalContext);
            }
        });
        jaxrsIT.inject(jaxrsObj, creationalContext);
        assertNotNull(jaxrsObj.getHttp());
        assertNotNull(jaxrsObj.request);
        mockContext.assertIsSatisfied();
    }

    public void testInjectJAXRSProvider() {
        final JAXRSProviderClass jaxrsObj = new JAXRSProviderClass();
        mockContext.checking(new Expectations() {
            {
                oneOf(it).inject(jaxrsObj, creationalContext);
            }
        });
        jaxrsIT.inject(jaxrsObj, creationalContext);
        assertNotNull(jaxrsObj.getHttp());
        assertNotNull(jaxrsObj.request);
        mockContext.assertIsSatisfied();
    }

    public void testInjectJAXRSResource() {
        final JAXRSResourceClass jaxrsObj = new JAXRSResourceClass();
        mockContext.checking(new Expectations() {
            {
                oneOf(it).inject(jaxrsObj, creationalContext);
            }
        });
        jaxrsIT.inject(jaxrsObj, creationalContext);
        assertNotNull(jaxrsObj.getHttp());
        assertNotNull(jaxrsObj.request);
        mockContext.assertIsSatisfied();
    }

    public void testPostConstruct() {
        final Object instance = new Object();
        mockContext.checking(new Expectations() {
            {
                oneOf(it).postConstruct(instance);
            }
        });
        jaxrsIT.postConstruct(instance);
        mockContext.assertIsSatisfied();
    }

    public void testPreDestroy() {
        final Object instance = new Object();
        mockContext.checking(new Expectations() {
            {
                oneOf(it).preDestroy(instance);
            }
        });
        jaxrsIT.preDestroy(instance);
        mockContext.assertIsSatisfied();
    }

    public void testDispose() {
        final Object instance = new Object();
        mockContext.checking(new Expectations() {
            {
                oneOf(it).dispose(instance);
            }
        });
        jaxrsIT.dispose(instance);
        mockContext.assertIsSatisfied();
    }

    public void testGetInjectionPoints() {
        final Set<InjectionPoint> expectedIPSet = new HashSet<InjectionPoint>();
        mockContext.checking(new Expectations() {
            {
                oneOf(it).getInjectionPoints();
                will(returnValue(expectedIPSet));
            }
        });
        Set<InjectionPoint> actualIPSet = jaxrsIT.getInjectionPoints();
        assertEquals(expectedIPSet, actualIPSet);
        mockContext.assertIsSatisfied();
    }

    public void testProduce() {
        final Object expectedProducedInstance = new Object();
        mockContext.checking(new Expectations() {
            {
                oneOf(it).produce(creationalContext);
                will(returnValue(expectedProducedInstance));
            }
        });
        Object retValue = jaxrsIT.produce(creationalContext);
        assertEquals(expectedProducedInstance, retValue);
        mockContext.assertIsSatisfied();
    }
}
