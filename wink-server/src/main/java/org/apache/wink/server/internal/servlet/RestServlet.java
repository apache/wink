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
package org.apache.wink.server.internal.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.ObjectFactory;
import org.apache.wink.common.internal.properties.WinkSystemProperties;
import org.apache.wink.common.internal.utils.ClassUtils;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.RequestProcessor;
import org.apache.wink.server.internal.application.ServletWinkApplication;
import org.apache.wink.server.internal.log.Providers;
import org.apache.wink.server.internal.log.Resources;
import org.apache.wink.server.internal.utils.ServletFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Main servlet that is used by the runtime to handle the incoming request.
 * <p>
 * The init-params are supported:
 * <ul>
 * <li><b>propertiesLocation</b> - custom properties file</li>
 * <li><b>applicationConfigLocation</b> - locations of flat application
 * configuration files. Relevant only if the no
 * <tt>javax.ws.rs.core.Application</tt> is provided.</li>
 * <li><b>deploymentConfiguration</b> - custom deployment configuration class
 * name. The deployment configuration must extend
 * <tt>org.apache.wink.server.internal.DeploymentConfiguration</tt>.</li>
 * </ul>
 * <p>
 * <b>Important! The init-params are relevant only when working without the
 * Spring support module.</b> When working with Spring, the
 * <tt>org.springframework.web.context.ContextLoaderListener</tt> must be
 * configured and the whole customization should occur via the spring context.
 */
public class RestServlet extends AbstractRestServlet {

    private static final long   serialVersionUID        = 8797036173835816706L;

    private static final Logger logger                  =
                                                            LoggerFactory
                                                                .getLogger(RestServlet.class);

    public static final String  APPLICATION_INIT_PARAM  = "javax.ws.rs.Application";          //$NON-NLS-1$
    public static final String  PROPERTIES_DEFAULT_FILE = "META-INF/wink-default.properties"; //$NON-NLS-1$
    public static final String  PROPERTIES_INIT_PARAM   = "propertiesLocation";               //$NON-NLS-1$
    public static final String  APP_LOCATION_PARAM      = "applicationConfigLocation";        //$NON-NLS-1$
    public static final String  DEPLOYMENT_CONF_PARAM   = "deploymentConfiguration";          //$NON-NLS-1$

    @Override
    public void init() throws ServletException {

        logger.trace("Initializing {} servlet", this); //$NON-NLS-1$

        try {
            super.init();
            // try to get the request processor
            // the request processor can be available if it was loaded by a
            // listener
            // or when working with Spring
            RequestProcessor requestProcessor = getRequestProcessor();
            if (requestProcessor == null) {
                // create the request processor
                requestProcessor = createRequestProcessor();
                if (requestProcessor == null) {
                    throw new IllegalStateException(Messages
                        .getMessage("restServletRequestProcessorCouldNotBeCreated")); //$NON-NLS-1$
                }
                storeRequestProcessorOnServletContext(requestProcessor);
            }
            if (requestProcessor.getConfiguration().getServletConfig() == null) {
                requestProcessor.getConfiguration().setServletConfig(getServletConfig());
            }
            if (requestProcessor.getConfiguration().getServletContext() == null) {
                requestProcessor.getConfiguration().setServletContext(getServletContext());
            }
        } catch (Exception e) {
            // when exception occurs during the servlet initialization
            // it should be marked as unavailable
            logger.error(e.getMessage(), e);
            throw new UnavailableException(e.getMessage());
        }
    }

    @Override
    protected void service(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse) throws ServletException,
        IOException {
        getRequestProcessor().handleRequest(httpServletRequest, httpServletResponse);
    }

    protected RequestProcessor createRequestProcessor() throws ClassNotFoundException,
        InstantiationException, IllegalAccessException, IOException {
        DeploymentConfiguration deploymentConfiguration = getDeploymentConfiguration();
        // order of next two lines is important to allow Application to have
        // control over priority order of Providers
        Application app = getApplication();
        if (app == null) {
            app = getApplication(deploymentConfiguration);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Setting application to " + app.toString());
        }
        deploymentConfiguration.addApplication(app, false);

        if (!LoggerFactory.getLogger(Resources.class).isTraceEnabled()) {
            /*
             * if just debug or higher is enabled, then log only user
             * applications
             */
            new Resources(deploymentConfiguration.getResourceRegistry()).log();
        }

        if (!LoggerFactory.getLogger(Providers.class).isTraceEnabled()) {
            /*
             * if just debug or higher is enabled, then log only user
             * applications
             */
            new Providers(deploymentConfiguration.getProvidersRegistry()).log();
        }

        RequestProcessor requestProcessor = new RequestProcessor(deploymentConfiguration);
        logger.trace("Creating request processor {} for servlet {}", requestProcessor, this); //$NON-NLS-1$

        if (LoggerFactory.getLogger(Resources.class).isTraceEnabled()) {
            /*
             * if full trace is enabled, then log everything
             */

            new Resources(deploymentConfiguration.getResourceRegistry()).log();
        }

        if (LoggerFactory.getLogger(Providers.class).isTraceEnabled()) {
            /*
             * if full trace is enabled, then log everything
             */
            new Providers(deploymentConfiguration.getProvidersRegistry()).log();
        }

        return requestProcessor;
    }

