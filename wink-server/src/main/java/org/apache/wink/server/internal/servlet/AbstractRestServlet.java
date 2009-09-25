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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.wink.server.internal.RequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for RestServlet and AdminServlet
 */
public abstract class AbstractRestServlet extends HttpServlet {

    private final Logger        logger                      =
                                                                LoggerFactory
                                                                    .getLogger(AbstractRestServlet.class);

    private static final String REQUEST_PROCESSOR_ATTRIBUTE = "requestProcessorAttribute";
    private static final long   serialVersionUID            = 7721777326714438571L;
    private String              requestProcessorAttribute;

    @Override
    public void init() throws ServletException {
        super.init();
        requestProcessorAttribute = getInitParameter(REQUEST_PROCESSOR_ATTRIBUTE);
        logger.debug("Request processor attribute is {} for {}", requestProcessorAttribute, this);
    }

    protected RequestProcessor getRequestProcessor() {
        return RequestProcessor.getRequestProcessor(getServletContext(), requestProcessorAttribute);
    }

    protected void storeRequestProcessorOnServletContext(RequestProcessor requestProcessor) {
        requestProcessor.storeRequestProcessorOnServletContext(getServletContext(),
                                                               requestProcessorAttribute);
    }

}
