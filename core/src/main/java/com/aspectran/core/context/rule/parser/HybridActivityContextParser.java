/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.context.rule.parser;

import com.aspectran.core.context.rule.appender.HybridRuleAppendHandler;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.converter.ParametersToRules;
import com.aspectran.core.context.rule.params.AspectranParameters;

/**
 * The Class HybridActivityContextParser.
 * 
 * <p>Created: 2015. 01. 27 PM 10:36:29</p>
 */
public class HybridActivityContextParser extends AbstractActivityContextParser {

    public HybridActivityContextParser(ContextRuleAssistant assistant) {
        super(assistant);
    }

    @Override
    public ContextRuleAssistant parse(String rootFile) throws ActivityContextParserException {
        try {
            if (rootFile == null) {
                throw new IllegalArgumentException("rootFile must not be null");
            }

            RuleAppendHandler appendHandler = createRuleAppendHandler();
            appendHandler.handle(resolveAppender(rootFile));

            return getContextRuleAssistant();
        } catch (Exception e) {
            throw new ActivityContextParserException("Failed to parse configuration: " + rootFile, e);
        }
    }

    @Override
    public ContextRuleAssistant parse(AspectranParameters aspectranParameters) throws ActivityContextParserException {
        try {
            if (aspectranParameters == null) {
                throw new IllegalArgumentException("aspectranParameters must not be null");
            }

            RuleAppendHandler appendHandler = createRuleAppendHandler();

            ParametersToRules ruleConverter = new ParametersToRules(getContextRuleAssistant());
            ruleConverter.asRules(aspectranParameters);

            appendHandler.handle(null);

            return getContextRuleAssistant();
        } catch (Exception e) {
            throw new ActivityContextParserException("Failed to parse configuration with given AspectranParameters", e);
        }
    }

    private RuleAppendHandler createRuleAppendHandler() {
        RuleAppendHandler appendHandler = new HybridRuleAppendHandler(getContextRuleAssistant(), getEncoding());
        appendHandler.setDebugMode(isDebugMode());
        getContextRuleAssistant().setRuleAppendHandler(appendHandler);
        return appendHandler;
    }

}
