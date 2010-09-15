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

package org.apache.wink.server.internal.registry;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.PathSegmentImpl;
import org.apache.wink.common.internal.registry.BoundInjectable;
import org.apache.wink.common.internal.registry.ContextAccessor;
import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.InjectableFactory;
import org.apache.wink.common.internal.registry.ValueConvertor.ConversionException;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.internal.utils.StringUtils;
import org.apache.wink.server.internal.handlers.SearchResult;

public class ServerInjectableFactory extends InjectableFactory {

    @Override
    public Injectable createContextParam(Class<?> classType, Annotation[] annotations, Member member) {
        return new ServerContextParam(classType, annotations, member);
    }

    @Override
    public Injectable createCookieParam(String value,
                                        Class<?> classType,
                                        Type genericType,
                                        Annotation[] annotations,
                                        Member member) {
        return new CookieParamBinding(value, classType, genericType, annotations, member);
    }

    @Override
    public Injectable createEntityParam(Class<?> classType,
                                        Type genericType,
                                        Annotation[] annotations,
                                        Member member) {
        return new EntityParam(classType, genericType, annotations, member);
    }

    @Override
    public Injectable createFormParam(String value,
                                      Class<?> classType,
                                      Type genericType,
                                      Annotation[] annotations,
                                      Member member) {
        return new FormParamBinding(value, classType, genericType, annotations, member);
    }

    @Override
    public Injectable createHeaderParam(String value,
                                        Class<?> classType,
                                        Type genericType,
                                        Annotation[] annotations,
                                        Member member) {
        return new HeaderParamBinding(value, classType, genericType, annotations, member);
    }

    @Override
    public Injectable createMatrixParam(String value,
                                        Class<?> classType,
                                        Type genericType,
                                        Annotation[] annotations,
                                        Member member) {
        return new MatrixParamBinding(value, classType, genericType, annotations, member);
    }

    @Override
    public Injectable createPathParam(String value,
                                      Class<?> classType,
                                      Type genericType,
                                      Annotation[] annotations,
                                      Member member) {
        return new PathParamBinding(value, classType, genericType, annotations, member);
    }

    @Override
    public Injectable createQueryParam(String value,
                                       Class<?> classType,
                                       Type genericType,
                                       Annotation[] annotations,
                                       Member member) {
        return new QueryParamBinding(value, classType, genericType, annotations, member);
    }

    /**
     * Used for injecting a field or parameter of JAX-RS resource with a
     * context, as defined by the JAX-RS spec. First searches for a
     * ContextResolver to get the context to inject, and if none is found, then
     * tries one of the built-in types of context
     */
    public static class ServerContextParam extends Injectable {

        private ContextAccessor contextAccessor;

        public ServerContextParam(Class<?> type, Annotation[] annotations, Member member) {
            super(ParamType.CONTEXT, type, type, annotations, member);
            if (type != HttpServletRequest.class && type != HttpServletResponse.class) {
                contextAccessor = new ContextAccessor();
            } else {
                // due to strict checking of HttpServletRequest and
                // HttpServletResponse
                // injections, a special injector must be used
                contextAccessor = new ServletContextAccessor();
            }
        }

        @Override
        public Object getValue(RuntimeContext runtimeContext) {
            return contextAccessor.getContext(getType(), runtimeContext);
        }
    }

    /**
     * Used for injecting a field or parameter of JAX-RS resource that has no
     * annotation on it - represents the request entity.
     */
    public static class EntityParam extends Injectable {

        public EntityParam(Class<?> type, Type genericType, Annotation[] annotations, Member member) {
            super(ParamType.ENTITY, type, genericType, annotations, member);
        }

        @SuppressWarnings("unchecked")
        public Object getValue(RuntimeContext runtimeContext) throws IOException {
            if (runtimeContext == null) {
                return null;
            }

            Class<?> paramType = getType();

            // check if there is a provider that can handle this parameter
            Providers providers = runtimeContext.getProviders();
            if (providers != null) {
                MediaType mediaType = runtimeContext.getHttpHeaders().getMediaType();
                if (mediaType == null) {
                    mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
                }
                MessageBodyReader mbr =
                    providers.getMessageBodyReader(paramType,
                                                   getGenericType(),
                                                   getAnnotations(),
                                                   mediaType);

                if (mbr != null) {
                    Object read =
                        mbr.readFrom(paramType,
                                     getGenericType(),
                                     getAnnotations(),
                                     mediaType,
                                     runtimeContext.getHttpHeaders().getRequestHeaders(),
                                     runtimeContext.getInputStream());
                    return read;
                }
            }
            throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
        }
    }

