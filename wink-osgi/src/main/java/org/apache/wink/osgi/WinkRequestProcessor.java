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
package org.apache.wink.osgi;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.wink.common.internal.runtime.RuntimeDelegateImpl;
import org.apache.wink.osgi.internal.DummyRuntimeDelegate;
import org.apache.wink.osgi.internal.ThreadRootResourceDeploymentConfiguration;
import org.apache.wink.server.internal.RequestProcessor;
import org.apache.wink.server.utils.RegistrationUtils.InnerApplication;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * This component listens to registrations of services of type <code>java.lang.Object</code>
 * with the property <code>javax.ws.rs</code> set to true and registers them with the 
 * Wink RequestProcessor. Root resources and providers that are not exposed as such a service
 * can be registered by invoking the bindComponent method.
 *
 */
@Component
@Reference(name="component", referenceInterface=Object.class, target="(javax.ws.rs=true)", 
		cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE, policy=ReferencePolicy.DYNAMIC)
@Service(WinkRequestProcessor.class)
public class WinkRequestProcessor {

	private final static Logger log = LoggerFactory.getLogger(WinkRequestProcessor.class);
	private RequestProcessor requestProcessor;
	private Set<Object> components = new HashSet<Object>();	
	private boolean requestProcessorOutdated = false;
	
	/**
     * Dispatches the request and fills the response (even with an error
     * message.
     * 
     * @param request AS or mock request
     * @param response AS or mock response
     */
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException {
		ensureNotOutdated();
		requestProcessor.handleRequest(request, response);
	}
	
	/**
     * Dispatches the request and fills the response (even with an error
     * message using the spcified root resource instance. Note that any
     * @Path annotation on the root resource class as well as methods with
     * auch an annotation are ignored.
     * 
     * @param request AS or mock request
     * @param response AS or mock response
     * @param resource the root resource instance to be used
     */
	public void handleRequest(HttpServletRequest request, HttpServletResponse response, 
			Object resource) 
			throws ServletException {
		ensureNotOutdated();
		ThreadRootResourceDeploymentConfiguration.threadLocalRootResource.set(resource);
		requestProcessor.handleRequest(request, response);
		ThreadRootResourceDeploymentConfiguration.threadLocalRootResource.remove();
	}

	protected void activate(ComponentContext context) {
		// hint from http://www.amdatu.org/confluence/display/Amdatu/OSGiification
		// dummy RuntimeDelegate
		RuntimeDelegate.setInstance(new DummyRuntimeDelegate());
		RuntimeDelegate.setInstance(new RuntimeDelegateImpl());	
		init();
	}
	
	
	protected void deactivate(ComponentContext context) {
		components.clear();
		requestProcessor = null;
		requestProcessorOutdated = false;
	}
	
	/**
	 * @param component
	 *            The new JAX-RS component (root resource or provider) to bind.
	 */
	public void bindComponent(Object component) {
		if (requestProcessor != null) {
			ensureNotOutdated();
			registerComponent(component);
		}
		//has to be called after ensureUpdate as endureUpdate might cause reinitialization
		//based on components
		synchronized (this) {
			components.add(component);
		}
	}
	
	/**
	 * @param component
	 *            The new JAX-RS component (root resource or provider) to bind.
	 */
	public synchronized void unbindComponent(Object component) {
		components.remove(component);
		//since it seems not possible to remove from RequestProcessor
		//we need to create a new one, we defer this to prevent
		//creation of many new RequestProcessors when unregistering many resources
		requestProcessorOutdated = true;
	}
	
	/**
	 * Used in JaxRsFilter to check if the path can be handled by wink
	 */
	RequestProcessor getRequestProcessor() {
		ensureNotOutdated();
		return requestProcessor;
	}
	
	private void ensureNotOutdated() {
		if (requestProcessorOutdated) {
			synchronized(this) {
				if (requestProcessorOutdated) {
					init();
					requestProcessorOutdated = false;
				}
			}
		}
	}
	
	private void init() {
		ThreadRootResourceDeploymentConfiguration configuration = new ThreadRootResourceDeploymentConfiguration();
		configuration.init();
		configuration.getProperties().setProperty("wink.rootResource", "none");
		requestProcessor = new RequestProcessor(configuration);
		for (Object component : components) {
			registerComponent(component);
		}
	}

	private void registerComponent(Object component) {
		Application application = new InnerApplication(component);
		requestProcessor.getConfiguration().addApplication(application, false);
		//FIXME: fix this to comply with externalization requirements
		//log.info("registered component {}", component);
	}

	

}
