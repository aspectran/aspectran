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
package com.aspectran.demo.anatomy;

import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.bean.DefaultBeanRegistry;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.bean.event.EventListenerRegistry;
import com.aspectran.core.component.bean.event.ListenerMethod;
import com.aspectran.core.component.converter.TypeConverter;
import com.aspectran.core.component.template.DefaultTemplateRenderer;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.converter.RulesToParameters;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.AponLines;
import com.aspectran.utils.apon.Parameters;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A service that provides framework anatomy data.
 */
@Component
@Bean("anatomyService")
public class AnatomyService implements ActivityContextAware {

    private ActivityContext context;

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    public Map<String, Object> getAnatomyData() {
        Map<String, Object> anatomyData = new LinkedHashMap<>();

        // 1. Translet Rules
        if (context.getTransletRuleRegistry() != null) {
            Map<String, TransletRule> ruleMap = context.getTransletRuleRegistry().getTransletRuleMap();
            List<Map<String, Object>> ruleData = ruleMap.entrySet().stream()
                    .map(entry -> convertTransletRuleToApon(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            anatomyData.put("Translet Rules", ruleData);
        }

        // 2. Bean Rules
        if (context.getBeanRegistry() instanceof DefaultBeanRegistry registry) {
            BeanRuleRegistry beanRuleRegistry = registry.getBeanRuleRegistry();
            Set<BeanRule> allBeanRules = new HashSet<>();
            allBeanRules.addAll(beanRuleRegistry.getIdBasedBeanRules());
            beanRuleRegistry.getTypeBasedBeanRules().forEach(allBeanRules::addAll);
            allBeanRules.addAll(beanRuleRegistry.getConfigurableBeanRules());

            List<Map<String, Object>> ruleData = allBeanRules.stream()
                    .map(this::convertBeanRuleToApon)
                    .collect(Collectors.toList());
            anatomyData.put("Bean Rules", ruleData);

            // Event Listeners
            EventListenerRegistry eventListenerRegistry = registry.getEventListenerRegistry();
            if (eventListenerRegistry != null) {
                Map<Class<?>, List<ListenerMethod>> listenerMap = eventListenerRegistry.getListenerMap();
                List<Map<String, Object>> listenerData = listenerMap.entrySet().stream()
                        .map(entry -> convertEventListenerToApon(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
                anatomyData.put("Event Listeners", listenerData);
            }
        }

        // 3. Aspect Rules
        if (context.getAspectRuleRegistry() != null) {
            Collection<AspectRule> rules = context.getAspectRuleRegistry().getAspectRules();
            List<Map<String, Object>> ruleData = rules.stream()
                    .map(this::convertAspectRuleToApon)
                    .collect(Collectors.toList());
            anatomyData.put("Aspect Rules", ruleData);
        }

        // 4. Schedule Rules
        if (context.getScheduleRuleRegistry() != null) {
            Collection<ScheduleRule> rules = context.getScheduleRuleRegistry().getScheduleRules();
            List<Map<String, Object>> ruleData = rules.stream()
                    .map(this::convertScheduleRuleToApon)
                    .collect(Collectors.toList());
            anatomyData.put("Schedule Rules", ruleData);
        }

        // 5. Template Rules
        if (context.getTemplateRenderer() instanceof DefaultTemplateRenderer renderer) {
            Collection<TemplateRule> rules = renderer.getTemplateRules();
            List<Map<String, Object>> ruleData = rules.stream()
                    .map(this::convertTemplateRuleToApon)
                    .collect(Collectors.toList());
            anatomyData.put("Template Rules", ruleData);
        }

        // 6. Type Converters
        if (context.getTypeConverterRegistry() != null) {
            Map<Class<?>, TypeConverter<?>> converters = context.getTypeConverterRegistry().getConverters();
            List<Map<String, Object>> ruleData = converters.entrySet().stream()
                    .map(entry -> convertTypeConverterToApon(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            anatomyData.put("Type Converters", ruleData);
        }

        return anatomyData;
    }

    @NonNull
    private Map<String, Object> convertTransletRuleToApon(String ruleName, @NonNull TransletRule rule) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", ruleName);
        map.put("apon", RulesToParameters.toTransletParameters(rule).toString());
        return map;
    }

    @NonNull
    private Map<String, Object> convertBeanRuleToApon(@NonNull BeanRule rule) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", rule.getId());
        map.put("className", rule.getClassName());
        map.put("apon", RulesToParameters.toBeanParameters(rule).toString());
        return map;
    }

    @NonNull
    private Map<String, Object> convertAspectRuleToApon(@NonNull AspectRule rule) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", rule.getId());
        map.put("apon", RulesToParameters.toAspectParameters(rule).toString());
        return map;
    }

    @NonNull
    private Map<String, Object> convertScheduleRuleToApon(@NonNull ScheduleRule rule) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", rule.getId());
        Parameters params = RulesToParameters.toScheduleParameters(rule);
        map.put("apon", params.toString());
        return map;
    }

    @NonNull
    private Map<String, Object> convertTemplateRuleToApon(@NonNull TemplateRule rule) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", rule.getId());
        Parameters params = RulesToParameters.toTemplateParameters(rule);
        map.put("apon", params.toString());
        return map;
    }

    @NonNull
    private Map<String, Object> convertTypeConverterToApon(@NonNull Class<?> type, @NonNull TypeConverter<?> converter) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", type.getName());
        AponLines apon = new AponLines();
        apon.line("type", type.getName());
        apon.line("converter", converter.getClass().getName());
        map.put("apon", apon.toString());
        return map;
    }

    @NonNull
    private Map<String, Object> convertEventListenerToApon(@NonNull Class<?> eventType, @NonNull List<ListenerMethod> listenerMethods) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", eventType.getName());
        AponLines apon = new AponLines();
        apon.array(eventType.getName());
        for (ListenerMethod listenerMethod : listenerMethods) {
            apon.line(listenerMethod.toString());
        }
        apon.end();
        map.put("apon", apon.toString());
        return map;
    }

}
