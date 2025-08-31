package com.aspectran.core.context.rule.ability;

import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;

public interface HasParameters {

    ItemRuleMap getParameterItemRuleMap();

    void setParameterItemRuleMap(ItemRuleMap parameterItemRuleMap);

    ItemRule newParameterItemRule(String parameterName);

    void addParameterItemRule(ItemRule parameterItemRule);
}
