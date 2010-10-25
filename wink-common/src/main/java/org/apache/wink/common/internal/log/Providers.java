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

package org.apache.wink.common.internal.log;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Providers {

    private static final Logger                logger = LoggerFactory.getLogger(Providers.class);

    private Map<MessageBodyReader<?>, Boolean> mbrs   = null;
    private Map<MessageBodyWriter<?>, Boolean> mbws   = null;

    public void addMessageBodyReader(MessageBodyReader<?> reader, boolean result) {
        if (this.mbrs == null) {
            this.mbrs = new LinkedHashMap<MessageBodyReader<?>, Boolean>();
        }
        this.mbrs.put(reader, result);
    }

    public void addMessageBodyWriter(MessageBodyWriter<?> writer, boolean result) {
        if (this.mbws == null) {
            this.mbws = new LinkedHashMap<MessageBodyWriter<?>, Boolean>();
        }
        this.mbws.put(writer, result);
    }

    public void log() {
        if (logger.isDebugEnabled()) {
            if (this.mbrs != null && this.mbrs.size() > 0) {
                String providerResults = "";
                for (MessageBodyReader<?> mbr : this.mbrs.keySet()) {
                    providerResults +=
                        ("\n" + mbr.getClass().getCanonicalName() + ".isReadable() returned " + this.mbrs
                            .get(mbr));
                }
                logger.debug(providerResults);
            }

            if (this.mbws != null && this.mbws.size() > 0) {
                String providerResults = "";
                for (MessageBodyWriter<?> mbw : this.mbws.keySet()) {
                    providerResults +=
                        ("\n" + mbw.getClass().getCanonicalName() + ".isWritable() returned " + this.mbws
                            .get(mbw));
                }
                logger.debug(providerResults);
            }
        }
    }
}
