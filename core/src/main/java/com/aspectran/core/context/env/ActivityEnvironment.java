/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.ItemEvaluation;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;

/**
 * The Class ActivityEnvironment.
 */
public class ActivityEnvironment implements Environment {

    private final EnvironmentProfiles environmentProfiles;

    private final ActivityContext activityContext;

    private ItemRuleMap propertyItemRuleMap;

    public ActivityEnvironment(EnvironmentProfiles environmentProfiles, ActivityContext activityContext) {
        this.environmentProfiles = environmentProfiles;
        this.activityContext = activityContext;
    }

    @Override
    public String[] getActiveProfiles() {
        return environmentProfiles.getActiveProfiles();
    }

    @Override
    public String[] getDefaultProfiles() {
        return environmentProfiles.getDefaultProfiles();
    }

    @Override
    public boolean acceptsProfiles(String... profiles) {
        return environmentProfiles.acceptsProfiles(profiles);
    }

    public ItemRuleMap getPropertyItemRuleMap() {
        return propertyItemRuleMap;
    }

    public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
        this.propertyItemRuleMap = propertyItemRuleMap;
    }

    public void addPropertyItemRule(ItemRuleMap propertyItemRuleMap) {
        if (this.propertyItemRuleMap == null) {
            this.propertyItemRuleMap = new ItemRuleMap(propertyItemRuleMap);
        } else {
            this.propertyItemRuleMap.putAll(propertyItemRuleMap);
        }
    }

    @Override
    public <T> T getProperty(String name) {
        return getProperty(name, activityContext.getAvailableActivity());
    }

    @Override
    public <T> T getProperty(String name, Activity activity) {
        if (propertyItemRuleMap == null || propertyItemRuleMap.isEmpty()) {
            return null;
        }
        ItemRule itemRule = propertyItemRuleMap.get(name);
        if (itemRule != null) {
            ItemEvaluator evaluator = new ItemEvaluation(activity);
            return evaluator.evaluate(itemRule);
        } else {
            return null;
        }
    }

}
