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
package com.aspectran.core.context.rule.appender;

import com.aspectran.core.context.env.EnvironmentProfiles;
import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.parsing.RuleParsingContext;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for {@link RuleAppendHandler} implementations.
 */
abstract class AbstractAppendHandler implements RuleAppendHandler {

    /** Logger available to subclasses */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final RuleParsingContext ruleParsingContext;

    private final EnvironmentProfiles environmentProfiles;

    private List<RuleAppender> pendingList;

    private RuleAppender currentRuleAppender;

    private boolean useAponToLoadXml;

    private boolean debugMode;

    AbstractAppendHandler(@NonNull RuleParsingContext ruleParsingContext) {
        this.ruleParsingContext = ruleParsingContext;
        this.environmentProfiles = ruleParsingContext.getEnvironmentProfiles();
    }

    @Override
    public RuleParsingContext getRuleParsingContext() {
        return ruleParsingContext;
    }

    @Override
    public void pending(@NonNull AppendRule appendRule) {
        RuleAppender appender = null;
        if (appendRule.getAspectranParameters() != null) {
            appender = new ParametersRuleAppender();
        } else if (StringUtils.hasText(appendRule.getFile())) {
            appender = new FileRuleAppender(ruleParsingContext.getBasePath(), appendRule.getFile());
        } else if (StringUtils.hasText(appendRule.getResource())) {
            appender = new ResourceRuleAppender(appendRule.getResource(), ruleParsingContext.getClassLoader());
        } else if (StringUtils.hasText(appendRule.getUrl())) {
            appender = new UrlRuleAppender(appendRule.getUrl());
        }
        if (appender != null) {
            appender.setAppendRule(appendRule);
            pending(appender);
        }
    }

    /**
     * Adds a {@code RuleAppender} to the pending list.
     * @param appender the rule appender to add
     */
    private void pending(RuleAppender appender) {
        if (pendingList == null) {
            pendingList = new ArrayList<>();
        }
        pendingList.add(appender);

        if (logger.isTraceEnabled()) {
            logger.trace("pending RuleAppender {}", appender);
        }
    }

    /**
     * Handles all pending {@link RuleAppender}s.
     * @throws Exception if an error occurs during handling
     */
    protected void handle() throws Exception {
        if (pendingList != null) {
            List<RuleAppender> pendedList = pendingList;
            pendingList = null;

            for (RuleAppender appender : pendedList) {
                if (environmentProfiles == null ||
                    environmentProfiles.acceptsProfiles(appender.getProfiles())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Appending rules {}", appender);
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

    /**
     * Returns whether to use APON parser to load XML configuration.
     * @return true if APON parser is used for XML
     */
    protected boolean isUseAponToLoadXml() {
        return useAponToLoadXml;
    }

    @Override
    public void setUseAponToLoadXml(boolean useAponToLoadXml) {
        this.useAponToLoadXml = useAponToLoadXml;
    }

    /**
     * Returns whether to run in debug mode.
     * @return true if in debug mode
     */
    protected boolean isDebugMode() {
        return debugMode;
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

}
