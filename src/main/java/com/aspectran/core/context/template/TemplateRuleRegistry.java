/**
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.context.template;

import java.util.LinkedHashMap;
import java.util.Map;

import com.aspectran.core.context.builder.assistant.AssistantLocal;
import com.aspectran.core.context.builder.assistant.DefaultSettings;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class TemplateRuleRegistry.
 *
 * <p>Created: 2016. 1. 11.</p>
 */
public class TemplateRuleRegistry {

    private final Log log = LogFactory.getLog(TemplateRuleRegistry.class);

    private final Map<String, TemplateRule> templateRuleMap = new LinkedHashMap<>();

    private AssistantLocal assistantLocal;
    
    public TemplateRuleRegistry() {
    }

	public void setAssistantLocal(AssistantLocal assistantLocal) {
		this.assistantLocal = assistantLocal;
	}

	public Map<String, TemplateRule> getTemplateRuleMap() {
        return templateRuleMap;
    }

    public boolean contains(String templateId) {
        return templateRuleMap.containsKey(templateId);
    }

    public TemplateRule getTemplateRule(String templateId) {
        return templateRuleMap.get(templateId);
    }

    public void addTemplateRule(TemplateRule templateRule) {
        if (templateRule.getEngine() == null && assistantLocal != null) {
            DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
            if (defaultSettings != null && defaultSettings.getDefaultTemplateEngine() != null) {
            	templateRule.setEngine(defaultSettings.getDefaultTemplateEngine());
            }
        }
		if (templateRule.getEngine() != null) {
			assistantLocal.getAssistant().reserveBeanReference(templateRule.getEngine(), templateRule);
		}

        templateRuleMap.put(templateRule.getId(), templateRule);

        if (log.isTraceEnabled()) {
			log.trace("add TemplateRule " + templateRule);
		}
    }

    public void clear() {
        templateRuleMap.clear();
    }

}
