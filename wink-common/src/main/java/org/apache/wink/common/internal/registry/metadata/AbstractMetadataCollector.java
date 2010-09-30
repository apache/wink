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

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Encoded;

import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.InjectableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects common class meta data of JAX-RS Resources and Providers
 */
public abstract class AbstractMetadataCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractMetadataCollector.class);
    
    private final ClassMetadata metadata;

    public AbstractMetadataCollector(Class<?> clazz) {
        metadata = new ClassMetadata(clazz);
    }

    /**
     * Parses fields.
     * <p>
     * <b>Warning! Don't call this method from constructor, since it invoked an
     * abstract method parseField()!</b> It's possible to call this method from
     * constructor only, when you override parseField() and set it to be final.
     */
    protected final void parseFields() {
        Class<?> resourceClass = metadata.getResourceClass();

        List<Injectable> injectableFields = metadata.getInjectableFields();
        
        // add fields
        while (resourceClass != Object.class && resourceClass != null) {
            for (Field field : resourceClass.getDeclaredFields()) {
                Type fieldType = field.getGenericType();
                Injectable injectable = parseAccessibleObject(field, fieldType);
                logger.trace("Field is {} and injectable is {}", fieldType, injectable);

                if (injectable != null) {
                    injectableFields.add(injectable);
                }
            }
            resourceClass = resourceClass.getSuperclass();
        }
        logger.trace("Injectable fields: {}", injectableFields);

        // add properties
        PropertyDescriptor[] propertyDescriptors = metadata.getBeanInfo().getPropertyDescriptors();
        if(logger.isTraceEnabled()) {
            logger.trace("Property descriptors are: {}", Arrays.asList(propertyDescriptors));
        }
        if (propertyDescriptors != null) {
            l: for (PropertyDescriptor pd : propertyDescriptors) {
                Method writeMethod = pd.getWriteMethod();
                if (writeMethod == null) {
                    // the property cannot be written, ignore it.
                    continue l;
                }
                if(logger.isTraceEnabled()) {
                    logger.trace("Method under inspection: {}", writeMethod.getName());
                }
                Type genericReturnType = writeMethod.getParameterTypes()[0];
                Injectable injectable = parseAccessibleObject(writeMethod, genericReturnType);
                if (injectable != null) {
                    injectableFields.add(injectable);
                }
                if(logger.isTraceEnabled()) {
                    logger.trace("Injectable under inspection: {}", injectable);
                }
            }
        }
    }

    protected abstract Injectable parseAccessibleObject(AccessibleObject field, Type fieldType);

    protected abstract boolean isConstructorParameterValid(Injectable fp);

    /**
     * Parses constructors.
     * <p>
     * <b>Warning! Don't call this method from constructor, since it invoked an
     * abstract method isConstructorParameterValid()!</b> It's possible to call
     * this method from constructor only, when you override
     * isConstructorParameterValid() and set it to be final.
     */
    protected final void parseConstructors() {
        ConstructorMetadata constructorMetadata = new ConstructorMetadata();
        List<Injectable> formalParameters = new ArrayList<Injectable>();
        Class<?> resourceClass = metadata.getResourceClass();

        L1: for (Constructor<?> constructor : resourceClass.getDeclaredConstructors()) {
            int modifiers = constructor.getModifiers();
            if (!Modifier.isPublic(modifiers)) {
                continue;
            }

            constructorMetadata.setEncoded(constructor.getAnnotation(Encoded.class) != null);

            // gather all formal parameters
            formalParameters.clear();

            // TODO: Notice that due to the bug
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6332964 in
            // Java 1.5, this method will throw
            // and exception for inner non-static classes
            // need to catch an exception and print warning message
            // In addition need to check that it works in Java 1.6
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            Type[] paramTypes = constructor.getGenericParameterTypes();
            // boolean isValidConstructor = true;
            // gather all formal parameters as list of injectable data
            for (int pos = 0, limit = paramTypes.length; pos < limit; pos++) {
                Injectable fp =
                    InjectableFactory.getInstance()
                        .create(paramTypes[pos],
                                parameterAnnotations[pos],
                                constructor,
                                getMetadata().isEncoded() || constructorMetadata.isEncoded(),
                                null);

                if (!isConstructorParameterValid(fp)) {
                    continue L1;
                }
                formalParameters.add(fp);
            }

            // if this is the first valid constructor found or
            // if found another valid constructor with more parameters
            if ((constructorMetadata.getConstructor() == null) || (constructorMetadata
                .getFormalParameters().size() < paramTypes.length)) {
                constructorMetadata.setConstructor(constructor);
                constructorMetadata.getFormalParameters().clear();
                constructorMetadata.getFormalParameters().addAll(formalParameters);
            } else if (constructorMetadata.getFormalParameters().size() == paramTypes.length) {
                // TODO: log warning about finding another valid constructor
                // with the same number
                // of parameters
            } else {
                // this is a constructor will less parameters, nothing to do
            }
        }

        metadata.setConstructor(constructorMetadata);
    }

    protected void parseEncoded(Class<?> cls) {
        Encoded encoded = cls.getAnnotation(Encoded.class);
        if (encoded != null) {
            metadata.setEncoded(true);
        }
    }

    public ClassMetadata getMetadata() {
        return metadata;
    }

}
