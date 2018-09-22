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
package com.aspectran.core.activity;

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.component.template.TemplateProcessor;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;

import java.util.List;

/**
 * The Interface Activity.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public interface Activity extends BeanRegistry {

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
     * Performs the prepared activity but does not respond to the client.
     */
    void performWithoutResponse();

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
     * Returns the request method.
     *
     * @return the request method
     */
    MethodType getRequestMethod();

    /**
     * Returns the name of the current translet.
     *
     * @return the translet name
     */
    String getTransletName();

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
     * Execute aspect advices with given rules.
     *
     * @param aspectAdviceRuleList the aspect advice rules
     */
    void executeAdvice(List<AspectAdviceRule> aspectAdviceRuleList);

    /**
     * Execute aspect advices with given rules, and does not raise exceptions.
     *
     * @param aspectAdviceRuleList the aspect advice rules
     */
    void executeAdviceWithoutThrow(List<AspectAdviceRule> aspectAdviceRuleList);

    /**
     * Executes an aspect advice with a given rule.
     *
     * @param aspectAdviceRule the aspect advice rule
     */
    void executeAdvice(AspectAdviceRule aspectAdviceRule);

    /**
     * Executes an aspect advice with a given rule and does not raise an exception.
     *
     * @param aspectAdviceRule the aspect advice rule
     */
    void executeAdviceWithoutThrow(AspectAdviceRule aspectAdviceRule);

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
     * Exception handling.
     *
     * @param exceptionRuleList the exception rule list
     */
    void handleException(List<ExceptionRule> exceptionRuleList);

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
     * Create a new inner activity.
     *
     * @param <T> the type of the activity
     * @return the activity object
     */
    <T extends Activity> T newActivity();

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
     * Gets the bean registry.
     *
     * @return the bean registry
     */
    BeanRegistry getBeanRegistry();

    /**
     * Gets the template processor.
     *
     * @return the template processor
     */
    TemplateProcessor getTemplateProcessor();

    /**
     * Gets the setting value in the translet scope.
     *
     * @param <T> the type of the value
     * @param settingName the setting name
     * @return the setting value
     */
    <T> T getSetting(String settingName);

    /**
     * Register an aspect rule dynamically.
     *
     * @param aspectRule the aspect rule
     */
    void registerAspectRule(AspectRule aspectRule);

    /**
     * Gets the aspect advice bean.
     *
     * @param <T> the type of the bean
     * @param aspectId the aspect id
     * @return the aspect advice bean object
     */
    <T> T getAspectAdviceBean(String aspectId);

}
