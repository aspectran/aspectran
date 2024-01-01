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
package com.aspectran.core.context.expr;

import com.aspectran.core.context.rule.ItemRule;

/**
 * The Class ItemEvaluationException.
 */
public class ItemEvaluationException extends RuntimeException {

    private static final long serialVersionUID = -139399791062499758L;

    private final ItemRule itemRule;

    /**
     * Instantiates a new item evaluation exception.
     * @param itemRule the item rule
     * @param cause the root cause
     */
    public ItemEvaluationException(ItemRule itemRule, Throwable cause) {
        super("Failed to evaluate item " + itemRule, cause);
        this.itemRule = itemRule;
    }

    /**
     * Gets the item rule which is failed to evaluate expression.
     * @return the item rule
     */
    public ItemRule getItemRule() {
        return this.itemRule;
    }

}
