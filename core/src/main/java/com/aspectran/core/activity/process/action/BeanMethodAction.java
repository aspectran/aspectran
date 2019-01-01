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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.BeanMethodActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * The BeanMethodAction that invoking method in the bean instance.
 * 
 * <p>Created: 2008. 03. 22 PM 5:50:35</p>
 */
public class BeanMethodAction extends AbstractAction {

    private static final Log log = LogFactory.getLog(BeanMethodAction.class);

    private final BeanMethodActionRule beanMethodActionRule;

    private AspectAdviceRule aspectAdviceRule;

    private Boolean requiresTranslet;

    /**
     * Instantiates a new BeanMethodAction.
     *
     * @param beanMethodActionRule the bean method action rule
     * @param parent the parent
     */
    public BeanMethodAction(BeanMethodActionRule beanMethodActionRule, ActionList parent) {
        super(parent);

        this.beanMethodActionRule = beanMethodActionRule;
    }

    /**
     * Gets the aspect advice rule.
     *
     * @return the aspect advice rule
     */
    public AspectAdviceRule getAspectAdviceRule() {
        return aspectAdviceRule;
    }

    /**
     * Sets the aspect advice rule.
     *
     * @param aspectAdviceRule the new aspect advice rule
     */
    public void setAspectAdviceRule(AspectAdviceRule aspectAdviceRule) {
        this.aspectAdviceRule = aspectAdviceRule;
    }

    @Override
    public Object execute(Activity activity) throws Exception {
        try {
            Object bean = null;
            if (aspectAdviceRule != null) {
                bean = activity.getAspectAdviceBean(aspectAdviceRule.getAspectId());
                if (bean == null) {
                    throw new ActionExecutionException("No such bean; Invalid AspectAdviceRule" + aspectAdviceRule);
                }
            } else {
                if (beanMethodActionRule.getBeanClass() != null) {
                    bean = activity.getBean(beanMethodActionRule.getBeanClass());
                } else if (beanMethodActionRule.getBeanId() != null) {
                    bean = activity.getBean(beanMethodActionRule.getBeanId());
                }
                if (bean == null) {
                    throw new ActionExecutionException("No such bean; Invalid BeanMethodActionRule " + beanMethodActionRule);
                }
            }

            ItemRuleMap propertyItemRuleMap = beanMethodActionRule.getPropertyItemRuleMap();
            ItemRuleMap argumentItemRuleMap = beanMethodActionRule.getArgumentItemRuleMap();
            ItemEvaluator evaluator = null;
            if ((propertyItemRuleMap != null && !propertyItemRuleMap.isEmpty()) ||
                    (argumentItemRuleMap != null && !argumentItemRuleMap.isEmpty())) {
                evaluator = new ItemExpressionParser(activity);
            }
            if (propertyItemRuleMap != null && !propertyItemRuleMap.isEmpty()) {
                Map<String, Object> valueMap = evaluator.evaluate(propertyItemRuleMap);
                for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                    BeanUtils.setProperty(bean, entry.getKey(), entry.getValue());
                }
            }

            Method method = beanMethodActionRule.getMethod();
            if (method != null) {
                if (argumentItemRuleMap != null && !argumentItemRuleMap.isEmpty()) {
                    Object[] args = createArguments(activity, argumentItemRuleMap, evaluator, beanMethodActionRule.isRequiresTranslet());
                    return method.invoke(bean, args);
                } else {
                    return ConfigBeanMethodAction.invokeMethod(activity, bean, method, beanMethodActionRule.isRequiresTranslet());
                }
            } else {
                String methodName = beanMethodActionRule.getMethodName();
                Object result;
                if (activity.getTranslet() != null) {
                    if (requiresTranslet == null) {
                        try {
                            result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, evaluator, true);
                            requiresTranslet = Boolean.TRUE;
                        } catch (NoSuchMethodException e) {
                            if (log.isDebugEnabled()) {
                                log.debug("No have a Translet argument on bean method " + beanMethodActionRule);
                            }
                            requiresTranslet = Boolean.FALSE;
                            result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, evaluator, false);
                        }
                    } else {
                        result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, evaluator, requiresTranslet);
                    }
                } else {
                    result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, evaluator, false);
                }
                return result;
            }
        } catch (Exception e) {
            if (aspectAdviceRule != null) {
                log.error("Failed to execute advice bean method action " + aspectAdviceRule);
            } else {
                log.error("Failed to execute bean method action " + beanMethodActionRule);
            }
            throw e;
        }
    }

    private static Object[] createArguments(Activity activity, ItemRuleMap argumentItemRuleMap,
                                            ItemEvaluator evaluator, boolean requiresTranslet) {
        if (evaluator == null) {
            evaluator = new ItemExpressionParser(activity);
        }

        Object[] args;
        int size = argumentItemRuleMap.size();
        int index;
        if (requiresTranslet) {
            index = 1;
            args = new Object[size + index];
            args[0] = activity.getTranslet();
        } else {
            index = 0;
            args = new Object[size];
        }

        Map<String, Object> valueMap = evaluator.evaluate(argumentItemRuleMap);
        for (String name : argumentItemRuleMap.keySet()) {
            Object o = valueMap.get(name);
            args[index] = o;
            index++;
        }

        return args;
    }

    private static Object invokeMethod(Activity activity, Object bean, String methodName,
                                       ItemRuleMap argumentItemRuleMap, ItemEvaluator evaluator,
                                       boolean requiresTranslet)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?>[] argsTypes = null;
        Object[] argsObjects = null;

        if (argumentItemRuleMap != null && !argumentItemRuleMap.isEmpty()) {
            if (evaluator == null) {
                evaluator = new ItemExpressionParser(activity);
            }

            Map<String, Object> valueMap = evaluator.evaluate(argumentItemRuleMap);
            int argSize = argumentItemRuleMap.size();
            int argIndex;

            if (requiresTranslet) {
                argIndex = 1;
                argsTypes = new Class<?>[argSize + argIndex];
                argsObjects = new Object[argsTypes.length];
                argsTypes[0] = Translet.class;
                argsObjects[0] = activity.getTranslet();
            } else {
                argIndex = 0;
                argsTypes = new Class<?>[argSize];
                argsObjects = new Object[argsTypes.length];
            }

            for (ItemRule ir : argumentItemRuleMap.values()) {
                Object o = valueMap.get(ir.getName());
                argsTypes[argIndex] = ItemRule.getPrototypeClass(ir, o);
                argsObjects[argIndex] = o;
                argIndex++;
            }
        } else if (requiresTranslet) {
            argsTypes = new Class<?>[] { Translet.class };
            argsObjects = new Object[] { activity.getTranslet() };
        }

        return MethodUtils.invokeMethod(bean, methodName, argsObjects, argsTypes);
    }

    /**
     * Returns the bean method action rule.
     *
     * @return the bean method action rule
     */
    public BeanMethodActionRule getBeanMethodActionRule() {
        return beanMethodActionRule;
    }

    @Override
    public ActionList getParent() {
        return parent;
    }

    @Override
    public String getActionId() {
        return beanMethodActionRule.getActionId();
    }

    @Override
    public boolean isHidden() {
        return beanMethodActionRule.isHidden();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.BEAN_METHOD;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getActionRule() {
        return (T)beanMethodActionRule;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("actionType", getActionType());
        tsb.append("beanMethodActionRule", beanMethodActionRule);
        if (aspectAdviceRule != null) {
            tsb.append("aspectAdviceRule", aspectAdviceRule.toString(true));
        }
        return tsb.toString();
    }

}
