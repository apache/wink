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
import org.apache.wink.common.SymphonyApplication;
import org.apache.wink.common.internal.utils.FileLoader;


/**
 * Provides a naive implementation for SymphonyApplication. Looks for names of
 * the classes in <tt>/WEB-INF/application</tt> file.
 */
public class SimpleSymphonyApplication extends SymphonyApplication {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSymphonyApplication.class);
    private static final String FILE_SEPARATOR = ";";
    private Set<Class<?>> jaxRSClasses;
    private String applicationConfigFiles;

    public SimpleSymphonyApplication(String applicationConfigFiles) {
        this.applicationConfigFiles = applicationConfigFiles;
    }

    @Override
    public Set<Class<?>> getClasses() {
        if (jaxRSClasses != null) {
            return jaxRSClasses;
        }
        jaxRSClasses = loadImplAppConfigFiles(applicationConfigFiles);
        return jaxRSClasses;
    }

    private Set<Class<?>> loadImplAppConfigFiles(String applicationConfigFiles) {
        Set<Class<?>> jaxRSClasses = new LinkedHashSet<Class<?>>();
        if (applicationConfigFiles != null) {
            String[] applicationConfigFilesArray = applicationConfigFiles.split(FILE_SEPARATOR);
            for (String applicationConfigFile : applicationConfigFilesArray) {
                applicationConfigFile = applicationConfigFile.trim();
                try {
                    jaxRSClasses.addAll(getApplicationFileLoader(getFileStream(applicationConfigFile)).getClasses());
                } catch (FileNotFoundException e) {
                    logger.warn("Could not find {}. Ignoring.", applicationConfigFile);
                }
            }
        }
        return jaxRSClasses;
    }

    protected ApplicationFileLoader getApplicationFileLoader(InputStream is) throws FileNotFoundException {
        return new ApplicationFileLoader(is);
    }

    protected InputStream getFileStream(String applicationConfigFile) throws FileNotFoundException {
        return FileLoader.loadFileAsStream(applicationConfigFile);
    }

}
