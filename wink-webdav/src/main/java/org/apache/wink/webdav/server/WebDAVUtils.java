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

import java.io.StringReader;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.xml.bind.Unmarshaller;

import org.apache.wink.common.http.HttpHeadersEx;
import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.internal.utils.HeaderUtils;
import org.apache.wink.server.internal.contexts.UriInfoImpl;
import org.apache.wink.server.internal.registry.ResourceInstance;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.apache.wink.webdav.WebDAVHeaders;
import org.apache.wink.webdav.model.Activelock;
import org.apache.wink.webdav.model.Lockdiscovery;
import org.apache.wink.webdav.model.Lockinfo;
import org.apache.wink.webdav.model.Prop;
import org.apache.wink.webdav.model.WebDAVModelHelper;

public class WebDAVUtils {

    // 1000 years ~ infinite timeout ("Infinite" constant is not accepted by MS
    // client)
    private static final String LOCK_TIMEOUT = "Second-31536000000";

    /**
     * Provides a default response with two additional headers for WebDAV and MS
     * compatibility.
     * 
     * @return
     */
    public static Response getOptions(UriInfo info) {
        List<ResourceInstance> matchedResourceInstances =
            ((UriInfoImpl)info).getMatchedResourceInstances();
        ResourceRegistry resourceRegistry =
            RuntimeContextTLS.getRuntimeContext().getAttribute(ResourceRegistry.class);
        Set<String> options = resourceRegistry.getOptions(matchedResourceInstances.get(0));
        String allowHeader = HeaderUtils.buildOptionsHeader(options);
        Response response =
            RuntimeDelegate.getInstance().createResponseBuilder().header(WebDAVHeaders.DAV, "1")
                .header(WebDAVHeaders.MS_AUTHOR_VIA, "DAV")
                .header(HttpHeadersEx.ALLOW, allowHeader).entity("").build();
        return response;
    }

    /**
     * This method does not perform a real lock but returns a 'dummy' lock
     * response for compatibility with MS Windows. It opens any resource as
     * read-only file when a lock is not received.
     * 
     * @param body the lock request xml
     * @return a response instance
     */
    public static Response msCompatibilityLock(String body) {

        // empty request means refreshing a lock
        // it should not happen since we set infinite timeouts
        // we do not respond properly - sending 200 OK with empty body
        if (body == null || body.length() == 0) {
            Response response = RuntimeDelegate.getInstance().createResponseBuilder().build();
            return response;
        }

        // parse the request
        Unmarshaller unmarshaller = WebDAVModelHelper.createUnmarshaller();
        Lockinfo lockinfo =
            WebDAVModelHelper.unmarshal(unmarshaller,
                                        new StringReader(body),
                                        Lockinfo.class,
                                        "lockinfo");

        // make a response
        Activelock activelock = new Activelock();
        // set lock type from the request
        activelock.setLocktype(lockinfo.getLocktype());
        // set lock scope from the request
        activelock.setLockscope(lockinfo.getLockscope());
        // set 0 depth
        activelock.setDepth("0");
        // set owner from the request
        activelock.setOwner(lockinfo.getOwner());
        // set infinite timeout
        activelock.setTimeout(LOCK_TIMEOUT);
        // a lock token is not necessary for MS compatibility

        Lockdiscovery lockdiscovery = new Lockdiscovery();
        lockdiscovery.getActivelock().add(activelock);
        Prop prop = new Prop();
        prop.setLockdiscovery(lockdiscovery);

        Response response =
            RuntimeDelegate.getInstance().createResponseBuilder().entity(prop).build();
        return response;
    }

    /**
     * This method does not perform a real unlock but returns a NO_CONTENT
     * response for compatibility with MS Windows.
     * 
     * @return a response instance
     */
    public static Response msCompatibilityUnlock() {
        Response response =
            RuntimeDelegate.getInstance().createResponseBuilder().status(HttpStatus.NO_CONTENT
                .getCode()).build();
        return response;
    }

}