    /**
     * Used for injecting a field or parameter of JAX-RS resource with the value
     * of a matrix parameter
     */
    public static class MatrixParamBinding extends BoundInjectable {

        public MatrixParamBinding(String variableName,
                                  Class<?> type,
                                  Type genericType,
                                  Annotation[] annotations,
                                  Member member) {
            super(ParamType.MATRIX, variableName, type, genericType, annotations, member);
        }

        @Override
        public Object getValue(RuntimeContext runtimeContext) throws IOException {
            if (runtimeContext == null) {
                return null;
            }
            List<String> allValues = new ArrayList<String>();
            List<PathSegment> segments =
                runtimeContext.getAttribute(SearchResult.class).getData().getMatchedURIs().get(0);
            // get the matrix parameter only from the last segment
            PathSegment segment = segments.get(segments.size() - 1);
            MultivaluedMap<String, String> matrixParameters = segment.getMatrixParameters();
            List<String> values = matrixParameters.get(getName());
            if (values != null) {
                allValues.addAll(values);
            }

            if (allValues.size() == 0 && hasDefaultValue()) {
                allValues.add(getDefaultValue());
            }

            decodeValues(allValues);

            // we found matrix parameters with the specified name
            try {
                return getConvertor().convert(allValues);
            } catch (ConversionException e) {
                throw new WebApplicationException(e.getCause(), Response.Status.NOT_FOUND);
            }
        }
    }

    /**
     * Used for injecting a field or parameter of JAX-RS resource with the value
     * of a query parameter
     */
    public static class QueryParamBinding extends BoundInjectable {

        public QueryParamBinding(String variableName,
                                 Class<?> type,
                                 Type genericType,
                                 Annotation[] annotations,
                                 Member member) {
            super(ParamType.QUERY, variableName, type, genericType, annotations, member);
        }

        @Override
        public Object getValue(RuntimeContext runtimeContext) throws IOException {
            if (runtimeContext == null) {
                return null;
            }
            UriInfo uriInfo = runtimeContext.getUriInfo();
            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters(false);
            List<String> values = queryParameters.get(getName());
            if (values == null) {
                values = new LinkedList<String>();
            }
            if (values.size() == 0 && hasDefaultValue()) {
                values.add(getDefaultValue());
            }

            decodeValues(values);

            // we found query parameter values with the specified name
            try {
                return getConvertor().convert(values);
            } catch (ConversionException e) {
                throw new WebApplicationException(e.getCause(), Response.Status.NOT_FOUND);
            }
        }

        @Override
        protected String decodeValue(String value) {
            // also decodes the '+' signs into spaces
            return UriEncoder.decodeQuery(value);
        }

    }

    /**
     * Used for injecting a field or parameter of JAX-RS resource with the value
     * of a Form parameter
     */
    public static class FormParamBinding extends BoundInjectable {

        static final String                                FORM_PARAMATERS             =
                                                                                           "wink.formParameters"; //$NON-NLS-1$
        public final static MultivaluedMap<String, String> dummyMultivaluedMap         = null;
        private static Type                                MULTIVALUED_MAP_STRING_TYPE = null;

        static {
            try {
                MULTIVALUED_MAP_STRING_TYPE =
                    FormParamBinding.class.getField("dummyMultivaluedMap").getGenericType(); //$NON-NLS-1$
            } catch (SecurityException e) {
                throw new WebApplicationException(e);
            } catch (NoSuchFieldException e) {
                throw new WebApplicationException(e);
            }
        }

