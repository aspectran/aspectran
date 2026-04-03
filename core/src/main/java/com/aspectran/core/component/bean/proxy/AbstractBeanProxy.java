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
package com.aspectran.core.component.bean.proxy;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityPerformException;
import com.aspectran.core.activity.HintParameters;
import com.aspectran.core.activity.ProxyActivity;
import com.aspectran.core.activity.aspect.AdviceConstraintViolationException;
import com.aspectran.core.activity.aspect.AdviceException;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.component.aspect.AdviceRuleRegistry;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.aspect.RelevantAspectRuleHolder;
import com.aspectran.core.component.aspect.pointcut.PointcutPattern;
import com.aspectran.core.component.bean.annotation.Advisable;
import com.aspectran.core.component.bean.annotation.Async;
import com.aspectran.core.component.bean.annotation.Hint;
import com.aspectran.core.component.bean.annotation.Hints;
import com.aspectran.core.component.bean.async.AsyncExecutionException;
import com.aspectran.core.component.bean.async.AsyncTaskExecutor;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.NoActivityStateException;
import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.thread.ThreadContextHelper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;

/**
 * Base support class for bean proxies that apply Aspectran AOP advice.
 * This class handles the core logic of intercepting method invocations,
 * applying relevant advice (before, after, finally), and managing
 * synchronous and asynchronous executions.
 *
 * <p>Created: 2017. 1. 27.</p>
 */
public abstract class AbstractBeanProxy {

    private final ActivityContext context;

    private final AspectRuleRegistry aspectRuleRegistry;

    private Map<Method, List<HintParameters>> methodHints;

    /**
     * Creates a new AbstractBeanProxy.
     * @param context the activity context
     */
    public AbstractBeanProxy(@NonNull ActivityContext context) {
        this.context = context;
        this.aspectRuleRegistry = context.getAspectRuleRegistry();
    }

    /**
     * Returns the {@code BeanRule} for the bean being proxied.
     * @return the bean rule
     */
    protected abstract BeanRule getBeanRule();

    /**
     * Intercepts a method invocation. If the method is advisable (annotated with
     * {@link Advisable} or {@link Async}), it applies AOP advice. Otherwise,
     * it invokes the original method directly.
     * @param method the method being invoked
     * @param args the arguments to the method
     * @param superInvoker a callback to invoke the original (super) method
     * @return the result of the method invocation
     * @throws Exception if the invocation fails
     */
    protected Object invoke(Method method, Object[] args, SuperInvoker superInvoker)
            throws Exception {
        if (isAdvisableMethod(method)) {
            Activity activity = (context.hasCurrentActivity() ? context.getCurrentActivity() : null);
            if (activity == null && !method.isAnnotationPresent(Async.class)) {
                String beanId = getBeanRule().getId();
                String className = getBeanRule().getClassName();
                String methodName = method.getName();
                AdviceRuleRegistry adviceRuleRegistry = retrieveAdviceRuleRegistry(null, beanId, className, methodName);
                if (adviceRuleRegistry != null) {
                    throw new NoActivityStateException();
                }
            }
            return invoke(method, args, superInvoker, activity);
        } else if (isHintableMethod(method)) {
            Activity activity = (context.hasCurrentActivity() ? context.getCurrentActivity() : null);
            if (activity != null) {
                List<HintParameters> hints = methodHints.get(method);
                if (hints != null) {
                    int pushedCount = activity.pushHint(hints);
                    if (pushedCount > 0) {
                        try {
                            return superInvoker.invoke();
                        } finally {
                            activity.popHint(pushedCount);
                        }
                    }
                }
            }
        }
        return superInvoker.invoke();
    }

    /**
     * Determines whether to execute the method synchronously or asynchronously
     * based on the presence of the {@link Async} annotation.
     * @param method the method being invoked
     * @param args the arguments to the method
     * @param superInvoker a callback to invoke the original method
     * @param activity the current activity, or null if none exists
     * @return the result of the method invocation, which may be a {@link Future} for async calls
     * @throws Exception if the invocation fails
     */
    @Nullable
    private Object invoke(
            @NonNull Method method, Object[] args, SuperInvoker superInvoker,
            @Nullable Activity activity) throws Exception {
        Async async = method.getAnnotation(Async.class);
        if (async != null) {
            return invokeAsync(method, args, superInvoker, async, activity);
        } else {
            return invokeSync(method, superInvoker, activity, null);
        }
    }

