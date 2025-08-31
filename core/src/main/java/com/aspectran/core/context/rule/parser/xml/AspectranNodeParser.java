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

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;
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

    private final NodeletGroup nodeletGroup = new NodeletGroup("aspectran");

    private final NodeletGroup chooseNodeletGroup = new NodeletGroup();
    private final NodeletGroup itemNodeletGroup = new NodeletGroup();
    private final NodeletGroup innerBeanNodeletGroup = new NodeletGroup();

    private final NodeletAdder actionNodeletAdder;
    private final NodeletAdder adviceInnerNodeAdder;
    private final NodeletAdder exceptionInnerNodeletAdder;
    private final NodeletAdder responseInnerNodeletAdder;

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


        try {
            currentAspectranNodeParser.set(this);

            this.actionNodeletAdder = new ActionNodeletAdder();
            this.adviceInnerNodeAdder = new AdviceInnerNodeletAdder();
            this.exceptionInnerNodeletAdder = new ExceptionInnerNodeletAdder();
            this.responseInnerNodeletAdder = new ResponseInnerNodeletAdder();

            this.chooseNodeletGroup.with(new ChooseNodeletAdder());
            this.itemNodeletGroup.with(new ItemNodeletAdder());
            this.innerBeanNodeletGroup.with(new InnerBeanNodeletAdder());

//            this.nodeletGroup = new AspectranNodeletGroup();
//            this.nodeletGroup = new NodeletGroup("aspectran");
            this.nodeletGroup
                    .with(createDescriptionNodeletAdder())
                    .with(createSettingsNodeletAdder())
                    .with(createTypeAliasNodeletAdder())
                    .with(createAppendNodeletAdder())
                    .with(new EnvironmentNodeletAdder())
                    .with(new AspectNodeletAdder())
                    .with(new BeanNodeletAdder())
                    .with(new ScheduleNodeletAdder())
                    .with(new TemplateNodeletAdder())
                    .with(new TransletNodeletAdder());
        } finally {
            currentAspectranNodeParser.remove();
        }
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

//    AspectranNodeletGroup getNodeletGroup() {
//        return nodeletGroup;
//    }

    public NodeletAdder getActionNodeletAdder() {
//        return actionNodeletAdder;
        return new ActionNodeletAdder();
    }

    public NodeletAdder getAdviceInnerNodeAdder() {
        return adviceInnerNodeAdder;
    }

    public NodeletAdder getExceptionInnerNodeletAdder() {
        return exceptionInnerNodeletAdder;
    }

    public NodeletAdder getResponseInnerNodeletAdder() {
        return responseInnerNodeletAdder;
    }

    public NodeletGroup getChooseNodeletGroup() {
        return chooseNodeletGroup;
    }

    public NodeletGroup getItemNodeletGroup() {
        return itemNodeletGroup;
    }

    public NodeletGroup getInnerBeanNodeletGroup() {
        return innerBeanNodeletGroup;
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

    @NonNull
    private static NodeletAdder createDescriptionNodeletAdder() {
        return group -> group.child("description")
                .nodelet(attrs -> {
                    String profile = attrs.get("profile");
                    String style = attrs.get("style");

                    DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
                    AspectranNodeParser.current().pushObject(descriptionRule);
                })
                .endNodelet(text -> {
                    DescriptionRule descriptionRule = AspectranNodeParser.current().popObject();
                    descriptionRule.setContent(text);
                    descriptionRule = AspectranNodeParser.current().getAssistant().profiling(
                            descriptionRule, AspectranNodeParser.current().getAssistant().getAssistantLocal().getDescriptionRule());
                    AspectranNodeParser.current().getAssistant().getAssistantLocal().setDescriptionRule(descriptionRule);
                });
    }

    @NonNull
    private static NodeletAdder createSettingsNodeletAdder() {
        return group -> group.child("settings")
                .endNodelet("/aspectran/settings", text -> {
                    AspectranNodeParser.current().getAssistant().applySettings();
                })
                .child("setting")
                .nodelet(attrs -> {
                    String name = attrs.get("name");
                    String value = attrs.get("value");
                    AspectranNodeParser.current().pushObject(value);
                    AspectranNodeParser.current().pushObject(name);
                })
                .endNodelet(text -> {
                    String name = AspectranNodeParser.current().popObject();
                    String value = AspectranNodeParser.current().popObject();
                    if (value != null) {
                        AspectranNodeParser.current().getAssistant().putSetting(name, value);
                    } else if (text != null) {
                        AspectranNodeParser.current().getAssistant().putSetting(name, text);
                    }
                });
    }

    @NonNull
    private static NodeletAdder createTypeAliasNodeletAdder() {
        return group -> group.child("typeAliases")
                .nodelet(attrs -> {
                    String alias = attrs.get("alias");
                    String type = attrs.get("type");
                    AspectranNodeParser.current().pushObject(type);
                    AspectranNodeParser.current().pushObject(alias);
                })
                .endNodelet(text -> {
                    String alias = AspectranNodeParser.current().popObject();
                    String type = AspectranNodeParser.current().popObject();
                    if (type != null) {
                        AspectranNodeParser.current().getAssistant().addTypeAlias(alias, type);
                    } else if (text != null) {
                        AspectranNodeParser.current().getAssistant().addTypeAlias(alias, text);
                    }
                });
    }

    @NonNull
    private static NodeletAdder createAppendNodeletAdder() {
        return group -> group.child("append")
                .nodelet(attrs -> {
                    String file = attrs.get("file");
                    String resource = attrs.get("resource");
                    String url = attrs.get("url");
                    String format = attrs.get("format");
                    String profile = attrs.get("profile");

                    RuleAppendHandler appendHandler = AspectranNodeParser.current().getAssistant().getRuleAppendHandler();
                    if (appendHandler != null) {
                        AppendRule appendRule = AppendRule.newInstance(file, resource, url, format, profile);
                        appendHandler.pending(appendRule);
                    }
                });
    }

}
