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
package org.apache.wink.server.handlers;

import java.util.Properties;

/**
 * Convenience class for implementing a handler that is unaware of the handlers
 * chain. A handler may extend this class and implement any or all of
 * {@link #init(Properties)}, {@link #handleRequest(MessageContext)} and
 * {@link #handleResponse(MessageContext)}
 */
public abstract class AbstractHandler implements RequestHandler, ResponseHandler {

    public final void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
        handleRequest(context);
        chain.doChain(context);
    }

    public final void handleResponse(MessageContext context, HandlersChain chain) throws Throwable {
        handleResponse(context);
        chain.doChain(context);
    }

    /**
     * Override this method if initialization functionality is required
     * 
     * @see {@link Handler#init(Properties)}
     */
    public void init(Properties props) {
    }

    /**
     * Override this method to handle the request. This method releases the
     * responsibility of the user to continue the chain, as it is done
     * automatically at the end of the method invocation
     * 
     * @param context the current message context
     * @see {@link RequestHandler#handleRequest(MessageContext, HandlersChain)}
     */
    protected void handleRequest(MessageContext context) throws Throwable {
    }

    /**
     * Override this method to handle the response. This method releases the
     * responsibility of the user to continue the chain, as it is done
     * automatically at the end of the method invocation
     * 
     * @param context the current message context
     * @see {@link ResponseHandler#handleResponse(MessageContext, HandlersChain)}
     */
    protected void handleResponse(MessageContext context) throws Throwable {
    }

}
