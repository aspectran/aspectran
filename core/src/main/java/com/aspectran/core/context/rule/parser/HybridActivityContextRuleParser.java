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
package com.aspectran.core.context.rule.parser;

import com.aspectran.core.context.rule.appender.HybridRuleAppendHandler;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.core.context.rule.converter.ParametersToRules;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.parser.xml.AspectranNodeParser;
import com.aspectran.core.context.rule.parser.xml.AspectranNodeParsingContext;
import com.aspectran.core.context.rule.parsing.RuleParsingContext;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * A hybrid parser that can handle both XML and APON formats for Aspectran's
 * configuration files.
 * <p>It determines the format based on the file extension or an explicit
 * format hint and delegates the parsing to the appropriate underlying parser
 * ({@link com.aspectran.core.context.rule.parser.xml.AspectranNodeParser} for XML
 * or {@code AponReader} for APON).</p>
 *
 * <p>Created: 2015. 01. 27 PM 10:36:29</p>
 */
public class HybridActivityContextRuleParser extends AbstractActivityContextRuleParser {

    private AspectranNodeParser aspectranNodeParser;

    private FileAppendedListener fileAppendedListener;

    public HybridActivityContextRuleParser(RuleParsingContext ruleParsingContext) {
        super(ruleParsingContext);
    }

    @Override
    public void setFileAppendedListener(FileAppendedListener listener) {
        this.fileAppendedListener = listener;
    }

    @Override
    public RuleParsingContext parse(String[] contextRules) throws ActivityContextRuleParserException {
        try {
            if (contextRules == null) {
                throw new IllegalArgumentException("contextRules must not be null");
            }

            RuleAppendHandler appendHandler = createRuleAppendHandler();
            for (String ruleFile : contextRules) {
                appendHandler.handle(resolveAppender(ruleFile));
            }

            return getRuleParsingContext();
        } catch (Exception e) {
            throw new ActivityContextRuleParserException("Failed to parse configurations [" +
                    StringUtils.joinWithCommas(contextRules) + "]", e);
        }
    }

    @Override
    public RuleParsingContext parse(AspectranParameters aspectranParameters) throws ActivityContextRuleParserException {
        try {
            if (aspectranParameters == null) {
                throw new IllegalArgumentException("aspectranParameters must not be null");
            }

            RuleAppendHandler appendHandler = createRuleAppendHandler();

            ParametersToRules ruleConverter = new ParametersToRules(getRuleParsingContext());
            ruleConverter.toRules(aspectranParameters);

            appendHandler.handle(null);

            return getRuleParsingContext();
        } catch (Exception e) {
            throw new ActivityContextRuleParserException("Failed to parse configuration with given AspectranParameters", e);
        }
    }

    @Override
    public AspectranNodeParser getAspectranNodeParser() {
        if (aspectranNodeParser == null) {
            aspectranNodeParser = new AspectranNodeParser(getRuleParsingContext());
            AspectranNodeParsingContext.set(aspectranNodeParser);
        }
        return aspectranNodeParser;
    }

    @Override
    public void close() {
        if (aspectranNodeParser != null) {
            AspectranNodeParsingContext.clear();
            aspectranNodeParser = null;
        }
    }

    @NonNull
    private RuleAppendHandler createRuleAppendHandler() {
        RuleAppendHandler appendHandler = new HybridRuleAppendHandler(this, getEncoding());
        appendHandler.setUseAponToLoadXml(isUseXmlToApon());
        appendHandler.setDebugMode(isDebugMode());
        if (fileAppendedListener != null) {
            appendHandler.setFileAppendedListener(fileAppendedListener);
        }
        getRuleParsingContext().setRuleAppendHandler(appendHandler);
        return appendHandler;
    }

}
