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
package com.aspectran.freemarker.view;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.response.dispatch.AbstractViewDispatcher;
import com.aspectran.core.activity.response.dispatch.ViewDispatcherException;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.freemarker.FreeMarkerTemplateEngine;
import com.aspectran.utils.Assert;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import freemarker.template.Configuration;

/**
 * The Class FreeMarkerViewDispatcher.
 *
 * <p>Created: 2016. 1. 27.</p>
 *
 * @since 2.0.0
 */
public class FreeMarkerViewDispatcher extends AbstractViewDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(FreeMarkerViewDispatcher.class);

    private final Configuration configuration;

    public FreeMarkerViewDispatcher(FreeMarkerTemplateEngine templateEngine) {
        Assert.notNull(templateEngine, "templateEngine must not be null");
        this.configuration = templateEngine.getConfiguration();
    }

    public FreeMarkerViewDispatcher(Configuration configuration) {
        Assert.notNull(configuration, "configuration must not be null");
        this.configuration = configuration;
    }

    @Override
    public void dispatch(Activity activity, DispatchRule dispatchRule) throws ViewDispatcherException {
        String viewName = null;
        try {
            viewName = resolveViewName(dispatchRule, activity);

            ResponseAdapter responseAdapter = activity.getResponseAdapter();

            String contentType = dispatchRule.getContentType();
            if (contentType == null) {
                contentType = getContentType();
            }
            if (contentType != null) {
                responseAdapter.setContentType(contentType);
            }

            String encoding = dispatchRule.getEncoding();
            if (encoding == null && responseAdapter.getEncoding() == null) {
                encoding = activity.getTranslet().getIntendedResponseEncoding();
            }
            if (encoding != null) {
                responseAdapter.setEncoding(encoding);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Dispatching to FreeMarker template [" + viewName + "]");
            }

            FreeMarkerTemplateEngine.process(configuration, viewName, activity);
        } catch (Exception e) {
            activity.setRaisedException(e);
            throw new ViewDispatcherException("Failed to dispatch to FreeMarker template " +
                    dispatchRule.toString(this, viewName), e);
        }
    }

}
