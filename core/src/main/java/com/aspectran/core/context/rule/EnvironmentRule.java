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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.env.Profiles;
import com.aspectran.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class EnvironmentRule.
 *
 * <p>Created: 2016. 05. 06 PM 11:23:35</p>
 */
public class EnvironmentRule {

    private String profile;

    private Profiles profiles;

    private List<ItemRuleMap> propertyItemRuleMapList;

    private DescriptionRule descriptionRule;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
        this.profiles = (profile != null ? Profiles.of(profile) : null);
    }

    public Profiles getProfiles() {
        return profiles;
    }

    public List<ItemRuleMap> getPropertyItemRuleMapList() {
        return propertyItemRuleMapList;
    }

    public void setPropertyItemRuleMapList(List<ItemRuleMap> propertyItemRuleMapList) {
        this.propertyItemRuleMapList = propertyItemRuleMapList;
    }

    public void addPropertyItemRuleMap(ItemRuleMap itemRuleMap) {
        if (propertyItemRuleMapList == null) {
            propertyItemRuleMapList = new ArrayList<>();
        }
        propertyItemRuleMapList.add(itemRuleMap);
    }

    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    public void setDescriptionRule(DescriptionRule descriptionRule) {
        this.descriptionRule = descriptionRule;
    }

    /**
     * Returns a new instance of EnvironmentRule.
     * @param profile the profile
     * @return an instance of EnvironmentRule
     */
    public static EnvironmentRule newInstance(String profile) {
        EnvironmentRule environmentRule = new EnvironmentRule();
        if (StringUtils.hasText(profile)) {
            environmentRule.setProfile(profile);
        }
        return environmentRule;
    }

}
