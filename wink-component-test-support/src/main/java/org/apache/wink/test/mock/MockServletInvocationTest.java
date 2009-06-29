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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * <p>
 * The unit tests should extend this class in order to simulate the servlet
 * invocation.
 * <p>
 * The method <tt>invoke</tt> invokes the servlet call.
 * <p>
 * In order to add configuration to the unit test override the following
 * methods: <tt>getJaxRSClasses</tt>, <tt>getUrletClasses</tt>,
 * <tt>getJaxRSSingletons</tt>, <tt>getUrlets</tt>, <tt>getPropertiesFile</tt> -
 * see javadoc on the methods for more details.
 * 
 * @param <PROCESSOR>
 * @param <REGISTRY>
 * @param <BUILDER>
 */
public abstract class MockServletInvocationTest extends TestCase {

    private Object              requestProcessor;
    private final Set<Class<?>> jaxRSClasses      = new LinkedHashSet<Class<?>>();

    @SuppressWarnings("unchecked")
    protected <T> T getRequestProcessor(Class<T> cls) {
        return (T) requestProcessor;
    }

    class MockApplication extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> set = new LinkedHashSet<Class<?>>();
            for (Class<?> cls : MockServletInvocationTest.this.getClasses()) {
                set.add(cls);
            }
            set.addAll(jaxRSClasses);
            return set;
        }

        @Override
        public Set<Object> getSingletons() {
            HashSet<Object> set = new LinkedHashSet<Object>();
            for (Object o : MockServletInvocationTest.this.getSingletons()) {
                set.add(o);
            }
            return set;
        }

    }

    protected Properties getProperties() throws IOException {
        String propertiesFile = getPropertiesFile();
        if (propertiesFile != null) {
            Properties properties = new Properties();
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(
                propertiesFile);
            if (resourceAsStream != null) {
                properties.load(resourceAsStream);
            }
            return properties;
        }
        return null;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

//        loadCoreApplicationFile();

        Class<?> configurationClass = Class.forName("org.apache.wink.server.internal.DeploymentConfiguration");
        Class<?> requestProcessorClass = Class.forName("org.apache.wink.server.internal.RequestProcessor");
        Constructor<?> requestProcessorConstructor = requestProcessorClass.getConstructor(configurationClass);
        Method setPropertiesMethod = configurationClass.getMethod("setProperties", Properties.class);
        Method initMethod = configurationClass.getMethod("init");
        Method addApplicationMethod = configurationClass.getMethod("addApplication",
            Application.class);

        Object configuration = createDeploymentConfiguration(configurationClass);
        Properties properties = getProperties();
        if (properties != null) {
            setPropertiesMethod.invoke(configuration, properties);
        }
        initMethod.invoke(configuration);
        addApplicationMethod.invoke(configuration, getApplication());

        requestProcessor = requestProcessorConstructor.newInstance(configuration);
    }

    protected Object createDeploymentConfiguration(Class<?> configurationClass) throws Exception {
        return configurationClass.newInstance();
    }

    /**
     * @return the name of the properties file. The file must be located on the
     *         classpath.
     */
    protected String getPropertiesFile() {
        return null;
    }

    /**
     * @return the classes of JAX-RS that will be returned by the
     *         Application.getClasses()
     */
    protected Class<?>[] getClasses() {
        return new Class<?>[0];
    }

    /**
     * @return the singletons of JAX-RS that will be returned by the
     *         Application.getSingletons
     */
    protected Object[] getSingletons() {
        return new Object[0];
    }

    protected Object[] getInstances() {
        return new Object[0];
    }

    protected Application getApplication() {
        return new MockApplication();
    }

//    private void loadCoreApplicationFile() throws IOException {
//
//        Enumeration<URL> resources = getClass().getClassLoader().getResources(
//            "META-INF/server/symphony.app");
//        while (resources.hasMoreElements()) {
//            InputStream is = resources.nextElement().openStream();
//            if (is != null) {
//                loadClasses(is);
//            } else {
//                throw new RuntimeException("File not found");
//            }
//        }
//    }

//    private void loadClasses(InputStream is) {
//        try {
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                Class<?> cls = null;
//                try {
//                    line = line.trim();
//                    if (line.length() == 0 || line.startsWith("#")) {
//                        continue;
//                    }
//                    cls = Class.forName(line);
//                    if (cls.getAnnotation(Path.class) != null
//                        || cls.getAnnotation(Provider.class) != null) {
//                        jaxRSClasses.add(cls);
//                    } else {
//                        symphonyInstances.add(cls.newInstance());
//                    }
//                } catch (ClassNotFoundException e) {
//                    logger.error(String.format("%s is not a class", line));
//                } catch (InstantiationException e) {
//                    logger.error(String.format("Failed to instantiate %s", String.valueOf(cls)));
//                } catch (IllegalAccessException e) {
//                    logger.error(String.format("Illegal access to %s", String.valueOf(cls)));
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                is.close();
//            } catch (IOException e) {
//                logger.info(String.format("Exception when closing file "), e);
//            }
//        }
//    }

    /**
     * Passes the test to the servlet instance simulating AS behaviour.
     * 
     * @param request
     *            the filled request
     * @return a new response as filled by the servlet
     * @throws IOException
     *             io error
     */
    public MockHttpServletResponse invoke(MockHttpServletRequest request) throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        try {
            Method handleRequestMethod = requestProcessor.getClass().getMethod("handleRequest",
                HttpServletRequest.class, HttpServletResponse.class);
            handleRequestMethod.invoke(requestProcessor, request, response);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            response.getOutputStream().flush();
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getResourceRegistry(Class<T> cls) {
        try {
            Method getConfigurationMethod = requestProcessor.getClass().getMethod(
                "getConfiguration");
            Object configuration = getConfigurationMethod.invoke(requestProcessor);
            Method getUrletRegistryMethod = configuration.getClass().getMethod(
                "getResourceRegistry");
            return (T) getUrletRegistryMethod.invoke(configuration);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
