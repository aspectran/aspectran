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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.utils.ArrayStack;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.nodelet.NodeletParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.InputStream;

/**
 * The Class AspectranNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 4:39:24</p>
 */
public class AspectranNodeParser {

    private static final Logger logger = LoggerFactory.getLogger(AspectranNodeParser.class);

    private final ActivityRuleAssistant assistant;

    private final NodeletParser nodeletParser;

    /**
     * Instantiates a new AspectranNodeParser.
     * @param assistant the assistant for Context Builder
     */
    public AspectranNodeParser(ActivityRuleAssistant assistant) {
        this(assistant, true, true);
    }

    /**
     * Instantiates a new AspectranNodeParser.
     * @param assistant the context builder assistant
     * @param validating true if the parser produced will validate documents
     *      as they are parsed; false otherwise
     * @param trackingLocation true if tracing the location of the node being
     *      parsed; false otherwise
     */
    public AspectranNodeParser(ActivityRuleAssistant assistant, boolean validating, boolean trackingLocation) {
        this.assistant = assistant;
        this.nodeletParser = new NodeletParser(AspectranNodeletGroup.instance());
        this.nodeletParser.setValidating(validating);
        this.nodeletParser.setEntityResolver(new AspectranDtdResolver(validating));
        if (trackingLocation) {
            this.nodeletParser.trackingLocation();
        }
    }

    public ActivityRuleAssistant getAssistant() {
        return assistant;
    }

    public ArrayStack<Object> getObjectStack() {
        return nodeletParser.getObjectStack();
    }

    /**
     * Parses the aspectran configuration.
     * @param ruleAppender the rule appender
     * @throws Exception the exception
     */
    public void parse(RuleAppender ruleAppender) throws Exception {
        try {
            ruleAppender.setNodeTracker(nodeletParser.getNodeTracker());
            try (InputStream inputStream = ruleAppender.getInputStream()) {
                InputSource inputSource = new InputSource(inputStream);
                inputSource.setSystemId(ruleAppender.getQualifiedName());
                AspectranNodeParsingContext.set(this);
                nodeletParser.parse(inputSource);
            } finally {
                AspectranNodeParsingContext.clear();
            }
        } catch (Exception e) {
            Throwable cause = ExceptionUtils.getRootCause(e);
            if (cause instanceof IllegalRuleException) {
                parsingFailed(cause.getMessage(), cause);
            } else {
                parsingFailed("Error parsing aspectran configuration", e);
            }
        }
    }

    public void parsingFailed(String message, Throwable cause) throws Exception {
        String detail;
        if (getAssistant().getRuleAppendHandler().getCurrentRuleAppender().getNodeTracker().getName() != null) {
            detail = message + ": " +
                getAssistant().getRuleAppendHandler().getCurrentRuleAppender().getNodeTracker() + " on " +
                getAssistant().getRuleAppendHandler().getCurrentRuleAppender().getQualifiedName();
        } else {
            detail = message + ": " +
                getAssistant().getRuleAppendHandler().getCurrentRuleAppender().getQualifiedName();
        }
        logger.error(detail);
        throw new Exception(detail, cause);
    }

}
