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

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.http.EntityTagMatchHeader;

public class EntityTagMatchHeaderDelegate implements HeaderDelegate<EntityTagMatchHeader> {

    private final static RuntimeDelegate           delegate                   =
                                                                                  RuntimeDelegate
                                                                                      .getInstance();
    private static final HeaderDelegate<EntityTag> ENTITY_TAG_HEADER_DELEGATE =
                                                                                  delegate
                                                                                      .createHeaderDelegate(EntityTag.class);

    public EntityTagMatchHeader fromString(String value) throws IllegalArgumentException {
        String[] valueTokens = value.split(","); //$NON-NLS-1$
        EntityTagMatchHeader ifMatchHeader = new EntityTagMatchHeader();
        for (String token : valueTokens) {
            ifMatchHeader.addETag(ENTITY_TAG_HEADER_DELEGATE.fromString(token));
        }
        return ifMatchHeader;
    }

    public String toString(EntityTagMatchHeader value) {
        throw new UnsupportedOperationException();
    }

}
