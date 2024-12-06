package com.aspectran.core.context.env;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.utils.Assert;

/**
 * <p>Created: 2024-12-06</p>
 */
public class ActivityEnvironmentBuilder {

    private final ActivityContext context;

    private final EnvironmentProfiles environmentProfiles;

    private final ItemRuleMap propertyItemRuleMap = new ItemRuleMap();

    public ActivityEnvironmentBuilder(ActivityContext context, EnvironmentProfiles environmentProfiles) {
        Assert.notNull(context, "ActivityContext must not be null");
        Assert.notNull(environmentProfiles, "EnvironmentProfiles must not be null");
        this.context = context;
        this.environmentProfiles = environmentProfiles;
    }

    public ActivityEnvironmentBuilder putPropertyItemRules(ItemRuleMap propertyItemRuleMap) {
        if (propertyItemRuleMap != null) {
            this.propertyItemRuleMap.putAll(propertyItemRuleMap);
        }
        return this;
    }

    public ActivityEnvironment build() {
        ActivityEnvironment activityEnvironment = new ActivityEnvironment(context, environmentProfiles);
        for (ItemRule itemRule : propertyItemRuleMap.values()) {
            activityEnvironment.putPropertyItemRule(itemRule);
        }
        return activityEnvironment;
    }

}
