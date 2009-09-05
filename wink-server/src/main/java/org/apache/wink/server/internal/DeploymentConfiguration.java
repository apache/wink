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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.lifecycle.ScopeLifecycleManager;
import org.apache.wink.common.internal.registry.InjectableFactory;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.utils.FileLoader;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.server.handlers.Handler;
import org.apache.wink.server.handlers.HandlersFactory;
import org.apache.wink.server.handlers.RequestHandler;
import org.apache.wink.server.handlers.RequestHandlersChain;
import org.apache.wink.server.handlers.ResponseHandler;
import org.apache.wink.server.handlers.ResponseHandlersChain;
import org.apache.wink.server.internal.application.ApplicationProcessor;
import org.apache.wink.server.internal.handlers.CheckLocationHeaderHandler;
import org.apache.wink.server.internal.handlers.CreateInvocationParametersHandler;
import org.apache.wink.server.internal.handlers.FindResourceMethodHandler;
import org.apache.wink.server.internal.handlers.FindRootResourceHandler;
import org.apache.wink.server.internal.handlers.FlushResultHandler;
import org.apache.wink.server.internal.handlers.HeadMethodHandler;
import org.apache.wink.server.internal.handlers.InvokeMethodHandler;
import org.apache.wink.server.internal.handlers.OptionsMethodHandler;
import org.apache.wink.server.internal.handlers.PopulateErrorResponseHandler;
import org.apache.wink.server.internal.handlers.PopulateResponseMediaTypeHandler;
import org.apache.wink.server.internal.handlers.PopulateResponseStatusHandler;
import org.apache.wink.server.internal.handlers.SearchResultHandler;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.apache.wink.server.internal.registry.ServerInjectableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class implements a default deployment configuration for Wink. In order
 * to change this configuration, extend this class and override the relevant
 * methods. In general it's possible to override any methods of this class, but
 * the best practices are to override methods the "init" methods. See the
 * javadoc for each method for more details.
 */
public class DeploymentConfiguration {

    private static final Logger       logger                            =
                                                                            LoggerFactory
                                                                                .getLogger(DeploymentConfiguration.class);
    private static final String       ALTERNATIVE_SHORTCUTS             =
                                                                            "META-INF/wink-alternate-shortcuts.properties";
    private static final String       HTTP_METHOD_OVERRIDE_HEADERS_PROP =
                                                                            "wink.httpMethodOverrideHeaders";
    private static final String       HANDLERS_FACTORY_CLASS_PROP       =
                                                                            "wink.handlersFactoryClass";
    private static final String       VALIDATE_LOCATION_HEADER          =
                                                                            "wink.validateLocationHeader";
    // handler chains
    private RequestHandlersChain      requestHandlersChain;
    private ResponseHandlersChain     responseHandlersChain;
    private ResponseHandlersChain     errorHandlersChain;

    private List<RequestHandler>      requestUserHandlers;
    private List<ResponseHandler>     responseUserHandlers;
    private List<ResponseHandler>     errorUserHandlers;

    // registries
    private ProvidersRegistry         providersRegistry;
    private ResourceRegistry          resourceRegistry;
    private LifecycleManagersRegistry ofFactoryRegistry;

    // mappers
    private MediaTypeMapper           mediaTypeMapper;
    private Map<String, String>       alternateShortcutMap;

    // external properties
    private Properties                properties;

    // servlet configuration
    private ServletConfig             servletConfig;
    private ServletContext            servletContext;
    private FilterConfig              filterConfig;

    private String[]                  httpMethodOverrideHeaders;

    /**
     * Makes sure that the object was properly initialized. Should be invoked
     * AFTER all the setters were invoked.
     */
    public void init() {
        if (properties == null) {
            properties = new Properties();
        }

        // check to see if an override property was specified. if so, then
        // configure
        // the headers from there using a comma delimited string.
        String httpMethodOverrideHeadersProperty =
            properties.getProperty(HTTP_METHOD_OVERRIDE_HEADERS_PROP);
        httpMethodOverrideHeaders =
            (httpMethodOverrideHeadersProperty != null && httpMethodOverrideHeadersProperty
                .length() > 0) ? httpMethodOverrideHeadersProperty.split(",") : null;

        initRegistries();
        initAlternateShortcutMap();
        initMediaTypeMapper();
        initHandlers();
    }