    /**
     * Handles the asynchronous invocation of a method.
     * It submits the task to an {@link AsyncTaskExecutor} and returns a {@link Future}
     * if the method has a return value, otherwise returns null.
     * @param method the method to invoke asynchronously
     * @param args the method arguments
     * @param superInvoker a callback to invoke the original method
     * @param async the {@link Async} annotation instance
     * @param activity the current activity
     * @return a {@link Future} representing the result of the async computation, or null for void methods
     * @throws Exception if submitting the task fails
     */
    @Nullable
    private Object invokeAsync(
            @NonNull Method method, Object[] args, SuperInvoker superInvoker,
            Async async, @Nullable Activity activity) throws Exception {
        AsyncTaskExecutor executor = findAsyncExecutor(async);
        if (method.getReturnType() != Void.TYPE && !Future.class.isAssignableFrom(method.getReturnType())) {
            throw new AsyncExecutionException("Cannot use @Async on a method that does not return void or Future: " + method);
        }
        if (method.getReturnType() == Void.TYPE) {
            executor.execute(() -> {
                try {
                    invokeAsync(method, args, superInvoker, activity, executor);
                } catch (Exception e) {
                    // The exception is handled by the uncaught exception handler, so no need to rethrow.
                }
            });
            return null;
        } else {
            return executor.submit(() -> {
                try {
                    return invokeAsync(method, args, superInvoker, activity, executor);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            });
        }
    }

    /**
     * The core logic for an asynchronous method invocation, executed within a worker thread.
     * It sets up a {@link ProxyActivity} to ensure the activity context is available
     * and then proceeds with a synchronous-style advice execution.
     * @param method the method to invoke
     * @param args the method arguments
     * @param superInvoker a callback to invoke the original method
     * @param activity the original activity from the calling thread
     * @param executor the async task executor
     * @return the result of the method invocation
     * @throws Exception if the invocation fails
     */
    private Object invokeAsync(
            @NonNull Method method, Object[] args, SuperInvoker superInvoker,
            @Nullable Activity activity, AsyncTaskExecutor executor) throws Exception {
        return ThreadContextHelper.call(context.getClassLoader(), () -> {
            try {
                final Activity proxyActivity;
                Object result;
                if (activity == null) {
                    // When there is no parent activity, the proxy activity itself becomes the execution subject.
                    proxyActivity = new ProxyActivity(context);
                    result = proxyActivity.perform(() -> invokeSync(method, superInvoker, proxyActivity, null));
                } else {
                    // The proxy activity serves only as a wrapper, and the actual execution subject is the parent activity.
                    proxyActivity = new ProxyActivity(activity);
                    result = proxyActivity.perform(() -> invokeSync(method, superInvoker, activity, proxyActivity));
                }
                if (result instanceof Future<?> future) {
                    return future.get();
                } else {
                    return result;
                }
            } catch (Exception e) {
                Exception cause = e;
                if (e instanceof ActivityPerformException && e.getCause() instanceof Exception ex) {
                    cause = ex;
                }
                if (executor.getAsyncUncaughtExceptionHandler() != null) {
                    executor.getAsyncUncaughtExceptionHandler().handleUncaughtException(cause, method, args);
                }
                throw e;
            }
        });
    }

    /**
     * Handles the synchronous invocation of a method, applying all relevant AOP advice.
     * This is the core of the AOP proxy, executing before, after, and finally advice,
     * as well as exception handling advice.
     * @param method the method to invoke
     * @param superInvoker a callback to invoke the original method
     * @param activity the current activity
     * @return the result of the method invocation
     * @throws Exception if the invocation or advice execution fails
     */
    @Nullable
    private Object invokeSync(
            @NonNull Method method, SuperInvoker superInvoker,
            @Nullable Activity activity, @Nullable Activity proxyActivity) throws Exception {
        if (activity == null) {
            return superInvoker.invoke();
        }

        List<HintParameters> hints = (methodHints != null ? methodHints.get(method) : null);
        int pushedCount = 0;
        if (hints != null) {
            Activity activityToUse = (proxyActivity != null ? proxyActivity : activity);
            pushedCount = activityToUse.pushHint(hints);
        }

        try {
            String beanId = getBeanRule().getId();
            String className = getBeanRule().getClassName();
            String methodName = method.getName();

            AdviceRuleRegistry adviceRuleRegistry = retrieveAdviceRuleRegistry(activity, beanId, className, methodName);
            if (adviceRuleRegistry == null) {
                return superInvoker.invoke();
            }

            try {
                try {
                    executeAdvice(adviceRuleRegistry.getBeforeAdviceRuleList(), activity);
                    Object result = superInvoker.invoke();
                    executeAdvice(adviceRuleRegistry.getAfterAdviceRuleList(), activity);
                    return result;
                } catch (Exception e) {
                    activity.setRaisedException(e);
                    throw e;
                } finally {
                    executeAdvice(adviceRuleRegistry.getFinallyAdviceRuleList(), activity);
                }
            } catch (Exception e) {
                activity.setRaisedException(e);
                if (handleException(adviceRuleRegistry.getExceptionRuleList(), activity)) {
                    return null;
                }
                throw e;
            }
        } finally {
            if (pushedCount > 0) {
                Activity activityToUse = (proxyActivity != null ? proxyActivity : activity);
                activityToUse.popHint(pushedCount);
            }
        }
    }

    /**
     * Retrieves the {@link AdviceRuleRegistry} for the current method invocation,
     * considering the current request, bean, and method details.
     * @param activity the current activity
     * @param beanId the ID of the bean
     * @param className the class name of the bean
     * @param methodName the name of the method being invoked
     * @return the relevant advice rule registry
     * @throws AdviceConstraintViolationException if advice constraints are violated
     * @throws AdviceException if there is an error in advice processing
     */
    private AdviceRuleRegistry retrieveAdviceRuleRegistry(
            @Nullable Activity activity, String beanId, String className, String methodName)
            throws AdviceConstraintViolationException, AdviceException {
        String requestName;
        boolean literalPattern;
        if (activity != null && activity.hasTranslet()) {
            requestName = activity.getTranslet().getRequestName();
            literalPattern = !activity.getTranslet().hasPathVariables();
        } else {
            requestName = null;
            literalPattern = true;
        }

        PointcutPattern pointcutPattern = new PointcutPattern(requestName, beanId, className, methodName);
        RelevantAspectRuleHolder holder;
        if (literalPattern) {
            holder = aspectRuleRegistry.retrieveFromSoftCache(pointcutPattern);
        } else {
            holder = aspectRuleRegistry.retrieveFromWeakCache(pointcutPattern);
        }

        AdviceRuleRegistry adviceRuleRegistry = holder.getAdviceRuleRegistry();
        if (activity != null) {
            if (adviceRuleRegistry != null && adviceRuleRegistry.getSettingsAdviceRuleList() != null) {
                for (SettingsAdviceRule sar : adviceRuleRegistry.getSettingsAdviceRuleList()) {
                    activity.registerSettingsAdviceRule(sar);
                }
            }
            if (holder.getDynamicAspectRuleList() != null) {
                for (AspectRule aspectRule : holder.getDynamicAspectRuleList()) {
                    // register dynamically
                    activity.registerAdviceRule(aspectRule);
                }
            }
        }
        return adviceRuleRegistry;
    }

    /**
     * Executes a list of advice rules.
     * @param adviceRuleList the list of advice rules to execute
     * @param activity the current activity
     * @throws AdviceException if an advice action fails
     */
    private void executeAdvice(List<AdviceRule> adviceRuleList, Activity activity)
            throws AdviceException {
        if (adviceRuleList != null) {
            for (AdviceRule adviceRule : adviceRuleList) {
                if (!isSameBean(getBeanRule(), adviceRule)) {
                    activity.executeAdvice(adviceRule);
                }
            }
        }
    }

    /**
     * Handles exceptions according to the defined exception handling rules.
     * @param exceptionRuleList the list of applicable exception rules
     * @param activity the current activity, which holds the raised exception
     * @return true if the exception was handled and a response is reserved, false otherwise
     * @throws ActionExecutionException if the exception handling action fails
     */
    private boolean handleException(List<ExceptionRule> exceptionRuleList, @NonNull Activity activity)
            throws ActionExecutionException {
        if (exceptionRuleList != null) {
            activity.handleException(exceptionRuleList);
            return activity.isResponseReserved();
        }
        return false;
    }

    /**
     * Checks if a method is marked as advisable (i.e., eligible for AOP interception).
     /**
      * A method is advisable if it is annotated with {@link Advisable} or {@link Async}.
      * @param method the method to check
      * @return true if the method is advisable, false otherwise
      */
     private boolean isAdvisableMethod(@NonNull Method method) {
         return (method.isAnnotationPresent(Advisable.class) || method.isAnnotationPresent(Async.class));
     }

     /**
      * A method is hintable if it has hints configured in the proxy.
      * @param method the method to check
      * @return true if the method is hintable, false otherwise
      */
     private boolean isHintableMethod(@NonNull Method method) {
         return (methodHints != null && methodHints.containsKey(method));
     }

    /**
     * Checks if the given bean rule is the same as the bean associated with the advice rule.
     * This is used to prevent an advice from advising itself if it's defined as a bean.
     * @param beanRule the bean rule of the advised bean
     * @param adviceRule the advice rule being considered
     * @return true if they refer to the same bean, false otherwise
     */
    private boolean isSameBean(@NonNull BeanRule beanRule, AdviceRule adviceRule) {
        if (beanRule.getId() != null && beanRule.getId().equals(adviceRule.getAdviceBeanId())) {
            return true;
        }
        if (beanRule.getBeanClass() != null && adviceRule.getAdviceBeanClass() != null) {
            return (beanRule.getBeanClass() == adviceRule.getAdviceBeanClass());
        }
        return false;
    }

    /**
     * Finds the appropriate {@link AsyncTaskExecutor} for an async method.
     * If a specific executor is specified in the {@link Async} annotation (by bean ID or class),
     * it will be used. Otherwise, the default async task executor from the context is returned.
     * @param async the {@link Async} annotation from the method
     * @return the resolved {@link AsyncTaskExecutor}
     * @throws AsyncExecutionException if the specified executor cannot be found
     */
    private AsyncTaskExecutor findAsyncExecutor(@NonNull Async async) throws AsyncExecutionException {
        String beanId = StringUtils.emptyToNull(async.value());
        Class<? extends AsyncTaskExecutor> beanClass = async.executor();
        if (beanId == null && (beanClass == null || beanClass == AsyncTaskExecutor.class)) {
            return context.getAsyncTaskExecutor();
        }
        AsyncTaskExecutor executor;
        try {
            executor = context.getBeanRegistry().getBean(beanClass, beanId);
        } catch (Exception e) {
            if (beanId != null) {
                throw new AsyncExecutionException("No AsyncTaskExecutor found for beanId: " + beanId, e);
            } else {
                throw new AsyncExecutionException("No AsyncTaskExecutor found for beanClass: " + beanClass, e);
            }
        }
        return executor;
    }

    /**
     * Scans the given bean class and its interfaces for {@link Hint} annotations
     * and caches them for later use during method invocation.
     * @param beanClass the bean class to scan
     */
    protected void scanHints(Class<?> beanClass) {
        if (beanClass == null) {
            return;
        }
        // Scan the class itself
        for (Method method : beanClass.getMethods()) {
            List<HintParameters> hints = scanMethodHints(method);
            if (!hints.isEmpty()) {
                addMethodHints(method, hints);
                // If it's an implementation of an interface method, also register for that interface method
                registerInterfaceMethods(beanClass, method, hints);
            }
        }
        // Scan all interfaces
        for (Class<?> iface : getAllInterfaces(beanClass)) {
            for (Method method : iface.getMethods()) {
                List<HintParameters> hints = scanMethodHints(method);
                if (!hints.isEmpty()) {
                    addMethodHints(method, hints);
                }
            }
        }
    }

    /**
     * Scans a specific method for {@link Hint} or {@link Hints} annotations.
     * @param method the method to scan
     * @return a list of hints found on the method
     */
    @NonNull
    private List<HintParameters> scanMethodHints(@NonNull Method method) {
        List<HintParameters> hints = new ArrayList<>();
        Hints hintsAnnotation = method.getAnnotation(Hints.class);
        if (hintsAnnotation != null) {
            for (Hint hint : hintsAnnotation.value()) {
                hints.add(parseHint(hint));
            }
        } else {
            Hint hintAnnotation = method.getAnnotation(Hint.class);
            if (hintAnnotation != null) {
                hints.add(parseHint(hintAnnotation));
            }
        }
        return hints;
    }

    /**
     * Adds the collected hints for a method to the cache.
     * @param method the method to which the hints apply
     * @param hints the list of hints to add
     */
    private void addMethodHints(Method method, List<HintParameters> hints) {
        if (methodHints == null) {
            methodHints = new HashMap<>();
        }
        methodHints.computeIfAbsent(method, k -> new ArrayList<>()).addAll(hints);
    }

    /**
     * Registers implementation-level hints for all corresponding interface methods.
     * @param beanClass the bean class
     * @param implMethod the implementation method
     * @param hints the hints to register
     */
    private void registerInterfaceMethods(Class<?> beanClass, Method implMethod, List<HintParameters> hints) {
        for (Class<?> iface : getAllInterfaces(beanClass)) {
            try {
                Method ifaceMethod = iface.getMethod(implMethod.getName(), implMethod.getParameterTypes());
                addMethodHints(ifaceMethod, hints);
            } catch (NoSuchMethodException e) {
                // ignore
            }
        }
    }

    /**
     * Returns all interfaces implemented by the given class and its superclasses.
     * @param clazz the class to inspect
     * @return a list of all implemented interfaces
     */
    @NonNull
    private List<Class<?>> getAllInterfaces(Class<?> clazz) {
        List<Class<?>> interfaces = new ArrayList<>();
        while (clazz != null) {
            for (Class<?> iface : clazz.getInterfaces()) {
                if (!interfaces.contains(iface)) {
                    interfaces.add(iface);
                    getAllInterfaces(iface, interfaces);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return interfaces;
    }

    /**
     * Recursively collects all super-interfaces of the given interface.
     * @param iface the interface to inspect
     * @param allInterfaces the list to populate with discovered interfaces
     */
    private void getAllInterfaces(@NonNull Class<?> iface, List<Class<?>> allInterfaces) {
        for (Class<?> superIface : iface.getInterfaces()) {
            if (!allInterfaces.contains(superIface)) {
                allInterfaces.add(superIface);
                getAllInterfaces(superIface, allInterfaces);
            }
        }
    }

    /**
     * Parses a {@link Hint} annotation's value into a {@link HintParameters} object.
     * @param hintAnnotation the hint annotation to parse
     * @return the parsed hint parameters
     * @throws BeanProxyException if parsing the hint value fails
     */
    @NonNull
    private HintParameters parseHint(@NonNull Hint hintAnnotation) {
        try {
            return new HintParameters(hintAnnotation.type(), hintAnnotation.value(), hintAnnotation.propagated());
        } catch (Exception e) {
            throw new BeanProxyException(getBeanRule(), "Failed to parse @Hint(type=\"" +
                hintAnnotation.type() + "\", value=\"" + hintAnnotation.value() + "\")", e);
        }
    }

    /**
     * A functional interface for invoking the original (super) method of a proxied bean.
     */
    @FunctionalInterface
    protected interface SuperInvoker {

        /**
         * Invokes the original method.
         * @return the result of the invocation
         * @throws Exception if the invocation fails
         */
        Object invoke() throws Exception;

    }

}
