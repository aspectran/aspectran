/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.util.ExceptionUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.core.util.nodelet.NodeletParser;
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

    private final ActionNodeletAdder actionNodeletAdder;

    private final AspectAdviceInnerNodeletAdder aspectAdviceInnerNodeletAdder;

    private final AspectNodeletAdder aspectNodeletAdder;

    private final BeanNodeletAdder beanNodeletAdder;

    private final ChooseNodeletAdder chooseNodeletAdder;

    private final EnvironmentNodeletAdder environmentNodeletAdder;

    private final ExceptionInnerNodeletAdder exceptionInnerNodeletAdder;

    private final ItemNodeletAdder[] itemNodeletAdders;

    private final InnerBeanNodeletAdder[] innerBeanNodeletAdders;

    private final ResponseInnerNodeletAdder responseInnerNodeletAdder;

    private final ScheduleNodeletAdder scheduleNodeletAdder;

    private final TemplateNodeletAdder templateNodeletAdder;

    private final TransletNodeletAdder transletNodeletAdder;

    private final NodeletParser parser;

    /**
     * Instantiates a new AspectranNodeParser.
     *
     * @param assistant the assistant for Context Builder
     */
    public AspectranNodeParser(ActivityRuleAssistant assistant) {
        this(assistant, true, true);
    }

    /**
     * Instantiates a new AspectranNodeParser.
     *
     * @param assistant the context builder assistant
     * @param validating true if the parser produced will validate documents
     *      as they are parsed; false otherwise
     * @param trackingLocation true if tracing the location of the node being
     *      parsed; false otherwise
     */
    public AspectranNodeParser(ActivityRuleAssistant assistant, boolean validating, boolean trackingLocation) {
        this.assistant = assistant;
        this.actionNodeletAdder = new ActionNodeletAdder();
        this.aspectAdviceInnerNodeletAdder = new AspectAdviceInnerNodeletAdder();
        this.aspectNodeletAdder = new AspectNodeletAdder();
        this.beanNodeletAdder = new BeanNodeletAdder();
        this.chooseNodeletAdder = new ChooseNodeletAdder();
        this.environmentNodeletAdder = new EnvironmentNodeletAdder();
        this.exceptionInnerNodeletAdder = new ExceptionInnerNodeletAdder();
        this.responseInnerNodeletAdder = new ResponseInnerNodeletAdder();
        this.scheduleNodeletAdder = new ScheduleNodeletAdder();
        this.templateNodeletAdder = new TemplateNodeletAdder();
        this.transletNodeletAdder = new TransletNodeletAdder();
        this.itemNodeletAdders = new ItemNodeletAdder[] {
                new ItemNodeletAdder(0),
                new ItemNodeletAdder(1),
                new ItemNodeletAdder(2)
        };
        this.innerBeanNodeletAdders = new InnerBeanNodeletAdder[] {
                null,
                new InnerBeanNodeletAdder(1),
                new InnerBeanNodeletAdder(2)
        };

        this.parser = new NodeletParser(this);
        this.parser.setValidating(validating);
        this.parser.setEntityResolver(new AspectranDtdResolver(validating));
        if (trackingLocation) {
            this.parser.trackingLocation();
        }

        addDescriptionNodelets();
        addSettingsNodelets();
        addTypeAliasNodelets();
        addEnvironmentNodelets();
        addAspectNodelets();
        addBeanNodelets();
        addScheduleNodelets();
        addTemplateNodelets();
        addTransletNodelets();
        addAppendNodelets();
    }

    public ActivityRuleAssistant getAssistant() {
        return assistant;
    }

    /**
     * Parses the aspectran configuration.
     *
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
     * Adds the description nodelets.
     */
    private void addDescriptionNodelets() {
        parser.setXpath("/aspectran/description");
        parser.addNodelet(attrs -> {
            String profile = attrs.get("profile");
            String style = attrs.get("style");

            DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
            parser.pushObject(descriptionRule);
        });
        parser.addNodeEndlet(text -> {
            DescriptionRule descriptionRule = parser.popObject();
            descriptionRule.setContent(text);
            descriptionRule = assistant.profiling(descriptionRule, assistant.getAssistantLocal().getDescriptionRule());
            assistant.getAssistantLocal().setDescriptionRule(descriptionRule);
        });
    }

    /**
     * Adds the settings nodelets.
     */
    private void addSettingsNodelets() {
        parser.setXpath("/aspectran/settings");
        parser.addNodeEndlet(text -> {
            assistant.applySettings();
        });
        parser.setXpath("/aspectran/settings/setting");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            String value = attrs.get("value");
            parser.pushObject(value);
            parser.pushObject(name);
        });
        parser.addNodeEndlet(text -> {
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
     * Adds the type alias nodelets.
     */
    private void addTypeAliasNodelets() {
        parser.setXpath("/aspectran/typeAliases/typeAlias");
        parser.addNodelet(attrs -> {
            String alias = attrs.get("alias");
            String type = attrs.get("type");
            parser.pushObject(type);
            parser.pushObject(alias);
        });
        parser.addNodeEndlet(text -> {
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
     * Adds the environment nodelets.
     */
    private void addEnvironmentNodelets() {
        parser.addNodelet("/aspectran", environmentNodeletAdder);
    }

    /**
     * Adds the aspect rule nodelets.
     */
    private void addAspectNodelets() {
        parser.addNodelet("/aspectran", aspectNodeletAdder);
    }

    /**
     * Adds the bean nodelets.
     */
    private void addBeanNodelets() {
        parser.addNodelet("/aspectran", beanNodeletAdder);
    }

    /**
     * Adds the schedule rule nodelets.
     */
    private void addScheduleNodelets() {
        parser.addNodelet("/aspectran", scheduleNodeletAdder);
    }

    /**
     * Adds the template nodelets.
     */
    private void addTemplateNodelets() {
        parser.addNodelet("/aspectran", templateNodeletAdder);
    }

    /**
     * Adds the translet nodelets.
     */
    private void addTransletNodelets() {
        parser.addNodelet("/aspectran", transletNodeletAdder);
    }

    /**
     * Adds the append nodelets.
     */
    private void addAppendNodelets() {
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

    void addActionNodelets() {
        parser.addNodelet(actionNodeletAdder);
    }

    void addNestedActionNodelets() {
        String xpath = parser.getXpath();
        parser.addNodelet(actionNodeletAdder);
        parser.addNodelet(chooseNodeletAdder);
        parser.setXpath(xpath + "/choose/when");
        parser.addNodelet(chooseNodeletAdder);
        parser.setXpath(xpath + "/choose/otherwise");
        parser.addNodelet(chooseNodeletAdder);
        parser.setXpath(xpath + "/choose/when/choose/when/choose");
        parser.addNodelet(attrs -> {
            throw new IllegalRuleException("The <choose> element can only be nested up to 2 times");
        });
        parser.setXpath(xpath + "/choose/when/choose/otherwise/choose");
        parser.addNodelet(attrs -> {
            throw new IllegalRuleException("The <choose> element can only be nested up to 2 times");
        });
        parser.setXpath(xpath + "/choose/otherwise/choose/when/choose");
        parser.addNodelet(attrs -> {
            throw new IllegalRuleException("The <choose> element can only be nested up to 2 times");
        });
        parser.setXpath(xpath + "/choose/otherwise/choose/otherwise/choose");
        parser.addNodelet(attrs -> {
            throw new IllegalRuleException("The <choose> element can only be nested up to 2 times");
        });
        parser.setXpath(xpath);
    }

    void addAspectAdviceInnerNodelets() {
        parser.addNodelet(aspectAdviceInnerNodeletAdder);
    }

    void addExceptionInnerNodelets() {
        parser.addNodelet(exceptionInnerNodeletAdder);
    }

    void addResponseInnerNodelets() {
        parser.addNodelet(responseInnerNodeletAdder);
    }

    void addItemNodelets() {
        addItemNodelets(0);
    }

    void addItemNodelets(int depth) {
        parser.addNodelet(itemNodeletAdders[depth]);
    }

    void addInnerBeanNodelets(int depth) {
        if (depth < innerBeanNodeletAdders.length - 1) {
            parser.addNodelet(innerBeanNodeletAdders[depth + 1]);
        }
    }

    int getMaxInnerBeans() {
        return (innerBeanNodeletAdders.length - 1);
    }

}
