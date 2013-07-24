/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.wink.jcdi.server.test.util;


import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;

//based on org.apache.deltaspike.test.utils.ShrinkWrapArchiveUtil
public abstract class ShrinkWrapArchiveUtil {
    public static JavaArchive[] getArchives(String markerFile,
                                            String[] includeIfPackageExists,
                                            String[] excludeIfPackageExists,
                                            String archiveName) {
        ClassLoader classLoader = ShrinkWrapArchiveUtil.class.getClassLoader();

        try {
            Enumeration<URL> foundFiles = classLoader.getResources(markerFile);

            List<JavaArchive> archives = new ArrayList<JavaArchive>();

            while (foundFiles.hasMoreElements()) {
                URL foundFile = foundFiles.nextElement();

                JavaArchive archive
                        = createArchive(foundFile, markerFile, includeIfPackageExists, excludeIfPackageExists, archiveName);
                if (archive != null) {
                    archives.add(archive);
                }
            }

            return archives.toArray(new JavaArchive[archives.size()]);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static JavaArchive createArchive(URL foundFile, String markerFile,
                                             String[] includeIfPackageExists,
                                             String[] excludeIfPackageExists,
                                             String archiveName)
            throws IOException {
        String urlString = foundFile.toString();
        int idx = urlString.lastIndexOf(markerFile);
        urlString = urlString.substring(0, idx);

        String jarUrlPath = isJarUrl(urlString);
        if (jarUrlPath != null) {
            JavaArchive foundJar = ShrinkWrap.createFromZipFile(JavaArchive.class, new File(URI.create(jarUrlPath)));

            if (excludeIfPackageExists != null) {
                for (String excludePackage : excludeIfPackageExists) {
                    if (foundJar.contains(excludePackage.replaceAll("\\.", "\\/"))) {
                        return null;
                    }
                }
            }
            if (includeIfPackageExists != null) {
                for (String includePackage : includeIfPackageExists) {
                    if (foundJar.contains(includePackage.replaceAll("\\.", "\\/"))) {
                        return foundJar;
                    }
                }
            }
            return null; // couldn't find any jar
        } else {
            File f = new File((new URL(ensureCorrectUrlFormat(urlString))).getFile());
            if (!f.exists()) {
                // try a fallback if the URL contains %20 -> spaces
                if (urlString.contains("%20")) {
                    urlString = urlString.replaceAll("%20", " ");
                    f = new File((new URL(ensureCorrectUrlFormat(urlString))).getFile());
                }

            }

            return addFileArchive(f, includeIfPackageExists, excludeIfPackageExists, archiveName);
        }
    }

    private static JavaArchive addFileArchive(File archiveBasePath,
                                              String[] includeIfPackageExists,
                                              String[] excludeIfPackageExists,
                                              String archiveName)
            throws IOException {
        if (!archiveBasePath.exists()) {
            return null;
        }

        JavaArchive ret = null;

        if (archiveName == null) {
            archiveName = UUID.randomUUID().toString();
        } else {
            archiveName += "_" + UUID.randomUUID().toString();
        }
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar");

        if (includeIfPackageExists == null) {
            // no include rule, thus add it immediately
            ret = javaArchive;
        }

        int basePathLength = archiveBasePath.getAbsolutePath().length() + 1;

        for (File archiveEntry : collectArchiveEntries(archiveBasePath)) {
            String entryName = archiveEntry.getAbsolutePath().substring(basePathLength);

            // exclude rule
            if (excludeIfPackageExists(entryName, excludeIfPackageExists)) {
                continue;
            }

            // include rule
            if (ret == null && includeIfPackageExists(entryName, includeIfPackageExists)) {
                ret = javaArchive;
            }

            if (entryName.endsWith(".class")) {
                String className
                        = pathToClassName(entryName.substring(0, entryName.length() - (".class".length())));

                javaArchive.addClass(className);
            } else {
                javaArchive.addAsResource(archiveEntry, entryName.replace('\\', '/'));
            }
        }

        return ret;
    }

    private static List<File> collectArchiveEntries(File archiveBasePath) {
        if (archiveBasePath.isDirectory()) {
            List<File> archiveEntries = new ArrayList<File>();
            File[] files = archiveBasePath.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        archiveEntries.addAll(collectArchiveEntries(file));
                    } else {
                        archiveEntries.add(file);
                    }
                }
            }

            return archiveEntries;
        }

        return Collections.emptyList();
    }

    private static boolean excludeIfPackageExists(String jarEntryName, String[] excludeOnPackages) {
        if (excludeOnPackages != null) {
            String packageName = pathToClassName(jarEntryName);

            for (String excludeOnPackage : excludeOnPackages) {
                if (packageName.startsWith(excludeOnPackage)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean includeIfPackageExists(String jarEntryName, String[] includeOnPackages) {
        if (includeOnPackages == null) {
            return true;
        }

        String packageName = pathToClassName(jarEntryName);

        for (String includeOnPackage : includeOnPackages) {
            if (packageName.startsWith(includeOnPackage)) {
                return true;
            }
        }

        return false;
    }

    private static String isJarUrl(String urlPath) {
        final int jarColon = urlPath.indexOf(':');
        if (urlPath.endsWith("!/") && jarColon > 0) {
            urlPath = urlPath.substring(jarColon + 1, urlPath.length() - 2);
            return urlPath;
        }

        return null;
    }

    private static String ensureCorrectUrlFormat(String url) {
        //fix for wls
        if (!url.startsWith("file:/")) {
            url = "file:/" + url;
        }
        return url;
    }

    private static String pathToClassName(String pathName) {
        return pathName.replace('/', '.').replace('\\', '.');   // replace unix and windows separators
    }
}
