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
     * @return the {@link Mode} for the currently running activity
     */
    Mode getMode();

    /**
     * Returns the {@link ActivityContext} associated with this activity.
     * @return the activity context
     */
    ActivityContext getActivityContext();

    /**
     * Returns the context path of the current activity.
     * @return the context path, or {@code null} for the root context
     */
    String getContextPath();

    /**
     * Returns the reverse context path used as a prefix to the actual request name.
     * This is typically used in web environments for URL rewriting.
     * @return the reverse context path
     */
    String getReverseContextPath();

    /**
     * Returns the {@link ClassLoader} associated with this activity's context.
     * @return the active ClassLoader
     */
    ClassLoader getClassLoader();

    /**
     * Returns the {@link Environment} of the current activity context.
     * @return the environment
     */
    Environment getEnvironment();

    /**
     * Returns the {@link ApplicationAdapter} for the current application environment.
     * @return the application adapter
     */
    ApplicationAdapter getApplicationAdapter();

    /**
     * Returns whether a {@link SessionAdapter} is available for this activity.
     * @return {@code true} if a session adapter is present; {@code false} otherwise
     */
    boolean hasSessionAdapter();

    /**
     * Returns the {@link SessionAdapter} for the current session environment.
     * @return the session adapter
     */
    SessionAdapter getSessionAdapter();

    /**
     * Returns the {@link RequestAdapter} for the current request.
     * @return the request adapter
     */
    RequestAdapter getRequestAdapter();

    /**
     * Returns the {@link ResponseAdapter} for the current response.
     * @return the response adapter
     */
    ResponseAdapter getResponseAdapter();

    /**
     * Performs the prepared activity, executing the translet's lifecycle.
     * @throws ActivityPerformException if an exception occurs during activity execution
     */
    void perform() throws ActivityPerformException;

    /**
     * Performs the given instant action within the context of this activity.
     * An instant action is a lightweight, short-lived operation.
     * @param <V> the result type of the instant action
     * @param instantAction the instant action to perform
     * @return the result of performing the instant action
     * @throws ActivityPerformException if an exception occurs while performing the instant action
     */
    <V> V perform(InstantAction<V> instantAction) throws ActivityPerformException;

    /**
     * Throws an {@link ActivityTerminatedException} to terminate the current activity.
     * This method is used to gracefully exit the activity processing flow.
     * @throws ActivityTerminatedException if the activity is terminated
     */
    void terminate() throws ActivityTerminatedException;

    /**
     * Throws an {@link ActivityTerminatedException} with a specified cause to terminate the current activity.
     * @param cause the reason for terminating the activity
     * @throws ActivityTerminatedException if the activity is terminated
     */
    void terminate(String cause) throws ActivityTerminatedException;

    /**
     * Returns the {@link Translet} instance associated with this activity.
     * @return the current translet
     */
    Translet getTranslet();

    /**
     * Indicates whether a {@link Translet} is currently associated with this activity.
     * @return {@code true} if a translet is present; {@code false} otherwise
     */
    boolean hasTranslet();

    /**
     * Returns the {@link ProcessResult} of the activity's execution.
     * This contains results from actions performed during the activity lifecycle.
     * @return the process result
     */
    ProcessResult getProcessResult();

    /**
     * Returns the mutable data map associated with this activity's execution.
     * This map can be used to store and retrieve data within the activity's scope.
     * @return the current {@link ActivityData}
     */
    ActivityData getActivityData();

    /**
     * Returns the originally declared {@link Response} for this activity.
     * @return the declared response
     */
    Response getDeclaredResponse();

    /**
     * Returns whether the response has been reserved (e.g., for a redirect or forward).
     * @return {@code true} if the response is reserved; {@code false} otherwise
     */
    boolean isResponseReserved();

    /**
     * Returns whether a response has been attempted after performing the activity.
     * @return {@code true} if a response was attempted; {@code false} otherwise
     */
    boolean isResponded();

    /**
     * Returns whether an exception was raised during the activity's execution.
     * @return {@code true} if an exception was raised; {@code false} otherwise
     */
    boolean isExceptionRaised();

    /**
     * Returns the exception that was raised during the activity's execution.
     * @return the raised exception, or {@code null} if no exception was raised
     */
    Throwable getRaisedException();

    /**
     * Returns the innermost cause of the chained (wrapped) exceptions that were raised.
     * @return the root cause of the raised exception, or {@code null} if no exception was raised
     */
    Throwable getRootCauseOfRaisedException();

    /**
     * Sets the exception that was raised during the activity's execution.
     * @param raisedException the exception to set
     */
    void setRaisedException(Exception raisedException);

    /**
     * Clears any exception that was previously set for this activity.
     */
    void clearRaisedException();

    /**
     * Dynamically registers an aspect rule with the current activity context.
     * This allows for runtime modification of AOP behavior.
     * @param aspectRule the aspect rule to register
     * @throws AdviceConstraintViolationException if an advice constraint is violated during registration
     * @throws AdviceException if an error occurs during advice processing
     */
    void registerAdviceRule(AspectRule aspectRule)
            throws AdviceConstraintViolationException, AdviceException;

    /**
     * Dynamically registers a settings advice rule with the current activity context.
     * Settings advice rules are applied to the activity's settings.
     * @param settingsAdviceRule the settings advice rule to register
     */
    void registerSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule);

    /**
     * Executes a list of advice actions.
     * @param adviceRuleList the list of advice rules to execute
     * @throws AdviceException if an error occurs during advice execution
     */
    void executeAdvice(List<AdviceRule> adviceRuleList) throws AdviceException;

    /**
     * Executes a single advice action.
     * @param adviceRule the advice rule to execute
     * @throws AdviceException if an error occurs during advice execution
     */
    void executeAdvice(AdviceRule adviceRule) throws AdviceException;

    /**
     * Handles exceptions based on a list of exception rules.
     * This method typically dispatches to an error handling translet or performs other recovery actions.
     * @param exceptionRuleList the list of exception rules to apply
     * @throws ActionExecutionException if an error occurs while executing an action during exception handling
     */
    void handleException(List<ExceptionRule> exceptionRuleList) throws ActionExecutionException;

    /**
     * Returns the value of a specified setting from the current activity scope.
     * Settings are typically configured in the translet rule or dynamically added.
     * @param <V> the type of the setting value
     * @param name the name of the setting
     * @return the setting value, or {@code null} if not found
     */
    <V> V getSetting(String name);

    /**
     * Puts a specified setting value into the current activity scope.
     * @param name the name of the setting
     * @param value the value to set for the setting
     */
    void putSetting(String name, Object value);

    /**
     * Returns whether a {@link StringifyContext} is available for this activity.
     * The stringify context is used for converting objects to string representations.
     * @return {@code true} if a stringify context is present; {@code false} otherwise
     */
    boolean hasStringifyContext();

    /**
     * Returns the {@link StringifyContext} that governs string conversions during rendering.
     * @return the stringify context, or {@code null} if none
     */
    StringifyContext getStringifyContext();

    /**
     * Returns the {@link TemplateRenderer} used to render views or templates.
     * @return the template renderer
     */
    TemplateRenderer getTemplateRenderer();

    /**
     * Returns the {@link TokenEvaluator} used to evaluate template tokens.
     * @return the token evaluator
     */
    TokenEvaluator getTokenEvaluator();

    /**
     * Returns the {@link ItemEvaluator} used to evaluate ASEL (Aspectran Scripting Expression Language) items.
     * @return the item evaluator
     */
    ItemEvaluator getItemEvaluator();

    /**
     * Returns the {@link FlashMapManager} for storing attributes across requests or successive interactions.
     * @return the flash map manager
     */
    FlashMapManager getFlashMapManager();

    /**
     * Returns the {@link LocaleResolver} responsible for determining the current locale.
     * @return the locale resolver
     */
    LocaleResolver getLocaleResolver();

    /**
     * Returns the advice bean instance associated with the given aspect ID.
     * @param <V> the type of the advice bean
     * @param aspectId the ID of the aspect
     * @return the advice bean object, or {@code null} if not found
     */
    <V> V getAdviceBean(String aspectId);

    /**
     * Returns the value produced by the registered BEFORE advice for the given aspect ID.
     * @param <V> the result type
     * @param aspectId the aspect identifier
     * @return the BEFORE advice result, or {@code null} if none
     */
    <V> V getBeforeAdviceResult(String aspectId);

    /**
     * Returns the value produced by the registered AFTER advice for the given aspect ID.
     * @param <V> the result type
     * @param aspectId the aspect identifier
     * @return the AFTER advice result, or {@code null} if none
     */
    <V> V getAfterAdviceResult(String aspectId);

    /**
     * Returns the value produced by the registered AROUND advice for the given aspect ID.
     * @param <V> the result type
     * @param aspectId the aspect identifier
     * @return the AROUND advice result, or {@code null} if none
     */
    <V> V getAroundAdviceResult(String aspectId);

    /**
     * Returns the value produced by the registered FINALLY advice for the given aspect ID.
     * @param <V> the result type
     * @param aspectId the aspect identifier
     * @return the FINALLY advice result, or {@code null} if none
     */
    <V> V getFinallyAdviceResult(String aspectId);

    /**
     * Returns an instance of the bean that matches the given ID.
     * @param <V> the type of bean object retrieved
     * @param id the ID of the bean to retrieve
     * @return an instance of the bean
     * @throws com.aspectran.core.component.bean.NoSuchBeanException if the bean is not found
     * @throws com.aspectran.core.component.bean.BeanException if an error occurs during bean retrieval
     */
    <V> V getBean(String id);

    /**
     * Returns an instance of the bean that matches the given object type.
     * @param <V> the type of bean object retrieved
     * @param type the type the bean must match; can be an interface or superclass.
     * @return an instance of the bean
     * @throws IllegalArgumentException if the type is null
     * @throws com.aspectran.core.component.bean.NoSuchBeanException if the bean is not found
     * @throws com.aspectran.core.component.bean.NoUniqueBeanException if multiple beans of the same type are found
     * @throws com.aspectran.core.component.bean.BeanException if an error occurs during bean retrieval
     */
    <V> V getBean(Class<V> type);

    /**
     * Returns an instance of the bean that matches the given object type and ID.
     * @param <V> the type of bean object retrieved
     * @param type the type the bean must match; can be an interface or superclass.
     * @param id the ID of the bean to retrieve
     * @return an instance of the bean
     * @throws com.aspectran.core.component.bean.NoSuchBeanException if the bean is not found
     * @throws com.aspectran.core.component.bean.BeanException if an error occurs during bean retrieval
     */
    <V> V getBean(Class<V> type, String id);

    /**
     * Creates and returns a new instance of a prototype-scoped bean described by the given rule.
     * @param <V> the bean type
     * @param beanRule the bean rule describing the prototype
     * @return a newly created bean instance
     * @throws com.aspectran.core.component.bean.BeanException if an error occurs during bean creation
     */
    <V> V getPrototypeScopeBean(BeanRule beanRule);

    /**
     * Returns whether a bean with the specified ID is present in the context.
     * @param id the ID of the bean to query
     * @return {@code true} if a bean with the specified ID is present; {@code false} otherwise
     */
    boolean containsBean(String id);

    /**
     * Returns whether a bean with the specified object type is present in the context.
     * @param type the object type of the bean to query
     * @return {@code true} if a bean with the specified type is present; {@code false} otherwise
     */
    boolean containsBean(Class<?> type);

    /**
     * Returns whether the bean corresponding to the specified object type and ID exists in the context.
     * @param type the object type of the bean to query
     * @param id the ID of the bean to query
     * @return {@code true} if a bean with the specified type and ID is present; {@code false} otherwise
     */
    boolean containsBean(Class<?> type, String id);

}
