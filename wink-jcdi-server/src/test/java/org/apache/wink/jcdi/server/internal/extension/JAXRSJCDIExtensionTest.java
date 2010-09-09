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

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;

public class JAXRSJCDIExtensionTest extends TestCase {

    private Mockery                   mockContext     = new Mockery();

    private ProcessInjectionTarget<?> pij             =
                                                          mockContext
                                                              .mock(ProcessInjectionTarget.class);

    private InjectionTarget<?>        injectionTarget = mockContext.mock(InjectionTarget.class);

    private AnnotatedType<?>          aType           = mockContext.mock(AnnotatedType.class);

    private JCDIExtension             jcdiExtension   = new JCDIExtension();

    static class JAXRSApplicationClass extends Application {
    }

    @Provider
    static class JAXRSProviderClass implements MessageBodyReader<Object> {

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

    }

    public void testIsJAXRSBean() {
        assertFalse(JCDIExtension.isJAXRSBean(Object.class));
        assertFalse(JCDIExtension.isJAXRSBean(JCDIExtension.class));

        assertTrue(JCDIExtension.isJAXRSBean(JAXRSResourceClass.class));
        assertTrue(JCDIExtension.isJAXRSBean(JAXRSProviderClass.class));
        assertTrue(JCDIExtension.isJAXRSBean(JAXRSApplicationClass.class));
    }

    public void testObserveNonJAXRSProcessInjectionTarget() {
        mockContext.checking(new Expectations() {
            {
                oneOf(pij).getAnnotatedType();
                will(returnValue(aType));

                oneOf(aType).getJavaClass();
                will(returnValue(Object.class));
            }
        });
        jcdiExtension.observeProcessInjectionTarget(pij);
        mockContext.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    public void testObserveJAXRSProcessInjectionTargetForApplicationSubclasses() {
        mockContext.checking(new Expectations() {
            {
                oneOf(pij).getAnnotatedType();
                will(returnValue(aType));

                oneOf(aType).getJavaClass();
                will(returnValue(JAXRSApplicationClass.class));

                oneOf(pij).getInjectionTarget();
                will(returnValue(injectionTarget));

                oneOf(pij).setInjectionTarget(with(any(JAXRSJCDICustomInjectionTarget.class)));
            }
        });
        jcdiExtension.observeProcessInjectionTarget(pij);
        mockContext.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    public void testObserveJAXRSProcessInjectionTargetForResourceClasses() {
        mockContext.checking(new Expectations() {
            {
                oneOf(pij).getAnnotatedType();
                will(returnValue(aType));

                oneOf(aType).getJavaClass();
                will(returnValue(JAXRSResourceClass.class));

                oneOf(pij).getInjectionTarget();
                will(returnValue(injectionTarget));

                oneOf(pij).setInjectionTarget(with(any(JAXRSJCDICustomInjectionTarget.class)));
            }
        });
        jcdiExtension.observeProcessInjectionTarget(pij);
        mockContext.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    public void testObserveJAXRSProcessInjectionTargetForProviderClasses() {
        mockContext.checking(new Expectations() {
            {
                oneOf(pij).getAnnotatedType();
                will(returnValue(aType));

                oneOf(aType).getJavaClass();
                will(returnValue(JAXRSProviderClass.class));

                oneOf(pij).getInjectionTarget();
                will(returnValue(injectionTarget));

                oneOf(pij).setInjectionTarget(with(any(JAXRSJCDICustomInjectionTarget.class)));
            }
        });
        jcdiExtension.observeProcessInjectionTarget(pij);
        mockContext.assertIsSatisfied();
    }
}
