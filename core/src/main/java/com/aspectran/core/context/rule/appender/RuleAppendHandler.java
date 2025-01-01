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
package com.aspectran.core.context.rule.appender;

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;

import java.util.List;

/**
 * The Interface RuleAppendHandler.
 */
public interface RuleAppendHandler {

    ActivityRuleAssistant getContextRuleAssistant();

    void pending(AppendRule appendRule);

    void handle(RuleAppender appender) throws Exception;

    List<RuleAppender> getPendingList();

    RuleAppender getCurrentRuleAppender();

    void setCurrentRuleAppender(RuleAppender currentRuleAppender);

    void setUseAponToLoadXml(boolean useAponToLoadXml);

    void setDebugMode(boolean debugMode);

}