        public FormParamBinding(String variableName,
                                Class<?> type,
                                Type genericType,
                                Annotation[] annotations,
                                Member member) {
            super(ParamType.FORM, variableName, type, genericType, annotations, member);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object getValue(RuntimeContext runtimeContext) throws IOException {
            if (runtimeContext == null) {
                return null;
            }

            // request must be application/x-www-form-urlencoded
            MediaType mediaType = runtimeContext.getHttpHeaders().getMediaType();
            if (!MediaTypeUtils.equalsIgnoreParameters(mediaType,
                                                       MediaType.APPLICATION_FORM_URLENCODED_TYPE)) {
                return null;
            }

            // see if we already have the form parameters, which will happen if
            // there
            // is more than one form parameter on the method
            MultivaluedMap<String, String> formParameters =
                (MultivaluedMap<String, String>)runtimeContext.getAttributes().get(FORM_PARAMATERS);
            if (formParameters == null) {
                // read the request body as an entity parameter to get the form
                // parameters
                EntityParam entityParam =
                    new EntityParam(MultivaluedMap.class, MULTIVALUED_MAP_STRING_TYPE,
                                    getAnnotations(), null);
                formParameters =
                    (MultivaluedMap<String, String>)entityParam.getValue(runtimeContext);
                if (formParameters.isEmpty()) {
                    // see E011 at
                    // http://jcp.org/aboutJava/communityprocess/maintenance/jsr311/311ChangeLog.html
                    // Perhaps the message body was already consumed by a
                    // servlet filter. Let's try the servlet request parameters
                    // instead.
                    Map map =
                        RuntimeContextTLS.getRuntimeContext()
                            .getAttribute(HttpServletRequest.class).getParameterMap();
                    // We can't easily use MultivaluedMap.putAll because we have
                    // a map whose values are String[]
                    // Let's iterate and call the appropriate MultivaluedMap.put
                    // method.
                    for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                        String key = (String)it.next();
                        String[] value = (String[])map.get(key);
                        formParameters.put(key, Arrays.asList(value));
                    }
                }
                runtimeContext.getAttributes().put(FORM_PARAMATERS, formParameters);
            }

            // get the values of the parameter
            List<String> values = formParameters.get(getName());
            if (values == null) {
                values = new LinkedList<String>();
            }

            // TODO: do we add also all the query parameters???

            if (values.size() == 0 && hasDefaultValue()) {
                values.add(getDefaultValue());
            }

            // decode all values
            decodeValues(values);

            try {
                return getConvertor().convert(values);
            } catch (ConversionException e) {
                // See E010
                // http://jcp.org/aboutJava/communityprocess/maintenance/jsr311/311ChangeLog.html:
                // "400 status code should be returned if an exception is
                // raised during @FormParam-annotated parameter construction"
                throw new WebApplicationException(e.getCause(), Response.Status.BAD_REQUEST);
            }
        }

        @Override
        protected String decodeValue(String value) {
            // also decodes the '+' signs into spaces
            return UriEncoder.decodeQuery(value);
        }
    }

    /**
     * Used for injecting a field or parameter of JAX-RS resource with the value
     * of a path template variable
     */
    public static class PathParamBinding extends BoundInjectable {

        public PathParamBinding(String variableName,
                                Class<?> type,
                                Type genericType,
                                Annotation[] annotations,
                                Member member) {
            super(ParamType.PATH, variableName, type, genericType, annotations, member);
        }

        @Override
        public Object getValue(RuntimeContext runtimeContext) throws IOException {
            if (runtimeContext == null) {
                return null;
            }

            MultivaluedMap<String, List<PathSegment>> pathSegmentsMap =
                runtimeContext.getAttribute(SearchResult.class).getData()
                    .getMatchedVariablesPathSegments();
            List<PathSegment> segments = null;
            List<List<PathSegment>> listOfListPathSegments = pathSegmentsMap.get(getName());
            if (listOfListPathSegments != null && listOfListPathSegments.size() > 0) {
                segments = listOfListPathSegments.get(listOfListPathSegments.size() - 1);
            }
            if (segments != null && segments.size() > 0) {
                // special handling for PathSegment
                if (isTypeOf(PathSegment.class)) {
                    // return only the last segment
                    PathSegment segment = segments.get(segments.size() - 1);
                    if (!isEncoded()) {
                        segment = PathSegmentImpl.decode(segment);
                    }
                    return segment;
                }

                // special handling for collection of PathSegment
                if (isTypeCollectionOf(PathSegment.class)) {
                    // return all segments
                    List<PathSegment> list = segments;
                    if (!isEncoded()) {
                        // decode all path segments
                        list = new ArrayList<PathSegment>(segments.size());
                        for (PathSegment segment : segments) {
                            list.add(PathSegmentImpl.decode(segment));
                        }
                    }
                    return asTypeCollection(list, null);
                }
            }

            // for all other types and for cases where the default value should
            // be used
            UriInfo uriInfo = runtimeContext.getUriInfo();
            MultivaluedMap<String, String> variables = uriInfo.getPathParameters(false);
            List<String> values = variables.get(getName());
            if (values == null) {
                values = new LinkedList<String>();
            }

            // use default value
            if (values.size() == 0 && hasDefaultValue()) {
                String defaultValue = getDefaultValue();
                // if the injected type is a PathSegment or some collection of
                // PathSegment then
                // split the default value
                // into separate segments, otherwise, pass the default value
                // as-is.
                if (isTypeOf(PathSegment.class) || isTypeCollectionOf(PathSegment.class)) {
                    String[] segmentsArray = StringUtils.fastSplit(defaultValue, "/", true); //$NON-NLS-1$
                    values.addAll(Arrays.asList(segmentsArray));
                } else {
                    values.add(defaultValue);
                }
                decodeValues(values);
                try {
                    return getConvertor().convert(values);
                } catch (ConversionException e) {
                    throw new WebApplicationException(e.getCause(), Response.Status.NOT_FOUND);
                }
            }

            decodeValues(values);

            try {
                // does not make sense to support List as a PathParam method
                // parameter, so, we get the last value:
                if (values.size() > 0) {
                    return getConvertor().convert(values.get(values.size() - 1));
                } else {
                    return getConvertor().convert(values);
                }
            } catch (ConversionException e) {
                throw new WebApplicationException(e.getCause(), Response.Status.NOT_FOUND);
            }
        }
    }

