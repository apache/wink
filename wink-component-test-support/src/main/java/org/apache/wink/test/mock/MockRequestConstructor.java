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

package org.apache.wink.test.mock;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Constructs mock request for server testing
 */
public final class MockRequestConstructor {

    private MockRequestConstructor() {
    }

    /**
     * Construct a mock request to be used in tests.
     * 
     * @param method HTTP method
     * @param requestURI request URI
     * @param mediaType requested media type
     * @return new mock request
     */
    public static MockHttpServletRequest constructMockRequest(String method,
                                                              String requestURI,
                                                              MediaType mediaType) {
        return constructMockRequest(method, requestURI, mediaType.toString());
    }

    /**
     * Construct a mock request to be used in tests.
     * 
     * @param method HTTP method
     * @param requestURI request URI
     * @param acceptHeader request Accept header
     * @return new mock request
     */
    public static MockHttpServletRequest constructMockRequest(String method,
                                                              String requestURI,
                                                              String acceptHeader) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequestWrapper() {

            public String getPathTranslated() {
                return null; // prevent Spring to resolve the file on the
                             // filesystem which fails
            }

        };
        mockRequest.setMethod(method);
        mockRequest.setRequestURI(requestURI);
        mockRequest.addHeader("Accept", acceptHeader);
        return mockRequest;
    }

    /**
     * Construct a mock request to be used in tests.
     * 
     * @param method HTTP method
     * @param requestURI request URI
     * @param acceptHeader request Accept header
     * @param contentType request Content Type
     * @param content request content
     * @return new mock request
     */
    public static MockHttpServletRequest constructMockRequest(String method,
                                                              String requestURI,
                                                              String acceptHeader,
                                                              String contentType,
                                                              byte[] content) {
        MockHttpServletRequest mockRequest = constructMockRequest(method, requestURI, acceptHeader);
        mockRequest.setContentType(contentType);
        mockRequest.setContent(content);

        return mockRequest;
    }

    /**
     * Construct a mock request to be used in tests.
     * 
     * @param method HTTP method
     * @param requestURI request URI
     * @param acceptHeader request Accept header
     * @param parameters request query parameters
     * @param attributes request attributes
     * @return new mock request
     */
    public static MockHttpServletRequest constructMockRequest(String method,
                                                              String requestURI,
                                                              String acceptHeader,
                                                              Map<?, ?> parameters,
                                                              Map<String, Object> attributes) {
        MockHttpServletRequest mockRequest = constructMockRequest(method, requestURI, acceptHeader);
        if (attributes != null) {
            Set<String> attributeNames = attributes.keySet();
            Object attributeValue;
            for (String attributeName : attributeNames) {
                attributeValue = attributes.get(attributeName);
                mockRequest.setAttribute(attributeName, attributeValue);
            }
        }
        mockRequest.setParameters(parameters);

        return mockRequest;
    }

}
