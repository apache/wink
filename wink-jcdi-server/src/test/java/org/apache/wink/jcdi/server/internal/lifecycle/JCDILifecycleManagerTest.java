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
package org.apache.wink.jcdi.server.internal.lifecycle;

import static org.hamcrest.Matchers.anyOf;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.util.Collections;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Named;
import javax.inject.Qualifier;
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

public class JCDILifecycleManagerTest extends TestCase {

    private Mockery     mockContext      = new Mockery();

    private BeanManager beanManagerMock  = mockContext.mock(BeanManager.class);

    private BeanManager beanManager2Mock = mockContext.mock(BeanManager.class, "beanManager2Mock");

    private Bean<?>     beanMock         = mockContext.mock(Bean.class);

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

    @Named("myJAXRSResourceClassName")
    @Path("/hello")
    static class JAXRSResourceClassWithQualifier {

    }

    @Qualifier
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface MyQualifierAnnotation {
    }

    @Named("myJAXRSResourceClassName")
    @MyQualifierAnnotation
    @Path("/hello")
    static class JAXRSResourceClassWithMultipleQualifiers {

    }

    public void testIsJCDIManagedBeanResourceClass() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Path.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSResourceClass.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));
            }
        });
        assertTrue(JCDILifecycleManager
            .isJCDIManagedBean(JAXRSResourceClass.class, beanManagerMock));
        mockContext.assertIsSatisfied();
    }

    public void testIsJCDIManagedBeanResourceClassWithQualifier() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Path.class);
                will(returnValue(false));

                oneOf(beanManagerMock).isQualifier(Named.class);
                will(returnValue(true));

                oneOf(beanManagerMock)
                    .getBeans(JAXRSResourceClassWithQualifier.class,
                              new Annotation[] {JAXRSResourceClassWithQualifier.class
                                  .getAnnotation(Named.class)});
                will(returnValue(Collections.singleton(beanMock)));
            }
        });
        assertTrue(JCDILifecycleManager.isJCDIManagedBean(JAXRSResourceClassWithQualifier.class,
                                                          beanManagerMock));
        mockContext.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    public void testIsJCDIManagedBeanResourceClassWithMultipleQualifiers() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Path.class);
                will(returnValue(false));

                oneOf(beanManagerMock).isQualifier(Named.class);
                will(returnValue(true));

                oneOf(beanManagerMock).isQualifier(MyQualifierAnnotation.class);
                will(returnValue(true));

                oneOf(beanManagerMock)
                    .getBeans(with(same(JAXRSResourceClassWithMultipleQualifiers.class)),
                              with(anyOf(

                              equal(new Annotation[] {
                                  JAXRSResourceClassWithMultipleQualifiers.class
                                      .getAnnotation(Named.class),
                                  JAXRSResourceClassWithMultipleQualifiers.class
                                      .getAnnotation(MyQualifierAnnotation.class)}),

                              equal(new Annotation[] {
                                  JAXRSResourceClassWithMultipleQualifiers.class
                                      .getAnnotation(MyQualifierAnnotation.class),
                                  JAXRSResourceClassWithMultipleQualifiers.class
                                      .getAnnotation(Named.class)})

                              )));
                will(returnValue(Collections.singleton(beanMock)));
            }
        });
        assertTrue(JCDILifecycleManager
            .isJCDIManagedBean(JAXRSResourceClassWithMultipleQualifiers.class, beanManagerMock));
        mockContext.assertIsSatisfied();
    }

    public void testIsJCDIManagedBeanResourceClassReturnsFalseDueToEmptySet() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Path.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSResourceClass.class, new Annotation[0]);
                will(returnValue(Collections.emptySet()));
            }
        });
        assertFalse(JCDILifecycleManager.isJCDIManagedBean(JAXRSResourceClass.class,
                                                           beanManagerMock));
        mockContext.assertIsSatisfied();
    }

    public void testIsJCDIManagedBeanResourceClassReturnsFalseDueToNull() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Path.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSResourceClass.class, new Annotation[0]);
                will(returnValue(null));
            }
        });
        assertFalse(JCDILifecycleManager.isJCDIManagedBean(JAXRSResourceClass.class,
                                                           beanManagerMock));
        mockContext.assertIsSatisfied();
    }

    public void testSetGetBeanManager() {
        JCDILifecycleManager<Object> manager = new JCDILifecycleManager<Object>();

        // should not be able to lookup via JNDI
        assertNull(manager.getBeanManager());

        manager.setBeanManager(beanManagerMock);
        assertSame(beanManagerMock, manager.getBeanManager());

        manager.setBeanManager(beanManager2Mock);
        assertSame(beanManager2Mock, manager.getBeanManager());

        mockContext.assertIsSatisfied();
    }

    public void testCreateSingletonObjectFactory() {
        JCDILifecycleManager<Object> manager = new JCDILifecycleManager<Object>();
        try {
            manager.createObjectFactory(null);
            fail("Expected NPE");
        } catch (NullPointerException e) {
            // expected
        }
        assertNull(manager.createObjectFactory(new Object()));
        assertNull(manager.createObjectFactory(new JAXRSResourceClass()));
        assertNull(manager.createObjectFactory(new JAXRSApplicationClass()));
        assertNull(manager.createObjectFactory(new JAXRSProviderClass()));
        assertNull(manager.createObjectFactory(new JAXRSResourceClassWithQualifier()));
        assertNull(manager.createObjectFactory(new JAXRSResourceClassWithMultipleQualifiers()));
        mockContext.assertIsSatisfied();
    }

    public void testCreateClassObjectFactoryNull() {
        JCDILifecycleManager<JAXRSResourceClass> manager =
            new JCDILifecycleManager<JAXRSResourceClass>();
        try {
            Class<JAXRSResourceClass> c = null;
            manager.createObjectFactory(c);
            fail("Expected NPE");
        } catch (NullPointerException e) {
            // expected
        }
        mockContext.assertIsSatisfied();
    }

    public void testCreateClassObjectFactoryButNotManagedBean() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Path.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSResourceClass.class, new Annotation[0]);
                will(returnValue(null));
            }
        });
        JCDILifecycleManager<JAXRSResourceClass> manager =
            new JCDILifecycleManager<JAXRSResourceClass>();
        manager.setBeanManager(beanManagerMock);
        assertNull(manager.createObjectFactory(JAXRSResourceClass.class));
        mockContext.assertIsSatisfied();
    }

    public void testCreateClassObjectFactoryWithResourceManagedBean() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Path.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSResourceClass.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));
            }
        });
        JCDILifecycleManager<JAXRSResourceClass> manager =
            new JCDILifecycleManager<JAXRSResourceClass>();
        manager.setBeanManager(beanManagerMock);
        assertNotNull(manager.createObjectFactory(JAXRSResourceClass.class));
        mockContext.assertIsSatisfied();
    }
}
