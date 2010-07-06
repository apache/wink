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

package org.apache.wink.client;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.handlers.ConnectionHandler;
import org.apache.wink.client.internal.handlers.AcceptHeaderHandler;
import org.apache.wink.client.internal.handlers.HttpURLConnectionHandler;
import org.apache.wink.common.WinkApplication;
import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.common.internal.application.ApplicationFileLoader;
import org.apache.wink.common.internal.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides client configuration. The ClientConfig is implemented using the
 * builder pattern so method calls can be easily aggregated. Custom Providers
 * are defined by calling the {@link #applications(Application...)} method.
 * Custom client handlers are defined by calling the
 * {@link #handlers(ClientHandler...)} method.
 */
public class ClientConfig implements Cloneable, WinkConfiguration {

    private static final Logger       logger                             =
        LoggerFactory
            .getLogger(ClientConfig.class);
    
    private String                    proxyHost;
    private int                       proxyPort;
    private boolean                   followRedirects;
    private LinkedList<ClientHandler> handlers;
    private LinkedList<Application>   applications;
    private boolean                   modifiable;
    private boolean                   isAcceptHeaderAutoSet;
    private boolean                   loadWinkApplications               = true;

    private static final String       WINK_CLIENT_CONNECTTIMEOUT         =
                                                                             "wink.client.connectTimeout"; //$NON-NLS-1$
    private static final String       WINK_CLIENT_READTIMEOUT            =
                                                                             "wink.client.readTimeout"; //$NON-NLS-1$
    private static final String       WINK_SUPPORT_DTD_EXPANSION  =
                                                                             "wink.supportDTDEntityExpansion"; //$NON-NLS-1$

    private static int                WINK_CLIENT_CONNECTTIMEOUT_DEFAULT = 60000;
    private static int                WINK_CLIENT_READTIMEOUT_DEFAULT    = 60000;
    private static boolean            WINK_CLIENT_SUPPORT_DTD_EXPANSION_DEFAULT = false;
    
    private Properties properties = null;

    /**
     * Construct a new ClientConfig with the following default settings:
     * <ul>
     * <li>proxy: none</li>
     * <li>connect timeout: 60 seconds</li>
     * <li>read timeout: 60 seconds</li>
     * <li>follow redirects: true</li>
     * </ul>
     */
    public ClientConfig() {
        modifiable = true;
        proxyHost = null;
        proxyPort = 80;
        followRedirects = true;
        isAcceptHeaderAutoSet = true;
        handlers = new LinkedList<ClientHandler>();
    }

    private void initDefaultApplication() {

        if (applications != null) {
            return;
        } else {
            applications = new LinkedList<Application>();
        }
        
        try {
            final Set<Class<?>> classes =
                new ApplicationFileLoader(loadWinkApplications).getClasses();

            applications(new WinkApplication() {
                @Override
                public Set<Class<?>> getClasses() {
                    return classes;
                }

                @Override
                public double getPriority() {
                    return WinkApplication.SYSTEM_PRIORITY;
                }
            });
        } catch (FileNotFoundException e) {
            throw new ClientConfigException(e);
        }
    }

    /**
     * Get the proxy host
     * 
     * @return the proxy host
     */
    public final String getProxyHost() {
        return proxyHost;
    }

    /**
     * Set the proxy host
     * 
     * @param proxyHost proxy host
     * @return this client configuration
     * @throws ClientConfigException
     */
    public final ClientConfig proxyHost(String proxyHost) {
        if (!modifiable) {
            throw new ClientConfigException(Messages.getMessage("clientConfigurationUnmodifiable")); //$NON-NLS-1$
        }
        this.proxyHost = proxyHost;
        return this;
    }

    /**
     * Get the proxy port
     * 
     * @return the proxy port
     */
    public final int getProxyPort() {
        return proxyPort;
    }

    /**
     * Set the proxy port
     * 
     * @param proxyPort proxy port
     * @return this client configuration
     * @throws ClientConfigException
     */
    public final ClientConfig proxyPort(int proxyPort) {
        if (!modifiable) {
            throw new ClientConfigException(Messages.getMessage("clientConfigurationUnmodifiable")); //$NON-NLS-1$
        }
        if (proxyPort <= 0) {
            proxyPort = 80;
        }
        this.proxyPort = proxyPort;
        return this;
    }

    /**
     * Convenience method to get the int value of the wink.client.connectTimeout property
     * 
     * @return the connect timeout in milliseconds
     */
    public final int getConnectTimeout() {
        try {
            return Integer.valueOf(getProperties().getProperty(WINK_CLIENT_CONNECTTIMEOUT)).intValue();
        } catch (NumberFormatException e) {
            logger.debug("Value in properties for key {} is invalid.  Reverting to default: {}", WINK_CLIENT_CONNECTTIMEOUT, WINK_CLIENT_CONNECTTIMEOUT_DEFAULT); //$NON-NLS-1$
            getProperties().setProperty(WINK_CLIENT_CONNECTTIMEOUT, String.valueOf(WINK_CLIENT_CONNECTTIMEOUT_DEFAULT));
            return getReadTimeout();  // this is safe, because it's unit tested.  :)
        }
    }

    /**
     * Convenience method to set the wink.client.connectTimeout property
     * 
     * @param connectTimeout the connect timeout in milliseconds
     * @return this client configuration
     * @throws ClientConfigException
     */
    public final ClientConfig connectTimeout(int connectTimeout) {
        if (!modifiable) {
            throw new ClientConfigException(Messages.getMessage("clientConfigurationUnmodifiable")); //$NON-NLS-1$
        }
        getProperties().setProperty(WINK_CLIENT_CONNECTTIMEOUT, String.valueOf(connectTimeout));
        return this;
    }

    /**
     * Convenience method to get the int value of the wink.client.readTimeout property
     * 
     * @return the read timeout in milliseconds
     */
    public final int getReadTimeout() {
        try {
            return Integer.valueOf(getProperties().getProperty(WINK_CLIENT_READTIMEOUT)).intValue();
        } catch (NumberFormatException e) {
            logger.debug("Value in properties for key {} is invalid.  Reverting to default: {}", WINK_CLIENT_READTIMEOUT, WINK_CLIENT_READTIMEOUT_DEFAULT); //$NON-NLS-1$
            getProperties().setProperty(WINK_CLIENT_READTIMEOUT, String.valueOf(WINK_CLIENT_READTIMEOUT_DEFAULT));
            return getReadTimeout();  // this is safe, because it's unit tested.  :)
        }
    }

    /**
     * Convenience method to set the wink.client.readTimeout property
     * 
     * @param readTimeout the read timeout in milliseconds
     * @return this client configuration
     * @throws ClientConfigException
     */
    public final ClientConfig readTimeout(int readTimeout) {
        if (!modifiable) {
            throw new ClientConfigException(Messages.getMessage("clientConfigurationUnmodifiable")); //$NON-NLS-1$
        }
        getProperties().setProperty(WINK_CLIENT_READTIMEOUT, String.valueOf(readTimeout));
        return this;
    }
    
    /**
     * Convenience method to get the boolean value of the wink.supportDTDExpansion property
     * 
     * @return boolean
     */
    public final boolean isSupportDTDExpansion() {
        // this is safe.  See valueOf javadoc
        return Boolean.valueOf(getProperties().getProperty(WINK_SUPPORT_DTD_EXPANSION)).booleanValue();
    }
    
    /**
     * Convenience method to set the wink.supportDTDExpansion property
     * 
     * @param supportDTDExpansion boolean
     * @return this client configuration
     * @throws ClientConfigException
     */
    public final ClientConfig supportDTDExpansion(boolean supportDTDExpansion) {
        if (!modifiable) {
            throw new ClientConfigException(Messages.getMessage("clientConfigurationUnmodifiable")); //$NON-NLS-1$
        }
        getProperties().setProperty(WINK_SUPPORT_DTD_EXPANSION, String.valueOf(supportDTDExpansion));
        return this;
    }

    /**
     * Returns whether to client will automatically follow redirects
     * 
     * @return true if client will automatically follow redirects; false
     *         otherwise
     */
    public final boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
     * Set whether to client will automatically follow redirects
     * 
     * @param followRedirects whether to client will automatically follow
     *            redirects
     * @return this client configuration
     * @throws ClientConfigException
     */
    public final ClientConfig followRedirects(boolean followRedirects) {
        if (!modifiable) {
            throw new ClientConfigException(Messages.getMessage("clientConfigurationUnmodifiable")); //$NON-NLS-1$
        }
        this.followRedirects = followRedirects;
        return this;
    }

    /**
     * Returns whether client will automatically set an appropriate Accept
     * header
     * 
     * @return true if client will automatically set an appropriate Accept
     *         header; false otherwise
     */
    public final boolean isAcceptHeaderAutoSet() {
        return isAcceptHeaderAutoSet;
    }

    /**
     * Set whether client will automatically set an appropriate Accept header
     * 
     * @param isAcceptHeaderAutoSet whether client will automatically set an
     *            appropriate Accept header
     * @return this client configuration
     * @throws ClientConfigException
     */
    public final ClientConfig acceptHeaderAutoSet(boolean isAcceptHeaderAutoSet) {
        if (!modifiable) {
            throw new ClientConfigException(Messages.getMessage("clientConfigurationUnmodifiable")); //$NON-NLS-1$
        }
        this.isAcceptHeaderAutoSet = isAcceptHeaderAutoSet;
        return this;
    }

    /**
     * Get an unmodifiable list of the client handlers
     * 
     * @return an unmodifiable list of the client handlers
     */
    public final List<ClientHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }

    /**
     * Add client handlers
     * 
     * @param handlers the handlers to add
     * @return this client configuration
     * @throws ClientConfigException
     */
    public final ClientConfig handlers(ClientHandler... handlers) {
        if (!modifiable) {
            throw new ClientConfigException(Messages.getMessage("clientConfigurationUnmodifiable")); //$NON-NLS-1$
        }
        for (ClientHandler handler : handlers) {
            this.handlers.add(handler);
        }
        return this;
    }

    /* package */ClientConfig build() {
        if (isAcceptHeaderAutoSet) {
            handlers.add(new AcceptHeaderHandler());
        }
        handlers.add(getConnectionHandler());
        modifiable = false;
        return this;
    }

    /**
     * Returns the client handler that acts as the connection handler. This
     * handler is always the last handler on the chain and is automatically
     * added to the end of the defined list of handlers.
     * <p>
     * This method should be overridden in order to provide an alternate
     * connection handler.
     * 
     * @return the connection handler
     */
    protected ConnectionHandler getConnectionHandler() {
        return new HttpURLConnectionHandler();
    }

    /**
     * Get an unmodifiable list of the applications
     * 
     * @return
     */
    public final List<Application> getApplications() {
        if (applications == null) {
            initDefaultApplication();
        }
        return Collections.unmodifiableList(applications);
    }

    /**
     * Add applications
     * 
     * @param applications the applications to add
     * @return this client configuration
     * @throws ClientConfigException
     */
    public final ClientConfig applications(Application... applications) {
        if (!modifiable) {
            throw new ClientConfigException(Messages.getMessage("clientConfigurationUnmodifiable")); //$NON-NLS-1$
        }
        if (this.applications == null) {
            initDefaultApplication();
        }
        for (Application application : applications) {
            this.applications.add(application);
        }
        return this;
    }

    @Override
    protected ClientConfig clone() {
        if (applications == null) {
            initDefaultApplication();
        }
        try {
            ClientConfig clone = (ClientConfig)super.clone();
            clone.handlers = new LinkedList<ClientHandler>(handlers);
            clone.applications = new LinkedList<Application>(applications);
            // need to deep copy the properties:
            // TODO: thread safe?  Remember there's an Iterator at work under the putAll
            Properties props = new Properties();
            props.putAll(getProperties());
            clone.setProperties(props);
            return clone;
        } catch (CloneNotSupportedException e) {
            // can't happen
            throw new RuntimeException(e);
        }
    }

    public void setLoadWinkApplications(boolean loadWinkApplications) {
        this.loadWinkApplications = loadWinkApplications;
    }

    public boolean isLoadWinkApplications() {
        return loadWinkApplications;
    }

    /**
     * Convenience method for getting all properties registered on this instance.  System properties may be changed between creation of
     * new instances.
     * 
     * The following properties are meaningful to a ClientConfig instance:
     * wink.client.connectTimeout - value is in milliseconds, default is 60000
     * wink.client.readTimeout - value is in milliseconds, default is 60000
     * wink.supportDTDExpansion - value is "true" or "false" to allow DOCTYPE entity expansion when built-in providers parse XML, default is "false"
     * 
     * @return properties on this ClientConfig instance
     */
    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try {
                String connectTimeoutString = System.getProperty(WINK_CLIENT_CONNECTTIMEOUT, String.valueOf(WINK_CLIENT_CONNECTTIMEOUT_DEFAULT));
                int toSet = Integer.parseInt(connectTimeoutString);
                properties.put(WINK_CLIENT_CONNECTTIMEOUT, String.valueOf(toSet));
                logger.debug("Wink client connectTimeout default value is {}.", toSet); //$NON-NLS-1$
            } catch (Exception e) {
                logger.debug("Error processing {} system property: {}", WINK_CLIENT_CONNECTTIMEOUT, e); //$NON-NLS-1$
            }
            try {
                String readTimeoutString = System.getProperty(WINK_CLIENT_READTIMEOUT, String.valueOf(WINK_CLIENT_READTIMEOUT_DEFAULT));
                int toSet = Integer.parseInt(readTimeoutString);
                properties.put(WINK_CLIENT_READTIMEOUT, String.valueOf(toSet));
                logger.debug("Wink client readTimeout default value is {}.", toSet); //$NON-NLS-1$
            } catch (Exception e) {
                logger.debug("Error processing {} system property: {}", WINK_CLIENT_READTIMEOUT, e); //$NON-NLS-1$
            }
            try {
                String supportDTD = System.getProperty(WINK_SUPPORT_DTD_EXPANSION, String.valueOf(WINK_CLIENT_SUPPORT_DTD_EXPANSION_DEFAULT));
                boolean toSet = Boolean.valueOf(supportDTD);  // require "true" or "false", not "yes" or "no" or other variants (see parseBoolean vs. valueOf javadoc)
                properties.put(WINK_SUPPORT_DTD_EXPANSION, String.valueOf(toSet));
                logger.debug("Wink client readTimeout default value is {}.", String.valueOf(toSet)); //$NON-NLS-1$
            } catch (Exception e) {
                logger.debug("Error processing {} system property: {}", WINK_SUPPORT_DTD_EXPANSION, e); //$NON-NLS-1$
            }
        }
        return properties;
    }

    /**
     * Convenience method to set the client configuration properties.
     * 
     * The following properties are meaningful to a ClientConfig instance:
     * wink.client.connectTimeout - value is in milliseconds, default is 60000
     * wink.client.readTimeout - value is in milliseconds, default is 60000
     * wink.supportDTDExpansion - value is "true" or "false" to allow DOCTYPE entity expansion when built-in providers parse XML, default is "false"
     * 
     * @param properties the properties object to use.  If properties parameter is null, the properties on this ClientConfig will be cleared with Properties.clear()
     */
    public void setProperties(Properties properties) {
        if (!modifiable) {
            throw new ClientConfigException(Messages.getMessage("clientConfigurationUnmodifiable")); //$NON-NLS-1$
        }
        if (properties == null) {
            this.properties.clear();
            return;
        }
        this.properties = properties;
    }

}
