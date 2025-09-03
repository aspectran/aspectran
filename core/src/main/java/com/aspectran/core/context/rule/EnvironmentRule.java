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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.env.Profiles;
import com.aspectran.core.context.rule.ability.Describable;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a set of properties that are only active when a specific profile is enabled.
 * This is used to provide environment-specific configuration (e.g., for development vs. production).
 *
 * <p>Created: 2016. 05. 06 PM 11:23:35</p>
 */
public class EnvironmentRule implements Describable {

    private String profile;

    private Profiles profiles;

    private List<ItemRuleMap> propertyItemRuleMapList;

    private DescriptionRule descriptionRule;

    /**
     * Gets the profile expression.
     * @return the profile expression
     */
    public String getProfile() {
        return profile;
    }

    /**
     * Sets the profile expression that determines if this rule should be active.
     * @param profile the profile expression
     */
    public void setProfile(String profile) {
        this.profile = profile;
        this.profiles = (profile != null ? Profiles.of(profile) : null);
    }

    /**
     * Gets the parsed profiles.
     * @return the profiles
     */
    public Profiles getProfiles() {
        return profiles;
    }

    /**
     * Gets the list of property maps for this environment.
     * @return the list of property item rule maps
     */
    public List<ItemRuleMap> getPropertyItemRuleMapList() {
        return propertyItemRuleMapList;
    }

    /**
     * Sets the list of property maps for this environment.
     * @param propertyItemRuleMapList the list of property item rule maps
     */
    public void setPropertyItemRuleMapList(List<ItemRuleMap> propertyItemRuleMapList) {
        this.propertyItemRuleMapList = propertyItemRuleMapList;
    }

    /**
     * Adds a property map to this environment.
     * @param itemRuleMap the property item rule map to add
     */
    public void addPropertyItemRuleMap(ItemRuleMap itemRuleMap) {
        if (propertyItemRuleMapList == null) {
            propertyItemRuleMapList = new ArrayList<>();
        }
        propertyItemRuleMapList.add(itemRuleMap);
    }

    @Override
    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    @Override
    public void setDescriptionRule(DescriptionRule descriptionRule) {
        this.descriptionRule = descriptionRule;
    }

    /**
     * Creates a new instance of EnvironmentRule.
     * @param profile the profile expression
     * @return an instance of EnvironmentRule
     */
    @NonNull
    public static EnvironmentRule newInstance(String profile) {
        EnvironmentRule environmentRule = new EnvironmentRule();
        if (StringUtils.hasText(profile)) {
            environmentRule.setProfile(profile);
        }
        return environmentRule;
    }

}
