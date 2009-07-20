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

package org.apache.wink.common;

/**
 * Provides a basic implementation of the
 * org.apache.wink.common.DynamicResource.
 */
public abstract class AbstractDynamicResource implements DynamicResource {

    private String[] dispatchedPath;
    private Object[] parents;
    private String   workspaceTitle;
    private String   collectionTitle;
    private String   beanName;

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setDispatchedPath(String[] dispatchedPath) {
        this.dispatchedPath = dispatchedPath;
    }

    public String[] getDispatchedPath() {
        return dispatchedPath;
    }

    public void setParents(Object[] parents) {
        this.parents = parents;
    }

    public Object[] getParents() {
        return parents;
    }

    public void setWorkspaceTitle(String workspaceTitle) {
        this.workspaceTitle = workspaceTitle;
    }

    public String getWorkspaceTitle() {
        return workspaceTitle;
    }

    public void setCollectionTitle(String collectionTitle) {
        this.collectionTitle = collectionTitle;
    }

    public String getCollectionTitle() {
        return collectionTitle;
    }

}
