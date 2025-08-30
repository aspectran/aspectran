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

    static final ThreadLocal<AspectranNodeParser> currentAspectranNodeParser = new ThreadLocal<>();

    static final AspectranNodeletGroup nodeletGroup = new AspectranNodeletGroup();

    private final NodeletParser nodeletParser;

    private final ActivityRuleAssistant assistant;

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
        this.nodeletParser = new NodeletParser(nodeletGroup);
        this.nodeletParser.setValidating(validating);
        this.nodeletParser.setEntityResolver(new AspectranDtdResolver(validating));
        if (trackingLocation) {
            this.nodeletParser.trackingLocation();
        }
    }

    public ActivityRuleAssistant getAssistant() {
        return assistant;
    }

    AspectranNodeletGroup getNodeletGroup() {
        return nodeletGroup;
    }

    /**
     * Pushes an object onto the internal object stack.
     * This stack is used to manage context objects during parsing.
     * @param object the object to push
     */
    public void pushObject(Object object) {
        nodeletParser.getObjectStack().push(object);
    }

    /**
     * Pops an object from the top of the internal object stack.
     * @param <T> the expected type of the object
     * @return the object popped from the stack
     */
    @SuppressWarnings("unchecked")
    public <T> T popObject() {
        return (T)nodeletParser.getObjectStack().pop();
    }

    /**
     * Peeks at the object on the top of the internal object stack without removing it.
     * @param <T> the expected type of the object
     * @return the object at the top of the stack
     */
    @SuppressWarnings("unchecked")
    public <T> T peekObject() {
        return (T)nodeletParser.getObjectStack().peek();
    }

    /**
     * Peeks at an object at a specific depth from the top of the internal object stack.
     * @param <T> the expected type of the object
     * @param n the depth from the top (0 for top, 1 for next, etc.)
     * @return the object at the specified depth
     */
    @SuppressWarnings("unchecked")
    public <T> T peekObject(int n) {
        return (T)nodeletParser.getObjectStack().peek(n);
    }

    /**
     * Peeks at an object of a specific type from the internal object stack.
     * It searches the stack from top to bottom for the first object assignable to the target type.
     * @param <T> the expected type of the object
     * @param target the target class type
     * @return the object of the specified type, or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T peekObject(Class<?> target) {
        return (T)nodeletParser.getObjectStack().peek(target);
    }

    /**
     * Clears all objects from the internal object stack.
     */
    public void clearObjectStack() {
        nodeletParser.getObjectStack().clear();
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
                currentAspectranNodeParser.set(this);
                nodeletParser.parse(inputSource);
            } finally {
                currentAspectranNodeParser.remove();
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

    static AspectranNodeParser current() {
        return currentAspectranNodeParser.get();
    }

}
