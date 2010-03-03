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

package org.apache.wink.server.internal.providers.entity.html;

/**
 * This class will be used for the default HTML representation. The method
 * defines the HTML code that will be used to start and end the expandable tag.
 */
public final class ExpandableSectionHelper {

    /**
     * private constructor to avoid instantiation.
     */
    private ExpandableSectionHelper() {
    }

    /**
     * The method defines the HTML code that will be used to start the
     * expandable tag.
     * 
     * @param title the title of the section
     * @param id the id of the section
     * @param isStateCollapsed the state of the section (true=collapsed,
     *            false=expanded)
     * @return the HTML code of the section header
     */
    public static String getFormHeaderHtml(String title, String id, boolean isStateCollapsed) {
        StringBuilder stringBuilder = new StringBuilder();
        String collapseStr = "-"; //$NON-NLS-1$
        String expandStr = "+"; //$NON-NLS-1$

        stringBuilder.append("<script type='text/javascript' >").append('\n') //$NON-NLS-1$
            .append("var collapseExpand").append(id).append(" = new CollapseExpand('").append(id) //$NON-NLS-1$ //$NON-NLS-2$
            .append("');").append('\n').append("</script>").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // the section header
        stringBuilder
            .append("<table class='wide-table") //$NON-NLS-1$
            .append("' margin-top='0' cellpadding='0' cellspacing='0'>\n") //$NON-NLS-1$
            .append("<tr>\n") //$NON-NLS-1$
            .append("<td >\n") //$NON-NLS-1$
            .append("<table class='wide-table expandable-form-header'>\n") //$NON-NLS-1$
            .append("<tr>\n") //$NON-NLS-1$
            .append("<td style='{CURSOR: pointer};' class='portlet-expand-button minimum-icon-size'>") //$NON-NLS-1$
            .append("<div id='").append(id).append("_div' onclick=\"collapseExpand").append(id) //$NON-NLS-1$ //$NON-NLS-2$
            .append(".collapseExpand();\">").append(isStateCollapsed ? expandStr : collapseStr) //$NON-NLS-1$
            .append("</div>").append("</td>\n").append("<td>&nbsp;&nbsp;").append(title) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            .append("</td>\n").append("</tr>").append("</table>\n").append("</td>\n") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            .append("</tr>\n").append("</table>\n"); //$NON-NLS-1$ //$NON-NLS-2$

        // the collapsed/expanded table
        stringBuilder.append("<table id='").append(id).append("' style='").append(isStateCollapsed //$NON-NLS-1$ //$NON-NLS-2$
            ? "display: none" : "display: block") //$NON-NLS-1$ //$NON-NLS-2$
            .append("' class='wide-table' cellpadding=0 cellspacing=0>\n") //$NON-NLS-1$
            .append("<td class='form-area-width'>\n"); //$NON-NLS-1$

        return stringBuilder.toString();
    }

    /**
     * The method defines the HTML code that will be used to end the expandable
     * tag.
     * 
     * @param id the id of the section
     * @param isStateCollapsed the state of the section (true=collapsed,
     *            false=expanded)
     * @return HTML code the HTML code of the section footer
     */
    public static String getFormFooterHtml(String id, boolean isStateCollapsed) {

        StringBuilder stringBuilder = new StringBuilder();

        // end of collapsed/expended table
        stringBuilder.append("</td>\n").append("</table>\n"); //$NON-NLS-1$ //$NON-NLS-2$

        // collapse if needed
        if (isStateCollapsed) {
            stringBuilder.append("<script type='text/javascript' >\n").append("collapseExpand") //$NON-NLS-1$ //$NON-NLS-2$
                .append(id).append(".collapse();\n").append("</script>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return stringBuilder.toString();
    }
}
