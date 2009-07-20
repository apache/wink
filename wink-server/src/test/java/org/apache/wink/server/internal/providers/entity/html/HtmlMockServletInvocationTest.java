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

package org.apache.wink.server.internal.providers.entity.html;

import org.apache.wink.server.internal.providers.entity.html.HtmlRepresentationCollectionCustomizedTest.DefectsCustomizedResource;
import org.apache.wink.server.internal.providers.entity.html.HtmlRepresentationCollectionDefaultTest.DefectsDefaultResource;
import org.apache.wink.server.internal.providers.entity.html.HtmlRepresentationEntryCustomizedTest.DefectCustomizedResource;
import org.apache.wink.server.internal.providers.entity.html.HtmlRepresentationEntryDefaultTest.DefectDefaultResource;
import org.apache.wink.server.internal.providers.entity.html.HtmlRepresentationServiceDocumentTest.ExistHtmlResource;
import org.apache.wink.server.internal.providers.entity.html.HtmlRepresentationServiceDocumentTest.NoHtmlResource;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * This class defines the data that will be used to test the HTML representation
 * for collection and entry resources.
 */
public abstract class HtmlMockServletInvocationTest extends MockServletInvocationTest {

    protected static final String CUSTOMIZED_ENTRY_URL      =
                                                                "/HtmlCustRepresentation/customizedHtmlEntry.jsp";
    protected static final String CUSTOMIZED_COLLECTION_URL =
                                                                "/HtmlCustRepresentation/customizedHtmlCollection.jsp";
    protected static final String GET                       = "GET";

    protected Class<?>[] getClasses() {
        return new Class<?>[] {DefectsCustomizedResource.class, DefectsDefaultResource.class,
            DefectCustomizedResource.class, DefectDefaultResource.class, ExistHtmlResource.class,
            NoHtmlResource.class};
    }

    /**
     * This method is used to create HtmlMockHttpServletRequest.
     */
    protected MockHttpServletRequest constructMockRequest(String method,
                                                          String requestURI,
                                                          String acceptHeader) {
        HtmlMockHttpServletRequest mockRequest = new HtmlMockHttpServletRequest() {

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
}
