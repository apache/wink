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
package org.apache.wink.server.internal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.model.admin.MatrixParameters;
import org.apache.wink.common.internal.model.admin.Parameter;
import org.apache.wink.common.internal.model.admin.QueryParameters;
import org.apache.wink.common.internal.model.admin.Registry;
import org.apache.wink.common.internal.model.admin.Resource;
import org.apache.wink.common.internal.model.admin.Resources;
import org.apache.wink.common.internal.model.admin.SubResource;
import org.apache.wink.common.internal.model.admin.SubResources;
import org.apache.wink.common.internal.registry.BoundInjectable;
import org.apache.wink.common.internal.registry.Injectable;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor;
import org.apache.wink.common.internal.utils.JAXBUtils;
import org.apache.wink.server.internal.RequestProcessor;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.ResourceRegistry;

public class AdminServlet extends AbstractRestServlet {

    private static final long                                                      serialVersionUID       =
                                                                                                              -5972412568762942420L;
    private static final String                                                    APPLICATION_XML        =
                                                                                                              "application/xml";    //$NON-NLS-1$
    private static final String                                                    DOCUMENT_TYPE          =
                                                                                                              "doc";                //$NON-NLS-1$
    private static final String                                                    DOCUMENT_TYPE_REGISTRY =
                                                                                                              "registry";           //$NON-NLS-1$
    private static final String                                                    DOCUMENT_TYPE_RESOURCE =
                                                                                                              "resources";          //$NON-NLS-1$
    private static final String                                                    SLASH                  =
                                                                                                              "/";                  //$NON-NLS-1$

    private static final JAXBContext                                               resourceCtx;
    private static final org.apache.wink.common.internal.model.admin.ObjectFactory resourcesObjectFactory;

