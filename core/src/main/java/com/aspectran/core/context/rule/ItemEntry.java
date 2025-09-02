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
 * <p>Created: 2025-09-01</p>
 */
public class ItemEntry {

    private ItemRule itemRule;

    private String name;

    private String value;

    private boolean tokenizable;

    private ItemEntry(ItemRule itemRule, String name, String value, boolean tokenizable) {
        this.itemRule = itemRule;
        this.name = name;
        this.value = value;
        this.tokenizable = tokenizable;
    }

    public ItemRule getItemRule() {
        return itemRule;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isTokenizable() {
        return tokenizable;
    }

    public void setTokenizable(boolean tokenizable) {
        this.tokenizable = tokenizable;
    }

    @NonNull
    public static ItemEntry of(@NonNull ItemRule itemRule, String name, String value, String tokenize) {
        boolean tokenizable = BooleanUtils.toBoolean(
                BooleanUtils.toNullableBooleanObject(tokenize),
                itemRule.isTokenize());
        return new ItemEntry(itemRule, name, value, tokenizable);
    }

}
