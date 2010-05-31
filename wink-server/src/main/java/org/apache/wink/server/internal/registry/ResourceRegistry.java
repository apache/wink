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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.WinkApplication;
import org.apache.wink.common.http.HttpHeadersEx;
import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.common.internal.uritemplate.UriTemplateMatcher;
import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor;
import org.apache.wink.common.internal.utils.HeaderUtils;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.internal.utils.SoftConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for maintaining a set of all the known root resources and finding
 * the dispatch method of a request, following the JAX-RS spec.
 */
public class ResourceRegistry {

    private static final Logger                                                    logger             =
                                                                                                          LoggerFactory
                                                                                                              .getLogger(ResourceRegistry.class);

    private List<ResourceRecord>                                                   rootResources;

    private ResourceRecordFactory                                                  resourceRecordsFactory;

    private Lock                                                                   readersLock;
    private Lock                                                                   writersLock;
    private final ApplicationValidator                                             applicationValidator;

    private HashMap<Boolean, SoftConcurrentMap<String, ArrayList<ResourceRecord>>> uriToResourceCache =
                                                                                                          new HashMap<Boolean, SoftConcurrentMap<String, ArrayList<ResourceRecord>>>();

    public ResourceRegistry(LifecycleManagersRegistry factoryRegistry,
                            ApplicationValidator applicationValidator) {
        this.applicationValidator = applicationValidator;
        rootResources = new LinkedList<ResourceRecord>();
        resourceRecordsFactory = new ResourceRecordFactory(factoryRegistry);
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readersLock = readWriteLock.readLock();
        writersLock = readWriteLock.writeLock();
        uriToResourceCache.put(Boolean.TRUE,
                               new SoftConcurrentMap<String, ArrayList<ResourceRecord>>());
        uriToResourceCache.put(Boolean.FALSE,
                               new SoftConcurrentMap<String, ArrayList<ResourceRecord>>());
    }

    /**
     * Add a resource as an object to the registry
     * 
     * @param instance the object to add
     */
    public void addResource(Object instance) {
        addResource(instance, WinkApplication.DEFAULT_PRIORITY);
    }

    /**
     * Add a resource class to the registry
     * 
     * @param clazz the resource class to add
     */
    public void addResource(Class<?> clazz) {
        addResource(clazz, WinkApplication.DEFAULT_PRIORITY);
    }

    /**
     * Add a resource as an object to the registry with a priority
     * 
     * @param clazz the resource class to add
     * @param priority priority of the resource
     */
    public void addResource(Object instance, double priority) {
        logger.debug("Adding resource instance: {} with priority: {}", instance, priority); //$NON-NLS-1$

        writersLock.lock();
        try {
            if (!applicationValidator.isValidResource(instance.getClass())) {
                if (logger.isWarnEnabled()) {
                    logger.warn(Messages
                        .getMessage("resourceClassNotValid", instance.getClass().getName())); //$NON-NLS-1$
                }
                return;
            }

            ResourceRecord record = getRecord(instance);
            record.setPriority(priority);
            rootResources.add(record);
            assertSorted();
        } finally {
            writersLock.unlock();
        }
    }

    /**
     * Add a resource class to the registry with a priority
     * 
     * @param clazz the resource class to add
     * @param priority priority of the resource
     */
    public void addResource(Class<?> clazz, double priority) {
        logger.debug("Adding resource class: {} with priority: {}", clazz, priority); //$NON-NLS-1$

        writersLock.lock();
        try {
            if (!applicationValidator.isValidResource(clazz)) {
                if (logger.isWarnEnabled()) {
                    logger.warn(Messages.getMessage("resourceClassNotValid", clazz.getName())); //$NON-NLS-1$
                }
                return;
            }
            ResourceRecord record = getRecord(clazz);
            record.setPriority(priority);
            rootResources.add(record);
            assertSorted();
        } finally {
            writersLock.unlock();
        }
    }

    /**
     * Get the {@link ResourceRecord} of the specified root resource instance.
     * This is a shortcut for {@code getRecord(instance, true)}
     * 
     * @param instance the resource instance to get the record for
     * @return {@link ResourceRecord} of the instance
     */
    public ResourceRecord getRecord(Object instance) {
        return getRecord(instance, true);
    }

