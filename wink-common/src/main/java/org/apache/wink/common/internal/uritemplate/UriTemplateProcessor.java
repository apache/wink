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

package org.apache.wink.common.internal.uritemplate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.common.internal.utils.UriHelper;

/**
 * Abstract URI template processor for compiling, matching and expanding URI
 * templates
 */
public abstract class UriTemplateProcessor implements Comparable<UriTemplateProcessor>, Cloneable {

    protected static final String                    TEMPLATE_TAIL_NAME    = "wink.TemplateTail";
    protected static final String                    TEMPLATE_HEAD_NAME    = "wink.TemplateHead";
    protected static final String                    TEMPLATE_TAIL_PATTERN = "(/.*)?";

    protected String                                 template;
    protected Pattern                                pattern;
    protected MultivaluedMap<String, CapturingGroup> variables;
    protected List<TemplateElement>                  expanders;
    protected int                                    numOfLiteralCharacters;

    // this is used to hold the string that matches the complete original
    // template as a single
    // capturing group
    protected CapturingGroup                         head;

    // this is used to hold the tail of the matched strings,
    // that is, the trailing part of the matched uri that does not fall into the
    // original
    // the uri template
    protected CapturingGroup                         tail;

    protected UriTemplateProcessor() {
        template = null;
        pattern = null;
        tail = null;
        head = null;
        variables = new MultivaluedMapImpl<String, CapturingGroup>();
        expanders = new ArrayList<TemplateElement>();
        numOfLiteralCharacters = 0;
    }

    protected void reset() {
        variables.clear();
        expanders.clear();
        pattern = null;
        tail = null;
        head = null;
        numOfLiteralCharacters = 0;
    }

    /**
     * Get the template that is associated with this processor
     * 
     * @return the processor template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Get the regular expression pattern that this processor uses to match and
     * expand uri's. The regular expression is the result of compiling the uri
     * template that is associated with this processor.
     * 
     * @return the regular expression that this processor compiled
     */
    public String getPatternString() {
        assertPatternState();
        return pattern.pattern();
    }

    /* package */Pattern getPattern() {
        return pattern;
    }

    /* package */MultivaluedMap<String, CapturingGroup> getVariables() {
        return variables;
    }

    /**
     * Get a set of the variable names that exist in this processor's template.
     * The order of the variables in the set is the same as it appears in the
     * template.
     * 
     * @return a set of variable names as they appear in the template, and in
     *         the order in which they appear
     * @throws IllegalStateException if the pattern was not compiled
     *             successfully
     */
    public Set<String> getVariableNames() throws IllegalStateException {
        assertPatternState();
        Set<String> set = new LinkedHashSet<String>(variables.keySet());
        return set;
    }

    /**
     * Get a new instance of a {@link UriTemplateMatcher} that can be used to
     * perform matching and matched variables values retrieval
     * 
     * @return a new instance of UriTemplateMatcher
     */
    public UriTemplateMatcher matcher() {
        assertPatternState();
        return new UriTemplateMatcher(this);
    }

    /**
     * Set the template of this processor to the specified template. Only after
     * calling this method can the {@link #matcher()} method be called.
     * 
     * @param template the template to compile and set
     */
    public abstract void compile(String template);

    /**
     * Expands the template into a URI. Same as calling
     * <code>expand(values, true)</code>
     * 
     * @param variables map from variable names to variable values
     * @return the URI instance
     */
    public String expand(Map<String, ? extends Object> values) {
        return expand(values, true);
    }

    /**
     * Expands the template into a URI.
     * <p>
     * If the value is an array or a <code>List&lt;? extends Object&gt;</code>,
     * then all objects are converted to strings using the toString() method,
     * otherwise the value itself is converted to string using the toString()
     * method.
     * 
     * @param variables map from variable names to variable values
     * @param encode indicates whether to encode the values before expansion
     * @return the expanded URI string
     */
    public String expand(Map<String, ? extends Object> values, boolean encode) {
        MultivaluedMap<String, String> mValues = MultivaluedMapImpl.toMultivaluedMapString(values);
        return expand(mValues, encode);
    }

    /**
     * Expands the template into a URI. Same as calling
     * <code>expand(values, true)</code>
     * 
     * @param variables map from variable names to variable values
     * @return the URI instance
     */
    public String expand(MultivaluedMap<String, String> values) {
        return expand(values, true);
    }

