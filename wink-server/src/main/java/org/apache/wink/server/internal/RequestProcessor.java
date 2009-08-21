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
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.internal.application.ApplicationFileLoader;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.server.internal.handlers.ServerMessageContext;
import org.apache.wink.server.internal.resources.HtmlServiceDocumentResource;
import org.apache.wink.server.internal.resources.RootResource;
import org.apache.wink.server.utils.RegistrationUtils;

/**
 * Responsible for request processing.
 */
public class RequestProcessor {

    private static final Logger           logger                           =
                                                                               LoggerFactory
                                                                                   .getLogger(RequestProcessor.class);
    private static final String           PROPERTY_ROOT_RESOURCE_NONE      = "none";
    private static final String           PROPERTY_ROOT_RESOURCE_ATOM      = "atom";
    private static final String           PROPERTY_ROOT_RESOURCE_ATOM_HTML = "atom+html";
    private static final String           PROPERTY_ROOT_RESOURCE_DEFAULT   =
                                                                               PROPERTY_ROOT_RESOURCE_ATOM_HTML;
    private static final String           PROPERTY_ROOT_RESOURCE           =
                                                                               "wink.rootResource";
    private static final String           PROPERTY_ROOT_RESOURCE_CSS       =
                                                                               "wink.serviceDocumentCssPath";

    private final DeploymentConfiguration configuration;

    public RequestProcessor(DeploymentConfiguration configuration) {
        this.configuration = configuration;
        registerDefaultApplication();
        registerRootResources();
    }

    private void registerDefaultApplication() {
        try {
            final Set<Class<?>> classes = new ApplicationFileLoader().getClasses();
            configuration.addApplication(new RegistrationUtils.InnerApplication(classes));
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(e);
        }
    }

    private void registerRootResources() {
        Properties properties = configuration.getProperties();
        String registerRootResource =
            properties.getProperty(PROPERTY_ROOT_RESOURCE, PROPERTY_ROOT_RESOURCE_DEFAULT);
        if (registerRootResource.equals(PROPERTY_ROOT_RESOURCE_ATOM)) {
            RegistrationUtils.InnerApplication application =
                new RegistrationUtils.InnerApplication(RootResource.class);
            application.setPriority(0.1);
            configuration.addApplication(application);
        } else if (registerRootResource.equals(PROPERTY_ROOT_RESOURCE_NONE)) {
            // do nothing
        } else {
            String css = properties.getProperty(PROPERTY_ROOT_RESOURCE_CSS);
            HtmlServiceDocumentResource instance = new HtmlServiceDocumentResource();
            if (css != null) {
                instance.setServiceDocumentCssPath(css);
            }
            RegistrationUtils.InnerApplication application =
                new RegistrationUtils.InnerApplication(instance);
            application.setPriority(0.1);
            configuration.addApplication(application);
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
            handleRequestWithoutFaultBarrier(request, response);
        } catch (Throwable t) {
            // exception was not handled properly
            logger.error("Unhandled exception", t);
            if (t instanceof RuntimeException) {
                // let the servlet container to handle the runtime exception
                throw (RuntimeException)t;
            }
            throw new ServletException(t);
        }
    }

    private void handleRequestWithoutFaultBarrier(HttpServletRequest request,
                                                  HttpServletResponse response) throws Throwable {

        try {
            ServerMessageContext msgContext = createMessageContext(request, response);
            RuntimeContextTLS.setRuntimeContext(msgContext);
            // run the request handler chain
            configuration.getRequestHandlersChain().run(msgContext);
            // run the response handler chain
            configuration.getResponseHandlersChain().run(msgContext);
        } catch (Throwable t) {
            logException(t);
            ServerMessageContext msgContext = createMessageContext(request, response);
            RuntimeContextTLS.setRuntimeContext(msgContext);
            msgContext.setResponseEntity(t);
            // run the error handler chain
            configuration.getErrorHandlersChain().run(msgContext);
        } finally {
            RuntimeContextTLS.setRuntimeContext(null);
        }
    }

    private void logException(Throwable t) {
        String messageFormat = "%s occured during the handlers chain invocation";
        String exceptionName = t.getClass().getSimpleName();
        if (t instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException)t;
            int statusCode = wae.getResponse().getStatus();
            Status status = Response.Status.fromStatusCode(statusCode);
            String statusSep = "";
            String statusMessage = "";
            if (status != null) {
                statusSep = " - ";
                statusMessage = status.toString();
            }
            exceptionName =
                String.format("%s (%d%s%s)", exceptionName, statusCode, statusSep, statusMessage);
            if (statusCode >= 500) {
                logger.error(String.format(messageFormat, exceptionName), t);
            } else {
                logger.info(String.format(messageFormat, exceptionName), t);
            }
        } else {
            logger.error(String.format(messageFormat, exceptionName), t);
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
        return (RequestProcessor)servletContext.getAttribute(attributeName);
    }

    public void storeRequestProcessorOnServletContext(ServletContext servletContext,
                                                      String attributeName) {
        if (attributeName == null || attributeName.length() == 0) {
            attributeName = RequestProcessor.class.getName();
        }
        servletContext.setAttribute(attributeName, this);
    }

}
