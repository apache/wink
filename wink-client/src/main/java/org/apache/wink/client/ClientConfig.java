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
import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.handlers.ConnectionHandler;
import org.apache.wink.client.internal.handlers.AcceptHeaderHandler;
import org.apache.wink.client.internal.handlers.HttpURLConnectionHandler;
import org.apache.wink.common.WinkApplication;
import org.apache.wink.common.internal.application.ApplicationFileLoader;

/**
 * Provides client configuration. The ClientConfig is implemented using the
 * builder pattern so method calls can be easily aggregated. Custom Providers
 * are defined by calling the {@link #applications(Application...)} method.
 * Custom client handlers are defined by calling the
 * {@link #handlers(ClientHandler...)} method.
 */
public class ClientConfig implements Cloneable {
    private String                    proxyHost;
    private int                       proxyPort;
    private int                       connectTimeout;
    private int                       readTimeout;
    private boolean                   followRedirects;
    private LinkedList<ClientHandler> handlers;
    private LinkedList<Application>   applications;
    private boolean                   modifiable;
    private boolean                   isAcceptHeaderAutoSet;
    private boolean                   loadWinkApplications = true;

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
        connectTimeout = 60000;
        readTimeout = 60000;
        followRedirects = true;
        isAcceptHeaderAutoSet = true;
        handlers = new LinkedList<ClientHandler>();
        applications = new LinkedList<Application>();
        initDefaultApplication();
    }

    private void initDefaultApplication() {

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
                    return 0.1;
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
            throw new ClientConfigException("configuration is unmodifiable");
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
            throw new ClientConfigException("configuration is unmodifiable");
        }
        if (proxyPort <= 0) {
            proxyPort = 80;
        }
        this.proxyPort = proxyPort;
        return this;
    }

    /**
     * Get the connect timeout in milliseconds
     * 
     * @return the connect timeout in milliseconds
     */
    public final int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Set the connect timeout in milliseconds
     * 
     * @param connectTimeout the connect timeout in milliseconds
     * @return this client configuration
     * @throws ClientConfigException
     */
    public final ClientConfig connectTimeout(int connectTimeout) {
        if (!modifiable) {
            throw new ClientConfigException("configuration is unmodifiable");
        }
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Get the read timeout in milliseconds
     * 
     * @return the read timeout in milliseconds
     */
    public final int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Set the read timeout in milliseconds
     * 
     * @param readTimeout the read timeout in milliseconds
     * @return this client configuration
     * @throws ClientConfigException
     */
    public final ClientConfig readTimeout(int readTimeout) {
        if (!modifiable) {
            throw new ClientConfigException("configuration is unmodifiable");
        }
        this.readTimeout = readTimeout;
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
            throw new ClientConfigException("configuration is unmodifiable");
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
            throw new ClientConfigException("configuration is unmodifiable");
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
            throw new ClientConfigException("configuration is unmodifiable");
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
            throw new ClientConfigException("configuration is unmodifiable");
        }
        for (Application application : applications) {
            this.applications.add(application);
        }
        return this;
    }

    @Override
    protected ClientConfig clone() {
        try {
            ClientConfig clone = (ClientConfig)super.clone();
            clone.handlers = new LinkedList<ClientHandler>(handlers);
            clone.applications = new LinkedList<Application>(applications);
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

}
