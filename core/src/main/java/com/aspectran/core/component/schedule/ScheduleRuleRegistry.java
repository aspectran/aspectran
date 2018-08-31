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
package com.aspectran.core.component.schedule;

import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.assistant.AssistantLocal;
import com.aspectran.core.context.rule.assistant.DefaultSettings;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class ScheduleRuleRegistry.
 */
public class ScheduleRuleRegistry extends AbstractComponent {

    private final Log log = LogFactory.getLog(ScheduleRuleRegistry.class);

    private final Map<String, ScheduleRule> scheduleRuleMap = new LinkedHashMap<>();

    private AssistantLocal assistantLocal;

    public ScheduleRuleRegistry() {
    }

    public void setAssistantLocal(AssistantLocal assistantLocal) {
        this.assistantLocal = assistantLocal;
    }

    public Collection<ScheduleRule> getScheduleRules() {
        return scheduleRuleMap.values();
    }

    public ScheduleRule getScheduleRule(String scheduleId) {
        return scheduleRuleMap.get(scheduleId);
    }

    public boolean contains(String scheduleId) {
        return scheduleRuleMap.containsKey(scheduleId);
    }

    public void addScheduleRule(ScheduleRule scheduleRule) {
        if (scheduleRule.getSchedulerBeanId() == null && assistantLocal != null) {
            DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
            if (defaultSettings != null && defaultSettings.getDefaultSchedulerBean() != null) {
                scheduleRule.setSchedulerBeanId(defaultSettings.getDefaultSchedulerBean());
            }
        }
        if (scheduleRule.getSchedulerBeanId() != null) {
            assistantLocal.getAssistant().resolveBeanClass(scheduleRule);
        }

        scheduleRuleMap.put(scheduleRule.getId(), scheduleRule);

        if (log.isTraceEnabled()) {
            log.trace("add ScheduleRule " + scheduleRule);
        }
    }

    @Override
    protected void doInitialize() {
        // Nothing to do
    }

    @Override
    protected void doDestroy() {
        scheduleRuleMap.clear();
    }
    
}
