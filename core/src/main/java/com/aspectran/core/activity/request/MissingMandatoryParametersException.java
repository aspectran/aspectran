/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
 * A checked exception thrown to indicate that the required parameters are
 * missing from the request.
 *
 * <p>Created: 2016. 8. 21.</p>
 *
 * @since 3.0.0
 */
public class MissingMandatoryParametersException extends RequestParseException {

    @Serial
    private static final long serialVersionUID = 6311784727928597298L;

    private final Collection<ItemRule> itemRules;

    /**
     * Creates a new MissingMandatoryParametersException referencing
     * the specified parameter.
     * @param itemRules an item rule list that represents missing parameters
     */
    public MissingMandatoryParametersException(@NonNull Collection<ItemRule> itemRules) {
        super("Missing mandatory parameters: " + StringUtils.joinCommaDelimitedList(itemRules));
        this.itemRules = itemRules;
    }

    /**
     * Returns an item rule list that represents missing parameters.
     * @return an item rule list that represents missing parameters
     */
    @NonNull
    public Collection<ItemRule> getItemRules() {
        return itemRules;
    }

}
