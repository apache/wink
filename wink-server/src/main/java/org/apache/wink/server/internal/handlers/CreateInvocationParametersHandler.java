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

package org.apache.wink.server.internal.handlers;

import java.util.List;
import java.util.Properties;

import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.InjectableFactory;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateInvocationParametersHandler implements RequestHandler {

    private static Logger logger = LoggerFactory.getLogger(CreateInvocationParametersHandler.class);

    public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
        SearchResult result = context.getAttribute(SearchResult.class);

        // create and save the invocation parameters for the found method
        List<Injectable> formal = result.getMethod().getMetadata().getFormalParameters();
        logger.trace("Formal Injectable parameters list is: {}", formal); //$NON-NLS-1$
        Object[] parameters = InjectableFactory.getInstance().instantiate(formal, context);
        if(logger.isTraceEnabled()) {
            if(parameters == null) {
                logger.trace("Actual parameters list to inject is: null"); //$NON-NLS-1$
            } else {
                logger.trace("Actual parameters list to inject is: {}", parameters); //$NON-NLS-1$
            }
        }
        result.setInvocationParameters(parameters);

        chain.doChain(context);
    }

    public void init(Properties props) {
    }

}
