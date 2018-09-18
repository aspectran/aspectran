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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.BeanActionRule;
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
 * The BeanAction that invoking method in the bean instance.
 * 
 * <p>Created: 2008. 03. 22 PM 5:50:35</p>
 */
public class BeanAction extends AbstractAction {

    private final Log log = LogFactory.getLog(BeanAction.class);

    private final BeanActionRule beanActionRule;

    private AspectAdviceRule aspectAdviceRule;

    private Boolean requiresTranslet;

    /**
     * Instantiates a new BeanAction.
     *
     * @param beanActionRule the bean action rule
     * @param parent the parent
     */
    public BeanAction(BeanActionRule beanActionRule, ActionList parent) {
        super(parent);

        this.beanActionRule = beanActionRule;
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
                    throw new ActionExecutionException("No such bean; Invalid BeanActionRule" + aspectAdviceRule);
                }
            } else {
                if (beanActionRule.getBeanClass() != null) {
                    bean = activity.getBean(beanActionRule.getBeanClass());
                } else if (beanActionRule.getBeanId() != null) {
                    bean = activity.getBean(beanActionRule.getBeanId());
                }
                if (bean == null) {
                    throw new ActionExecutionException("No such bean; Invalid BeanActionRule " + beanActionRule);
                }
            }

            ItemRuleMap argumentItemRuleMap = beanActionRule.getArgumentItemRuleMap();
            ItemRuleMap propertyItemRuleMap = beanActionRule.getPropertyItemRuleMap();
            ItemEvaluator evaluator = null;
            if (propertyItemRuleMap != null || argumentItemRuleMap != null) {
                evaluator = new ItemExpressionParser(activity);
            }
            if (propertyItemRuleMap != null) {
                Map<String, Object> valueMap = evaluator.evaluate(propertyItemRuleMap);
                for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                    BeanUtils.setProperty(bean, entry.getKey(), entry.getValue());
                }
            }

            Method method = beanActionRule.getMethod();
            if (method != null) {
                if (argumentItemRuleMap == null) {
                    return MethodAction.invokeMethod(activity, bean, method, beanActionRule.isRequiresTranslet());
                } else {
                    Object[] args = createArguments(activity, argumentItemRuleMap, evaluator, beanActionRule.isRequiresTranslet());
                    return method.invoke(bean, args);
                }
            } else {
                String methodName = beanActionRule.getMethodName();
                Object result;
                if (activity.getTranslet() != null) {
                    if (requiresTranslet == null) {
                        try {
                            result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, evaluator, true);
                            requiresTranslet = Boolean.TRUE;
                        } catch (NoSuchMethodException e) {
                            if (log.isDebugEnabled()) {
                                log.debug("No have a Translet argument; beanActionRule " + beanActionRule);
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
            log.error("Failed to execute an action that invokes the bean's method " + beanActionRule);
            throw e;
        }
    }

    private static Object[] createArguments(Activity activity, ItemRuleMap argumentItemRuleMap,
                                            ItemEvaluator evaluator, boolean requiresTranslet) {
        Object[] args = null;
        if (argumentItemRuleMap != null) {
            if (evaluator == null) {
                evaluator = new ItemExpressionParser(activity);
            }

            Map<String, Object> valueMap = evaluator.evaluate(argumentItemRuleMap);
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

            for (String name : argumentItemRuleMap.keySet()) {
                Object o = valueMap.get(name);
                args[index] = o;
                index++;
            }
        } else if (requiresTranslet) {
            args = new Object[] { activity.getTranslet() };
        }

        return args;
    }

    private static Object invokeMethod(Activity activity, Object bean, String methodName,
                                       ItemRuleMap argumentItemRuleMap, ItemEvaluator evaluator,
                                       boolean requiresTranslet)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?>[] argsTypes = null;
        Object[] argsObjects = null;

        if (argumentItemRuleMap != null) {
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
                argsTypes[0] = activity.getTranslet().getTransletInterfaceClass();
                argsObjects[0] = activity.getTranslet();
            } else {
                argIndex = 0;
                argsTypes = new Class<?>[argSize];
                argsObjects = new Object[argsTypes.length];
            }

            for (ItemRule ir : argumentItemRuleMap.values()) {
                Object o = valueMap.get(ir.getName());
                argsTypes[argIndex] = ItemRule.getClassOfValue(ir, o);
                argsObjects[argIndex] = o;
                argIndex++;
            }
        } else if (requiresTranslet) {
            argsTypes = new Class<?>[] { activity.getTranslet().getTransletInterfaceClass() };
            argsObjects = new Object[] { activity.getTranslet() };
        }

        return MethodUtils.invokeMethod(bean, methodName, argsObjects, argsTypes);
    }

    /**
     * Returns the bean action rule.
     *
     * @return the bean action rule
     */
    public BeanActionRule getBeanActionRule() {
        return beanActionRule;
    }

    @Override
    public ActionList getParent() {
        return parent;
    }

    @Override
    public String getActionId() {
        return beanActionRule.getActionId();
    }

    @Override
    public boolean isHidden() {
        return beanActionRule.isHidden();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.BEAN;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getActionRule() {
        return (T)beanActionRule;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("actionType", getActionType());
        tsb.append("beanActionRule", beanActionRule);
        if (aspectAdviceRule != null) {
            tsb.append("aspectAdviceRule", aspectAdviceRule.toString(true));
        }
        return tsb.toString();
    }

}
