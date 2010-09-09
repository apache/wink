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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 */

package org.apache.wink.jcdi.server.internal.lifecycle;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import junit.framework.TestCase;

import org.apache.wink.common.RuntimeContext;
import org.jmock.Expectations;
import org.jmock.Mockery;

public class JCDISingletonObjectFactoryTest extends TestCase {

    private Mockery              mockContext           = new Mockery();

    private BeanManager          beanManagerMock       = mockContext.mock(BeanManager.class);

    private RuntimeContext       runtimeContextMock    = mockContext.mock(RuntimeContext.class);

    private Bean<?>              beanMock              = mockContext.mock(Bean.class);

    private CreationalContext<?> creationalContextMock = mockContext.mock(CreationalContext.class);

    private final JAXRSProvider  providerInstance      = new JAXRSProvider();

    @Provider
    static class JAXRSProvider implements MessageBodyReader<Object> {

        public boolean isReadable(Class<?> type,
                                  Type genericType,
                                  Annotation[] annotations,
                                  MediaType mediaType) {
            throw new UnsupportedOperationException();
        }

        public Object readFrom(Class<Object> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders,
                               InputStream entityStream) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    public void testInit() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Provider.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSProvider.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));

                oneOf(beanManagerMock).createCreationalContext(beanMock);
                will(returnValue(creationalContextMock));

                oneOf(beanManagerMock).getReference(beanMock,
                                                    JAXRSProvider.class,
                                                    creationalContextMock);
                will(returnValue(providerInstance));
            }
        });
        new JCDISingletonObjectFactory<JAXRSProvider>(JAXRSProvider.class, beanManagerMock);
        mockContext.assertIsSatisfied();
    }

    public void testGetInstanceClass() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Provider.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSProvider.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));

                oneOf(beanManagerMock).createCreationalContext(beanMock);
                will(returnValue(creationalContextMock));

                oneOf(beanManagerMock).getReference(beanMock,
                                                    JAXRSProvider.class,
                                                    creationalContextMock);
                will(returnValue(providerInstance));
            }
        });
        JCDISingletonObjectFactory<JAXRSProvider> objFactory =
            new JCDISingletonObjectFactory<JAXRSProvider>(JAXRSProvider.class, beanManagerMock);
        assertEquals(JAXRSProvider.class, objFactory.getInstanceClass());
        mockContext.assertIsSatisfied();
    }

    public void testGetInstance() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Provider.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSProvider.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));

                oneOf(beanManagerMock).createCreationalContext(beanMock);
                will(returnValue(creationalContextMock));

                oneOf(beanManagerMock).getReference(beanMock,
                                                    JAXRSProvider.class,
                                                    creationalContextMock);
                will(returnValue(providerInstance));
            }
        });
        JCDISingletonObjectFactory<JAXRSProvider> objFactory =
            new JCDISingletonObjectFactory<JAXRSProvider>(JAXRSProvider.class, beanManagerMock);

        JAXRSProvider provider = objFactory.getInstance(null);
        assertSame(providerInstance, provider);

        JAXRSProvider provider2 = objFactory.getInstance(null);
        assertSame(provider, provider2);

        JAXRSProvider provider3 = objFactory.getInstance(runtimeContextMock);
        assertSame(provider, provider3);

        mockContext.assertIsSatisfied();
    }

    public void testReleaseInstance() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Provider.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSProvider.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));

                oneOf(beanManagerMock).createCreationalContext(beanMock);
                will(returnValue(creationalContextMock));

                oneOf(beanManagerMock).getReference(beanMock,
                                                    JAXRSProvider.class,
                                                    creationalContextMock);
                will(returnValue(providerInstance));
            }
        });
        JCDISingletonObjectFactory<JAXRSProvider> objFactory =
            new JCDISingletonObjectFactory<JAXRSProvider>(JAXRSProvider.class, beanManagerMock);
        /* this should do absolutely nothing */
        objFactory.releaseInstance(providerInstance, runtimeContextMock);
        objFactory.releaseInstance(providerInstance, null);
        objFactory.releaseInstance(null, runtimeContextMock);
        objFactory.releaseInstance(null, null);
        mockContext.assertIsSatisfied();
    }

    public void testReleaseAllInstance() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Provider.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSProvider.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));

                oneOf(beanManagerMock).createCreationalContext(beanMock);
                will(returnValue(creationalContextMock));

                oneOf(beanManagerMock).getReference(beanMock,
                                                    JAXRSProvider.class,
                                                    creationalContextMock);
                will(returnValue(providerInstance));
            }
        });
        JCDISingletonObjectFactory<JAXRSProvider> objFactory =
            new JCDISingletonObjectFactory<JAXRSProvider>(JAXRSProvider.class, beanManagerMock);

        mockContext.checking(new Expectations() {
            {
                oneOf(creationalContextMock).release();
            }
        });
        objFactory.releaseAll(runtimeContextMock);
        objFactory.releaseAll(null);
        mockContext.assertIsSatisfied();
    }

    public void testReleaseAllWithNoGetInstance() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Provider.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSProvider.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));

                oneOf(beanManagerMock).createCreationalContext(beanMock);
                will(returnValue(creationalContextMock));

                oneOf(beanManagerMock).getReference(beanMock,
                                                    JAXRSProvider.class,
                                                    creationalContextMock);
                will(returnValue(providerInstance));
            }
        });
        JCDISingletonObjectFactory<JAXRSProvider> objFactory =
            new JCDISingletonObjectFactory<JAXRSProvider>(JAXRSProvider.class, beanManagerMock);

        mockContext.checking(new Expectations() {
            {
                oneOf(creationalContextMock).release();
            }
        });
        objFactory.releaseAll(null);
        objFactory.releaseAll(runtimeContextMock);
        mockContext.assertIsSatisfied();
    }

    public void testReleaseAllAfterGetInstance() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Provider.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSProvider.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));

                oneOf(beanManagerMock).createCreationalContext(beanMock);
                will(returnValue(creationalContextMock));

                oneOf(beanManagerMock).getReference(beanMock,
                                                    JAXRSProvider.class,
                                                    creationalContextMock);
                will(returnValue(providerInstance));
            }
        });
        JCDISingletonObjectFactory<JAXRSProvider> objFactory =
            new JCDISingletonObjectFactory<JAXRSProvider>(JAXRSProvider.class, beanManagerMock);

        JAXRSProvider provider = objFactory.getInstance(null);
        assertSame(providerInstance, provider);

        mockContext.checking(new Expectations() {
            {
                oneOf(creationalContextMock).release();
            }
        });
        objFactory.releaseAll(null);

        /* called again for nefarious reasons; should never happen in production */
        objFactory.releaseAll(null);

        mockContext.assertIsSatisfied();
    }

    public void testReleaseAllAfterGetInstanceWithRuntimeContext() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Provider.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSProvider.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));

                oneOf(beanManagerMock).createCreationalContext(beanMock);
                will(returnValue(creationalContextMock));

                oneOf(beanManagerMock).getReference(beanMock,
                                                    JAXRSProvider.class,
                                                    creationalContextMock);
                will(returnValue(providerInstance));
            }
        });

        JCDISingletonObjectFactory<JAXRSProvider> objFactory =
            new JCDISingletonObjectFactory<JAXRSProvider>(JAXRSProvider.class, beanManagerMock);

        JAXRSProvider provider = objFactory.getInstance(null);
        assertSame(providerInstance, provider);

        mockContext.checking(new Expectations() {
            {
                oneOf(creationalContextMock).release();
            }
        });
        objFactory.releaseAll(runtimeContextMock);

        /* called again for nefarious reasons; should never happen in production */
        objFactory.releaseAll(runtimeContextMock);

        mockContext.assertIsSatisfied();
    }

    public void testReleaseInstanceAfterGetInstance() {
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Provider.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSProvider.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));

                oneOf(beanManagerMock).createCreationalContext(beanMock);
                will(returnValue(creationalContextMock));

                oneOf(beanManagerMock).getReference(beanMock,
                                                    JAXRSProvider.class,
                                                    creationalContextMock);
                will(returnValue(providerInstance));
            }
        });

        JCDISingletonObjectFactory<JAXRSProvider> objFactory =
            new JCDISingletonObjectFactory<JAXRSProvider>(JAXRSProvider.class, beanManagerMock);

        JAXRSProvider provider = objFactory.getInstance(null);
        assertSame(providerInstance, provider);

        objFactory.releaseInstance(provider, runtimeContextMock);
        objFactory.releaseInstance(null, runtimeContextMock);
        objFactory.releaseInstance(provider, null);

        mockContext.assertIsSatisfied();
    }

}
