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

package org.apache.wink.client.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.ext.Providers;

import org.apache.wink.common.internal.contexts.ProvidersImpl;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.runtime.AbstractRuntimeContext;

public class ClientRuntimeContext extends AbstractRuntimeContext {

    public ClientRuntimeContext(ProvidersRegistry providersRegistry) {
        setAttribute(Providers.class, new ProvidersImpl(providersRegistry, this));
    }

    public InputStream getInputStream() throws IOException {
        return null;
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
