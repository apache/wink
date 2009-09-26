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

import java.util.List;

/**
 * <p>
 * MediaTypeMapperFactory is responsible to provide media type mappings from an
 * application's response media type to a preferred media type. The use case is
 * when certain user agents do not know how to handle a specific response media
 * type (such as an application/*+xml) and must use another media type (such as
 * text/xml).
 * <p>
 * The user should extend this class and override the relevant methods.
 * <p>
 * The sub-classes MUST have the public default constructor.
 */
public abstract class MediaTypeMapperFactory {

    /**
     * Returns a list of media type mappings that will be called to map response
     * media types to preferred media types.
     * 
     * @return set of media type mapping records
     */
    public abstract List<? extends MediaTypeMappingRecord> getMediaTypeMappings();
}
