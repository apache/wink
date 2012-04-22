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

package org.apache.wink.common.internal.registry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;

public class ContextAccessor {

    public <T> T getContext(Class<T> contextClass, RuntimeContext runtimeContext) {

        // try to get a context from a ContextResolver
        T context = getContextFromResolver(contextClass, runtimeContext);
        if (context != null) {
            return context;
        }

        // get a context from one of the message context accessors
        return getContextFromAccessor(contextClass, runtimeContext);
    }

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
    public <T> T getContextFromAccessor(final Class<T> contextClass, RuntimeContext runtimeContext) {

        // return context directly
        if (runtimeContext != null) {
            try {
                T instance = runtimeContext.getAttribute(contextClass);
                if (instance != null) {
                    return instance;
                }
                /*
                 * if the instance isn't directly available from the
                 * runtimecontext at this time, inject a proxy instead. This
                 * allows context attributes to be added after a class is
                 * instantiated
                 */
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

        // return a proxy that looks for a context on TLS during the method
        // invocation
        T proxy =
            (T)Proxy.newProxyInstance(Injectable.class.getClassLoader(),
                                      new Class[] {contextClass},
                                      new InvocationHandler() {
                                          public Object invoke(Object proxy,
                                                               Method method,
                                                               Object[] args) throws Throwable {
                                              // use runtimeContext from TLS
                                              RuntimeContext runtimeContext =
                                                  RuntimeContextTLS.getRuntimeContext();
                                              if (runtimeContext == null) {
                                                  if("toString".equals(method.getName()) && (args == null || args.length == 0)) {
                                                      return "Proxy for " + contextClass.getName();
                                                  }
                                                  throw new IllegalStateException();
                                              }
                                              // get the real context from the
                                              // RuntimeContext
                                              // We need to call getContext() instead of getContextFromAccessor() as some context will be created from the context resolvers
                                              Object context = getContext(contextClass, runtimeContext);
                                              // invoke the method on the real
                                              // context
                                              return method.invoke(context, args);
                                          }
                                      });
        return proxy;
    }

    /**
     * Get a context from on of the registered context providers
     * 
     * @param type
     * @param runtimeContext
     * @return
     */
    public <T> T getContextFromResolver(Class<T> contextClass, RuntimeContext runtimeContext) {
        if (runtimeContext == null) {
            return null;
        }
        Providers providers = runtimeContext.getProviders();
        if (providers != null) {
            HttpHeaders httpHeaders = runtimeContext.getHttpHeaders();
            MediaType mediaType = null;
            if (httpHeaders != null) {
                // this is hotfix for WINK-166
                mediaType = httpHeaders.getMediaType();
            }
            if (mediaType == null) {
                mediaType = MediaType.WILDCARD_TYPE;
            }
            ContextResolver<T> contextResolver =
                providers.getContextResolver(contextClass, mediaType);
            if (contextResolver != null) {
                T context = contextResolver.getContext(contextClass);
                if (context != null) {
                    return context;
                }
            }
        }
        return null;
    }

}