    static {
        resourcesObjectFactory = new org.apache.wink.common.internal.model.admin.ObjectFactory();
        try {
            resourceCtx =
                JAXBContext
                    .newInstance(org.apache.wink.common.internal.model.admin.ObjectFactory.class
                        .getPackage().getName());
        } catch (JAXBException e) {
            throw new RuntimeException(Messages
                .getMessage("adminServletFailCreateJAXBForAdminServlet"), e); //$NON-NLS-1$
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
        ServletException {

        RequestProcessor requestProcessor = getRequestProcessor();
        if (requestProcessor == null) {
            throw new ServletException(Messages
                .getMessage("adminServletRequestProcessorInitBeforeAdmin")); //$NON-NLS-1$
        }
        ResourceRegistry registry = requestProcessor.getConfiguration().getResourceRegistry();

        String[] parameterValues = request.getParameterValues(DOCUMENT_TYPE);
        if (parameterValues == null || parameterValues.length == 0) {
            buildAdminHome(response);
            return;
        }
        if (parameterValues[0].equals(DOCUMENT_TYPE_REGISTRY)) {
            buildRegistryDocument(request, response, registry);
        } else if (parameterValues[0].equals(DOCUMENT_TYPE_RESOURCE)) {
            buildResourcesDocument(request, response, registry);
        } else {
            buildAdminHome(response);
            return;
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        doGet(request, response);
    }

    private void buildRegistryDocument(HttpServletRequest request,
                                       HttpServletResponse response,
                                       ResourceRegistry registry) throws ServletException,
        IOException {

        Registry xmlRgistry = resourcesObjectFactory.createRegistry();
        org.apache.wink.common.internal.model.admin.Resources resources =
            resourcesObjectFactory.createResources();
        List<ResourceRecord> records = registry.getRecords();

        for (ResourceRecord record : records) {

            // Add current resource
            Resource xmLResource = buildResource(record);
            xmLResource.setPriority(new BigDecimal(record.getPriority()));
            xmLResource.setClassName(record.getMetadata().getResourceClass().getCanonicalName());

            // Add sub-resource locators & methods
            addSubRrcMethodsAndLocators(xmLResource, record);

            resources.getResource().add(xmLResource);

        }

        // Write Document
        xmlRgistry.setResources(resources);
        writeDocument(response, xmlRgistry);
        return;
    }

    private void addSubRrcMethodsAndLocators(Resource xmLResource, ResourceRecord record) {

        List<MethodMetadata> subResourceLocators = record.getMetadata().getSubResourceLocators();
        List<MethodMetadata> subResourceMethods = record.getMetadata().getSubResourceMethods();

        SubResources subResources = resourcesObjectFactory.createSubResources();
        buildSubReasource(record, subResourceLocators, subResources, true);
        buildSubReasource(record, subResourceMethods, subResources, false);
        xmLResource.setSubResources(subResources);
    }

    private void buildSubReasource(ResourceRecord record,
                                   List<MethodMetadata> subResourceLocators,
                                   SubResources subResources,
                                   boolean isLocator) {
        for (MethodMetadata subResourceLocator : subResourceLocators) {
            SubResource subResource = resourcesObjectFactory.createSubResource();
            UriTemplateProcessor uriTemplate = record.getTemplateProcessor();
            subResource.setUri(SLASH + uriTemplate.getTemplate() + subResourceLocator.getPath());
            if (isLocator) {
                subResource.setType("Locator"); //$NON-NLS-1$
            } else {
                subResource.setType("Method"); //$NON-NLS-1$
                subResource.setMethod(subResourceLocator.getHttpMethod().toString());
            }

            org.apache.wink.common.internal.model.admin.AcceptMediaTypes acceptMediaTypes =
                resourcesObjectFactory.createAcceptMediaTypes();
            buildCunsumeMimeTypes(subResourceLocator, acceptMediaTypes);
            subResource.setAcceptMediaTypes(acceptMediaTypes);

            // Add produce mime types
            org.apache.wink.common.internal.model.admin.ProducedMediaTypes producedMediaTypes =
                resourcesObjectFactory.createProducedMediaTypes();
            buildProduceMimeTypes(subResourceLocator, producedMediaTypes);
            subResource.setProducedMediaTypes(producedMediaTypes);

            // Add Query Parameters
            QueryParameters qParams = resourcesObjectFactory.createQueryParameters();
            buildQueryParams(subResourceLocator, qParams);
            subResource.setQueryParameters(qParams);

            // Add Matrinx Parameters
            MatrixParameters mParams = resourcesObjectFactory.createMatrixParameters();
            buildMatrixParams(subResourceLocator, mParams);
            subResource.setMatrixParameters(mParams);

            subResources.getSubResource().add(subResource);
        }
    }

    /**
     * Build Resources Document. This document will contain resource oriented
     * information about all registered resources
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param resourceRegistry TODO
     * @throws IOException
     * @throws ServletException
     */
    private void buildResourcesDocument(HttpServletRequest request,
                                        HttpServletResponse response,
                                        ResourceRegistry resourceRegistry) throws IOException,
        ServletException {
        List<ResourceRecord> records = resourceRegistry.getRecords();

        org.apache.wink.common.internal.model.admin.Resources resources =
            resourcesObjectFactory.createResources();

        for (ResourceRecord record : records) {

            // Add current resource
            resources.getResource().add(buildResource(record));

            // Add sub-resource locators & methods
            buildSubRrcMethodsAndLocatorsResources(resources, record);

        }

        // Write Document
        writeDocument(response, resources);
        return;

    }

    private void buildSubRrcMethodsAndLocatorsResources(Resources resources, ResourceRecord record) {

        List<MethodMetadata> subResourceLocators = record.getMetadata().getSubResourceLocators();
        List<MethodMetadata> subResourceMethods = record.getMetadata().getSubResourceMethods();

        Map<String, Resource> uri2ResourceMap = new HashMap<String, Resource>();

        for (MethodMetadata subResourceLocator : subResourceLocators) {

            UriTemplateProcessor uriTemplate = record.getTemplateProcessor();
            String uri = SLASH + uriTemplate.getTemplate() + subResourceLocator.getPath();
            org.apache.wink.common.internal.model.admin.Methods xmlSRL =
                createXMLSubResourceLocatorMethod(subResourceLocator);
            addResourceMethods(uri2ResourceMap, uri, xmlSRL);
        }

        // Add all Sub Resource Locators as resources
        resources.getResource().addAll(uri2ResourceMap.values());

        uri2ResourceMap.clear();
        for (MethodMetadata subResourceMethod : subResourceMethods) {
            UriTemplateProcessor uriTemplate = record.getTemplateProcessor();
            String uri = SLASH + uriTemplate.getTemplate() + SLASH + subResourceMethod.getPath();
            org.apache.wink.common.internal.model.admin.Methods xmlSRM =
                createXMLSubResourceMethod(subResourceMethod);
            addResourceMethods(uri2ResourceMap, uri, xmlSRM);
        }

        // Add all Sub Resource Methods as resources
        resources.getResource().addAll(uri2ResourceMap.values());
    }

    private void addResourceMethods(Map<String, Resource> uri2ResourceMap,
                                    String uri,
                                    org.apache.wink.common.internal.model.admin.Methods xmlSRL) {

        if (uri2ResourceMap.get(uri) != null) {
            Resource r = uri2ResourceMap.get(uri);
            r.getMethods().getMethod().addAll(xmlSRL.getMethod());
        } else {
            Resource newResource = resourcesObjectFactory.createResource();
            newResource.setUri(uri);
            newResource.setMethods(xmlSRL);
            uri2ResourceMap.put(uri, newResource);
        }
    }

    private org.apache.wink.common.internal.model.admin.Methods createXMLSubResourceLocatorMethod(MethodMetadata subResourceLocator) {

        org.apache.wink.common.internal.model.admin.Methods methods =
            resourcesObjectFactory.createMethods();
        org.apache.wink.common.internal.model.admin.Methods.Method httpMethod =
            resourcesObjectFactory.createMethodsMethod();

        httpMethod.setName("Dynamic"); //$NON-NLS-1$
        // Add consume mime types
        org.apache.wink.common.internal.model.admin.AcceptMediaTypes acceptMediaTypes =
            resourcesObjectFactory.createAcceptMediaTypes();
        buildCunsumeMimeTypes(subResourceLocator, acceptMediaTypes);

        // Add produce mime types
        org.apache.wink.common.internal.model.admin.ProducedMediaTypes producedMediaTypes =
            resourcesObjectFactory.createProducedMediaTypes();
        buildProduceMimeTypes(subResourceLocator, producedMediaTypes);

        // Add Query Parameters
        QueryParameters qParams = resourcesObjectFactory.createQueryParameters();
        buildQueryParams(subResourceLocator, qParams);

        // Add Matrinx Parameters
        MatrixParameters mParams = resourcesObjectFactory.createMatrixParameters();
        buildMatrixParams(subResourceLocator, mParams);

        httpMethod.setQueryParameters(qParams);
        httpMethod.setProducedMediaTypes(producedMediaTypes);
        httpMethod.setAcceptMediaTypes(acceptMediaTypes);
        httpMethod.setMatrixParameters(mParams);
        methods.getMethod().add(httpMethod);
        return methods;
    }

    private org.apache.wink.common.internal.model.admin.Methods createXMLSubResourceMethod(MethodMetadata subResourceMethod) {
        org.apache.wink.common.internal.model.admin.Methods methods =
            resourcesObjectFactory.createMethods();
        List<MethodMetadata> methodRecord = new LinkedList<MethodMetadata>();
        methodRecord.add(subResourceMethod);
        addResourceMethods(methodRecord, methods);
        return methods;
    }

    private Resource buildResource(ResourceRecord record) {

        Resource newResource = resourcesObjectFactory.createResource();

        // Add dispatched URIs
        UriTemplateProcessor uriTemplate = record.getTemplateProcessor();
        newResource.setUri(SLASH + uriTemplate.getTemplate());

        // Add Collection/Workspace data
        newResource.setWorkspace(record.getMetadata().getWorkspaceName());
        newResource.setCollection(record.getMetadata().getCollectionTitle());

        // Add supported HTTP methods
        List<MethodMetadata> methodRecords = new LinkedList<MethodMetadata>();
        methodRecords.addAll(record.getMetadata().getResourceMethods());
        org.apache.wink.common.internal.model.admin.Methods methods =
            resourcesObjectFactory.createMethods();
        addResourceMethods(methodRecords, methods);
        newResource.setMethods(methods);

        return newResource;

    }

    /**
     * Add Http methods supported by resource. Http methods are grouped by
     * method name
     * 
     * @param methodRecords
     * @param methods2xml
     */
    private void addResourceMethods(List<MethodMetadata> methodMD,
                                    org.apache.wink.common.internal.model.admin.Methods methods2xml) {

        for (MethodMetadata methodMetadata : methodMD) {
            org.apache.wink.common.internal.model.admin.Methods.Method httpMethod =
                resourcesObjectFactory.createMethodsMethod();
            httpMethod.setName(methodMetadata.getHttpMethod().toString());

            // Add consume mime types
            org.apache.wink.common.internal.model.admin.AcceptMediaTypes acceptMediaTypes =
                resourcesObjectFactory.createAcceptMediaTypes();
            buildCunsumeMimeTypes(methodMetadata, acceptMediaTypes);

            // Add produce mime types
            org.apache.wink.common.internal.model.admin.ProducedMediaTypes producedMediaTypes =
                resourcesObjectFactory.createProducedMediaTypes();
            buildProduceMimeTypes(methodMetadata, producedMediaTypes);

            // Add Query Parameters
            QueryParameters qParams = resourcesObjectFactory.createQueryParameters();
            buildQueryParams(methodMetadata, qParams);

            // Add Matrinx Parameters
            MatrixParameters mParams = resourcesObjectFactory.createMatrixParameters();
            buildMatrixParams(methodMetadata, mParams);

            httpMethod.setQueryParameters(qParams);
            httpMethod.setProducedMediaTypes(producedMediaTypes);
            httpMethod.setAcceptMediaTypes(acceptMediaTypes);
            httpMethod.setMatrixParameters(mParams);
            methods2xml.getMethod().add(httpMethod);
        }
    }

    private void buildMatrixParams(MethodMetadata methodMetadata, MatrixParameters params) {
        List<Injectable> formalParameters = methodMetadata.getFormalParameters();
        for (Injectable var : formalParameters) {
            if (var.getParamType() == Injectable.ParamType.MATRIX) {
                Parameter param = resourcesObjectFactory.createParameter();
                param.setValue(((BoundInjectable)var).getName());
                params.getParameter().add(param);
            }
        }
    }

    /**
     * Write XmlObject to the wire
     * 
     * @param response HttpServletResponse
     * @param document XmlObject
     * @throws IOException
     * @throws IOException
     */
    private void writeDocument(HttpServletResponse response, Object jaxbObject)
        throws ServletException, IOException {

        PrintWriter writer = response.getWriter();
        // Write Document
        response.setContentType(APPLICATION_XML);

        try {
            Marshaller marshaller = JAXBUtils.createMarshaller(resourceCtx);
            marshaller.marshal(jaxbObject, writer);
        } catch (JAXBException e) {
            throw new ServletException(Messages.getMessage("adminServletFailMarshalObject", //$NON-NLS-1$
                                                           jaxbObject.getClass().getName()), e);

        }

        response.flushBuffer();
        writer.close();

    }

    /**
     * Build Admin Home Page
     * 
     * @param response
     * @throws IOException
     */
    private void buildAdminHome(HttpServletResponse response) throws IOException {
        // Set the status code before writing content to the stream
        // per the servlet specification.
        response.setStatus(HttpStatus.BAD_REQUEST.getCode());
        
	PrintWriter writer = response.getWriter();
        writer
            .write("<html>\r\n" + "<head>\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                + "<title>Admin Console</title>\r\n" //$NON-NLS-1$
                + "<style type=\"text/css\" media=\"all\">  h2 {  padding: 4px 4px 4px 24px;  color: #333333;  background-color: #D8D8D8;  font-weight: bold;  font-size: 16px;} h1 {  padding: 4px 4px 4px 24px;  color: #F8F8F8;  background-color: #909090;  font-weight: bold;  font-size: 24px;}    </style>" //$NON-NLS-1$
                + "</head>\r\n" //$NON-NLS-1$
                + "<body>\r\n" //$NON-NLS-1$
                + "<form name=\"AdministrationPage\"  method=\"POST\">\r\n" //$NON-NLS-1$
                + "<div align=\"left\">\r\n" //$NON-NLS-1$
                + "</br>\r\n" //$NON-NLS-1$
                + "<h1>Wink Admin Console</h1>\r\n" //$NON-NLS-1$
                + "</br>\r\n" //$NON-NLS-1$
                + "</div>\r\n" //$NON-NLS-1$
                + "<h2>\r\n" //$NON-NLS-1$
                + "<a href=\"?doc=" //$NON-NLS-1$
                + DOCUMENT_TYPE_RESOURCE
                + "\"> Application resources xml view</a>&#45&#62\r\n" //$NON-NLS-1$
                + "</h2>\r\n" //$NON-NLS-1$
                + "<h2>\r\n" //$NON-NLS-1$
                + "<a href=\"?doc=" //$NON-NLS-1$
                + DOCUMENT_TYPE_REGISTRY
                + "\"> Wink Resource registry xml view</a>&#45&#62\r\n" //$NON-NLS-1$
                + "</h2>\r\n" //$NON-NLS-1$
                + "</form>\r\n" //$NON-NLS-1$
                + "</body>\r\n" //$NON-NLS-1$
                + "</html>"); //$NON-NLS-1$
        writer.close();
        return;
    }

    private void buildQueryParams(MethodMetadata methodMetadata, QueryParameters xmlQueryVariables) {
        List<Injectable> formalParameters = methodMetadata.getFormalParameters();
        for (Injectable var : formalParameters) {
            if (var.getParamType() == Injectable.ParamType.QUERY) {
                Parameter param = resourcesObjectFactory.createParameter();
                param.setValue(((BoundInjectable)var).getName());
                xmlQueryVariables.getParameter().add(param);
            }
        }
    }

    private void buildProduceMimeTypes(MethodMetadata methodMetadata,
                                       org.apache.wink.common.internal.model.admin.ProducedMediaTypes xmlProducedMediaTypes) {
        Set<MediaType> producedMime = methodMetadata.getProduces();
        for (MediaType mediaType : producedMime) {
            xmlProducedMediaTypes.getProducedMediaType().add(mediaType.getType() + SLASH
                + mediaType.getSubtype());
        }
    }

    private void buildCunsumeMimeTypes(MethodMetadata methodMetadata,
                                       org.apache.wink.common.internal.model.admin.AcceptMediaTypes xmlAcceptMediaTypes) {
        Set<MediaType> consumedMime = methodMetadata.getConsumes();
        for (MediaType mediaType : consumedMime) {
            xmlAcceptMediaTypes.getAcceptMediaType().add(mediaType.getType() + SLASH
                + mediaType.getSubtype());
        }
    }
}
