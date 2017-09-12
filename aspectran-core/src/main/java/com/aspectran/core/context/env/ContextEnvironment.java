/*
 * Copyright 2008-2017 Juho Jeong
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
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;

public class ContextEnvironment extends AbstractEnvironment {

    private final ActivityContext context;

    private ItemRuleMap propertyItemRuleMap;

    public ContextEnvironment(ActivityContext context) {
        this.context = context;
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return context.getApplicationAdapter();
    }

    public ItemRuleMap getPropertyItemRuleMap() {
        return propertyItemRuleMap;
    }

    public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
        this.propertyItemRuleMap = propertyItemRuleMap;
    }

    public void addPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
        if (this.propertyItemRuleMap == null) {
            this.propertyItemRuleMap = propertyItemRuleMap;
        } else {
            this.propertyItemRuleMap.putAll(propertyItemRuleMap);
        }
    }

    public <T> T getProperty(String name) {
        if (propertyItemRuleMap == null) {
            return null;
        }

        ItemRule itemRule = propertyItemRuleMap.get(name);
        if (itemRule == null) {
            return null;
        }

        Activity activity = context.getCurrentActivity();

        ItemEvaluator evaluator = new ItemExpressionParser(activity);
        return evaluator.evaluate(itemRule);
    }

}
