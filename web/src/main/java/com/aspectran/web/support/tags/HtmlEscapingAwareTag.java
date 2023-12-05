/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.lang.Nullable;
import com.aspectran.web.support.util.HtmlUtils;

import static com.aspectran.web.support.tags.HtmlEscapeTag.DEFAULT_HTML_ESCAPE_SETTING_NAME;

/**
 * Superclass for tags that output content that might get HTML-escaped.
 *
 * <p>Provides a "htmlEscape" property for explicitly specifying whether to
 * apply HTML escaping. If not set, a page-level default (e.g. from the
 * HtmlEscapeTag) or a setting value injected by Aspect (the
 * "defaultHtmlEscape" setting name) is used.</p>
 */
public abstract class HtmlEscapingAwareTag extends CurrentActivityAwareTag {

    private static final long serialVersionUID = 1853699535371633474L;

    @Nullable
    private Boolean htmlEscape;

    /**
     * Set HTML escaping for this tag, as boolean value.
     * Overrides the default HTML escaping setting for the current activity.
     *
     * @see HtmlEscapeTag#setDefaultHtmlEscape
     */
    public void setHtmlEscape(boolean htmlEscape) {
        this.htmlEscape = htmlEscape;
    }

    /**
     * Return the HTML escaping setting for this tag,
     * or the default setting if not overridden.
     *
     * @see #isDefaultHtmlEscape()
     */
    protected boolean isHtmlEscape() {
        if (this.htmlEscape != null) {
            return this.htmlEscape;
        } else {
            return isDefaultHtmlEscape();
        }
    }

    /**
     * Return the applicable default HTML escape setting for this tag.
     * <p>The default implementation checks the RequestContext's setting,
     * falling back to {@code false} in case of no explicit default given.</p>
     */
    protected boolean isDefaultHtmlEscape() {
        Object value = getCurrentActivity().getSetting(DEFAULT_HTML_ESCAPE_SETTING_NAME);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean)value;
        } else {
            return Boolean.parseBoolean(value.toString());
        }
    }

    /**
     * HTML-encodes the given String, only if the "htmlEscape" setting is enabled.
     * <p>The response encoding will be taken into account if the
     * "responseEncodedHtmlEscape" setting is enabled as well.
     * @param content the String to escape
     * @return the escaped String
     */
    protected String htmlEscape(String content) {
        String out = content;
        if (isHtmlEscape()) {
            String encoding = getCharacterEncoding();
            if (encoding != null) {
                out = HtmlUtils.htmlEscape(content, encoding);
            } else {
                out = HtmlUtils.htmlEscape(content);
            }
        }
        return out;
    }

    private String getCharacterEncoding() {
        String encoding = null;
        ResponseAdapter responseAdapter = getCurrentActivity().getResponseAdapter();
        if (responseAdapter.getEncoding() == null) {
            encoding = getCurrentActivity().getTranslet().getIntendedResponseEncoding();
        }
        return encoding;
    }

}
