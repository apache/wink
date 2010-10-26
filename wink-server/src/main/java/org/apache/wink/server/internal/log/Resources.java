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

import java.util.List;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs all the resource information.
 */
public class Resources {

    private static final Logger    logger = LoggerFactory.getLogger(Resources.class);

    private final ResourceRegistry resourceRegistry;

    public Resources(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    public void log() {
        logger.trace("log() entry");
        try {
            if (logger.isInfoEnabled()) {
                List<ResourceRecord> resourceRecords = resourceRegistry.getRecords();
                logResourceInfo(resourceRecords);

                if (logger.isDebugEnabled()) {
                    if (resourceRecords.isEmpty()) {
                        logger
                            .debug("There are no @javax.ws.rs.Path annotated classes defined in the application.");
                    }
                    LogUtilities.logResourceMetadata(resourceRecords, logger, false, false);
                }
            }
        } catch (Exception e) {
            logger.trace("Could not produce all the resource metadata.", e);
        }
        logger.trace("log() exit");
    }

    private void logResourceInfo(List<ResourceRecord> resourceRecords) {
        for (ResourceRecord record : resourceRecords) {
            try {
                final String resourceClassName = record.getMetadata().getResourceClass().getName();
                final ClassMetadata resourceMetadata = record.getMetadata();
                final String resourcePath = resourceMetadata.getPath();
                logger.info(Messages.getMessage("serverRegisterJAXRSResourceWithPath",
                                                resourceClassName,
                                                resourcePath));
            } catch (Exception e) {
                logger.trace("Could not print all of the resource metadata.", e);
            }
        }
    }
}
