/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.activity.process.result.ActionResult;
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
import com.aspectran.core.component.bean.scope.Scope;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.ItemEvaluation;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.token.Token;
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
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

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

    private CoreTranslet translet;

    private Response reservedResponse;

    private Response desiredResponse;

    private boolean committed;

    /**
     * Instantiates a new CoreActivity.
     * @param context the activity context
     */
    protected CoreActivity(ActivityContext context) {
        super(context);
    }

    /**
     * Prepare for the activity.
     * @param requestName the request name
     * @throws TransletNotFoundException thrown if the translet is not found
     * @throws ActivityPrepareException thrown when an exception occurs while preparing an activity
     */
    public void prepare(String requestName)
            throws TransletNotFoundException, ActivityPrepareException {
        TransletRule transletRule = getTransletRule(requestName, MethodType.GET);
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
     * @throws ActivityPrepareException thrown when an exception occurs while preparing an activity
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
     * @throws ActivityPrepareException thrown when an exception occurs while preparing an activity
     */
    public void prepare(String requestName, MethodType requestMethod)
            throws TransletNotFoundException, ActivityPrepareException {
        if (requestMethod == null) {
            requestMethod = MethodType.GET;
        }

        TransletRule transletRule = getTransletRule(requestName, requestMethod);
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
    public void prepare(String requestName, MethodType requestMethod, TransletRule transletRule)
            throws ActivityPrepareException {
        Translet parentTranslet = translet;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Translet " + transletRule);
            }

            translet = new CoreTranslet(transletRule, this);
            translet.setRequestName(requestName); // original request name
            translet.setRequestMethod(requestMethod);
            if (parentTranslet != null) {
                translet.setParentTranslet(parentTranslet);
            }

            MethodType allowedMethod = getRequestRule().getAllowedMethod();
            if (allowedMethod != null && !allowedMethod.equals(requestMethod)) {
                throw new RequestMethodNotAllowedException(allowedMethod);
            }

            prepareAspectAdviceRule(transletRule, requestName);
        } catch (Exception e) {
            throw new ActivityPrepareException("Failed to prepare activity for translet " + transletRule, e);
        }
    }

    protected void adapt() throws AdapterException {
    }

    protected void parseRequest() throws RequestParseException, ActivityTerminatedException {
        if (translet != null) {
            parseDeclaredParameters();
            parseDeclaredAttributes();
            parsePathVariables();
            if (!translet.hasParentTranslet()) {
                resolveLocale();
            }
        }
    }

    protected LocaleResolver resolveLocale() {
        LocaleResolver localeResolver = null;
        String localeResolverBeanId = getSetting(RequestRule.LOCALE_RESOLVER_SETTING_NAME);
        if (localeResolverBeanId != null) {
            localeResolver = getBean(LocaleResolver.class, localeResolverBeanId);
            localeResolver.resolveLocale(getTranslet());
            localeResolver.resolveTimeZone(getTranslet());
        }
        return localeResolver;
    }

    @Override
    public void perform() throws ActivityPerformException {
        perform(null);
    }

    @Override
    public <V> V perform(InstantAction<V> instantAction) throws ActivityPerformException {
        V result = null;
        ForwardRule forwardRule = null;
        try {
            if (translet == null || !translet.hasParentTranslet()) {
                saveCurrentActivity();
                adapt();
                parseRequest();
            }

            try {
                setCurrentAspectAdviceType(AspectAdviceType.BEFORE);
                executeAdvice(getBeforeAdviceRuleList(), true);

                if (translet != null) {
                    if (!isResponseReserved()) {
                        produce();
                    }

                    forwardRule = response();
                    if (forwardRule != null) {
                        return forward(forwardRule, instantAction);
                    }
                }

                if (instantAction != null) {
                    result = instantAction.execute();
                }

                setCurrentAspectAdviceType(AspectAdviceType.AFTER);
                executeAdvice(getAfterAdviceRuleList(), true);
            } catch (Exception e) {
                setRaisedException(e);
            } finally {
                if (forwardRule == null) {
                    setCurrentAspectAdviceType(AspectAdviceType.FINALLY);
                    executeAdvice(getFinallyAdviceRuleList(), false);
                }
            }

            if (isExceptionRaised()) {
                setCurrentAspectAdviceType(AspectAdviceType.THROWN);
                exception();
                if (translet != null) {
                    response();
                }
                if (isExceptionRaised()) {
                    throw getRaisedException();
                }
            }

            setCurrentAspectAdviceType(null);
        } catch (ActivityTerminatedException e) {
            throw e;
        } catch (Throwable e) {
            throw new ActivityPerformException("Failed to perform the activity", e);
        } finally {
            if (forwardRule == null) {
                finish();
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
            ProcessResult processResult = translet.getProcessResult();
            if (processResult == null) {
                processResult = new ProcessResult(contentList.size());
                processResult.setName(contentList.getName());
                processResult.setExplicit(contentList.isExplicit());
                translet.setProcessResult(processResult);
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

    private ForwardRule response() throws ResponseException {
        if (!committed) {
            committed = true;
        } else {
            return null;
        }

        Response res = getResponse();
        if (res != null) {
            res.commit(this);
            if (isExceptionRaised()) {
                clearRaisedException();
            }
            if (res.getResponseType() == ResponseType.FORWARD) {
                ForwardResponse forwardResponse = (ForwardResponse)res;
                return forwardResponse.getForwardRule();
            }
        }
        return null;
    }

    private <V> V forward(ForwardRule forwardRule, InstantAction<V> instantAction)
            throws TransletNotFoundException, ActivityPrepareException, ActivityPerformException {
        if (logger.isDebugEnabled()) {
            logger.debug("Forwarding from " + translet.getRequestName() + " to " +
                    forwardRule.getTransletName());
        }

        reserveResponse(null);
        committed = false;

        prepare(forwardRule.getTransletName(), forwardRule.getRequestMethod());
        return perform(instantAction);
    }

    private void exception() throws ActionExecutionException {
        if (logger.isDebugEnabled()) {
            logger.debug("Raised exception " + getRaisedException(), getRaisedException());
        }

        reserveResponse(null);
        committed = false;

        if (translet != null) {
            if (getTransletRule().getExceptionRule() != null) {
                handleException(getTransletRule().getExceptionRule());
            }
        }
        if (getExceptionRuleList() != null) {
            handleException(getExceptionRuleList());
        }
    }

    protected void release() {
    }

    private void finish() {
        try {
            Scope requestScope = getRequestAdapter().getRequestScope(false);
            if (requestScope != null) {
                requestScope.destroy();
            }

            release();

            if (getResponseAdapter() != null) {
                getResponseAdapter().flush();
            }
        } catch (Exception e) {
            logger.error("An error was detected while finishing an activity", e);
        } finally {
            removeCurrentActivity();
        }
    }

    protected void execute(ActionList actionList) throws ActionExecutionException {
        execute(actionList, null);
    }

    protected void execute(ActionList actionList, ContentResult contentResult) throws ActionExecutionException {
        if (contentResult == null) {
            ProcessResult processResult = translet.getProcessResult();
            if (processResult == null) {
                processResult = new ProcessResult(2); // Consider adding one when an exception occurs
                translet.setProcessResult(processResult);
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
                logger.debug("Action " + action);
            }

            if (action.getActionType() == ActionType.CHOOSE) {
                Object resultValue = action.execute(this);
                if (resultValue != ActionResult.NO_RESULT) {
                    ChooseWhenRule chooseWhenRule = (ChooseWhenRule)resultValue;
                    ActionList actionList = chooseWhenRule.getActionList();
                    execute(actionList, contentResult);
                    if (chooseWhenRule.getResponse() != null) {
                        reserveResponse(chooseWhenRule.getResponse());
                    }
                }
            } else {
                Object resultValue = action.execute(this);
                if (!action.isHidden() && contentResult != null && resultValue != ActionResult.NO_RESULT) {
                    if (resultValue instanceof ProcessResult) {
                        contentResult.addActionResult(action, (ProcessResult)resultValue);
                    } else {
                        contentResult.addActionResult(action, resultValue);
                    }
                }
            }
        } catch (ActionExecutionException e) {
            logger.error("Failed to execute action " + action, e);
            throw e;
        } catch (Exception e) {
            setRaisedException(e);
            throw new ActionExecutionException("Failed to execute action " + action, e);
        }
    }

    @Override
    public ExceptionThrownRule handleException(ExceptionRule exceptionRule) throws ActionExecutionException {
        ExceptionThrownRule exceptionThrownRule = super.handleException(exceptionRule);
        if (translet != null && exceptionThrownRule != null && !isResponseReserved()) {
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
        Response res = this.reservedResponse;
        if (res == null && !isExceptionRaised()) {
            res = getDeclaredResponse();
        }
        return res;
    }

    protected void reserveResponse(@Nullable Response response) {
        this.reservedResponse = response;
        if (response != null && !isExceptionRaised()) {
            this.desiredResponse = response;
        }
    }

    protected void reserveResponse() {
        if (this.reservedResponse == null) {
            this.reservedResponse = getDeclaredResponse();
        }
    }

    @Override
    public boolean isResponseReserved() {
        return (this.reservedResponse != null);
    }

    protected Response getDesiredResponse() {
        return (this.desiredResponse != null ? this.desiredResponse : getDeclaredResponse());
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public Translet getTranslet() {
        return translet;
    }

    @Override
    public ProcessResult getProcessResult() {
        return (translet != null ? translet.getProcessResult() : null);
    }

    @Override
    public Object getProcessResult(String actionId) {
        if (translet != null) {
            return translet.getProcessResult().getResultValue(actionId);
        } else {
            return null;
        }
    }

    private TransletRule getTransletRule(String transletName, MethodType requestMethod) {
        return getActivityContext().getTransletRuleRegistry().getTransletRule(transletName, requestMethod);
    }

    /**
     * Returns the translet rule.
     * @return the translet rule
     */
    protected TransletRule getTransletRule() {
        return translet.getTransletRule();
    }

    /**
     * Returns the request rule.
     * @return the request rule
     */
    protected RequestRule getRequestRule() {
        return translet.getRequestRule();
    }

    /**
     * Returns the response rule.
     * @return the response rule
     */
    protected ResponseRule getResponseRule() {
        return translet.getResponseRule();
    }

    @Override
    public Response getDeclaredResponse() {
        return (getResponseRule() != null ? getResponseRule().getResponse() : null);
    }

    /**
     * Determines the default request encoding.
     * @return the default request encoding
     */
    protected String getIntendedRequestEncoding() {
        String encoding = getRequestRule().getEncoding();
        if (encoding == null) {
            encoding = getSetting(RequestRule.CHARACTER_ENCODING_SETTING_NAME);
        }
        return encoding;
    }

    /**
     * Determines the default response encoding.
     * @return the default response encoding
     */
    protected String getIntendedResponseEncoding() {
        String encoding = getResponseRule().getEncoding();
        if (encoding == null) {
            encoding = getIntendedRequestEncoding();
        }
        return encoding;
    }

    /**
     * Parses the declared parameters.
     */
    protected void parseDeclaredParameters() throws MissingMandatoryParametersException {
        ItemRuleMap itemRuleMap = getRequestRule().getParameterItemRuleMap();
        if (itemRuleMap != null && !itemRuleMap.isEmpty()) {
            ItemEvaluator evaluator = null;
            ItemRuleList missingItemRules = null;
            for (ItemRule itemRule : itemRuleMap.values()) {
                if (itemRule.isEvaluable()) {
                    if (evaluator == null) {
                        evaluator = new ItemEvaluation(this);
                    }
                    String[] oldValues = getRequestAdapter().getParameterValues(itemRule.getName());
                    String[] newValues = evaluator.evaluateAsStringArray(itemRule);
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
            ItemEvaluator evaluator = null;
            ItemRuleList missingItemRules = null;
            for (ItemRule itemRule : itemRuleMap.values()) {
                if (itemRule.isEvaluable()) {
                    if (evaluator == null) {
                        evaluator = new ItemEvaluation(this);
                    }
                    Object oldValue = getRequestAdapter().getAttribute(itemRule.getName());
                    Object newValue = evaluator.evaluate(itemRule);
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
            PathVariableMap pathVariables = PathVariableMap.parse(nameTokens, translet.getRequestName());
            if (pathVariables != null) {
                pathVariables.applyTo(translet);
            }
        }
    }

}
