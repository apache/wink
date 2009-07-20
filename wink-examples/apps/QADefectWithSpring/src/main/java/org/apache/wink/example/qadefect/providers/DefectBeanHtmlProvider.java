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
package org.apache.wink.example.qadefect.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.apache.wink.example.qadefect.legacy.DefectBean;
import org.apache.wink.server.internal.providers.entity.html.HtmlDescriptor;

@Provider
@Produces(MediaType.TEXT_HTML)
public class DefectBeanHtmlProvider extends AbstractDescriptorWriter<DefectBean, HtmlDescriptor> {

    public static final String CUSTOMIZED_JSP_PATH =
                                                       "/HtmlCustomizedRepresentation/customizedHtmlEntry.jsp";
    public static final String CUSTOMIZED_JSP_ATTR = "DefectAssetAttr";

    @Override
    public HtmlDescriptor getDescriptor(DefectBean bean) {
        return new HtmlDescriptor(bean, CUSTOMIZED_JSP_PATH, CUSTOMIZED_JSP_ATTR);
    }

    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return type == DefectBean.class;
    }

}
