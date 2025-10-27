package com.aspectran.demo.anatomy;

import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.bean.DefaultBeanRegistry;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.template.DefaultTemplateRenderer;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.converter.RulesToParameters;
import com.aspectran.utils.annotation.jsr305.NonNull;
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
            Collection<TransletRule> rules = context.getTransletRuleRegistry().getTransletRules();
            List<Map<String, Object>> ruleData = rules.stream()
                    .map(this::convertTransletRuleToApon)
                    .collect(Collectors.toList());
            anatomyData.put("transletRules", ruleData);
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
            anatomyData.put("beanRules", ruleData);
        }

        // 3. Aspect Rules
        if (context.getAspectRuleRegistry() != null) {
            Collection<AspectRule> rules = context.getAspectRuleRegistry().getAspectRules();
            List<Map<String, Object>> ruleData = rules.stream()
                    .map(this::convertAspectRuleToApon)
                    .collect(Collectors.toList());
            anatomyData.put("aspectRules", ruleData);
        }

        // 4. Schedule Rules
        if (context.getScheduleRuleRegistry() != null) {
            Collection<ScheduleRule> rules = context.getScheduleRuleRegistry().getScheduleRules();
            List<Map<String, Object>> ruleData = rules.stream()
                    .map(this::convertScheduleRuleToApon)
                    .collect(Collectors.toList());
            anatomyData.put("scheduleRules", ruleData);
        }

        // 5. Template Rules
        if (context.getTemplateRenderer() instanceof DefaultTemplateRenderer renderer) {
            Collection<TemplateRule> rules = renderer.getTemplateRules();
            List<Map<String, Object>> ruleData = rules.stream()
                    .map(this::convertTemplateRuleToApon)
                    .collect(Collectors.toList());
            anatomyData.put("templateRules", ruleData);
        }

        return anatomyData;
    }

    @NonNull
    private Map<String, Object> convertTransletRuleToApon(@NonNull TransletRule rule) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", rule.getName());
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

}
