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
package com.aspectran.core.component.template;

import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.assistant.AssistantLocal;
import com.aspectran.core.context.rule.assistant.DefaultSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The TemplateRuleRegistry class is responsible for managing and providing access to {@link TemplateRule} instances.
 * It stores template rules in a map, using the template ID as the key, and provides methods for adding,
 * retrieving, and checking for the existence of template rules.
 *
 * <p>Created: 2016. 1. 11.</p>
 */
public class TemplateRuleRegistry extends AbstractComponent {

    private static final Logger logger = LoggerFactory.getLogger(TemplateRuleRegistry.class);

    private final Map<String, TemplateRule> templateRuleMap = new LinkedHashMap<>();

    private AssistantLocal assistantLocal;

    /**
     * Instantiates a new TemplateRuleRegistry.
     */
    public TemplateRuleRegistry() {
    }

    /**
     * Sets the assistant local.
     * @param assistantLocal the assistant local
     */
    public void setAssistantLocal(AssistantLocal assistantLocal) {
        this.assistantLocal = assistantLocal;
    }

    /**
     * Returns all the template rules.
     * @return a collection of all registered template rules
     */
    public Collection<TemplateRule> getTemplateRules() {
        return templateRuleMap.values();
    }

    /**
     * Returns the template rule for the given template ID.
     * @param templateId the ID of the template
     * @return the template rule, or {@code null} if no such rule exists
     */
    public TemplateRule getTemplateRule(String templateId) {
        return templateRuleMap.get(templateId);
    }

    /**
     * Checks if a template rule with the given ID exists.
     * @param templateId the ID of the template
     * @return {@code true} if the template rule exists, {@code false} otherwise
     */
    public boolean contains(String templateId) {
        return templateRuleMap.containsKey(templateId);
    }

    /**
     * Adds a new template rule to the registry.
     * @param templateRule the template rule to add
     * @throws IllegalRuleException if the rule is invalid
     */
    public void addTemplateRule(TemplateRule templateRule) throws IllegalRuleException {
        if (templateRule == null) {
            throw new IllegalArgumentException("templateRule must not be null");
        }
        if (templateRule.getEngine() == null && assistantLocal != null) {
            DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
            if (defaultSettings != null && defaultSettings.getDefaultTemplateEngineBean() != null) {
                templateRule.setEngineBeanId(defaultSettings.getDefaultTemplateEngineBean());
                templateRule.setTemplateSource(templateRule.getTemplateSource());
            }
        }

        if (assistantLocal != null) {
            assistantLocal.getAssistant().resolveBeanClass(templateRule);
        }
        templateRuleMap.put(templateRule.getId(), templateRule);

        if (logger.isTraceEnabled()) {
            logger.trace("add TemplateRule {}", templateRule);
        }
    }

    /**
     * Clears all the registered template rules.
     */
    private void clear() {
        templateRuleMap.clear();
    }

    @Override
    protected void doInitialize() {
        // Nothing to do
    }

    @Override
    protected void doDestroy() {
        clear();
    }

}
