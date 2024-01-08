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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.nodelet.NodeletParser;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Class AspectranNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 4:39:24</p>
 */
public class AspectranNodeParser {

    private static final Logger logger = LoggerFactory.getLogger(AspectranNodeParser.class);

    private final ActivityRuleAssistant assistant;

    private final ActionNodeParser actionNodeParser;

    private final AspectAdviceInnerNodeParser aspectAdviceInnerNodeParser;

    private final AspectNodeParser aspectNodeParser;

    private final BeanNodeParser beanNodeParser;

    private final ChooseNodeParser chooseNodeParser;

    private final EnvironmentNodeParser environmentNodeParser;

    private final ExceptionInnerNodeParser exceptionInnerNodeParser;

    private final ItemNodeParser[] itemNodeParsers;

    private final InnerBeanNodeParser[] innerBeanNodeParsers;

    private final ResponseInnerNodeParser responseInnerNodeParser;

    private final ScheduleNodeParser scheduleNodeParser;

    private final TemplateNodeParser templateNodeParser;

    private final TransletNodeParser transletNodeParser;

    private final NodeletParser parser;

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
        this.actionNodeParser = new ActionNodeParser();
        this.aspectAdviceInnerNodeParser = new AspectAdviceInnerNodeParser();
        this.aspectNodeParser = new AspectNodeParser();
        this.beanNodeParser = new BeanNodeParser();
        this.chooseNodeParser = new ChooseNodeParser();
        this.environmentNodeParser = new EnvironmentNodeParser();
        this.exceptionInnerNodeParser = new ExceptionInnerNodeParser();
        this.responseInnerNodeParser = new ResponseInnerNodeParser();
        this.scheduleNodeParser = new ScheduleNodeParser();
        this.templateNodeParser = new TemplateNodeParser();
        this.transletNodeParser = new TransletNodeParser();
        this.itemNodeParsers = new ItemNodeParser[] {
                new ItemNodeParser(0),
                new ItemNodeParser(1),
                new ItemNodeParser(2)
        };
        this.innerBeanNodeParsers = new InnerBeanNodeParser[] {
                null,
                new InnerBeanNodeParser(1),
                new InnerBeanNodeParser(2)
        };

        this.parser = new NodeletParser(this);
        this.parser.setValidating(validating);
        this.parser.setEntityResolver(new AspectranDtdResolver(validating));
        if (trackingLocation) {
            this.parser.trackingLocation();
        }

