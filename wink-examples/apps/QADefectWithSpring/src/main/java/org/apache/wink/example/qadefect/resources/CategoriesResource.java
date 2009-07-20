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
package org.apache.wink.example.qadefect.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.wink.common.AbstractDynamicResource;
import org.apache.wink.common.categories.Categories;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.atom.AtomCategory;

/**
 * This Resource holds all categories that are supported by QADefects
 * Application Each Category is identified by "Category Name"
 */

public class CategoriesResource extends AbstractDynamicResource {

    public static final String StatusCN        = "status";
    public static final String SeverityCN      = "severity";
    public static final String AllCatgoriesCN  = "all";
    public static final String CategoryParamCN = "category_name";
    public static final String CategoriesURL   = "categories";

    public static final String CategoryURL     = "/" + CategoriesURL + "/{" + CategoryParamCN + "}";

    /**
     * This method will be invoked to get Categories Document
     * 
     * @param categoryName
     * @return CategoriesDocumentResource
     */
    @GET
    @Produces(MediaTypeUtils.ATOM_CATEGORIES_DOCUMENT)
    public Categories getCategoriesDocument(@PathParam(CategoryParamCN) String categoryName) {

        Categories cats = null;
        if (categoryName.equals(SeverityCN)) {
            cats = buildSeverityCategoryDocument();
        } else if (categoryName.equals(StatusCN)) {
            cats = buildStatusCategoriesDocument();
        } else if (categoryName.equals(AllCatgoriesCN)) {
            cats = buildCompleteCategoriesDocument();
        }
        return cats;
    }

    private Categories buildCompleteCategoriesDocument() {
        Categories severityCategoryDocument = buildSeverityCategoryDocument();
        Categories statusCategoriesDocument = buildStatusCategoriesDocument();

        List<AtomCategory> categories = severityCategoryDocument.getCategories();
        List<AtomCategory> categories2 = statusCategoriesDocument.getCategories();
        List<AtomCategory> categoriesAll = new ArrayList<AtomCategory>();
        categoriesAll.addAll(categories);
        categoriesAll.addAll(categories2);
        Categories allCategories = new Categories(categoriesAll);
        allCategories.setScheme("urn:com:hp:qadefects:categories");
        return allCategories;
    }

    public static Categories buildSeverityCategoryDocument() {
        Categories cats = new Categories();
        cats.setScheme("urn:com:hp:qadefects:categories:severity");
        cats.setFixed(true);

        AtomCategory severityCritical = new AtomCategory();
        severityCritical.setLabel("critical");
        severityCritical.setScheme("urn:com:hp:qadefects:categories:severity");
        severityCritical.setTerm("1-critical");
        cats.addCategory(severityCritical);

        AtomCategory severityHigh = new AtomCategory();
        severityHigh.setLabel("high");
        severityHigh.setScheme("urn:com:hp:qadefects:categories:severity");
        severityHigh.setTerm("2-high");
        cats.addCategory(severityHigh);

        AtomCategory severityMidium = new AtomCategory();
        severityMidium.setLabel("medium");
        severityMidium.setScheme("urn:com:hp:qadefects:categories:severity");
        severityMidium.setTerm("3-medium");
        cats.addCategory(severityMidium);

        AtomCategory severityMinor = new AtomCategory();
        severityMinor.setLabel("minor");
        severityMinor.setScheme("urn:com:hp:qadefects:categories:severity");
        severityMinor.setTerm("4-minor");
        cats.addCategory(severityMinor);

        return cats;
    }

    public static Categories buildStatusCategoriesDocument() {
        Categories statusCategories = new Categories();
        statusCategories.setScheme("urn:com:hp:qadefects:categories:status");

        AtomCategory assigned = new AtomCategory();
        assigned.setLabel("Assigned");
        assigned.setScheme("urn:com:hp:qadefects:categories:status");
        assigned.setTerm("Assigned");
        statusCategories.addCategory(assigned);

        AtomCategory statusFixed = new AtomCategory();
        statusFixed.setLabel("Fixed");
        statusFixed.setScheme("urn:com:hp:qadefects:categories:status");
        statusFixed.setTerm("Fixed");
        statusCategories.addCategory(statusFixed);

        AtomCategory statusNew = new AtomCategory();
        statusNew.setLabel("New");
        statusNew.setScheme("urn:com:hp:qadefects:categories:status");
        statusNew.setTerm("New");
        statusCategories.addCategory(statusNew);

        AtomCategory statusRejected = new AtomCategory();
        statusRejected.setLabel("Rejected");
        statusRejected.setScheme("urn:com:hp:qadefects:categories:status");
        statusRejected.setTerm("Rejected");
        statusCategories.addCategory(statusRejected);

        return statusCategories;
    }
}
