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

package org.apache.wink.client.internal.handlers;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.handlers.HandlerContext;
import org.apache.wink.client.handlers.InputStreamAdapter;
import org.apache.wink.client.handlers.OutputStreamAdapter;

public class HandlerContextImpl implements HandlerContext {

    private ListIterator<ClientHandler>     chain;
    private LinkedList<OutputStreamAdapter> osAdapters;
    private LinkedList<InputStreamAdapter>  isAdapters;

    public HandlerContextImpl(List<ClientHandler> handlers) {
        this.chain = handlers.listIterator();
        osAdapters = new LinkedList<OutputStreamAdapter>();
        isAdapters = new LinkedList<InputStreamAdapter>();
    }

    public ClientResponse doChain(ClientRequest request) throws Exception {
        if (!chain.hasNext()) {
            return null;
        }

        try {
            return chain.next().handle(request, this);
        } finally {
            chain.previous();
        }
    }

    public void addOutputStreamAdapter(OutputStreamAdapter adapter) {
        osAdapters.addFirst(adapter);
    }

    public void addInputStreamAdapter(InputStreamAdapter adapter) {
        isAdapters.addFirst(adapter);
    }

    public List<OutputStreamAdapter> getOutputStreamAdapters() {
        return osAdapters;
    }

    public List<InputStreamAdapter> getInputStreamAdapters() {
        return isAdapters;
    }

}
