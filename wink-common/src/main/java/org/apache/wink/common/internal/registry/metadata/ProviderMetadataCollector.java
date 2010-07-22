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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.InjectableFactory;
import org.apache.wink.common.internal.utils.GenericsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderMetadataCollector extends AbstractMetadataCollector {

    private static final Logger logger = LoggerFactory.getLogger(ProviderMetadataCollector.class);

    private ProviderMetadataCollector(Class<?> clazz) {
        super(clazz);
    }

    public static boolean isProvider(Class<?> cls) {
        /*
         * look for the Provider annotation on super classes (even though
         * @Provider does not have @java.lang.annotation.Inherited) in order to
         * promote better compatibility with expected behavior
         */
        // return cls.getAnnotation(Provider.class) != null;
        if (cls == Object.class) {
            return false;
        }

        if (Modifier.isInterface(cls.getModifiers()) || Modifier.isAbstract(cls.getModifiers())) {
            if (logger.isWarnEnabled()) {
                logger.warn(Messages.getMessage("providerIsInterfaceOrAbstract", cls)); //$NON-NLS-1$
            }
            return false;
        }

        if (cls.getAnnotation(Provider.class) != null) {
            return true;
        }

        Class<?> declaringClass = cls;

        while (declaringClass != null && !declaringClass.equals(Object.class)) {
            // try a superclass
            Class<?> superclass = declaringClass.getSuperclass();
            if (superclass != null && superclass.getAnnotation(Provider.class) != null) {
                if (logger.isWarnEnabled()) {
                    logger.warn(Messages.getMessage("providerShouldBeAnnotatedDirectly", cls)); //$NON-NLS-1$
                }
                return true;
            }

            // try interfaces
            Class<?>[] interfaces = declaringClass.getInterfaces();
            for (Class<?> interfaceClass : interfaces) {
                if (interfaceClass.getAnnotation(Provider.class) != null) {
                    if (logger.isWarnEnabled()) {
                        logger.warn(Messages.getMessage("providerShouldBeAnnotatedDirectly", cls)); //$NON-NLS-1$
                    }
                    return true;
                }
            }
            declaringClass = declaringClass.getSuperclass();
        }

        return false;
    }

    public static ClassMetadata collectMetadata(Class<?> clazz) {
        ProviderMetadataCollector collector = new ProviderMetadataCollector(clazz);
        collector.parseConstructors();
        collector.parseFields();
        return collector.getMetadata();
    }

    @Override
    protected final boolean isConstructorParameterValid(Injectable fp) {
        // This method is declared as final, since parseConstructors(), which
        // calls it, is invoked from the constructor
        return fp.getParamType() == Injectable.ParamType.CONTEXT;
    }

    @Override
    protected Injectable parseAccessibleObject(AccessibleObject field, Type fieldType) {
        Context context = field.getAnnotation(Context.class);
        if (context != null) {
            return InjectableFactory.getInstance().createContextParam(GenericsUtils
                                                                          .getClassType(fieldType),
                                                                      field.getAnnotations(),
                                                                      (Member)field);
        }

        return null;
    }

}
