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

package org.apache.wink.common.internal.registry;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.wink.common.internal.runtime.RuntimeContext;
import org.apache.wink.common.internal.utils.GenericsUtils;

/**
 * Represents a class that can be injected into a Resource field or method
 * parameter during the invocation of request
 */
public abstract class Injectable {

    public enum ParamType {
        ENTITY, CONTEXT, PATH, QUERY, MATRIX, FORM, HEADER, COOKIE;
    }

    private final ParamType paramType;

    /**
     * the type of class to be injected
     */
    private final Class<?>  type;

    /**
     * the generic type of the class to be injected
     */
    private final Type      genericType;

    /**
     * Member that will be injected (field, constructor or method)
     */
    private final Member    member;

    /**
     * annotations that are present on the parameter
     */
    private Annotation[]    annotations;

    protected Injectable(ParamType paramType,
                         Class<?> type,
                         Type genericType,
                         Annotation[] annotations,
                         Member member) {
        this.paramType = paramType;
        this.type = type;
        this.genericType = genericType;
        this.annotations = annotations;
        this.member = member;
    }

    public Class<?> getType() {
        return type;
    }

    public ParamType getParamType() {
        return paramType;
    }

    protected boolean isTypeOf(Class<?> typeOf) {
        return type.equals(typeOf);
    }

    protected boolean isTypeCollectionOf(Class<?> collectionType) {
        return (type.equals(List.class) || type.equals(Set.class) || type.equals(SortedSet.class)) && collectionType
            .equals(GenericsUtils.getGenericParamType(genericType));
    }

    protected <T> Collection<T> elementAsTypeCollection(T object, Comparator<T> comparator) {
        return asTypeCollection(Collections.nCopies(1, object), comparator);
    }

    protected <T> Collection<T> asTypeCollection(Collection<T> collection, Comparator<T> comparator) {
        if (type.equals(List.class)) {
            return new ArrayList<T>(collection);
        }
        if (type.equals(Set.class)) {
            return new LinkedHashSet<T>(collection);
        }
        if (type.equals(SortedSet.class)) {
            TreeSet<T> treeSet = new TreeSet<T>(comparator);
            treeSet.addAll(collection);
            return treeSet;
        }
        return null;
    }

    public Type getGenericType() {
        return genericType;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public Member getMember() {
        return member;
    }

    /**
     * Abstract method to be implemented by all types of InjectableData, and
     * which is called during the instantiation of the injectable data
     * 
     * @param runtimeContext
     * @return
     */
    public abstract Object getValue(RuntimeContext runtimeContext) throws IOException;

}
