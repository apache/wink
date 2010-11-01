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

import org.apache.wink.client.ClientAuthenticationException;
import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.internal.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SecurityHandler for a client to perform http basic auth:
 * <p/>
 * <code>
 * Usage:<br/>
 * ClientConfig config = new ClientConfig();<br/>
 * BasicAuthSecurityHandler basicAuthSecHandler = new BasicAuthSecurityHandler();
 * basicAuthSecHandler.setUserName("username");
 * basicAuthSecHandler.setPassword("password");
 * config.handlers(basicAuthSecurityHandler);<br/>
 * // create the rest client instance<br/>
 * RestClient client = new RestClient(config);<br/>
 * // create the resource instance to interact with Resource<br/>
 * resource = client.resource("https://localhost:8080/path/to/resource");<br/>
 * </code>
 */
public class BasicAuthSecurityHandler extends AbstractAuthSecurityHandler implements ClientHandler {

    private static Logger    logger       = LoggerFactory.getLogger(BasicAuthSecurityHandler.class);

    private static final int UNAUTHORIZED = HttpStatus.UNAUTHORIZED.getCode();

    public BasicAuthSecurityHandler() {
        /* do nothing */
    }

    public BasicAuthSecurityHandler(final String username, final String password) {
        super(username, password);
    }

    /**
     * Performs basic HTTP authentication and proxy authentication, if
     * necessary.
     * 
     * @param client request object
     * @param handler context object
     * @return a client response object that may contain an HTTP Authorization
     *         header
     */
    public ClientResponse handle(ClientRequest request, HandlerContext context) throws Exception {
        logger.trace("Entering BasicAuthSecurityHandler.doChain()"); //$NON-NLS-1$
        ClientResponse response = context.doChain(request);
        if (response.getStatusCode() == UNAUTHORIZED) {

            if (!(handlerUsername == null || handlerUsername.equals("") || handlerPassword == null || handlerPassword.equals(""))) { //$NON-NLS-1$ //$NON-NLS-2$
                logger.trace("userid and password set so setting Authorization header"); //$NON-NLS-1$
                // we have a user credential
                if (handlerEncodedCredentials == null) {
                    handlerEncodedCredentials = getEncodedString(handlerUsername, handlerPassword);
                }
                request.getHeaders()
                    .putSingle("Authorization", handlerEncodedCredentials); //$NON-NLS-1$
                logger.trace("Issuing request again with Authorization header"); //$NON-NLS-1$
                response = context.doChain(request);
                if (response.getStatusCode() == UNAUTHORIZED) {
                    logger
                        .trace("After sending request with Authorization header, still got " + UNAUTHORIZED + " response"); //$NON-NLS-1$
                    throw new ClientAuthenticationException(Messages
                        .getMessage("serviceFailedToAuthenticateUser", handlerUsername)); //$NON-NLS-1$
                } else {
                    logger.trace("Got a non-" + UNAUTHORIZED + " response, so returning response"); //$NON-NLS-1$
                    return response;
                }
            } else {
                logger.trace("user and/or password were not set so throwing exception"); //$NON-NLS-1$
                // no user credential available
                throw new ClientAuthenticationException(Messages
                    .getMessage("missingClientAuthenticationCredentialForUser", handlerUsername)); //$NON-NLS-1$
            }
        } else {
            logger
                .trace("Status code was not " + UNAUTHORIZED + " so no need to re-issue request."); //$NON-NLS-1$
            return response;
        }

    }

}