    /**
     * Get the {@link ResourceRecord} of the specified resource instance
     * 
     * @param instance the resource instance to get the record for
     * @param isRootResource specifies whether the instance is a root resource
     *            (true) or sub-resource (false)
     * @return {@link ResourceRecord} of the instance
     */
    public ResourceRecord getRecord(Object instance, boolean isRootResource) {
        return resourceRecordsFactory.getResourceRecord(instance, isRootResource);
    }

    /**
     * Get the {@link ResourceRecord} of the specified resource class
     * 
     * @param clazz the resource class to get the record for
     * @return {@link ResourceRecord} of the instance
     */
    public ResourceRecord getRecord(Class<?> clazz) {
        return resourceRecordsFactory.getResourceRecord(clazz);
    }

    /**
     * Get the list of all the root resource records
     * 
     * @return unmodifiable list of all the root resource records
     */
    public List<ResourceRecord> getRecords() {
        readersLock.lock();
        try {
            return Collections.unmodifiableList(rootResources);
        } finally {
            readersLock.unlock();
        }
    }

    public Set<String> getOptions(ResourceInstance resource) {
        Set<String> set = new HashSet<String>();

        List<MethodMetadata> resourceMethods;
        if (resource.isExactMatch()) {
            resourceMethods = resource.getRecord().getMetadata().getResourceMethods();
        } else {
            String uri = UriTemplateProcessor.normalizeUri(resource.getMatcher().getTail(false));
            List<SubResourceInstance> matchingMethods =
                resource.getRecord().getMatchingSubResourceMethods(uri);
            resourceMethods = new LinkedList<MethodMetadata>();
            for (SubResourceInstance subResource : matchingMethods) {
                resourceMethods.add(subResource.getRecord().getMetadata());
            }
        }

        for (MethodMetadata method : resourceMethods) {
            set.add(method.getHttpMethod());
        }

        return set;
    }

    /**
     * Verify that the root resources list is sorted
     */
    private void assertSorted() {
        // we use the reverse-order comparator because the sort method
        // will sort the elements in ascending order, but we want
        // them sorted in descending order
        Collections.sort(rootResources, Collections.reverseOrder());
        uriToResourceCache.get(Boolean.TRUE).clear();
        uriToResourceCache.get(Boolean.FALSE).clear();
    }

    /**
     * Get a list of all the root resources that match the request.
     * 
     * @param context
     * @return
     */
    public List<ResourceInstance> getMatchingRootResources(String uri) {
        return getMatchingRootResources(uri, true);
    }

    /**
     * Get a list of all the root resources that match the request.
     * 
     * @param context
     * @return
     */
    public List<ResourceInstance> getMatchingRootResources(String uri,
                                                           boolean isContinuedSearchPolicy) {
        if (uri == null) {
            uri = ""; //$NON-NLS-1$
        }
        if (uri.startsWith("/")) { //$NON-NLS-1$
            uri = uri.substring(1);
        }

        ArrayList<ResourceInstance> found = new ArrayList<ResourceInstance>(1);
        ArrayList<ResourceRecord> previousMatched = null;
        /*
         * the previous matches are cached so if a previous URI used is still in
         * the cache, this will find the resources that matched skipping the
         * expensive UriTemplateMatcher.matches()
         */
        previousMatched = uriToResourceCache.get(isContinuedSearchPolicy).get(uri);
        if (previousMatched != null) {
            for (ResourceRecord record : previousMatched) {
                UriTemplateMatcher matcher = record.getTemplateProcessor().matcher();
                if (matcher.matches(uri)) {
                    found.add(new ResourceInstance(record, matcher));
                }
            }
            return found;
        }

        readersLock.lock();
        try {

            previousMatched = new ArrayList<ResourceRecord>();

            // the list of root resource records is already sorted
            for (ResourceRecord record : rootResources) {
                UriTemplateMatcher matcher = record.getTemplateProcessor().matcher();
                if (matcher.matches(uri)) {
                    if (matcher.isExactMatch() || record.hasSubResources()) {
                        previousMatched.add(record);
                        found.add(new ResourceInstance(record, matcher));
                        if (!isContinuedSearchPolicy) {
                            break;
                        }
                    }
                }
            }
            uriToResourceCache.get(isContinuedSearchPolicy).put(uri, previousMatched);
        } finally {
            readersLock.unlock();
        }

        return found;
    }

