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

package org.apache.wink.server.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.WinkApplication;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.server.internal.application.ServletApplicationFileLoader;
import org.apache.wink.server.internal.handlers.SearchResult;
import org.apache.wink.server.internal.handlers.ServerMessageContext;
import org.apache.wink.server.internal.registry.ResourceInstance;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.apache.wink.server.internal.resources.HtmlServiceDocumentResource;
import org.apache.wink.server.internal.resources.RootResource;
import org.apache.wink.server.utils.RegistrationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for request processing.
 */
public class RequestProcessor {

    private static final Logger           logger                           =
                                                                               LoggerFactory
                                                                                   .getLogger(RequestProcessor.class);
    private static final String           PROPERTY_ROOT_RESOURCE_NONE      = "none"; //$NON-NLS-1$
    private static final String           PROPERTY_ROOT_RESOURCE_ATOM      = "atom"; //$NON-NLS-1$
    private static final String           PROPERTY_ROOT_RESOURCE_ATOM_HTML = "atom+html"; //$NON-NLS-1$
    private static final String           PROPERTY_ROOT_RESOURCE_DEFAULT   =
                                                                               PROPERTY_ROOT_RESOURCE_ATOM_HTML;
    private static final String           PROPERTY_ROOT_RESOURCE           = "wink.rootResource"; //$NON-NLS-1$
    private static final String           PROPERTY_ROOT_RESOURCE_CSS       =
                                                                               "wink.serviceDocumentCssPath"; //$NON-NLS-1$
    private static final String           PROPERTY_LOAD_WINK_APPLICATIONS  =
                                                                               "wink.loadApplications"; //$NON-NLS-1$

    private final DeploymentConfiguration configuration;
    
    private String requestString;  // save off the request string in case we need to log it
    private String requestMethod;  // save off the request method in case we need to log it

    public RequestProcessor(DeploymentConfiguration configuration) {
        this.configuration = configuration;
        registerDefaultApplication();
        registerRootResources();
    }

    private void registerDefaultApplication() {
        try {
            String loadWinkApplicationsProperty =
                configuration.getProperties().getProperty(PROPERTY_LOAD_WINK_APPLICATIONS,
                                                          Boolean.toString(true));
            logger.trace("{} property is set to: {}", //$NON-NLS-1$
                         PROPERTY_LOAD_WINK_APPLICATIONS,
                         loadWinkApplicationsProperty);
            final Set<Class<?>> classes =
                new ServletApplicationFileLoader(Boolean.parseBoolean(loadWinkApplicationsProperty))
                    .getClasses();
            RegistrationUtils.InnerApplication application =
                new RegistrationUtils.InnerApplication(classes);
            application.setPriority(WinkApplication.SYSTEM_PRIORITY);
            configuration.addApplication(application, true);
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(e);
        }
    }

    private void registerRootResources() {
        Properties properties = configuration.getProperties();
        String registerRootResource =
            properties.getProperty(PROPERTY_ROOT_RESOURCE, PROPERTY_ROOT_RESOURCE_DEFAULT);
        logger.trace("{} property is set to: {}", PROPERTY_ROOT_RESOURCE, registerRootResource); //$NON-NLS-1$
        if (registerRootResource.equals(PROPERTY_ROOT_RESOURCE_ATOM)) {
            RegistrationUtils.InnerApplication application =
                new RegistrationUtils.InnerApplication(RootResource.class);
            application.setPriority(WinkApplication.SYSTEM_PRIORITY);
            configuration.addApplication(application, true);
        } else if (registerRootResource.equals(PROPERTY_ROOT_RESOURCE_NONE)) {
            // do nothing
        } else {
            String css = properties.getProperty(PROPERTY_ROOT_RESOURCE_CSS);
            logger.trace("{} property is set to: {}", PROPERTY_ROOT_RESOURCE_CSS, css); //$NON-NLS-1$
            HtmlServiceDocumentResource instance = new HtmlServiceDocumentResource();
            if (css != null) {
                instance.setServiceDocumentCssPath(css);
            }
            RegistrationUtils.InnerApplication application =
                new RegistrationUtils.InnerApplication(instance);
            application.setPriority(WinkApplication.SYSTEM_PRIORITY);
            configuration.addApplication(application, true);
        }
    }

    // --- request processing ---

