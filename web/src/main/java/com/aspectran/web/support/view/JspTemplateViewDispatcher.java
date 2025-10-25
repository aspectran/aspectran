/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.web.support.view;

import com.aspectran.core.activity.Activity;
import com.aspectran.utils.ToStringBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A JSP view dispatcher that implements the Composite View pattern.
 * <p>This dispatcher forwards the request to a single main template page (the layout).
 * The path to the actual content page is passed as a request attribute. The main
 * template is then responsible for including the content page, allowing for a
 * consistent layout across multiple views.
 * </p>
 * <p>For example, the main template can use {@code <jsp:include page="${includePage}"/>}
 * to render the content, where "includePage" is the configured {@code includePageKey}.
 * </p>
 *
 * <p>Created: 2019. 02. 18</p>
 */
public class JspTemplateViewDispatcher extends AbstractJspViewDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(JspTemplateViewDispatcher.class);

    private String template;

    private String includePageKey;

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setIncludePageKey(String includePageKey) {
        this.includePageKey = includePageKey;
    }

    @Override
    protected void doDispatch(Activity activity, HttpServletResponse response, String viewName)
            throws ServletException, IOException {
        if (template == null) {
            throw new IllegalArgumentException("No specified template page");
        }
        if (includePageKey == null) {
            throw new IllegalArgumentException("No attribute name to specify the include page name");
        }

        activity.getRequestAdapter().setAttribute(includePageKey, viewName);

        if (logger.isDebugEnabled()) {
            logger.debug("Dispatching to {} for {}", template, viewName);
        }
        forward(activity, response, template);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", super.toString());
        tsb.append("defaultContentType", getContentType());
        tsb.append("prefix", getPrefix());
        tsb.append("suffix", getSuffix());
        tsb.append("template", template);
        tsb.append("includePageKey", includePageKey);
        return tsb.toString();
    }

}
