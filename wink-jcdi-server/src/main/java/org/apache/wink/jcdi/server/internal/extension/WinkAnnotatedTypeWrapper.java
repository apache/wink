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
package org.apache.wink.jcdi.server.internal.extension;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class WinkAnnotatedTypeWrapper implements AnnotatedType {
    private final AnnotatedType wrapped;

    private Map<Class<? extends Annotation>, Annotation> annotations;
    private Set<Annotation> annotationSet;

    WinkAnnotatedTypeWrapper(AnnotatedType<?> wrapped, Annotation scopeAnnotation) {
        this.wrapped = wrapped;

        Set<Annotation> originalAnnotationSet = wrapped.getAnnotations();
        this.annotations = new HashMap<Class<? extends Annotation>, Annotation>(originalAnnotationSet.size() + 1);

        for (Annotation originalAnnotation : originalAnnotationSet) {
            this.annotations.put(originalAnnotation.annotationType(), originalAnnotation);
        }
        this.annotations.put(scopeAnnotation.annotationType(), scopeAnnotation);

        this.annotationSet = new HashSet<Annotation>(this.annotations.size());
        this.annotationSet.addAll(this.annotations.values());
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return this.annotations.containsKey(annotationType);
    }

    public Set<Annotation> getAnnotations() {
        return this.annotationSet;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return (T) this.annotations.get(annotationType);
    }

    /*
     * generated
     */

    public Class getJavaClass() {
        return wrapped.getJavaClass();
    }

    public Set<AnnotatedConstructor> getConstructors() {
        return wrapped.getConstructors();
    }

    public Set<AnnotatedMethod> getMethods() {
        return wrapped.getMethods();
    }

    public Set<AnnotatedField> getFields() {
        return wrapped.getFields();
    }

    public Type getBaseType() {
        return wrapped.getBaseType();
    }

    public Set<Type> getTypeClosure() {
        return wrapped.getTypeClosure();
    }
}
