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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.apache.wink.common.internal.registry.Injectable;

/**
 * Represents metadata of one Resource class.
 */
public class ClassMetadata extends AbstractMetadata {

    private final Class<?>       resourceClass;
    private String               workspaceName;
    private String               collectionTitle;
    private List<Class<?>>       parents;
    private List<Object>         parentInstances;
    private ConstructorMetadata  constructor;

    // a list of methods that handle requests directly
    private List<MethodMetadata> resourceMethods;
    // a list of methods that are sub-resource locators
    private List<MethodMetadata> subResourceLocators;
    // a list of methods that are sub-resource methods
    private List<MethodMetadata> subResourceMethods;
    // a map of fields that are to be injected with injectable data during a
    // request
    private List<Injectable>     injectableFields;
    // bean info of this class
    private final BeanInfo       beanInfo;

    ClassMetadata(Class<?> resourceClass) {
        this.resourceClass = resourceClass;
        this.constructor = null;
        this.parents = new ArrayList<Class<?>>();
        this.resourceMethods = new ArrayList<MethodMetadata>();
        this.subResourceLocators = new ArrayList<MethodMetadata>();
        this.subResourceMethods = new ArrayList<MethodMetadata>();
        this.parentInstances = new ArrayList<Object>();
        this.injectableFields = new ArrayList<Injectable>();
        try {
            beanInfo = Introspector.getBeanInfo(resourceClass);
        } catch (IntrospectionException e) {
            // should never happen
            throw new WebApplicationException(e);
        }
    }

    public Class<?> getResourceClass() {
        return resourceClass;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getCollectionTitle() {
        return collectionTitle;
    }

    public void setCollectionTitle(String collectionTitle) {
        this.collectionTitle = collectionTitle;
    }

    public Class<?> getParent() {
        if (getParents().size() == 0) {
            return null;
        }
        return getParents().get(0);
    }

    public Object getParentInstance() {
        if (getParentInstances().size() == 0) {
            return null;
        }
        return getParentInstances().get(0);
    }

    public List<Class<?>> getParents() {
        return parents;
    }

    public List<Object> getParentInstances() {
        return parentInstances;
    }

    public ConstructorMetadata getConstructor() {
        return constructor;
    }

    public void setConstructor(ConstructorMetadata constructor) {
        this.constructor = constructor;
    }

    public List<MethodMetadata> getResourceMethods() {
        return resourceMethods;
    }

    public List<MethodMetadata> getSubResourceLocators() {
        return subResourceLocators;
    }

    public List<MethodMetadata> getSubResourceMethods() {
        return subResourceMethods;
    }

    public void setParentInstances(List<Object> parentResourceInstances) {
        this.parentInstances = parentResourceInstances;
    }

    public List<Injectable> getInjectableFields() {
        return injectableFields;
    }

    public BeanInfo getBeanInfo() {
        return beanInfo;
    }

    @Override
    public String toString() {
        return String.format("Class: %s", String.valueOf(resourceClass)); //$NON-NLS-1$
    }

}
