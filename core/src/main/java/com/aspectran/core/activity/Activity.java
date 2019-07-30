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
package com.aspectran.core.activity;

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;

import java.util.List;

/**
 * The Interface Activity.
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public interface Activity {

    /**
     * Prepare for the activity.
     *
     * @param transletName the translet name
     */
    void prepare(String transletName);

    /**
     * Prepare for the activity.
     *
     * @param transletRule the translet rule
     */
    void prepare(TransletRule transletRule);

    /**
     * Prepare for the activity.
     *
     * @param transletName the translet name
     * @param transletRule the translet rule
     */
    void prepare(String transletName, TransletRule transletRule);

    /**
     * Prepare for the activity.
     *
     * @param transletName the translet name
     * @param requestMethod the request method
     */
    void prepare(String transletName, String requestMethod);

    /**
     * Prepare for the activity.
     *
     * @param transletName the translet name
     * @param requestMethod the request method
     */
    void prepare(String transletName, MethodType requestMethod);

    /**
     * Performs the prepared activity.
     */
    void perform();

    /**
     * Finish the current activity.
     * It must be called to finish the activity.
     */
    void finish();

    /**
     * Throws an ActivityTerminatedException to terminate the current activity.
     *
     * @throws ActivityTerminatedException if an activity terminated without completion
     */
    void terminate();

    /**
     * Throws an ActivityTerminatedException with the reason for terminating the current activity.
     *
     * @param cause the termination cause
     * @throws ActivityTerminatedException the exception to terminate activity
     */
    void terminate(String cause);

    /**
     * Returns an instance of the current translet.
     *
     * @return an instance of the current translet
     */
    Translet getTranslet();

    /**
     * Returns the process result.
     *
     * @return the process result
     */
    ProcessResult getProcessResult();

    /**
     * Returns an action result for the specified action id from the process result,
     * or {@code null} if the action does not exist.
     *
     * @param actionId the specified action id
     * @return an action result
     */
    Object getProcessResult(String actionId);

    /**
     * Returns the originally declared response.
     *
     * @return the declared response
     * @since 5.2.0
     */
    Response getDeclaredResponse();

    /**
     * Returns whether the response is reserved.
     *
     * @return true, if the response is reserved
     */
    boolean isResponseReserved();

    /**
     * Returns whether or not contained in other activity.
     *
     * @return true, if this activity is included in the other activity
     */
    boolean isIncluded();

    /**
     * Returns whether the exception was thrown.
     *
     * @return true, if is exception raised
     */
    boolean isExceptionRaised();

    /**
     * Returns an instance of the currently raised exception.
     *
     * @return an instance of the currently raised exception
     */
    Throwable getRaisedException();

    /**
     * Returns the innermost one of the chained (wrapped) exceptions.
     *
     * @return the innermost one of the chained (wrapped) exceptions
     */
    Throwable getRootCauseOfRaisedException();

    /**
     * Sets an instance of the currently raised exception.
     *
     * @param raisedException an instance of the currently raised exception
     */
    void setRaisedException(Throwable raisedException);

    /**
     * Clears the exception that occurred during activity processing.
     */
    void clearRaisedException();

    /**
     * Register an aspect rule dynamically.
     *
     * @param aspectRule the aspect rule
     */
    void registerAspectRule(AspectRule aspectRule);

    /**
     * Register a settings advice rule dynamically.
     *
     * @param settingsAdviceRule the settings advice rule
     */
    void registerSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule);

    /**
     * Execute aspect advices with given rules.
     *
     * @param aspectAdviceRuleList the aspect advice rules
     * @param throwable whether to raise an exception
     */
    void executeAdvice(List<AspectAdviceRule> aspectAdviceRuleList, boolean throwable);

    /**
     * Executes an aspect advice with a given rule.
     *
     * @param aspectAdviceRule the aspect advice rule
     * @param throwable whether to raise an exception
     */
    void executeAdvice(AspectAdviceRule aspectAdviceRule, boolean throwable);

    /**
     * Exception handling.
     *
     * @param exceptionRuleList the exception rule list
     */
    void handleException(List<ExceptionRule> exceptionRuleList);

    /**
     * Gets the setting value in the translet scope.
     *
     * @param <T> the type of the value
     * @param settingName the setting name
     * @return the setting value
     */
    <T> T getSetting(String settingName);

    /**
     * Gets the aspect advice bean.
     *
     * @param <T> the type of the bean
     * @param aspectId the aspect id
     * @return the aspect advice bean object
     */
    <T> T getAspectAdviceBean(String aspectId);

    /**
     * Gets the activity context.
     *
     * @return the activity context
     */
    ActivityContext getActivityContext();

    /**
     * Returns the environment of the current activity context.
     *
     * @return the environment
     */
    Environment getEnvironment();

    /**
     * Gets the application adapter.
     *
     * @return the application adapter
     */
    ApplicationAdapter getApplicationAdapter();

    /**
     * Gets the session adapter.
     *
     * @return the session adapter
     */
    SessionAdapter getSessionAdapter();

    /**
     * Gets the request adapter.
     *
     * @return the request adapter
     */
    RequestAdapter getRequestAdapter();

    /**
     * Gets the response adapter.
     *
     * @return the response adapter
     */
    ResponseAdapter getResponseAdapter();

    /**
     * Create a new inner activity.
     *
     * @param <T> the type of the activity
     * @return the activity object
     */
    <T extends Activity> T newActivity();

    <T extends Activity> T getOuterActivity();

    /**
     * Return an instance of the bean that matches the given id.
     *
     * @param <T> the generic type
     * @param id the id of the bean to retrieve
     * @return an instance of the bean
     */
    <T> T getBean(String id);

    /**
     * Return an instance of the bean that matches the given object type.
     *
     * @param <T> the generic type
     * @param requiredType the type the bean must match; can be an interface or superclass.
     *      {@code null} is disallowed.
     * @return an instance of the bean
     * @since 1.3.1
     */
    <T> T getBean(Class<T> requiredType);

    /**
     * Return an instance of the bean that matches the given id.
     * If the bean is not of the required type then throw a BeanNotOfRequiredTypeException.
     *
     * @param <T> the generic type
     * @param id the id of the bean to retrieve
     * @param requiredType type the bean must match; can be an interface or superclass.
     *      {@code null} is disallowed.
     * @return an instance of the bean
     * @since 1.3.1
     */
    <T> T getBean(String id, Class<T> requiredType);

    /**
     * Return an instance of the bean that matches the given object type.
     * If the bean is not exists ,retrieve the bean with the specified id.
     *
     * @param <T> the generic type
     * @param requiredType type the bean must match; can be an interface or superclass.
     *      {@code null} is allowed.
     * @param id the id of the bean to retrieve; if requiredType is {@code null}.
     * @return an instance of the bean
     * @since 2.0.0
     */
    <T> T getBean(Class<T> requiredType, String id);

    /**
     * Return the bean instance that matches the specified object type.
     * If the bean is not of the required type then throw a {@code BeanNotOfRequiredTypeException}.
     *
     * @param <T> the generic type
     * @param requiredType type the bean must match; can be an interface or superclass.
     *      {@code null} is disallowed.
     * @return an instance of the bean
     * @since 2.0.0
     */
    <T> T getBeanForConfig(Class<T> requiredType);

    /**
     * Return whether a bean with the specified id is present.
     *
     * @param id the id of the bean to query
     * @return whether a bean with the specified id is present
     */
    boolean containsBean(String id);

    /**
     * Return whether a bean with the specified object type is present.
     *
     * @param requiredType the object type of the bean to query
     * @return whether a bean with the specified type is present
     */
    boolean containsBean(Class<?> requiredType);

}
