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

import java.util.Formatter;
import java.util.List;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.registry.ProvidersRegistry.ProviderRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs the provider information.
 */
public class Providers {
    private static final String     CUSTOM                       = "Custom?";

    private static final String     CONSUMES_MEDIA_TYPE          = "Consumes Media Type";

    private static final String     PRODUCES_MEDIA_TYPE          = "Produces Media Type";

    private static final String     PROVIDER_FORMAT_LINE         = "%n%1$-35s %2$-25s %3$-8s %4$s";
    
    private static final String     EXCEPTION_MAPPER_FORMAT_LINE = "%n%1$-25s %2$-8s %3$s";
    
    private static final String     PROVIDER_CLASS               = "Provider Class";

    private static final String     GENERIC_TYPE                 = "Generic Type";

    /*
     * break the table into rows of BREAK_POINT. this may eliminate the
     * accumulation of a massive string (i.e. thousands of resources)
     */
    private static final int        BREAK_POINT                  = 20;

    private static final Logger     logger                       =
                                                                     LoggerFactory
                                                                         .getLogger(Providers.class);

    private final ProvidersRegistry providersRegistry;

    public Providers(ProvidersRegistry providersRegistry) {
        this.providersRegistry = providersRegistry;
    }

    public void log() {
        try {
            if (logger.isInfoEnabled()) {
                boolean isProviderDefined = false;

                /*
                 * readers
                 */
                List<ProviderRecord<?>> providerRecords =
                    providersRegistry.getMessageBodyReaderRecords();
                boolean temp = log(providerRecords, "MessageBodyReader");
                if (temp) {
                    isProviderDefined = true;
                }

                /*
                 * writers
                 */
                providerRecords = providersRegistry.getMessageBodyWriterRecords();
                temp = log(providerRecords, "MessageBodyWriter");
                if (temp) {
                    isProviderDefined = true;
                }

                /*
                 * exception mappers
                 */
                providerRecords = providersRegistry.getExceptionMapperRecords();
                temp = log(providerRecords);
                if (temp) {
                    isProviderDefined = true;
                }

                /*
                 * context resolvers
                 */
                providerRecords = providersRegistry.getContextResolverRecords();
                temp = log(providerRecords, "ContextResolver");
                if (temp) {
                    isProviderDefined = true;
                }

                /*
                 * debug or more
                 */
                if (isProviderDefined && logger.isDebugEnabled()) {
                    logTable();
                }

                if (!isProviderDefined) {
                    logger.info(Messages.getMessage("noJAXRSApplicationDefinedProviders"));
                }
            }
        } catch (Exception e) {
            logger.trace("Could not print the entire providers metadata", e);
        }
    }

    private boolean log(List<ProviderRecord<?>> records, String providerType) {
        if (records != null && !records.isEmpty()) {
            for (ProviderRecord<?> record : records) {
                if (record.getGenericType() == null || Object.class.equals(record.getGenericType())) {
                    logger.info(Messages
                        .getMessage("registeredJAXRSProviderWithMediaTypeAndAllGenericType", record
                            .getProviderClass().getName(), providerType, record.getMediaType()));
                } else {
                    logger.info(Messages.getMessage("registeredJAXRSProviderWithMediaType", record
                        .getProviderClass().getName(), providerType, record.getGenericType()
                        .getName(), record.getMediaType()));
                }
            }
            return true;
        }
        return false;
    }

    private boolean log(List<ProviderRecord<?>> records) {
        if (records != null && !records.isEmpty()) {
            for (ProviderRecord<?> record : records) {
                if (record.getGenericType() == null || Object.class.equals(record.getGenericType())) {
                    logger.info(Messages
                        .getMessage("registeredJAXRSProviderWithoutMediaTypeAndAllGenericType",
                                    record.getProviderClass().getName(),
                                    "ExceptionMapper"));
                } else {
                    logger.info(Messages.getMessage("registeredJAXRSProviderWithoutMediaType",
                                                    record.getProviderClass().getName(),
                                                    "ExceptionMapper",
                                                    record.getGenericType().getName()));
                }
            }
            return true;
        }
        return false;
    }

    private void log(List<ProviderRecord<?>> providerRecords,
                     String providerType,
                     String mediaTypeHeader) {
        StringBuffer sb = new StringBuffer();

        if (!providerRecords.isEmpty()) {
            Formatter f = new Formatter(sb);
            f.format(PROVIDER_FORMAT_LINE, mediaTypeHeader, GENERIC_TYPE, CUSTOM, PROVIDER_CLASS);

            int counter = 0;

            for (ProviderRecord<?> record : providerRecords) {
                ++counter;
                f.format(PROVIDER_FORMAT_LINE, record.getMediaType(), record.getGenericType()
                    .getSimpleName(), !record.isSystemProvider(), record.getProviderClass()
                    .getName());

                if (counter % BREAK_POINT == 0) {
                    logger.debug("The following JAX-RS {} providers are registered:{}",
                                 providerType,
                                 sb);
                    sb = new StringBuffer();
                    f = new Formatter(sb);
                    f.format(PROVIDER_FORMAT_LINE,
                             mediaTypeHeader,
                             GENERIC_TYPE,
                             CUSTOM,
                             PROVIDER_CLASS);
                }
            }

            if (counter % BREAK_POINT != 0) {
                logger.debug("The following JAX-RS {} providers are registered:{}",
                             providerType,
                             sb);
            }
        }
    }

    private void logExceptionMapper(List<ProviderRecord<?>> providerRecords) {
        StringBuffer sb = new StringBuffer();

        if (!providerRecords.isEmpty()) {
            Formatter f = new Formatter(sb);
            f.format(EXCEPTION_MAPPER_FORMAT_LINE, GENERIC_TYPE, CUSTOM, PROVIDER_CLASS);
            int counter = 0;
            for (ProviderRecord<?> record : providerRecords) {
                ++counter;

                f.format(EXCEPTION_MAPPER_FORMAT_LINE,
                         record.getGenericType().getSimpleName(),
                         !record.isSystemProvider(),
                         record.getProviderClass().getName());

                if (counter % BREAK_POINT == 0) {
                    logger.debug("The following JAX-RS {} providers are registered:{}",
                                 "ExceptionMapper",
                                 sb);
                    sb = new StringBuffer();
                    f = new Formatter(sb);
                    f.format(EXCEPTION_MAPPER_FORMAT_LINE, GENERIC_TYPE, CUSTOM, PROVIDER_CLASS);
                }
            }

            if (counter % BREAK_POINT != 0) {
                logger.debug("The following JAX-RS {} providers are registered:{}",
                             "ExceptionMapper",
                             sb);
            }
        }
    }

    private void logTable() {
        List<ProviderRecord<?>> providerRecords = providersRegistry.getMessageBodyReaderRecords();
        log(providerRecords, "MessageBodyReader", CONSUMES_MEDIA_TYPE);

        providerRecords = providersRegistry.getMessageBodyWriterRecords();
        log(providerRecords, "MessageBodyWriter", PRODUCES_MEDIA_TYPE);

        providerRecords = providersRegistry.getExceptionMapperRecords();
        logExceptionMapper(providerRecords);

        providerRecords = providersRegistry.getContextResolverRecords();
        log(providerRecords, "ContextResolver", PRODUCES_MEDIA_TYPE);
    }
}