    public RequestHandlersChain getRequestHandlersChain() {
        return requestHandlersChain;
    }

    public void setRequestHandlersChain(RequestHandlersChain requestHandlersChain) {
        this.requestHandlersChain = requestHandlersChain;
    }

    public ResponseHandlersChain getResponseHandlersChain() {
        return responseHandlersChain;
    }

    public void setResponseHandlersChain(ResponseHandlersChain responseHandlersChain) {
        this.responseHandlersChain = responseHandlersChain;
    }

    public ResponseHandlersChain getErrorHandlersChain() {
        return errorHandlersChain;
    }

    public void setErrorHandlersChain(ResponseHandlersChain errorHandlersChain) {
        this.errorHandlersChain = errorHandlersChain;
    }

    public ProvidersRegistry getProvidersRegistry() {
        return providersRegistry;
    }

    public ResourceRegistry getResourceRegistry() {
        return resourceRegistry;
    }

    public MediaTypeMapper getMediaTypeMapper() {
        return mediaTypeMapper;
    }

    public void setMediaTypeMapper(MediaTypeMapper mediaTypeMapper) {
        this.mediaTypeMapper = mediaTypeMapper;
    }

    public void setOfFactoryRegistry(LifecycleManagersRegistry ofFactoryRegistry) {
        this.ofFactoryRegistry = ofFactoryRegistry;
    }

    public LifecycleManagersRegistry getOfFactoryRegistry() {
        return ofFactoryRegistry;
    }

    public Map<String, String> getAlternateShortcutMap() {
        return alternateShortcutMap;
    }

