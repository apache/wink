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
package org.apache.wink.spring;

import java.util.Properties;

import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.test.mock.SpringAwareTestCase;

public class PropertiesOverrideTest extends SpringAwareTestCase {

    public void testPropertiesOverride() throws Exception {
        DeploymentConfiguration configuration = (DeploymentConfiguration) applicationContext.getBean("winkInternalDeploymentConfiguration");
        Properties properties = configuration.getProperties();

        // check custom
        assertEquals("atom", properties.getProperty("wink.searchPolicyContinuedSearch"));
        assertEquals("test me", properties.getProperty("test.property"));

        // check default
        assertEquals("true", properties.getProperty("wink.addAltParam"));
    }
}
