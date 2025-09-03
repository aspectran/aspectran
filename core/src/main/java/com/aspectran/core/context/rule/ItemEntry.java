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

import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Represents a single key-value entry within a map-type {@link ItemRule}.
 * This class is used to construct map-based properties or arguments during the configuration parsing process.
 *
 * <p>Created: 2025-09-01</p>
 */
public class ItemEntry {

    private final ItemRule itemRule;

    private String name;

    private String value;

    private boolean tokenizable;

    private ItemEntry(ItemRule itemRule, String name, String value, boolean tokenizable) {
        this.itemRule = itemRule;
        this.name = name;
        this.value = value;
        this.tokenizable = tokenizable;
    }

    /**
     * Gets the parent item rule.
     * @return the item rule
     */
    public ItemRule getItemRule() {
        return itemRule;
    }

    /**
     * Gets the name of the entry.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the entry.
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the value of the entry.
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the entry.
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns whether the value can be tokenized.
     * @return true if tokenizable, false otherwise
     */
    public boolean isTokenizable() {
        return tokenizable;
    }

    /**
     * Sets whether the value can be tokenized.
     * @param tokenizable true if tokenizable
     */
    public void setTokenizable(boolean tokenizable) {
        this.tokenizable = tokenizable;
    }

    /**
     * Creates a new ItemEntry.
     * @param itemRule the parent item rule
     * @param name the name of the entry
     * @param value the value of the entry
     * @param tokenize whether to tokenize the value
     * @return a new ItemEntry instance
     */
    @NonNull
    public static ItemEntry of(@NonNull ItemRule itemRule, String name, String value, String tokenize) {
        boolean tokenizable = BooleanUtils.toBoolean(
                BooleanUtils.toNullableBooleanObject(tokenize),
                itemRule.isTokenize());
        return new ItemEntry(itemRule, name, value, tokenizable);
    }

}