    /**
     * Attempts to find a resource method to invoke in the specified resource
     * 
     * @param resource the resource to find the method in
     * @param variablesValues a multivalued map of variables values that stores
     *            the variables of the templates that are matched during the
     *            search
     * @param context the context of the current request
     * @return
     */
    public MethodRecord findMethod(ResourceInstance resource, RuntimeContext context)
        throws WebApplicationException {
        List<MethodMetadata> methods = resource.getRecord().getMetadata().getResourceMethods();
        List<MethodMetadataRecord> records = new LinkedList<MethodMetadataRecord>();
        for (MethodMetadata metadata : methods) {
            records.add(new MethodMetadataRecord(metadata));
        }

        // filter the methods according to the spec
        filterDispatchMethods(resource, records, context);

        // select the best matching method
        return selectBestMatchingMethod(records, context);
    }

    /**
     * Attempts to find a sub-resource method to invoke in the specified
     * resource
     * 
     * @param pattern the regex pattern that the 'path' specified on the
     *            sub-resource method must match
     * @param subResourceRecords a list of all the sub-resources (methods and
     *            locators) in the specified resource that matched the request
     * @param resource the resource to find the method in
     * @param variablesValues a multivalued map of variables values that stores
     *            the variables of the templates that are matched during the
     *            search
     * @param context the context of the current request
     * @return
     */
    public SubResourceInstance findSubResourceMethod(String pattern,
                                                     List<SubResourceInstance> subResourceRecords,
                                                     ResourceInstance resource,
                                                     RuntimeContext context)
        throws WebApplicationException {
        // extract the sub-resource methods that have the same path template
        // as the first sub-resource method
        List<SubResourceInstance> subResourceMethods =
            extractSubResourceMethods(pattern, subResourceRecords);

        // filter the methods according to http method/consumes/produces
        filterDispatchMethods(resource, subResourceMethods, context);

        // select the best matching method
        return (SubResourceInstance)selectBestMatchingMethod(subResourceMethods, context);
    }

    /**
     * Extract the sub-resource methods from the specified list that have the
     * same path pattern as the specified pattern
     * 
     * @param pattern
     * @param subResourceRecords
     * @return a list of sub-resource methods whose 'path' pattern match the
     *         specified pattern
     */
    private List<SubResourceInstance> extractSubResourceMethods(String pattern,
                                                                List<SubResourceInstance> subResourceRecords) {

        List<SubResourceInstance> subResourceMethods = new LinkedList<SubResourceInstance>();
        for (SubResourceInstance instance : subResourceRecords) {
            SubResourceRecord record = instance.getRecord();
            String recordPattern = record.getTemplateProcessor().getPatternString();
            if (record instanceof SubResourceMethodRecord && recordPattern.equals(pattern)) {
                subResourceMethods.add(instance);
            }
        }
        return subResourceMethods;
    }

