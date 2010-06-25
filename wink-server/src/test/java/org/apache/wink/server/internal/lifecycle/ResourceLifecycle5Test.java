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

import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.lifecycle.LifecycleManager;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@SuppressWarnings("unchecked")
public class ResourceLifecycle5Test extends ResourceLifecycle {

    public static class CustomDeploymentConfiguration extends DeploymentConfiguration {
        public CustomDeploymentConfiguration() {
            LifecycleManagersRegistry lifecycleManagersRegistry = new LifecycleManagersRegistry();
            setOfFactoryRegistry(lifecycleManagersRegistry);
            lifecycleManagersRegistry.addFactoryFactory(lifecycleManagerMock);
        }
    }

    static Mockery          ofMockery            = new Mockery();

    static LifecycleManager lifecycleManagerMock = ofMockery.mock(LifecycleManager.class);

    static ObjectFactory    res1OfMock           =
                                                     ofMockery.mock(ObjectFactory.class,
                                                                    "res1OfMock");

    static ObjectFactory    res2OfMock           =
                                                     ofMockery.mock(ObjectFactory.class,
                                                                    "res2OfMock");
    static ObjectFactory    res3OfMock           =
                                                     ofMockery.mock(ObjectFactory.class,
                                                                    "res3OfMock");

    static ObjectFactory    exceptionResOfMock   =
                                                     ofMockery.mock(ObjectFactory.class,
                                                                    "exceptionResOfMock");

    static ExceptionMapper  exceptionMapperMock  = ofMockery.mock(ExceptionMapper.class);

    @Provider
    public static class MyExceptionMapper implements ExceptionMapper<WebApplicationException> {

        public Response toResponse(WebApplicationException arg0) {
            return exceptionMapperMock.toResponse(arg0);
        }
    }

    @Path("exception1")
    public static class MyExceptionResource {
        @GET
        public String helloException() {
            throw new WebApplicationException();
        }
    }

    @Override
    public String getDeploymentConfigurationClassName() {
        return CustomDeploymentConfiguration.class.getName();
    }

    static {
        ofMockery.checking(new Expectations() {
            {
                MyExceptionResource exceptionRes = new MyExceptionResource();

                final Sequence releaseSequence = ofMockery.sequence("sequence-name");

                oneOf(exceptionResOfMock).getInstance(with(any(RuntimeContext.class)));
                will(returnValue(exceptionRes));
                inSequence(releaseSequence);

                oneOf(exceptionMapperMock).toResponse(with(any(WebApplicationException.class)));
                will(returnValue(Response.status(Status.BAD_REQUEST)
                    .entity("exception mapper mock").type(MediaType.TEXT_PLAIN_TYPE).build()));
                inSequence(releaseSequence);

                // expectation is 1 release call during invocation after
                // exception mapper is already called
                oneOf(exceptionResOfMock).releaseInstance(with(exceptionRes),
                                                          with(any(RuntimeContext.class)));
                inSequence(releaseSequence);

                oneOf(lifecycleManagerMock).createObjectFactory(Resource1.class);
                will(returnValue(res1OfMock));
                oneOf(lifecycleManagerMock).createObjectFactory(Resource2.class);
                will(returnValue(res2OfMock));
                oneOf(lifecycleManagerMock).createObjectFactory(Resource3.class);
                will(returnValue(res3OfMock));
                oneOf(lifecycleManagerMock).createObjectFactory(MyExceptionResource.class);
                will(returnValue(exceptionResOfMock));
                allowing(lifecycleManagerMock).createObjectFactory(with(any(Class.class)));
                will(returnValue(null));
                allowing(lifecycleManagerMock).createObjectFactory(with(any(Object.class)));
                will(returnValue(null));
            }
        });
    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {Resource1.class, Resource2.class, Resource3.class,
            MyExceptionResource.class, MyExceptionMapper.class};
    }

    /**
     * Tests that the resource ObjectFactory's removeInstance call is made when
     * an unhandled exception occurs during the request processing. Also, the
     * removeInstance call should be made after the response processing is done.
     * 
     * @throws Throwable
     */
    public void testResourceRemoveInstanceCalled() throws Throwable {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/exception1", "text/plain");
        try {
            MockHttpServletResponse response = invoke(request);
            assertTrue(response.getStatus() == Status.BAD_REQUEST.getStatusCode());
            assertEquals("exception mapper mock", response.getContentAsString());
            ofMockery.assertIsSatisfied();
        } catch (ServletException e) {
            throw e.getRootCause();
        }
    }

}
