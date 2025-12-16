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
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * An action that invokes a specified method on a managed bean.
 *
 * <p>This is a core action used to execute business logic by calling methods on
 * beans defined in the application context. It operates in two modes:
 * <ol>
 *   <li><b>Pre-resolved Method:</b> If the {@link Method} object is provided in the
 *   {@link InvokeActionRule}, it is invoked directly. This is the most efficient mode.</li>
 *   <li><b>Dynamic Method Resolution:</b> If only a method name is provided, the action
 *   dynamically finds a matching method at runtime based on the method name and
 *   arguments. It intelligently determines whether to pass the current {@link Translet}
 *   as the first argument and caches this decision for subsequent invocations to
 *   optimize performance.</li>
 * </ol>
 * It also supports setting properties on the bean before the method invocation.
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
     * @throws ActionExecutionException if the bean cannot be found
     */
    protected Object resolveBean(@NonNull Activity activity) throws ActionExecutionException {
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
     * Prepares the bean by setting properties and then executes the method invocation.
     * @param activity the current activity
     * @param bean the target bean instance
     * @return the result of the method execution
     * @throws Exception if an error occurs during method invocation
     */
    private Object execute(Activity activity, Object bean) throws Exception {
        try {
            ItemRuleMap propertyItemRuleMap = invokeActionRule.getPropertyItemRuleMap();
            if (propertyItemRuleMap != null && !propertyItemRuleMap.isEmpty()) {
                Map<String, Object> valueMap = activity.getItemEvaluator().evaluate(propertyItemRuleMap);
                for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                    BeanUtils.setProperty(bean, entry.getKey(), entry.getValue());
                }
            }

            Method method = invokeActionRule.getMethod();
            if (method != null) {
                // Pre-resolved method for efficiency
                ItemRuleMap argumentItemRuleMap = invokeActionRule.getArgumentItemRuleMap();
                if (argumentItemRuleMap != null && !argumentItemRuleMap.isEmpty()) {
                    Object[] args = createArguments(activity, argumentItemRuleMap, invokeActionRule.isRequiresTranslet());
                    return invokeMethod(bean, method, args);
                } else {
                    return invokeMethod(activity, bean, method, invokeActionRule.isRequiresTranslet());
                }
            } else {
                // Dynamically resolve method by name
                return invokeMethodByName(activity, bean);
            }
        } catch (ActionExecutionException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw new ActionExecutionException(this, ExceptionUtils.getCause(e));
        } catch (Exception e) {
            throw new ActionExecutionException(this, e);
        }
    }

    /**
     * Dynamically resolves and invokes a method by its name.
     * This method handles the logic of determining whether to include the Translet
     * as an argument and caches the result for future calls.
     * @param activity the current activity
     * @param bean the target bean
     * @return the result of the method invocation
     * @throws Exception if invocation fails
     */
    private Object invokeMethodByName(@NonNull Activity activity, Object bean) throws Exception {
        String methodName = invokeActionRule.getMethodName();
        ItemRuleMap argumentItemRuleMap = invokeActionRule.getArgumentItemRuleMap();

        // First-time invocation: determine if the translet is a required argument.
        if (activity.hasTranslet() && this.requiresTranslet == null) {
            try {
                Object result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, true);
                this.requiresTranslet = Boolean.TRUE;
                return result;
            } catch (NoSuchMethodException e) {
                if (logger.isTraceEnabled()) {
                    logger.trace("No such accessible method to invoke action " + invokeActionRule +
                            " with Translet. Trying without it.");
                }
                this.requiresTranslet = Boolean.FALSE;
                // Fall through to invoke without the translet
            }
        }

        // Subsequent invocations use the cached 'requiresTranslet' value.
        boolean transletRequired = (activity.hasTranslet() && this.requiresTranslet == Boolean.TRUE);
        return invokeMethod(activity, bean, methodName, argumentItemRuleMap, transletRequired);
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
     * Invokes a pre-resolved method, determining whether to pass the translet as an argument.
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
     * Prepares arguments and invokes a method dynamically by its name.
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
        MethodArguments methodArgs = resolveMethodArguments(activity, argumentItemRuleMap, requiresTranslet);
        return invokeMethod(bean, methodName, methodArgs.args, methodArgs.paramTypes);
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
        Object result = MethodUtils.invokeMethod(object, method, args, paramTypes);
        if (method.getReturnType() == Void.TYPE) {
            return Void.TYPE;
        } else {
            return result;
        }
    }

    /**
     * Creates an array of arguments for a pre-resolved method invocation.
     * @param activity the current activity
     * @param argumentItemRuleMap the rules for the arguments
     * @param requiresTranslet whether to include the translet as the first argument
     * @return an array of resolved argument objects
     */
    @NonNull
    private static Object[] createArguments(
            @NonNull Activity activity, @NonNull ItemRuleMap argumentItemRuleMap, boolean requiresTranslet) {
        Map<String, Object> valueMap = activity.getItemEvaluator().evaluate(argumentItemRuleMap);
        int size = argumentItemRuleMap.size();
        Object[] args;
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
            args[index++] = valueMap.get(name);
        }
        return args;
    }

    /**
     * Resolves method arguments and their types based on the provided rules.
     * @param activity the current activity
     * @param argumentItemRuleMap the map of item rules for arguments
     * @param requiresTranslet whether to prepend the translet to the arguments
     * @return a {@link MethodArguments} object containing the resolved arguments and their types
     */
    @NonNull
    private static MethodArguments resolveMethodArguments(@NonNull Activity activity,
            ItemRuleMap argumentItemRuleMap, boolean requiresTranslet) {
        if (argumentItemRuleMap != null && !argumentItemRuleMap.isEmpty()) {
            Map<String, Object> valueMap = activity.getItemEvaluator().evaluate(argumentItemRuleMap);
            int argSize = argumentItemRuleMap.size();
            int offset = (requiresTranslet ? 1 : 0);
            Class<?>[] paramTypes = new Class<?>[argSize + offset];
            Object[] args = new Object[argSize + offset];

            if (requiresTranslet) {
                paramTypes[0] = Translet.class;
                args[0] = activity.getTranslet();
            }

            int i = offset;
            for (ItemRule ir : argumentItemRuleMap.values()) {
                Object val = valueMap.get(ir.getName());
                paramTypes[i] = ItemRuleUtils.getPrototypeClass(ir, val);
                args[i] = val;
                i++;
            }
            return new MethodArguments(paramTypes, args);
        } else if (requiresTranslet) {
            Class<?>[] paramTypes = new Class<?>[]{Translet.class};
            Object[] args = new Object[]{activity.getTranslet()};
            return new MethodArguments(paramTypes, args);
        } else {
            return MethodArguments.EMPTY;
        }
    }

    /**
     * A simple holder for method arguments and their corresponding parameter types.
     */
    private static class MethodArguments {

        static final MethodArguments EMPTY = new MethodArguments(null, null);

        final Class<?>[] paramTypes;

        final Object[] args;

        MethodArguments(Class<?>[] paramTypes, Object[] args) {
            this.paramTypes = paramTypes;
            this.args = args;
        }

    }

}
