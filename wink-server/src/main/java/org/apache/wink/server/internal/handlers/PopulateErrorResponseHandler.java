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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.server.handlers.AbstractHandler;
import org.apache.wink.server.handlers.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopulateErrorResponseHandler extends AbstractHandler {

    private static final RuntimeDelegate RUNTIME_DELEGATE = RuntimeDelegate.getInstance();
    private static final Logger          logger           =
                                                              LoggerFactory
                                                                  .getLogger(PopulateErrorResponseHandler.class);

    @SuppressWarnings("unchecked")
    public void handleResponse(MessageContext context) throws Throwable {
        Object result = context.getResponseEntity();
        if (result instanceof WebApplicationException) {
            handleWebApplicationException(context, (WebApplicationException)result);
        } else if(result instanceof Throwable) {
            Throwable exception = (Throwable)result;
            ExceptionMapper<Throwable> provider =
                (ExceptionMapper<Throwable>)findProvider(context, exception);
            if (provider != null) {
                logger.trace("Using provider {} to map exception {}", provider, exception); //$NON-NLS-1$
                context.setResponseEntity(executeProvider(exception, provider));
            } else {
                throw exception;
            }
        }
    }

    private Response executeProvider(Throwable exception, ExceptionMapper<Throwable> provider) {
        try {
            return provider.toResponse(exception);
        } catch (Throwable e) {
            logger.error(Messages.getMessage("exceptionOccurredDuringExceptionMapper", provider.getClass().getName()), e); //$NON-NLS-1$
            return RUNTIME_DELEGATE.createResponseBuilder().status(500).build();
        }
    }

    private ExceptionMapper<? extends Throwable> findProvider(MessageContext msgContext,
                                                              Throwable result) {
        return msgContext.getProviders().getExceptionMapper(result.getClass());
    }

    @SuppressWarnings("unchecked")
    private void handleWebApplicationException(MessageContext msgContext,
                                               WebApplicationException exception) {
        ExceptionMapper<WebApplicationException> provider = null;
        if (exception.getResponse().getEntity() == null) {
            // only look for a provider if the response entity on the
            // WebApplicationException is null (per spec)
            provider =
                (ExceptionMapper<WebApplicationException>)findProvider(msgContext, exception);
        }
        if (provider != null) {
            logger.trace("Using ExceptionMapper to map response from WebApplicationException"); //$NON-NLS-1$
            msgContext.setResponseEntity(provider.toResponse(exception));
        } else {
            logger.trace("Getting response directly from WebApplicationException"); //$NON-NLS-1$
            msgContext.setResponseEntity(exception.getResponse());
        }
    }

}
