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
package com.aspectran.core.context.expr;

import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.util.MultiValueMap;

import java.util.Collection;
import java.util.Map;

/**
 * Evaluates token expressions in item rules.
 *
 * @since 2010. 5. 6.
 */
public interface ItemEvaluator {

    Map<String, Object> evaluate(ItemRuleMap itemRuleMap);

    void evaluate(ItemRuleMap itemRuleMap, Map<String, Object> valueMap);

    <T> T evaluate(ItemRule itemRule);

    MultiValueMap<String, String> evaluateAsMultiValueMap(ItemRuleMap itemRuleMap);

    MultiValueMap<String, String> evaluateAsMultiValueMap(Collection<ItemRule> itemRuleLIst);

    ParameterMap evaluateAsParameterMap(ItemRuleMap itemRuleMap);

    ParameterMap evaluateAsParameterMap(Collection<ItemRule> itemRules);

    String[] evaluateAsStringArray(ItemRule itemRule);

}