    protected DeploymentConfiguration getDeploymentConfiguration() throws ClassNotFoundException,
        InstantiationException, IllegalAccessException, IOException {
        DeploymentConfiguration deploymentConfiguration = createDeploymentConfiguration();
        deploymentConfiguration.setServletConfig(getServletConfig());
        deploymentConfiguration.setServletContext(getServletContext());
        deploymentConfiguration.setProperties(getProperties());
        deploymentConfiguration.init();
        return deploymentConfiguration;
    }

    /**
     * order of loading and property precedence: wink-default.properties file
     * referred to by propertiesLocation init param (may override and add to
     * above set props) JVM system properties (only sets values for key/value
     * pairs where the value is null or empty)
     */
    protected Properties getProperties() throws IOException {
        Properties defaultProperties = loadProperties(PROPERTIES_DEFAULT_FILE, null);
        logger.trace("Default properties {} used in RestServlet {}", defaultProperties, this); //$NON-NLS-1$
        String propertiesLocation = getInitParameter(PROPERTIES_INIT_PARAM);
        if (propertiesLocation != null) {
            if (logger.isInfoEnabled()) {
                logger.info(Messages.getMessage("restServletUsePropertiesFileAtLocation", //$NON-NLS-1$
                                                propertiesLocation,
                                                PROPERTIES_INIT_PARAM));
            }

            // Load properties set on JVM. These should not override
            // the ones set in the configuration file.
            Properties properties = loadProperties(propertiesLocation, defaultProperties);
            properties.putAll(WinkSystemProperties.loadSystemProperties(properties));
            return properties;
        }
        logger.trace("Final properties {} used in RestServlet {}", defaultProperties, this); //$NON-NLS-1$

        // Load properties set on JVM. These should not override
        // the ones set in the configuration file.
        defaultProperties.putAll(WinkSystemProperties.loadSystemProperties(defaultProperties));

        return defaultProperties;
    }

    protected DeploymentConfiguration createDeploymentConfiguration()
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String initParameter = getInitParameter(DEPLOYMENT_CONF_PARAM);
        if (initParameter != null) {
            if (logger.isInfoEnabled()) {
                logger.info(Messages.getMessage("restServletUseDeploymentConfigurationParam", //$NON-NLS-1$
                                                initParameter,
                                                DEPLOYMENT_CONF_PARAM));
            }
            // use ClassUtils.loadClass instead of Class.forName so we have
            // classloader visibility into the Web module in J2EE environments
            Class<?> confClass = ClassUtils.loadClass(initParameter);
            return (DeploymentConfiguration)confClass.newInstance();
        }
        return new DeploymentConfiguration();
    }

    @SuppressWarnings("unchecked")
    protected Application getApplication(DeploymentConfiguration configuration)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<? extends Application> appClass = null;
        String initParameter = getInitParameter(APPLICATION_INIT_PARAM);
        if (initParameter != null) {
            if (logger.isInfoEnabled()) {
                logger.info(Messages.getMessage("restServletJAXRSApplicationInitParam", //$NON-NLS-1$
                                                initParameter,
                                                APPLICATION_INIT_PARAM));
            }
            // use ClassUtils.loadClass instead of Class.forName so we have
            // classloader visibility into the Web module in J2EE environments
            appClass = (Class<? extends Application>)ClassUtils.loadClass(initParameter);

            // let the lifecycle manager create the instance and process fields
            // for injection
            ObjectFactory of = configuration.getOfFactoryRegistry().getObjectFactory(appClass);
            configuration.addApplicationObjectFactory(of);

            return (Application)of.getInstance(null);
        }
        String appLocationParameter = getInitParameter(APP_LOCATION_PARAM);
        if (appLocationParameter == null) {
            if (logger.isWarnEnabled()) {
                logger.warn(Messages.getMessage("propertyNotDefined", APP_LOCATION_PARAM)); //$NON-NLS-1$
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info(Messages.getMessage("restServletWinkApplicationInitParam", //$NON-NLS-1$
                                            appLocationParameter,
                                            APP_LOCATION_PARAM));
        }
        return new ServletWinkApplication(getServletContext(), appLocationParameter);
    }

    protected Application getApplication() throws ClassNotFoundException, InstantiationException,
        IllegalAccessException {
        /*
         * this is a legacy call. in the end, should call
         * getApplication(DeploymentConfiguration) by default but this is left
         * as a call for legacy
         */
        return null;
    }

    /**
     * loadProperties will try to load the properties from the resource,
     * overriding existing properties in defaultProperties, and adding new ones,
     * and return the result
     * 
     * @param resourceName
     * @param defaultProperties
     * @return
     * @throws IOException
     */
    private Properties loadProperties(String resourceName, Properties defaultProperties)
        throws IOException {
        Properties properties =
            defaultProperties == null ? new Properties() : new Properties(defaultProperties);

        InputStream is = null;
        try {
            is = ServletFileLoader.loadFileAsStream(getServletContext(), resourceName);
            properties.load(is);
        } catch (FileNotFoundException e) {
            logger.debug("FileNotFoundException for {}", resourceName); //$NON-NLS-1$
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger
                        .warn(Messages.getMessage("exceptionClosingFile") + ": " + resourceName, e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }

        return properties;
    }

    @Override
    public void destroy() {
        getRequestProcessor().getConfiguration().getProvidersRegistry().removeAllProviders();
        getRequestProcessor().getConfiguration().getResourceRegistry().removeAllResources();
        for (ObjectFactory<?> of : getRequestProcessor().getConfiguration()
            .getApplicationObjectFactories()) {
            of.releaseAll(null);
        }

        /*
         * Be sure to call super.destroy()
         */
        super.destroy();
    }
}
