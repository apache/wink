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

import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.http.ContentDispositionHeader;

/**
 * Partial implementation of Content-Disposition header. See <a
 * href="http://www.ietf.org/rfc/rfc2183.txt">RFC 2183</a>
 */
public class ContentDispositionHeaderDelegate implements HeaderDelegate<ContentDispositionHeader> {

    private static final String INLINE_DISPOSITION_TYPE     = "inline";    // disposition //$NON-NLS-1$
                                                                            // type
                                                                            // values
                                                                            // are
                                                                            // case
                                                                            // insensitive
    private static final String ATTACHMENT_DISPOSITION_TYPE = "attachment"; //$NON-NLS-1$

    public ContentDispositionHeader fromString(String value) throws IllegalArgumentException {
        // TODO: implement according to spec
        throw new UnsupportedOperationException();
    }

    public String toString(ContentDispositionHeader header) {
        String fileName = header.getFileName();
        StringBuilder value = new StringBuilder();
        value.append(header.isAttachment() ? ATTACHMENT_DISPOSITION_TYPE : INLINE_DISPOSITION_TYPE);
        if (fileName != null) {
            value.append("; filename=\""); //$NON-NLS-1$
            value.append(fileName);
            String defaultExtension = header.getDefaultExtension();
            if (defaultExtension != null) {
                value.append('.');
                value.append(defaultExtension);
            }
            value.append('"');
        }
        return value.toString();
    }

}
