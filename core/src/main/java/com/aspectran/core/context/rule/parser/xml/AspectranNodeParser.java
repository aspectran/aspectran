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

import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.parsing.RuleParsingContext;
import com.aspectran.utils.ArrayStack;
import com.aspectran.utils.Assert;
import com.aspectran.utils.nodelet.NodeletException;
import com.aspectran.utils.nodelet.NodeletParser;
import org.xml.sax.InputSource;

import java.io.InputStream;

/**
 * The Class AspectranNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 4:39:24</p>
 */
public class AspectranNodeParser {

    private final RuleParsingContext ruleParsingContext;

    private final NodeletParser nodeletParser;

    /**
     * Instantiates a new AspectranNodeParser.
     * @param ruleParsingContext the rule-parsing context
     */
    public AspectranNodeParser(RuleParsingContext ruleParsingContext) {
        this(ruleParsingContext, true, true);
    }

    /**
     * Instantiates a new AspectranNodeParser.
     * @param ruleParsingContext the rule-parsing context
     * @param validating true if the parser produced will validate documents
     *      as they are parsed; false otherwise
     * @param trackingLocation true if tracing the location of the node being
     *      parsed; false otherwise
     */
    public AspectranNodeParser(RuleParsingContext ruleParsingContext, boolean validating, boolean trackingLocation) {
        this.ruleParsingContext = ruleParsingContext;
        this.nodeletParser = new NodeletParser(AspectranNodeletGroup.instance());
        this.nodeletParser.setValidating(validating);
        this.nodeletParser.setEntityResolver(new AspectranDtdResolver(validating));
        if (trackingLocation) {
            this.nodeletParser.trackingLocation();
        }
    }

    public RuleParsingContext getRuleParsingContext() {
        return ruleParsingContext;
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
        Assert.notNull(ruleAppender, "RuleAppender must not be null");
        if (nodeletParser.getNodeTracker() != null) {
            ruleAppender.setNodeTracker(nodeletParser.getNodeTracker());
        }
        try (InputStream inputStream = ruleAppender.getInputStream()) {
            InputSource inputSource = new InputSource(inputStream);
            inputSource.setSystemId(ruleAppender.getQualifiedName());
            nodeletParser.parse(inputSource);
        } catch (NodeletException e) {
            parsingFailed(e.getCause());
        } catch (Exception e) {
            parsingFailed(e);
        }
    }

    public void parsingFailed(Throwable cause) throws Exception {
        throw new Exception("Error parsing aspectran configuration", cause);
    }

}
