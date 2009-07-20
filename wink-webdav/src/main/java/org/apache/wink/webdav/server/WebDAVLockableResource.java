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

package org.apache.wink.webdav.server;

import java.io.IOException;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.wink.webdav.WebDAVMethod;

/**
 * Base resource for WebDAV-enabled Lockable resources.
 */
public abstract class WebDAVLockableResource extends WebDAVResource {

    /**
     * This method does not perform a real lock but returns a 'dummy' lock
     * response for compatibility with MS Windows. It opens any resource as
     * read-only file when a lock is not received.
     * 
     * @return response
     * @throws IOException when an I/O error occurs
     */
    @WebDAVMethod.LOCK
    @Produces(MediaType.APPLICATION_XML)
    public Response msCompatibilityLock(String body) {
        return WebDAVUtils.msCompatibilityLock(body);
    }

    /**
     * This method does not perform a real unlock but returns a NO_CONTENT
     * response for compatibility with MS Windows.
     * 
     * @return
     */
    @WebDAVMethod.UNLOCK
    public Response msCompatibilityUnlock() {
        return WebDAVUtils.msCompatibilityUnlock();
    }

}
