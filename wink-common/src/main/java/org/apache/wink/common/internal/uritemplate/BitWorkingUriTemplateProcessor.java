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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.common.internal.utils.StringUtils;

/**
 * <a href="http://bitworking.org/projects/URI-Templates/">BitWorking</a> style
 * template processor for compiling, matching and expanding URI templates
 */
public class BitWorkingUriTemplateProcessor extends UriTemplateProcessor {

    /*
     * op = 1ALPHA arg =(reserved / unreserved / pct-encoded) varname = (ALPHA /
     * DIGIT)(ALPHA / DIGIT / "." / "_" / "-" ) vardefault =(unreserved /
     * pct-encoded) var = varname [ "=" vardefault ] vars = var [("," var) ]
     * operator = "-" op "|" arg "|" vars expansion = "{" ( var / operator ) "}"
     */
    private static final String  HEX                         = "[0-9A-Fa-f]";                       //$NON-NLS-1$
    private static final String  RESERVED                    = "[;/?:@&=+$,]";                      //$NON-NLS-1$
    private static final String  UNRESERVED                  = "[\\w\\.!~*'()-]";                   //$NON-NLS-1$
    private static final String  PCT_ENCONDED                = "(?:%" + HEX + HEX + ")";            //$NON-NLS-1$ //$NON-NLS-2$
    private static final String  ALPHA                       = "[a-zA-Z]";                          //$NON-NLS-1$

    private static final String  BITWORKING_OP               = "(" + ALPHA + "+)";                  //$NON-NLS-1$ //$NON-NLS-2$
    private static final String  BITWORKING_ARG              = "((?:" + RESERVED //$NON-NLS-1$
                                                                 + "|" //$NON-NLS-1$
                                                                 + UNRESERVED
                                                                 + "|" //$NON-NLS-1$
                                                                 + PCT_ENCONDED
                                                                 + ")*)";                           //$NON-NLS-1$
    private static final String  BITWORKING_VARNAME          = "\\w[\\w\\.-]*";                     //$NON-NLS-1$
    private static final String  BITWORKING_VARDEFAULT       = "((?:" + UNRESERVED //$NON-NLS-1$
                                                                 + "|" //$NON-NLS-1$
                                                                 + PCT_ENCONDED
                                                                 + ")*)";                           //$NON-NLS-1$
    private static final String  BITWORKING_VAR              = "(" + BITWORKING_VARNAME //$NON-NLS-1$
                                                                 + ")(?:=" //$NON-NLS-1$
                                                                 + BITWORKING_VARDEFAULT
                                                                 + ")?";                            //$NON-NLS-1$
    private static final String  BITWORKING_VARS             = "(" + BITWORKING_VAR //$NON-NLS-1$
                                                                 + "(?:," //$NON-NLS-1$
                                                                 + BITWORKING_VAR
                                                                 + ")*)";                           //$NON-NLS-1$
    private static final String  BITWORKING_OPERATOR         = "(?:-" + BITWORKING_OP //$NON-NLS-1$
                                                                 + "[|]" //$NON-NLS-1$
                                                                 + BITWORKING_ARG
                                                                 + "[|]" //$NON-NLS-1$
                                                                 + BITWORKING_VARS
                                                                 + ")";                             //$NON-NLS-1$
    private static final String  BITWORKING_EXPANSION        = "\\{(?:" + BITWORKING_VAR //$NON-NLS-1$
                                                                 + "|" //$NON-NLS-1$
                                                                 + BITWORKING_OPERATOR
                                                                 + ")\\}";                          //$NON-NLS-1$
    private static final Pattern BITWORKING_VARIABLE_PATTERN =
                                                                 Pattern
                                                                     .compile(BITWORKING_EXPANSION);

    /**
     * Create a processor without a template
     */
    public BitWorkingUriTemplateProcessor() {
        super();
    }

    /**
     * Create an processor with the provided template. The
     * {@link #compile(String)} method is called on the provided template.
     * 
     * @param template the template that this processor is associated with
     */
    public BitWorkingUriTemplateProcessor(String template) {
        this();
        compile(template);
    }

    @Override
    public final void compile(String uriTemplate) {
        compile(uriTemplate, new BitWorkingPatternBuilder(this));
    }

