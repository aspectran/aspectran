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
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

/**
 * The Class PebbleViewDispatcher.
 *
 * <p>Created: 2016. 1. 27.</p>
 *
 * @since 2.0.0
 */
public class PebbleViewDispatcher implements ViewDispatcher {

    private static final Log log = LogFactory.getLog(PebbleViewDispatcher.class);

    private static final boolean debugEnabled = log.isDebugEnabled();

    private final PebbleEngine pebbleEngine;

    private String templateNamePrefix;

    private String templateNameSuffix;

    public PebbleViewDispatcher(PebbleEngine pebbleEngine) {
        this.pebbleEngine = pebbleEngine;
    }
    /**
     * Sets the prefix for the template name.
     *
     * @param templateNamePrefix the new prefix for the template name
     */
    public void setTemplateNamePrefix(String templateNamePrefix) {
        this.templateNamePrefix = templateNamePrefix;
    }

    /**
     * Sets the suffix for the template name.
     *
     * @param templateNameSuffix the new suffix for the template name
     */
    public void setTemplateNameSuffix(String templateNameSuffix) {
        this.templateNameSuffix = templateNameSuffix;
    }

    @Override
    public void dispatch(Activity activity, DispatchResponseRule dispatchResponseRule) throws ViewDispatcherException {
        String dispatchName = null;

        try {
            dispatchName = dispatchResponseRule.getName();
            if (dispatchName == null) {
                throw new IllegalArgumentException("No specified dispatch name");
            }

            if (templateNamePrefix != null && templateNameSuffix != null) {
                dispatchName = templateNamePrefix + dispatchName + templateNameSuffix;
            } else if (templateNamePrefix != null) {
                dispatchName = templateNamePrefix + dispatchName;
            } else if (templateNameSuffix != null) {
                dispatchName = dispatchName + templateNameSuffix;
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
            PebbleTemplate compiledTemplate = pebbleEngine.getTemplate(dispatchName);
            compiledTemplate.evaluate(responseAdapter.getWriter(), model);

            if (debugEnabled) {
                log.debug("Dispatch to Pebble template [" + dispatchName + "]");
            }
        } catch (Exception e) {
            throw new ViewDispatcherException("Failed to dispatch to Pebble template " +
                    dispatchResponseRule.toString(this, dispatchName), e);
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
