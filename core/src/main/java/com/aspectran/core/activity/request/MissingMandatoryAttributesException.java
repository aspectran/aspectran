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
package com.aspectran.core.activity.request;

import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.util.Collection;

/**
 * A checked exception thrown to indicate that the required attributes are
 * missing from the request.
 *
 * <p>Created: 2019. 6. 5.</p>
 *
 * @since 6.1.0
 */
public class MissingMandatoryAttributesException extends RequestParseException {

    @Serial
    private static final long serialVersionUID = 797995027591720096L;

    private final Collection<ItemRule> itemRules;

    /**
     * Creates a new MissingMandatoryAttributesException referencing
     * the specified attribute.
     * @param itemRules an item rule list that represents missing attributes
     */
    public MissingMandatoryAttributesException(@NonNull Collection<ItemRule> itemRules) {
        super("Missing mandatory attributes: " + StringUtils.joinWithCommas(itemRules));
        this.itemRules = itemRules;
    }

    /**
     * Returns an item rule list that represents missing attributes.
     * @return an item rule list that represents missing attributes
     */
    @NonNull
    public Collection<ItemRule> getItemRules() {
        return itemRules;
    }

}
