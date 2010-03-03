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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.wink.server.handlers.MediaTypeMappingRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to map response media types to other media types based on user agent.
 * The purpose is to overcome deficients in user agents.
 */
public final class MediaTypeMapper {

    private static final Logger          logger   = LoggerFactory.getLogger(MediaTypeMapper.class);

    private List<MediaTypeMappingRecord> mappings = new ArrayList<MediaTypeMappingRecord>();

    public void addMappings(List<? extends MediaTypeMappingRecord> records) {
        if (records == null) {
            logger.debug("No media type mapping records to add"); //$NON-NLS-1$
            return;
        }
        logger.debug("Media type mapping records to add: {}", records); //$NON-NLS-1$
        this.mappings.addAll(records);
    }

    /**
     * Maps the real content type to value that should be written to
     * Content-Type header.
     * 
     * @param responseMediaType media type of the response
     * @param userAgent User-Agent header; null is allowed
     * @return responseMediaType parameter or some non-null value
     */
    public MediaType mapOutputMediaType(MediaType responseMediaType, HttpHeaders requestHeaders) {
        for (MediaTypeMappingRecord mappingRecord : mappings) {
            logger.debug("Attempting to map media type using mapping record: {}", mappingRecord); //$NON-NLS-1$
            MediaType replacement = mappingRecord.match(requestHeaders, responseMediaType);
            if (replacement != null) {
                logger.debug("Mapped user media type to: {} using mapping record: {}", //$NON-NLS-1$
                             replacement,
                             mappingRecord);
                return replacement;
            }
        }
        logger.debug("Did not find a mapping record so returning original response media type: {}", //$NON-NLS-1$
                     responseMediaType);
        return responseMediaType; // returning the same
    }

}
