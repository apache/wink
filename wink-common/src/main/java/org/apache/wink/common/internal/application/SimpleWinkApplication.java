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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.WinkApplication;
import org.apache.wink.common.internal.utils.FileLoader;

/**
 * <p>
 * Loads one or more flat application files using the
 * <tt>ApplicationFileLoader</tt>.
 * <p>
 * The implementation is lazy, meaning the files will be loaded on the first use
 * and only once.
 * <p>
 * This implementation search for files using the <tt>FileLoader</tt>. See
 * <tt>ServletWinkApplication</tt> that loads files using
 * <tt>ServletFileLoader</tt>.
 * 
 * @see org.apache.wink.common.internal.utils.FileLoader
 * @see org.apache.wink.common.internal.application.ApplicationFileLoader
 * @see org.apache.wink.server.internal.application.ServletWinkApplication
 */
public class SimpleWinkApplication extends WinkApplication {

    private static final Logger logger         =
                                                   LoggerFactory
                                                       .getLogger(SimpleWinkApplication.class);
    private static final String FILE_SEPARATOR = ";";
    private final String        applicationConfigFiles;
    private Set<Class<?>>       jaxRSClasses;

    public SimpleWinkApplication(String applicationConfigFiles) {
        this.applicationConfigFiles = applicationConfigFiles;
    }

    @Override
    public Set<Class<?>> getClasses() {
        if (jaxRSClasses != null) {
            return jaxRSClasses;
        }
        jaxRSClasses = loadImplAppConfigFiles();
        return jaxRSClasses;
    }

    private Set<Class<?>> loadImplAppConfigFiles() {
        Set<Class<?>> jaxRSClasses = new LinkedHashSet<Class<?>>();
        if (applicationConfigFiles != null) {
            String[] applicationConfigFilesArray = applicationConfigFiles.split(FILE_SEPARATOR);
            for (String applicationConfigFile : applicationConfigFilesArray) {
                applicationConfigFile = applicationConfigFile.trim();
                try {
                    jaxRSClasses
                        .addAll(getApplicationFileLoader(getFileStream(applicationConfigFile))
                            .getClasses());
                } catch (FileNotFoundException e) {
                    logger.warn("Could not find {}. Ignoring.", applicationConfigFile);
                }
            }
        }
        return jaxRSClasses;
    }

    protected ApplicationFileLoader getApplicationFileLoader(InputStream is)
        throws FileNotFoundException {
        return new ApplicationFileLoader(is);
    }

    protected InputStream getFileStream(String applicationConfigFile) throws FileNotFoundException {
        return FileLoader.loadFileAsStream(applicationConfigFile);
    }

}
