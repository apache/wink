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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.uri.UriEncoder;

/**
 * JAX-RS style template processor for compiling, matching and expanding URI
 * templates
 */
public class JaxRsUriTemplateProcessor extends UriTemplateProcessor {

    /*
     * From the JAX-RS API specification: param = "{"WSP nameWSP [ ":"WSP
     * regexWSP ] "}" name = (ALPHA / DIGIT / "_")(ALPHA / DIGIT / "." / "_" /
     * "-" ) ; \w[\w\.-] regex =( nonbrace / "{" nonbrace "}" ) ; where nonbrace
     * is any char other than "{" and "}"
     */
    private static final String  JAXRS_VARIABLE_PATTERN_WSP      = "[ \\t]*"; //$NON-NLS-1$
    private static final String  JAXRS_VARIABLE_PATTERN_NAME     = "(\\w[\\w\\.-]*)"; //$NON-NLS-1$
    private static final String  JAXRS_VARIABLE_PATTERN_NONBRACE = "[^{}]"; //$NON-NLS-1$
    private static final String  JAXRS_VARIABLE_PATTERN_REGEX    =
                                                                     "((?:(?:" + JAXRS_VARIABLE_PATTERN_NONBRACE //$NON-NLS-1$
                                                                         + ")|(?:\\{" //$NON-NLS-1$
                                                                         + JAXRS_VARIABLE_PATTERN_NONBRACE
                                                                         + "*\\}))*)"; //$NON-NLS-1$
    private static final String  JAXRS_VARIABLE_PATTERN_PARAM    =
                                                                     "\\{" + JAXRS_VARIABLE_PATTERN_WSP //$NON-NLS-1$
                                                                         + JAXRS_VARIABLE_PATTERN_NAME
                                                                         + JAXRS_VARIABLE_PATTERN_WSP
                                                                         + "(?::" //$NON-NLS-1$
                                                                         + JAXRS_VARIABLE_PATTERN_WSP
                                                                         + JAXRS_VARIABLE_PATTERN_REGEX
                                                                         + JAXRS_VARIABLE_PATTERN_WSP
                                                                         + ")?\\}"; //$NON-NLS-1$
    private static final Pattern JAXRS_VARIABLE_PATTERN          =
                                                                     Pattern
                                                                         .compile(JAXRS_VARIABLE_PATTERN_PARAM);

    protected int                numOfNonDefaultRegexes;

    /**
     * Create a processor without a template
     */
    public JaxRsUriTemplateProcessor() {
        super();
        numOfNonDefaultRegexes = 0;
    }

    /**
     * Create an processor with the provided template. The
     * {@link #compile(String)} method is called on the provided template.
     * 
     * @param uriTemplate the template that this processor is associated with
     */
    public JaxRsUriTemplateProcessor(String template) {
        this();
        compile(template);
    }

    @Override
    protected void reset() {
        super.reset();
        numOfNonDefaultRegexes = 0;
    }

    @Override
    public final void compile(String template) {
        compile(template, new JaxRsPatternBuilder(this));
    }

    /**
     * Compile the provided uri template and pass compilation events to the
     * provided {@link JaxRsCompilationHandler}.
     * 
     * @param template the template to compile
     * @param handler the {@link JaxRsCompilationHandler} that will receive
     *            compilation events
     */
    public static void compile(String template, JaxRsCompilationHandler handler) {
        if (template == null) {
            throw new NullPointerException("uriTemplate"); //$NON-NLS-1$
        }
        if (handler == null) {
            throw new NullPointerException("handler"); //$NON-NLS-1$
        }

        int start = 0;
        String literal = ""; //$NON-NLS-1$

        // fire start
        handler.startCompile(template);

        // search for JAX-RS style variables
        Matcher matcher = JAXRS_VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            // get the literal part which is the characters up to the variable
            literal = template.substring(start, matcher.start());
            start = matcher.end();
            // fire literal
            handler.literal(literal);

            // capturing group 1 is the variable name
            String variable = matcher.group(1);
            // capturing group 2 is the regular expression (will be null if it
            // doesn't exist)
            String regex = matcher.group(2);
            // fire variable
            handler.variable(variable, regex);
        }
        // get the trailing literal part
        literal = template.substring(start);

