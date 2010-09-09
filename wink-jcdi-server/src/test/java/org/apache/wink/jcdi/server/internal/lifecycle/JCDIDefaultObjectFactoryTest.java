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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;

import junit.framework.TestCase;

import org.apache.wink.common.RuntimeContext;
import org.jmock.Expectations;
import org.jmock.Mockery;

public class JCDIDefaultObjectFactoryTest extends TestCase {

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

    private Mockery              mockContext           = new Mockery();

    private BeanManager          beanManagerMock       = mockContext.mock(BeanManager.class);

    private RuntimeContext       runtimeContextMock    = mockContext.mock(RuntimeContext.class);

    private Bean<?>              beanMock              = mockContext.mock(Bean.class);

    private CreationalContext<?> creationalContextMock = mockContext.mock(CreationalContext.class);

    private Map<?, ?>            attributeMap          = mockContext.mock(Map.class);

    public void testInit() {
        JCDIDefaultObjectFactory<JAXRSResourceClass> objFactory =
            new JCDIDefaultObjectFactory<JAXRSResourceClass>(JAXRSResourceClass.class, beanManagerMock);
        assertEquals(JAXRSResourceClass.class, objFactory.getInstanceClass());
        assertNull(objFactory.getCreationalContextMap());
        mockContext.assertIsSatisfied();
    }

    public void testGetInstanceWithNullRuntimeContext() {
        final JAXRSResourceClass resourceInstance = new JAXRSResourceClass();
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Path.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSResourceClass.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));

                oneOf(beanManagerMock).createCreationalContext(beanMock);
                will(returnValue(creationalContextMock));

                oneOf(beanManagerMock).getReference(beanMock,
                                                    JAXRSResourceClass.class,
                                                    creationalContextMock);
                will(returnValue(resourceInstance));
            }
        });

        JCDIDefaultObjectFactory<JAXRSResourceClass> objFactory =
            new JCDIDefaultObjectFactory<JAXRSResourceClass>(JAXRSResourceClass.class, beanManagerMock);
        assertSame(resourceInstance, objFactory.getInstance(null));
        assertNotNull(objFactory.getCreationalContextMap());
        assertSame(creationalContextMock, objFactory.getCreationalContextMap()
            .get(resourceInstance));
        mockContext.assertIsSatisfied();

        /*
         * now test a releaseInstance; this should never occur in practice
         * because the getInstance was passed a null RuntimeContext (so it is a
         * global/ApplicationScoped object) but testing it
         */

        mockContext.checking(new Expectations() {
            {
                oneOf(runtimeContextMock).getAttributes();
                will(returnValue(attributeMap));

                oneOf(attributeMap).remove(CreationalContext.class.getName());
                will(returnValue(null));
            }
        });

        objFactory.releaseInstance(resourceInstance, runtimeContextMock);
        assertNotNull(objFactory.getCreationalContextMap());
        mockContext.assertIsSatisfied();

        /* now test the releaseAll */

        mockContext.checking(new Expectations() {
            {
                oneOf(creationalContextMock).release();
            }
        });

        objFactory.releaseAll(null);
        assertNull(objFactory.getCreationalContextMap());
        mockContext.assertIsSatisfied();
    }

    public void testGetInstanceWithRuntimeContext() {
        final JAXRSResourceClass resourceInstance = new JAXRSResourceClass();
        mockContext.checking(new Expectations() {
            {
                oneOf(beanManagerMock).isQualifier(Path.class);
                will(returnValue(false));

                oneOf(beanManagerMock).getBeans(JAXRSResourceClass.class, new Annotation[0]);
                will(returnValue(Collections.singleton(beanMock)));

                oneOf(beanManagerMock).createCreationalContext(beanMock);
                will(returnValue(creationalContextMock));

                oneOf(beanManagerMock).getReference(beanMock,
                                                    JAXRSResourceClass.class,
                                                    creationalContextMock);
                will(returnValue(resourceInstance));

                oneOf(runtimeContextMock).setAttribute(CreationalContext.class,
                                                       creationalContextMock);
            }
        });

        JCDIDefaultObjectFactory<JAXRSResourceClass> objFactory =
            new JCDIDefaultObjectFactory<JAXRSResourceClass>(JAXRSResourceClass.class, beanManagerMock);
        assertSame(resourceInstance, objFactory.getInstance(runtimeContextMock));
        assertNull(objFactory.getCreationalContextMap());
        mockContext.assertIsSatisfied();

        /* now test the release */

        mockContext.checking(new Expectations() {
            {
                oneOf(creationalContextMock).release();

                oneOf(runtimeContextMock).getAttributes();
                will(returnValue(attributeMap));

                oneOf(attributeMap).remove(CreationalContext.class.getName());
                will(returnValue(creationalContextMock));
            }
        });

        objFactory.releaseInstance(resourceInstance, runtimeContextMock);
        assertNull(objFactory.getCreationalContextMap());

        mockContext.assertIsSatisfied();
    }

    public void testReleaseInstanceNull() {
        JCDIDefaultObjectFactory<JAXRSResourceClass> objFactory =
            new JCDIDefaultObjectFactory<JAXRSResourceClass>(JAXRSResourceClass.class, beanManagerMock);
        assertNull(objFactory.getCreationalContextMap());
        objFactory.releaseInstance(null, null);
        assertNull(objFactory.getCreationalContextMap());

        mockContext.assertIsSatisfied();
    }

    public void testReleaseInstanceRuntimeContextNoCreationalContext() {
        JCDIDefaultObjectFactory<JAXRSResourceClass> objFactory =
            new JCDIDefaultObjectFactory<JAXRSResourceClass>(JAXRSResourceClass.class, beanManagerMock);
        assertNull(objFactory.getCreationalContextMap());
        objFactory.releaseInstance(new JAXRSResourceClass(), null);
        assertNull(objFactory.getCreationalContextMap());
        mockContext.assertIsSatisfied();

        mockContext.checking(new Expectations() {
            {
                oneOf(runtimeContextMock).getAttributes();
                will(returnValue(attributeMap));

                oneOf(attributeMap).remove(CreationalContext.class.getName());
                will(returnValue(null));
            }
        });

        assertNull(objFactory.getCreationalContextMap());
        objFactory.releaseInstance(null, runtimeContextMock);
        assertNull(objFactory.getCreationalContextMap());

        mockContext.assertIsSatisfied();
    }

    public void testReleaseAllNull() {
        JCDIDefaultObjectFactory<JAXRSResourceClass> objFactory =
            new JCDIDefaultObjectFactory<JAXRSResourceClass>(JAXRSResourceClass.class, beanManagerMock);
        assertNull(objFactory.getCreationalContextMap());
        objFactory.releaseAll(null);
        assertNull(objFactory.getCreationalContextMap());

        mockContext.assertIsSatisfied();
    }

    public void testReleaseAllNoApplicationScopedObjects() {
        JCDIDefaultObjectFactory<JAXRSResourceClass> objFactory =
            new JCDIDefaultObjectFactory<JAXRSResourceClass>(JAXRSResourceClass.class, beanManagerMock);
        assertNull(objFactory.getCreationalContextMap());
        objFactory.releaseAll(runtimeContextMock);
        assertNull(objFactory.getCreationalContextMap());

        mockContext.assertIsSatisfied();
    }
    
    /* TODO need to test multiple runs */
}
