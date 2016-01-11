package com.aspectran.core.context.template;

import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TemplateRuleMap;

/**
 * Created by gulendol on 2016. 1. 11..
 */
public class TemplateRuleRegistry {

    private TemplateRuleMap templateRuleMap;

    public TemplateRuleRegistry(TemplateRuleMap templateRuleMap) {
        this.templateRuleMap = templateRuleMap;
    }

    public TemplateRuleMap getTemplateRuleMap() {
        return templateRuleMap;
    }

    public boolean contains(String templateId) {
        return templateRuleMap.containsKey(templateId);
    }

    public TemplateRule getTemplateRule(String templateId) {
        return templateRuleMap.get(templateId);
    }

    public void destroy() {
        if(templateRuleMap != null) {
            templateRuleMap.clear();
            templateRuleMap = null;
        }
    }

}
