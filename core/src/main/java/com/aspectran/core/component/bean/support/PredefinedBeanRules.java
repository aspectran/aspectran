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
package com.aspectran.core.component.bean.support;

import com.aspectran.core.component.bean.async.AsyncTaskExecutor;
import com.aspectran.core.component.bean.async.DefaultAsyncTaskExecutor;
import com.aspectran.core.context.config.AsyncConfig;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * <p>Created: 2025-08-24</p>
 */
public abstract class PredefinedBeanRules {

    @NonNull
    public static BeanRule createDefaultAsyncTaskExecutorBeanRule(@NonNull AsyncConfig asyncConfig) {
        BeanRule beanRule = new BeanRule();
        beanRule.setId(AsyncTaskExecutor.DEFAULT_TASK_EXECUTOR_BEAN_ID);
        beanRule.setBeanClass(DefaultAsyncTaskExecutor.class);
        beanRule.setScopeType(ScopeType.SINGLETON);

        ItemRuleMap propertyItems = new ItemRuleMap();
        if (asyncConfig.getCorePoolSize() > 0) {
            ItemRule itemRule = new ItemRule();
            itemRule.setName("corePoolSize");
            itemRule.setValue(String.valueOf(asyncConfig.getCorePoolSize()));
            itemRule.setValueType(ItemValueType.INT);
            propertyItems.putItemRule(itemRule);
        }
        if (asyncConfig.getMaxPoolSize() > 0) {
            ItemRule itemRule = new ItemRule();
            itemRule.setName("maxPoolSize");
            itemRule.setValue(String.valueOf(asyncConfig.getMaxPoolSize()));
            itemRule.setValueType(ItemValueType.INT);
            propertyItems.putItemRule(itemRule);
        }
        if (asyncConfig.getKeepAliveSeconds() > 0) {
            ItemRule itemRule = new ItemRule();
            itemRule.setName("keepAliveSeconds");
            itemRule.setValue(String.valueOf(asyncConfig.getKeepAliveSeconds()));
            itemRule.setValueType(ItemValueType.INT);
            propertyItems.putItemRule(itemRule);
        }
        if (asyncConfig.getQueueCapacity() > 0) {
            ItemRule itemRule = new ItemRule();
            itemRule.setName("queueCapacity");
            itemRule.setValue(String.valueOf(asyncConfig.getQueueCapacity()));
            itemRule.setValueType(ItemValueType.INT);
            propertyItems.putItemRule(itemRule);
        }
        if (asyncConfig.isWaitForTasksToCompleteOnShutdown()) {
            ItemRule itemRule = new ItemRule();
            itemRule.setName("waitForTasksToCompleteOnShutdown");
            itemRule.setValue(String.valueOf(asyncConfig.isWaitForTasksToCompleteOnShutdown()));
            itemRule.setValueType(ItemValueType.BOOLEAN);
            propertyItems.putItemRule(itemRule);
        }
        if (!propertyItems.isEmpty()) {
            beanRule.setPropertyItemRuleMap(propertyItems);
        }
        return beanRule;
    }

}
