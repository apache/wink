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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.PathSegment;

import org.apache.wink.common.internal.utils.GenericsUtils;
import org.apache.wink.common.internal.utils.UriHelper;

/**
 * Provides conversion from string value to proper java object.
 */
public abstract class ValueConvertor {

    public static class ConversionException extends RuntimeException {

        private static final long serialVersionUID = -450326706168680880L;

        public ConversionException() {
            super();
        }

        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }

        public ConversionException(String message) {
            super(message);
        }

        public ConversionException(Throwable cause) {
            super(cause);
        }
    }

    public abstract Object convert(String value) throws WebApplicationException;

    public abstract Object convert(List<String> value) throws WebApplicationException;

    public Class<?> getConcreteType(Class<?> type) {
        return type;
    }

    public Object convert(String[] value) throws WebApplicationException {
        if (value == null || value.length == 0) {
            return convert(new ArrayList<String>());
        }
        return convert(Arrays.asList(value));
    }

    public static ValueConvertor createValueConvertor(Class<?> type) {
        return createValueConvertor(type, null);
    }

    public static ValueConvertor createValueConvertor(Class<?> type, Type genericType) {
        if (type.isArray()) {
            return createArrayValueConvertor(type.getComponentType(), genericType);
        }
        return createConcreteValueConvertor(type, genericType);
    }

    private static ValueConvertor createArrayValueConvertor(Class<?> componentType, Type genericType) {
        ValueConvertor concreteConvertor = createConcreteValueConvertor(componentType, genericType);
        return new ArrayValueConvertor(concreteConvertor, componentType);
    }

    public static ValueConvertor createConcreteValueConvertor(Class<?> classType, Type genericType) {
        // if (classType.equals(List.class)
        // &&
        // PathSegment.class.equals(GenericsUtils.getGenericParamType(genericType)))
        // {
        // return new DummyConvertor();
        // } else
        if (classType.equals(List.class)) {
            return new ListConvertor(getSingleValueConvertor(GenericsUtils
                .getGenericParamType(genericType)));
        } else if (classType.equals(SortedSet.class)) {
            return new SortedSetConvertor(getSingleValueConvertor(GenericsUtils
                .getGenericParamType(genericType)));
        } else if (classType.equals(Set.class)) {
            return new SetConvertor(getSingleValueConvertor(GenericsUtils
                .getGenericParamType(genericType)));
        } else if (classType.isEnum()) {
            return getEnumValueConvertor(classType);
        } else {
            return getSingleValueConvertor(classType);
        }
    }

    private static ValueConvertor getEnumValueConvertor(Class<?> classType) {
        if (classType == null) {
            return null;
        }

        try {
            Constructor<?> constructor = classType.getConstructor(String.class);
            return new ConstructorConvertor(constructor);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        }

        // see JAX-RS 1.1 C006:
        // http://jcp.org/aboutJava/communityprocess/maintenance/jsr311/311ChangeLog.html
        // precendence for enums is fromString, then valueOf
        try {
            Method valueOf = classType.getDeclaredMethod("fromString", String.class);
            return new FromStringConvertor(valueOf);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
            try {
                Method fromString = classType.getDeclaredMethod("valueOf", String.class);
                return new ValueOfConvertor(fromString);
            } catch (SecurityException e2) {
            } catch (NoSuchMethodException e2) {
            }
        }

        throw new IllegalArgumentException("type '" + classType
            + "' is not a supported resource method parameter");
    }

    private static ValueConvertor getSingleValueConvertor(Class<?> classType) {
        if (classType.equals(String.class)) {
            return new StringConvertor();
        } else if (classType.equals(Character.class)) {
            return new CharacterConvertor();
        } else if (classType.isPrimitive()) {
            return new PrimitiveConvertor(classType);
        } else if (classType.equals(PathSegment.class)) {
            return new PathSegmentConvertor();
        } else {
            return getComplexValueConverter(classType);
        }
    }

    private static ValueConvertor getComplexValueConverter(Class<?> classType) {
        if (classType == null) {
            return null;
        }

        try {
            Constructor<?> constructor = classType.getConstructor(String.class);
            return new ConstructorConvertor(constructor);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        }

        try {
            Method valueOf = classType.getDeclaredMethod("valueOf", String.class);
            return new ValueOfConvertor(valueOf);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
            // see JAX-RS 1.1 C006:
            // http://jcp.org/aboutJava/communityprocess/maintenance/jsr311/311ChangeLog.html
            // fallback to fromString method when no valueOf method exists
            try {
                Method fromString = classType.getDeclaredMethod("fromString", String.class);
                return new FromStringConvertor(fromString);
            } catch (SecurityException e2) {
            } catch (NoSuchMethodException e2) {
            }
        }

        throw new IllegalArgumentException("type '" + classType
            + "' is not a supported resource method parameter");
    }

    private static class ArrayValueConvertor extends ValueConvertor {

        private ValueConvertor concrete;
        private Class<?>       type;

        public ArrayValueConvertor(ValueConvertor concrete, Class<?> type) {
            this.concrete = concrete;
            this.type = concrete.getConcreteType(type);
        }

        @Override
        public Object convert(String value) throws WebApplicationException {
            Object[] array = (Object[])Array.newInstance(type, 1);
            array[0] = concrete.convert(value);
            return null;
        }

        @Override
        public Object convert(List<String> value) throws WebApplicationException {
            if (value == null || value.size() == 0) {
                return Array.newInstance(type, 0);
            }

            Object array = Array.newInstance(type, value.size());
            for (int i = 0; i < value.size(); ++i) {
                Array.set(array, i, concrete.convert(value.get(i)));
            }
            return array;
        }
    }

    private static abstract class SingleValueConvertor extends ValueConvertor {

        RuntimeException createConversionException(String value, Class<?> targetClass, Throwable e) {
            if (e instanceof WebApplicationException) {
                return (RuntimeException)e;
            }
            String message = String.format("Cannot convert value '%s' to %s", value, targetClass);
            return new ConversionException(message, e);
        }

        public Object convert(List<String> values) throws WebApplicationException {
            if (values == null || values.size() == 0) {
                return convert((String)null);
            }
            return convert(values.get(0));
        }
    }

    private static class ConstructorConvertor extends SingleValueConvertor {

        private Constructor<?> constructor;

        public ConstructorConvertor(Constructor<?> constructor) {
            this.constructor = constructor;
        }

        public Object convert(String value) {
            if (value == null) {
                return null;
            }
            try {
                return constructor.newInstance(value);
            } catch (IllegalArgumentException e) {
                throw createConversionException(value, constructor.getDeclaringClass(), e);
            } catch (InstantiationException e) {
                throw createConversionException(value, constructor.getDeclaringClass(), e);
            } catch (IllegalAccessException e) {
                throw createConversionException(value, constructor.getDeclaringClass(), e);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                throw createConversionException(value,
                                                constructor.getDeclaringClass(),
                                                targetException);
            }
        }
    }

    private static class ValueOfConvertor extends SingleValueConvertor {

        private Method method;

        public ValueOfConvertor(Method method) {
            this.method = method;
        }

        public Object convert(String value) {
            if (value == null) {
                return null;
            }
            try {
                Object objToReturn = method.invoke(null, value);
                // can't use instanceof here?
                if (!objToReturn.getClass().equals((method.getDeclaringClass()))) {
                    // enforce E009 from http://jcp.org/aboutJava/communityprocess/maintenance/jsr311/311ChangeLog.html
                    // note that we don't care what return object type the method declares, only what it actually returns
                    throw createConversionException(value, method.getDeclaringClass(),
                            new Exception("Value returned from method " + method.toString() + " must be "
                                    + "of type " + method.getDeclaringClass() + ".  "
                                    + "Returned object was type " + objToReturn.getClass()));
                }
                return objToReturn;
            } catch (IllegalArgumentException e) {
                throw createConversionException(value, method.getDeclaringClass(), e);
            } catch (IllegalAccessException e) {
                throw createConversionException(value, method.getDeclaringClass(), e);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                throw createConversionException(value, method.getDeclaringClass(), targetException);
            }
        }
    }

    /**
     * FromStringConvertor class exists only to make it obvious which method we
     * picked up from the custom *Param type being converted See
     * http://jcp.org/aboutJava
     * /communityprocess/maintenance/jsr311/311ChangeLog.html C006
     */
    private static class FromStringConvertor extends ValueOfConvertor {
        public FromStringConvertor(Method method) {
            super(method);
        }
    }

    private static class StringConvertor extends SingleValueConvertor {

        @Override
        public Object convert(String value) throws WebApplicationException {
            return value;
        }
    }

    private static class CharacterConvertor extends SingleValueConvertor {

        @Override
        public Object convert(String value) throws WebApplicationException {
            if (value == null || value.length() == 0) {
                return null;
            }
            return Character.valueOf(value.charAt(0));
        }
    }

    private static class PathSegmentConvertor extends SingleValueConvertor {

        @Override
        public PathSegment convert(String value) throws WebApplicationException {
            if (value == null) {
                return null;
            }
            List<PathSegment> segments = UriHelper.parsePath(value);
            if (segments.isEmpty()) {
                return null;
            }
            return segments.get(segments.size() - 1);
        }
    }

    // private static class PathSegmentListConvertor extends
    // SingleValueConvertor {
    // @Override
    // public List<PathSegment> convert(String value) throws
    // WebApplicationException {
    // if (value == null) {
    // return new ArrayList<PathSegment>();
    // }
    // return UriBuilderImpl.parsePath(value);
    // }
    // }
    //
    private static class PrimitiveConvertor extends SingleValueConvertor {

        final protected Class<?> targetClass;

        PrimitiveConvertor(Class<?> targetClass) {
            this.targetClass = targetClass;
        }

        @Override
        public Object convert(String value) throws WebApplicationException {
            try {
                if (targetClass.equals(boolean.class)) {
                    if (value == null) {
                        return Boolean.valueOf(false);
                    }
                    return Boolean.valueOf(value).booleanValue();
                }
                if (targetClass.equals(char.class)) {
                    if (value == null || value.length() == 0) {
                        return Character.valueOf('\u0000');
                    }
                    return Character.valueOf(value.charAt(0)).charValue();
                }
                if (targetClass.equals(byte.class)) {
                    if (value == null) {
                        return Byte.valueOf((byte)0);
                    }
                    return Byte.valueOf(value).byteValue();
                }
                if (targetClass.equals(short.class)) {
                    if (value == null) {
                        return Short.valueOf((short)0);
                    }
                    return Short.valueOf(value).shortValue();
                }
                if (targetClass.equals(int.class)) {
                    if (value == null) {
                        return Integer.valueOf(0);
                    }
                    return Integer.valueOf(value).intValue();
                }
                if (targetClass.equals(long.class)) {
                    if (value == null) {
                        return Long.valueOf(0L);
                    }
                    return Long.valueOf(value).longValue();
                }
                if (targetClass.equals(float.class)) {
                    if (value == null) {
                        return Float.valueOf(0.0f);
                    }
                    return Float.valueOf(value).floatValue();
                }
                if (targetClass.equals(double.class)) {
                    if (value == null) {
                        return Double.valueOf(0.0d);
                    }
                    return Double.valueOf(value).doubleValue();
                }
            } catch (Exception e) {
                throw createConversionException(value, targetClass, e);
            }
            throw createConversionException(value, targetClass, null);
        }
    }

    private static abstract class CollectionValueConvertor extends ValueConvertor {

        protected ValueConvertor converter;

        public CollectionValueConvertor(ValueConvertor converter) {
            this.converter = converter;
        }

        public Object convert(String value) {
            ArrayList<String> list = new ArrayList<String>();
            if (value != null) {
                list.add(value);
            }
            return convert(list);
        }

        protected Collection<Object> convertCollection(List<String> values, Collection<Object> out) {
            for (String string : values) {
                out.add(converter.convert(string));
            }
            return out;
        }
    }

    private static class ListConvertor extends CollectionValueConvertor {

        public ListConvertor(ValueConvertor converter) {
            super(converter);
        }

        public Object convert(List<String> values) throws WebApplicationException {
            return convertCollection(values, new ArrayList<Object>(values.size()));
        }
    }

    private static class SetConvertor extends CollectionValueConvertor {

        public SetConvertor(ValueConvertor converter) {
            super(converter);
        }

        public Object convert(List<String> values) throws WebApplicationException {
            return convertCollection(values, new LinkedHashSet<Object>());
        }
    }

    private static class SortedSetConvertor extends CollectionValueConvertor {

        public SortedSetConvertor(ValueConvertor converter) {
            super(converter);
        }

        public Object convert(List<String> values) throws WebApplicationException {
            return convertCollection(values, new TreeSet<Object>());
        }
    }

}
