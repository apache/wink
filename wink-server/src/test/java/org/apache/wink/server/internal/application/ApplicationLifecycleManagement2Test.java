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

package org.apache.wink.server.internal.application;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.wink.common.internal.lifecycle.LifecycleManager;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.jmock.Expectations;
import org.jmock.Mockery;

@SuppressWarnings("unchecked")
public class ApplicationLifecycleManagement2Test extends MockServletInvocationTest {

    public static class CustomDeploymentConfiguration extends DeploymentConfiguration {
        public CustomDeploymentConfiguration() {
            LifecycleManagersRegistry lifecycleManagersRegistry = new LifecycleManagersRegistry();
            setOfFactoryRegistry(lifecycleManagersRegistry);
            lifecycleManagersRegistry.addFactoryFactory(lifecycleManagerMock);
        }
    }

    private static Mockery          ofMockery            = new Mockery();

    private static ObjectFactory    ofMock               = ofMockery.mock(ObjectFactory.class);

    private static LifecycleManager lifecycleManagerMock = ofMockery.mock(LifecycleManager.class);

    static {
        ofMockery.checking(new Expectations() {
            {
                oneOf(ofMock).getInstance(null);
                will(returnValue(new MyApp()));

                oneOf(lifecycleManagerMock).createObjectFactory(MyApp.class);
                will(returnValue(ofMock));
                allowing(lifecycleManagerMock).createObjectFactory(with(any(Class.class)));
                will(returnValue(null));
                allowing(lifecycleManagerMock).createObjectFactory(with(any(Object.class)));
                will(returnValue(null));
            }
        });
    }

    public static class MyApp extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            return null;
        }

    }

    @Override
    protected String getApplicationClassName() {
        return MyApp.class.getName();
    }

    @Override
    protected String getDeploymentConfigurationClassName() {
        return CustomDeploymentConfiguration.class.getName();
    }

    public void test() throws Exception {
        ofMockery.assertIsSatisfied();
    }

}
