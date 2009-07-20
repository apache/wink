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
package org.apache.wink.server.internal.application;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.AbstractDynamicResource;
import org.apache.wink.common.WinkApplication;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.providers.entity.FileProvider;
import org.apache.wink.common.internal.providers.entity.StreamingOutputProvider;
import org.apache.wink.common.internal.providers.entity.StringProvider;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.server.internal.application.ApplicationProcessor;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.apache.wink.server.internal.resources.HtmlServiceDocumentResource;
import org.apache.wink.server.internal.resources.RootResource;

import junit.framework.TestCase;

public class ApplicationProcessorTest extends TestCase {

    private static class ResourceRegistryMock extends ResourceRegistry {

        List<Object>   instances = new ArrayList<Object>();
        List<Class<?>> classes   = new ArrayList<Class<?>>();

        public ResourceRegistryMock() {
            super(new LifecycleManagersRegistry(), null);
        }

        public void addResource(Object instance) {
            addResource(instance, 0.5);
        }

        public void addResource(Object instance, double priority) {
            if (instance instanceof BadResource) {
                throw new BadResource("BadResource cannot be added");
            }
            instances.add(instance);
        }

        public void addResource(Class<?> clazz) {
            addResource(clazz, 0.5);
        }

        public void addResource(Class<?> clazz, double priority) {
            if (BadResource.class == clazz) {
                throw new BadResource("BadResource cannot be added");
            }
            classes.add(clazz);
        }
    }

    private static class ProvidersRegistryMock extends ProvidersRegistry {

        List<Object>   instances = new ArrayList<Object>();
        List<Class<?>> classes   = new ArrayList<Class<?>>();

        public ProvidersRegistryMock() {
            super(new LifecycleManagersRegistry(), null);
        }

        public boolean addProvider(Class<?> cls) {
            return addProvider(cls, 0.5);
        }

        public boolean addProvider(Object provider) {
            return addProvider(provider, 0.5);
        }

        public boolean addProvider(Class<?> cls, double priority) {
            if (BadProvider.class == cls) {
                throw new BadProvider("BadProvider cannot be added");
            }
            return classes.add(cls);
        }

        public boolean addProvider(Object provider, double priority) {
            if (provider instanceof BadProvider) {
                throw new BadProvider("BadProvider cannot be added");
            }
            return instances.add(provider);
        }
    }

    @Provider
    private static class BadProvider extends RuntimeException {
        public BadProvider() {
            super();
        }

        public BadProvider(String message) {
            super(message);
        }
    }

    @Path("bad-resource")
    private static class BadResource extends RuntimeException {
        public BadResource() {
            super();
        }

        public BadResource(String message) {
            super(message);
        }
    }

    private static final StreamingOutputProvider StreamingOutputProvider =
                                                                             new StreamingOutputProvider();
    private static final RootResource            rootResource            = new RootResource();

    private static class ApplicationMock extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            LinkedHashSet<Class<?>> classes = new LinkedHashSet<Class<?>>();
            classes.add(FileProvider.class); // provider
            classes.add(BadResource.class); // simulate resource exception
            classes.add(String.class); // should be ignored
            classes.add(BadProvider.class); // simulate provider exception
            classes.add(RootResource.class); // resource
            return classes;
        }

        @Override
        public Set<Object> getSingletons() {
            LinkedHashSet<Object> instances = new LinkedHashSet<Object>();
            instances.add(StreamingOutputProvider); // provider
            instances.add(new BadResource()); // simulate resource exception
            instances.add("bla-bla"); // should be ignored
            instances.add(new BadProvider()); // simulate provider exception
            instances.add(rootResource);
            return instances;
        }

    }

    private static final HtmlServiceDocumentResource HtmlServiceDocument =
                                                                             new HtmlServiceDocumentResource();
    private static final StringProvider              StringProvider      = new StringProvider();
    private static final AbstractDynamicResource     DynamicResource     =
                                                                             new AbstractDynamicResource() {
                                                                             };

    private static class WinkApplicationMock extends WinkApplication {

        @Override
        public Set<Class<?>> getClasses() {
            LinkedHashSet<Class<?>> classes = new LinkedHashSet<Class<?>>();
            classes.add(FileProvider.class); // provider
            classes.add(BadResource.class); // simulate resource exception
            classes.add(String.class); // should be ignored
            classes.add(BadProvider.class); // simulate provider exception
            classes.add(RootResource.class); // resource
            return classes;
        }

        @Override
        public Set<Object> getSingletons() {
            LinkedHashSet<Object> instances = new LinkedHashSet<Object>();
            instances.add(StreamingOutputProvider); // provider
            instances.add(new BadResource()); // simulate resource exception
            instances.add("bla-bla"); // should be ignored
            instances.add(new BadProvider()); // simulate provider exception
            instances.add(rootResource); // resource
            return instances;
        }

        @Override
        public Set<Object> getInstances() {
            LinkedHashSet<Object> instances = new LinkedHashSet<Object>();
            instances.add(StringProvider);
            instances.add("bla-bla"); // should be ignored
            instances.add(new BadResource()); // simulate resource exception
            instances.add(HtmlServiceDocument);
            instances.add(new BadProvider()); // simulate provider exception
            instances.add(DynamicResource);
            return instances;
        }
    }

    public void testApplication() {
        ResourceRegistryMock resourceRegistry = new ResourceRegistryMock();
        ProvidersRegistryMock providersRegistry = new ProvidersRegistryMock();
        new ApplicationProcessor(new ApplicationMock(), resourceRegistry, providersRegistry)
            .process();
        assertTrue(providersRegistry.classes.contains(FileProvider.class));
        assertTrue(resourceRegistry.classes.contains(RootResource.class));
        assertEquals(1, providersRegistry.classes.size());
        assertEquals(1, resourceRegistry.classes.size());
        assertTrue(providersRegistry.instances.contains(StreamingOutputProvider));
        assertTrue(resourceRegistry.instances.contains(rootResource));
        assertEquals(1, providersRegistry.instances.size());
        assertEquals(1, resourceRegistry.instances.size());
    }

    public void testWinkApplication() {
        ResourceRegistryMock resourceRegistry = new ResourceRegistryMock();
        ProvidersRegistryMock providersRegistry = new ProvidersRegistryMock();
        new ApplicationProcessor(new WinkApplicationMock(), resourceRegistry, providersRegistry)
            .process();
        assertTrue(providersRegistry.classes.contains(FileProvider.class));
        assertTrue(resourceRegistry.classes.contains(RootResource.class));
        assertEquals(1, providersRegistry.classes.size());
        assertEquals(1, resourceRegistry.classes.size());
        assertTrue(providersRegistry.instances.contains(StreamingOutputProvider));
        assertTrue(providersRegistry.instances.contains(StringProvider));
        assertTrue(resourceRegistry.instances.contains(rootResource));
        assertTrue(resourceRegistry.instances.contains(HtmlServiceDocument));
        assertTrue(resourceRegistry.instances.contains(DynamicResource));
        assertEquals(2, providersRegistry.instances.size());
        assertEquals(3, resourceRegistry.instances.size());
    }
}
