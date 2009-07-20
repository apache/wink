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

package org.apache.wink.common.categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.model.atom.AtomCategory;

/**
 * The Atom Publishing Protocol introduces the notion of a "Category Document"
 * and the app:categories element. These are used to provide a listing of
 * atom:Category elements that may be used with the members of an Atom
 * Publishing Protocol collection.
 */
public class Categories {

    /**
     * The app:categories "scheme" attribute is an IRI that identifies a
     * categorization scheme. <h4>NOTE</h4> An atom:category child element that
     * has no "scheme" attribute inherits the attribute from its app:categories
     * parent.
     */
    private String                         scheme;

    /**
     * The app:categories element MAY contain an "href" attribute, whose value
     * MUST be an IRI reference identifying a Category Document
     */
    private String                         href;

    /**
     * The app:categories element can contain a "fixed" attribute, with a value
     * of either "yes" or "no", indicating whether the list of categories is a
     * fixed or an open set. The absence of the "fixed" attribute is equivalent
     * to the presence of a "fixed" attribute with a value of "no".
     */
    private boolean                        isFixed;

    /**
     * List of atom:category elements. <h4>NOTE</h4> An app:categories element
     * can contain zero or more atom:category elements from the Atom Syndication
     * Format [RFC4287] namespace ("http://www.w3.org/2005/Atom"). An
     * atom:category child element that has no "scheme" attribute inherits the
     * attribute from its app:categories parent. An atom:category child element
     * with an existing "scheme" attribute does not inherit the "scheme" value
     * of its app:categories parent element.
     */
    private List<AtomCategory>             categories;

    /**
     * Class that will provide Categories Document
     */
    private Class<?>                       handlingClass;

    /**
     * Instance that will provide Categories Document
     */
    private Object                         handlingInstance;

    /**
     * Uri parameters for handling Resource
     */
    private MultivaluedMap<String, String> templateParams;

    /**
     * Constructor
     * 
     * @param categories
     */
    public Categories(List<AtomCategory> categories) {
        this.categories = categories;
        isFixed = false;
    }

    /**
     * Constructor
     * 
     * @param categories
     */
    public Categories() {
        this.categories = new ArrayList<AtomCategory>();
        isFixed = false;
    }

    /**
     * When contained within an app:collection element, the app:categories
     * element can have an href attribute whose value MUST point to an Atompub
     * Categories Document.
     * 
     * @return The href attribute value
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href attribute.
     * 
     * @param href The location of an Atompub Categories Document
     */
    public Categories setHref(String href) {
        this.href = href;

        // Clear previous setting (in case other setHref() was called)
        this.handlingClass = null;
        this.templateParams = null;
        this.handlingInstance = null;
        return this;
    }

    /**
     * Set Resource class that will generate Categories Document
     * 
     * @param handlingClass Resource Class
     * @param templateParams Map of URI variable names and their values
     */
    public void setHref(Class<?> handlingClass, MultivaluedMap<String, String> templateParams) {
        this.handlingClass = handlingClass;
        this.templateParams = templateParams;

        // Clear previous setting (in case other setHref() was called)
        this.handlingInstance = null;
        this.href = null;
    }

    /**
     * Set Resource that will generate Categories Document
     * 
     * @param handlingInstance Resource instance
     * @param templateParams Map of URI variable names and their values
     */
    public void setHref(Object handlingInstance, MultivaluedMap<String, String> templateParams) {
        this.handlingInstance = handlingInstance;
        this.templateParams = templateParams;

        // Clear previous setting (in case other setHref() was called)
        this.handlingClass = null;
        this.href = null;
    }

    /**
     * If an app:categories element is marked as fixed, then the set of
     * atom:Category elements is considered to be a closed set. That is, Atom
     * Publishing Protocol clients SHOULD only use the atom:Category elements
     * listed. The default is false (fixed="no")
     * 
     * @return True if the categories listing is fixed
     */
    public boolean isFixed() {
        return isFixed;
    }

    /**
     * Sets whether or not this is a fixed listing of categories. If set to
     * false, the fixed attribute will be removed from the app:categories
     * element.
     * 
     * @param fixed True if the app:categories listing is fixed
     */
    public Categories setFixed(boolean fixed) {
        isFixed = fixed;
        return this;
    }