        // fire end
        handler.endCompile(literal);
    }

    /**
     * Expand the provided template using the provided values. Regular
     * expressions of variables in the template are ignored. All variables
     * defined in the template must have a value.
     * 
     * @param template the uri template to expand
     * @param values a map with the values of the variables
     * @return an expanded uri using the supplied variable values
     */
    public static String expand(String template, MultivaluedMap<String, String> values) {
        if (template == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        expand(template, values, result);
        return result.toString();
    }

    /**
     * Expand the provided template using the provided values. Regular
     * expressions of variables in the template are ignored. All variables
     * defined in the template must have a value.
     * 
     * @param uriTemplate the uri template to expand
     * @param values a map with the values of the variables
     * @param out the output for the expansion
     */
    public static void expand(String template,
                              MultivaluedMap<String, String> values,
                              StringBuilder out) {
        if (template == null) {
            return;
        }
        JaxRsTemplateExpander expander = new JaxRsTemplateExpander(values, out);
        compile(template, expander);
    }

    @Override
    public int compareTo(UriTemplateProcessor other) {
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }
        if (!(other instanceof JaxRsUriTemplateProcessor)) {
            return result;
        }

        return compareNumOfNonDefaultRegexes((JaxRsUriTemplateProcessor)other);
    }

    private int compareNumOfNonDefaultRegexes(JaxRsUriTemplateProcessor jaxRsProcessor) {
        return (numOfNonDefaultRegexes - jaxRsProcessor.numOfNonDefaultRegexes);
    }

    /**
     * Factory method for normalized uri-templates.
     * 
     * @param uriTemplate uri-template specification
     * @return instance representing (normalized) uri-template
     * @see UriTemplateProcessor#normalizeUri(String)
     */
    public static UriTemplateProcessor newNormalizedInstance(String uriTemplate) {
        return new JaxRsUriTemplateProcessor(UriTemplateProcessor.normalizeUri(uriTemplate));
    }

    /**
     * This interface is used for receiving events during the compilation of a
     * JAX-RS uri template.
     * 
     * @see {@link JaxRsUriTemplateProcessor#compile(String, JaxRsCompilationHandler)}
     */
    public static interface JaxRsCompilationHandler extends BaseCompilationHandler {
        /**
         * Variable event.
         * 
         * @param name the name of the variable
         * @param regex the regex as it appears in the template being compiled,
         *            or <code>null</code> if no regex appeared in the template.
         */
        public void variable(String name, String regex);

    }

    /**
     * This compilation handler handles the compilation events for compiling the
     * uri template of a processor instance. It creates the regex pattern to use
     * for matching and the variables used for matching and expansion, and then
     * sets them on the processor instance.
     */
    private static class JaxRsPatternBuilder extends AbstractPatternBuilder implements
        JaxRsCompilationHandler {

        public JaxRsPatternBuilder(JaxRsUriTemplateProcessor processor) {
            super(processor);
        }

        @Override
        public void literal(String literal) {
            super.literal(UriEncoder.encodeUriTemplate(literal, true));
        }

        public void variable(String name, String regex) {
            // create a new variable
            CapturingGroup variable = createVariable(name, regex, null);
            // save it to the map of capturing variables for use during matching
            processor.variables.add(name, variable);
            // save it for use during expansion
            processor.expanders.add(variable);
            if (regex != null) {
                ((JaxRsUriTemplateProcessor)processor).numOfNonDefaultRegexes += 1;
            }
        }

        @Override
        protected void createTail() {
            // overriding the tail creation to handle the special case
            // where the complete path template is empty (i.e. it was either
            // @Path("") or @Path("/")).
            // we need this to be able to handle resources with @Path("/") that 
            // have sub-resource methods or locators.
            // if we don't do this and there's a resource with @Path("/") and a 
            // sub-resource @Path("hello"), then a request to "/hello" will not
            // be picked up becuase the default tail is "(/.*)?"
            if (processor.template.equals("")) { //$NON-NLS-1$
                // let the tail catch all characters, whether there is a / or not
                processor.tail = createVariable(TEMPLATE_TAIL_NAME, "(.*)?", null); //$NON-NLS-1$
            } else {
                // normal behavior
                super.createTail();
            }
        }
    }

    /**
     * This compilation handler is used for creating an expansion string by
     * replacing all variables with their values from the provided map.
     */
    private static class JaxRsTemplateExpander extends AbstractTemplateExpander implements
        JaxRsCompilationHandler {

        public JaxRsTemplateExpander(MultivaluedMap<String, String> values, StringBuilder out) {
            super(values, out);
        }

        public void variable(String name, String regex) {
            if (values == null) {
                throw new NullPointerException(Messages.getMessage("variableNotSuppliedAValue", name)); //$NON-NLS-1$
            }
            String valueStr = values.getFirst(name);
            if (valueStr == null) {
                throw new NullPointerException(Messages.getMessage("variableNotSuppliedAValue", name)); //$NON-NLS-1$
            }
            out.append(valueStr);
        }
    }

}
