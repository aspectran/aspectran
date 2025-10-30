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
 * Defines the contract for an evaluator that resolves the values of {@link com.aspectran.core.context.rule.ItemRule}s.
 * <p>This interface acts as a bridge between static configuration rules (e.g., {@code <parameters>},
 * {@code <attributes>}) and the dynamic, typed objects used during an activity's execution.
 * An implementation processes {@link com.aspectran.core.context.rule.ItemRule} instances,
 * resolves any embedded AsEL tokens, and constructs the final data structures such as
 * Maps, Lists, or other objects as defined by the rule.</p>
 *
 * @since 2010. 5. 6.
 * @see com.aspectran.core.context.rule.ItemRule
 * @see ItemEvaluation
 */
public interface ItemEvaluator {

    /**
     * Evaluates all item rules within the given {@link ItemRuleMap} and returns the results
     * as a map of item names to their resolved values.
     * <p>Each item's value is resolved by evaluating its defined tokens or bean references.</p>
     * @param itemRuleMap the map of item rules to evaluate
     * @return a new map containing the evaluated key-value pairs
     */
    Map<String, Object> evaluate(ItemRuleMap itemRuleMap);

    /**
     * Evaluates all item rules within the given {@link ItemRuleMap} and populates an existing
     * map with the results.
     * <p>This method is useful for aggregating values into a pre-existing map.</p>
     * @param itemRuleMap the map of item rules to evaluate
     * @param valueMap the map to be populated with the evaluated key-value pairs
     */
    void evaluate(ItemRuleMap itemRuleMap, Map<String, Object> valueMap);

    /**
     * Evaluates a single {@link ItemRule} and returns its fully resolved value.
     * <p>The type of the returned object depends on the item's definition (e.g.,
     * String, List, Map, or a bean instance).</p>
     * @param <T> the expected type of the resolved value
     * @param itemRule the item rule to evaluate
     * @return the resolved value of the item
     */
    <T> T evaluate(ItemRule itemRule);

    /**
     * Evaluates an {@link ItemRuleMap} and converts the results into a {@link MultiValueMap},
     * where each value is represented as a string.
     * @param itemRuleMap the map of item rules to evaluate
     * @return a new {@code MultiValueMap} containing the evaluated key-value pairs
     */
    MultiValueMap<String, String> evaluateAsMultiValueMap(ItemRuleMap itemRuleMap);

    /**
     * Evaluates a collection of {@link ItemRule} instances and converts the results into a
     * {@link MultiValueMap}, where each value is represented as a string.
     * @param itemRules the collection of item rules to evaluate
     * @return a new {@code MultiValueMap} containing the evaluated key-value pairs
     */
    MultiValueMap<String, String> evaluateAsMultiValueMap(Collection<ItemRule> itemRules);

    /**
     * Evaluates an {@link ItemRuleMap} and converts the results into a {@link ParameterMap},
     * a structure commonly used for request parameters.
     * @param itemRuleMap the map of item rules to evaluate
     * @return a new {@code ParameterMap} containing the evaluated key-value pairs
     */
    ParameterMap evaluateAsParameterMap(ItemRuleMap itemRuleMap);

    /**
     * Evaluates a collection of {@link ItemRule} instances and converts the results into a
     * {@link ParameterMap}.
     * @param itemRules the collection of item rules to evaluate
     * @return a new {@code ParameterMap} containing the evaluated key-value pairs
     */
    ParameterMap evaluateAsParameterMap(Collection<ItemRule> itemRules);

    /**
     * Evaluates a single {@link ItemRule} and coerces its resolved value(s) into a
     * string array.
     * <p>If the item resolves to a single value, it will be an array with one element.
     * If it resolves to a collection or array, each element will be converted to a string.</p>
     * @param itemRule the item rule to evaluate
     * @return a string array representing the resolved value(s)
     */
    String[] evaluateAsStringArray(ItemRule itemRule);

}
