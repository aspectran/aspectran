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
package com.aspectran.pebble.view;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.response.dispatch.ViewDispatcher;
import com.aspectran.core.activity.response.dispatch.ViewDispatcherException;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.component.template.TemplateModel;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PebbleViewDispatcher.class);

    private final PebbleEngine pebbleEngine;

    private String contentType;

    private String prefix;

    private String suffix;

    public PebbleViewDispatcher(PebbleEngine pebbleEngine) {
        this.pebbleEngine = pebbleEngine;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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
    public void dispatch(Activity activity, DispatchRule dispatchRule) throws ViewDispatcherException {
        String dispatchName = null;

        try {
            dispatchName = dispatchRule.getName();
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

            String contentType = dispatchRule.getContentType();
            if (contentType != null) {
                responseAdapter.setContentType(contentType);
            }

            String encoding = dispatchRule.getEncoding();
            if (encoding != null) {
                responseAdapter.setEncoding(encoding);
            } else if (responseAdapter.getEncoding() == null) {
                encoding = activity.getTranslet().getIntendedResponseEncoding();
                if (encoding != null) {
                    responseAdapter.setEncoding(encoding);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Dispatching to Pebble template [" + dispatchName + "]");
            }

            TemplateModel model = new TemplateModel(activity);
            PebbleTemplate compiledTemplate = pebbleEngine.getTemplate(dispatchName);
            compiledTemplate.evaluate(responseAdapter.getWriter(), model);
        } catch (Exception e) {
            throw new ViewDispatcherException("Failed to dispatch to Pebble template " +
                    dispatchRule.toString(this, dispatchName), e);
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
