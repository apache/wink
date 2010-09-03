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
package org.apache.wink.common.internal.utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import org.apache.wink.common.internal.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericsUtils {

    private static final Logger logger = LoggerFactory.getLogger(GenericsUtils.class);

    private GenericsUtils() {
        // prevents creating this class
    }

    /**
     * <p>
     * Returns true if the <tt>cls</tt> is assignable from the generic interface
     * of the <tt>assignable</tt> of the specific raw type.
     * <p>
     * E.g. Let A be class that implements <tt>List&lt;String&gt;</tt>. Calling
     * 
     * <tt>isGenericInterfaceAssignableFrom(String.class, A.class, List.class)</tt>
     * will return <tt>true</tt>.
     * 
     * @param cls
     * @param assignable
     * @param rawType
     * @return
     */
    public static boolean isGenericInterfaceAssignableFrom(Class<?> cls,
                                                           Class<?> assignable,
                                                           Class<?> rawType) {
        Type genericType = GenericsUtils.getGenericInterfaceParamType(assignable, rawType);
        // if genericType == null, assume developer did something like forgot to parameterize
        // their interface, in which case the genericType is indeed assignable from cls
        return (genericType == null) || isAssignableFrom(genericType, cls);
    }

    /**
     * <p>
     * Checks if the <tt>cls</tt> is assignable of from the type
     * <tt>assignable</tt>.
     * <p>
     * For arrays, checks that types of the arrays are assignable.
     * <p>
     * For parameterized types, checks only the raw type and doesn't check the
     * parameters.
     * 
     * @param cls
     * @param assignable
     * @return
     */
    public static boolean isAssignableFrom(Type type, Class<?> cls) {
        if (cls.isArray()) {
            if (type instanceof GenericArrayType) {
                GenericArrayType genericArray = (GenericArrayType)type;
                Class<?> componentType = cls.getComponentType();
                return isAssignableFrom(genericArray.getGenericComponentType(), componentType);
            }
        } else {
            if (type instanceof GenericArrayType == false) {
                Class<?> classType = getClassType(type);
                if (classType.isAssignableFrom(cls)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the Type of parameter of the generic interface of the class.
     * <p>
     * E.g. Let A be class that implements <tt>List&lt;String&gt;</tt>. Calling
     * <tt>getGenericInterfaceType(A.class, List.class)</tt> will return
     * <tt>String.class</tt>.
     * <p>
     * In case the interface has more than one parameter, only the type of the
     * first parameter is returned by this method.
     * 
     * @param cls
     * @param rawType
     * @return java.lang.reflect.Type
     */
    public static Type getGenericInterfaceParamType(Class<?> cls, Class<?> rawType) {
        while (cls != null) {
            Type[] interfaces = cls.getGenericInterfaces();
            for (Type type : interfaces) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType)type;
                    if (pType.getRawType() == rawType) {
                        return pType.getActualTypeArguments()[0];
                    } else {
                        continue;
                    }
                }
                // look through the base interfaces of the current interface
                Type interfaceType = getGenericInterfaceParamType((Class<?>)type, rawType);
                if (interfaceType != null) {
                    return interfaceType;
                }
            }
            cls = cls.getSuperclass();
        }
        // if we're done with the recursive calls, perhaps developer
        // did not parameterize their interface
        return null;
    }

    /**
     * Get the class type of the provided type. If the type is a Class, then
     * type is returned. If the type is ParameterizedType, then the Raw type is
     * returned.
     * <p>
     * E.g. if type is <code>String.class</code>, then <code>String.class</code>
     * is returned. If type is <code>List&lt;String&gt;</code>, then
     * <code>List.class</code> is returned.
     * 
     * @param type the type to return the class type for
     * @return the class type of type
     */
    public static Class<?> getClassType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>)type;
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            return (Class<?>)parameterizedType.getRawType();
        }

        if (type instanceof GenericArrayType) {
            GenericArrayType genericArray = (GenericArrayType)type;
            Class<?> classType = getClassType(genericArray.getGenericComponentType());
            return Array.newInstance(classType, 0).getClass();
        }

        if (type instanceof TypeVariable<?>) {
            return getClassType(((TypeVariable<?>)type).getBounds()[0]);
        }

        if (type instanceof WildcardType) {
            return getClassType(((WildcardType)type).getUpperBounds()[0]);
        }

        logger.error(Messages.getMessage("methodCannotHandleType", String.valueOf(type))); //$NON-NLS-1$
        return null;
    }

    /**
     * Get the class of the parameter of the provided parameterized type. If the
     * type is a Class, then <code>null</code> is returned. If the type is
     * ParameterizedType, then the actual type argument is returned.
     * <p>
     * E.g. if type is <code>String.class</code>, then <code>null</code> is
     * returned. If type is <code>List&lt;String&gt;</code>, then
     * <code>String.class</code> is returned.
     * <p>
     * In case the type has more than one parameter, only the type of the first
     * parameter is returned by this method.
     * 
     * @param type the type to return the class of the parameter for
     * @return the class of the generic parameter of type
     */
    public static Class<?> getGenericParamType(Type type) {
        Class<?> generic = null;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            generic = (Class<?>)actualTypeArguments[0];
        }
        return generic;
    }

}
