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
import java.util.List;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.wink.common.internal.runtime.RuntimeContext;
import org.apache.wink.common.internal.utils.GenericsUtils;

public class InjectableFactory {

    private static InjectableFactory instance = new InjectableFactory();

    public static InjectableFactory getInstance() {
        return instance;
    }

    public static void setInstance(InjectableFactory instance) {
        InjectableFactory.instance = instance;
    }

    /**
     * Instantiates a list of formal parameters into an Object array
     * 
     * @param runtimeContext
     * @param formalParameters
     * @return
     * @throws IOException
     */
    public Object[] instantiate(List<Injectable> formalParameters, RuntimeContext runtimeContext)
        throws IOException {
        Object[] result = new Object[formalParameters.size()];
        for (int pos = 0; pos < result.length; pos++) {
            Injectable fp = formalParameters.get(pos);
            result[pos] = fp.getValue(runtimeContext);
        }
        return result;
    }

    public Injectable create(Type genericType, Annotation[] annotations, Member member,
        boolean enclosingEncoded) {
        Class<?> classType = GenericsUtils.getClassType(genericType);

        MatrixParam matrix = null;
        PathParam path = null;
        QueryParam query = null;
        HeaderParam header = null;
        CookieParam cookie = null;
        FormParam form = null;
        Context context = null;
        Encoded encodedAnn = null;
        DefaultValue defaultValueAnn = null;

        Injectable injectable = null;
        int annotationsCounter = 0;
        for (int i = 0; i < annotations.length; ++i) {
            if (annotations[i].annotationType().equals(MatrixParam.class)) {
                matrix = (MatrixParam) annotations[i];
                ++annotationsCounter;
            } else if (annotations[i].annotationType().equals(PathParam.class)) {
                path = (PathParam) annotations[i];
                ++annotationsCounter;
            } else if (annotations[i].annotationType().equals(QueryParam.class)) {
                query = (QueryParam) annotations[i];
                ++annotationsCounter;
            } else if (annotations[i].annotationType().equals(HeaderParam.class)) {
                header = (HeaderParam) annotations[i];
                ++annotationsCounter;
            } else if (annotations[i].annotationType().equals(CookieParam.class)) {
                cookie = (CookieParam) annotations[i];
                ++annotationsCounter;
            } else if (annotations[i].annotationType().equals(FormParam.class)) {
                form = (FormParam) annotations[i];
                ++annotationsCounter;
            } else if (annotations[i].annotationType().equals(Context.class)) {
                context = (Context) annotations[i];
                ++annotationsCounter;
            } else if (annotations[i].annotationType().equals(Encoded.class)) {
                encodedAnn = (Encoded) annotations[i];
            } else if (annotations[i].annotationType().equals(DefaultValue.class)) {
                defaultValueAnn = (DefaultValue) annotations[i];
            }
        }

        if (annotationsCounter > 1) {
            throw new IllegalStateException("Conflicting parameter annotations for "
                + member.getName());
        }

        if (matrix != null) {
            injectable = createMatrixParam(matrix.value(), classType, genericType, annotations,
                member);
        } else if (path != null) {
            injectable = createPathParam(path.value(), classType, genericType, annotations, member);
        } else if (query != null) {
            injectable = createQueryParam(query.value(), classType, genericType, annotations,
                member);
        } else if (header != null) {
            injectable = createHeaderParam(header.value(), classType, genericType, annotations,
                member);
        } else if (cookie != null) {
            injectable = createCookieParam(cookie.value(), classType, genericType, annotations,
                member);
        } else if (form != null) {
            injectable = createFormParam(form.value(), classType, genericType, annotations, member);
        } else if (context != null) {
            injectable = createContextParam(classType, annotations, member);
        } else {
            injectable = createEntityParam(classType, genericType, annotations, member);
        }

        if (injectable instanceof BoundInjectable) {
            BoundInjectable binding = (BoundInjectable) injectable;
            if (enclosingEncoded || encodedAnn != null) {
                binding.setEncoded(true);
            }
            if (defaultValueAnn != null) {
                binding.setDefaultValue(defaultValueAnn.value());
            }
        }

        return injectable;
    }

    private static class NullInjectable extends Injectable {

        protected NullInjectable(ParamType paramType) {
            super(paramType, null, null, null, null);
        }

        @Override
        public Object getValue(RuntimeContext runtimeContext) {
            return null;
        }
    }

    /**
     * Used for injecting a field or parameter of JAX-RS resource with a
     * context, as defined by the JAX-RS spec. First searches for a
     * ContextResolver to get the context to inject, and if none is found, then
     * tries one of the built-in types of context
     */
    public static class ContextParam extends Injectable {

        private ContextAccessor contextAccessor;

        public ContextParam(Class<?> type, Annotation[] annotations, Member member) {
            super(ParamType.CONTEXT, type, type, annotations, member);
            contextAccessor = new ContextAccessor();
        }

        @Override
        public Object getValue(RuntimeContext runtimeContext) {
            return contextAccessor.getContext(getType(), runtimeContext);
        }
    }

    public Injectable createContextParam(Class<?> classType, Annotation[] annotations, Member member) {
        return new ContextParam(classType, annotations, member);
    }

    public Injectable createMatrixParam(String value, Class<?> classType, Type genericType,
        Annotation[] annotations, Member member) {
        return new NullInjectable(Injectable.ParamType.MATRIX);
    }

    public Injectable createPathParam(String value, Class<?> classType, Type genericType,
        Annotation[] annotations, Member member) {
        return new NullInjectable(Injectable.ParamType.PATH);
    }

    public Injectable createQueryParam(String value, Class<?> classType, Type genericType,
        Annotation[] annotations, Member member) {
        return new NullInjectable(Injectable.ParamType.QUERY);
    }

    public Injectable createHeaderParam(String value, Class<?> classType, Type genericType,
        Annotation[] annotations, Member member) {
        return new NullInjectable(Injectable.ParamType.HEADER);
    }

    public Injectable createCookieParam(String value, Class<?> classType, Type genericType,
        Annotation[] annotations, Member member) {
        return new NullInjectable(Injectable.ParamType.COOKIE);
    }

    public Injectable createFormParam(String value, Class<?> classType, Type genericType,
        Annotation[] annotations, Member member) {
        return new NullInjectable(Injectable.ParamType.FORM);
    }

    public Injectable createEntityParam(Class<?> classType, Type genericType,
        Annotation[] annotations, Member member) {
        return new NullInjectable(Injectable.ParamType.ENTITY);
    }

}
