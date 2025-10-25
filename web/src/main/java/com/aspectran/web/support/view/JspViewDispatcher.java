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
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A view dispatcher for integrating with JSP (JavaServer Pages) or other web
 * resources that can be dispatched to via a {@link jakarta.servlet.RequestDispatcher}.
 * <p>This dispatcher takes the results from an activity (the model) and exposes
 * them as request attributes before forwarding the request to a JSP page for
 * rendering.
 * </p>
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class JspViewDispatcher extends AbstractJspViewDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(JspViewDispatcher.class);

    @Override
    protected void doDispatch(Activity activity, HttpServletResponse response, String viewName)
            throws ServletException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Dispatching to {}", viewName);
        }
        forward(activity, response, viewName);
    }

}
