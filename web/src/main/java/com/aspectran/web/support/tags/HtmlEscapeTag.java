/*
 * Copyright (c) 2008-2022 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.web.support.tags;

import jakarta.servlet.jsp.JspException;

/**
 * The {@code <htmlEscape>} tag sets default HTML escape value for the current
 * page. The actual value  can be overridden by escaping-aware tags.
 * The default is "false".
 *
 * <p>Note: You can also set a "defaultHtmlEscape" web.xml context-param.
 * A page-level setting overrides a context-param.
 *
 * <table>
 * <caption>Attribute Summary</caption>
 * <thead>
 * <tr>
 * <th>Attribute</th>
 * <th>Required?</th>
 * <th>Runtime Expression?</th>
 * <th>Description</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>defaultHtmlEscape</td>
 * <td>true</td>
 * <td>true</td>
 * <td>Set the default value for HTML escaping, to be put into the current
 * PageContext.</td>
 * </tr>
 * </tbody>
 * </table>
 */
public class HtmlEscapeTag extends CurrentActivityAwareTag {

    /**
     * HTML escape parameter at the servlet context level
     * (i.e. a context-param in {@code web.xml}): "defaultHtmlEscape".
     */
    public static final String DEFAULT_HTML_ESCAPE_SETTING_NAME = "defaultHtmlEscape";

    private boolean defaultHtmlEscape;

    /**
     * Set the default value for HTML escaping,
     * to be put into the current PageContext.
     */
    public void setDefaultHtmlEscape(boolean defaultHtmlEscape) {
        this.defaultHtmlEscape = defaultHtmlEscape;
    }

    @Override
    protected int doStartTagInternal() throws JspException {
        getCurrentActivity().putSetting(DEFAULT_HTML_ESCAPE_SETTING_NAME, defaultHtmlEscape);
        return EVAL_BODY_INCLUDE;
    }

}
