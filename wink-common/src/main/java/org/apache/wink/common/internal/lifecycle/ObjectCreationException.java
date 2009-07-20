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
package org.apache.wink.common.internal.lifecycle;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ObjectCreationException extends WebApplicationException {

    public ObjectCreationException() {
        super();
    }

    public ObjectCreationException(int status) {
        super(status);
    }

    public ObjectCreationException(Response response) {
        super(response);
    }

    public ObjectCreationException(Status status) {
        super(status);
    }

    public ObjectCreationException(Throwable cause, int status) {
        super(cause, status);
    }

    public ObjectCreationException(Throwable cause, Response response) {
        super(cause, response);
    }

    public ObjectCreationException(Throwable cause, Status status) {
        super(cause, status);
    }

    public ObjectCreationException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 3590157052851377676L;

}
