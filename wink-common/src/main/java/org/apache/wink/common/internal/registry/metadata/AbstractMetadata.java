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

package org.apache.wink.common.internal.registry.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMetadata {

    private static final Logger logger            = LoggerFactory.getLogger(AbstractMetadata.class);

    private List<String>   paths;
    private Set<MediaType> consumes;
    private Set<MediaType> produces;
    private boolean        encoded;
    private String         defaultValue;

    public AbstractMetadata() {
        paths = new ArrayList<String>();
        consumes = new LinkedHashSet<MediaType>();
        produces = new LinkedHashSet<MediaType>();
        encoded = false;
        defaultValue = null;
    }

    public String getPath() {
        logger.trace("getPath() entry");
        if (paths.size() == 0) {
            logger.trace("getPath() exit returning null");
            return null;
        }
        String p = paths.get(0);
        logger.trace("getPath() exit returning {}", p);
        return p;
    }

    public void addPath(String path) {
        logger.trace("addPath({}) entry", path);
        paths.add(path);
        logger.trace("addPath() exit");
    }

    public void addPaths(Collection<String> paths) {
        logger.trace("addPath({}) entry", paths);
        this.paths.addAll(paths);
        logger.trace("addPath() exit", paths);
    }

    public void addConsumes(MediaType mt) {
        logger.trace("addConsumes({}) entry", mt);
        consumes.add(mt);
        logger.trace("addConsumes() exit");
    }

    public void addProduces(MediaType mt) {
        logger.trace("addProduces({}) entry", mt);
        produces.add(mt);
        logger.trace("addProduces() exit");
    }

    public List<String> getPaths() {
        return Collections.unmodifiableList(paths);
    }

    public Set<MediaType> getConsumes() {
        return Collections.unmodifiableSet(consumes);
    }

    public Set<MediaType> getProduces() {
        return Collections.unmodifiableSet(produces);
    }

    @Override
    public String toString() {
        return "[" + (consumes != null ? "consumes=" + consumes + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            + (paths != null ? "paths=" + paths + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + (produces != null ? "produces=" + produces : "") //$NON-NLS-1$ //$NON-NLS-2$
            + "]"; //$NON-NLS-1$
    }

    public void setEncoded(boolean encoded) {
        logger.trace("setEncoded({}) entry", encoded);
        this.encoded = encoded;
        logger.trace("setEncoded({}) exit", encoded);
    }

    public boolean isEncoded() {
        return encoded;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        logger.trace("setDefaultValue({}) entry", defaultValue);
        this.defaultValue = defaultValue;
        logger.trace("setDefaultValue() exit");        
    }
}
