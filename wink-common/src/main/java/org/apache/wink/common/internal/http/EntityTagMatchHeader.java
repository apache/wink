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
package org.apache.wink.common.internal.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.EntityTag;

/**
 * Used to represent headers that contains multiple Entity Tags (like If-Match,
 * If-None-Match)
 */
public class EntityTagMatchHeader {

    private List<EntityTag> eTags      = new ArrayList<EntityTag>();
    private boolean         isWildcard = false;

    public boolean addETag(EntityTag eTag) {
        if (isWildcard) {
            return true;
        }
        if (eTag.getValue().equals("*")) { //$NON-NLS-1$
            isWildcard = true;
            eTags.clear();
            return true;
        }
        return eTags.add(eTag);
    }

    public List<EntityTag> getETags() {
        return Collections.unmodifiableList(eTags);
    }

    public boolean isMatch(EntityTag eTag) {
        if (eTag == null) {
            return false;
        }
        if (isWildcard) {
            return true;
        }
        String value = eTag.getValue();
        for (EntityTag e : eTags) {
            if (value.equals(e.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (isWildcard) {
            return "*"; //$NON-NLS-1$
        }
        return eTags.toString();
    }
}
