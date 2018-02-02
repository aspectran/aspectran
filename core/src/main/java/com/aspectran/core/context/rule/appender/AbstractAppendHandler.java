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
package com.aspectran.core.context.rule.appender;

import com.aspectran.core.context.env.ContextEnvironment;
import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AbstractAppendHandler.
 */
abstract class AbstractAppendHandler implements RuleAppendHandler {

    protected final Log log = LogFactory.getLog(getClass());

    private final ContextRuleAssistant assistant;

    private final ContextEnvironment environment;

    private List<RuleAppender> pendingList;

    private RuleAppender currentRuleAppender;

    AbstractAppendHandler(ContextRuleAssistant assistant) {
        this.assistant = assistant;
        this.environment = assistant.getContextEnvironment();
    }

    @Override
    public ContextRuleAssistant getContextRuleAssistant() {
        return assistant;
    }

    @Override
    public void pending(AppendRule appendRule) {
        RuleAppender appender = null;
        if (appendRule.getAspectranParameters() != null) {
            appender = new ParametersRuleAppender(appendRule.getAspectranParameters());
        } else if (StringUtils.hasText(appendRule.getFile())) {
            appender = new FileRuleAppender(assistant.getBasePath(), appendRule.getFile());
        } else if (StringUtils.hasText(appendRule.getResource())) {
            appender = new ResourceRuleAppender(appendRule.getResource(), assistant.getClassLoader());
        } else if (StringUtils.hasText(appendRule.getUrl())) {
            appender = new UrlRuleAppender(appendRule.getUrl());
        }
        if (appender != null) {
            appender.setAppendRule(appendRule);
            pending(appender);
        }
    }

    private void pending(RuleAppender appender) {
        if (pendingList == null) {
            pendingList = new ArrayList<>();
        }

        pendingList.add(appender);

        if (log.isDebugEnabled()) {
            log.debug("Pending appender " + appender);
        }
    }

    protected void handle() throws Exception {
        if (pendingList != null) {
            List<RuleAppender> pendedList = pendingList;
            pendingList = null;

            if (environment != null) {
                for (RuleAppender appender : pendedList) {
                    if (environment.acceptsProfiles(appender.getProfiles())) {
                        if (log.isDebugEnabled()) {
                            log.debug("Appending " + appender);
                        }
                        handle(appender);
                    }
                }
            } else {
                for (RuleAppender appender : pendedList) {
                    if (log.isDebugEnabled()) {
                        log.debug("Appending " + appender);
                    }
                    handle(appender);
                }
            }
        }
    }

    @Override
    public List<RuleAppender> getPendingList() {
        return pendingList;
    }

    @Override
    public RuleAppender getCurrentRuleAppender() {
        return currentRuleAppender;
    }

    @Override
    public void setCurrentRuleAppender(RuleAppender currentRuleAppender) {
        this.currentRuleAppender = currentRuleAppender;
    }

}
