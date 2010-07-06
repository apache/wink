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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.wink.server.internal.registry;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.ContextAccessor;
import org.apache.wink.server.internal.contexts.HttpServletRequestWrapperImpl;
import org.apache.wink.server.internal.contexts.HttpServletResponseWrapperImpl;

public class ServletContextAccessor extends ContextAccessor {

    /**
     * Obtain a context from one of the contexts available through the
     * RuntimeContext. This can be done in two ways: if runtimeContext is
     * provided, the context is obtained directly. If runtimeContext is null,
     * then a proxy is created which obtains the RuntimeContext using the TLS.
     * 
     * @param <T> - interface of the context
     * @param contextClass - interface of the context
     * @param runtimeContext - RuntimeContext
     * @return instance of T if context was found or null otherwise.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getContextFromAccessor(final Class<T> contextClass, RuntimeContext runtimeContext) {
        // return context directly
        if (runtimeContext != null) {
            try {
                return runtimeContext.getAttribute(contextClass);
            } catch (Exception e) {
                if (e instanceof InvocationTargetException) {
                    Throwable ite = ((InvocationTargetException)e).getTargetException();
                    if (ite instanceof RuntimeException) {
                        throw (RuntimeException)ite;
                    }
                    throw new WebApplicationException(ite);
                }
                throw new WebApplicationException(e);
            }
        }

        if (HttpServletRequest.class == contextClass) {
            return (T)new HttpServletRequestWrapperImpl();
        } else if (HttpServletResponse.class == contextClass) {
            return (T)new HttpServletResponseWrapperImpl();
        }

        throw new IllegalArgumentException(Messages.getMessage("invalidServletContextAccessor", contextClass)); //$NON-NLS-1$
    }
}
