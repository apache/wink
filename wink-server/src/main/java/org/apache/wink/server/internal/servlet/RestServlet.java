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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.utils.ClassUtils;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.RequestProcessor;
import org.apache.wink.server.internal.application.ServletWinkApplication;
import org.apache.wink.server.internal.properties.WinkSystemProperties;
import org.apache.wink.server.internal.utils.ServletFileLoader;

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
    private static final String APPLICATION_INIT_PARAM  = "javax.ws.rs.Application";
    private static final String PROPERTIES_DEFAULT_FILE = "META-INF/wink-default.properties";
    private static final String PROPERTIES_INIT_PARAM   = "propertiesLocation";
    private static final String APP_LOCATION_PARAM      = "applicationConfigLocation";
    private static final String DEPLYMENT_CONF_PARAM    = "deploymentConfiguration";

    @Override
    public void init() throws ServletException {

        logger.debug("Initializing {} servlet", this);

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
                    throw new IllegalStateException("Request processor could not be created.");
                }
                storeRequestProcessorOnServletContext(requestProcessor);
            }
        } catch (Exception e) {
            // when exception occurs during the servlet initialization
            // it should be marked as unavailable
            logger.error(e.getMessage(), e);
            throw new UnavailableException(e.getMessage());
        }
    }

    @Override
    protected final void service(HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse) throws ServletException,
        IOException {
        getRequestProcessor().handleRequest(httpServletRequest, httpServletResponse);
    }

    protected RequestProcessor createRequestProcessor() throws ClassNotFoundException,
        InstantiationException, IllegalAccessException, IOException {
        DeploymentConfiguration deploymentConfiguration = getDeploymentConfiguration();
        RequestProcessor requestProcessor = new RequestProcessor(deploymentConfiguration);
        logger.debug("Creating request processor {} for servlet {}", requestProcessor, this);
        deploymentConfiguration.addApplication(getApplication(), false);
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

    protected Properties getProperties() throws IOException {
        Properties defaultProperties = loadProperties(PROPERTIES_DEFAULT_FILE, null);
        logger.debug("Default properties {} used in RestServlet {}", defaultProperties, this);
        String propertiesLocation = getInitParameter(PROPERTIES_INIT_PARAM);
        if (propertiesLocation != null) {
            logger.info(Messages.getMessage("restServletUsePropertiesFileAtLocation"),
                        propertiesLocation,
                        PROPERTIES_INIT_PARAM);

            // Load properties set on JVM. These should not override
            // the ones set in the configuration file.
            Properties properties = loadProperties(propertiesLocation, defaultProperties);
            properties.putAll(WinkSystemProperties.loadSystemProperties(properties));
            return properties;
        }
        logger.debug("Final properties {} used in RestServlet {}", defaultProperties, this);

        // Load properties set on JVM. These should not override
        // the ones set in the configuration file.
        defaultProperties.putAll(WinkSystemProperties.loadSystemProperties(defaultProperties));

        return defaultProperties;
    }

    protected DeploymentConfiguration createDeploymentConfiguration()
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String initParameter = getInitParameter(DEPLYMENT_CONF_PARAM);
        if (initParameter != null) {
            logger.info(Messages.getMessage("restServletUseDeploymentConfigurationParam"),
                        initParameter,
                        DEPLYMENT_CONF_PARAM);
            // use ClassUtils.loadClass instead of Class.forName so we have
            // classloader visibility into the Web module in J2EE environments
            Class<?> confClass = ClassUtils.loadClass(initParameter);
            return (DeploymentConfiguration)confClass.newInstance();
        }
        return new DeploymentConfiguration();
    }

    @SuppressWarnings("unchecked")
    protected Application getApplication() throws ClassNotFoundException, InstantiationException,
        IllegalAccessException {
        Class<? extends Application> appClass = null;
        String initParameter = getInitParameter(APPLICATION_INIT_PARAM);
        if (initParameter != null) {
            logger.info(Messages.getMessage("restServletJAXRSApplicationInitParam"),
                        initParameter,
                        APPLICATION_INIT_PARAM);
            // use ClassUtils.loadClass instead of Class.forName so we have
            // classloader visibility into the Web module in J2EE environments
            appClass = (Class<? extends Application>)ClassUtils.loadClass(initParameter);
            return appClass.newInstance();
        }
        String appLocationParameter = getInitParameter(APP_LOCATION_PARAM);
        if (appLocationParameter == null) {
            logger.warn(Messages.getMessage("propertyNotDefined"), APP_LOCATION_PARAM);
        } else {
            logger.info(Messages.getMessage("restServletWinkApplicationInitParam"),
                        appLocationParameter,
                        APP_LOCATION_PARAM);
        }
        return new ServletWinkApplication(getServletContext(), appLocationParameter);
    }

    /**
     * loadProperties will try to load the properties in the following order.
     * <ol>
     * <li>Custom configuration file (defined in init parameter)</li>
     * <li>The default properties file</li>
     * <li>System properties (reads the keys defined in the properties to do the
     * specific lookups and does a lookup for properties that have empty values)
     * </li>
     * </ol>
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
            logger.debug("FileNotFoundException for {}", resourceName);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                logger.warn(Messages.getMessage("exceptionClosingFile") + ": " + resourceName, e);
            }
        }

        return properties;
    }
}
