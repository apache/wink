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

import javax.servlet.ServletInputStream;

import org.springframework.mock.web.MockHttpServletRequest;

public class MockHttpServletRequestWrapper extends MockHttpServletRequest {

    private ServletInputStream inputStream = null;

    @Override
    public ServletInputStream getInputStream() {
        if (inputStream != null) {
            return inputStream;
        }
        inputStream = super.getInputStream();
        return inputStream;
    }

    @Override
    public void setContentType(String contentType) {
        if (getCharacterEncoding() != null && !contentType.contains("charset=")) {
            contentType += ";charset=" + getCharacterEncoding();
        }
        addHeader("Content-Type", contentType);
    }

    @Override
    public void setContent(byte[] content) {
        super.setContent(content);
        if (content != null) {
            addHeader("Content-Length", String.valueOf(content.length));
        }
    }
}
