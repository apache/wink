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
import java.util.Formatter;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.SubResourceRecord;
import org.slf4j.Logger;

public class LogUtilities {

    /*
     * break the table into rows of BREAK_POINT. this may eliminate the
     * accumulation of a massive string (i.e. thousands of resources)
     */
    static final int BREAK_POINT = 20;

    private LogUtilities() {
        /* do nothing */
    }

    static String constructMethodString(Method javaMethod) {
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

    static String constructMediaTypeString(Set<MediaType> mtSet) {
        if (mtSet == null || mtSet.isEmpty()) {
            return "[*/*]";
        }
        return mtSet.toString();
    }

    public static void logResourceMetadata(List<ResourceRecord> resourceRecords,
                                        Logger logger,
                                        boolean isTrace,
                                        boolean isLogForOneResource) {
        StringBuffer sb = new StringBuffer();
        Formatter f = new Formatter(sb);
        f.format("%n%1$-80s %2$-13s %3$-20s %4$-20s %5$s",
                 "Path",
                 "HTTP Method",
                 "Consumes",
                 "Produces",
                 "Resource Method");

        int counter = 0;
        for (ResourceRecord record : resourceRecords) {
            try {
                final String resourceClassName = record.getMetadata().getResourceClass().getName();
                final ClassMetadata resourceMetadata = record.getMetadata();
                final String resourcePath = resourceMetadata.getPath();
                for (MethodMetadata methodMetadata : resourceMetadata.getResourceMethods()) {
                    ++counter;
                    try {
                        String path = resourcePath;
                        String httpMethod = methodMetadata.getHttpMethod();

                        String consumes = constructMediaTypeString(methodMetadata.getConsumes());
                        String produces = constructMediaTypeString(methodMetadata.getProduces());

                        String methodName =
                            constructMethodString(methodMetadata.getReflectionMethod());

                        /* path is null so regular resource method */
                        f.format("%n%1$-80s %2$-13s %3$-20s %4$-20s %5$s.%6$s",
                                 path,
                                 httpMethod,
                                 consumes,
                                 produces,
                                 resourceClassName,
                                 methodName);
                    } catch (Exception e) {
                        logger.trace("Could not print the entire method metadata for {}",
                                     resourceClassName,
                                     e);
                    }

                    if (counter % LogUtilities.BREAK_POINT == 0) {
                        if (isTrace) {
                            if (isLogForOneResource) {
                                logger
                                    .trace("Resource information for {}:{}", resourceMetadata.getResourceClass().getName(), sb); //$NON-NLS-1$
                            } else {
                                logger.trace(Messages.getMessage("registeredResources", sb)); //$NON-NLS-1$
                            }
                        } else {
                            if (isLogForOneResource) {
                                logger
                                    .debug("Resource information for {}:{}", resourceMetadata.getResourceClass().getName(), sb); //$NON-NLS-1$
                            } else {
                                logger.debug(Messages.getMessage("registeredResources", sb)); //$NON-NLS-1$
                            }
                        }
                        sb = new StringBuffer();
                        f = new Formatter(sb);
                        f.format("%n%1$-80s %2$-13s %3$-20s %4$-20s %5$s",
                                 "Path",
                                 "HTTP Method",
                                 "Consumes",
                                 "Produces",
                                 "Resource Method");
                    }
                }

                for (SubResourceRecord subResourceRecord : record.getSubResourceRecords()) {
                    ++counter;

                    try {
                        MethodMetadata method = subResourceRecord.getMetadata();

                        StringBuilder path = new StringBuilder(resourcePath);
                        if (!resourcePath.endsWith("/")) {
                            path.append("/");
                        }
                        path.append(method.getPath());

                        String httpMethod = method.getHttpMethod();
                        if (httpMethod == null) {
                            httpMethod = "(Sub-Locator)";
                        }

                        String consumes = constructMediaTypeString(method.getConsumes());
                        String produces = constructMediaTypeString(method.getProduces());

                        String methodName = constructMethodString(method.getReflectionMethod());

                        f.format("%n%1$-80s %2$-13s %3$-20s %4$-20s %5$s.%6$s",
                                 path,
                                 httpMethod,
                                 consumes,
                                 produces,
                                 resourceClassName,
                                 methodName);
                    } catch (Exception e) {
                        logger.trace("Could not print the entire method metadata for {}",
                                     resourceClassName,
                                     e);
                    }

                    if (counter % LogUtilities.BREAK_POINT == 0) {
                        if (isTrace) {
                            if (isLogForOneResource) {
                                logger
                                    .trace("Resource information for {}:{}", resourceMetadata.getResourceClass().getName(), sb); //$NON-NLS-1$
                            } else {
                                logger.trace(Messages.getMessage("registeredResources", sb)); //$NON-NLS-1$
                            }
                        } else {
                            if (isLogForOneResource) {
                                logger
                                    .debug("Resource information for {}:{}", resourceMetadata.getResourceClass().getName(), sb); //$NON-NLS-1$
                            } else {
                                logger.debug(Messages.getMessage("registeredResources", sb)); //$NON-NLS-1$
                            }
                        }
                        sb = new StringBuffer();
                        f = new Formatter(sb);
                        f.format("%n%1$-80s %2$-13s %3$-20s %4$-20s %5$s",
                                 "Path",
                                 "HTTP Method",
                                 "Consumes",
                                 "Produces",
                                 "Resource Method");
                    }
                }
            } catch (Exception e) {
                logger.trace("Could not print the entire resource metadata", e);
            }
        }

        if (counter % LogUtilities.BREAK_POINT != 0) {
            if (isTrace) {
                if (isLogForOneResource) {
                    logger
                        .trace("Resource information for {}:{}", resourceRecords.get(0).getMetadata().getResourceClass().getName(), sb); //$NON-NLS-1$
                } else {
                    logger.trace(Messages.getMessage("registeredResources", sb)); //$NON-NLS-1$
                }
            } else {
                if (isLogForOneResource) {
                    logger
                        .debug("Resource information for {}:{}", resourceRecords.get(0).getMetadata().getResourceClass().getName(), sb); //$NON-NLS-1$
                } else {
                    logger.debug(Messages.getMessage("registeredResources", sb)); //$NON-NLS-1$
                }
            }
            sb = new StringBuffer();
            f = new Formatter(sb);
            f.format("%n%1$-80s %2$-13s %3$-20s %4$-20s %5$s",
                     "Path",
                     "HTTP Method",
                     "Consumes",
                     "Produces",
                     "Resource Method");
        }
    }
}
