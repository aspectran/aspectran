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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ItemRuleUtils;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.BeanUtils;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.MethodUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * An action that invokes a specified method on a managed bean.
 *
 * <p>This is a core action used to execute business logic by calling methods on
 * beans defined in the application context. It supports setting properties on the
 * bean before invocation and dynamically resolving method arguments from the current
 * {@link com.aspectran.core.activity.Translet}.
 */
public class InvokeAction implements Executable {

    private static final Logger logger = LoggerFactory.getLogger(InvokeAction.class);

    private final InvokeActionRule invokeActionRule;

    private volatile Boolean requiresTranslet;

    /**
     * Instantiates a new InvokeAction.
     * @param invokeActionRule the rule that defines the bean and method to invoke
     */
    public InvokeAction(InvokeActionRule invokeActionRule) {
        this.invokeActionRule = invokeActionRule;
    }

    /**
     * Executes the invoke action. This method resolves the target bean and then
     * proceeds with the method invocation.
     * @param activity the current activity
     * @return the result of the method invocation
     * @throws Exception if an error occurs during action execution
     */
    @Override
    public Object execute(@NonNull Activity activity) throws Exception {
        Object bean = resolveBean(activity);
        return execute(activity, bean);
    }

    /**
     * Resolves the target bean instance from the activity context using the bean ID
     * or class specified in the rule.
     * @param activity the current activity
     * @return the resolved bean instance
     * @throws Exception if the bean cannot be found
     */
    protected Object resolveBean(@NonNull Activity activity) throws Exception {
        Object bean = null;
        if (invokeActionRule.getBeanClass() != null) {
            bean = activity.getBean(invokeActionRule.getBeanClass());
        } else if (invokeActionRule.getBeanId() != null) {
            bean = activity.getBean(invokeActionRule.getBeanId());
        }
        if (bean == null) {
            throw new ActionExecutionException("No bean found for " + invokeActionRule);
        }
        return bean;
    }

