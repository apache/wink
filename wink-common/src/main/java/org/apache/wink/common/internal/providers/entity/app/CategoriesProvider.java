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

package org.apache.wink.common.internal.providers.entity.app;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.apache.wink.common.annotations.Scope;
import org.apache.wink.common.annotations.Scope.ScopeType;
import org.apache.wink.common.categories.Categories;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.app.AppCategories;
import org.apache.wink.common.model.app.AppYesNo;
import org.apache.wink.common.model.atom.AtomCategory;

/**
 * Representation of Atom Category Document. Category Document is a document
 * that describes the categories allowed in a Collection. This provider is used
 * when collection resource exposes its categories by implementing
 * CollectionCategories interface
 */
@Provider
@Scope(ScopeType.PROTOTYPE)
@Produces(MediaTypeUtils.ATOM_CATEGORIES_DOCUMENT)
public class CategoriesProvider implements MessageBodyWriter<Categories> {

    @Context
    Providers                        providers;

    // AppCategories serializer
    MessageBodyWriter<AppCategories> appCatProvider;

    public long getSize(Categories t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {

        if (type != Categories.class) {
            return false;
        }

        appCatProvider = providers.getMessageBodyWriter(AppCategories.class, null, null, mediaType);

        if (appCatProvider == null || type != Categories.class) {
            return false;
        }

        return true;

    }

    public void writeTo(Categories categories,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        AppCategories catDoc = buildAppCatDoc(categories);
        appCatProvider.writeTo(catDoc, AppCategories.class, AppCategories.class, annotations, mediaType, httpHeaders, entityStream);
    }

    private AppCategories buildAppCatDoc(Categories cats) {

        AppCategories appCategories = new AppCategories();
        List<AtomCategory> categoryList = appCategories.getCategory();

        List<AtomCategory> categories = cats.getCategories();

        for (AtomCategory cat : categories) {
            AtomCategory atomCategory = new AtomCategory();

            String catLabel = cat.getLabel();
            if (catLabel != null) {
                atomCategory.setLabel(catLabel);
            }

            String catScheme = cat.getScheme();
            if (catScheme != null) {
                atomCategory.setScheme(catScheme);
            }

            String catTerm = cat.getTerm();
            if (catTerm != null) {
                atomCategory.setTerm(catTerm);
            }
            categoryList.add(atomCategory);
        }

        String catsScheme = cats.getScheme();
        boolean fixed = cats.isFixed();

        if (catsScheme != null) {
            appCategories.setScheme(catsScheme);
        }
        if (fixed == true) {
            appCategories.setFixed(AppYesNo.YES);
        }

        return appCategories;
    }

}
