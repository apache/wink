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
package org.apache.wink.common.internal.lifecycle;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.InjectableFactory;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.ConstructorMetadata;
import org.apache.wink.common.internal.runtime.RuntimeContext;

public class CreationUtils {

    private CreationUtils() {
    }

    /**
     * creates object (StaticResource or Provider) based on its ClassMetadata
     * 
     * @param metadata
     * @param runtimeContext
     * @return created object
     */
    static Object createObject(ClassMetadata metadata, RuntimeContext runtimeContext) {
        try {
            // use constructor to create a prototype
            ConstructorMetadata constructorMetadata = metadata.getConstructor();
            Constructor<?> constructor = constructorMetadata.getConstructor();
            List<Injectable> formalParameters = constructorMetadata.getFormalParameters();
            Object[] params =
                InjectableFactory.getInstance().instantiate(formalParameters, runtimeContext);
            Object object = constructor.newInstance(params);
            injectFields(object, metadata, runtimeContext);
            return object;
        } catch (RuntimeException e) {
            throw e;
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException)targetException;
            }
            throw new ObjectCreationException(targetException);
        } catch (Exception e) {
            throw new ObjectCreationException(e);
        }
    }

    public static void injectFields(final Object object,
                                    ClassMetadata metadata,
                                    RuntimeContext runtimeContext) throws IOException,
        PrivilegedActionException {

        List<Injectable> injectableFields = metadata.getInjectableFields();
        for (Injectable injectableData : injectableFields) {

            Object value = injectableData.getValue(runtimeContext);
            Member member = injectableData.getMember();
            if (member instanceof Field) {
                injectField(object, value, (Field)member);
            } else if (member instanceof Method) {
                invokeMethod(object, value, (Method)member);
            } else {
                // should never get here
                throw new WebApplicationException();
            }
        }
    }

    static void invokeMethod(final Object object, final Object value, final Method method)
        throws PrivilegedActionException {
        AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

            public Object run() throws Exception {
                if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method
                    .getDeclaringClass().getModifiers())) {
                    method.setAccessible(true);
                }
                method.invoke(object, value);
                return null;
            }
        });

    }

    static void injectField(final Object object, final Object value, final Field field)
        throws PrivilegedActionException {
        AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

            public Object run() throws Exception {
                if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field
                    .getDeclaringClass().getModifiers())) {
                    field.setAccessible(true);
                }
                field.set(object, value);
                return null;
            }
        });
    }
}
