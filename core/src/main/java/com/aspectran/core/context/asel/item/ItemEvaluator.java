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
package com.aspectran.core.context.asel.item;

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.utils.MultiValueMap;

import java.util.Collection;
import java.util.Map;

/**
 * Defines the contract for evaluating {@link com.aspectran.core.context.rule.ItemRule}
 * instances to resolve their runtime values.
 * <p>This interface provides methods to convert item definitions into various
 * data structures, such as maps, lists, or simple values, by evaluating any
 * embedded AsEL tokens or bean references.</p>
 *
 * @since 2010. 5. 6.
 */
public interface ItemEvaluator {

    /**
     * Evaluates an {@link ItemRuleMap} and returns the results as a map of item names to their resolved values.
     * @param itemRuleMap the map of item rules to evaluate
     * @return a map containing the evaluated key-value pairs
     */
    Map<String, Object> evaluate(ItemRuleMap itemRuleMap);

    /**
     * Evaluates an {@link ItemRuleMap} and populates the given map with the results.
     * @param itemRuleMap the map of item rules to evaluate
     * @param valueMap the map to be populated with the evaluated key-value pairs
     */
    void evaluate(ItemRuleMap itemRuleMap, Map<String, Object> valueMap);

    /**
     * Evaluates a single {@link ItemRule} and returns its resolved value.
     * @param <V> the expected type of the resolved value
     * @param itemRule the item rule to evaluate
     * @return the resolved value of the item
     */
    <V> V evaluate(ItemRule itemRule);

    /**
     * Evaluates an {@link ItemRuleMap} and returns the results as a {@link MultiValueMap}.
     * @param itemRuleMap the map of item rules to evaluate
     * @return a {@code MultiValueMap} containing the evaluated key-value pairs
     */
    MultiValueMap<String, String> evaluateAsMultiValueMap(ItemRuleMap itemRuleMap);

    /**
     * Evaluates a collection of {@link ItemRule} instances and returns the results as a {@link MultiValueMap}.
     * @param itemRuleList the collection of item rules to evaluate
     * @return a {@code MultiValueMap} containing the evaluated key-value pairs
     */
    MultiValueMap<String, String> evaluateAsMultiValueMap(Collection<ItemRule> itemRuleList);

    /**
     * Evaluates an {@link ItemRuleMap} and returns the results as a {@link ParameterMap}.
     * @param itemRuleMap the map of item rules to evaluate
     * @return a {@code ParameterMap} containing the evaluated key-value pairs
     */
    ParameterMap evaluateAsParameterMap(ItemRuleMap itemRuleMap);

    /**
     * Evaluates a collection of {@link ItemRule} instances and returns the results as a {@link ParameterMap}.
     * @param itemRules the collection of item rules to evaluate
     * @return a {@code ParameterMap} containing the evaluated key-value pairs
     */
    ParameterMap evaluateAsParameterMap(Collection<ItemRule> itemRules);

    /**
     * Evaluates a single {@link ItemRule} and returns its resolved value(s) as a string array.
     * @param itemRule the item rule to evaluate
     * @return a string array representing the resolved value(s)
     */
    String[] evaluateAsStringArray(ItemRule itemRule);

}
