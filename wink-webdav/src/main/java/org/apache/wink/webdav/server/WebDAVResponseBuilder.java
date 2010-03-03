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

package org.apache.wink.webdav.server;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.model.synd.SyndBase;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.common.model.synd.SyndLink;
import org.apache.wink.webdav.model.Allprop;
import org.apache.wink.webdav.model.Collection;
import org.apache.wink.webdav.model.Creationdate;
import org.apache.wink.webdav.model.Displayname;
import org.apache.wink.webdav.model.Error;
import org.apache.wink.webdav.model.Getcontentlanguage;
import org.apache.wink.webdav.model.Getcontentlength;
import org.apache.wink.webdav.model.Getcontenttype;
import org.apache.wink.webdav.model.Getetag;
import org.apache.wink.webdav.model.Getlastmodified;
import org.apache.wink.webdav.model.Lockdiscovery;
import org.apache.wink.webdav.model.Multistatus;
import org.apache.wink.webdav.model.Prop;
import org.apache.wink.webdav.model.Propfind;
import org.apache.wink.webdav.model.Propstat;
import org.apache.wink.webdav.model.Resourcetype;
import org.apache.wink.webdav.model.Supportedlock;
import org.apache.wink.webdav.model.WebDAVModelHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WebDAVResponseBuilder {

    private static final Logger logger = LoggerFactory.getLogger(WebDAVResponseBuilder.class);

    private UriInfo             uriInfo;

    private WebDAVResponseBuilder(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public static WebDAVResponseBuilder create(UriInfo uriInfo) {
        return new WebDAVResponseBuilder(uriInfo);
    }

    /**
     * Process the PROPFIND request for a given entry and create a response
     * using the default implementation of {@link PropertyHandler}
     * 
     * @param entry the entry containing the data to use to create the propfind
     *            response
     * @param propfindXml the propfind xml request to create the response for
     * @return a response to the profind request
     * @throws IOException
     */
    public Response propfind(SyndEntry entry, String propfindXml) throws IOException {
        return propfind(entry, propfindXml, new PropertyHandler());
    }

    /**
     * Process the PROPFIND request for given document resource and create a
     * response.
     * 
     * @param entry the entry containing the data to use to create the propfind
     *            response
     * @param propfindXml the propfind xml request to create the response for
     * @param handler a {@link PropertyHandler} that will be used to retrieve
     *            the values of properties for the response
     * @return a response to the profind request
     * @throws IOException
     */
    public Response propfind(SyndEntry entry, String propfindXml, PropertyHandler handler)
        throws IOException {

        // parse the request (no content means 'all properties')
        Propfind propfind = null;
        if (propfindXml == null || propfindXml.length() == 0) {
            propfind = new Propfind();
            propfind.setAllprop(new Allprop());
        } else {
            propfind = Propfind.unmarshal(new StringReader(propfindXml));
        }

        // make the response
        Multistatus multistatus = new Multistatus();

        // fill the multistatus object with the response
        addResponseToMultistatus(multistatus, propfind, entry, handler);

        // HTTP response
        Response httpResponse =
            Response.status(HttpStatus.MULTI_STATUS.getCode()).entity(multistatus).build();
        return httpResponse;
    }

    /**
     * Process the PROPFIND request for the given feed and create a response
     * using the default implementation of {@link CollectionPropertyHandler}
     * 
     * @param feed the feed containing the data to use to create the propfind
     *            response
     * @param propfindXml the propfind xml request to create the response for
     * @param depthStr the value of the Depth header
     * @return a response to the profind request
     * @throws IOException
     */
    public Response propfind(SyndFeed feed, String propfindXml, String depthStr) throws IOException {
        return propfind(feed, propfindXml, depthStr, new CollectionPropertyHandler());
    }

    /**
     * Process the PROPFIND request for the given collection resource and create
     * a response.
     * 
     * @param feed the feed containing the data to use to create the propfind
     *            response
     * @param propfindXml the propfind xml request to create the response for
     * @param depthStr the value of the Depth header
     * @param provider a CollectionPropertyProvider that will be used to
     *            retrieve the values of properties for the response
     * @return a response to the profind request
     * @throws IOException
     */
    public Response propfind(SyndFeed feed,
                             String propfindXml,
                             String depthStr,
                             CollectionPropertyHandler provider) throws IOException {

        // parse the request (no content means 'all properties')
        Propfind propfind = null;
        if (propfindXml == null || propfindXml.length() == 0) {
            propfind = new Propfind();
            propfind.setAllprop(new Allprop());
        } else {
            propfind = Propfind.unmarshal(new StringReader(propfindXml));
        }

        // make the response
        Multistatus multistatus = new Multistatus();

        // root collection
        addResponseToMultistatus(multistatus, propfind, feed, provider);

        // sub-collections and entries
        // get Depth header
        int depth = 1; // the default depth should be infinity but we support
                       // only 0 or 1
        // String strDepth = requestHeaders.getFirst(WebDAVHeaders.DEPTH);
        if (depthStr != null) {
            depth = Integer.parseInt(depthStr);
        }
        // limit depth to 0 or 1 - robust behaviour (do not report an error)
        if (depth < 0) {
            depth = 0;
        } else if (depth > 1) {
            depth = 1;
        }

        if (depth > 0) { // depth == 1
            // entries
            for (SyndEntry entry : feed.getEntries()) {
                entry.setBase(feed.getBase()); // use the feed URI base
                if (provider.isSubCollection(entry)) {
                    // sub-collection
                    SyndFeed subCollection = provider.getSubCollection(entry);
                    if (subCollection != null) {
                        addResponseToMultistatus(multistatus, propfind, subCollection, provider);
                    }
                } else {
                    // entry
                    addResponseToMultistatus(multistatus, propfind, entry, provider
                        .getEntryPropertyHandler());
                }
            }

            // sub-collections
            List<SyndFeed> subCollections = provider.getSubCollections(this, feed);
            if (subCollections != null) {
                for (SyndFeed subCollection : subCollections) {
                    addResponseToMultistatus(multistatus, propfind, subCollection, provider);
                }
            }
        }

        // HTTP response
        Response httpResponse =
            Response.status(HttpStatus.MULTI_STATUS.getCode()).entity(multistatus).build();
        return httpResponse;
    }

    /**
     * Adds a WebDAV response element to the given multistatus element.
     * 
     * @param multistatus the multistatus response
     * @param propfind the propfind request
     * @param synd either feed or entry
     * @param handler the property handler to use to set property values
     */
    private void addResponseToMultistatus(Multistatus multistatus,
                                          Propfind propfind,
                                          SyndBase synd,
                                          PropertyHandler handler) {

        // create response and add it to the multistatus object
        org.apache.wink.webdav.model.Response response =
            new org.apache.wink.webdav.model.Response();
        response.getHref().add(getResourceLink(synd));
        multistatus.getResponse().add(response);

        // the request is for all property names
        if (propfind.getPropname() != null) {
            Propstat propstat =
                response.getOrCreatePropstat(Response.Status.OK.getStatusCode(), null, null);
            Prop prop = propstat.getProp();
            // call the abstract method to allow the handler to fill in the
            // property names
            handler.setAllPropertyNames(this, prop, synd);
            // ensure that all property values are empty
            ensurePropertiesAreEmpty(prop);
        } else {
            Prop prop = null;
            // the request is for all properties
            if (propfind.getAllprop() != null) {
                prop = new Prop();
                handler.setAllPropertyNames(this, prop, synd);
            } else {
                // the request is for specific properties
                prop = propfind.getProp();
            }
            setPropertyValues(response, prop, synd, handler);
        }
    }

    /**
     * Set property values in the provided Response instance. The properties to
     * set are indicated in the provided Prop object.
     * 
     * @param prop the Prop instance to fill with values
     */
    private void setPropertyValues(org.apache.wink.webdav.model.Response response,
                                   Prop sourceProp,
                                   SyndBase synd,
                                   PropertyHandler provider) {
        if (sourceProp == null || response == null) {
            return;
        }

        if (sourceProp.getCreationdate() != null) {
            provider.setPropertyValue(this, response, new Creationdate(), synd);
        }
        if (sourceProp.getDisplayname() != null) {
            provider.setPropertyValue(this, response, new Displayname(), synd);
        }
        if (sourceProp.getGetcontentlanguage() != null) {
            provider.setPropertyValue(this, response, new Getcontentlanguage(), synd);
        }
        if (sourceProp.getGetcontentlength() != null) {
            provider.setPropertyValue(this, response, new Getcontentlength(), synd);
        }
        if (sourceProp.getGetcontenttype() != null) {
            provider.setPropertyValue(this, response, new Getcontenttype(), synd);
        }
        if (sourceProp.getGetetag() != null) {
            provider.setPropertyValue(this, response, new Getetag(), synd);
        }
        if (sourceProp.getGetlastmodified() != null) {
            provider.setPropertyValue(this, response, new Getlastmodified(), synd);
        }
        if (sourceProp.getLockdiscovery() != null) {
            provider.setPropertyValue(this, response, new Lockdiscovery(), synd);
        }
        if (sourceProp.getResourcetype() != null) {
            provider.setPropertyValue(this, response, new Resourcetype(), synd);
        }
        if (sourceProp.getSupportedlock() != null) {
            provider.setPropertyValue(this, response, new Supportedlock(), synd);
        }
        for (Element element : sourceProp.getAny()) {
            Element newElement =
                WebDAVModelHelper.createElement(element.getNamespaceURI(), element.getLocalName());
            provider.setPropertyValue(this, response, newElement, synd);
        }
    }

    private void ensurePropertiesAreEmpty(Prop prop) {
        if (prop == null) {
            return;
        }

        if (prop.getCreationdate() != null) {
            prop.getCreationdate().setValue((String)null);
        }
        if (prop.getDisplayname() != null) {
            prop.getDisplayname().setValue(null);
        }
        if (prop.getGetcontentlanguage() != null) {
            prop.getGetcontentlanguage().setValue(null);
        }
        if (prop.getGetcontentlength() != null) {
            prop.getGetcontentlength().setValue(null);
        }
        if (prop.getGetcontenttype() != null) {
            prop.getGetcontenttype().setValue(null);
        }
        if (prop.getGetetag() != null) {
            prop.getGetetag().setValue(null);
        }
        if (prop.getGetlastmodified() != null) {
            prop.getGetlastmodified().setValue((String)null);
        }
        if (prop.getLockdiscovery() != null) {
            prop.getLockdiscovery().getActivelock().clear();
        }
        if (prop.getResourcetype() != null) {
            prop.getResourcetype().setCollection(null);
            prop.getResourcetype().getAny().clear();
        }
        if (prop.getSupportedlock() != null) {
            prop.getSupportedlock().getLockentry().clear();
        }
        for (Element element : prop.getAny()) {
            // remove all child nodes (including text)
            NodeList nodes = element.getChildNodes();
            if (nodes != null && nodes.getLength() > 0) {
                for (int i = 0; i < nodes.getLength(); ++i) {
                    element.removeChild(nodes.item(0));
                }
            }
        }
    }

    /**
     * Get the normalized URL of the resource by first trying the 'edit' link
     * and then the 'self' link.
     * 
     * @param synd the synd to extract the link from
     * @return the URL of the resource
     * @throws WebApplicationException if neither the 'edit' nor the 'self'
     *             links exist.
     */
    private String getResourceLink(SyndBase synd) {
        // try 'edit' link
        SyndLink link = synd.getLink("edit"); //$NON-NLS-1$
        if (link == null) {
            // try 'self' link
            link = synd.getLink("self"); //$NON-NLS-1$
        }
        if (link == null) {
            // no link in the resource
            logger.error(Messages.getMessage("webDAVNoEditOrSelfLink", synd.getId())); //$NON-NLS-1$
            throw new WebApplicationException();
        }

        URI uri = URI.create(link.getHref()).normalize();
        if (!uri.isAbsolute()) {
            // add base URI for relative links
            URI base = uriInfo.getAbsolutePath();
            if (synd.getBase() != null) {
                base = URI.create(synd.getBase());
            }
            return base.resolve(uri).getRawPath(); // keep the path escaped
        } else {
            return uri.getRawPath(); // keep the path escaped
        }
    }

    /**
     * Used during the creation of a WebDAV multistatus response to get the
     * properties and their values. Applications may override any of the methods
     * as required.
     */
    public static class PropertyHandler {

        /**
         * Set the Prop instance with empty (no values) properties. This method
         * is invoked to create a response for a propfind request containing a
         * propname element
         * 
         * @param builder the current WebDAVResponseBuilder which can be used to
         *            obtain context information
         * @param synd instance of the synd to set the property names for
         * @param prop the Prop instance to fill
         */
        public void setAllPropertyNames(WebDAVResponseBuilder builder, Prop prop, SyndBase synd) {
            prop.setDisplayname(new Displayname());
            prop.setGetlastmodified(new Getlastmodified());
            prop.setResourcetype(new Resourcetype());

            // also returns the creation date by default for an entry
            if (synd instanceof SyndEntry) {
                prop.setCreationdate(new Creationdate());
            }
        }

        /**
         * Set the value of a provided property, and set the property on the
         * response object with the correct status.
         * <p>
         * Applications should override this method with their own
         * implementation in order to set the values of proprietary properties
         * and to extend the default behavior.
         * <p>
         * see {@link Response#setProperty(Object, int, String, Error)} for a
         * detailed description of the possible property values.
         * <p>
         * Examples:
         * <p>
         * if the property is <code>DAV:getcontentlanguage</code> then the type
         * of the property is an instance of Getcontentlanguage, and setting
         * it's value is performed as follows:
         * 
         * <pre>
         * if (property instanceof Getcontentlanguage) {
         *     ((Getcontentlanguage)property).setValue(&quot;en-us&quot;);
         *     response.setOk(property);
         * }
         * </pre>
         * 
         * if the property is <code>K:myprop</code> then the type of the
         * property is an instance of org.w3c.dom.Element, and setting it's
         * value is performed as follows:
         * 
         * <pre>
         * if (property instanceof Element) {
         *     ((Element)property).setTextContent(&quot;My Property Value!&quot;);
         *     response.setOk(property);
         * }
         * </pre>
         * 
         * If the requested property does not have a value, the application
         * should call the {@link Response#setPropertyNotFound(Object)} method
         * and pass it the property object, like so:
         * 
         * <pre>
         * response.setNotFound(property);
         * </pre>
         * 
         * @param builder the current WebDAVResponseBuilder which can be used to
         *            obtain context information
         * @param response the {@link org.apache.wink.webdav.model.Response}
         *            instance that is to receive the property and its value
         * @param property the property object. see
         *            {@link org.apache.wink.webdav.model.Response#setProperty(Object, int, String, Error)}
         *            for a detailed description of the possible property values
         * @param synd an instance of synd (either feed or entry)
         */
        public void setPropertyValue(WebDAVResponseBuilder builder,
                                     org.apache.wink.webdav.model.Response response,
                                     Object property,
                                     SyndBase synd) {
            if (property instanceof Displayname) {
                if (synd.getTitle().getValue() != null) {
                    ((Displayname)property).setValue(synd.getTitle().getValue());
                    response.setPropertyOk(property);
                    return;
                }
            } else if (property instanceof Getlastmodified) {
                if (synd.getUpdated() != null) {
                    ((Getlastmodified)property).setValue(synd.getUpdated());
                    response.setPropertyOk(property);
                    return;
                }
            }

            if (synd instanceof SyndEntry) {
                SyndEntry entry = (SyndEntry)synd;
                if (property instanceof Creationdate) {
                    if (entry.getPublished() != null) {
                        ((Creationdate)property).setValue(entry.getPublished());
                        response.setPropertyOk(property);
                        return;
                    }
                } else if (property instanceof Resourcetype) {
                    response.setPropertyOk(property);
                    return;
                }
            }

            if (synd instanceof SyndFeed) {
                if (property instanceof Resourcetype) {
                    ((Resourcetype)property).setCollection(new Collection());
                    response.setPropertyOk(property);
                    return;
                }
            }

            response.setPropertyNotFound(property);
        }
    }

    /**
     * Extends the {@link PropertyHandler} to provide additional data for the
     * creation of multistatus responses for collections (feeds).
     */
    public static class CollectionPropertyHandler extends PropertyHandler {

        private PropertyHandler entryPropertyHandler;

        /**
         * Constructs a CollectionPropertyProvider that is also the property
         * handler for the entries
         */
        public CollectionPropertyHandler() {
            setEntryPropertyHandler(this);
        }

        /**
         * Constructor accepting another {@link PropertyHandler} to provide
         * properties for the entries in the collection.
         * 
         * @param entryPropertyHandler
         */
        public CollectionPropertyHandler(PropertyHandler entryPropertyHandler) {
            setEntryPropertyHandler(entryPropertyHandler);
        }

        /**
         * Gets the list of sub-collection feeds for a given feed. The default
         * implementation returns <code>null</code>
         * 
         * @param builder the current WebDAVResponseBuilder which can be used to
         *            obtain context information
         * @param feed the feed to obtain the sub collections from
         * @return the sub-collections
         */
        public List<SyndFeed> getSubCollections(WebDAVResponseBuilder builder, SyndFeed feed) {
            return null;
        }

        /**
         * Specifies if an entry actually represents a feed. This method is
         * called only when building the response for an entry that is part of a
         * feed. The default implementation returns <code>false</code>
         * 
         * @param entry the entry
         * @return <code>true</code> if the entry is a sub-collection, otherwise
         *         <code>false</code>
         */
        public boolean isSubCollection(SyndEntry entry) {
            return false;
        }

        /**
         * Get the feed that this entry represents. This method is called if the
         * {@link #isEntrySubCollection(DocumentResource)} method returns
         * <code>true</code> for the given entry. The default implementation
         * returns <code>null</code>
         * 
         * @param entry the entry that represents a feed
         * @return an instance of a SyndFeed
         */
        public SyndFeed getSubCollection(SyndEntry entry) {
            return null;
        }

        /**
         * Get the PropertyHandler that is used to set properties for the
         * entries in the collection
         * 
         * @return the PropertyHandler used to set properties for the entries in
         *         the collection
         */
        public final PropertyHandler getEntryPropertyHandler() {
            return entryPropertyHandler;
        }

        /**
         * Set the PropertyHandler that is used to set properties for the
         * entries in the collection
         * 
         * @param entryPropertyHandler the entry PropertyHandler
         */
        public final void setEntryPropertyHandler(PropertyHandler entryPropertyHandler) {
            this.entryPropertyHandler = entryPropertyHandler;
        }
    }

}
