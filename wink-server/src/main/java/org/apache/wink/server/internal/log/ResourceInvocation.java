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
 */
package org.apache.wink.server.internal.log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Properties;

import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.apache.wink.server.internal.handlers.SearchResult;
import org.apache.wink.server.internal.registry.ResourceInstance;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs the resource invocation as it goes through each level (i.e. sub-resource
 * locator) in a table in DEBUG mode. Logs the resource metadata for each level
 * in TRACE mode.
 */
public class ResourceInvocation implements RequestHandler {

    public static class ResourceInvocationData {
        private List<ResourceRecord> resourceMetadata = new ArrayList<ResourceRecord>();

        private List<MethodMetadata> methodMeta       = new ArrayList<MethodMetadata>();

        public List<ResourceRecord> getResourceMetadata() {
            return resourceMetadata;
        }

        public List<MethodMetadata> getMethodMetadata() {
            return methodMeta;
        }

        /**
         * Called every single time a resource has been invoked.
         * 
         * @param context
         */
        public void addInvocation(MessageContext context) {
            try {
                ResourceInvocationData data = context.getAttribute(ResourceInvocationData.class);
                if (data == null) {
                    return;
                }
                SearchResult result = context.getAttribute(SearchResult.class);
                data.getResourceMetadata().add(result.getResource().getRecord());
                if (result.isFound()) {
                    data.getMethodMetadata().add(result.getMethod().getMetadata());
                }
            } catch (Exception e) {
                logger.trace("Encountered exception while calling addInvocation", e);
            }
        }
    }

    final private static Logger logger = LoggerFactory.getLogger(ResourceInvocation.class);

    public void init(Properties props) {
        /* do nothing */
    }

    public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
        logger.trace("handleRequest({}, {}) entry", context, chain);
        try {
            if (logger.isDebugEnabled()) {
                logStartRequest(context);
            }
            chain.doChain(context);
        } finally {
            if (logger.isDebugEnabled()) {
                logFinishRequest(context);
            }
        }
        logger.trace("handleRequest({}, {}) exit", context, chain);
    }

    private void logStartRequest(MessageContext context) {
        /*
         * setup for some resource invocation logging
         */
        context.setAttribute(ResourceInvocationData.class, new ResourceInvocationData());
    }

    public void logFinishRequest(MessageContext context) {
        try {
            ResourceInvocationData data = context.getAttribute(ResourceInvocationData.class);
            if (data == null) {
                return;
            }

            List<ResourceRecord> resClassData = data.getResourceMetadata();
            List<MethodMetadata> resMethodData = data.getMethodMetadata();

            StringBuffer sb = new StringBuffer();
            Formatter f = new Formatter(sb);
            int size = resMethodData.size();

            for (int i = 0; i < size; ++i) {
                f.format("%n-> %1$s.%2$s", resClassData.get(i).getMetadata().getResourceClass()
                    .getName(), prettyMethodPrint(resMethodData.get(i).getReflectionMethod()));
                if (logger.isTraceEnabled()) {
                    LogUtilities
                        .logResourceMetadata(Collections.singletonList(resClassData.get(i)),
                                             logger,
                                             true,
                                             true);
                }
            }

            SearchResult result = context.getAttribute(SearchResult.class);
            if (result.isError()) {
                ResourceInstance instance = result.getResource();
                if (instance != null) {
                    f.format("%n-> %1$s.(No matching method)", instance.getResourceClass()
                        .getName());
                    if (logger.isTraceEnabled()) {
                        LogUtilities.logResourceMetadata(Collections.singletonList(result
                            .getResource().getRecord()), logger, true, true);
                    }
                } else {
                    f.format("No resource instance was invoked.");
                }
            }
            logger.debug("Resource invocation:{}", sb);
        } catch (Exception e) {
            logger.trace("Encountered exception while calling logFinishRequest", e);
        }
    }

    private String prettyMethodPrint(Method javaMethod) {
        if (javaMethod == null) {
            return "";
        }
        StringBuffer methodName = new StringBuffer();
        methodName.append(javaMethod.getName());
        boolean isFirst = true;
        methodName.append("(");
        for (Class<?> paramTypes : javaMethod.getParameterTypes()) {
            if (!isFirst) {
                methodName.append(",");
            } else {
                isFirst = false;
            }
            methodName.append(paramTypes.getSimpleName());
        }
        methodName.append(")");
        return methodName.toString();
    }
}