    /**
     * Expand the template of this processor using the provided values. Regular
     * expressions of variables in the template are ignored. All variables
     * defined in the template must have a value.
     * 
     * @param values a map with the values of the variables
     * @return an expanded uri using the supplied variable values
     */
    public String expand(MultivaluedMap<String, String> values, boolean encode) {
        if (values == null) {
            values = new MultivaluedMapImpl<String, String>();
        }
        assertPatternState();

        StringBuilder result = new StringBuilder();
        // go over all of the expanders and invoke the expand() method
        for (TemplateElement expander : expanders) {
            expander.expand(values, encode, result);
        }
        String resultStr = result.toString();
        return resultStr;
    }

//    @Override
//    public boolean equals(Object obj) {
//        return obj instanceof UriTemplateProcessor && template
//            .equals(((UriTemplateProcessor)obj).template);
//    }

    
    
    @Override
    public String toString() {
        return template;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((template == null) ? 0 : template.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UriTemplateProcessor other = (UriTemplateProcessor)obj;
        if (template == null) {
            if (other.template != null)
                return false;
        } else if (!template.equals(other.template))
            return false;
        return true;
    }

    @Override
    public UriTemplateProcessor clone() {
        try {
            UriTemplateProcessor ret = (UriTemplateProcessor)super.clone();
            return ret;
        } catch (CloneNotSupportedException e) {
            // can't happen
            throw new WebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
        }
    }

    public int compareTo(UriTemplateProcessor other) {
        int ret = compareLiteralCharacters(other);
        if (ret != 0) {
            return ret;
        }
        return compareNumOfVariables(other);
    }

    private int compareNumOfVariables(UriTemplateProcessor other) {
        return (variables.size() - other.variables.size());
    }

    private int compareLiteralCharacters(UriTemplateProcessor other) {
        return (numOfLiteralCharacters - other.numOfLiteralCharacters);
    }

    protected void assertPatternState() {
        if (pattern == null) {
            throw new IllegalStateException("pattern not compiled");
        }
    }

    /**
     * Normalizes the URI to a standard form. This includes for example handling
     * null, removing slashes, unnecessary segments, ...
     * 
     * @param uri the input template; <code>null</code> is allowed
     * @return the transformed version of the URI
     */
    public static String normalizeUri(String uri) {
        String normalizedUri;
        if (uri != null) {
            normalizedUri = UriHelper.normalize(uri);
        } else {
            normalizedUri = "";
        }
        if (normalizedUri.startsWith("/")) {
            normalizedUri = normalizedUri.substring(1);
        }

        return normalizedUri;
    }

    /**
     * Factory method for normalized uri-templates.
     * 
     * @param uriTemplate uri-template specification
     * @return instance representing (normalized) uri-template
     * @see UriTemplateProcessor#normalizeUri(String)
     */
    public static UriTemplateProcessor newNormalizedInstance(String uriTemplate) {
//        try {
            return JaxRsUriTemplateProcessor.newNormalizedInstance(uriTemplate);
//        } catch (IllegalArgumentException e) {
//            // fallback to handle BitWorking style templates
//            return BitWorkingUriTemplateProcessor.newNormalizedInstance(uriTemplate);
//        }
    }

    /**
     * Check if the <code>path</code> parameter match the <code>template</code>
     * 
     * @param template
     * @param path
     * @return
     */
    public static boolean match(String template, String path) {
        UriTemplateProcessor processor = UriTemplateProcessor.newNormalizedInstance(template);
        UriTemplateMatcher matcher = processor.matcher();
        return matcher.matches(path);
    }

    /**
     * Represents a basic element of a template. A template consists of literals
     * and variables.
     */
    protected static interface TemplateElement {

        /**
         * Called during the compilation of a template to build a regular
         * expression pattern corresponding to this element
         * 
         * @param builder the output StringBuilder
         */
        public void build(StringBuilder builder);

        /**
         * Called during the expansion of a template to expand this template
         * into the output StringBuilder.
         * 
         * @param values a multivalued map of values to use for the expansion
         * @param encode indicates whether to uri-encode the value before
         *            writing it to the output
         * @param builder the output StringBuilder
         */
        public void expand(MultivaluedMap<String, String> values,
                           boolean encode,
                           StringBuilder builder);
    }

    /**
     * Represents a literal element of the template. A literal is a sequence of
     * characters in the template without any variables in it. For instance
     * "/foo/" is a literal in the template "/foo/{var}".
     */
    protected static class Literal implements TemplateElement {
        private String literal;

        public Literal(String literal) {
            assertValid(literal);
            this.literal = literal;
        }

        public void build(StringBuilder builder) {
            builder.append(Pattern.quote(literal));
        }

        private static void assertValid(String literal) {
            // assert that the literal does not contain curly brackets
            if (literal.indexOf('{') != -1 || literal.indexOf('}') != -1) {
                throw new IllegalArgumentException("Syntax error: '" + literal
                    + "' contains invalid template form");
            }
        }

        public void expand(MultivaluedMap<String, String> values,
                           boolean encode,
                           StringBuilder builder) {
            String literal = this.literal;
            if (!encode) {
                literal = UriEncoder.decodeString(literal);
            }
            builder.append(literal);
        }
    }

    /**
     * Represents a single capturing group in the pattern created from a
     * template uri.
     */
    protected abstract static class CapturingGroup implements TemplateElement {
        protected static final String REGEX0           = "[^/]*?";
        protected static final String REGEX1           = "[^/]+?";

        protected int                 capturingGroupId = -1;

        /**
         * Get the pattern capturing group id that is associated with this
         * variable
         * 
         * @return capturing group id
         */
        public int getCapturingGroupId() {
            return capturingGroupId;
        }

        /**
         * Set the pattern capturing group id that is associated with this
         * variable
         * 
         * @param capturingGroupId capturing group id
         */
        public void setCapturingGroupId(int capturingGroupId) {
            this.capturingGroupId = capturingGroupId;
        }

        /**
         * Called during the retrieval of the value(s) of this template variable
         * from a matched uri
         * 
         * @param matched the string that was a match to the pattern of this
         *            variable
         * @param values the output multivalued map to put the values into
         * @param startIndex the start index of the matched string in the input
         *            uri
         * @param indices the output multivalued map to put the index into
         */
        public abstract void onMatch(String matched,
                                     MultivaluedMap<String, String> values,
                                     int startIndex,
                                     MultivaluedMap<String, Integer> indices);

    }

    /**
     * Represents a single variable in a template
     */
    protected static class Variable extends CapturingGroup {
        // variable name
        protected String name;
        // variable regular expression
        protected String regex;
        // variable default value
        protected String defaultValue;

        public Variable(String name, String regex, String defaultValue) {
            super();
            this.name = name;
            this.defaultValue = defaultValue;

            if (regex == null) {
                this.regex = REGEX1;
            } else {
                this.regex = regex;
            }
        }

        public String getName() {
            return name;
        }

        public String getRegex() {
            return regex;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void build(StringBuilder builder) {
            // we don't want any regex supplied by the user to contain any
            // capturing groups
            // that can interfere with the capturing groups of the regex that we
            // are building,
            // so we convert all the capturing groups into non-capturing groups
            regex = convertAllGroupsToNonCapturing(regex);
            builder.append("(");
            builder.append(regex);
            builder.append(")");
        }

        @Override
        public void onMatch(String matched,
                            MultivaluedMap<String, String> values,
                            int startIndex,
                            MultivaluedMap<String, Integer> indices) {
            values.add(name, matched);
            indices.add(name, startIndex);
        }

        public void expand(MultivaluedMap<String, String> values,
                           boolean encode,
                           StringBuilder builder) {
            String value = values.getFirst(name);
            if (value == null) {
                value = defaultValue;
            }

            if (value == null) {
                throw new IllegalArgumentException("variable '" + name
                    + "' was not supplied a value");
            }

            if (encode) {
                value = UriEncoder.encodeString(value);
            }
            builder.append(value);
        }

        /*
         * converts all the capturing groups in the provided regex into
         * non-capturing groups. e.g.: 1. "a(b)*c" is converted to "a(?:b)*c" 2.
         * "(a+)(?:b+)" is converted to "(?:a+)(?:b+)"
         */
        private String convertAllGroupsToNonCapturing(String regex) {
            StringBuffer sb = new StringBuffer();
            int regexLen = regex.length();
            for (int i = 0; i < regexLen; ++i) {
                char ch = regex.charAt(i);
                sb.append(ch);
                // if the char is '(', and the char after it is not a '?',
                // then this is a capturing group, so replace it with
                // a non-capturing group
                if ((ch == '(') && (i + 1 < regexLen) && (regex.charAt(i + 1) != '?')) {
                    sb.append("?:");
                }
            }
            return sb.toString();
        }
    }

    protected static interface BaseCompilationHandler {
        /**
         * Start of compilation event
         * 
         * @param template the uri template that is being compiled
         */
        public void startCompile(String template);

        /**
         * Literal part event. The literal part is all the characters from the
         * the previous variable (or the start of the uri template if there was
         * no variable), up to the next variable found (or the end of the uri
         * template if none exist).
         * <p>
         * E.g. if the template is "{var1}/foo/{var2}/goo" then there will be
         * two literal events fired, one for "/foo/" and one for "/goo".
         * 
         * @param literal the literal part
         */
        public void literal(String literal);

        /**
         * End of compilation event. Indicates that the template contains no
         * more variables.
         * 
         * @param literal the last literal part of the template after the last
         *            variable that was found
         */
        public void endCompile(String literal);
    }

    /**
     * Abstract base class for concrete pattern builders
     */
    protected static abstract class AbstractPatternBuilder implements BaseCompilationHandler {
        // the processor whose uri template is being compiled and set
        protected UriTemplateProcessor processor;
        // holds the regex template string that will be compiled at the end of
        // the compilation
        protected StringBuilder        patternBuilder;
        // counter to track the regex pattern capturing group id
        protected int                  capturingGroupId;

        protected AbstractPatternBuilder(UriTemplateProcessor processor) {
            this.processor = processor;
        }

        public void startCompile(String template) {
            processor.template = template;
            processor.reset();
            patternBuilder = new StringBuilder(template.length());
            capturingGroupId = 0;
            openHead();
        }

        public void literal(String literal) {
            int length = literal.length();
            if (length == 0) {
                return;
            }
            // create a new literal element and save it for use during expansion
            Literal element = new Literal(literal);
            element.build(patternBuilder);
            // save it for use during expansion
            processor.expanders.add(element);
            processor.numOfLiteralCharacters += length;
        }

        public void endCompile(String literal) {
            literal(removeTrailingSlash(literal));

            closeHead();
            createTail();

            // compile and save the regex pattern
            String templatePatternStr = patternBuilder.toString();
            processor.pattern = Pattern.compile(templatePatternStr);
        }

        // remove the trailing '/' from the literal if it exists
        protected String removeTrailingSlash(String literal) {
            if (literal.length() > 0 && literal.charAt(literal.length() - 1) == '/') {
                literal = literal.substring(0, literal.length() - 1);
            }
            return literal;
        }

        // Open the head capturing group
        protected void openHead() {
            patternBuilder.append('(');
            // Create new Variable that holds the head
            processor.head = new Variable(TEMPLATE_HEAD_NAME, null, null);
            ++capturingGroupId;
            processor.head.setCapturingGroupId(capturingGroupId);
        }

        // Close the head capturing group
        protected void closeHead() {
            patternBuilder.append(')');
        }

        // add the tail part of the template pattern as defined by the JAX-RS
        // spec
        protected void createTail() {
            processor.tail = createVariable(TEMPLATE_TAIL_NAME, TEMPLATE_TAIL_PATTERN, null);
        }

        /**
         * Create a new template variable
         * 
         * @param name name of the variable
         * @param regex the regular expression of the variable
         * @param defaultValue the default value of the variable
         * @return
         */
        protected CapturingGroup createVariable(String name, String regex, String defaultValue) {
            CapturingGroup variable = new Variable(name, regex, defaultValue);
            // build it into the pattern
            variable.build(patternBuilder);
            // set its capturing group
            ++capturingGroupId;
            variable.setCapturingGroupId(capturingGroupId);
            return variable;
        }
    }

    /**
     * Abstract base class for concrete template expanders
     */
    protected abstract static class AbstractTemplateExpander implements BaseCompilationHandler {
        protected MultivaluedMap<String, String> values;
        protected StringBuilder                  out;

        public AbstractTemplateExpander(MultivaluedMap<String, String> values, StringBuilder out) {
            this.values = values;
            this.out = out;
        }

        public void startCompile(String template) {
            if (values == null) {
                values = new MultivaluedMapImpl<String, String>();
            }
        }

        public void literal(String literal) {
            out.append(literal);
        }

        public void endCompile(String literal) {
            literal(literal);
        }
    }
}
