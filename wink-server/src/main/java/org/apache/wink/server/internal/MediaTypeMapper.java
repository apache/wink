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

package org.apache.wink.server.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.internal.utils.MediaTypeUtils;

/**
 * Allows to map response media types to other media types based on user agent.
 * The purpose is to overcome deficients in user agents.
 */
public final class MediaTypeMapper {

    private static final Logger logger = LoggerFactory.getLogger(MediaTypeMapper.class);

    private interface MappingRecord {

        boolean match(String userAgent, MediaType responseMediaType);

        MediaType getReplacement();

    }

    private static class AgentStartsWith implements MappingRecord {

        private String    userAgentStartsWith;
        private MediaType responseType;
        private MediaType replacementType;

        public AgentStartsWith(String userAgentStartsWith,
                               String responseType,
                               String replacementType) {
            if (userAgentStartsWith == null)
                throw new NullPointerException();
            this.userAgentStartsWith = userAgentStartsWith;
            this.responseType = MediaType.valueOf(responseType);
            this.replacementType = MediaType.valueOf(replacementType);
        }

        public boolean match(String userAgent, MediaType responseMediaType) {
            return userAgent.startsWith(userAgentStartsWith) && MediaTypeUtils
                .equalsIgnoreParameters(responseMediaType, responseType);
        }

        public MediaType getReplacement() {
            return replacementType;
        }
    }

    private List<MappingRecord> mappings = new ArrayList<MappingRecord>();

    public void setMappings(List<Map<String, String>> mappings) {
        for (Map<String, String> record : mappings) {
            String userAgent = record.get("userAgentStartsWith"); //$NON-NLS-1$
            String resultMimeType = record.get("resultMediaType"); //$NON-NLS-1$
            String typeToSend = record.get("typeToSend"); //$NON-NLS-1$
            addMapping(userAgent, resultMimeType, typeToSend);
        }
    }

    public void addMapping(String userAgent, String resultMimeType, String typeToSend) {
        if (userAgent == null || resultMimeType == null || typeToSend == null) {
            logger.warn("Record {} is not complete => ignored", userAgent);
            return;
        }
        this.mappings.add(new AgentStartsWith(userAgent, resultMimeType, typeToSend));
    }

    /**
     * Maps the real content type to value that should be written to
     * Content-Type header.
     * 
     * @param responseMediaType media type of the response
     * @param userAgent User-Agent header; null is allowed
     * @return responseMediaType parameter or some non-null value
     */
    public MediaType mapOutputMediaType(MediaType responseMediaType, String userAgent) {
        if (userAgent != null) {
            for (MappingRecord mappingRecord : mappings) {
                if (mappingRecord.match(userAgent, responseMediaType)) {
                    return mappingRecord.getReplacement();
                }
            }
        }
        return responseMediaType; // returning the same
    }

}
