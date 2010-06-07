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
import javax.ws.rs.core.Response.Status;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.lifecycle.LifecycleManager;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@SuppressWarnings("unchecked")
public class ResourceLifecycle2Test extends ResourceLifecycle {

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

    @Override
    public String getDeploymentConfigurationClassName() {
        return CustomDeploymentConfiguration.class.getName();
    }

    static {
        ofMockery.checking(new Expectations() {
            {
                Resource2 res2 = new Resource2();

                oneOf(res2OfMock).getInstance(with(any(RuntimeContext.class)));
                will(returnValue(res2));

                // expectation is 1 release call during invocation
                oneOf(res2OfMock).releaseInstance(with(res2), with(any(RuntimeContext.class)));

                // expectation is every resource object factory will eventually
                // have removeAll called
                oneOf(res1OfMock).releaseAll(null);
                oneOf(res2OfMock).releaseAll(null);
                oneOf(res3OfMock).releaseAll(null);

                oneOf(lifecycleManagerMock).createObjectFactory(Resource1.class);
                will(returnValue(res1OfMock));
                oneOf(lifecycleManagerMock).createObjectFactory(Resource2.class);
                will(returnValue(res2OfMock));
                oneOf(lifecycleManagerMock).createObjectFactory(Resource3.class);
                will(returnValue(res3OfMock));
                allowing(lifecycleManagerMock).createObjectFactory(with(any(Class.class)));
                will(returnValue(null));
                allowing(lifecycleManagerMock).createObjectFactory(with(any(Object.class)));
                will(returnValue(null));
            }
        });
    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {Resource1.class, Resource2.class, Resource3.class};
    }

    /**
     * Tests that the resource ObjectFactory's removeAll call is made when the
     * servlet is destroyed and when the ObjectFactory represents a regular request
     * scoped resource.
     * 
     * @throws Throwable
     */
    public void testResourceRemoveAllCalled() throws Throwable {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/resource2", "text/plain");
        try {
            MockHttpServletResponse response = invoke(request);
            assertTrue(response.getStatus() == Status.OK.getStatusCode());
            assertEquals("resource2", response.getContentAsString());
            getServlet().destroy();
            ofMockery.assertIsSatisfied();
        } catch (ServletException e) {
            throw e.getRootCause();
        }
    }

}