    /**
     * Filter the methods that do not conform to the current request by
     * modifying the input list:
     * <ol>
     * <li>Filter all methods that do not match the http method of the request.
     * If a method does not have an http method, it passes the filter.</li>
     * <li>Filter all methods that do not match the media type of the input
     * entity. If a method does not have the @Consumes annotation, it passes the
     * filter</li>
     * <li>Filter all methods that do not match the Accept header. If a method
     * does not have the @Produces annotation, it passes the filter</li>
     * </ol>
     * The elements in the list remain in the same order
     * 
     * @param resource
     * @param methodRecords a list of method records to filter according to the
     *            request context
     * @param context the context of the current request
     * @return
     */
    private void filterDispatchMethods(ResourceInstance resource,
                                       List<? extends MethodRecord> methodRecords,
                                       RuntimeContext context) throws WebApplicationException {
        // filter by http method
        ListIterator<? extends MethodRecord> iterator = methodRecords.listIterator();
        while (iterator.hasNext()) {
            MethodRecord record = iterator.next();
            if (filterByHttpMethod(record, context)) {
                iterator.remove();
            }
        }
        if (methodRecords.size() == 0) {
            if (logger.isInfoEnabled()) {
                logger.info(Messages.getMessage("noMethodInClassSupportsHTTPMethod", resource
                    .getResourceClass().getName(), context.getRequest().getMethod()));
            }
            Set<String> httpMethods = getOptions(resource);
            ResponseBuilder builder = Response.status(HttpStatus.METHOD_NOT_ALLOWED.getCode());
            // add 'Allow' header to the response
            String allowHeader = HeaderUtils.buildOptionsHeader(httpMethods);
            builder.header(HttpHeadersEx.ALLOW, allowHeader);
            throw new WebApplicationException(builder.build());
        }

        // filter by consumes
        iterator = methodRecords.listIterator();
        while (iterator.hasNext()) {
            MethodRecord record = iterator.next();
            if (filterByConsumes(record, context)) {
                iterator.remove();
            }
        }
        if (methodRecords.size() == 0) {
            logger.info(Messages.getMessage("noMethodInClassConsumesHTTPMethod", resource //$NON-NLS-1$
                .getResourceClass().getName(), context.getHttpHeaders().getMediaType()));
            throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
        }

        // filter by produces
        iterator = methodRecords.listIterator();
        while (iterator.hasNext()) {
            MethodRecord record = iterator.next();
            if (filterByProduces(record, context)) {
                iterator.remove();
            }
        }
        if (methodRecords.size() == 0) {
            logger.info(Messages.getMessage("noMethodInClassProducesHTTPMethod", resource //$NON-NLS-1$
                .getResourceClass().getName(), context.getHttpHeaders()
                .getRequestHeader(HttpHeaders.ACCEPT)));
            throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
        }
    }

