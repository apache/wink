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
 *  
 */
package org.apache.wink.server.internal.contexts;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.common.internal.contexts.MediaTypeCharsetAdjuster;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.utils.ProviderUtils;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMediaTypeCharsetAdjuster implements MediaTypeCharsetAdjuster {

    final private static ServerMediaTypeCharsetAdjuster instance =
                                                                     new ServerMediaTypeCharsetAdjuster();
    // enforce singleton
    private ServerMediaTypeCharsetAdjuster() {
    }
    
    public static ServerMediaTypeCharsetAdjuster getInstance() {
        return instance;
    }

    private static final Logger logger =
                                           LoggerFactory
                                               .getLogger(ServerMediaTypeCharsetAdjuster.class);

    public MediaType setDefaultCharsetOnMediaTypeHeader(MultivaluedMap<String, Object> httpHeaders,
                                                        MediaType mediaType) {
        logger.debug("setDefaultCharsetOnMediaTypeHeader({}, {}) entry", httpHeaders, mediaType); //$NON-NLS-1$

        RuntimeContext context = RuntimeContextTLS.getRuntimeContext();
        // we're on the server, so this is a safe cast
        DeploymentConfiguration config = (DeploymentConfiguration)context.getAttribute(WinkConfiguration.class);
        if (config.isDefaultResponseCharset() || config.isUseAcceptCharset()) {
            if (httpHeaders != null && (httpHeaders.isEmpty() || httpHeaders
                    .get(HttpHeaders.CONTENT_TYPE) == null)) {
                // only correct the MediaType if the MediaType was not explicitly
                // set
                logger.debug("Media Type not explicitly set on Response so going to correct charset parameter if necessary"); //$NON-NLS-1$
                if (ProviderUtils.getCharsetOrNull(mediaType) == null) { //$NON-NLS-1$
                    try {
                        String charsetValue = "UTF-8";
                        if (config.isUseAcceptCharset()) {
                            // configuration says to inspect and use the Accept-Charset header to determine response charset
                            HttpHeaders requestHeaders = null;
                            if (context != null) {
                                requestHeaders = context.getHttpHeaders();
                            }
                            charsetValue = ProviderUtils.getCharset(mediaType, requestHeaders);
                        }
                        String newMediaTypeStr = mediaType.toString() + ";charset=" + charsetValue;
                        mediaType = MediaType.valueOf(newMediaTypeStr);
                        httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, newMediaTypeStr);
                        logger.debug("Changed media type to be {} in Content-Type HttpHeader", newMediaTypeStr); //$NON-NLS-1$
                    } catch (Exception e) {
                        logger.debug("Caught exception while trying to set the charset", e); //$NON-NLS-1$
                    }

                }
            }
        } else {
            logger.debug("No default charset was applied to the response Content-Type header due to deployment configuration directive.");  // $NON-NLS-1$
        }

        logger.debug("setDefaultCharsetOnMediaTypeHeader() exit returning {}", mediaType); //$NON-NLS-1$
        return mediaType;
    }

}
