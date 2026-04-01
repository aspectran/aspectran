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
package com.aspectran.core.context.env;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.TokenType;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An {@link Environment} implementation specific to the {@link ActivityContext}.
 *
 * <p>This class manages profiles via {@link EnvironmentProfiles} and provides a
 * unique approach to properties. Instead of holding static property values,
 * it stores {@link ItemRule} instances. Property values are resolved at runtime
 * by evaluating these rules within the context of the current {@link Activity}.
 * Resolved values are cached for subsequent access if they contain property
 * references, ensuring consistency when one property depends on another.</p>
 */
public class ActivityEnvironment implements Environment {

    private final ActivityContext context;

    private final EnvironmentProfiles environmentProfiles;

    private final ItemRuleMap propertyItemRuleMap = new ItemRuleMap();

    private final Map<String, Object> propertyCache = new ConcurrentHashMap<>();

    /**
     * Instantiates a new activity environment.
     * @param context the activity context
     * @param environmentProfiles the environment profiles
     */
    public ActivityEnvironment(ActivityContext context, EnvironmentProfiles environmentProfiles) {
        this.context = context;
        this.environmentProfiles = environmentProfiles;
    }

    @Override
    public String[] getBaseProfiles() {
        return environmentProfiles.getBaseProfiles();
    }

    @Override
    public String[] getDefaultProfiles() {
        return environmentProfiles.getDefaultProfiles();
    }

    @Override
    public String[] getActiveProfiles() {
        return environmentProfiles.getActiveProfiles();
    }

    @Override
    public String[] getCurrentProfiles() {
        String[] activeProfiles = getActiveProfiles();
        if (activeProfiles.length > 0) {
            return activeProfiles;
        } else {
            return getDefaultProfiles();
        }
    }

    @Override
    public boolean matchesProfiles(String profileExpression) {
        return environmentProfiles.matchesProfiles(profileExpression);
    }

    @Override
    public boolean acceptsProfiles(Profiles profiles) {
        return environmentProfiles.acceptsProfiles(profiles);
    }

    @Override
    public boolean acceptsProfiles(String... profiles) {
        return environmentProfiles.acceptsProfiles(profiles);
    }

    @Override
    public void addActiveProfile(String profile) {
        environmentProfiles.addActiveProfile(profile);
    }

    @Override
    public <T> T getProperty(String name) {
        return getProperty(name, context.getAvailableActivity());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name, Activity activity) {
        Object value = propertyCache.get(name);
        if (value == null) {
            ItemRule itemRule = propertyItemRuleMap.get(name);
            if (itemRule != null && activity != null) {
                value = activity.getItemEvaluator().evaluate(itemRule);
                if (value != null && itemRule.hasToken(TokenType.PROPERTY)) {
                    Object existing = propertyCache.putIfAbsent(name, value);
                    if (existing != null) {
                        value = existing;
                    }
                }
            }
        }
        return (T)value;
    }

    @Override
    public Iterator<String> getPropertyNames() {
        return propertyItemRuleMap.keySet().iterator();
    }

    /**
     * Adds an item rule for a property.
     * @param propertyItemRule the property item rule
     */
    protected void putPropertyItemRule(ItemRule propertyItemRule) {
        this.propertyItemRuleMap.putItemRule(propertyItemRule);
    }

}