    /**
     * Dispatches the request and fills the response (even with an error
     * message.
     * 
     * @param request AS or mock request
     * @param response AS or mock response
     * @throws IOException I/O error
     */
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        try {
            requestMethod = request.getMethod();
            requestString = request.getRequestURL().toString();
            requestString += ((request.getQueryString() != null && request.getQueryString().length() > 0) ? ("?" + request.getQueryString()) : ""); //$NON-NLS-1$ $NON-NLS-2$
            if (logger.isDebugEnabled()) {
                logger.debug(Messages.getMessage("processingRequestTo", requestMethod, requestString, request.getContentType(), request.getHeader("Accept"))); //$NON-NLS-1$ $NON-NLS-2$
            }
            handleRequestWithoutFaultBarrier(request, response);
        } catch (Throwable t) {
            // exception was not handled properly
            if (logger.isTraceEnabled()) {
                logger.trace(Messages.getMessage("unhandledExceptionToContainer"), t); //$NON-NLS-1$
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info(Messages.getMessage("unhandledExceptionToContainer")); //$NON-NLS-1$
                }
            }
            if (t instanceof RuntimeException) {
                // let the servlet container to handle the runtime exception
                throw (RuntimeException)t;
            }
            throw new ServletException(t);
        }
    }

    private void handleRequestWithoutFaultBarrier(HttpServletRequest request,
                                                  HttpServletResponse response) throws Throwable {
        boolean isReleaseResourcesCalled = false;
        try {
            ServerMessageContext msgContext = createMessageContext(request, response);
            RuntimeContextTLS.setRuntimeContext(msgContext);
            logger.trace("Set message context and starting request handlers chain: {}", msgContext); //$NON-NLS-1$
            // run the request handler chain
            configuration.getRequestHandlersChain().run(msgContext);
            logger
                .trace("Finished request handlers chain and starting response handlers chain: {}", //$NON-NLS-1$
                       msgContext);
            // run the response handler chain
            configuration.getResponseHandlersChain().run(msgContext);

            logger.trace("Attempting to release resource instance");
            isReleaseResourcesCalled = true;
            try {
                releaseResources(msgContext);
            } catch (Exception e) {
                logger.trace("Caught exception when releasing resource object", e);
                throw e;
            }
        } catch (Throwable t) {
            RuntimeContext originalContext = RuntimeContextTLS.getRuntimeContext();
            ServerMessageContext msgContext = null;
            try {
                logException(t);
                msgContext = createMessageContext(request, response);
                RuntimeContextTLS.setRuntimeContext(msgContext);
                msgContext.setResponseEntity(t);
                // run the error handler chain
                logger.trace("Exception occured, starting error handlers chain: {}", msgContext); //$NON-NLS-1$
                configuration.getErrorHandlersChain().run(msgContext);

                RuntimeContextTLS.setRuntimeContext(originalContext);
                if (!isReleaseResourcesCalled) {
                    isReleaseResourcesCalled = true;
                    try {
                        releaseResources(originalContext);
                    } catch (Exception e2) {
                        logger.trace("Caught exception when releasing resource object", e2);
                    }
                }
            } catch (Exception e) {
                RuntimeContextTLS.setRuntimeContext(originalContext);
                if (!isReleaseResourcesCalled) {
                    isReleaseResourcesCalled = true;
                    try {
                        releaseResources(originalContext);
                    } catch (Exception e2) {
                        logger.trace("Caught exception when releasing resource object", e2);
                    }
                }
                throw e;
            }
        } finally {
            logger.trace("Finished response handlers chain"); //$NON-NLS-1$
            RuntimeContextTLS.setRuntimeContext(null);
        }
    }

    private void releaseResources(RuntimeContext msgContext) throws Exception {
        SearchResult searchResult = msgContext.getAttribute(SearchResult.class);
        if (searchResult != null) {
            List<ResourceInstance> resourceInstances = searchResult.getData().getMatchedResources();
            for (ResourceInstance res : resourceInstances) {
                logger.trace("Releasing resource instance");
                res.releaseInstance(msgContext);
            }
        }
    }

    private void logException(Throwable t) {
        String messageFormat;
        if (t instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException)t;
            int statusCode = wae.getResponse().getStatus();
            Status status = Response.Status.fromStatusCode(statusCode);
            String statusSep = ""; //$NON-NLS-1$
            String statusMessage = ""; //$NON-NLS-1$
            if (status != null) {
                statusSep = " - "; //$NON-NLS-1$
                statusMessage = status.toString();
            }
            String exceptionName =
                String.format("%s (%d%s%s)", t.getClass().getSimpleName(), statusCode, statusSep, statusMessage); //$NON-NLS-1$
            messageFormat = Messages.getMessage("exceptionOccurredDuringInvocation", exceptionName, requestMethod, requestString); //$NON-NLS-1$
        } else {
            messageFormat = Messages.getMessage("exceptionOccurredDuringInvocation", t.getClass().getSimpleName(), requestMethod, requestString); //$NON-NLS-1$
        }
        if (logger.isDebugEnabled()) {
            logger.debug(messageFormat, t);
            ResourceRegistry rr = this.configuration.getResourceRegistry();
            List<ResourceRecord> resourceRecords = rr.getRecords();
            StringBuffer sb = new StringBuffer();
            for (ResourceRecord record : resourceRecords) {
                sb.append("\n  " + record.toString()); //$NON-NLS-1$
            }
            logger.debug(Messages.getMessage("registeredResources", (sb.toString().length() > 0) ? sb.toString() : "{}")); //$NON-NLS-1$ $NON-NLS-2$
            logger.debug(this.configuration.getProvidersRegistry().getLogFormattedProvidersList(true));
        } else {
            logger.info(messageFormat);
        }
    }

    private ServerMessageContext createMessageContext(HttpServletRequest request,   
                                                      HttpServletResponse response) {
        ServerMessageContext messageContext =
            new ServerMessageContext(request, response, configuration);
        return messageContext;
    }

    public DeploymentConfiguration getConfiguration() {
        return configuration;
    }

    public static RequestProcessor getRequestProcessor(ServletContext servletContext,
                                                       String attributeName) {
        if (attributeName == null) {
            attributeName = RequestProcessor.class.getName();
        }
        RequestProcessor requestProcessor =
            (RequestProcessor)servletContext.getAttribute(attributeName);
        logger
            .trace("Retrieving request processor {} using attribute name {} in servlet context {}", //$NON-NLS-1$
                   new Object[] {requestProcessor, attributeName, servletContext});
        return requestProcessor;
    }

    public void storeRequestProcessorOnServletContext(ServletContext servletContext,
                                                      String attributeName) {
        if (attributeName == null || attributeName.length() == 0) {
            attributeName = RequestProcessor.class.getName();
        }
        logger.trace("Storing request processor {} using attribute name {} in servlet context {}", //$NON-NLS-1$
                     new Object[] {this, attributeName, servletContext});
        servletContext.setAttribute(attributeName, this);
    }
}
