/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.wink.guice.server.internal.servlet;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.utils.ClassUtils;
import org.apache.wink.guice.server.internal.GuiceDeploymentConfiguration;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.servlet.RestServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiceRestServlet extends RestServlet {

    private static final String DEPLYMENT_CONF_PARAM = "deploymentConfiguration";

    private static final Logger logger               =
                                                         LoggerFactory
                                                             .getLogger(GuiceRestServlet.class);

    private static final long   serialVersionUID     = -1920970727031271538L;

    @Override
    protected DeploymentConfiguration createDeploymentConfiguration()
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String initParameter = getInitParameter(DEPLYMENT_CONF_PARAM);
        if (initParameter != null) {
            if (logger.isInfoEnabled()) {
                logger.info(Messages.getMessage("restServletUseDeploymentConfigurationParam",
                                                initParameter,
                                                DEPLYMENT_CONF_PARAM));
            }
            // use ClassUtils.loadClass instead of Class.forName so we have
            // classloader visibility into the Web module in J2EE environments
            Class<?> confClass = ClassUtils.loadClass(initParameter);
            return (DeploymentConfiguration)confClass.newInstance();
        }
        return new GuiceDeploymentConfiguration();
    }

}
