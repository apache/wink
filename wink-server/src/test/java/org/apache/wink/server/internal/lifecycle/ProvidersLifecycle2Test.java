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

package org.apache.wink.server.internal.lifecycle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.servlet.ServletException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.lifecycle.LifecycleManager;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@SuppressWarnings("unchecked")
public class ProvidersLifecycle2Test extends MockServletInvocationTest {

    public static class CustomDeploymentConfiguration extends DeploymentConfiguration {
        public CustomDeploymentConfiguration() {
            LifecycleManagersRegistry lifecycleManagersRegistry = new LifecycleManagersRegistry();
            setOfFactoryRegistry(lifecycleManagersRegistry);
            lifecycleManagersRegistry.addFactoryFactory(lifecycleManagerMock);
        }
    }

    static Mockery          ofMockery                 = new Mockery();

    static LifecycleManager lifecycleManagerMock      = ofMockery.mock(LifecycleManager.class);

    static ObjectFactory    messageBodyProviderOFMock =
                                                          ofMockery
                                                              .mock(ObjectFactory.class,
                                                                    "messageBodyProviderOfFactory");

    static ObjectFactory    exceptionMapperOFMock     =
                                                          ofMockery.mock(ObjectFactory.class,
                                                                         "exceptionMapperOFMock");
    static ObjectFactory    contextResolverOFMock     =
                                                          ofMockery.mock(ObjectFactory.class,
                                                                         "contextResolverOFMock");

    @Override
    public String getDeploymentConfigurationClassName() {
        return CustomDeploymentConfiguration.class.getName();
    }

    @Provider
    public static class MyExceptionMapper implements ExceptionMapper<NullPointerException> {

        public Response toResponse(NullPointerException arg0) {
            return null;
        }

    }

    @Provider
    public static class MyContextResolver implements ContextResolver<Object> {

        public Object getContext(Class<?> arg0) {
            return null;
        }

    }

    @Provider
    public static class MessageBodyEntityProvider implements MessageBodyReader<String>,
        MessageBodyWriter<String> {

        public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
            return true;
        }

        public String readFrom(Class<String> arg0,
                               Type arg1,
                               Annotation[] arg2,
                               MediaType arg3,
                               MultivaluedMap<String, String> arg4,
                               InputStream arg5) throws IOException, WebApplicationException {
            return "Read overwrite whatever written";
        }

        public long getSize(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
            return -1;
        }

        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
            return true;
        }

        public void writeTo(String arg0,
                            Class<?> arg1,
                            Type arg2,
                            Annotation[] arg3,
                            MediaType arg4,
                            MultivaluedMap<String, Object> arg5,
                            OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write((arg0 + "::write overwrite whatever written").getBytes());
        }

    }

    static {
        ofMockery.checking(new Expectations() {
            {
                exactly(2).of(messageBodyProviderOFMock)
                    .getInstance(with(any(RuntimeContext.class)));
                will(returnValue(new MessageBodyEntityProvider()));

                allowing(messageBodyProviderOFMock).getInstanceClass();
                will(returnValue(MessageBodyEntityProvider.class));

                exactly(2).of(messageBodyProviderOFMock).releaseAll(null);

                allowing(exceptionMapperOFMock).getInstanceClass();
                will(returnValue(MyExceptionMapper.class));

                oneOf(exceptionMapperOFMock).releaseAll(null);

                allowing(contextResolverOFMock).getInstanceClass();
                will(returnValue(MyContextResolver.class));

                oneOf(contextResolverOFMock).releaseAll(null);

                oneOf(lifecycleManagerMock).createObjectFactory(MessageBodyEntityProvider.class);
                will(returnValue(messageBodyProviderOFMock));
                oneOf(lifecycleManagerMock).createObjectFactory(MyExceptionMapper.class);
                will(returnValue(exceptionMapperOFMock));
                oneOf(lifecycleManagerMock).createObjectFactory(MyContextResolver.class);
                will(returnValue(contextResolverOFMock));

                allowing(lifecycleManagerMock).createObjectFactory(with(any(Class.class)));
                will(returnValue(null));
                allowing(lifecycleManagerMock).createObjectFactory(with(any(Object.class)));
                will(returnValue(null));
            }
        });
    }

    @Path("myresource")
    public static class MyResource {
        @POST
        public String getHello(String entity) {
            return entity;
        }
    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {MyResource.class, MessageBodyEntityProvider.class,
            MyExceptionMapper.class, MyContextResolver.class};
    }

    /**
     * Tests that the resource ObjectFactory's removeInstance() call is made
     * when the ObjectFactory represents a regular request scoped resource.
     * 
     * @throws Throwable
     */
    public void testResourceRemoveInstanceCalled() throws Throwable {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/myresource",
                                                        "text/plain",
                                                        "text/plain",
                                                        "hello".getBytes());
        try {
            MockHttpServletResponse response = invoke(request);
            assertTrue(response.getStatus() == Status.OK.getStatusCode());
            assertEquals("Read overwrite whatever written::write overwrite whatever written",
                         response.getContentAsString());
            getServlet().destroy();
            ofMockery.assertIsSatisfied();
        } catch (ServletException e) {
            throw e.getRootCause();
        }
    }
}
