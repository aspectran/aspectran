/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.context.rule;

import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class EnvironmentRule.
 * 
 * <p>Created: 2016. 05. 06 PM 11:23:35</p>
 */
public class EnvironmentRule {

    private String profile;

    private ItemRuleMap propertyItemRuleMap;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    /**
     * Gets the property item rule map.
     *
     * @return the property item rule map
     */
    public ItemRuleMap getPropertyItemRuleMap() {
        return propertyItemRuleMap;
    }

    /**
     * Sets the property item rule map.
     *
     * @param propertyItemRuleMap the new property item rule map
     */
    public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
        this.propertyItemRuleMap = propertyItemRuleMap;
    }

    /**
     * Adds a new property rule with the specified name and returns it.
     *
     * @param propertyName the property name
     * @return the property item rule
     */
    public ItemRule newPropertyItemRule(String propertyName) {
        ItemRule itemRule = new ItemRule();
        itemRule.setName(propertyName);
        addPropertyItemRule(itemRule);
        return itemRule;
    }

    /**
     * Adds the property item rule.
     *
     * @param propertyItemRule the property item rule
     */
    public void addPropertyItemRule(ItemRule propertyItemRule) {
        if (propertyItemRuleMap == null) {
            propertyItemRuleMap = new ItemRuleMap();
        }
        propertyItemRuleMap.putItemRule(propertyItemRule);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("profile", profile);
        if (propertyItemRuleMap != null) {
            tsb.append("properties", propertyItemRuleMap.keySet());
        }
        return tsb.toString();
    }

    /**
     * Returns a new instance of EnvironmentRule.
     *
     * @param profile the profile
     * @param propertyItemRuleMap the property item rule map
     * @return an instance of EnvironmentRule
     */
    public static EnvironmentRule newInstance(String profile, ItemRuleMap propertyItemRuleMap) {
        EnvironmentRule environmentRule = new EnvironmentRule();
        environmentRule.setProfile(profile);
        environmentRule.setPropertyItemRuleMap(propertyItemRuleMap);
        return environmentRule;
    }

}
