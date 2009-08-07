/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.wink.itest.exceptionmappers.nullconditions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GuestbookThrowExceptionMapper implements ExceptionMapper<GuestbookThrowException> {

    public Response toResponse(GuestbookThrowException arg0) {
        /*
         * throwing exception/error in here should cause a HTTP 500 status to
         * occur
         */

        if (arg0.getMessage().contains("exception")) {
            throw new GuestbookNullException();
        } else {
            throw new Error("error");
        }

        // TODO: throw this inside a subresource locator
    }

}
