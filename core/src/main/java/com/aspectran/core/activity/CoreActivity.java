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

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.MissingMandatoryAttributesException;
import com.aspectran.core.activity.request.MissingMandatoryParametersException;
import com.aspectran.core.activity.request.PathVariableMap;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.component.bean.NoSuchBeanException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.rule.ChooseWhenRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AdviceType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.support.i18n.locale.LocaleChangeInterceptor;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static com.aspectran.core.context.rule.RequestRule.CHARACTER_ENCODING_SETTING_NAME;
import static com.aspectran.core.context.rule.RequestRule.LOCALE_CHANGE_INTERCEPTOR_SETTING_NAME;
import static com.aspectran.core.context.rule.RequestRule.LOCALE_RESOLVER_SETTING_NAME;

/**
 * The core implementation of the {@link Activity} interface.
 * <p>This class is the central engine for processing requests in Aspectran.
 * It manages the entire lifecycle of a request, including preparation, request parsing,
 * AOP advice execution, action processing, response generation, and exception handling.
 * It operates as a state machine, ensuring that each phase of the activity is executed
 * in the correct order.</p>
 *
 * <p>This class is generally not thread-safe. It is primarily designed
 * for use in a single thread only.</p>
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class CoreActivity extends AdviceActivity {

    private static final Logger logger = LoggerFactory.getLogger(CoreActivity.class);

    private final String contextPath;

    private CoreTranslet translet;

    private ActivityData activityData;

    private Response reservedResponse;

    private Response desiredResponse;

    private boolean adapted;

    private boolean requestParsed;

    private boolean forwarding;

    private boolean responded;

    /**
     * Instantiates a new CoreActivity.
     * @param context the activity context
     */
    protected CoreActivity(ActivityContext context) {
        this(context, null);
    }

    /**
     * Instantiates a new CoreActivity.
     * @param context the activity context
     * @param contextPath the context path
     */
    protected CoreActivity(ActivityContext context, String contextPath) {
        super(context);
        this.contextPath = StringUtils.emptyToNull(contextPath);
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getReverseContextPath() {
        return contextPath;
    }

    /**
     * Prepares the activity for a given request name. Assumes the GET method.
     * @param requestName the name of the request to be processed
     * @throws TransletNotFoundException if no translet matches the request name
     * @throws ActivityPrepareException if an error occurs during preparation
     */
    public void prepare(String requestName) throws TransletNotFoundException, ActivityPrepareException {
        TransletRule transletRule = findTransletRule(requestName, MethodType.GET);
        if (transletRule == null) {
            throw new TransletNotFoundException(requestName, MethodType.GET);
        }

        prepare(requestName, MethodType.GET, transletRule);
    }

    /**
     * Prepares the activity for a given {@link TransletRule}.
     * Assumes the GET method and uses the translet rule's name as the request name.
     * @param transletRule the pre-resolved translet rule to execute
     * @throws ActivityPrepareException if an error occurs during preparation
     */
    public void prepare(TransletRule transletRule) throws ActivityPrepareException {
        prepare(transletRule.getName(), transletRule);
    }

    /**
     * Prepares the activity for a given {@link TransletRule} with a specific request name.
     * This is useful when the request name (e.g., from a wildcard match) differs from the
     * translet rule's defined name. Assumes the GET method.
     * @param requestName the name of the request to be processed
     * @param transletRule the pre-resolved translet rule to execute
     * @throws ActivityPrepareException if an error occurs during preparation
     */
    public void prepare(String requestName, TransletRule transletRule) throws ActivityPrepareException {
        prepare(requestName, MethodType.GET, transletRule);
    }

    /**
     * Prepares the activity for a given request name and HTTP method.
     * @param requestName the name of the request to be processed
     * @param requestMethod the HTTP method as a string (e.g., "GET", "POST")
     * @throws TransletNotFoundException if no translet matches the request name and method
     * @throws ActivityPrepareException if an error occurs during preparation
     */
    public void prepare(String requestName, String requestMethod)
            throws TransletNotFoundException, ActivityPrepareException {
        prepare(requestName, MethodType.resolve(requestMethod));
    }

    /**
     * Prepares the activity for a given request name and {@link MethodType}.
     * If the method is null, it defaults to GET.
     * @param requestName the name of the request to be processed
     * @param requestMethod the HTTP method
     * @throws TransletNotFoundException if no translet matches the request name and method
     * @throws ActivityPrepareException if an error occurs during preparation
     */
    public void prepare(String requestName, MethodType requestMethod)
            throws TransletNotFoundException, ActivityPrepareException {
        if (requestMethod == null) {
            requestMethod = MethodType.GET;
        }

        TransletRule transletRule = findTransletRule(requestName, requestMethod);
        if (transletRule == null) {
            throw new TransletNotFoundException(requestName, requestMethod);
        }

        prepare(requestName, requestMethod, transletRule);
    }

    /**
     * Initializes the core components of the activity based on the provided translet rule.
     * <p>This internal method sets up the current translet, validates the request method against
     * the rule's allowed methods, and prepares the AOP advice chain for execution. If a previous
     * translet existed (e.g., in a forward), its process results are carried over to the new translet.</p>
     * @param requestName the name of the request being processed
     * @param requestMethod the HTTP method
     * @param transletRule the resolved translet rule for the current request
     * @throws ActivityPrepareException if initialization fails
     */
    protected void prepare(String requestName, MethodType requestMethod, TransletRule transletRule)
            throws ActivityPrepareException {
        Assert.notNull(requestName, "requestName must not be null");
        Assert.notNull(requestMethod, "requestMethod must not be null");
        Assert.notNull(transletRule, "transletRule must not be null");

        Translet prevTranslet = translet;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Translet {}", transletRule);
            }

            translet = new CoreTranslet(transletRule, this);
            translet.setRequestName(requestName);
            translet.setRequestMethod(requestMethod);
            if (prevTranslet != null) {
                translet.setProcessResult(prevTranslet.getProcessResult());
            }

            MethodType allowedMethod = getRequestRule().getAllowedMethod();
            if (allowedMethod != null && !allowedMethod.equals(requestMethod)) {
                throw new RequestMethodNotAllowedException(allowedMethod);
            }

            prepareAdviceRules(transletRule, transletRule.getName());
        } catch (Exception e) {
            throw new ActivityPrepareException("Failed to prepare activity for translet " + transletRule, e);
        }
    }

    /**
     * Adapts the native request and response objects to Aspectran's generic interfaces.
     * This method is a hook for subclasses to wrap vendor-specific objects (e.g.,
     * {@code HttpServletRequest}) with a corresponding {@code RequestAdapter}.
     * @throws AdapterException if the adaptation fails
     */
    protected void adapt() throws AdapterException {
        adapted = true;
    }

    /**
     * Orchestrates the request parsing phase of the activity.
     * <p>This involves processing declared parameters and attributes, resolving path variables
     * from the request name, loading flash attributes from a previous request, and resolving
     * the current locale.</p>
     * @throws RequestParseException if a parsing error occurs
     * @throws ActivityTerminatedException if the activity is terminated during parsing
     */
    protected void parseRequest() throws RequestParseException, ActivityTerminatedException {
        parseDeclaredParameters();
        parseDeclaredAttributes();
        parsePathVariables();
        if (!forwarding) {
            loadFlashAttributes();
            resolveLocale();
        }
        requestParsed = true;
    }

    /**
     * Resolves the locale and time zone for the current request.
     * It uses the configured {@link LocaleResolver} to determine the locale and time zone,
     * and then applies any configured {@link LocaleChangeInterceptor} to allow for
     * on-the-fly locale changes.
     */
    protected void resolveLocale() {
        LocaleResolver localeResolver = getLocaleResolver();
        String localeResolverBeanId = getSetting(LOCALE_RESOLVER_SETTING_NAME);
        if (localeResolverBeanId != null && !localeResolverBeanId.equals(LocaleResolver.LOCALE_RESOLVER_BEAN_ID)) {
            localeResolver = getBean(LocaleResolver.class, localeResolverBeanId);
        }
        if (localeResolver != null) {
            localeResolver.resolveLocale(getTranslet());
            localeResolver.resolveTimeZone(getTranslet());
            try {
                String localeChangeInterceptorId = getSetting(LOCALE_CHANGE_INTERCEPTOR_SETTING_NAME);
                LocaleChangeInterceptor localeChangeInterceptor = getBean(LocaleChangeInterceptor.class, localeChangeInterceptorId);
                localeChangeInterceptor.handle(getTranslet(), localeResolver);
            } catch (NoSuchBeanException e) {
                // ignore
            }
        }
    }

    @Override
    public void perform() throws ActivityPerformException {
        perform(null);
    }

    /**
     * Executes the entire activity lifecycle. This is the main entry point for activity processing.
     * The lifecycle includes:
     * <ol>
     *     <li>Initialization and context setup.</li>
     *     <li>Execution of 'before' AOP advice.</li>
     *     <li>Execution of the main content (either a translet's actions or an instant action).</li>
     *     <li>Execution of 'after' AOP advice (on successful execution).</li>
     *     <li>Execution of 'finally' AOP advice (always).</li>
     *     <li>If an exception was raised, the 'thrown' AOP advice and exception handling rules are processed.</li>
     *     <li>Resource cleanup and finalization.</li>
     * </ol>
     * @param instantAction an optional {@link InstantAction} to be executed as the main logic.
     *                      If null, the configured translet will be executed.
     * @param <V> the type of the result from the instant action
     * @return the result of the instant action, or null if a translet was executed
     * @throws ActivityPerformException if an unhandled exception occurs during the activity execution
     */
    @Override
    public <V> V perform(InstantAction<V> instantAction) throws ActivityPerformException {
        if (translet == null && instantAction == null) {
            throw new IllegalArgumentException("Either translet or instantAction is required");
        }

        V result = null;
        try {
            getActivityContext().getActivityCounter().increment();

            if (!adapted) {
                adapt();
            }

            if (!forwarding) {
                saveCurrentActivity();
            }

            if (translet != null && !requestParsed) {
                parseRequest();
            }

            try {
                setCurrentAdviceType(AdviceType.BEFORE);
                executeAdvice(getBeforeAdviceRuleList());

                if (translet != null) {
                    if (!isResponseReserved()) {
                        produce();
                    }

                    ForwardRule forwardRule = response();
                    if (forwardRule != null) {
                        if (forwarding) {
                            forward(forwardRule);
                        } else {
                            forwarding = true;
                            forward(forwardRule);
                            forwarding = false;
                        }
                    }
                }

                if (!forwarding) {
                    if (instantAction != null) {
                        result = instantAction.execute();
                    }

                    setCurrentAdviceType(AdviceType.AFTER);
                    executeAdvice(getAfterAdviceRuleList());
                }
            } catch (Exception e) {
                if (e instanceof ActionExecutionException && e.getCause() != null) {
                    setRaisedException(ExceptionUtils.getCause(e));
                } else {
                    setRaisedException(e);
                }
            } finally {
                if (!forwarding) {
                    setCurrentAdviceType(AdviceType.FINALLY);
                    executeAdvice(getFinallyAdviceRuleList());
                }
            }

            if (!forwarding) {
                if (isExceptionRaised()) {
                    setCurrentAdviceType(AdviceType.THROWN);
                    handleRaisedException();
                }
                setCurrentAdviceType(null);
            }
        } catch (ActivityTerminatedException e) {
            throw e;
        } catch (Throwable e) {
            throw createActivityPerformException(e, instantAction);
        } finally {
            try {
                if (!forwarding) {
                    finish();
                }
            } finally {
                getActivityContext().getActivityCounter().decrement();
            }
        }
        return result;
    }

    /**
     * Executes the main logic of the translet by processing its content sections.
     * <p>This method iterates through the {@code <content>} sections defined in the translet rule
     * and executes the associated actions. It also executes any actions defined directly
     * within the final {@code <response>} rule.</p>
     * @throws ActionExecutionException if an action fails during execution
     */
    private void produce() throws ActionExecutionException {
        ContentList contentList = getTransletRule().getContentList();
        if (contentList != null) {
            ProcessResult processResult = getTranslet().getProcessResult();
            if (processResult == null) {
                processResult = new ProcessResult(contentList.size());
                processResult.setName(contentList.getName());
                processResult.setExplicit(contentList.isExplicit());
                getTranslet().setProcessResult(processResult);
            }
            for (ActionList actionList : contentList) {
                execute(actionList);
                if (isResponseReserved()) {
                    break;
                }
            }
        }

        ActionList actionList = getResponseRule().getActionList();
        if (actionList != null) {
            execute(actionList);
        }
    }

    /**
     * Triggers the response generation process for the current activity.
     * <p>This method selects the appropriate {@link Response} object (a reserved response takes
     * precedence over a declared one) and invokes its {@code respond()} method. Once response
     * processing begins, any raised exception is cleared, as it is considered handled.
     * For redirect responses, it saves flash attributes. For forward responses, it returns
     * the corresponding {@link ForwardRule} to be handled by the {@code perform} method.</p>
     * @return a {@link ForwardRule} if the response is a forward; otherwise {@code null}
     * @throws ResponseException if an error occurs during response generation
     */
    @Nullable
    private ForwardRule response() throws ResponseException {
        if (!responded) {
            responded = true;
        } else {
            return null;
        }

        Response response = getResponse();
        if (response != null) {
            try {
                response.respond(this);
            } finally {
                clearRaisedException();
            }
            if (response.getResponseType() == ResponseType.REDIRECT) {
                // Save flash attributes
                if (getFlashMapManager() != null) {
                    getFlashMapManager().saveFlashMap(getTranslet());
                }
            } else if (response.getResponseType() == ResponseType.FORWARD) {
                ForwardResponse forwardResponse = (ForwardResponse)response;
                return forwardResponse.getForwardRule();
            }
        }
        return null;
    }

    /**
     * Forwards the current activity to another translet.
     * <p>This method resets the response state and recursively calls {@link #perform()} for the
     * new translet defined in the {@link ForwardRule}.</p>
     * @param forwardRule the rule defining the forward action
     * @throws TransletNotFoundException if the target translet is not found
     * @throws ActivityPrepareException if preparing the new activity fails
     * @throws ActivityPerformException if an error occurs during the forwarded activity's execution
     */
    private void forward(@NonNull ForwardRule forwardRule)
            throws TransletNotFoundException, ActivityPrepareException, ActivityPerformException {
        reserveResponse(null);
        responded = false;
        requestParsed = false;

        prepare(forwardRule.getTransletName(), forwardRule.getRequestMethod());
        perform();
    }

    /**
     * Performs final cleanup of the activity after execution and response generation.
     * <p>This includes destroying any request-scoped beans and committing the response,
     * which may involve flushing output streams. It ensures that the current activity is
     * removed from the thread-local context.</p>
     */
    private void finish() {
        try {
            if (getRequestAdapter() != null) {
                if (getRequestAdapter().hasRequestScope()) {
                    getRequestAdapter().getRequestScope().destroy();
                }
            }
            if (getResponseAdapter() != null && !isExceptionRaised()) {
                getResponseAdapter().commit();
            }
        } catch (Exception e) {
            logger.error("Error detected while finishing activity", e);
        } finally {
            removeCurrentActivity();
        }
    }

    //-------------------------------------------------------------------------------------
    // Exception Handling Methods
    //-------------------------------------------------------------------------------------

    /**
     * Orchestrates the handling of a previously raised exception.
     * <p>This method first processes all applicable exception handling rules from the translet
     * and any active aspects. If a rule handles the exception and specifies a response,
     * this method attempts to generate that response. If the exception remains unhandled
     * after all rules are processed, it is re-thrown to the caller.</p>
     * @throws Exception the original raised exception, if not handled by any rule
     */
    private void handleRaisedException() throws Exception {
        try {
            processExceptionRules();
            if (translet != null) {
                response();
            }
        } catch (Exception e) {
            logger.error("A new exception occurred while handling the original exception. The original exception will be re-thrown.", e);
        }
        if (isExceptionRaised()) {
            throw getRaisedException();
        }
    }

    /**
     * Executes all applicable exception handling rules for the currently raised exception.
     * <p>It processes rules defined both within the current translet and within any
     * active AOP aspects. This method resets the response state before processing the rules.</p>
     * @throws ActionExecutionException if an action within an exception handler fails
     */
    private void processExceptionRules() throws ActionExecutionException {
        reserveResponse(null);
        responded = false;

        if (hasTranslet() && getTransletRule().getExceptionRule() != null) {
            handleException(getTransletRule().getExceptionRule());
        }
        if (getExceptionRuleList() != null) {
            handleException(getExceptionRuleList());
        }
    }

    @Override
    protected ExceptionThrownRule handleException(ExceptionRule exceptionRule) throws ActionExecutionException {
        ExceptionThrownRule exceptionThrownRule = super.handleException(exceptionRule);
        if (hasTranslet() && exceptionThrownRule != null && !isResponseReserved()) {
            Response response = getDesiredResponse();
            String contentType = (response != null ? response.getContentType() : null);
            Response targetResponse = exceptionThrownRule.getResponse(contentType);
            if (targetResponse != null) {
                reserveResponse(targetResponse);
            }
        }
        return exceptionThrownRule;
    }

    /**
     * Creates a new {@link ActivityPerformException} with a detailed, context-aware message.
     * @param cause the root cause of the failure
     * @param instantAction the instant action being executed, if any
     * @return a new, informative {@code ActivityPerformException}
     */
    @NonNull
    private ActivityPerformException createActivityPerformException(Throwable cause, InstantAction<?> instantAction) {
        String contextDesc = null;
        if (translet != null) {
            contextDesc = " for Translet " + translet.getTransletRule();
        } else if (instantAction != null) {
            contextDesc = " for instant action " + instantAction;
        }
        String message = "Failed to perform activity" + (contextDesc != null ? "" : contextDesc);
        return new ActivityPerformException(message, cause);
    }

    //-------------------------------------------------------------------------------------
    // Action Executing Methods
    //-------------------------------------------------------------------------------------

    /**
     * Executes the actions in the given list.
     * @param actionList the list of actions to execute
     * @throws ActionExecutionException if an action fails during execution
     */
    private void execute(ActionList actionList) throws ActionExecutionException {
        execute(actionList, null);
    }

    /**
     * Executes the actions in the given list, storing the results in a {@link ContentResult}.
     * <p>If a {@code contentResult} is not provided, this method will create or retrieve one
     * from the current translet's {@link ProcessResult}.</p>
     * @param actionList the list of actions to execute
     * @param contentResult the container for storing action results
     * @throws ActionExecutionException if an action fails during execution
     */
    private void execute(ActionList actionList, ContentResult contentResult) throws ActionExecutionException {
        if (contentResult == null) {
            ProcessResult processResult = getTranslet().getProcessResult();
            if (processResult == null) {
                processResult = new ProcessResult(2); // Consider adding one when an exception occurs
                getTranslet().setProcessResult(processResult);
            }
            contentResult = processResult.getContentResult(actionList.getName(), actionList.isExplicit());
            if (contentResult == null) {
                contentResult = new ContentResult(processResult, actionList.size());
                contentResult.setName(actionList.getName());
                if (!processResult.isExplicit()) {
                    contentResult.setExplicit(actionList.isExplicit());
                }
            }
        }
        for (Executable action : actionList) {
            execute(action, contentResult);
            if (isResponseReserved()) {
                break;
            }
        }
    }

    /**
     * Executes a single action and records its result.
     * <p>This method handles the special case for {@code <choose>} actions, executing the
     * nested actions of the first matching {@code <when>} condition. For all other actions,
     * it executes the action and, if it is not hidden, adds the result to the provided
     * {@link ContentResult}.</p>
     * @param action the action to execute
     * @param contentResult the container for storing the action's result
     * @throws ActionExecutionException if the action fails during execution
     */
    private void execute(Executable action, ContentResult contentResult) throws ActionExecutionException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Action {}", action);
            }
            if (action.getActionType() == ActionType.CHOOSE) {
                Object resultValue = action.execute(this);
                if (resultValue != Void.TYPE) {
                    ChooseWhenRule chooseWhenRule = (ChooseWhenRule)resultValue;
                    ActionList actionList = chooseWhenRule.getActionList();
                    execute(actionList, contentResult);
                    if (chooseWhenRule.getResponse() != null) {
                        reserveResponse(chooseWhenRule.getResponse());
                    }
                }
            } else {
                Object resultValue = action.execute(this);
                if (!action.isHidden() && contentResult != null && resultValue != Void.TYPE) {
                    if (resultValue instanceof ProcessResult processResult) {
                        contentResult.addActionResult(action, processResult);
                    } else {
                        contentResult.addActionResult(action, resultValue);
                    }
                }
            }
        } catch (ActionExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ActionExecutionException(action, e);
        }
    }

    //-------------------------------------------------------------------------------------
    // Response-related Methods
    //-------------------------------------------------------------------------------------

    /**
     * Returns the response to be executed.
     * <p>A reserved response takes precedence over a declared response.
     * If an exception has been raised, only a reserved response will be returned.</p>
     * @return the response to execute, or {@code null} if none is available
     */
    private Response getResponse() {
        Response response = reservedResponse;
        if (response == null && !isExceptionRaised()) {
            response = getDeclaredResponse();
        }
        return response;
    }

    @Override
    public Response getDeclaredResponse() {
        return (getResponseRule() != null ? getResponseRule().getResponse() : null);
    }

    /**
     * Reserves a response to be executed, overriding the default declared response.
     * <p>If an exception has not been raised, the reserved response is also stored as the
     * "desired response" for later reference, such as in exception handling scenarios.</p>
     * @param response the response to reserve; can be {@code null} to clear a reservation
     */
    protected void reserveResponse(@Nullable Response response) {
        reservedResponse = response;
        if (response != null && !isExceptionRaised()) {
            desiredResponse = response;
        }
    }

    /**
     * Reserves the currently declared response.
     */
    protected void reserveResponse() {
        if (reservedResponse == null) {
            reservedResponse = getDeclaredResponse();
        }
    }

    @Override
    public boolean isResponseReserved() {
        return (reservedResponse != null);
    }

    /**
     * Returns the response that was desired before any exception was raised.
     * <p>This is useful in exception handlers to determine what the original
     * response would have been.</p>
     * @return the desired response
     */
    protected Response getDesiredResponse() {
        return (desiredResponse != null ? desiredResponse : getDeclaredResponse());
    }

    @Override
    public boolean isResponded() {
        return responded;
    }

    //-------------------------------------------------------------------------------------
    // Other Methods
    //-------------------------------------------------------------------------------------

    @Override
    public CoreTranslet getTranslet() {
        Assert.state(translet != null, "No Translet has been prepared");
        return translet;
    }

    /**
     * Returns whether a translet has been prepared for this activity.
     * @return true if a translet is available, false otherwise
     */
    public boolean hasTranslet() {
        return (translet != null);
    }

    @Override
    public ProcessResult getProcessResult() {
        return (translet != null ? translet.getProcessResult() : null);
    }

    @Override
    public ActivityData getActivityData() {
        if (activityData == null) {
            activityData = new ActivityData(this);
        } else {
            activityData.refresh();
        }
        return activityData;
    }

    /**
     * Finds a translet rule matching the given request name and method.
     * @param requestName the name of the request
     * @param requestMethod the HTTP method
     * @return the matching {@link TransletRule}, or {@code null} if not found
     */
    private TransletRule findTransletRule(String requestName, MethodType requestMethod) {
        return getActivityContext().getTransletRuleRegistry().getTransletRule(requestName, requestMethod);
    }

    /**
     * Returns the rule for the current translet.
     * @return the current {@link TransletRule}
     */
    protected TransletRule getTransletRule() {
        return getTranslet().getTransletRule();
    }

    /**
     * Returns the request rule for the current translet.
     * @return the current {@link RequestRule}
     */
    protected RequestRule getRequestRule() {
        return getTranslet().getRequestRule();
    }

    /**
     * Returns the response rule for the current translet.
     * @return the current {@link ResponseRule}
     */
    protected ResponseRule getResponseRule() {
        return getTranslet().getResponseRule();
    }

    /**
     * Determines the definitive request character encoding.
     * <p>The encoding is resolved in the following order of precedence:
     * 1. Encoding specified in the translet's request rule.
     * 2. Default encoding specified in the Aspectran settings.
     * 3. "UTF-8" as the final fallback.</p>
     * @return the definitive request encoding, never {@code null}
     */
    protected String getDefinitiveRequestEncoding() {
        String encoding = getRequestRule().getEncoding();
        if (encoding == null) {
            encoding = getSetting(CHARACTER_ENCODING_SETTING_NAME);
        }
        return (encoding != null ? encoding : StandardCharsets.UTF_8.name());
    }

    /**
     * Determines the definitive response character encoding.
     * <p>The encoding is resolved in the following order of precedence:
     * 1. Encoding specified in the translet's response rule.
     * 2. The definitive request encoding.</p>
     * @return the definitive response encoding, never {@code null}
     */
    protected String getDefinitiveResponseEncoding() {
        String encoding = getResponseRule().getEncoding();
        if (encoding == null) {
            encoding = getDefinitiveRequestEncoding();
        }
        return encoding;
    }

    /**
     * Parses and evaluates parameters as defined in the request rule.
     * <p>This method processes {@code <parameter>} rules, evaluates their values if specified,
     * and checks for the presence of all mandatory parameters.</p>
     * @throws MissingMandatoryParametersException if a mandatory parameter is not found
     */
    protected void parseDeclaredParameters() throws MissingMandatoryParametersException {
        ItemRuleMap itemRuleMap = getRequestRule().getParameterItemRuleMap();
        if (itemRuleMap != null && !itemRuleMap.isEmpty()) {
            ItemRuleList missingItemRules = null;
            for (ItemRule itemRule : itemRuleMap.values()) {
                if (itemRule.isEvaluable()) {
                    String[] oldValues = getRequestAdapter().getParameterValues(itemRule.getName());
                    String[] newValues = getItemEvaluator().evaluateAsStringArray(itemRule);
                    if (oldValues != newValues) {
                        getRequestAdapter().setParameter(itemRule.getName(), newValues);
                    }
                }
                if (itemRule.isMandatory()) {
                    String[] values = getRequestAdapter().getParameterValues(itemRule.getName());
                    if (values == null) {
                        if (missingItemRules == null) {
                            missingItemRules = new ItemRuleList();
                        }
                        missingItemRules.add(itemRule);
                    }
                }
            }
            if (missingItemRules != null) {
                throw new MissingMandatoryParametersException(missingItemRules);
            }
        }
    }

    /**
     * Parses and evaluates attributes as defined in the request rule.
     * <p>This method processes {@code <attribute>} rules, evaluates their values if specified,
     * and checks for the presence of all mandatory attributes.</p>
     * @throws MissingMandatoryAttributesException if a mandatory attribute is not found
     */
    protected void parseDeclaredAttributes() throws MissingMandatoryAttributesException {
        ItemRuleMap itemRuleMap = getRequestRule().getAttributeItemRuleMap();
        if (itemRuleMap != null && !itemRuleMap.isEmpty()) {
            ItemRuleList missingItemRules = null;
            for (ItemRule itemRule : itemRuleMap.values()) {
                if (itemRule.isEvaluable()) {
                    Object oldValue = getRequestAdapter().getAttribute(itemRule.getName());
                    Object newValue = getItemEvaluator().evaluate(itemRule);
                    if (oldValue != newValue) {
                        getRequestAdapter().setAttribute(itemRule.getName(), newValue);
                    }
                }
                if (itemRule.isMandatory()) {
                    Object value = getRequestAdapter().getAttribute(itemRule.getName());
                    if (value == null) {
                        if (missingItemRules == null) {
                            missingItemRules = new ItemRuleList();
                        }
                        missingItemRules.add(itemRule);
                    }
                }
            }
            if (missingItemRules != null) {
                throw new MissingMandatoryAttributesException(missingItemRules);
            }
        }
    }

    /**
     * Parses path variables from the request name if the translet rule contains variable tokens.
     * The extracted variables are then added to the activity's attributes.
     */
    private void parsePathVariables() {
        Token[] nameTokens = getTransletRule().getNameTokens();
        if (nameTokens != null && !(nameTokens.length == 1 && nameTokens[0].getType() == TokenType.TEXT)) {
            PathVariableMap pathVariables = PathVariableMap.parse(nameTokens, getTranslet().getRequestName());
            if (pathVariables != null) {
                pathVariables.applyTo(getTranslet());
            }
        }
    }

    /**
     * Retrieves flash attributes from the {@link FlashMapManager} and adds them to the current
     * translet's attributes. Flash attributes are typically used to pass data between redirects.
     */
    private void loadFlashAttributes() {
        if (getFlashMapManager() != null) {
            FlashMap flashMap = getFlashMapManager().retrieveAndUpdate(translet);
            if (flashMap != null) {
                translet.setInputFlashMap(Collections.unmodifiableMap(flashMap));
            }
        }
    }

}
