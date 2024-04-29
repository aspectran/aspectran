/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.dispatch.ViewDispatcher;
import com.aspectran.core.activity.response.dispatch.ViewDispatcherException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.activity.request.ActivityRequestWrapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JSP or other web resource integration.
 * Sends the model produced by Aspectran's internal activity
 * to the JSP to render the final view page.
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class JspViewDispatcher implements ViewDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(JspViewDispatcher.class);

    private static final String DEFAULT_CONTENT_TYPE = "text/html;charset=ISO-8859-1";

    private String contentType;

    private String prefix;

    private String suffix;

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets the prefix for the template name.
     * @param prefix the new prefix for the template name
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Sets the suffix for the template name.
     * @param suffix the new suffix for the template name
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void dispatch(Activity activity, DispatchRule dispatchRule) throws ViewDispatcherException {
        String resource = null;
        try {
            resource = dispatchRule.getName(activity);
            if (resource == null) {
                throw new IllegalArgumentException("No specified dispatch name");
            }

            if (prefix != null && suffix != null) {
                resource = prefix + resource + suffix;
            } else if (prefix != null) {
                resource = prefix + resource;
            } else if (suffix != null) {
                resource = resource + suffix;
            }

            RequestAdapter requestAdapter = activity.getRequestAdapter();
            ResponseAdapter responseAdapter = activity.getResponseAdapter();

            String contentType = dispatchRule.getContentType();
            if (contentType == null) {
                contentType = getContentType();
            }
            if (contentType != null) {
                responseAdapter.setContentType(contentType);
            } else {
                responseAdapter.setContentType(DEFAULT_CONTENT_TYPE);
            }

            String encoding = dispatchRule.getEncoding();
            if (encoding == null && responseAdapter.getEncoding() == null) {
                encoding = activity.getTranslet().getIntendedResponseEncoding();
            }
            if (encoding != null) {
                responseAdapter.setEncoding(encoding);
            }

            ProcessResult processResult = activity.getProcessResult();
            DispatchResponse.saveAttributes(requestAdapter, processResult);

            HttpServletResponse response = responseAdapter.getAdaptee();
            if (response.isCommitted()) {
                response.reset();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Dispatching to " + resource);
            }

            ActivityRequestWrapper requestWrapper = new ActivityRequestWrapper(activity.getRequestAdapter());
            RequestDispatcher requestDispatcher = requestWrapper.getRequestDispatcher(resource);
            requestDispatcher.forward(requestWrapper, response);

            if (response.getStatus() == 404) {
                logger.warn("Resource file [" + resource + "] not found");
            }
        } catch (Exception e) {
            activity.setRaisedException(e);
            throw new ViewDispatcherException("Failed to dispatch to JSP " +
                    dispatchRule.toString(this, resource), e);
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", super.toString());
        tsb.append("defaultContentType", contentType);
        tsb.append("prefix", prefix);
        tsb.append("suffix", suffix);
        return tsb.toString();
    }

}