    /**
     * Executes the method invocation on the specified bean instance.
     * @param activity the current activity
     * @param bean the target bean instance
     * @return the result of the method execution
     * @throws Exception if an error occurs during method invocation
     */
    private Object execute(Activity activity, Object bean) throws Exception {
        try {
            ItemRuleMap propertyItemRuleMap = invokeActionRule.getPropertyItemRuleMap();
            ItemRuleMap argumentItemRuleMap = invokeActionRule.getArgumentItemRuleMap();
            if (propertyItemRuleMap != null && !propertyItemRuleMap.isEmpty()) {
                Map<String, Object> valueMap = activity.getItemEvaluator().evaluate(propertyItemRuleMap);
                for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                    BeanUtils.setProperty(bean, entry.getKey(), entry.getValue());
                }
            }

            Method method = invokeActionRule.getMethod();
            if (method != null) {
                if (argumentItemRuleMap != null && !argumentItemRuleMap.isEmpty()) {
                    Object[] args = createArguments(activity, argumentItemRuleMap, invokeActionRule.isRequiresTranslet());
                    return invokeMethod(bean, method, args);
                } else {
                    return invokeMethod(activity, bean, method, invokeActionRule.isRequiresTranslet());
                }
            } else {
                String methodName = invokeActionRule.getMethodName();
                Object result;
                if (activity.hasTranslet()) {
                    if (requiresTranslet == null) {
                        try {
                            result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, true);
                            requiresTranslet = Boolean.TRUE;
                        } catch (NoSuchMethodException e) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("No such accessible method to invoke action {}", invokeActionRule);
                            }
                            requiresTranslet = Boolean.FALSE;
                            result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, false);
                        }
                    } else {
                        result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, requiresTranslet);
                    }
                } else {
                    result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, false);
                }
                return result;
            }
        } catch (ActionExecutionException e) {
            throw e;
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                throw new ActionExecutionException(this, ExceptionUtils.getCause(e));
            } else {
                throw new ActionExecutionException(this, e);
            }
        } catch (Exception e) {
            throw new ActionExecutionException(this, e);
        }
    }

    /**
     * Returns the rule that defines this invoke action.
     * @return the invoke action rule
     */
    public InvokeActionRule getInvokeActionRule() {
        return invokeActionRule;
    }

    @Override
    public String getActionId() {
        return invokeActionRule.getActionId();
    }

    @Override
    public boolean isHidden() {
        return invokeActionRule.isHidden();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.INVOKE;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append(getActionType().toString(), invokeActionRule);
        return tsb.toString();
    }

    /**
     * Invokes a method with or without the current translet as an argument.
     * @param activity the current activity
     * @param bean the target bean
     * @param method the method to invoke
     * @param requiresTranslet whether the translet should be passed as an argument
     * @return the result of the invocation
     * @throws Exception if the invocation fails
     */
    private static Object invokeMethod(Activity activity, Object bean, Method method, boolean requiresTranslet)
            throws Exception {
        Object[] args;
        if (requiresTranslet) {
            args = new Object[] { activity.getTranslet() };
        } else {
            args = MethodUtils.EMPTY_OBJECT_ARRAY;
        }
        return invokeMethod(bean, method, args);
    }

    /**
     * Invokes the specified method on the target bean with the given arguments.
     * @param bean the target bean
     * @param method the method to invoke
     * @param args the arguments to pass to the method
     * @return the result of the invocation, or {@link Void#TYPE} if the method returns void
     * @throws Exception if the invocation fails
     */
    private static Object invokeMethod(Object bean, @NonNull Method method, Object[] args) throws Exception {
        if (method.getReturnType() == Void.TYPE) {
            method.invoke(bean, args);
            return Void.TYPE;
        } else {
            return method.invoke(bean, args);
        }
    }

    /**
     * Dynamically resolves and invokes a method based on its name and argument rules.
     * @param activity the current activity
     * @param bean the target bean
     * @param methodName the name of the method to invoke
     * @param argumentItemRuleMap the rules for resolving method arguments
     * @param requiresTranslet whether the translet should be passed as the first argument
     * @return the result of the invocation
     * @throws Exception if the method cannot be found or the invocation fails
     */
    private static Object invokeMethod(
            Activity activity, Object bean, String methodName,
            ItemRuleMap argumentItemRuleMap, boolean requiresTranslet) throws Exception {
        Class<?>[] argsTypes = null;
        Object[] argsObjects = null;

        if (argumentItemRuleMap != null && !argumentItemRuleMap.isEmpty()) {
            Map<String, Object> valueMap = activity.getItemEvaluator().evaluate(argumentItemRuleMap);
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
                argsTypes[argIndex] = ItemRuleUtils.getPrototypeClass(ir, o);
                argsObjects[argIndex] = o;
                argIndex++;
            }
        } else if (requiresTranslet) {
            argsTypes = new Class<?>[] { Translet.class };
            argsObjects = new Object[] { activity.getTranslet() };
        }

        return invokeMethod(bean, methodName, argsObjects, argsTypes);
    }

    /**
     * Invokes a method on an object with the given arguments and parameter types.
     * @param object the object to invoke the method on
     * @param methodName the name of the method
     * @param args the arguments to pass
     * @param paramTypes the parameter types of the method
     * @return the result of the invocation, or {@link Void#TYPE} if the method returns void
     * @throws NoSuchMethodException if a matching method is not found
     * @throws IllegalAccessException if the method is not accessible
     * @throws InvocationTargetException if the invoked method throws an exception
     */
    private static Object invokeMethod(
            @NonNull Object object, String methodName, Object[] args, Class<?>[] paramTypes)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = MethodUtils.getMatchingAccessibleMethod(object.getClass(), methodName, args, paramTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: " + methodName + "() on object: " +
                    object.getClass().getName());
        }
        if (method.getReturnType() == Void.TYPE) {
            MethodUtils.invokeMethod(object, method, args, paramTypes);
            return Void.TYPE;
        } else {
            return MethodUtils.invokeMethod(object, method, args, paramTypes);
        }
    }

    /**
     * Creates an array of arguments for a method invocation.
     * @param activity the current activity
     * @param argumentItemRuleMap the rules for the arguments
     * @param requiresTranslet whether to include the translet as the first argument
     * @return an array of resolved argument objects
     */
    @NonNull
    private static Object[] createArguments(
            @NonNull Activity activity, @NonNull ItemRuleMap argumentItemRuleMap, boolean requiresTranslet) {
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
        Map<String, Object> valueMap = activity.getItemEvaluator().evaluate(argumentItemRuleMap);
        for (String name : argumentItemRuleMap.keySet()) {
            Object o = valueMap.get(name);
            args[index] = o;
            index++;
        }
        return args;
    }

}