    /**
     * Compile the provided uri template and pass compilation events to the
     * provided {@link BitWorkingCompilationHandler}.
     * 
     * @param template the template to compile
     * @param handler the CompilationHandler that will receive compilation
     *            events
     */
    public static void compile(String template, BitWorkingCompilationHandler handler) {
        if (template == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "template")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (handler == null) {
            throw new NullPointerException(Messages.getMessage("variableIsNull", "handler")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        int start = 0;
        String literal = ""; //$NON-NLS-1$

        // fire start
        handler.startCompile(template);

        Matcher matcher = BITWORKING_VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            // get the literal part which is the characters up to the variable
            literal = template.substring(start, matcher.start());
            start = matcher.end();

            // fire literal
            handler.literal(literal);

            // get the different parts according to the following:
            // group 1: variable name
            // group 2: variable default value
            // group 3: operator name
            // group 4: operator arg
            // group 5: operator vars
            // if a variable was matched then all the operator groups will be
            // null
            // if an operator was matched then all the variable groups will be
            // null

            String variable = matcher.group(1);
            if (variable != null) {
                // variable
                String defaultValue = matcher.group(2);
                // fire variable
                handler.variable(variable, defaultValue);
            } else {
                // operator
                String operator = matcher.group(3);
                String arg = matcher.group(4);
                String vars = matcher.group(5);

                // extract the variable names of the operator
                String[] arrayVars = StringUtils.fastSplit(vars, ","); //$NON-NLS-1$
                Map<String, String> varsMap = new LinkedHashMap<String, String>();
                for (String var : arrayVars) {
                    String defaultValue = null;
                    int index = var.indexOf('=');
                    // is there a default value?
                    if (index != -1) {
                        defaultValue = var.substring(index + 1);
                        var = var.substring(0, index);
                    }
                    varsMap.put(var, defaultValue);
                }

                // fire operator
                handler.operator(operator, arg, varsMap);
            }
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
     * @param template the uri template to expand
     * @param values a map with the values of the variables
     * @param out the output for the expansion
     */
    public static void expand(String template,
                              MultivaluedMap<String, String> values,
                              StringBuilder out) {
        if (template == null) {
            return;
        }
        BitWorkingTemplateExpander expander = new BitWorkingTemplateExpander(values, out);
        compile(template, expander);
    }

    /**
     * Factory method for normalized uri-templates.
     * 
     * @param uriTemplate uri-template specification
     * @return instance representing (normalized) uri-template
     * @see UriTemplateProcessor#normalizeUri(String)
     */
    public static UriTemplateProcessor newNormalizedInstance(String uriTemplate) {
        return new BitWorkingUriTemplateProcessor(UriTemplateProcessor.normalizeUri(uriTemplate));
    }

    /**
     * This interface is used for receiving events during the compilation of a
     * uri template.
     * 
     * @see {@link BitWorkingUriTemplateProcessor#compile(String, BitWorkingCompilationHandler)}
     */
    public static interface BitWorkingCompilationHandler extends BaseCompilationHandler {
        /**
         * Variable event.
         * 
         * @param name the name of the variable
         * @param defaultValue the default value of the variable, or null
         */
        public void variable(String name, String defaultValue);

        /**
         * Operator event.
         * 
         * @param name the name of the operator
         * @param arg the argument of the operator
         * @param vars a map of variables and their default values
         */
        public void operator(String name, String arg, Map<String, String> vars);

    }

    /**
     * This compilation handler handles the compilation events for compiling the
     * uri template of a processor instance. It creates the regex pattern to use
     * for matching and the variables used for matching and expansion, and then
     * sets them on the processor instance.
     */
    private static class BitWorkingPatternBuilder extends AbstractPatternBuilder implements
        BitWorkingCompilationHandler {

        public BitWorkingPatternBuilder(BitWorkingUriTemplateProcessor processor) {
            super(processor);
        }

        public void variable(String name, String defaultValue) {
            // create a new variable
            CapturingGroup variable = createVariable(name, null, defaultValue);
            // save it to the map of capturing variables for use during matching
            processor.variables.add(name, variable);
            // save it for use during expansion
            processor.expanders.add(variable);
        }

        public void operator(String name, String arg, Map<String, String> vars) {
            // get a new operator
            BitWorkingOperator operator = BitWorkingOperator.forName(name);
            if (operator == null) {
                throw new IllegalArgumentException(Messages.getMessage("unsupportedOperator", name)); //$NON-NLS-1$
            }
            // set the arg and vars of the operator
            operator.setArg(arg);
            operator.setVars(vars);
            // build it into the pattern
            operator.build(patternBuilder);
            // set its capturing group
            ++capturingGroupId;
            operator.setCapturingGroupId(capturingGroupId);

            // save it to the map of capturing variables for use during matching
            for (String var : vars.keySet()) {
                processor.variables.add(var, operator);
            }

            // save it for use during expansion
            processor.expanders.add(operator);
        }
    }

    /**
     * Represents a BitWorking operator
     */
    private abstract static class BitWorkingOperator extends CapturingGroup {
        protected String              name;
        protected String              arg;
        protected Map<String, String> vars;

        protected BitWorkingOperator(String name) {
            super();
            this.name = name;
            this.arg = null;
            this.vars = null;
        }

        public String getName() {
            return name;
        }

        public String getArg() {
            return arg;
        }

        public void setArg(String arg) {
            this.arg = arg;
        }

        public Map<String, String> getVars() {
            return vars;
        }

        public void setVars(Map<String, String> vars) {
            this.vars = vars;
        }

        /**
         * Return an instance of an operator for the specified name
         * 
         * @param name the name of the operator
         * @return an instance of an operator
         */
        public static BitWorkingOperator forName(String name) {
            if (name == null) {
                return null;
            }
            if (name.equals("neg")) { //$NON-NLS-1$
                return new Neg();
            }
            if (name.equals("opt")) { //$NON-NLS-1$
                return new Opt();
            }
            if (name.equals("prefix")) { //$NON-NLS-1$
                return new Prefix();
            }
            if (name.equals("suffix")) { //$NON-NLS-1$
                return new Suffix();
            }
            if (name.equals("list")) { //$NON-NLS-1$
                return new List();
            }
            if (name.equals("join")) { //$NON-NLS-1$
                return new Join();
            }
            return null;
        }

        /**
         * Represents the "neg" operator
         */
        private static class Neg extends BitWorkingOperator {
            public Neg() {
                super("neg"); //$NON-NLS-1$
            }

            public void build(StringBuilder builder) {
                builder.append("("); //$NON-NLS-1$
                builder.append(Pattern.quote(arg));
                builder.append(")?"); //$NON-NLS-1$
            }

            @Override
            public void onMatch(String matched,
                                MultivaluedMap<String, String> values,
                                int startIndex,
                                MultivaluedMap<String, Integer> indices) {
                // do nothing
            }

            public void expand(MultivaluedMap<String, String> values,
                               boolean encode,
                               StringBuilder builder) {
                for (String var : vars.keySet()) {
                    if (values.containsKey(var) && values.get(var).size() > 0) {
                        return;
                    }
                }
                builder.append(arg);
            }
        }

        /**
         * Represents the "opt" operator
         */
        private static class Opt extends BitWorkingOperator {
            public Opt() {
                super("opt"); //$NON-NLS-1$
            }

            public void build(StringBuilder builder) {
                builder.append("("); //$NON-NLS-1$
                builder.append(Pattern.quote(arg));
                builder.append(")?"); //$NON-NLS-1$
            }

            @Override
            public void onMatch(String matched,
                                MultivaluedMap<String, String> values,
                                int startIndex,
                                MultivaluedMap<String, Integer> indices) {
                // do nothing
            }

            public void expand(MultivaluedMap<String, String> values,
                               boolean encode,
                               StringBuilder builder) {
                for (String var : vars.keySet()) {
                    if (values.containsKey(var) && values.get(var).size() > 0) {
                        builder.append(arg);
                        return;
                    }
                }
            }
        }

        /**
         * Represents the "prefix" operator
         */
        private static class Prefix extends BitWorkingOperator {
            public Prefix() {
                super("prefix"); //$NON-NLS-1$
            }

            public void build(StringBuilder builder) {
                if (vars.size() != 1) {
                    throw new IllegalArgumentException(Messages
                        .getMessage("prefixOperatorMustHaveOnlyOneVariable")); //$NON-NLS-1$
                }

                builder.append("((?:"); //$NON-NLS-1$
                builder.append(Pattern.quote(arg));
                builder.append(REGEX0);
                builder.append(")*)"); //$NON-NLS-1$
            }

            @Override
            public void onMatch(String matched,
                                MultivaluedMap<String, String> values,
                                int startIndex,
                                MultivaluedMap<String, Integer> indices) {
                String var = vars.keySet().iterator().next();
                if (matched == null || matched.length() == 0) {
                    values.putSingle(var, null);
                    indices.putSingle(var, startIndex);
                    return;
                }

                if (!matched.startsWith(arg)) {
                    throw new IllegalArgumentException(Messages
                        .getMessage("matchedSuffixMustStartWith", arg)); //$NON-NLS-1$
                }

                // clear the previous values
                values.put(var, null);
                String[] array = StringUtils.fastSplit(matched, arg);
                // array[0] will always contain the empty string, so skip it
                for (int i = 1; i < array.length; ++i) {
                    values.add(var, array[i]);
                    indices.add(var, startIndex);
                }
            }

            public void expand(MultivaluedMap<String, String> values,
                               boolean encode,
                               StringBuilder builder) {
                // we have only one var
                String var = vars.keySet().iterator().next();
                java.util.List<String> varValues = values.get(var);
                if (varValues != null) {
                    for (String value : varValues) {
                        builder.append(arg);
                        if (encode) {
                            value = UriEncoder.encodeString(value);
                        }
                        builder.append(value);
                    }
                }
            }
        }

        /**
         * Represents the "suffix" operator
         */
        private static class Suffix extends BitWorkingOperator {
            public Suffix() {
                super("suffix"); //$NON-NLS-1$
            }

            public void build(StringBuilder builder) {
                if (vars.size() != 1) {
                    throw new IllegalArgumentException(Messages
                        .getMessage("suffixOperatorMustOnlyHaveOneVariable")); //$NON-NLS-1$
                }
                builder.append("((?:"); //$NON-NLS-1$
                builder.append(REGEX0);
                builder.append(Pattern.quote(arg));
                builder.append(")*)"); //$NON-NLS-1$
            }

            @Override
            public void onMatch(String matched,
                                MultivaluedMap<String, String> values,
                                int startIndex,
                                MultivaluedMap<String, Integer> indices) {
                String var = vars.keySet().iterator().next();
                if (matched == null || matched.length() == 0) {
                    values.putSingle(var, null);
                    indices.putSingle(var, startIndex);
                    return;
                }

                if (!matched.endsWith(arg)) {
                    throw new IllegalArgumentException(Messages
                        .getMessage("matchedSuffixMustEndWith", arg)); //$NON-NLS-1$
                }

                // clear the previous values
                values.put(var, null);
                String[] array = StringUtils.fastSplit(matched, arg);
                // the last element in the array will always contain the empty
                // string, so skip it
                for (int i = 0; i < array.length - 1; ++i) {
                    values.add(var, array[i]);
                    indices.add(var, startIndex);
                }
            }

            public void expand(MultivaluedMap<String, String> values,
                               boolean encode,
                               StringBuilder builder) {
                // we have only one var
                String var = vars.keySet().iterator().next();
                java.util.List<String> varValues = values.get(var);
                if (varValues != null) {
                    for (String value : varValues) {
                        if (encode) {
                            value = UriEncoder.encodeString(value);
                        }
                        builder.append(value);
                        builder.append(arg);
                    }
                }
            }
        }

        /**
         * Represents the "list" operator
         */
        private static class List extends BitWorkingOperator {
            public List() {
                super("list"); //$NON-NLS-1$
            }

            public void build(StringBuilder builder) {
                if (vars.size() != 1) {
                    throw new IllegalArgumentException(Messages
                        .getMessage("listOperatorMustHaveOnlyOneVariable")); //$NON-NLS-1$
                }
                builder.append("("); //$NON-NLS-1$
                builder.append(REGEX0);
                builder.append("(?:"); //$NON-NLS-1$
                builder.append(Pattern.quote(arg));
                builder.append(REGEX0);
                builder.append(")*)"); //$NON-NLS-1$
            }

            @Override
            public void onMatch(String matched,
                                MultivaluedMap<String, String> values,
                                int startIndex,
                                MultivaluedMap<String, Integer> indices) {
                String var = vars.keySet().iterator().next();

                if (matched == null || matched.length() == 0) {
                    values.putSingle(var, ""); //$NON-NLS-1$
                    indices.putSingle(var, startIndex);
                    return;
                }

                // clear the previous values
                values.put(var, null);
                String[] array = StringUtils.fastSplit(matched, arg);
                for (int i = 0; i < array.length; ++i) {
                    values.add(var, array[i]);
                    indices.add(var, startIndex);
                }
            }

            public void expand(MultivaluedMap<String, String> values,
                               boolean encode,
                               StringBuilder builder) {
                // we have only one var
                String var = vars.keySet().iterator().next();
                String delim = ""; //$NON-NLS-1$
                java.util.List<String> varValues = values.get(var);
                if (varValues != null) {
                    for (String value : varValues) {
                        builder.append(delim);
                        if (encode) {
                            value = UriEncoder.encodeString(value);
                        }
                        builder.append(value);
                        delim = arg;
                    }
                }
            }
        }

        /**
         * Represents the "join" operator
         */
        private static class Join extends BitWorkingOperator {
            public Join() {
                super("join"); //$NON-NLS-1$
            }

            public void build(StringBuilder builder) {
                String orSign = ""; //$NON-NLS-1$
                StringBuilder keysAndValuesPattern = new StringBuilder();
                keysAndValuesPattern.append("(?:"); //$NON-NLS-1$
                for (String var : vars.keySet()) {
                    keysAndValuesPattern.append(orSign);
                    keysAndValuesPattern.append("(?:"); //$NON-NLS-1$
                    keysAndValuesPattern.append(var);
                    keysAndValuesPattern.append("="); //$NON-NLS-1$
                    keysAndValuesPattern.append(REGEX0);
                    keysAndValuesPattern.append(")"); //$NON-NLS-1$
                    orSign = "|"; //$NON-NLS-1$
                }
                keysAndValuesPattern.append(")"); //$NON-NLS-1$

                // with a capturing group
                builder.append("("); //$NON-NLS-1$
                builder.append(keysAndValuesPattern.toString());
                builder.append("(?:"); //$NON-NLS-1$
                builder.append(Pattern.quote(arg));
                builder.append(keysAndValuesPattern.toString());
                builder.append(")*)?"); //$NON-NLS-1$
            }

            @Override
            public void onMatch(String matched,
                                MultivaluedMap<String, String> values,
                                int startIndex,
                                MultivaluedMap<String, Integer> indices) {
                // extract all the keys and values and prepare a temporary map
                Map<String, String> extractedValues = new HashMap<String, String>();
                if (matched != null) {
                    String[] array = StringUtils.fastSplit(matched, arg);
                    for (int i = 0; i < array.length; ++i) {
                        String var = array[i];
                        String value = ""; //$NON-NLS-1$
                        int index = var.indexOf('=');
                        if (index != -1) {
                            value = var.substring(index + 1);
                            var = var.substring(0, index);
                        }
                        extractedValues.put(var, value);
                    }
                }

                // add all the values to the output multivalued map.
                // if a certain key did not have a value, then null will added
                for (String key : vars.keySet()) {
                    values.putSingle(key, extractedValues.get(key));
                    indices.putSingle(key, startIndex);
                }
            }

            public void expand(MultivaluedMap<String, String> values,
                               boolean encode,
                               StringBuilder builder) {
                String delim = ""; //$NON-NLS-1$
                for (String var : vars.keySet()) {
                    java.util.List<String> varValues = values.get(var);
                    String value = ""; //$NON-NLS-1$
                    if (varValues != null) {
                        if (varValues.size() > 1) {
                            throw new IllegalArgumentException(Messages
                                .getMessage("variableContainsMoreThanOneValueForJoinOperator", var)); //$NON-NLS-1$
                        }
                        if (varValues.size() == 1) {
                            value = varValues.get(0);
                            if (value == null) {
                                continue;
                            }
                            builder.append(delim);
                            builder.append(var);
                            builder.append("="); //$NON-NLS-1$
                            if (encode) {
                                value = UriEncoder.encodeString(value);
                            }
                            builder.append(value);
                            delim = arg;
                        }
                    }
                }
            }
        }
    }

    /**
     * This compilation handler is used for creating an expansion string by
     * replacing all variables with their values from the provided map.
     */
    private static class BitWorkingTemplateExpander extends AbstractTemplateExpander implements
        BitWorkingCompilationHandler {

        public BitWorkingTemplateExpander(MultivaluedMap<String, String> values, StringBuilder out) {
            super(values, out);
        }

        public void variable(String name, String defaultValue) {
            if (values == null) {
                throw new NullPointerException(Messages.getMessage("variableIsNull", name)); //$NON-NLS-1$
            }
            Variable expander = new Variable(name, null, defaultValue);
            expander.expand(values, false, out);
        }

        public void operator(String name, String arg, Map<String, String> vars) {
            if (values == null) {
                throw new NullPointerException(Messages.getMessage("variableIsNull", name)); //$NON-NLS-1$
            }
            BitWorkingOperator expander = BitWorkingOperator.forName(name);
            if (expander == null) {
                throw new IllegalArgumentException(Messages.getMessage("unsupportedOperator", name)); //$NON-NLS-1$
            }
            expander.expand(values, false, out);
        }

    }

}
