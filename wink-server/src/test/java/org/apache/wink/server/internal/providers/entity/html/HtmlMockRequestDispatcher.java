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

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockRequestDispatcher;
import org.springframework.util.Assert;

/**
 * This dispatcher overrides the include method. This is needed because in HTML
 * the response is wrapped to OutputStreamHttpServletResponseWrapper and isn't a
 * type of MockHttpServletResponse.
 */
public class HtmlMockRequestDispatcher extends MockRequestDispatcher {
    private final Logger logger = LoggerFactory.getLogger(super.getClass());
    private final String url;

    /**
     * @param url
     */
    public HtmlMockRequestDispatcher(String url) {
        super(url);
        Assert.notNull(url, "URL must not be null");
        this.url = url;
    }

    /**
     * This method writes into the response the URL of the file to include.
     */
    @Override
    public void include(ServletRequest request, ServletResponse response) {
        Assert.notNull(request, "Request must not be null");
        Assert.notNull(response, "Response must not be null");
        if (!(response instanceof OutputStreamHttpServletResponseWrapper))
            throw new IllegalArgumentException(
                                               "HtmlMockRequestDispatcher requires OutputStreamHttpServletResponseWrapper");

        OutputStreamHttpServletResponseWrapper res =
            (OutputStreamHttpServletResponseWrapper)response;

        try {
            res.getWriter().write(url);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.isTrue(false);
        }

        logger.trace("MockRequestDispatcher: including URL [{}]", url);
    }
}
