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

package org.apache.wink.server.internal.application;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.wink.common.internal.application.ApplicationFileLoader;
import org.apache.wink.common.internal.application.SimpleWinkApplication;
import org.apache.wink.server.internal.utils.ServletFileLoader;

public class ServletWinkApplication extends SimpleWinkApplication {

    private final ServletContext servletContext;

    public ServletWinkApplication(ServletContext servletContext, String applicationConfigFiles) {
        super(applicationConfigFiles);
        this.servletContext = servletContext;
    }

    @Override
    protected ApplicationFileLoader getApplicationFileLoader(InputStream is)
        throws FileNotFoundException {
        return new ServletApplicationFileLoader(is);
    }

    @Override
    protected InputStream getFileStream(String applicationConfigFile) throws FileNotFoundException {
        return ServletFileLoader.loadFileAsStream(servletContext, applicationConfigFile);
    }

}