    public void setAlternateShortcutMap(Map<String, String> alternateShortcutMap) {
        this.alternateShortcutMap = alternateShortcutMap;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setRequestUserHandlers(List<RequestHandler> requestUserHandlers) {
        this.requestUserHandlers = requestUserHandlers;
    }

    public void setResponseUserHandlers(List<ResponseHandler> responseUserHandlers) {
        this.responseUserHandlers = responseUserHandlers;
    }

    public List<? extends ResponseHandler> getResponseUserHandlers() {
        return responseUserHandlers;
    }

    public List<? extends RequestHandler> getRequestUserHandlers() {
        return requestUserHandlers;
    }

    public void setErrorUserHandlers(List<ResponseHandler> errorUserHandlers) {
        this.errorUserHandlers = errorUserHandlers;
    }

    public List<? extends ResponseHandler> getErrorUserHandlers() {
        return errorUserHandlers;
    }

    public void addApplication(Application application) {
        new ApplicationProcessor(application, resourceRegistry, providersRegistry).process();
    }

    // init methods

    /**
     * Initializes registries. Usually there should be no need to override this
     * method. When creating Resources or Providers registry, ensure that they
     * use the same instance of the ApplicationValidator.
     */
    protected void initRegistries() {
        InjectableFactory.setInstance(new ServerInjectableFactory());
        if (ofFactoryRegistry == null) {
            ofFactoryRegistry = new LifecycleManagersRegistry();
            ofFactoryRegistry.addFactoryFactory(new ScopeLifecycleManager<Object>());
        }
        ApplicationValidator applicationValidator = new ApplicationValidator();
        providersRegistry = new ProvidersRegistry(ofFactoryRegistry, applicationValidator);
        resourceRegistry = new ResourceRegistry(ofFactoryRegistry, applicationValidator);
    }

    /**
     * Initializes the AlternateShortcutMap. Override this method in order to
     * provide a custom AlternateShortcutMap.
     */
    protected void initAlternateShortcutMap() {
        if (alternateShortcutMap == null) {
            InputStream is = null;
            try {
                is = FileLoader.loadFileAsStream(ALTERNATIVE_SHORTCUTS);
                Properties lproperties = new Properties();

                lproperties.load(is);
                alternateShortcutMap = new HashMap<String, String>();
                for (Entry<Object, Object> entry : lproperties.entrySet()) {
                    alternateShortcutMap.put((String)entry.getKey(), (String)entry.getValue());
                }
            } catch (IOException e) {
                logger.error(Messages.getMessage("alternateShortcutMapLoadFailure"), e);
                throw new WebApplicationException(e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    logger
                        .info(Messages.getMessage("alternateShortcutMapCloseFailure") + ALTERNATIVE_SHORTCUTS,
                              e);
                }
            }
        }
    }

    /**
     * Initializes the MediaTypeMapper. Override it to provide a custom
     * MediaTypeMapper.
     */
    protected void initMediaTypeMapper() {
        if (mediaTypeMapper == null) {
            mediaTypeMapper = new MediaTypeMapper();
            mediaTypeMapper.addMapping("Mozilla/",
                                       MediaType.APPLICATION_ATOM_XML,
                                       MediaType.TEXT_XML);
            mediaTypeMapper.addMapping("Mozilla/",
                                       MediaTypeUtils.ATOM_SERVICE_DOCUMENT,
                                       MediaType.TEXT_XML);
            mediaTypeMapper.addMapping("Mozilla/", MediaTypeUtils.OPENSEARCH, MediaType.TEXT_XML);
        }
    }

    /**
     * Initializes the main handlers chain. Override in order to change the
     * chains.
     */
    @SuppressWarnings("unchecked")
    private void initHandlers() {

        String handlersFactoryClassName = properties.getProperty(HANDLERS_FACTORY_CLASS_PROP);
        if (handlersFactoryClassName != null) {
            try {
                Class<HandlersFactory> handlerFactoryClass =
                    (Class<HandlersFactory>)Class.forName(handlersFactoryClassName);
                HandlersFactory handlersFactory = handlerFactoryClass.newInstance();
                if (requestUserHandlers == null) {
                    requestUserHandlers =
                        (List<RequestHandler>)handlersFactory.getRequestHandlers();
                }
                if (responseUserHandlers == null) {
                    responseUserHandlers =
                        (List<ResponseHandler>)handlersFactory.getResponseHandlers();
                }
                if (errorUserHandlers == null) {
                    errorUserHandlers = (List<ResponseHandler>)handlersFactory.getErrorHandlers();
                }
            } catch (ClassNotFoundException e) {
                logger.error(Messages.getMessage("isNotAClass", handlersFactoryClassName), e);
            } catch (InstantiationException e) {
                logger.error(Messages.getMessage("classInstantiationException",
                                                 handlersFactoryClassName), e);
            } catch (IllegalAccessException e) {
                logger
                    .error(Messages.getMessage("classIllegalAccess", handlersFactoryClassName), e);
            }
        }

        if (requestUserHandlers == null) {
            requestUserHandlers = initRequestUserHandlers();
        }
        if (responseUserHandlers == null) {
            responseUserHandlers = initResponseUserHandlers();
        }
        if (errorUserHandlers == null) {
            errorUserHandlers = initErrorUserHandlers();
        }

        if (requestHandlersChain == null) {
            requestHandlersChain = initRequestHandlersChain();
        }
        if (responseHandlersChain == null) {
            responseHandlersChain = initResponseHandlersChain();
        }
        if (errorHandlersChain == null) {
            errorHandlersChain = initErrorHandlersChain();
        }
    }

    /**
     * Initializes the Request Handlers Chain (the chain that handles the
     * in-bound request). Usually the user won't need to override this method,
     * but <tt>initRequestUserHandlers</tt> instead.
     * 
     * @see initRequestUserHandlers
     */
    protected RequestHandlersChain initRequestHandlersChain() {
        RequestHandlersChain handlersChain = new RequestHandlersChain();
        handlersChain.addHandler(createHandler(SearchResultHandler.class));
        handlersChain.addHandler(createHandler(OptionsMethodHandler.class));
        handlersChain.addHandler(createHandler(HeadMethodHandler.class));
        handlersChain.addHandler(createHandler(FindRootResourceHandler.class));
        handlersChain.addHandler(createHandler(FindResourceMethodHandler.class));
        handlersChain.addHandler(createHandler(CreateInvocationParametersHandler.class));
        if (requestUserHandlers != null) {
            for (RequestHandler h : requestUserHandlers) {
                h.init(properties);
                handlersChain.addHandler(h);
            }
        }
        handlersChain.addHandler(createHandler(InvokeMethodHandler.class));
        return handlersChain;
    }

    /**
     * Initializes request (inbound) user handler. By default this method
     * returns an empty list. Override to add user handlers.
     * 
     * @return list of RequestHandler
     * @see RequestHandler
     */
    protected List<RequestHandler> initRequestUserHandlers() {
        return Collections.emptyList();
    }

    /**
     * Initializes response (outbound) user handlers. By default this method
     * returns an empty list. Override to add user handlers.
     * 
     * @return list of ResponseHandler
     * @see ResponseHandler
     */
    protected List<ResponseHandler> initResponseUserHandlers() {
        ArrayList<ResponseHandler> list = new ArrayList<ResponseHandler>(1);
        if (Boolean.parseBoolean(properties.getProperty(VALIDATE_LOCATION_HEADER))) {
            list.add(new CheckLocationHeaderHandler());
        }
        return list;

    }

    /**
     * Initializes error user handlers.By default this method returns an empty
     * list. Override to add user handlers.
     * 
     * @return list of ResponseHandler
     * @see ResponseHandler
     */
    protected List<ResponseHandler> initErrorUserHandlers() {
        return Collections.emptyList();
    }

    /**
     * Initializes the Response Handlers Chain (the chain that handles the
     * out-bound response). Usually the user won't need to override this method,
     * but <tt>initResponseUserHandlers</tt> instead.
     */
    protected ResponseHandlersChain initResponseHandlersChain() {
        ResponseHandlersChain handlersChain = new ResponseHandlersChain();
        handlersChain.addHandler(createHandler(PopulateResponseStatusHandler.class));
        handlersChain.addHandler(createHandler(PopulateResponseMediaTypeHandler.class));
        if (responseUserHandlers != null) {
            for (ResponseHandler h : responseUserHandlers) {
                h.init(properties);
                handlersChain.addHandler(h);
            }
        }
        handlersChain.addHandler(createHandler(FlushResultHandler.class));
        handlersChain.addHandler(createHandler(HeadMethodHandler.class));
        return handlersChain;
    }

    /**
     * Initializes the Error Handlers Chain (the chain that handles the
     * exceptions). Usually the user won't need to override this method, but
     * <tt>initErrorUserHandlers</tt> instead.
     */
    protected ResponseHandlersChain initErrorHandlersChain() {
        ResponseHandlersChain handlersChain = new ResponseHandlersChain();
        handlersChain.addHandler(createHandler(PopulateErrorResponseHandler.class));
        handlersChain.addHandler(createHandler(PopulateResponseStatusHandler.class));
        PopulateResponseMediaTypeHandler populateMediaTypeHandler =
            createHandler(PopulateResponseMediaTypeHandler.class);
        populateMediaTypeHandler.setErrorFlow(true);
        handlersChain.addHandler(populateMediaTypeHandler);
        if (errorUserHandlers != null) {
            for (ResponseHandler h : errorUserHandlers) {
                h.init(properties);
                handlersChain.addHandler(h);
            }
        }
        handlersChain.addHandler(createHandler(FlushResultHandler.class));
        return handlersChain;
    }

    private <T extends Handler> T createHandler(Class<T> cls) {
        try {
            T handler = cls.newInstance();
            handler.init(getProperties());
            return handler;
        } catch (InstantiationException e) {
            throw new WebApplicationException(e);
        } catch (IllegalAccessException e) {
            throw new WebApplicationException(e);
        }
    }

    public void setHttpMethodOverrideHeaders(String[] httpMethodOverrideHeaders) {
        this.httpMethodOverrideHeaders = httpMethodOverrideHeaders;
    }

    public String[] getHttpMethodOverrideHeaders() {
        return httpMethodOverrideHeaders;
    }

}
