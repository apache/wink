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

public abstract class AbstractMetadata {

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
        if (paths.size() == 0) {
            return null;
        }
        return paths.get(0);
    }

    public void addPath(String path) {
        paths.add(path);
    }

    public void addPaths(Collection<String> paths) {
        this.paths.addAll(paths);
    }

    public void addConsumes(MediaType mt) {
        consumes.add(mt);
    }

    public void addProduces(MediaType mt) {
        produces.add(mt);
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
        return "[" + (consumes != null ? "consumes=" + consumes + ", " : "")
            + (paths != null ? "paths=" + paths + ", " : "")
            + (produces != null ? "produces=" + produces : "") + "]";
    }

    public void setEncoded(boolean encoded) {
        this.encoded = encoded;
    }

    public boolean isEncoded() {
        return encoded;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
