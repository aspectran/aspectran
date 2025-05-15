/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.thymeleaf.view;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.response.dispatch.AbstractViewDispatcher;
import com.aspectran.core.activity.response.dispatch.ViewDispatcherException;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.thymeleaf.ThymeleafTemplateEngine;
import com.aspectran.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.ITemplateEngine;

/**
 * The Class ThymeleafViewDispatcher.
 *
 * <p>Created: 2024. 11. 18.</p>
 *
 * @since 8.2.0
 */
public class ThymeleafViewDispatcher extends AbstractViewDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(ThymeleafViewDispatcher.class);

    private final ITemplateEngine templateEngine;

    public ThymeleafViewDispatcher(ThymeleafTemplateEngine thymeleafTemplateEngine) {
        Assert.notNull(thymeleafTemplateEngine, "thymeleafTemplateEngine must not be null");
        this.templateEngine = thymeleafTemplateEngine.getTemplateEngine();
    }

    public ThymeleafViewDispatcher(ITemplateEngine templateEngine) {
        Assert.notNull(templateEngine, "templateEngine must not be null");
        this.templateEngine = templateEngine;
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
                encoding = activity.getTranslet().getDefinitiveResponseEncoding();
            }
            if (encoding != null) {
                responseAdapter.setEncoding(encoding);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Dispatching to Thymeleaf template {}", viewName);
            }

            ThymeleafTemplateEngine.process(templateEngine, viewName, activity);
        } catch (Exception e) {
            throw new ViewDispatcherException("Failed to dispatch to Thymeleaf template " +
                    dispatchRule.toString(this, viewName), e);
        }
    }

}
