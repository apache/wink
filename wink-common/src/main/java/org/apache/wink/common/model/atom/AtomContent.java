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
package org.apache.wink.common.model.atom;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.wink.common.RestException;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.model.AnyContentHandler;
import org.apache.wink.common.internal.model.ModelUtils;
import org.apache.wink.common.model.synd.SyndContent;

/**
 * The &quot;atom:content&quot; element Per RFC4287
 * 
 * <pre>
 * The &quot;atom:content&quot; element either contains or links to the content of the entry. The content of atom:content is Language-Sensitive.
 * 
 * atomInlineTextContent =
 *    element atom:content {
 *       atomCommonAttributes,
 *       attribute type { &quot;text&quot; | &quot;html&quot; }?,
 *       (text)*
 *    }
 * 
 * atomInlineXHTMLContent =
 *    element atom:content {
 *       atomCommonAttributes,
 *       attribute type { &quot;xhtml&quot; },
 *       xhtmlDiv
 *    }
 * 
 * atomInlineOtherContent =
 *    element atom:content {
 *       atomCommonAttributes,
 *       attribute type { atomMediaType }?,
 *       (text|anyElement)*
 *    }
 * 
 * atomOutOfLineContent =
 *    element atom:content {
 *       atomCommonAttributes,
 *       attribute type { atomMediaType }?,
 *       attribute src { atomUri },
 *       empty
 *    }
 * 
 * atomContent = atomInlineTextContent
 *  | atomInlineXHTMLContent
 *  | atomInlineOtherContent
 *  | atomOutOfLineContent
 *  
 * o The &quot;type&quot; Attribute
 * 
 *    On the atom:content element, the value of the &quot;type&quot; attribute MAY be
 *    one of &quot;text&quot;, &quot;html&quot;, or &quot;xhtml&quot;.  Failing that, it MUST conform to
 *    the syntax of a MIME media type, but MUST NOT be a composite type
 *    (see Section 4.2.6 of [MIMEREG]).  If neither the type attribute nor
 *    the src attribute is provided, Atom Processors MUST behave as though
 *    the type attribute were present with a value of &quot;text&quot;.
 * 
 * o The &quot;src&quot; Attribute
 * 
 *    atom:content MAY have a &quot;src&quot; attribute, whose value MUST be an IRI
 *    reference [RFC3987].  If the &quot;src&quot; attribute is present, atom:content
 *    MUST be empty.  Atom Processors MAY use the IRI to retrieve the
 *    content and MAY choose to ignore remote content or to present it in a
 *    different manner than local content.
 * 
 *    If the &quot;src&quot; attribute is present, the &quot;type&quot; attribute SHOULD be
 *    provided and MUST be a MIME media type [MIMEREG], rather than &quot;text&quot;,
 *    &quot;html&quot;, or &quot;xhtml&quot;.  The value is advisory; that is to say, when the
 *    corresponding URI (mapped from an IRI, if necessary) is dereferenced,
 *    if the server providing that content also provides a media type, the
 *    server-provided media type is authoritative.
 * 
 * o Processing Model
 * 
 *    Atom Documents MUST conform to the following rules.  Atom Processors
 *    MUST interpret atom:content according to the first applicable rule.
 * 
 *    1.  If the value of &quot;type&quot; is &quot;text&quot;, the content of atom:content
 *        MUST NOT contain child elements.  Such text is intended to be
 *        presented to humans in a readable fashion.  Thus, Atom Processors
 *        MAY collapse white space (including line breaks), and display the
 *        text using typographic techniques such as justification and
 *        proportional fonts.
 * 
 *    2.  If the value of &quot;type&quot; is &quot;html&quot;, the content of atom:content
 *        MUST NOT contain child elements and SHOULD be suitable for
 *        handling as HTML [HTML].  The HTML markup MUST be escaped; for
 *        example, &quot;&lt;br&gt;&quot; as &quot;&lt;br&gt;&quot;.  The HTML markup SHOULD be such
 *        that it could validly appear directly within an HTML &lt;DIV&gt;
 *        element.  Atom Processors that display the content MAY use the
 *        markup to aid in displaying it.
 * 
 *    3.  If the value of &quot;type&quot; is &quot;xhtml&quot;, the content of atom:content
 *        MUST be a single XHTML div element [XHTML] and SHOULD be suitable
 *        for handling as XHTML.  The XHTML div element itself MUST NOT be
 *        considered part of the content.  Atom Processors that display the
 *        content MAY use the markup to aid in displaying it.  The escaped
 *        versions of characters such as &quot;&amp;&quot; and &quot;&gt;&quot; represent those
 *        characters, not markup.
 * 
 *    4.  If the value of &quot;type&quot; is an XML media type [RFC3023] or ends
 *        with &quot;+xml&quot; or &quot;/xml&quot; (case insensitive), the content of
 *        atom:content MAY include child elements and SHOULD be suitable
 *        for handling as the indicated media type.  If the &quot;src&quot; attribute
 *        is not provided, this would normally mean that the &quot;atom:content&quot;
 *        element would contain a single child element that would serve as
 *        the root element of the XML document of the indicated type.
 * 
 *    5.  If the value of &quot;type&quot; begins with &quot;text/&quot; (case insensitive),
 *        the content of atom:content MUST NOT contain child elements.
 * 
 *    6.  For all other values of &quot;type&quot;, the content of atom:content MUST
 *        be a valid Base64 encoding, as described in [RFC3548], section 3.
 *        When decoded, it SHOULD be suitable for handling as the indicated
 *        media type.  In this case, the characters in the Base64 encoding
 *        MAY be preceded and followed in the atom:content element by white
 *        space, and lines are separated by a single newline (U+000A)
 *        character.
 * 
 * o Examples
 * 
 *    XHTML inline:
 * 
 *    ...
 *    &lt;content type=&quot;xhtml&quot;&gt;
 *       &lt;div xmlns=&quot;http://www.w3.org/1999/xhtml&quot;&gt;
 *          This is &lt;b&gt;XHTML&lt;/b&gt; content.
 *       &lt;/div&gt;
 *    &lt;/content&gt;
 *    ...
 *    &lt;content type=&quot;xhtml&quot;&gt;
 *       &lt;xhtml:div xmlns:xhtml=&quot;http://www.w3.org/1999/xhtml&quot;&gt;
 *          This is &lt;xhtml:b&gt;XHTML&lt;/xhtml:b&gt; content.
 *       &lt;/xhtml:div&gt;
 *    &lt;/content&gt;
 *    ...
 * 
 *    The following example assumes that the XHTML namespace has been bound
 *    to the &quot;xh&quot; prefix earlier in the document:
 * 
 *    ...
 *    &lt;content type=&quot;xhtml&quot;&gt;
 *       &lt;xh:div&gt;
 *          This is &lt;xh:b&gt;XHTML&lt;/xh:b&gt; content.
 *       &lt;/xh:div&gt;
 *    &lt;/content&gt;
 *    ...
 * 
 * </pre>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "atomContent", propOrder = {"any"})
public class AtomContent extends AtomCommonAttributes {

    @XmlTransient
    private List<Object> any;

    @XmlAttribute
    protected String     type;
    @XmlAttribute
    protected String     src;

    @XmlTransient
    private Object       savedValue = null;

    @XmlTransient
    private Providers providers;
    
    public AtomContent() {
    }

    public AtomContent(SyndContent value) {
        super(value);
        if (value == null) {
            return;
        }
        setSrc(value.getSrc());
        setType(value.getType());
        // copies the value AS IS without invoking providers
        setValue(value.getValue(Object.class));
    }

    public SyndContent toSynd(SyndContent value) {
        if (value == null) {
            return value;
        }
        super.toSynd(value);
        value.setSrc(getSrc());
        value.setType(getType());
        // copies the value AS IS without invoking providers
        value.setValue(getValue(Object.class));
        return value;
    }
    
    /**
     * Sets the Providers on a local field so that the registry of custom and system
     * providers is available when a client application retrieves the value, expecting
     * it to be seamlessly unmarshalled or converted to the expected type declared in
     * getValue(Class).
     * 
     * Client applications should NOT call this method.
     */
    public void setProviders(Providers _providers) {
        providers = _providers;
    }

    /**
     * Gets the value of type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of type.
     */
    public void setType(String value) {
        this.type = value;
        checkValidity();
    }

    /**
     * Gets the value of src.
     */
    public String getSrc() {
        return src;
    }

    /**
     * Sets the value of src.
     */
    public void setSrc(String value) {
        this.src = value;
        checkValidity();
    }

    /**
     * Sets the content of the "atom:content" element as a String. The "type"
     * attribute should be set prior to setting the contents.
     * <p>
     * Atom Documents MUST conform to the following rules. Atom Processors MUST
     * interpret atom:content according to the first applicable rule.
     * </p>
     * <ol>
     * <li>If the value of "type" is "text", the content of atom:content MUST
     * NOT contain child elements. Such text is intended to be presented to
     * humans in a readable fashion. Thus, Atom Processors MAY collapse white
     * space (including line breaks), and display the text using typographic
     * techniques such as justification and proportional fonts.
     * <li>
     * <li>If the value of "type" is "html", the content of atom:content MUST
     * NOT contain child elements and SHOULD be suitable for handling as HTML
     * [HTML]. The HTML markup MUST be escaped; for example, "<br>
     * " as "&lt;br>". The HTML markup SHOULD be such that it could validly
     * appear directly within an HTML <DIV> element. Atom Processors that
     * display the content MAY use the markup to aid in displaying it.
     * <li>
     * <li>If the value of "type" is "xhtml", the content of atom:content MUST
     * be a single XHTML div element [XHTML] and SHOULD be suitable for handling
     * as XHTML. The XHTML div element itself MUST NOT be considered part of the
     * content. Atom Processors that display the content MAY use the markup to
     * aid in displaying it. The escaped versions of characters such as "&" and
     * ">" represent those characters, not markup.
     * <li>
     * <li>If the value of "type" is an XML media type [RFC3023] or ends with
     * "+xml" or "/xml" (case insensitive), the content of atom:content MAY
     * include child elements and SHOULD be suitable for handling as the
     * indicated media type. If the "src" attribute is not provided, this would
     * normally mean that the "atom:content" element would contain a single
     * child element that would serve as the root element of the XML document of
     * the indicated type.
     * <li>
     * <li>If the value of "type" begins with "text/" (case insensitive), the
     * content of atom:content MUST NOT contain child elements.
     * <li>
     * <li>For all other values of "type", the content of atom:content MUST be a
     * valid Base64 encoding, as described in [RFC3548], section 3. When
     * decoded, it SHOULD be suitable for handling as the indicated media type.
     * In this case, the characters in the Base64 encoding MAY be preceded and
     * followed in the atom:content element by white space, and lines are
     * separated by a single newline (U+000A) character.
     * <li>
     * </ol>
     */
    public void setValue(Object value) {
        if (value != null) {
            any = Arrays.asList(value);
        } else {
            any = null;
        }
        checkValidity();
    }

    /**
     * <p>
     * Gets the content of the "atom:content" element as a String. The "type"
     * attribute should be used to determine how to treat the content.
     * <p>
     * Pay attention that de-serialization occurs each time the method is
     * called, so multiple calls to this method may effect the application
     * performance.
     */
    public String getValue() {
        return getValue(String.class);
    }

    /**
     * <p>
     * Gets the content of the "atom:content" element serialized to provided
     * class. The "type" attribute should be used to determine how to treat the
     * content.
     * <p>
     * Pay attention that de-serialization occurs each time the method is
     * called, so multiple calls to this method may effect the application
     * performance.
     */
    public <T> T getValue(Class<T> cls) {
        try {
            return getValue(cls,
                            cls,
                            providers,
                            ModelUtils.EMPTY_ARRAY,
                            ModelUtils.EMPTY_STRING_MAP,
                            ModelUtils.determineMediaType(type));
        } catch (IOException e) {
            // should never happen
            throw new WebApplicationException(e);
        }
    }
    /**
     * <p>
     * Gets the content of the "atom:content" element serialized to provided
     * class according to provided parameters.
     * <p>
     * Pay attention that de-serialization occurs each time the method is
     * called, so multiple calls to this method may effect the application
     * performance.
     */
    public <T> T getValue(Class<T> cls,
                          Type genericType,
                          Providers providers,
                          Annotation[] annotations,
                          MultivaluedMap<String, String> httpHeaders,
                          MediaType mediaType) throws IOException {
        return ModelUtils.readValue(getAny(),
                                       cls,
                                       providers,
                                       genericType,
                                       annotations,
                                       httpHeaders,
                                       mediaType);
    }

    @XmlMixed
    @XmlAnyElement(lax = true, value = AnyContentHandler.class)
    List<Object> getAny() {
        ModelUtils.fixAnyContent(any, type);
        return any;
    }

    void setAny(List<Object> any) {
        this.any = any;
    }

    public void checkValidity() {
        if (src != null && any != null) {
            throw new RestException(Messages.getMessage("contentMayHaveInlineOrOutContent")); //$NON-NLS-1$
        } else if (src != null && type != null) {
            if (type.equals("text") || type.equals("html") || type.equals("xhtml")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                throw new RestException(
                                        Messages.getMessage("typeAttribMustHaveValidMimeType")); //$NON-NLS-1$
            }
        }
    }

    /* package */void revertValue() {
        setValue(savedValue);
        savedValue = null;
    }

    /* package */Object saveValue() {
        this.savedValue = getValue();
        setValue(null);
        return this.savedValue;
    }

}
