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
package com.aspectran.core.component.template;

import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.assistant.AssistantLocal;
import com.aspectran.core.context.rule.assistant.DefaultSettings;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class TemplateRuleRegistry.
 *
 * <p>Created: 2016. 1. 11.</p>
 */
public class TemplateRuleRegistry extends AbstractComponent {

    private final Log log = LogFactory.getLog(TemplateRuleRegistry.class);

    private final Map<String, TemplateRule> templateRuleMap = new LinkedHashMap<>();

    private AssistantLocal assistantLocal;
    
    public TemplateRuleRegistry() {
    }

    public void setAssistantLocal(AssistantLocal assistantLocal) {
        this.assistantLocal = assistantLocal;
    }

    public Collection<TemplateRule> getTemplateRules() {
        return templateRuleMap.values();
    }

    public TemplateRule getTemplateRule(String templateId) {
        return templateRuleMap.get(templateId);
    }

    public boolean contains(String templateId) {
        return templateRuleMap.containsKey(templateId);
    }

    public void addTemplateRule(TemplateRule templateRule) {
        if (templateRule.getEngine() == null && assistantLocal != null) {
            DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
            if (defaultSettings != null && defaultSettings.getDefaultTemplateEngineBean() != null) {
                templateRule.setEngineBeanId(defaultSettings.getDefaultTemplateEngineBean());
                templateRule.setTemplateSource(templateRule.getTemplateSource());
            }
        }

        assistantLocal.getAssistant().resolveBeanClass(templateRule);
        templateRuleMap.put(templateRule.getId(), templateRule);

        if (log.isTraceEnabled()) {
            log.trace("add TemplateRule " + templateRule);
        }
    }

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
