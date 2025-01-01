/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
