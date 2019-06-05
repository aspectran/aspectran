/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.activity.request;

import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.util.StringUtils;

/**
 * An exception to indicate mandatory attributes missing from the request.
 *
 * <p>Created: 2019. 6. 5.</p>
 *
 * @since 6.1.0
 */
public class MissingMandatoryAttributesException extends RequestException {

    /** @serial */
    private static final long serialVersionUID = 797995027591720096L;

    private final ItemRuleList itemRuleList;

    /**
     * Creates a new MissingMandatoryAttributesException referencing
     * the specified attribute.
     *
     * @param itemRuleList an item rule list that represents missing attributes
     */
    public MissingMandatoryAttributesException(ItemRuleList itemRuleList) {
        super("Missing mandatory attributes: " + StringUtils.joinCommaDelimitedList(itemRuleList));
        this.itemRuleList = itemRuleList;
    }

    /**
     * Returns an item rule list that represents missing attributes.
     *
     * @return an item rule list that represents missing attributes
     */
    public ItemRuleList getItemRuleList() {
        return itemRuleList;
    }

}
