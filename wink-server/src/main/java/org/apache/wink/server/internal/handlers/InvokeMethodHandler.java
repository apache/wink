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

import org.apache.wink.common.internal.application.ApplicationExceptionAttribute;
import org.apache.wink.common.internal.log.LogUtils;
import org.apache.wink.server.handlers.AbstractHandler;
import org.apache.wink.server.handlers.MessageContext;
//import org.apache.wink.server.internal.log.ResourceInvocation.ResourceInvocationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvokeMethodHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(InvokeMethodHandler.class);

    @Override
    public void handleRequest(MessageContext context) throws Throwable {
        // vars declared outside of try block so we can log them in case of exception
        Method javaMethod = null;
        Object instance = null;
        Object[] parameters = null;
        try {
            SearchResult searchResult = context.getAttribute(SearchResult.class);
            javaMethod = searchResult.getMethod().getMetadata().getReflectionMethod();
            parameters = searchResult.getInvocationParameters();
            instance = searchResult.getResource().getInstance(context);
            if (logger.isTraceEnabled()) {
                logger
                    .trace("Invoking method {} of declaring class {} on the instance of a class {}@{} with parameters {}", //$NON-NLS-1$
                           new Object[] {javaMethod.getName(),
                               javaMethod.getDeclaringClass().getName(),
                               instance.getClass().getName(),
                               Integer.toHexString(System.identityHashCode(instance)),
                               Arrays.toString(parameters)});
            }
//            ResourceInvocationData resInvocationData =
//                context.getAttribute(ResourceInvocationData.class);
//            if (resInvocationData != null) {
//                resInvocationData.addInvocation(context);
//            }
            Object result = javaMethod.invoke(instance, parameters);
            context.setResponseEntity(result);
        } catch (InvocationTargetException ite) {
            try {
                String newLine = System.getProperty("line.separator"); //$NON-NLS-1$
                String debugMsg = String.format("%s with message \"%s\" was encountered during invocation of method %s of declaring class %s on the instance of a class %s@%s with parameters %s" + newLine + "%s", //$NON-NLS-1$ $NON-NLS-2$
                        new Object[] {
                        ite.getTargetException().getClass().getName(),
                        ite.getTargetException().getMessage(),
                        javaMethod == null ? "UNKNOWN" : javaMethod.getName(), //$NON-NLS-1$
                        javaMethod == null ? "UNKNOWN" : javaMethod.getDeclaringClass().getName(), //$NON-NLS-1$
                        instance == null ? "UNKNOWN" : instance.getClass().getName(), //$NON-NLS-1$
                        instance == null ? "UNKNOWN" : Integer.toHexString(System.identityHashCode(instance)), //$NON-NLS-1$
                        parameters == null ? "UNKNOWN" : Arrays.toString(parameters), //$NON-NLS-1$
                        // send exception through stackToString because it may have been intentionally thrown
                        // from resource method; we don't want to scare the log readers, so it's recorded as DEBUG
                        LogUtils.stackToDebugString(ite.getTargetException())
                        }
                );
                context.setAttribute(ApplicationExceptionAttribute.class, new ApplicationExceptionAttribute(debugMsg));
            } catch (Throwable t) {
                // just to be extra super duper cautious.  It'll still be logged, just not via the format above.
                logger.trace("Could not format log output for exception originating in provider.", t);
            }
                
            throw ite.getTargetException(); // unpack the original exception
        }
    }
    
}
