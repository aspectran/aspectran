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
import com.aspectran.core.activity.InstantActivityException;
import com.aspectran.core.activity.InstantProxyActivity;
import com.aspectran.core.activity.aspect.AdviceConstraintViolationException;
import com.aspectran.core.activity.aspect.AdviceException;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.component.aspect.AdviceRuleRegistry;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.aspect.RelevantAspectRuleHolder;
import com.aspectran.core.component.aspect.pointcut.PointcutPattern;
import com.aspectran.core.component.bean.annotation.Advisable;
import com.aspectran.core.component.bean.annotation.Async;
import com.aspectran.core.component.bean.async.AsyncExecutionException;
import com.aspectran.core.component.bean.async.AsyncTaskExecutor;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;

/**
 * Base support class for bean proxies that apply Aspectran AOP advice.
 */
public abstract class AbstractBeanProxy {

    private final ActivityContext context;

    private final AspectRuleRegistry aspectRuleRegistry;

    /**
     * Creates a new AbstractBeanProxy.
     * @param context the activity context
     */
    public AbstractBeanProxy(@NonNull ActivityContext context) {
        this.context = context;
        this.aspectRuleRegistry = context.getAspectRuleRegistry();
    }

    protected Object invoke(BeanRule beanRule, Method method, Object[] args, SuperInvoker superInvoker)
            throws Throwable {
        if (!isAdvisableMethod(method)) {
            return superInvoker.invoke();
        }

        Async async = method.getAnnotation(Async.class);
        if (async != null) {
            if (context.hasCurrentActivity()) {
                return invokeAsync(beanRule, method, args, superInvoker, async, context.getCurrentActivity());
            } else {
                return invokeAsync(beanRule, method, args, superInvoker, async, null);
            }
        } else {
            if (context.hasCurrentActivity()) {
                return invokeSync(beanRule, method, superInvoker, context.getCurrentActivity());
            } else {
                try {
                    Activity activity = new InstantProxyActivity(context);
                    return activity.perform(() -> invokeSync(beanRule, method, superInvoker, activity));
                } catch (Exception e) {
                    throw new InstantActivityException(e);
                }
            }
        }
    }

    @Nullable
    private Object invokeAsync(
            BeanRule beanRule, @NonNull Method method, Object[] args,
            SuperInvoker superInvoker, Async async, Activity activity)
            throws AsyncExecutionException {
        AsyncTaskExecutor executor = findAsyncExecutor(async);
        if (method.getReturnType() != Void.TYPE && !Future.class.isAssignableFrom(method.getReturnType())) {
            throw new AsyncExecutionException("Cannot use @Async on a method that does not return void or Future: " + method);
        }
        Future<Object> future;
        if (activity != null) {
            future = executor.submit(() -> {
                Activity oldActivity = null;
                try {
                    if (context.hasCurrentActivity()) {
                        oldActivity = context.getCurrentActivity();
                    }
                    context.setCurrentActivity(activity);
                    Object result = invokeSync(beanRule, method, superInvoker, activity);
                    if (result instanceof Future<?> nestedFuture) {
                        return nestedFuture.get();
                    } else {
                        return result;
                    }
                } catch (Throwable e) {
                    throw handleAsyncException(e, executor, method, args);
                } finally {
                    if (oldActivity != null) {
                        context.setCurrentActivity(oldActivity);
                    } else {
                        context.removeCurrentActivity();
                    }
                }
            });
        } else {
            future = executor.submit(() -> {
                try {
                    Activity instanctActivity = new InstantProxyActivity(context);
                    Object result = instanctActivity.perform(() -> invokeSync(beanRule, method, superInvoker, instanctActivity));
                    if (result instanceof Future<?> nestedFuture) {
                        return nestedFuture.get();
                    } else {
                        return result;
                    }
                } catch (Exception e) {
                    throw handleAsyncException(e, executor, method, args);
                }
            });
        }
        if (method.getReturnType() == Void.TYPE) {
            return null;
        } else {
            return future;
        }
    }

    @Nullable
    private Object invokeSync(@NonNull BeanRule beanRule, @NonNull Method method, SuperInvoker superInvoker,
                              Activity activity) throws Throwable {
        String beanId = beanRule.getId();
        String className = beanRule.getClassName();
        String methodName = method.getName();

        AdviceRuleRegistry adviceRuleRegistry = getAdviceRuleRegistry(activity, beanId, className, methodName);
        if (adviceRuleRegistry == null) {
            return superInvoker.invoke();
        }

        try {
            try {
                executeAdvice(adviceRuleRegistry.getBeforeAdviceRuleList(), beanRule, activity);
                Object result = superInvoker.invoke();
                executeAdvice(adviceRuleRegistry.getAfterAdviceRuleList(), beanRule, activity);
                return result;
            } catch (Exception e) {
                activity.setRaisedException(e);
                throw e;
            } finally {
                executeAdvice(adviceRuleRegistry.getFinallyAdviceRuleList(), beanRule, activity);
            }
        } catch (Exception e) {
            activity.setRaisedException(e);
            if (handleException(adviceRuleRegistry.getExceptionRuleList(), activity)) {
                return null;
            }
            throw e;
        }
    }

    private AdviceRuleRegistry getAdviceRuleRegistry(
            @NonNull Activity activity, String beanId, String className, String methodName)
            throws AdviceConstraintViolationException, AdviceException {
        String requestName;
        boolean literalPattern;
        if (activity.hasTranslet()) {
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
        return adviceRuleRegistry;
    }

    private void executeAdvice(List<AdviceRule> adviceRuleList, BeanRule beanRule, Activity activity)
            throws AdviceException {
        if (adviceRuleList != null) {
            for (AdviceRule adviceRule : adviceRuleList) {
                if (!isSameBean(beanRule, adviceRule)) {
                    activity.executeAdvice(adviceRule);
                }
            }
        }
    }

    private boolean handleException(List<ExceptionRule> exceptionRuleList, @NonNull Activity activity)
            throws ActionExecutionException {
        if (exceptionRuleList != null) {
            activity.handleException(exceptionRuleList);
            return activity.isResponseReserved();
        }
        return false;
    }

    private boolean isAdvisableMethod(@NonNull Method method) {
        return (method.isAnnotationPresent(Advisable.class) || method.isAnnotationPresent(Async.class));
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

    @NonNull
    private CompletionException handleAsyncException(
            Throwable exception, AsyncTaskExecutor executor, Method method, Object[] args) {
        Throwable actualCause = exception;
        if (exception instanceof ActivityPerformException pe) {
            actualCause = pe.getCause();
        }
        if (executor.getAsyncUncaughtExceptionHandler() != null) {
            executor.getAsyncUncaughtExceptionHandler().handleUncaughtException(actualCause, method, args);
        }
        return new CompletionException(actualCause);
    }

    @FunctionalInterface
    protected interface SuperInvoker {

        Object invoke() throws Throwable;

    }

}