        parseDescriptionNode();
        parseSettingsNode();
        parseTypeAliasNode();
        parseEnvironmentNode();
        parseAspectNode();
        parseBeanNode();
        parseScheduleNode();
        parseTemplateNode();
        parseTransletNode();
        parseAppendNode();
    }

    public ActivityRuleAssistant getAssistant() {
        return assistant;
    }

    /**
     * Parses the aspectran configuration.
     * @param ruleAppender the rule appender
     * @throws Exception the exception
     */
    public void parse(RuleAppender ruleAppender) throws Exception {
        InputStream inputStream = null;
        try {
            ruleAppender.setNodeTracker(parser.getNodeTracker());

            inputStream = ruleAppender.getInputStream();
            InputSource inputSource = new InputSource(inputStream);
            inputSource.setSystemId(ruleAppender.getQualifiedName());
            parser.parse(inputSource);
        } catch (Exception e) {
            Throwable cause = ExceptionUtils.getRootCause(e);
            if (cause instanceof IllegalRuleException) {
                parsingFailed(cause.getMessage(), cause);
            } else {
                parsingFailed("Error parsing aspectran configuration", cause);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public void parsingFailed(String message, Throwable cause) throws Exception {
        String detail;
        if (assistant.getRuleAppendHandler().getCurrentRuleAppender().getNodeTracker().getName() != null) {
            detail = message + ": " +
                assistant.getRuleAppendHandler().getCurrentRuleAppender().getNodeTracker() + " on " +
                assistant.getRuleAppendHandler().getCurrentRuleAppender().getQualifiedName();
        } else {
            detail = message + ": " +
                assistant.getRuleAppendHandler().getCurrentRuleAppender().getQualifiedName();
        }
        logger.error(detail);
        throw new Exception(detail, cause);
    }

    /**
     * Parse the description node.
     */
    private void parseDescriptionNode() {
        parser.setXpath("/aspectran/description");
        parser.addNodelet(attrs -> {
            String profile = attrs.get("profile");
            String style = attrs.get("style");

            DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
            parser.pushObject(descriptionRule);
        });
        parser.addEndNodelet(text -> {
            DescriptionRule descriptionRule = parser.popObject();
            descriptionRule.setContent(text);
            descriptionRule = assistant.profiling(descriptionRule, assistant.getAssistantLocal().getDescriptionRule());
            assistant.getAssistantLocal().setDescriptionRule(descriptionRule);
        });
    }

    /**
     * Parse the settings node.
     */
    private void parseSettingsNode() {
        parser.setXpath("/aspectran/settings");
        parser.addEndNodelet(text -> {
            assistant.applySettings();
        });
        parser.setXpath("/aspectran/settings/setting");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            String value = attrs.get("value");
            parser.pushObject(value);
            parser.pushObject(name);
        });
        parser.addEndNodelet(text -> {
            String name = parser.popObject();
            String value = parser.popObject();
            if (value != null) {
                assistant.putSetting(name, value);
            } else if (text != null) {
                assistant.putSetting(name, text);
            }
        });
    }

    /**
     * Parse the type alias node.
     */
    private void parseTypeAliasNode() {
        parser.setXpath("/aspectran/typeAliases/typeAlias");
        parser.addNodelet(attrs -> {
            String alias = attrs.get("alias");
            String type = attrs.get("type");
            parser.pushObject(type);
            parser.pushObject(alias);
        });
        parser.addEndNodelet(text -> {
            String alias = parser.popObject();
            String type = parser.popObject();
            if (type != null) {
                assistant.addTypeAlias(alias, type);
            } else if (text != null) {
                assistant.addTypeAlias(alias, text);
            }
        });
    }

    /**
     * Parse the environment node.
     */
    private void parseEnvironmentNode() {
        parser.addNodelet("/aspectran", environmentNodeParser);
    }

    /**
     * Parse the aspect rule node.
     */
    private void parseAspectNode() {
        parser.addNodelet("/aspectran", aspectNodeParser);
    }

    /**
     * Parse the bean node.
     */
    private void parseBeanNode() {
        parser.addNodelet("/aspectran", beanNodeParser);
    }

    /**
     * Parse the schedule rule node.
     */
    private void parseScheduleNode() {
        parser.addNodelet("/aspectran", scheduleNodeParser);
    }

    /**
     * Parse the template node.
     */
    private void parseTemplateNode() {
        parser.addNodelet("/aspectran", templateNodeParser);
    }

    /**
     * Parse the translet node.
     */
    private void parseTransletNode() {
        parser.addNodelet("/aspectran", transletNodeParser);
    }

    /**
     * Parse the append node.
     */
    private void parseAppendNode() {
        parser.setXpath("/aspectran/append");
        parser.addNodelet(attrs -> {
            String file = attrs.get("file");
            String resource = attrs.get("resource");
            String url = attrs.get("url");
            String format = attrs.get("format");
            String profile = attrs.get("profile");

            RuleAppendHandler appendHandler = assistant.getRuleAppendHandler();
            if (appendHandler != null) {
                AppendRule appendRule = AppendRule.newInstance(file, resource, url, format, profile);
                appendHandler.pending(appendRule);
            }
        });
    }

    void parseActionNode() {
        parser.addNodelet(actionNodeParser);
    }

    void parseNestedActionNode() {
        String xpath = parser.getXpath();
        parser.addNodelet(actionNodeParser);
        parser.addNodelet(chooseNodeParser);
        parser.setXpath(xpath + "/choose/when");
        parser.addNodelet(chooseNodeParser);
        parser.setXpath(xpath + "/choose/otherwise");
        parser.addNodelet(chooseNodeParser);
        parser.setXpath(xpath + "/choose/when/choose/when/choose");
        parser.addNodelet(attrs -> {
            chooseElementsNestingLimitExceeded();
        });
        parser.setXpath(xpath + "/choose/when/choose/otherwise/choose");
        parser.addNodelet(attrs -> {
            chooseElementsNestingLimitExceeded();
        });
        parser.setXpath(xpath + "/choose/otherwise/choose/when/choose");
        parser.addNodelet(attrs -> {
            chooseElementsNestingLimitExceeded();
        });
        parser.setXpath(xpath + "/choose/otherwise/choose/otherwise/choose");
        parser.addNodelet(attrs -> {
            chooseElementsNestingLimitExceeded();
        });
        parser.setXpath(xpath);
    }

    void parseAspectAdviceInnerNode() {
        parser.addNodelet(aspectAdviceInnerNodeParser);
    }

    void parseExceptionInnerNode() {
        parser.addNodelet(exceptionInnerNodeParser);
    }

    void parseResponseInnerNode() {
        parser.addNodelet(responseInnerNodeParser);
    }

    void parseItemNode() {
        parseItemNode(0);
    }

    void parseItemNode(int depth) {
        parser.addNodelet(itemNodeParsers[depth]);
    }

    void parseInnerBeanNode(int depth) {
        if (depth < innerBeanNodeParsers.length - 1) {
            parser.addNodelet(innerBeanNodeParsers[depth + 1]);
        }
    }

    int getMaxInnerBeans() {
        return (innerBeanNodeParsers.length - 1);
    }

    private void chooseElementsNestingLimitExceeded() throws IllegalRuleException {
        throw new IllegalRuleException("The <choose> element can be nested up to 2 times");
    }

}
