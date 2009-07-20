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

package org.apache.wink.webdav;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.HttpMethod;

/**
 * WebDAV HTTP methods.
 */
public enum WebDAVMethod {

    PROPFIND, PROPPATCH, MKCOL, COPY, MOVE, LOCK, UNLOCK;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @HttpMethod(value = "PROPFIND")
    public @interface PROPFIND {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @HttpMethod(value = "PROPPATCH")
    public @interface PROPPATCH {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @HttpMethod(value = "MKCOL")
    public @interface MKCOL {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @HttpMethod(value = "COPY")
    public @interface COPY {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @HttpMethod(value = "MOVE")
    public @interface MOVE {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @HttpMethod(value = "LOCK")
    public @interface LOCK {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @HttpMethod(value = "UNLOCK")
    public @interface UNLOCK {
    }
}