    /**
     * The app:categories element may specify a default scheme attribute for
     * listed atom:Category elements that do not have their own scheme
     * attribute.
     * 
     * @return The scheme String
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the default scheme for this listing of categories
     * 
     * @param scheme The default scheme used for this listing of categories
     */
    public Categories setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * Lists the complete set of categories
     * 
     * @return This app:categories listing of atom:Category elements
     */
    public List<AtomCategory> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    /**
     * Lists the complete set of categories that use the specified scheme
     * 
     * @param scheme The String of an atom:Category scheme
     * @return A listing of atom:Category elements that use the specified scheme
     */
    public List<AtomCategory> getCategories(String scheme) {
        List<AtomCategory> newcats = new ArrayList<AtomCategory>();
        for (AtomCategory cat : categories) {
            if (cat.getScheme().endsWith(scheme)) {
                newcats.add(cat);
            }
        }
        return Collections.unmodifiableList(newcats);
    }

    /**
     * Add an atom:Category to the listing
     * 
     * @param Category The atom:Category to add to the listing
     */
    public Categories addCategory(AtomCategory category) {
        categories.add(category);
        return this;
    }

    /**
     * Create and add an atom:Category to the listing
     * 
     * @param term The string term
     * @return The newly created atom:Category
     */
    public AtomCategory addCategory(String term) {
        AtomCategory newCategory = new AtomCategory();
        newCategory.setTerm(term);
        categories.add(newCategory);
        return newCategory;
    }

    /**
     * Create an add an atom:Category to the listing
     * 
     * @param scheme The scheme String for the newly created Category
     * @param term The string term
     * @param label The human readable label for the Category
     * @return The newly created atom:Category
     */
    public AtomCategory addCategory(String scheme, String term, String label) {
        AtomCategory newCategory = new AtomCategory();
        newCategory.setScheme(scheme);
        newCategory.setTerm(term);
        newCategory.setLabel(label);
        categories.add(newCategory);
        return null;
    }

    /**
     * Returns true if this app:categories listing contains a Category with the
     * specified term
     * 
     * @param term The term to look for
     * @return True if the term is found
     */
    public boolean contains(String term) {
        return contains(term, null);
    }

    /**
     * Returns true if this app:categories listing contains a Category with the
     * specified term and scheme
     * 
     * @param term The term to look for
     * @param scheme The String scheme
     * @return True if the term and scheme are found
     */
    public boolean contains(String term, String scheme) {
        String catscheme = getScheme();
        String searchScheme = (scheme != null) ? scheme : catscheme;
        for (AtomCategory category : categories) {
            String t = category.getTerm();
            String s = (category.getScheme() != null) ? category.getScheme() : catscheme;
            if (t.equals(term) && ((searchScheme != null) ? searchScheme.equals(s) : s == null))
                return true;
        }
        return false;
    }

    /**
     * Returns true if the href attribute is set
     */
    public boolean isOutOfLine() {

        boolean answer = false;
        if (getHref() != null || handlingClass != null || handlingInstance != null) {
            answer = true;
        } else {
            answer = false;
        }
        return answer;
    }

    /**
     * Get Categories Document handling Resource class
     * 
     * @return Resource class
     */
    public Class<?> getHandlingClass() {
        return handlingClass;
    }

    /**
     * Set a list of categories
     * 
     * @param categories
     */
    public void setCategories(List<AtomCategory> categories) {
        this.categories = categories;
    }

    /**
     * Get handling Bean for Categories Document
     * 
     * @return handlingBean
     */
    public Object getHandlingInstance() {
        return handlingInstance;
    }

    /**
     * Get Uri Template Variables
     * 
     * @return templateParams
     */
    public MultivaluedMap<String, String> getTemplateParameters() {
        return templateParams;
    }

    /**
     * Set Uri Template Variables
     * 
     * @param templateParams
     */
    public void setTemplateParameters(MultivaluedMap<String, String> templateParams) {
        this.templateParams = templateParams;
    }
}
