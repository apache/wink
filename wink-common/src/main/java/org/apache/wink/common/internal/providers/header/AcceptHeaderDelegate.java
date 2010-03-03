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

package org.apache.wink.common.internal.providers.header;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.http.Accept;
import org.apache.wink.common.internal.utils.SoftConcurrentMap;
import org.apache.wink.common.internal.utils.StringUtils;

public class AcceptHeaderDelegate implements HeaderDelegate<Accept> {

    private static final SoftConcurrentMap<String, Accept> cache = new SoftConcurrentMap<String, Accept>();
    
    public Accept fromString(String value) throws IllegalArgumentException {
        Accept cached = cache.get(value);
        if (cached != null) {
            return cached;
        }
        
        // if there is no Accept header it means that all media types are
        // acceptable
        if (value == null) {
            value = MediaType.WILDCARD;
        }
        List<MediaType> list = new LinkedList<MediaType>();
        String[] mediaTypes = StringUtils.fastSplit(value, ","); //$NON-NLS-1$
        for (String mediaRange : mediaTypes) {
            mediaRange = mediaRange.trim();
            if (mediaRange.length() == 0) {
                continue;
            }
            list.add(MediaType.valueOf(mediaRange));
        }
        return cache.put(value, new Accept(list));
    }

    public String toString(Accept value) {
        StringBuilder result = new StringBuilder();
        for (MediaType valuedMediaType : value.getMediaTypes()) {
            if (result.length() != 0)
                result.append(", "); //$NON-NLS-1$
            result.append(valuedMediaType.toString());
        }
        return result.toString();
    }

}
