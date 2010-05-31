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
package org.apache.wink.common.internal.application;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.apache.wink.common.internal.utils.ClassUtils;
import org.apache.wink.common.internal.utils.FileLoader;

/**
 * <p>
 * Loads application file. The format of the file is a text file, while each
 * line contains a class name.
 * <p>
 * The default constructor loads core application.
 */
public class ApplicationFileLoader {

    private static final String WINK_APPLICATION = "META-INF/wink-application";                  //$NON-NLS-1$
    private static final Logger logger           =
                                                     LoggerFactory
                                                         .getLogger(ApplicationFileLoader.class);
    private static final String CORE_APPLICATION = "META-INF/core/wink-providers";               //$NON-NLS-1$
    private final Set<Class<?>> classes          = new LinkedHashSet<Class<?>>();

    /**
     * Loads core application file.
     * 
     * @param loadWinkApplication - indicates if classes from
     *            "META-INF/wink-application" files should be loaded
     * @throws FileNotFoundException if file is not found (should never happen)
     */
    public ApplicationFileLoader(boolean loadWinkApplication) throws FileNotFoundException {
        this(CORE_APPLICATION);

        // load wink-application
        try {
            if (loadWinkApplication) {
                Enumeration<URL> applications =
                    FileLoader.loadFileUsingClassLoaders(WINK_APPLICATION);
                while (applications.hasMoreElements()) {
                    URL url = applications.nextElement();
                    if (logger.isInfoEnabled()) {
                        logger
                            .info(Messages.getMessage("loadingApplication", url.toExternalForm())); //$NON-NLS-1$
                    }
                    loadClasses(url.openStream());
                }
            }
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * loads specific applicationConfigFile
     * 
     * @param applicationConfigFile
     * @throws FileNotFoundException - if file is not found
     */
    public ApplicationFileLoader(String appConfigFile) throws FileNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug(Messages.getMessage("loadingApplication", appConfigFile)); //$NON-NLS-1$
        }
        loadClasses(FileLoader.loadFileAsStream(appConfigFile));
    }

    /**
     * loads specific applicationConfigFile
     * 
     * @param applicationConfigFile
     * @throws FileNotFoundException - if file is not found
     */
    public ApplicationFileLoader(InputStream appConfigFileStream) throws FileNotFoundException {
        loadClasses(appConfigFileStream);
    }

    final protected void loadClasses(InputStream is) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line;
            l: while ((line = bufferedReader.readLine()) != null) {
                // ignore leading and trailing whitespace
                line = line.trim();
                if (line.length() == 0) {
                    // ignore empty lines
                    continue l;
                }
                if (line.charAt(0) == '#') {
                    // ignore lines that start with #
                    continue l;
                }

                Class<?> cls = null;
                try {
                    // use ClassUtils.loadClass instead of Class.forName so we
                    // have classloader visibility into the Web module in J2EE
                    // environments
                    cls = ClassUtils.loadClass(line);
                    if (ResourceMetadataCollector.isStaticResource(cls) || ProviderMetadataCollector
                        .isProvider(cls)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(Messages.getMessage("loadingClassToApplication", line));
                        }
                        classes.add(cls);
                    } else {
                        if (logger.isWarnEnabled()) {
                            logger.warn(Messages.getMessage("classNotAResourceNorProvider", cls
                                .getName()));
                        }
                    }
                } catch (ClassNotFoundException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("ClassNotFoundException while loading class", e); //$NON-NLS-1$
                    }
                } catch (NoClassDefFoundError e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(Messages
                            .getMessage("classInstantiationExceptionWithMsgFormat", line), e); //$NON-NLS-1$
                    }
                }
            }
        } catch (IOException e) {
            throw new WebApplicationException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                if (logger.isInfoEnabled()) {
                    logger.info(Messages.getMessage("exceptionClosingFile"), e); //$NON-NLS-1$
                }
            }
        }
    }

    public Set<Class<?>> getClasses() {
        return classes;
    }

}
