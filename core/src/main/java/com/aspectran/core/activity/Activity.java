/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.core.activity;

import com.aspectran.core.activity.aspect.AdviceConstraintViolationException;
import com.aspectran.core.activity.aspect.AspectAdviceException;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.template.TemplateRenderer;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.asel.item.ItemEvaluator;
import com.aspectran.core.context.asel.token.TokenEvaluator;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.utils.StringifyContext;

import java.util.List;

/**
 * An activity is a set of actions that an Aspectran service performs to process a request.
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public interface Activity {

    enum Mode {
        DAEMON,
        DEFAULT,
        PROXY,
        SCHEDULER,
        SHELL,
        WEB
    }

    Mode getMode();

    /**
     * Gets the activity context.
     * @return the activity context
     */
    ActivityContext getActivityContext();

    ClassLoader getClassLoader();

    /**
     * Returns the environment of the current activity context.
     * @return the environment
     */
    Environment getEnvironment();

    /**
     * Gets the application adapter.
     * @return the application adapter
     */
    ApplicationAdapter getApplicationAdapter();

    boolean hasSessionAdapter();

    /**
     * Gets the session adapter.
     * @return the session adapter
     */
    SessionAdapter getSessionAdapter();

    /**
     * Gets the request adapter.
     * @return the request adapter
     */
    RequestAdapter getRequestAdapter();

    /**
     * Gets the response adapter.
     * @return the response adapter
     */
    ResponseAdapter getResponseAdapter();

    /**
     * Returns the context path. If the context path is not specified,
     * {@code null} is returned rather than an empty string.
     * @return the context path of this activity, or {@code null} for
     *      the root context
     */
    String getContextPath();

    /**
     * Returns the reverse context path used as a prefix to the actual request name.
     * @return the reverse context path
     */
    String getReverseContextPath();

    /**
     * Performs the prepared activity.
     * @throws ActivityPerformException thrown when an exception occurs while performing an activity
     */
    void perform() throws ActivityPerformException;

    /**
     * Performs the given instant activity.
     * @param <V> the result type of the instant action
     * @param instantAction the instant action
     * @return An object that is the result of performing an instant activity
     * @throws ActivityPerformException thrown when an exception occurs while performing an activity
     */
    <V> V perform(InstantAction<V> instantAction) throws ActivityPerformException;

    /**
     * Throws an ActivityTerminatedException to terminate the current activity.
     * @throws ActivityTerminatedException if an activity terminated without completion
     */
    void terminate() throws ActivityTerminatedException;

    /**
     * Throws an ActivityTerminatedException with the reason for terminating the current activity.
     * @param cause the termination cause
     * @throws ActivityTerminatedException the exception to terminate activity
     */
    void terminate(String cause) throws ActivityTerminatedException;

    /**
     * Returns an instance of the current translet.
     * @return an instance of the current translet
     */
    Translet getTranslet();

    boolean hasTranslet();

    /**
     * Returns the process result.
     * @return the process result
     */
    ProcessResult getProcessResult();

    /**
     * Returns an action result for the specified action id from the process result,
     * or {@code null} if the action does not exist.
     * @param actionId the specified action id
     * @return an action result
     */
    Object getProcessResult(String actionId);

    ActivityData getActivityData();

    /**
     * Returns the originally declared response.
     * @return the declared response
     * @since 5.2.0
     */
    Response getDeclaredResponse();

    /**
     * Returns whether the response is reserved.
     * @return true, if the response is reserved
     */
    boolean isResponseReserved();

    /**
     * Returns whether a response was attempted after performing the activity.
     * @return true if a response was attempted, false otherwise
     */
    boolean isCommitted();

    /**
     * Returns whether an exception was thrown in the activity.
     * @return true if there was an exception thrown by the activity, false otherwise
     */
    boolean isExceptionRaised();

    /**
     * Returns an instance of the currently raised exception.
     * @return an instance of the currently raised exception
     */
    Throwable getRaisedException();

    /**
     * Returns the innermost one of the chained (wrapped) exceptions.
     * @return the innermost one of the chained (wrapped) exceptions
     */
    Throwable getRootCauseOfRaisedException();

    /**
     * Sets an instance of the currently raised exception.
     * @param raisedException an instance of the currently raised exception
     */
    void setRaisedException(Throwable raisedException);

    /**
     * Clears the exception that occurred during activity processing.
     */
    void clearRaisedException();

    /**
     * Register an aspect rule dynamically.
     * @param aspectRule the aspect rule
     * @throws AdviceConstraintViolationException thrown when an Advice Constraint Violation occurs
     * @throws AspectAdviceException thrown when an error occurs while running advice
     */
    void registerAspectAdviceRule(AspectRule aspectRule)
            throws AdviceConstraintViolationException, AspectAdviceException;

    /**
     * Register a settings advice rule dynamically.
     * @param settingsAdviceRule the settings advice rule
     */
    void registerSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule);

    /**
     * Execute aspect advices with given rules.
     * @param aspectAdviceRuleList the aspect advice rules
     * @param throwable whether to raise an exception
     * @throws AspectAdviceException thrown when an error occurs while running advice
     */
    void executeAdvice(List<AspectAdviceRule> aspectAdviceRuleList, boolean throwable) throws AspectAdviceException;

    /**
     * Executes an aspect advice with a given rule.
     * @param aspectAdviceRule the aspect advice rule
     * @param throwable whether to raise an exception
     * @throws AspectAdviceException thrown when an error occurs while running advice
     */
    void executeAdvice(AspectAdviceRule aspectAdviceRule, boolean throwable) throws AspectAdviceException;

    /**
     * Exception handling.
     * @param exceptionRuleList the exception rule list
     * @throws ActionExecutionException thrown when an error occurs while executing an action
     */
    void handleException(List<ExceptionRule> exceptionRuleList) throws ActionExecutionException;

    /**
     * Gets the specified setting value from the current activity scope.
     * @param <V> the type of the value
     * @param name the setting name
     * @return the setting value
     */
    <V> V getSetting(String name);

    /**
     * Puts the specified setting value in the current activity scope.
     * @param name the setting name
     * @param value the setting value
     */
    void putSetting(String name, Object value);

    boolean hasStringifyContext();

    StringifyContext getStringifyContext();

    TemplateRenderer getTemplateRenderer();

    TokenEvaluator getTokenEvaluator();

    ItemEvaluator getItemEvaluator();

    FlashMapManager getFlashMapManager();

    LocaleResolver getLocaleResolver();

    /**
     * Gets the aspect advice bean.
     * @param <V> the type of the bean
     * @param aspectId the aspect id
     * @return the aspect advice bean object
     */
    <V> V getAspectAdviceBean(String aspectId);

    <V> V getBeforeAdviceResult(String aspectId);

    <V> V getAfterAdviceResult(String aspectId);

    <V> V getAroundAdviceResult(String aspectId);

    <V> V getFinallyAdviceResult(String aspectId);

    /**
     * Returns an instance of the bean that matches the given id.
     * @param <V> the type of bean object retrieved
     * @param id the id of the bean to retrieve
     * @return an instance of the bean
     */
    <V> V getBean(String id);

    /**
     * Returns an instance of the bean that matches the given object type.
     * @param <V> the type of bean object retrieved
     * @param type the type the bean must match; can be an interface or superclass.
     *      {@code null} is disallowed.
     * @return an instance of the bean
     * @since 1.3.1
     */
    <V> V getBean(Class<V> type);

    /**
     * Returns an instance of the bean that matches the given object type.
     * @param <V> the type of bean object retrieved
     * @param type type the bean must match; can be an interface or superclass.
     *      {@code null} is allowed.
     * @param id the id of the bean to retrieve
     * @return an instance of the bean
     * @since 2.0.0
     */
    <V> V getBean(Class<V> type, String id);

    <V> V getPrototypeScopeBean(BeanRule beanRule);

    /**
     * Returns whether a bean with the specified id is present.
     * @param id the id of the bean to query
     * @return whether a bean with the specified id is present
     */
    boolean containsBean(String id);

    /**
     * Returns whether a bean with the specified object type is present.
     * @param type the object type of the bean to query
     * @return whether a bean with the specified type is present
     */
    boolean containsBean(Class<?> type);

    /**
     * Returns whether the bean corresponding to the specified object type and ID exists.
     * @param type the object type of the bean to query
     * @param id the id of the bean to query
     * @return whether a bean with the specified type is present
     */
    boolean containsBean(Class<?> type, String id);

}
