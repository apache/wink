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

package org.apache.wink.server.internal.utils;

import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.internal.handlers.ServerMessageContext;
import org.apache.wink.server.utils.LinkBuilders;
import org.apache.wink.server.utils.SingleLinkBuilder;
import org.apache.wink.server.utils.SystemLinksBuilder;

/**
 * Implementation of {@link LinkBuilders}.
 */
public final class LinkBuildersImpl implements LinkBuilders {

    private final MessageContext msgContext;

    public LinkBuildersImpl(MessageContext msgContext) {
        this.msgContext = msgContext;
    }

    public SingleLinkBuilder createSingleLinkBuilder() {
        return new SingleLinkBuilderImpl((ServerMessageContext)msgContext);
    }

    public SystemLinksBuilder createSystemLinksBuilder() {
        return new SystemLinksBuilderImpl((ServerMessageContext)msgContext);
    }

}
