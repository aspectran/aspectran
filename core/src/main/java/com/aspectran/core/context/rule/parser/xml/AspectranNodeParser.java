/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.type.ContentStyleType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.apon.VariableParameters;
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

    private final ContextRuleAssistant assistant;

    private final NodeletParser parser;

    /**
     * Instantiates a new AspectranNodeParser.
     *
     * @param assistant the assistant for Context Builder
     */
    public AspectranNodeParser(ContextRuleAssistant assistant) {
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
    public AspectranNodeParser(ContextRuleAssistant assistant, boolean validating, boolean trackingLocation) {
        this.assistant = assistant;

        this.parser = new NodeletParser();
        this.parser.setValidating(validating);
        this.parser.setEntityResolver(new AspectranDtdResolver(validating));
        if (trackingLocation) {
            this.parser.trackingLocation();
        }

        addDescriptionNodelets();
        addSettingsNodelets();
        addEnvironmentNodelets();
        addTypeAliasNodelets();
        addAspectNodelets();
        addBeanNodelets();
        addScheduleNodelets();
        addTemplateNodelets();
        addTransletNodelets();
        addAppendNodelets();
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
            throw new Exception("Error parsing aspectran configuration", e);
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

    /**
     * Adds the description nodelets.
     */
    private void addDescriptionNodelets() {
        parser.setXpath("/aspectran/description");
        parser.addNodelet(attrs -> {
            String style = attrs.get("style");
            parser.pushObject(style);
        });
        parser.addNodeEndlet(text -> {
            String style = parser.popObject();
            if (style != null) {
                text = ContentStyleType.styling(text, style);
            }
            if (StringUtils.hasText(text)) {
                assistant.getAssistantLocal().setDescription(text);
            }
        });
    }

    /**
     * Adds the settings nodelets.
     */
    private void addSettingsNodelets() {
        parser.setXpath("/aspectran/settings");
        parser.addNodeEndlet(text -> {
            if (StringUtils.hasText(text)) {
                Parameters parameters = new VariableParameters(text);
                for (String name : parameters.getParameterNameSet()) {
                    assistant.putSetting(name, parameters.getString(name));
                }
            }
            assistant.applySettings();
        });
        parser.setXpath("/aspectran/settings/setting");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            String value = attrs.get("value");

            assistant.putSetting(name, value);
            parser.pushObject(name);

        });
        parser.addNodeEndlet(text -> {
            String name = parser.popObject();
            if (text != null) {
                assistant.putSetting(name, text);
            }
        });
    }

    /**
     * Adds the environment nodelets.
     */
    private void addEnvironmentNodelets() {
        parser.setXpath("/aspectran/environment");
        parser.addNodelet(attrs -> {
            String profile = attrs.get("profile");

            EnvironmentRule environmentRule = EnvironmentRule.newInstance(profile, null);
            parser.pushObject(environmentRule);
        });
        parser.addNodeEndlet(text -> {
            EnvironmentRule environmentRule = parser.popObject();
            assistant.addEnvironmentRule(environmentRule);
        });
        parser.setXpath("/aspectran/environment/properties");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            parser.pushObject(irm);
        });
        parser.addNodelet(new ItemNodeletAdder(assistant));
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            EnvironmentRule environmentRule = parser.peekObject();
            if (!irm.isEmpty()) {
                environmentRule.setPropertyItemRuleMap(irm);
            } else if (StringUtils.hasLength(text)) {
                ItemRuleMap propertyItemRuleMap = ItemRule.toItemRuleMap(text);
                environmentRule.setPropertyItemRuleMap(propertyItemRuleMap);
            }
        });
    }

    /**
     * Adds the type alias nodelets.
     */
    private void addTypeAliasNodelets() {
        parser.setXpath("/aspectran/typeAliases");
        parser.addNodeEndlet(text -> {
            if (StringUtils.hasLength(text)) {
                Parameters parameters = new VariableParameters(text);
                for (String alias : parameters.getParameterNameSet()) {
                    assistant.addTypeAlias(alias, parameters.getString(alias));
                }
            }
        });
        parser.setXpath("/aspectran/typeAliases/typeAlias");
        parser.addNodelet(attrs -> {
            String alias = attrs.get("alias");
            String type = attrs.get("type");

            assistant.addTypeAlias(alias, type);
        });
    }

    /**
     * Adds the aspect rule nodelets.
     */
    private void addAspectNodelets() {
        parser.addNodelet("/aspectran", new AspectNodeletAdder(assistant));
    }

    /**
     * Adds the bean nodelets.
     */
    private void addBeanNodelets() {
        parser.addNodelet("/aspectran", new BeanNodeletAdder(assistant));
    }

    /**
     * Adds the schedule rule nodelets.
     */
    private void addScheduleNodelets() {
        parser.addNodelet("/aspectran", new ScheduleNodeletAdder(assistant));
    }

    /**
     * Adds the template nodelets.
     */
    private void addTemplateNodelets() {
        parser.addNodelet("/aspectran", new TemplateNodeletAdder(assistant));
    }

    /**
     * Adds the translet nodelets.
     */
    private void addTransletNodelets() {
        parser.addNodelet("/aspectran", new TransletNodeletAdder(assistant));
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

}
