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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.wink.server.handlers;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * MediaTypeMappingRecords can map response media types to a different response
 * media type if necessary.
 */
public interface MediaTypeMappingRecord {

    /**
     * Maps response media types to a different response media type if
     * necessary. If the match does not apply, return null.
     * 
     * @param requestHeaders the current request headers
     * @param responseMediaType the response media type
     * @return the media type to use instead
     */
    MediaType match(HttpHeaders requestHeaders, MediaType responseMediaType);

}
