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
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static com.aspectran.core.context.rule.RequestRule.CHARACTER_ENCODING_SETTING_NAME;
import static com.aspectran.core.context.rule.RequestRule.LOCALE_CHANGE_INTERCEPTOR_SETTING_NAME;
import static com.aspectran.core.context.rule.RequestRule.LOCALE_RESOLVER_SETTING_NAME;

/**
 * Core activity for handling official requests in Aspectran services.
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
     * Prepare for the activity.
     * @param requestName the request name
     * @throws TransletNotFoundException thrown if the translet is not found
     * @throws ActivityPrepareException  thrown when an exception occurs while preparing an activity
     */
    public void prepare(String requestName) throws TransletNotFoundException, ActivityPrepareException {
        TransletRule transletRule = findTransletRule(requestName, MethodType.GET);
        if (transletRule == null) {
            throw new TransletNotFoundException(requestName, MethodType.GET);
        }

        prepare(requestName, MethodType.GET, transletRule);
    }

    /**
     * Prepare for the activity.
     * @param transletRule the translet rule
     * @throws ActivityPrepareException thrown when an exception occurs while preparing an activity
     */
    public void prepare(TransletRule transletRule) throws ActivityPrepareException {
        prepare(transletRule.getName(), transletRule);
    }

    /**
     * Prepare for the activity.
     * @param requestName the request name
     * @param transletRule the translet rule
     * @throws ActivityPrepareException thrown when an exception occurs while preparing an activity
     */
    public void prepare(String requestName, TransletRule transletRule) throws ActivityPrepareException {
        prepare(requestName, MethodType.GET, transletRule);
    }

    /**
     * Prepare for the activity.
     * @param requestName the request name
     * @param requestMethod the request method
     * @throws TransletNotFoundException thrown if the translet is not found
     * @throws ActivityPrepareException  thrown when an exception occurs while preparing an activity
     */
    public void prepare(String requestName, String requestMethod)
            throws TransletNotFoundException, ActivityPrepareException {
        prepare(requestName, MethodType.resolve(requestMethod));
    }

    /**
     * Prepare for the activity.
     * @param requestName the request name
     * @param requestMethod the request method
     * @throws TransletNotFoundException thrown if the translet is not found
     * @throws ActivityPrepareException  thrown when an exception occurs while preparing an activity
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
     * Prepares a new activity for the Translet Rule by taking
     * the results of the process that was created earlier.
     * @param requestName the request name
     * @param requestMethod the request method
     * @param transletRule the translet rule
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

    protected void adapt() throws AdapterException {
        adapted = true;
    }

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
                executeAdvice(getBeforeAdviceRuleList(), true);

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
                    executeAdvice(getAfterAdviceRuleList(), true);
                }
            } catch (Exception e) {
                setRaisedException(e);
            } finally {
                if (!forwarding) {
                    setCurrentAdviceType(AdviceType.FINALLY);
                    executeAdvice(getFinallyAdviceRuleList(), false);
                }
            }

            if (!forwarding) {
                if (isExceptionRaised()) {
                    setCurrentAdviceType(AdviceType.THROWN);
                    exception();
                    if (translet != null) {
                        response();
                    }
                    if (isExceptionRaised()) {
                        throw getRaisedException();
                    }
                }

                setCurrentAdviceType(null);
            }
        } catch (ActivityTerminatedException e) {
            throw e;
        } catch (Throwable e) {
            if (translet != null) {
                throw new ActivityPerformException("Failed to perform activity for Translet " +
                        translet.getTransletRule(), e);
            }
            if (instantAction != null) {
                throw new ActivityPerformException("Failed to perform activity for instant action " +
                        instantAction, e);
            } else {
                throw new ActivityPerformException("Failed to perform activity", e);
            }
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
     * Produce the result of the content and its subordinate actions.
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

    @Nullable
    private ForwardRule response() throws ResponseException {
        if (!responded) {
            responded = true;
        } else {
            return null;
        }

        // Save flash attributes
        if (getFlashMapManager() != null && getTranslet() != null) {
            getFlashMapManager().saveFlashMap(getTranslet());
        }

        Response response = getResponse();
        if (response != null) {
            response.respond(this);
            if (isExceptionRaised()) {
                clearRaisedException();
            }
            if (response.getResponseType() == ResponseType.FORWARD) {
                ForwardResponse forwardResponse = (ForwardResponse)response;
                return forwardResponse.getForwardRule();
            }
        }
        return null;
    }

    private void forward(@NonNull ForwardRule forwardRule)
            throws TransletNotFoundException, ActivityPrepareException, ActivityPerformException {
        reserveResponse(null);
        responded = false;
        requestParsed = false;

        prepare(forwardRule.getTransletName(), forwardRule.getRequestMethod());
        perform();
    }

    private void exception() throws ActionExecutionException {
        reserveResponse(null);
        responded = false;

        if (hasTranslet() && getTransletRule().getExceptionRule() != null) {
            handleException(getTransletRule().getExceptionRule());
        }
        if (getExceptionRuleList() != null) {
            handleException(getExceptionRuleList());
        }
    }

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

    protected void execute(ActionList actionList) throws ActionExecutionException {
        execute(actionList, null);
    }

    protected void execute(ActionList actionList, ContentResult contentResult) throws ActionExecutionException {
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
     * Execute an action.
     * @param action the executable action
     * @param contentResult the content result
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
            setRaisedException(e);
            throw e;
        } catch (Exception e) {
            setRaisedException(e);
            throw new ActionExecutionException(action, e);
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

    private Response getResponse() {
        Response resp = this.reservedResponse;
        if (resp == null && !isExceptionRaised()) {
            resp = getDeclaredResponse();
        }
        return resp;
    }

    @Override
    public Response getDeclaredResponse() {
        return (getResponseRule() != null ? getResponseRule().getResponse() : null);
    }

    protected void reserveResponse(@Nullable Response response) {
        reservedResponse = response;
        if (response != null && !isExceptionRaised()) {
            desiredResponse = response;
        }
    }

    protected void reserveResponse() {
        if (reservedResponse == null) {
            reservedResponse = getDeclaredResponse();
        }
    }

    @Override
    public boolean isResponseReserved() {
        return (reservedResponse != null);
    }

    protected Response getDesiredResponse() {
        return (desiredResponse != null ? desiredResponse : getDeclaredResponse());
    }

    @Override
    public boolean isResponded() {
        return responded;
    }

    @Override
    public CoreTranslet getTranslet() {
        if (translet == null) {
            throw new IllegalStateException("No Translet");
        }
        return translet;
    }

    public boolean hasTranslet() {
        return (translet != null);
    }

    @Override
    public ProcessResult getProcessResult() {
        return (translet != null ? translet.getProcessResult() : null);
    }

    @Override
    public Object getProcessResult(String actionId) {
        if (getProcessResult() != null) {
            return getProcessResult().getResultValue(actionId);
        } else {
            return null;
        }
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

    private TransletRule findTransletRule(String requestName, MethodType requestMethod) {
        return getActivityContext().getTransletRuleRegistry().getTransletRule(requestName, requestMethod);
    }

    protected TransletRule getTransletRule() {
        return getTranslet().getTransletRule();
    }

    protected RequestRule getRequestRule() {
        return getTranslet().getRequestRule();
    }

    protected ResponseRule getResponseRule() {
        return getTranslet().getResponseRule();
    }

    /**
     * Returns the definitive request encoding.
     * @return the definitive request encoding
     */
    protected String getDefinitiveRequestEncoding() {
        String encoding = getRequestRule().getEncoding();
        if (encoding == null) {
            encoding = getSetting(CHARACTER_ENCODING_SETTING_NAME);
        }
        return encoding;
    }

    /**
     * Returns the definitive response encoding.
     * @return the definitive response encoding
     */
    protected String getDefinitiveResponseEncoding() {
        String encoding = getResponseRule().getEncoding();
        if (encoding == null) {
            encoding = getDefinitiveRequestEncoding();
        }
        return encoding;
    }

    /**
     * Parses the declared parameters.
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
     * Parses the declared attributes.
     * @throws MissingMandatoryAttributesException thrown if a required attribute is missing from the request
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

    private void parsePathVariables() {
        Token[] nameTokens = getTransletRule().getNameTokens();
        if (nameTokens != null && !(nameTokens.length == 1 && nameTokens[0].getType() == TokenType.TEXT)) {
            PathVariableMap pathVariables = PathVariableMap.parse(nameTokens, getTranslet().getRequestName());
            if (pathVariables != null) {
                pathVariables.applyTo(getTranslet());
            }
        }
    }

    private void loadFlashAttributes() {
        if (getFlashMapManager() != null) {
            FlashMap flashMap = getFlashMapManager().retrieveAndUpdate(translet);
            if (flashMap != null) {
                translet.setInputFlashMap(Collections.unmodifiableMap(flashMap));
            }
        }
    }

}
