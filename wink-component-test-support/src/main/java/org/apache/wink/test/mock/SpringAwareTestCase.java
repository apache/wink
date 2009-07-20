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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ContextLoader;

/**
 * Loads Spring Context for the unit tests
 */
public abstract class SpringAwareTestCase extends TestCase {

    protected ApplicationContext applicationContext;
    protected MockServletContext servletContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ContextLoader contextLoader = new ContextLoader();
        servletContext = new MockServletContext();
        servletContext.addInitParameter(ContextLoader.CONFIG_LOCATION_PARAM,
                                        getApplicationContextPath());
        applicationContext = contextLoader.initWebApplicationContext(servletContext);
    }

    /**
     * @return additional context, override to add more context files
     */
    protected List<String> getAdditionalContextNames() {
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("META-INF/server/wink-core-context.xml");
        return arrayList;
    }

    /**
     * @return application context path, override to change the application
     *         context path construction
     */
    private String getApplicationContextPath() {
        ArrayList<String> contextList = new ArrayList<String>();

        List<String> additionalContextNames = getAdditionalContextNames();
        if (!additionalContextNames.isEmpty()) {
            contextList.addAll(additionalContextNames);
        }

        try {
            String classPathName =
                getPackagePath() + File.separator + getClass().getSimpleName() + "Context.xml";

            URL resource = getClass().getClassLoader().getResource(classPathName);
            if (resource != null) {
                File file = new File(resource.toURI());
                if (file.isFile()) {
                    contextList.add(classPathName);
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        StringBuilder buf = new StringBuilder(contextList.get(0));
        for (int i = 1; i < contextList.size(); ++i) {
            buf.append(',');
            buf.append(contextList.get(i));
        }
        return buf.toString();

    }

    protected String getPackagePath() {
        return TestUtils.packageToPath(getClass().getPackage().getName());
    }

}
