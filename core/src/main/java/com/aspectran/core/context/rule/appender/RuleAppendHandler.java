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

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.parser.FileAppendedListener;
import com.aspectran.core.context.rule.parsing.RuleParsingContext;

import java.util.List;

/**
 * Defines the contract for handling the appending of context rules.
 */
public interface RuleAppendHandler {

    /**
     * Sets the file appended listener.
     * @param listener the file appended listener
     */
    void setFileAppendedListener(FileAppendedListener listener);

    /**
     * Returns the rule parsing context.
     * @return the rule parsing context
     */
    RuleParsingContext getRuleParsingContext();

    /**
     * Adds an {@code AppendRule} to the pending list for later processing.
     * @param appendRule the append rule
     */
    void pending(AppendRule appendRule);

    /**
     * Handles the given {@code RuleAppender} to append rules.
     * @param appender the rule appender
     * @throws Exception if an error occurs during handling
     */
    void handle(RuleAppender appender) throws Exception;

    /**
     * Returns the list of pending rule appenders.
     * @return the list of pending rule appenders
     */
    List<RuleAppender> getPendingList();

    /**
     * Returns the currently processing rule appender.
     * @return the current rule appender
     */
    RuleAppender getCurrentRuleAppender();

    /**
     * Sets the currently processing rule appender.
     * @param currentRuleAppender the current rule appender
     */
    void setCurrentRuleAppender(RuleAppender currentRuleAppender);

    /**
     * Sets whether to use APON parser to load XML configuration.
     * @param useAponToLoadXml true to use APON parser for XML
     */
    void setUseAponToLoadXml(boolean useAponToLoadXml);

    /**
     * Sets whether to run in debug mode.
     * @param debugMode true to run in debug mode
     */
    void setDebugMode(boolean debugMode);

}
