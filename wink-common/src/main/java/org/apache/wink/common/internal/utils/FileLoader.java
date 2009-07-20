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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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
        return loadFileUsingClassLoaders(fileName);
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

        if (fileName == null || fileName.trim().equals("")) {
            throw new NullPointerException("fileName");
        }

        logger.debug("Searching for {} in file system.", fileName);

        File file = new File(fileName);
        if (file.isFile()) {
            // since file is a normal file, return it
            logger.debug("File {} found in file system.", fileName);
            return new FileInputStream(file);
        }

        // file is not a normal file, try to find it using classloaders
        URL url = loadFileUsingClassLoaders(fileName);
        try {
            // decode any escaped sequences such as <space> which is %20 in URL
            URI uri = url.toURI();
            String path = uri.getSchemeSpecificPart();
            url = new URL(url.getProtocol(), null, path);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new FileNotFoundException(fileName);
        } catch (URISyntaxException e) {
            // do nothing, but return the real (!) url
        }

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
     * @throws FileNotFoundException
     */
    public static URL loadFileUsingClassLoaders(String filename) throws FileNotFoundException {
        logger.debug("Searching for {} using thread context classloader.", filename);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = loadFileUsingClassLoader(classLoader, filename);
        if (url != null) {
            return url;
        }

        logger.debug("Searching for {} using current classloader.", filename);
        classLoader = FileLoader.class.getClassLoader();
        url = loadFileUsingClassLoader(classLoader, filename);
        if (url != null) {
            return url;
        }

        logger.debug("Searching for {} using system classloader.", filename);
        url = ClassLoader.getSystemResource(filename);
        if (url == null) {
            // well, the last attempt has failed! throw FileNotFoundException
            logger.error("Failed to find file using classloaders");
            throw new FileNotFoundException(filename);
        }

        return url;
    }

    private static URL loadFileUsingClassLoader(ClassLoader classLoader, String filename) {
        URL url = null;
        if (classLoader != null) {
            url = classLoader.getResource(filename);
        }
        return url;
    }

}
