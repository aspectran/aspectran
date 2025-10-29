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
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A specialized {@link java.util.LinkedHashMap} for holding a collection of named {@link ItemRule} objects.
 * It supports profile-based rules, allowing different sets of items to be merged based on the active
 * environment profile. This is the primary data structure for storing properties, attributes, and parameters.
 *
 * <p>Created: 2008. 03. 29 PM 5:00:20</p>
 */
public class ItemRuleMap extends LinkedHashMap<String, ItemRule> {

    @Serial
    private static final long serialVersionUID = 192817512158305803L;

    private String profile;

    private Profiles profiles;

    private List<ItemRuleMap> candidates;

    /**
     * Instantiates a new ItemRuleMap.
     */
    public ItemRuleMap() {
        super();
    }

    /**
     * Adds an item rule to the map. If the item rule has no name, a name is auto-generated.
     * @param itemRule the item rule to add
     * @return the previous value associated with the key, or null if there was no mapping
     */
    public ItemRule putItemRule(@NonNull ItemRule itemRule) {
        if (itemRule.isAutoNamed()) {
            autoNaming(itemRule);
        }
        return put(itemRule.getName(), itemRule);
    }

    /**
     * Auto-generates a name for an unnamed item.
     * @param itemRule the item rule to name
     */
    private void autoNaming(@NonNull ItemRule itemRule) {
        if (itemRule.getName() == null) {
            itemRule.setName("item#" + size());
        }
    }

    /**
     * Gets the profile expression.
     * @return the profile expression
     */
    public String getProfile() {
        return profile;
    }

    /**
     * Sets the profile expression that determines if this map should be active.
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
     * Gets the list of candidate item rule maps for different profiles.
     * @return the list of candidate maps
     */
    public List<ItemRuleMap> getCandidates() {
        return candidates;
    }

    /**
     * Sets the list of candidate item rule maps.
     * @param candidates the list of candidate maps
     */
    public void setCandidates(List<ItemRuleMap> candidates) {
        this.candidates = candidates;
    }

    /**
     * Adds a candidate item rule map.
     * @param itemRuleMap the candidate map to add
     * @return true if the candidate was added successfully
     */
    public boolean addCandidate(ItemRuleMap itemRuleMap) {
        if (candidates == null) {
            candidates = new LinkedList<>();
        }
        return candidates.add(itemRuleMap);
    }

}
