/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.support.view;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.response.dispatch.ViewDispatcher;
import com.aspectran.core.activity.response.dispatch.ViewDispatcherException;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.component.template.TemplateDataMap;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * The Class FreeMarkerViewDispatcher.
 *
 * <p>Created: 2016. 1. 27.</p>
 *
 * @since 2.0.0
 */
public class FreeMarkerViewDispatcher implements ViewDispatcher {

    private static final Log log = LogFactory.getLog(FreeMarkerViewDispatcher.class);

    private static final boolean debugEnabled = log.isDebugEnabled();

    private Configuration configuration;

    private String prefix;

    private String suffix;

    public FreeMarkerViewDispatcher(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Sets the prefix for the template name.
     *
     * @param prefix the new prefix for the template name
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Sets the suffix for the template name.
     *
     * @param suffix the new suffix for the template name
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void dispatch(Activity activity, DispatchResponseRule dispatchResponseRule) throws ViewDispatcherException {
        String dispatchName = null;

        try {
            dispatchName = dispatchResponseRule.getName(activity);
            if (dispatchName == null) {
                throw new IllegalArgumentException("No specified dispatch name");
            }

            if (prefix != null && suffix != null) {
                dispatchName = prefix + dispatchName + suffix;
            } else if (prefix != null) {
                dispatchName = prefix + dispatchName;
            } else if (suffix != null) {
                dispatchName = dispatchName + suffix;
            }

            ResponseAdapter responseAdapter = activity.getResponseAdapter();

            String contentType = dispatchResponseRule.getContentType();
            if (contentType != null) {
                responseAdapter.setContentType(contentType);
            }

            String encoding = dispatchResponseRule.getEncoding();
            if (encoding != null) {
                responseAdapter.setEncoding(encoding);
            } else {
                encoding = activity.getTranslet().getResponseEncoding();
                if (encoding != null) {
                    responseAdapter.setEncoding(encoding);
                }
            }

            TemplateDataMap model = new TemplateDataMap(activity);
            Template template = configuration.getTemplate(dispatchName);
            template.process(model, responseAdapter.getWriter());

            if (debugEnabled) {
                log.debug("Dispatch to FreeMarker template [" + dispatchName + "]");
            }
        } catch (Exception e) {
            throw new ViewDispatcherException("Failed to dispatch to FreeMarker template " +
                    dispatchResponseRule.toString(this, dispatchName), e);
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
