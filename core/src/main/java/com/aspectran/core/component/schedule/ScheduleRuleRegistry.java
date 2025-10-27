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
import com.aspectran.core.context.rule.parsing.DefaultSettings;
import com.aspectran.core.context.rule.parsing.RuleParsingScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A central registry for all {@link ScheduleRule} definitions.
 * This class holds all the parsed schedule rules and provides a way to access them.
 * It also applies default settings, such as a default scheduler bean, to the rules upon registration.
 *
 * @since 2013. 04. 12
 */
public class ScheduleRuleRegistry extends AbstractComponent {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleRuleRegistry.class);

    private final Map<String, ScheduleRule> scheduleRuleMap = new LinkedHashMap<>();

    private RuleParsingScope ruleParsingScope;

    /**
     * Sets the RuleParsingScope that provides access to context-wide helpers and default settings.
     * @param ruleParsingScope the RuleParsingScope instance
     */
    public void setRuleParsingScope(RuleParsingScope ruleParsingScope) {
        this.ruleParsingScope = ruleParsingScope;
    }

    /**
     * Returns a collection of all registered {@link ScheduleRule}s.
     * @return a collection of schedule rules
     */
    public Collection<ScheduleRule> getScheduleRules() {
        return scheduleRuleMap.values();
    }

    /**
     * Retrieves a specific {@link ScheduleRule} by its ID.
     * @param scheduleId the ID of the schedule
     * @return the corresponding {@code ScheduleRule}, or {@code null} if not found
     */
    public ScheduleRule getScheduleRule(String scheduleId) {
        return scheduleRuleMap.get(scheduleId);
    }

    /**
     * Checks if a {@link ScheduleRule} with the specified ID is registered.
     * @param scheduleId the ID of the schedule to check
     * @return {@code true} if the schedule rule exists, {@code false} otherwise
     */
    public boolean contains(String scheduleId) {
        return scheduleRuleMap.containsKey(scheduleId);
    }

    /**
     * Adds a new {@link ScheduleRule} to the registry.
     * If the rule does not specify a scheduler bean, a default scheduler from
     * {@link DefaultSettings} will be applied.
     * @param scheduleRule the schedule rule to add
     * @throws IllegalRuleException if the rule is misconfigured
     */
    public void addScheduleRule(ScheduleRule scheduleRule) throws IllegalRuleException {
        if (scheduleRule == null) {
            throw new IllegalArgumentException("scheduleRule must not be null");
        }
        if (scheduleRule.getSchedulerBeanId() == null && ruleParsingScope != null) {
            DefaultSettings defaultSettings = ruleParsingScope.getDefaultSettings();
            if (defaultSettings != null && defaultSettings.getDefaultSchedulerBean() != null) {
                scheduleRule.setSchedulerBeanId(defaultSettings.getDefaultSchedulerBean());
            }
        }
        if (scheduleRule.getSchedulerBeanId() != null) {
            if (scheduleRule.getSchedulerBeanClass() == null) {
                ruleParsingScope.getRuleParsingContext().resolveBeanClass(scheduleRule);
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

    /**
     * Finds and returns all {@link ScheduledJobRule}s that are associated with the given translet names.
     * @param transletNames an array of translet names to search for
     * @return a set of matching {@code ScheduledJobRule}s
     */
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
        // This component requires no specific initialization logic.
    }

    @Override
    protected void doDestroy() {
        scheduleRuleMap.clear();
    }

}
