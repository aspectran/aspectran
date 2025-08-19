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
package com.aspectran.core.activity;

import com.aspectran.core.activity.aspect.AdviceConstraintViolationException;
import com.aspectran.core.activity.aspect.AdviceException;
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
import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.utils.StringifyContext;

import java.util.List;

/**
 * Represents the central execution object for a single request-response lifecycle.
 * The Activity is the runtime engine that manages the entire process of handling a request,
 * from creation to completion.
 *
 * <p>As the primary orchestrator, an Activity instance holds all the contextual information
 * required for processing. This includes access to the core {@link ActivityContext},
 * environment-specific adapters (request, response, session), and the bean registry.
 * It is responsible for:
 * <ul>
 *   <li>Interpreting the {@code <translet>} rule that matches the incoming request.</li>
 *   <li>Creating and managing the {@link Translet} data context.</li>
 *   <li>Executing the defined lifecycle, which includes AOP advice (aspects) and a
 *       sequence of {@link com.aspectran.core.activity.process.action.Executable} actions.</li>
 *   <li>Maintaining the state of the ongoing process, such as handling exceptions and
 *       managing response flows (e.g., forward, redirect).</li>
 *   <li>Providing services like bean retrieval and expression evaluation to all
 *       framework components involved in the request.</li>
 * </ul>
 * This interface serves as the main entry point for the framework's internal components
 * to interact with the current request processing environment.</p>
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public interface Activity {

    /**
     * Defines the operational modes of the currently running activity.
     * <p>Each constant represents a distinct mode that influences how the activity behaves.</p>
     * <ul>
     *   <li><b>DAEMON</b> - Mode for activities run by daemon services with no user interface.</li>
     *   <li><b>DEFAULT</b> - Mode for activities run when no configured services are present.</li>
     *   <li><b>PROXY</b> - Mode for activities run within proxied objects.</li>
     *   <li><b>SCHEDULER</b> - Mode for activities run by scheduled tasks.</li>
     *   <li><b>SHELL</b> - Mode for activities run from the console via the command line.</li>
     *   <li><b>WEB</b> - Mode for activities run to handle web requests.</li>
     * </ul>
     */
    enum Mode {
        DAEMON,
        DEFAULT,
        PROXY,
        SCHEDULER,
        SHELL,
        WEB
    }

    /**
     * Returns the operating mode of the currently running activity.
     * @return {@link Mode} for the currently running activity
     */
    Mode getMode();

    /**
     * Gets the activity context.
     * @return the activity context
     */
    ActivityContext getActivityContext();

    /**
     * Returns the ClassLoader associated with this activity's context.
     * @return the active ClassLoader
     */
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

    /**
     * Returns whether a session adapter is available for this activity.
     * @return {@code true} if a session adapter is present; {@code false} otherwise
     */
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

    /**
     * Indicates whether a {@link Translet} is currently associated with this activity.
     * @return {@code true} if a translet is present; {@code false} otherwise
     */
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

    /**
     * Returns the mutable data map associated with this activity's execution.
     * @return the current {@link ActivityData}
     */
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
    boolean isResponded();

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
     * @throws AdviceException thrown when an error occurs while running advice
     */
    void registerAdviceRule(AspectRule aspectRule)
            throws AdviceConstraintViolationException, AdviceException;

    /**
     * Register a settings advice rule dynamically.
     * @param settingsAdviceRule the settings advice rule
     */
    void registerSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule);

    /**
     * Execute advice actions with given rules.
     * @param adviceRuleList the advice rules
     * @throws AdviceException thrown when an error occurs while running advice
     */
    void executeAdvice(List<AdviceRule> adviceRuleList) throws AdviceException;

    /**
     * Executes an advice action with a given rule.
     * @param adviceRule the advice rule
     * @throws AdviceException thrown when an error occurs while running advice
     */
    void executeAdvice(AdviceRule adviceRule) throws AdviceException;

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

    /**
     * Returns whether a {@link StringifyContext} is available for this activity.
     * @return {@code true} if a stringify context is present; {@code false} otherwise
     */
    boolean hasStringifyContext();

    /**
     * Returns the {@link StringifyContext} that governs string conversions during rendering.
     * @return the stringify context, or {@code null} if none
     */
    StringifyContext getStringifyContext();

    /**
     * Returns the template renderer used to render views/templates.
     * @return the template renderer
     */
    TemplateRenderer getTemplateRenderer();

    /**
     * Returns the token evaluator used to evaluate template tokens.
     * @return the token evaluator
     */
    TokenEvaluator getTokenEvaluator();

    /**
     * Returns the item evaluator used to evaluate ASEL items.
     * @return the item evaluator
     */
    ItemEvaluator getItemEvaluator();

    /**
     * Returns the {@link FlashMapManager} for storing attributes across requests.
     * @return the flash map manager
     */
    FlashMapManager getFlashMapManager();

    /**
     * Returns the {@link LocaleResolver} responsible for determining the current locale.
     * @return the locale resolver
     */
    LocaleResolver getLocaleResolver();

    /**
     * Gets the advice bean.
     * @param <V> the type of the bean
     * @param aspectId the aspect id
     * @return the advice bean object
     */
    <V> V getAdviceBean(String aspectId);

    /**
     * Returns the value produced by the registered BEFORE advice for the given aspect id.
     * @param <V> the result type
     * @param aspectId the aspect identifier
     * @return the BEFORE advice result, or {@code null} if none
     */
    <V> V getBeforeAdviceResult(String aspectId);

    /**
     * Returns the value produced by the registered AFTER advice for the given aspect id.
     * @param <V> the result type
     * @param aspectId the aspect identifier
     * @return the AFTER advice result, or {@code null} if none
     */
    <V> V getAfterAdviceResult(String aspectId);

    /**
     * Returns the value produced by the registered AROUND advice for the given aspect id.
     * @param <V> the result type
     * @param aspectId the aspect identifier
     * @return the AROUND advice result, or {@code null} if none
     */
    <V> V getAroundAdviceResult(String aspectId);

    /**
     * Returns the value produced by the registered FINALLY advice for the given aspect id.
     * @param <V> the result type
     * @param aspectId the aspect identifier
     * @return the FINALLY advice result, or {@code null} if none
     */
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

    /**
     * Creates and returns a new instance of a prototype-scoped bean described by the given rule.
     * @param <V> the bean type
     * @param beanRule the bean rule describing the prototype
     * @return a newly created bean instance
     */
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
