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
 
package org.apache.wink.common;

import javax.xml.namespace.QName;


/**
 * List of exception codes.
 */
public final class RestErrorCodes {

    private RestErrorCodes() {
        // no instances
    }

    public static final QName CLIENT_ERROR = new QName(RestConstants.NAMESPACE_REST_SDK, "client-error");

    public static final QName APPLICATION_ERROR = new QName(RestConstants.NAMESPACE_REST_SDK, "application-error");

    public static final QName INTERNAL_SERVER_ERROR = new QName(RestConstants.NAMESPACE_REST_SDK, "internal-server-error");

    public static final QName DOCUMENT_NOT_FOUND = new QName(RestConstants.NAMESPACE_REST_SDK, "document-not-found");
    public static final QName COLLECTION_NOT_FOUND = new QName(RestConstants.NAMESPACE_REST_SDK, "collection-not-found");

    public static final QName REST_INVALID_PATH = new QName(RestConstants.NAMESPACE_REST_SDK, "rest-invalid-path");

    public static final QName DOCUMENT_ALREADY_EXISTS = new QName(RestConstants.NAMESPACE_REST_SDK, "document-already-exists");

    public static final QName NOT_IMPLEMENTED= new QName(RestConstants.NAMESPACE_REST_SDK, "not-implemented");

    public static final QName LANG_NOT_SPECIFIED= new QName(RestConstants.NAMESPACE_REST_SDK, "content-language-not-specified");

    public static final QName EXEC_NOT_FINISHED = new QName(RestConstants.NAMESPACE_REST_SDK, "document-execution-not-finished");

    public static final QName INVALID_PAGING_PARAMETER = new QName(RestConstants.NAMESPACE_REST_SDK, "invalid-paging-parameter");

    public static final QName PRECONDITION_FAILED = new QName(RestConstants.NAMESPACE_REST_SDK, "precondition-failed");

    public static final QName METHOD_NOT_ALLOWED = new QName(RestConstants.NAMESPACE_REST_SDK, "method-not-allowed");

    /* todo: other codes defined in apollo2rf:
        RestErrorCodes.CONCURRENT_MODIFICATION
        RestErrorCodes.EXECUTE_ERROR
        RestErrorCodes.HTTP_ERROR_GENERAL
        RestErrorCodes.HTTP_ERROR_UNAUTHORIZED
        RestErrorCodes.INTERNAL_SERVER_ERROR
        RestErrorCodes.MALFORMED_XML_ERROR
        RestErrorCodes.REPORT_ERROR
        RestErrorCodes.REST_INVALID_CONTENT_TYPE
        RestErrorCodes.REST_INVALID_OPERATION
        RestErrorCodes.SECURITY_USER_STORE_ERROR
        RestErrorCodes.SECURITY_VIOLATION
        RestErrorCodes.FORBIDDEN_OPERATION
        RestErrorCodes.FORBIDDEN_SET_OWNERSHIP
        RestErrorCodes.FORBIDDEN_SET_ACL
        RestErrorCodes.EMPTY_CONTENT
     */
}
