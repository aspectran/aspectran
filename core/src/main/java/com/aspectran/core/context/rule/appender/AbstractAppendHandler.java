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
package com.aspectran.core.context.rule.appender;

import com.aspectran.core.context.env.EnvironmentProfiles;
import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AbstractAppendHandler.
 */
abstract class AbstractAppendHandler implements RuleAppendHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ActivityRuleAssistant assistant;

    private final EnvironmentProfiles environmentProfiles;

    private List<RuleAppender> pendingList;

    private RuleAppender currentRuleAppender;

    private boolean useAponToLoadXml;

    private boolean debugMode;

    AbstractAppendHandler(ActivityRuleAssistant assistant) {
        this.assistant = assistant;
        this.environmentProfiles = assistant.getEnvironmentProfiles();
    }

    @Override
    public ActivityRuleAssistant getContextRuleAssistant() {
        return assistant;
    }

    @Override
    public void pending(@NonNull AppendRule appendRule) {
        RuleAppender appender = null;
        if (appendRule.getAspectranParameters() != null) {
            appender = new ParametersRuleAppender();
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

        if (logger.isTraceEnabled()) {
            logger.trace("pending RuleAppender " + appender);
        }
    }

    protected void handle() throws Exception {
        if (pendingList != null) {
            List<RuleAppender> pendedList = pendingList;
            pendingList = null;

            for (RuleAppender appender : pendedList) {
                if (environmentProfiles == null ||
                    environmentProfiles.acceptsProfiles(appender.getProfiles())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Append rules " + appender);
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

    protected boolean isUseAponToLoadXml() {
        return useAponToLoadXml;
    }

    @Override
    public void setUseAponToLoadXml(boolean useAponToLoadXml) {
        this.useAponToLoadXml = useAponToLoadXml;
    }

    protected boolean isDebugMode() {
        return debugMode;
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

}
