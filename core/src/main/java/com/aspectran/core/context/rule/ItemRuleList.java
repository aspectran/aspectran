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

import java.util.ArrayList;
import java.util.Collection;

/**
 * The Class ItemRuleList.
 *
 * <p>Created: 2016. 08. 22.</p>
 */
public class ItemRuleList extends ArrayList<ItemRule> {

    /** @serial */
    private static final long serialVersionUID = -7578440777195693622L;

    public ItemRuleList() {
        super();
    }

    public ItemRuleList(int initialCapacity) {
        super(initialCapacity);
    }

    public ItemRuleList(Collection<ItemRule> itemRules) {
        super(itemRules);
    }

    public String[] getItemNames() {
        String[] itemNames = new String[size()];
        for (int i = 0; i < itemNames.length; i++) {
            itemNames[i] = get(i).getName();
        }
        return itemNames;
    }

}
