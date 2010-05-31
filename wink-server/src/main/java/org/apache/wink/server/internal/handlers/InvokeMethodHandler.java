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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.wink.server.handlers.AbstractHandler;
import org.apache.wink.server.handlers.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvokeMethodHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(InvokeMethodHandler.class);

    @Override
    public void handleRequest(MessageContext context) throws Throwable {
        try {
            SearchResult searchResult = context.getAttribute(SearchResult.class);
            Method javaMethod = searchResult.getMethod().getMetadata().getReflectionMethod();
            Object[] parameters = searchResult.getInvocationParameters();
            Object instance = searchResult.getResource().getInstance(context);
            if (logger.isDebugEnabled()) {
                logger
                    .debug("Invoking method {} of declaring class {} on the instance of a class {}@{} with parameters {}", //$NON-NLS-1$
                           new Object[] {javaMethod.getName(),
                               javaMethod.getDeclaringClass().getName(),
                               instance.getClass().getName(),
                               Integer.toHexString(System.identityHashCode(instance)),
                               Arrays.toString(parameters)});
            }
            Object result = javaMethod.invoke(instance, parameters);
            context.setResponseEntity(result);
        } catch (InvocationTargetException ite) {
            logger.debug("Exception encountered during invocation:", ite.getTargetException()); //$NON-NLS-1$
            throw ite.getTargetException(); // unpack the original exception
        }
    }

}
