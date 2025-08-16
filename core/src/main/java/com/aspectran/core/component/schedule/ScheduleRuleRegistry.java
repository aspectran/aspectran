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
package com.aspectran.core.component.schedule;

import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.core.context.rule.assistant.AssistantLocal;
import com.aspectran.core.context.rule.assistant.DefaultSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ScheduleRuleRegistry extends AbstractComponent {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleRuleRegistry.class);

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

    public void addScheduleRule(ScheduleRule scheduleRule) throws IllegalRuleException {
        if (scheduleRule == null) {
            throw new IllegalArgumentException("scheduleRule must not be null");
        }
        if (scheduleRule.getSchedulerBeanId() == null && assistantLocal != null) {
            DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
            if (defaultSettings != null && defaultSettings.getDefaultSchedulerBean() != null) {
                scheduleRule.setSchedulerBeanId(defaultSettings.getDefaultSchedulerBean());
            }
        }
        if (scheduleRule.getSchedulerBeanId() != null) {
            if (scheduleRule.getSchedulerBeanClass() == null) {
                assistantLocal.getAssistant().resolveBeanClass(scheduleRule);
            }
        } else if (scheduleRule.getSchedulerBeanClass() != null) {
            scheduleRule.setSchedulerBeanId(BeanRule.CLASS_DIRECTIVE_PREFIX +
                    scheduleRule.getSchedulerBeanClass().getName());
        }

        scheduleRuleMap.put(scheduleRule.getId(), scheduleRule);

        if (logger.isTraceEnabled()) {
            logger.trace("add ScheduleRule {}", scheduleRule);
        }
    }

    public Set<ScheduledJobRule> getScheduledJobRules(String[] transletNames) {
        Set<ScheduledJobRule> scheduledJobRules = new LinkedHashSet<>();
        if (transletNames != null) {
            for (ScheduleRule scheduleRule : getScheduleRules()) {
                for (ScheduledJobRule jobRule : scheduleRule.getScheduledJobRuleList()) {
                    for (String transletName : transletNames) {
                        if (jobRule.getTransletName().equals(transletName)) {
                            scheduledJobRules.add(jobRule);
                        }
                    }
                }
            }
        }
        return scheduledJobRules;
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
