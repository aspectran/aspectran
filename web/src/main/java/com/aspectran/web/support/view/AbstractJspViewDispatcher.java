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
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.dispatch.AbstractViewDispatcher;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.dispatch.ViewDispatcherException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.activity.request.ActivityRequestWrapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Abstract base class for JSP-based view dispatchers.
 */
public abstract class AbstractJspViewDispatcher extends AbstractViewDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(AbstractJspViewDispatcher.class);

    protected static final String DEFAULT_CONTENT_TYPE = "text/html;charset=UTF-8";

    @Override
    public void dispatch(Activity activity, DispatchRule dispatchRule) throws ViewDispatcherException {
        String viewName = null;
        try {
            viewName = resolveViewName(dispatchRule, activity);

            RequestAdapter requestAdapter = activity.getRequestAdapter();
            ResponseAdapter responseAdapter = activity.getResponseAdapter();

            // Set Content-Type
            String contentType = dispatchRule.getContentType();
            if (contentType == null) {
                contentType = getContentType();
            }
            if (contentType != null) {
                responseAdapter.setContentType(contentType);
            } else {
                responseAdapter.setContentType(DEFAULT_CONTENT_TYPE);
            }

            // Set Encoding
            String encoding = dispatchRule.getEncoding();
            if (encoding == null && responseAdapter.getEncoding() == null) {
                encoding = activity.getTranslet().getDefinitiveResponseEncoding();
            }
            if (encoding != null) {
                responseAdapter.setEncoding(encoding);
            }

            // Save Model Attributes
            ProcessResult processResult = activity.getProcessResult();
            DispatchResponse.saveAttributes(requestAdapter, processResult);

            HttpServletResponse response = responseAdapter.getAdaptee();
            if (response.isCommitted()) {
                response.reset();
            }

            // Delegate to subclass for actual dispatch
            doDispatch(activity, response, viewName);

        } catch (Exception e) {
            throw new ViewDispatcherException("Failed to dispatch to JSP " +
                    dispatchRule.toString(this, viewName), e);
        }
    }

    /**
     * Subclasses must implement this method to perform the actual dispatching logic.
     * @param activity the current activity
     * @param response the HTTP servlet response
     * @param viewName the resolved view name to dispatch to
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected abstract void doDispatch(Activity activity, HttpServletResponse response, String viewName)
            throws ServletException, IOException;

    protected final void forward(@NonNull Activity activity, HttpServletResponse response, String path)
            throws ServletException, IOException {
        ActivityRequestWrapper requestWrapper = new ActivityRequestWrapper(activity.getRequestAdapter());
        RequestDispatcher requestDispatcher = requestWrapper.getRequestDispatcher(path);
        if (requestDispatcher == null) {
            throw new ServletException("No request dispatcher available for view name '" + path + "'");
        }
        requestDispatcher.forward(requestWrapper, response);

        if (response.getStatus() == 404) {
            logger.warn("Resource file {} not found", path);
        }
    }

}
