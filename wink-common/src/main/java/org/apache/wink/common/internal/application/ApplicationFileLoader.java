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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.registry.metadata.ResourceMetadataCollector;
import org.apache.wink.common.internal.utils.FileLoader;

/**
 * <p>
 * Loads application file. The format of the file is a text file, while each
 * line contains a class name.
 * <p>
 * The default constructor loads core application.
 */
public class ApplicationFileLoader {

    private static final Logger logger           =
                                                     LoggerFactory
                                                         .getLogger(ApplicationFileLoader.class);
    private static final String CORE_APPLICATION = "META-INF/core/wink-providers";
    private final Set<Class<?>> classes          = new LinkedHashSet<Class<?>>();

    /**
     * Loads core application file.
     * 
     * @throws FileNotFoundException if file is not found (should never happen)
     */
    public ApplicationFileLoader() throws FileNotFoundException {
        this(CORE_APPLICATION);
    }

    /**
     * loads specific applicationConfigFile
     * 
     * @param applicationConfigFile
     * @throws FileNotFoundException - if file is not found
     */
    public ApplicationFileLoader(String appConfigFile) throws FileNotFoundException {
        this(FileLoader.loadFileAsStream(appConfigFile));
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
                    cls = Class.forName(line);
                    if (ResourceMetadataCollector.isStaticResource(cls) || ProviderMetadataCollector
                        .isProvider(cls)) {
                        classes.add(cls);
                    } else {
                        logger.warn("The {} is neither resource nor provider. Ignoring.", cls);
                    }
                } catch (ClassNotFoundException e) {
                    logger.error("{} is not a class. Ignoring.", line);
                }
            }
        } catch (IOException e) {
            throw new WebApplicationException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                logger.info("Exception when closing file", e);
            }
        }
    }

    public Set<Class<?>> getClasses() {
        return classes;
    }

}