    /**
     * Checks if the method record matches the http method of the request
     * 
     * @param record the method record to check
     * @param context the context of the current request
     * @return true if the method should be filtered, false otherwise
     */
    private boolean filterByHttpMethod(MethodRecord record, RuntimeContext context) {
        String httpMethod = context.getRequest().getMethod();
        String recordHttpMethod = record.getMetadata().getHttpMethod();

        // non existing http method (with a path on the method),
        // then it's a sub-resource locator and it's ok
        if (recordHttpMethod == null) {
            return false;
        }

        // the http method is different than the request http method,
        // then the resource method should be filtered
        if (!recordHttpMethod.equals(httpMethod)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the method record matches the media type of the input entity
     * 
     * @param record the method record to check
     * @param context the context of the current request
     * @return true if the method should be filtered, false otherwise
     */
    private boolean filterByConsumes(MethodRecord record, RuntimeContext context) {
        Set<MediaType> consumedMimes = record.getMetadata().getConsumes();
        // if not specified, then treat as if consumes */*
        if (consumedMimes.size() == 0) {
            return false;
        }
        MediaType inputMediaType = context.getHttpHeaders().getMediaType();
        if (inputMediaType == null) {
            inputMediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
        }
        for (MediaType mediaType : consumedMimes) {
            if (mediaType.isCompatible(inputMediaType)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the method record matches the Accept header of the request
     * 
     * @param record the method record to check
     * @param context the context of the current request
     * @return true if the method should be filtered, false otherwise
     */
    private boolean filterByProduces(MethodRecord record, RuntimeContext context) {
        Set<MediaType> producedMimes = record.getMetadata().getProduces();
        // if not specified, then treat as if produces */*
        if (producedMimes.size() == 0) {
            return false;
        }
        List<MediaType> receivedMediaTypes = context.getHttpHeaders().getAcceptableMediaTypes();
        // if no accept media type was specified
        if (receivedMediaTypes.size() == 0) {
            return false;
        }
        List<MediaType> deniableMediaTypes = new ArrayList<MediaType>();
        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();

        for (MediaType received : receivedMediaTypes) {
            String q = received.getParameters().get("q"); //$NON-NLS-1$
            if (q != null && Double.valueOf(q).equals(0.0)) {
                deniableMediaTypes.add(received);
            } else {
                acceptableMediaTypes.add(received);
            }
        }

        l1: for (MediaType mediaType : producedMimes) {
            for (MediaType deniable : deniableMediaTypes) {
                if (MediaTypeUtils.isCompatibleNonCommutative(deniable, mediaType)) {
                    continue l1;
                }
            }
            for (MediaType acceptableMediaType : acceptableMediaTypes) {
                if (mediaType.isCompatible(acceptableMediaType)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Select the method that best matches the details of the request by
     * comparing the media types of the input entity and those specified in the
     * Accept header
     * 
     * @param methodRecords the list of methods to select the best match from
     * @param context the context of the current request
     * @return
     */
    private MethodRecord selectBestMatchingMethod(List<? extends MethodRecord> methodRecords,
                                                  RuntimeContext context) {
        HttpHeaders httpHeaders = context.getHttpHeaders();
        MediaType inputMediaType = httpHeaders.getMediaType();
        List<MediaType> acceptableMediaTypes = httpHeaders.getAcceptableMediaTypes();

        MethodRecord bestMatch = null;
        for (MethodRecord record : methodRecords) {
            if (compareMethods(record, bestMatch, inputMediaType, acceptableMediaTypes) > 0) {
                bestMatch = record;
            }
        }
        return bestMatch;
    }

    /**
     * Compare two methods in terms of "which is a better match to the request".
     * First a match to the input media type is compared, then a match to the
     * Accept header media types is compared
     * 
     * @param record1 the first method
     * @param record2 the second method
     * @param inputMediaType the input entity media type
     * @param acceptableMediaTypes the media types of the Accept header
     * @return positive integer if record1 is a better match, negative integer
     *         if record2 is a better match, 0 if they are equal in matching
     *         terms
     */
    private int compareMethods(MethodRecord record1,
                               MethodRecord record2,
                               MediaType inputMediaType,
                               List<MediaType> acceptableMediaTypes) {

        if (record1 == null && record2 == null) {
            return 0;
        }

        if (record1 != null && record2 == null) {
            return 1;
        }

        if (record1 == null && record2 != null) {
            return -1;
        }

        // compare consumes
        int res = compareMethodsConsumes(record1, record2, inputMediaType);
        if (res != 0) {
            return res;
        }

        // compare produces
        res = compareMethodsProduces(record1, record2, acceptableMediaTypes);
        if (res != 0) {
            return res;
        }

        // this is just to make the search a bit more deterministic,
        // and it should remain undocumented and application implementors
        // should not rely on this behavior (i.e. comparing the number of
        // parameters)
        return compareMethodsParameters(record1, record2);
    }

    /**
     * Compares two methods the in terms of the number of parameters
     * 
     * @param record1 the first method
     * @param record2 the second method
     * @return positive integer if record1 has more parameters, negative integer
     *         if record2 has more parameters, 0 if they are equal in number of
     *         parameters
     */
    private int compareMethodsParameters(MethodRecord record1, MethodRecord record2) {
        List<Injectable> params1 = record1.getMetadata().getFormalParameters();
        List<Injectable> params2 = record2.getMetadata().getFormalParameters();
        return params1.size() - params2.size();
    }

    /**
     * Compare two methods in terms of matching to the input media type
     * 
     * @param record1 the first method
     * @param record2 the second method
     * @param inputMediaType the input entity media type
     * @return positive integer if record1 is a better match, negative integer
     *         if record2 is a better match, 0 if they are equal in matching
     *         terms
     */
    private int compareMethodsConsumes(MethodRecord record1,
                                       MethodRecord record2,
                                       MediaType inputMediaType) {
        // get media type of metadata 1 best matching the input media type
        MediaType bestMatch1 =
            selectBestMatchingMediaType(inputMediaType, record1.getMetadata().getConsumes());
        // get media type of metadata 2 best matching the input media type
        MediaType bestMatch2 =
            selectBestMatchingMediaType(inputMediaType, record2.getMetadata().getConsumes());

        if (bestMatch1 == null && bestMatch2 == null) {
            return 0;
        }

        if (bestMatch1 != null && bestMatch2 == null) {
            return 1;
        }

        if (bestMatch1 == null && bestMatch2 != null) {
            return -1;
        }

        int retVal = MediaTypeUtils.compareTo(bestMatch1, bestMatch2);

        if (retVal != 0) {
            return retVal;
        }

        Map<String, String> inputParameters = inputMediaType.getParameters();

        Map<String, String> bestMatch1Params = bestMatch1.getParameters();
        boolean didMatchAllParamsForBestMatch1 = true;
        for (String key : bestMatch1Params.keySet()) {
            String inputValue = inputParameters.get(key);
            String value1 = bestMatch1Params.get(key);
            if (!value1.equals(inputValue)) {
                didMatchAllParamsForBestMatch1 = false;
                break;
            }
        }

        Map<String, String> bestMatch2Params = bestMatch2.getParameters();
        boolean didMatchAllParamsForBestMatch2 = true;
        for (String key : bestMatch2Params.keySet()) {
            String inputValue = inputParameters.get(key);
            String value2 = bestMatch2Params.get(key);
            if (!value2.equals(inputValue)) {
                didMatchAllParamsForBestMatch2 = false;
                break;
            }
        }

        if (didMatchAllParamsForBestMatch1 && !didMatchAllParamsForBestMatch2) {
            return 1;
        }

        if (!didMatchAllParamsForBestMatch1 && didMatchAllParamsForBestMatch2) {
            return -1;
        }

        if (didMatchAllParamsForBestMatch1 && didMatchAllParamsForBestMatch2) {
            int size1 = bestMatch1Params.size();
            int size2 = bestMatch2Params.size();
            if (size1 > size2) {
                return 1;
            } else if (size2 > size1) {
                return -1;
            }
        }

        return 0;
    }

    /**
     * Compare two methods in terms of matching to the Accept header media types
     * 
     * @param record1 the first method
     * @param record2 the second method
     * @param acceptableMediaTypes the media types of the Accept header
     * @return positive integer if record1 is a better match, negative integer
     *         if record2 is a better match, 0 if they are equal in matching
     *         terms
     */
    private int compareMethodsProduces(MethodRecord record1,
                                       MethodRecord record2,
                                       List<MediaType> acceptableMediaTypes) {

        MediaType bestMatch1 = null;
        MediaType bestMatch2 = null;

        // the acceptMediaTypes list is already sorted according to preference
        // of media types,
        // so we need to stop with the first media type that has a match
        for (MediaType acceptableMediaType : acceptableMediaTypes) {
            // get media type of metadata 1 best matching the current acceptable
            // media type
            bestMatch1 =
                selectBestMatchingMediaType(acceptableMediaType, record1.getMetadata()
                    .getProduces());
            // get media type of metadata 2 best matching the current acceptable
            // media type
            bestMatch2 =
                selectBestMatchingMediaType(acceptableMediaType, record2.getMetadata()
                    .getProduces());
            // if either of them returned a match, it's enough for a comparison
            if (bestMatch1 != null || bestMatch2 != null) {
                break;
            }
        }

        if (bestMatch1 == null && bestMatch2 == null) {
            return 0;
        }

        if (bestMatch1 != null && bestMatch2 == null) {
            return 1;
        }

        if (bestMatch1 == null && bestMatch2 != null) {
            return -1;
        }

        return MediaTypeUtils.compareTo(bestMatch1, bestMatch2);
    }

    /**
     * Select the media type from the list of media types that best matches the
     * specified media type
     * 
     * @param mediaType the media type to match against
     * @param mediaTypes the list of media types to select the best match from
     * @return the best matching media type from the list
     */
    private MediaType selectBestMatchingMediaType(MediaType mediaType, Set<MediaType> mediaTypes) {
        MediaType bestMatch = null;
        for (MediaType mt : mediaTypes) {
            if (!mt.isCompatible(mediaType)) {
                continue;
            }
            if (bestMatch == null || MediaTypeUtils.compareTo(mt, bestMatch) > 0) {
                bestMatch = mt;
            }
        }
        if (bestMatch == null) {
            return null;
        }
        return bestMatch;
    }

}
