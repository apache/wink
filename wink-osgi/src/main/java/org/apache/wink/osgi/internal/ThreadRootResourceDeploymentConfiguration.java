/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wink.osgi.internal;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.apache.wink.common.internal.uritemplate.UriTemplateMatcher;
import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.registry.MethodRecord;
import org.apache.wink.server.internal.registry.ResourceInstance;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.apache.wink.server.internal.registry.SubResourceInstance;


/**
 * A deployment configuration that returns a thread local root resource instance if set.
 */
public class ThreadRootResourceDeploymentConfiguration extends DeploymentConfiguration {

	public static ThreadLocal<Object> threadLocalRootResource = new ThreadLocal<Object>();
	
	/** 
	 * @return the thread local root-resource as ResourceInstance or null
	 */
	private ResourceInstance getThreadLocalResourceInstance() {
		final Object rootResource = threadLocalRootResource.get();
		if (rootResource == null) {
			return null;
		}
		final ObjectFactory<?> objectFactory = new ObjectFactory<Object>() {
            public Object getInstance(RuntimeContext context) {
                return rootResource;
            }

            public Class<Object> getInstanceClass() {
                return Object.class;
            }

            public void releaseInstance(Object instance, RuntimeContext context) {
                /* do nothing */
            }

            public void releaseAll(RuntimeContext context) {
                /* do nothing */
            }
        };

		final ClassMetadata metadata = ResourceMetadataCollector.collectMetadata(rootResource.getClass());
		//for some reason we need a UriTemplateMatcher with a successful last match 
		final UriTemplateProcessor processor = new UriTemplateProcessor() {

			@Override
			public void compile(String arg0) {
				pattern = Pattern.compile(".*");
			}
			
		};
		processor.compile("{.*}");
		final UriTemplateMatcher uriTemplateMatcher = processor.matcher();
		uriTemplateMatcher.match("foo");
		return new ResourceInstance(rootResource, 
				new ResourceRecord(metadata, objectFactory, processor), uriTemplateMatcher);
	}
	

	
	
	public class FixedResourceRegistry extends ResourceRegistry {

		private ResourceRegistry wrapped;
		//private LifecycleManagersRegistry lifecycleManagersRegistry = new LifecycleManagersRegistry();

		public FixedResourceRegistry(ResourceRegistry wrapped) {
			super(new LifecycleManagersRegistry(), null);
			this.wrapped = wrapped;
		}

		@Override
		public void addResource(Class<?> arg0, double arg1) {
			wrapped.addResource(arg0, arg1);
		}

		@Override
		public void addResource(Class<?> clazz) {
			wrapped.addResource(clazz);
		}

		@Override
		public void addResource(Object arg0, double arg1) {
			wrapped.addResource(arg0, arg1);
		}

		@Override
		public void addResource(Object instance) {
			wrapped.addResource(instance);
		}

		@Override
		public MethodRecord findMethod(ResourceInstance arg0,
				RuntimeContext arg1) throws WebApplicationException {
			return wrapped.findMethod(arg0, arg1);
		}

		@Override
		public SubResourceInstance findSubResourceMethod(String pattern,
				List<SubResourceInstance> subResourceRecords,
				ResourceInstance resource, RuntimeContext context)
				throws WebApplicationException {

			return wrapped.findSubResourceMethod(pattern, subResourceRecords, resource,
					context);
		}

		@Override
		public List<ResourceInstance> getMatchingRootResources(String arg0,
				boolean arg1) {
			ResourceInstance threadLocaleInstance = getThreadLocalResourceInstance();
			if (threadLocaleInstance == null) {
				return wrapped.getMatchingRootResources(arg0, arg1);
			} else {
				return Collections.singletonList(threadLocaleInstance);
			}
		}

		@Override
		public List<ResourceInstance> getMatchingRootResources(String uri) {
			return wrapped.getMatchingRootResources(uri);
		}

		@Override
		public Set<String> getOptions(ResourceInstance arg0) {
			return wrapped.getOptions(arg0);
		}

		@Override
		public ResourceRecord getRecord(Class<?> clazz) {
			return wrapped.getRecord(clazz);
		}

		@Override
		public ResourceRecord getRecord(Object instance, boolean isRootResource) {
			return wrapped.getRecord(instance, isRootResource);
		}

		@Override
		public ResourceRecord getRecord(Object instance) {
			return wrapped.getRecord(instance);
		}

		@Override
		public List<ResourceRecord> getRecords() {
			return wrapped.getRecords();
		}

		@Override
		public void removeAllResources() {
			wrapped.removeAllResources();
		}

	}

	@Override
	public ResourceRegistry getResourceRegistry() {
		return new FixedResourceRegistry(super.getResourceRegistry());
	}
	

}
