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

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.TokenEvaluator;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.utils.LinkedMultiValueMap;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The default, concrete implementation of the {@link ItemEvaluator}.
 * <p>This class orchestrates the evaluation process by recursively resolving values
 * defined in an {@link ItemRule}. It uses a {@link TokenEvaluator} to handle the
 * low-level resolution of AsEL tokens and bean references, and then assembles the
 * results into the final, typed data structures (e.g., {@link List}, {@link Map},
 * {@link Properties}) as specified by the item rule.</p>
 *
 * @since 2008. 06. 19
 */
public class ItemEvaluation implements ItemEvaluator {

    private final TokenEvaluator tokenEvaluator;

    /**
     * Instantiates a new {@code ItemEvaluation}.
     * This evaluator relies on a token evaluator to resolve the underlying values
     * of items.
     * @param tokenEvaluator the token evaluator
     */
    public ItemEvaluation(TokenEvaluator tokenEvaluator) {
        this.tokenEvaluator = tokenEvaluator;
    }

    @Override
    public Map<String, Object> evaluate(ItemRuleMap itemRuleMap) {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        evaluate(itemRuleMap, valueMap);
        return valueMap;
    }

    @Override
    public void evaluate(ItemRuleMap itemRuleMap, Map<String, Object> valueMap) {
        if (itemRuleMap != null) {
            for (ItemRule itemRule : itemRuleMap.values()) {
                valueMap.put(itemRule.getName(), evaluate(itemRule));
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T evaluate(ItemRule itemRule) {
        try {
            ItemType itemType = itemRule.getType();
            ItemValueType valueType = itemRule.getValueType();
            Object value = null;
            if (itemType == ItemType.SINGLE) {
                if (valueType == ItemValueType.BEAN) {
                    value = evaluateBean(itemRule.getBeanRule());
                } else {
                    value = evaluate(itemRule.getTokens(), valueType);
                }
            } else if (itemType == ItemType.ARRAY) {
                if (valueType == ItemValueType.BEAN) {
                    value = evaluateBeanAsArray(itemRule.getBeanRuleList());
                } else {
                    value = evaluateAsArray(itemRule.getTokensList(), valueType);
                }
            } else if (itemType == ItemType.LIST) {
                if (valueType == ItemValueType.BEAN) {
                    value = evaluateBeanAsList(itemRule.getBeanRuleList());
                } else {
                    value = evaluateAsList(itemRule.getTokensList(), valueType);
                }
            } else if (itemType == ItemType.SET) {
                if (valueType == ItemValueType.BEAN) {
                    value = evaluateBeanAsSet(itemRule.getBeanRuleList());
                } else {
                    value = evaluateAsSet(itemRule.getTokensList(), valueType);
                }
            } else if (itemType == ItemType.MAP) {
                if (valueType == ItemValueType.BEAN) {
                    value = evaluateBeanAsMap(itemRule.getBeanRuleMap());
                } else {
                    value = evaluateAsMap(itemRule.getTokensMap(), valueType);
                }
            } else if (itemType == ItemType.PROPERTIES) {
                if (valueType == ItemValueType.BEAN) {
                    value = evaluateBeanAsProperties(itemRule.getBeanRuleMap());
                } else {
                    value = evaluateAsProperties(itemRule.getTokensMap(), valueType);
                }
            }
            return (T)value;
        } catch (Exception e) {
            throw new ItemEvaluationException(itemRule, e);
        }
    }

    @Override
    public MultiValueMap<String, String> evaluateAsMultiValueMap(@NonNull ItemRuleMap itemRuleMap) {
        return evaluateAsMultiValueMap(itemRuleMap.values());
    }

    @Override
    public MultiValueMap<String, String> evaluateAsMultiValueMap(@NonNull Collection<ItemRule> itemRules) {
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
        for (ItemRule itemRule : itemRules) {
            String[] values = evaluateAsStringArray(itemRule);
            valueMap.set(itemRule.getName(), values);
        }
        return valueMap;
    }

    @Override
    public ParameterMap evaluateAsParameterMap(@NonNull ItemRuleMap itemRuleMap) {
        return evaluateAsParameterMap(itemRuleMap.values());
    }

    @Override
    public ParameterMap evaluateAsParameterMap(@NonNull Collection<ItemRule> itemRules) {
        ParameterMap params = new ParameterMap();
        for (ItemRule itemRule : itemRules) {
            String[] values = evaluateAsStringArray(itemRule);
            params.put(itemRule.getName(), values);
        }
        return params;
    }

    @Override
    public String[] evaluateAsStringArray(ItemRule itemRule) {
        try {
            ItemType itemType = itemRule.getType();
            ItemValueType valueType = itemRule.getValueType();
            String[] values = null;
            if (itemType == ItemType.SINGLE) {
                Object value;
                if (valueType == ItemValueType.BEAN) {
                    value = evaluateBean(itemRule.getBeanRule());
                } else {
                    Token[] tokens = itemRule.getTokens();
                    value = evaluate(tokens, valueType);
                }
                if (value != null) {
                    if (value instanceof String[]) {
                        values = (String[])value;
                    } else {
                        values = new String[] { value.toString() };
                    }
                }
            } else if (itemType == ItemType.ARRAY) {
                Object[] arr;
                if (valueType == ItemValueType.BEAN) {
                    arr = evaluateBeanAsArray(itemRule.getBeanRuleList());
                } else {
                    arr = evaluateAsArray(itemRule.getTokensList(), valueType);
                }
                if (arr != null) {
                    if (arr instanceof String[]) {
                        values = (String[])arr;
                    } else {
                        values = Arrays.stream(arr).map(Object::toString).toArray(String[]::new);
                    }
                }
            } else if (itemType == ItemType.LIST) {
                List<Object> list;
                if (valueType == ItemValueType.BEAN) {
                    list = evaluateBeanAsList(itemRule.getBeanRuleList());
                } else {
                    list = evaluateAsList(itemRule.getTokensList(), valueType);
                }
                if (list != null) {
                    values = Arrays.stream(list.toArray()).map(Object::toString).toArray(String[]::new);
                }
            } else if (itemType == ItemType.SET) {
                Set<Object> set;
                if (valueType == ItemValueType.BEAN) {
                    set = evaluateBeanAsSet(itemRule.getBeanRuleList());
                } else {
                    set = evaluateAsSet(itemRule.getTokensList(), valueType);
                }
                if (set != null) {
                    values = Arrays.stream(set.toArray()).map(Object::toString).toArray(String[]::new);
                }
            } else if (itemType == ItemType.MAP) {
                Map<String, Object> map;
                if (valueType == ItemValueType.BEAN) {
                    map = evaluateBeanAsMap(itemRule.getBeanRuleMap());
                } else {
                    map = evaluateAsMap(itemRule.getTokensMap(), valueType);
                }
                if (map != null) {
                    values = new String[] { map.toString() };
                }
            } else if (itemType == ItemType.PROPERTIES) {
                Properties props;
                if (valueType == ItemValueType.BEAN) {
                    props = evaluateBeanAsProperties(itemRule.getBeanRuleMap());
                } else {
                    props = evaluateAsProperties(itemRule.getTokensMap(), valueType);
                }
                if (props != null) {
                    values = new String[] { props.toString() };
                }
            }
            return values;
        } catch (Exception e) {
            throw new ItemEvaluationException(itemRule, e);
        }
    }

    private Object evaluate(Token[] tokens, ItemValueType valueType) throws Exception {
        Object value = tokenEvaluator.evaluate(tokens);
        return (value == null || valueType == null ? value : valuelize(value, valueType));
    }

    @SuppressWarnings("all")
    @Nullable
    private Object[] evaluateAsArray(List<Token[]> tokensList, ItemValueType valueType)
            throws Exception {
        List<Object> list = evaluateAsList(tokensList, valueType);
        if (list == null) {
            return null;
        }
        if (valueType == ItemValueType.STRING) {
            return list.toArray(new String[0]);
        } else if (valueType == ItemValueType.INT) {
            return list.toArray(new Integer[0]);
        } else if (valueType == ItemValueType.LONG) {
            return list.toArray(new Long[0]);
        } else if (valueType == ItemValueType.FLOAT) {
            return list.toArray(new Float[0]);
        } else if (valueType == ItemValueType.DOUBLE) {
            return list.toArray(new Double[0]);
        } else if (valueType == ItemValueType.BOOLEAN) {
            return list.toArray(new Boolean[0]);
        } else if (valueType == ItemValueType.PARAMETERS) {
            return list.toArray(new Parameters[0]);
        } else if (valueType == ItemValueType.FILE) {
            return list.toArray(new File[0]);
        } else if (valueType == ItemValueType.MULTIPART_FILE) {
            return list.toArray(new FileParameter[0]);
        } else {
            return list.toArray(new Object[0]);
        }
    }

    private List<Object> evaluateAsList(List<Token[]> tokensList, ItemValueType valueType)
            throws Exception {
        if (tokensList == null || tokensList.isEmpty()) {
            return null;
        }
        List<Object> valueList = new ArrayList<>(tokensList.size());
        for (Token[] tokens : tokensList) {
            Object value = tokenEvaluator.evaluate(tokens);
            if (value != null && valueType != null) {
                value = valuelize(value, valueType);
            }
            valueList.add(value);
        }
        return valueList;
    }

    private Set<Object> evaluateAsSet(List<Token[]> tokensList, ItemValueType valueType)
            throws Exception {
        if (tokensList == null || tokensList.isEmpty()) {
            return null;
        }
        Set<Object> valueSet = new LinkedHashSet<>();
        for (Token[] tokens : tokensList) {
            Object value = tokenEvaluator.evaluate(tokens);
            if (value != null && valueType != null) {
                value = valuelize(value, valueType);
            }
            valueSet.add(value);
        }
        return valueSet;
    }

    private Map<String, Object> evaluateAsMap(Map<String, Token[]> tokensMap, ItemValueType valueType)
            throws Exception {
        if (tokensMap == null || tokensMap.isEmpty()) {
            return null;
        }
        Map<String, Object> valueMap = new LinkedHashMap<>();
        for (Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
            Object value = tokenEvaluator.evaluate(entry.getValue());
            if (value != null && valueType != null) {
                value = valuelize(value, valueType);
            }
            valueMap.put(entry.getKey(), value);
        }
        return valueMap;
    }

    private Properties evaluateAsProperties(Map<String, Token[]> tokensMap, ItemValueType valueType)
            throws Exception {
        if (tokensMap == null || tokensMap.isEmpty()) {
            return null;
        }
        Properties props = new Properties();
        for (Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
            Object value = tokenEvaluator.evaluate(entry.getValue());
            if (value != null && valueType != null) {
                value = valuelize(value, valueType);
            }
            if (value != null) {
                props.put(entry.getKey(), value);
            }
        }
        return props;
    }

    private Object valuelize(Object value, ItemValueType valueType) throws Exception {
        if (valueType == ItemValueType.STRING) {
            return value.toString();
        } else if (valueType == ItemValueType.INT) {
            return (value instanceof Integer ? value : Integer.valueOf(value.toString()));
        } else if (valueType == ItemValueType.LONG) {
            return (value instanceof Long ? value : Long.valueOf(value.toString()));
        } else if (valueType == ItemValueType.FLOAT) {
            return (value instanceof Float ? value : Float.valueOf(value.toString()));
        } else if (valueType == ItemValueType.DOUBLE) {
            return (value instanceof Double ? value : Double.valueOf(value.toString()));
        } else if (valueType == ItemValueType.BOOLEAN) {
            return (value instanceof Boolean ? value : Boolean.valueOf(value.toString()));
        } else if (valueType == ItemValueType.PARAMETERS) {
            return new VariableParameters(value.toString());
        } else if (valueType == ItemValueType.FILE) {
            return (value instanceof File ? value : new File(value.toString()));
        } else if (valueType == ItemValueType.MULTIPART_FILE) {
            return (value instanceof FileParameter ? value : new FileParameter(new File(value.toString())));
        } else {
            return value;
        }
    }

    private Object evaluateBean(BeanRule beanRule) {
        return tokenEvaluator.getActivity().getPrototypeScopeBean(beanRule);
    }

    @Nullable
    private Object[] evaluateBeanAsArray(List<BeanRule> beanRuleList) {
        List<Object> valueList = evaluateBeanAsList(beanRuleList);
        if (valueList == null) {
            return null;
        }
        Class<?> componentType = null;
        for (Object value : valueList) {
            if (value != null) {
                if (componentType == null) {
                    componentType = value.getClass();
                } else if (componentType != value.getClass()) {
                    componentType = null;
                    break;
                }
            }
        }
        if (componentType != null) {
            Object values = Array.newInstance(componentType, valueList.size());
            for (int i = 0; i < valueList.size(); i++) {
                Array.set(values, i, valueList.get(i));
            }
            return (Object[])values;
        } else {
            return valueList.toArray(new Object[0]);
        }
    }

    private List<Object> evaluateBeanAsList(List<BeanRule> beanRuleList) {
        if (beanRuleList == null || beanRuleList.isEmpty()) {
            return null;
        }
        List<Object> valueList = new ArrayList<>(beanRuleList.size());
        for (BeanRule beanRule : beanRuleList) {
            valueList.add(evaluateBean(beanRule));
        }
        return valueList;
    }

    private Set<Object> evaluateBeanAsSet(List<BeanRule> beanRuleList) {
        if (beanRuleList == null || beanRuleList.isEmpty()) {
            return null;
        }
        Set<Object> valueSet = new LinkedHashSet<>();
        for (BeanRule beanRule : beanRuleList) {
            Object value = evaluateBean(beanRule);
            valueSet.add(value);
        }
        return valueSet;
    }

    private Map<String, Object> evaluateBeanAsMap(Map<String, BeanRule> beanRuleMap) {
        if (beanRuleMap == null || beanRuleMap.isEmpty()) {
            return null;
        }
        Map<String, Object> valueMap = new LinkedHashMap<>();
        for (Map.Entry<String, BeanRule> entry : beanRuleMap.entrySet()) {
            Object value = evaluateBean(entry.getValue());
            valueMap.put(entry.getKey(), value);
        }
        return valueMap;
    }

    private Properties evaluateBeanAsProperties(Map<String, BeanRule> beanRuleMap) {
        if (beanRuleMap == null || beanRuleMap.isEmpty()) {
            return null;
        }
        Properties props = new Properties();
        for (Map.Entry<String, BeanRule> entry : beanRuleMap.entrySet()) {
            Object value = evaluateBean(entry.getValue());
            if (value != null) {
                props.put(entry.getKey(), value);
            }
        }
        return props;
    }

}
