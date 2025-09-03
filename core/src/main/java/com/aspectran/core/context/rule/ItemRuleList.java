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

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A specialized {@link java.util.ArrayList} for holding a list of {@link ItemRule} objects.
 * This provides a type-safe collection for rules that contain a list of items,
 * such as arguments or properties.
 *
 * <p>Created: 2016. 08. 22.</p>
 */
public class ItemRuleList extends ArrayList<ItemRule> {

    @Serial
    private static final long serialVersionUID = -7578440777195693622L;

    /**
     * Instantiates a new ItemRuleList.
     */
    public ItemRuleList() {
        super();
    }

    /**
     * Instantiates a new ItemRuleList with the specified initial capacity.
     * @param initialCapacity the initial capacity of the list
     */
    public ItemRuleList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Instantiates a new ItemRuleList with the specified collection of item rules.
     * @param itemRules the collection whose elements are to be placed into this list
     */
    public ItemRuleList(Collection<ItemRule> itemRules) {
        super(itemRules);
    }

    /**
     * Gets the names of all items in this list.
     * @return an array of item names
     */
    public String[] getItemNames() {
        String[] itemNames = new String[size()];
        for (int i = 0; i < itemNames.length; i++) {
            itemNames[i] = get(i).getName();
        }
        return itemNames;
    }

}