    /**
     * Used for injecting a field or parameter of JAX-RS resource with the value
     * of a request header
     */
    public static class HeaderParamBinding extends BoundInjectable {

        public HeaderParamBinding(String variableName,
                                  Class<?> type,
                                  Type genericType,
                                  Annotation[] annotations,
                                  Member member) {
            super(ParamType.HEADER, variableName, type, genericType, annotations, member);
        }

        @Override
        public Object getValue(RuntimeContext runtimeContext) throws IOException {
            if (runtimeContext == null) {
                return null;
            }

            // for all headers
            HttpHeaders httpHeaders = runtimeContext.getHttpHeaders();
            List<String> values = httpHeaders.getRequestHeader(getName());
            if (values == null) {
                values = new LinkedList<String>();
            }
            if (values.size() == 0 && hasDefaultValue()) {
                values.add(getDefaultValue());
            }

            try {
                return getConvertor().convert(values);
            } catch (ConversionException e) {
                throw new WebApplicationException(e.getCause(), Response.Status.BAD_REQUEST);
            }
        }
    }

    /**
     * Used for injecting a field or parameter of JAX-RS resource with the value
     * of a request cookie
     */
    public static class CookieParamBinding extends BoundInjectable {

        public CookieParamBinding(String variableName,
                                  Class<?> type,
                                  Type genericType,
                                  Annotation[] annotations,
                                  Member member) {
            super(ParamType.COOKIE, variableName, type, genericType, annotations, member);
        }

        @Override
        public Object getValue(RuntimeContext runtimeContext) throws IOException {
            if (runtimeContext == null) {
                return null;
            }
            String value = null;
            HttpHeaders httpHeaders = runtimeContext.getHttpHeaders();
            Map<String, Cookie> values = httpHeaders.getCookies();

            Cookie cookie = null;
            if (values.size() > 0) {
                cookie = values.get(getName());
            }

            if (cookie == null && hasDefaultValue()) {
                cookie = new Cookie(getName(), getDefaultValue());
            }

            if (cookie != null) {
                // special handling for List<Cookie>
                if (isTypeCollectionOf(Cookie.class)) {
                    return elementAsTypeCollection(cookie, new CookieComparator());
                }

                // special handling for Cookie
                if (isTypeOf(Cookie.class)) {
                    return cookie;
                }

                // for all other types
                value = cookie.getValue();
            }

            try {
                return getConvertor().convert(value);
            } catch (ConversionException e) {
                throw new WebApplicationException(e.getCause(), Response.Status.BAD_REQUEST);
            }
        }

        public static class CookieComparator implements Comparator<Cookie> {

            public int compare(Cookie o1, Cookie o2) {
                int val = 0;
                if (o1.getName() != null) {
                    val = o1.getName().compareTo(o2.getName());
                    if (val != 0) {
                        return val;
                    }
                }
                if (o1.getValue() != null) {
                    val = o1.getValue().compareTo(o2.getValue());
                    if (val != 0) {
                        return val;
                    }
                }
                if (o1.getPath() != null) {
                    val = o1.getPath().compareTo(o2.getPath());
                    if (val != 0) {
                        return val;
                    }
                }
                if (o1.getDomain() != null) {
                    val = o1.getDomain().compareTo(o2.getDomain());
                    if (val != 0) {
                        return val;
                    }
                }
                return o1.getVersion() - o2.getVersion();
            }
        }
    }

}
