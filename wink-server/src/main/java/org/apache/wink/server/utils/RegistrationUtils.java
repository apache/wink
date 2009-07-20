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
package org.apache.wink.server.utils;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;

import org.apache.wink.common.WinkApplication;
import org.apache.wink.server.internal.RequestProcessor;

public class RegistrationUtils {

    /**
     * Registers resources and providers provided by the Application. The
     * methods adds new providers and resources and DOES NOT removes the already
     * registered.
     * 
     * @param application - application to register
     * @param servletContext - current servlet context
     */
    public static void registerApplication(Application application, ServletContext servletContext) {
        registerApplication(application, servletContext, null);
    }

    public static void registerClasses(ServletContext servletContext, Class<?>... classes) {
        registerClasses(servletContext, null, classes);
    }

    public static void registerInstances(ServletContext servletContext, Object... instances) {
        registerInstances(servletContext, null, instances);
    }

    public static void registerInstances(ServletContext servletContext,
                                         String requestProcessorAttribute,
                                         Object... instances) {
        registerApplication(new InnerApplication(instances),
                            servletContext,
                            requestProcessorAttribute);
    }

    public static void registerClasses(ServletContext servletContext,
                                       String requestProcessorAttribute,
                                       Class<?>... classes) {
        registerApplication(new InnerApplication(classes),
                            servletContext,
                            requestProcessorAttribute);
    }

    /**
     * Registers resources and providers provided by the Application. The
     * methods adds new providers and resources and DOES NOT removes the already
     * registered.
     * 
     * @param application - application to register
     * @param servletContext - current servlet context
     * @param requestProcessorAttribute - attribute on which the request
     *            processor is stored. It's useful, when there are multiple rest
     *            servlets in the system and each one has a request processor.
     */
    public static void registerApplication(Application application,
                                           ServletContext servletContext,
                                           String requestProcessorAttribute) {
        RequestProcessor requestProcessor =
            RequestProcessor.getRequestProcessor(servletContext, requestProcessorAttribute);
        requestProcessor.getConfiguration().addApplication(application);
    }

    public static class InnerApplication extends WinkApplication {

        private Set<Class<?>> classes   = null;
        private Set<Object>   instances = null;
        private double        priority  = DEFAULT_PRIORITY;

        public InnerApplication(Set<Class<?>> set) {
            classes = set;
        }

        public InnerApplication(Class<?>... classes) {
            this.classes = new HashSet<Class<?>>(classes.length);
            for (Class<?> cls : classes) {
                this.classes.add(cls);
            }
        }

        public InnerApplication(Object... instances) {
            this.instances = new HashSet<Object>(instances.length);
            for (Object obj : instances) {
                this.instances.add(obj);
            }
        }

        @Override
        public Set<Class<?>> getClasses() {
            if (classes == null) {
                return super.getClasses();
            }
            return classes;
        }

        @Override
        public Set<Object> getInstances() {
            if (instances == null) {
                return super.getInstances();
            }
            return instances;
        }

        public void setPriority(double priority) {
            this.priority = priority;
        }

        public double getPriority() {
            return priority;
        }

    }
}
