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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.registry.Injectable;

public class MethodMetadata extends AbstractMetadata {

    private ClassMetadata    parent;
    private Method           reflectionMethod;
    private String           httpMethod;
    private List<Injectable> formalParameters;

    public MethodMetadata(ClassMetadata parent) {
        this.parent = parent;
        this.formalParameters = new ArrayList<Injectable>();
        this.httpMethod = null;
        this.reflectionMethod = null;
    }

    public Method getReflectionMethod() {
        return reflectionMethod;
    }

    public void setReflectionMethod(Method reflectionMethod) {
        this.reflectionMethod = reflectionMethod;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<Injectable> getFormalParameters() {
        return formalParameters;
    }

    @Override
    public Set<MediaType> getConsumes() {
        Set<MediaType> set = super.getConsumes();
        if (set.size() == 0) {
            set = parent.getConsumes();
        }
        return set;
    }

    @Override
    public Set<MediaType> getProduces() {
        Set<MediaType> set = super.getProduces();
        if (set.size() == 0) {
            set = parent.getProduces();
        }
        return set;
    }

    @Override
    public String toString() {
        return "MethodMetadata [" + super.toString()
            + (formalParameters != null ? "formalParameters=" + formalParameters + ", " : "")
            + (httpMethod != null ? "httpMethod=" + httpMethod + ", " : "")
            + (parent != null ? "parent=" + parent + ", " : "")
            + (reflectionMethod != null ? "reflectionMethod=" + reflectionMethod : "")
            + "]";
    }

}
