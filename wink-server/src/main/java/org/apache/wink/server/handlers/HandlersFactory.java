/*******************************************************************************
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *     
 *      http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 *******************************************************************************/
package org.apache.wink.server.handlers;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * HandlersFactory is responsible to provide the user handlers to the
 * application.
 * <p>
 * The user should extend this class and override the relevant methods.
 * <p>
 * The sub-classes MUST have the public default constructor.
 */
public abstract class HandlersFactory {

    /**
     * Returns a list of user request handlers. The user request handlers are
     * invoked before the actual invocation of the relevant method on the
     * resource in the order specified by the returned list.
     * 
     * @return list of request handlers
     */
    public List<? extends RequestHandler> getRequestHandlers() {
        return Collections.emptyList();
    }

    /**
     * Returns a list of user response handlers. The user response handlers are
     * invoked after the actual invocation of the relevant method on the
     * resource in the order specified by the returned list.
     * 
     * @return list of response handlers
     */
    public List<? extends ResponseHandler> getResponseHandlers() {
        return Collections.emptyList();
    }

    /**
     * Returns a list of user error handlers. The user error handlers are
     * invoked before the "flushing" the result to user in the order specified
     * by the returned list.
     * 
     * @return list of response handlers
     */
    public List<? extends ResponseHandler> getErrorHandlers() {
        return Collections.emptyList();
    }

}
