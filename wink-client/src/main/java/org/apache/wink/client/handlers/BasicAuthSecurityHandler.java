/*
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
 */
package org.apache.wink.client.handlers;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.codec.binary.Base64;
import org.apache.wink.client.ClientAuthenticationException;
import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SecurityHandler for a client to perform http basic auth:
 * <p/>
 * <code>
 * Usage:<br/>
 * ClientConfig config = new ClientConfig();<br/>
 * BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();<br/>
 * basicAuth.setUserName("user1");<br/>
 * basicAuth.setPassword("password2");<br/>
 * config.handlers(basicAuth);<br/>
 * // create the rest client instance<br/>
 * RestClient client = new RestClient(config);<br/>
 * // create the resource instance to interact with Resource<br/>
 * resource = client.resource("https://localhost:8080/path/to/resource");<br/>
 * </code>
 */
public class BasicAuthSecurityHandler implements ClientHandler {

    private static Logger   logger          =
                                                LoggerFactory
                                                    .getLogger(BasicAuthSecurityHandler.class);

    private volatile String handlerUsername = null;
    private volatile String handlerPassword = null;

    /**
     * Sets the username to use.
     * 
     * @param aUserName the user name
     */
    public void setUserName(String aUserName) {
        logger.debug("Setting the username to {}", aUserName); //$NON-NLS-1$
        this.handlerUsername = aUserName;
    }

    /**
     * Sets the password to use.
     * 
     * @param aPassword the password to use
     */
    public void setPassword(String aPassword) {
        logger.debug("Setting the password"); //$NON-NLS-1$
        this.handlerPassword = aPassword;
    }

    /**
     * Performs basic HTTP authentication.
     * 
     * @param client request object
     * @param handler context object
     * @return a client response object that may contain an HTTP Authorization
     *         header
     */
    public ClientResponse handle(ClientRequest request, HandlerContext context) throws Exception {
        logger.debug("Entering BasicAuthSecurityHandler.doChain()"); //$NON-NLS-1$
        ClientResponse response = context.doChain(request);

        int statusCode = response.getStatusCode();
        logger.debug("Response status code was {}", statusCode); //$NON-NLS-1$
        if (statusCode != 401) {
            logger.debug("Status code was not 401 so no need to re-issue request."); //$NON-NLS-1$
            return response;
        } else {
            String userid = handlerUsername;
            String password = handlerPassword;
            if (logger.isDebugEnabled()) {
                logger.debug("The 'username' property was set to: {}", userid); //$NON-NLS-1$
                logger.debug("Was the 'password' property set: {}", password != null); //$NON-NLS-1$
            }

            if (!(userid == null || userid.equals("") || password == null || password.equals(""))) { //$NON-NLS-1$ //$NON-NLS-2$
                logger.debug("userid and password set so setting Authorization header"); //$NON-NLS-1$
                // we have a user credential
                String credential = userid + ":" + password; //$NON-NLS-1$
                byte[] credBytes = credential.getBytes();
                byte[] encodedCredBytes = Base64.encodeBase64(credBytes, false);
                // id and password needs to be base64 encoded
                String credEncodedString = "Basic " + new String(encodedCredBytes); //$NON-NLS-1$
                request.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, credEncodedString);
                logger.debug("Issuing request again with Authorization header"); //$NON-NLS-1$
                response = context.doChain(request);
                if (response.getStatusCode() == 401) {
                    logger
                        .debug("After sending request with Authorization header, still got 401 response"); //$NON-NLS-1$
                    throw new ClientAuthenticationException(
                                                            "Service failed to authenticate user: " + userid); //$NON-NLS-1$
                } else {
                    logger.debug("Got a non-401 response, so returning response"); //$NON-NLS-1$
                    return response;
                }
            } else {
                logger.debug("userid and/or password were not set so throwing exception"); //$NON-NLS-1$
                // no user credential available
                throw new ClientAuthenticationException(
                                                        "Missing client authentication credential for user: " + userid); //$NON-NLS-1$
            }

        } // end if block
    } // end handle

} // end class SecurityHandler

