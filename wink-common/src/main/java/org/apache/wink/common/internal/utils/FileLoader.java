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

package org.apache.wink.common.internal.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

import org.apache.wink.common.internal.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileLoader {

    protected FileLoader() {
    }

    private static final Logger logger = LoggerFactory.getLogger(FileLoader.class);

    /**
     * search for file using classloaders only and returns URL to the file if
     * found
     * 
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    public static URL loadFile(String fileName) throws FileNotFoundException {
        Enumeration<URL> resources;
        try {
            resources = loadFileUsingClassLoaders(fileName);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new FileNotFoundException(fileName);
        }
        if (!resources.hasMoreElements()) {
            throw new FileNotFoundException(fileName);
        }
        return resources.nextElement();
    }

    /**
     * <p>
     * loads file
     * <p>
     * first searches for file in file system
     * <p>
     * if not found, searchs for file using classloaders
     * 
     * @param fileName
     * @return
     * @throws FileNotFoundException
     * @throws URISyntaxException
     */
    public static InputStream loadFileAsStream(String fileName) throws FileNotFoundException {

        if (fileName == null || fileName.trim().equals("")) { //$NON-NLS-1$
            throw new NullPointerException(Messages.getMessage("variableIsNull", "fileName")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        logger.trace("Searching for {} in file system.", fileName); //$NON-NLS-1$

        File file = new File(fileName);
        if (file.isFile()) {
            // since file is a normal file, return it
            logger.trace("File {} found in file system.", fileName); //$NON-NLS-1$
            return new FileInputStream(file);
        }

        // file is not a normal file, try to find it using classloaders
        Enumeration<URL> resources;
        try {
            resources = loadFileUsingClassLoaders(fileName);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new FileNotFoundException(fileName);
        }
        if (!resources.hasMoreElements()) {
            throw new FileNotFoundException(fileName);
        }
        URL url = resources.nextElement();
        try {
            return url.openStream();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new FileNotFoundException(fileName);
        }
    }

    /**
     * find file using class loaders
     * 
     * @param filename
     * @return
     * @throws IOException
     */
    public static Enumeration<URL> loadFileUsingClassLoaders(String filename) throws IOException {

        /*
         * TODO: perhaps desirable to move this code to
         * org.apache.wink.common.internal.utils.ClassUtils?
         */

        logger.trace("Searching for {} using thread context classloader.", filename); //$NON-NLS-1$
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = loadFileUsingClassLoader(classLoader, filename);
        if (resources.hasMoreElements()) {
            return resources;
        }

        logger.trace("Searching for {} using current classloader.", filename); //$NON-NLS-1$
        classLoader = FileLoader.class.getClassLoader();
        resources = loadFileUsingClassLoader(classLoader, filename);
        if (resources.hasMoreElements()) {
            return resources;
        }

        logger.trace("Searching for {} using system classloader.", filename); //$NON-NLS-1$
        return ClassLoader.getSystemResources(filename);
    }

    private static Enumeration<URL> loadFileUsingClassLoader(ClassLoader classLoader,
                                                             String filename) throws IOException {
        Enumeration<URL> resources = null;
        if (classLoader != null) {
            resources = classLoader.getResources(filename);
        }
        return resources;
    }

}
