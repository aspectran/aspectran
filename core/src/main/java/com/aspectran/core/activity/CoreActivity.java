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

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.MissingMandatoryParametersException;
import com.aspectran.core.activity.request.PathVariableMap;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.BasicSessionAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.bean.scope.Scope;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.BooleanExpression;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpression;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.ChooseRuleMap;
import com.aspectran.core.context.rule.ChooseWhenRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Core activity that handles all external requests.
 *
 * <p>This class is generally not thread-safe. It is primarily designed
 * for use in a single thread only.</p>
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class CoreActivity extends AdviceActivity {

    private static final Log log = LogFactory.getLog(CoreActivity.class);

    private CoreTranslet translet;

    private Response reservedResponse;

    private Response desiredResponse;

    private boolean committed;

    private Set<Integer> processedChooses;

    /**
     * Instantiates a new CoreActivity.
     *
     * @param context the activity context
     */
    protected CoreActivity(ActivityContext context) {
        super(context);
    }

    @Override
    public void prepare(String requestName) {
        TransletRule transletRule = getTransletRule(requestName, MethodType.GET);
        if (transletRule == null) {
            throw new TransletNotFoundException(requestName);
        }

        prepare(requestName, MethodType.GET, transletRule, null);
    }

    @Override
    public void prepare(TransletRule transletRule) {
        prepare(transletRule.getName(), transletRule);
    }

    @Override
    public void prepare(String requestName, TransletRule transletRule) {
        prepare(requestName, MethodType.GET, transletRule, null);
    }

    @Override
    public void prepare(String requestName, String requestMethod) {
        prepare(requestName, MethodType.resolve(requestMethod));
    }

    @Override
    public void prepare(String requestName, MethodType requestMethod) {
        prepare(requestName, requestMethod, null);
    }

    private void prepare(String requestName, MethodType requestMethod, Translet parentTranslet) {
        if (requestMethod == null) {
            requestMethod = MethodType.GET;
        }

        TransletRule transletRule = getTransletRule(requestName, requestMethod);
        if (transletRule == null) {
            throw new TransletNotFoundException(requestName);
        }

        prepare(requestName, requestMethod, transletRule, parentTranslet);
    }

    /**
     * Prepares a new activity for the Translet Rule by taking
     * the results of the process that was created earlier.
     *
     * @param requestName the request name
     * @param requestMethod the request method
     * @param transletRule the translet rule
     * @param parentTranslet the process result that was created earlier
     */
    private void prepare(String requestName, MethodType requestMethod, TransletRule transletRule,
                         Translet parentTranslet) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Translet " + transletRule);
            }

            newTranslet(requestMethod, requestName, transletRule, parentTranslet);

            if (parentTranslet == null) {
                if (isIncluded()) {
                    backupCurrentActivity();
                    saveCurrentActivity();
                } else {
                    saveCurrentActivity();
                }
                adapt();
            }

            prepareAspectAdviceRule(transletRule, (parentTranslet != null));
            parseRequest();
            parsePathVariables();

            if (parentTranslet == null) {
                resolveLocale();
            }
        } catch (ActivityTerminatedException e) {
            throw e;
        } catch (Exception e) {
            throw new ActivityPrepareException("Failed to prepare activity for translet " + transletRule, e);
        }
    }

    protected void adapt() throws AdapterException {
        SessionAdapter sessionAdapter = getSessionAdapter();
        if (sessionAdapter != null) {
            if (sessionAdapter instanceof BasicSessionAdapter) {
                ((BasicSessionAdapter)sessionAdapter).sessionAccess();
            }
        }
    }

    protected void release() {
        SessionAdapter sessionAdapter = getSessionAdapter();
        if (sessionAdapter != null) {
            if (sessionAdapter instanceof BasicSessionAdapter) {
                ((BasicSessionAdapter)sessionAdapter).sessionComplete();
            }
        }
    }

    @Override
    public void perform() {
        ForwardRule forwardRule = null;
        try {
            try {
                setCurrentAspectAdviceType(AspectAdviceType.BEFORE);
                executeAdvice(getBeforeAdviceRuleList(), true);

                if (!isResponseReserved()) {
                    produce();
                }

                forwardRule = response();
                if (forwardRule != null) {
                    forward(forwardRule);
                    return;
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
                response();

                if (isExceptionRaised()) {
                    throw getRootCauseOfRaisedException();
                }
            }

            setCurrentAspectAdviceType(null);
        } catch (ActivityTerminatedException e) {
            throw e;
        } catch (Throwable e) {
            throw new ActivityPerformException("Failed to perform the activity", e);
        } finally {
            if (forwardRule == null) {
                Scope requestScope = getRequestAdapter().getRequestScope(false);
                if (requestScope != null) {
                    requestScope.destroy();
                }
            }
        }
    }

    /**
     * Produce the result of the content and its subordinate actions.
     */
    private void produce() {
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

        Response res = getResponse();
        if (res != null) {
            ActionList actionList = res.getActionList();
            if (actionList != null) {
                execute(actionList);
            }
        }
    }

    private ForwardRule response() {
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

    private void forward(ForwardRule forwardRule) {
        if (log.isDebugEnabled()) {
            log.debug("Forwarding from [" + translet.getRequestName() + "] to [" +
                    forwardRule.getTransletName() + "]");
        }

        reserveResponse(null);
        committed = false;

        prepare(forwardRule.getTransletName(), forwardRule.getRequestMethod(), translet);
        perform();
    }

    private void exception() {
        reserveResponse(null);
        committed = false;

        if (getTransletRule().getExceptionRule() != null) {
            handleException(getTransletRule().getExceptionRule());
        }
        if (getExceptionRuleList() != null) {
            handleException(getExceptionRuleList());
        }
    }

    @Override
    public void finish() {
        try {
            release();

            if (getResponseAdapter() != null) {
                getResponseAdapter().flush();
            }
        } catch (Exception e) {
            log.error("An error was detected while finishing an activity", e);
        } finally {
            removeCurrentActivity();
        }
    }

    private Response getResponse() {
        Response res = this.reservedResponse;
        if (res == null && !isExceptionRaised()) {
            res = getDeclaredResponse();
        }
        return res;
    }

    protected void reserveResponse(Response response) {
        this.reservedResponse = response;

        if (response != null && !isExceptionRaised()) {
            this.desiredResponse = response;
        }
    }

    protected void reserveResponse() {
        if (this.reservedResponse != null) {
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

    /**
     * Determines the request encoding.
     *
     * @return the request encoding
     */
    protected String resolveRequestEncoding() {
        String encoding = getRequestRule().getEncoding();
        if (encoding == null) {
            encoding = getSetting(RequestRule.CHARACTER_ENCODING_SETTING_NAME);
        }
        return encoding;
    }

    /**
     * Determines the response encoding.
     *
     * @return the response encoding
     */
    protected String resolveResponseEncoding() {
        String encoding = getRequestRule().getEncoding();
        if (encoding == null) {
            encoding = resolveRequestEncoding();
        }
        return encoding;
    }

    /**
     * Resolve the current locale.
     *
     * @return the current locale
     */
    protected LocaleResolver resolveLocale() {
        LocaleResolver localeResolver = null;
        String localeResolverBeanId = getSetting(RequestRule.LOCALE_RESOLVER_SETTING_NAME);
        if (localeResolverBeanId != null) {
            localeResolver = getBean(localeResolverBeanId, LocaleResolver.class);
            localeResolver.resolveLocale(getTranslet());
            localeResolver.resolveTimeZone(getTranslet());
        }
        return localeResolver;
    }

    /**
     * Parses the declared parameters and attributes.
     */
    protected void parseRequest() {
        parseDeclaredParameters();
        parseDeclaredAttributes();
    }

    /**
     * Parses the declared parameters.
     */
    protected void parseDeclaredParameters() {
        ItemRuleMap parameterItemRuleMap = getRequestRule().getParameterItemRuleMap();
        if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
            ItemEvaluator evaluator = null;
            ItemRuleList missingItemRules = null;
            for (ItemRule itemRule : parameterItemRuleMap.values()) {
                Token[] tokens = itemRule.getTokens();
                if (tokens != null) {
                    if (evaluator == null) {
                        evaluator = new ItemExpression(this);
                    }
                    String[] values = evaluator.evaluateAsStringArray(itemRule);
                    String[] oldValues = getRequestAdapter().getParameterValues(itemRule.getName());
                    if (values != oldValues) {
                        getRequestAdapter().setParameter(itemRule.getName(), values);
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
     */
    protected void parseDeclaredAttributes() {
        ItemRuleMap attributeItemRuleMap = getRequestRule().getAttributeItemRuleMap();
        if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
            ItemEvaluator evaluator = new ItemExpression(this);
            for (ItemRule itemRule : attributeItemRuleMap.values()) {
                Object value = evaluator.evaluate(itemRule);
                getRequestAdapter().setAttribute(itemRule.getName(), value);
            }
        }
    }

    private void parsePathVariables() {
        Token[] nameTokens = getTransletRule().getNameTokens();
        if (nameTokens != null && !(nameTokens.length == 1 && nameTokens[0].getType() == TokenType.TEXT)) {
            PathVariableMap pathVariableMap = PathVariableMap.parse(nameTokens, translet.getRequestName());
            if (pathVariableMap != null) {
                pathVariableMap.applyTo(translet);
            }
        }
    }

    @Override
    public void handleException(ExceptionRule exceptionRule) {
        ExceptionThrownRule exceptionThrownRule = exceptionRule.getExceptionThrownRule(getRaisedException());
        if (exceptionThrownRule != null) {
            Executable action = exceptionThrownRule.getAction();
            if (action != null) {
                executeAdvice(action);
            }
            if (!isResponseReserved() && translet != null) {
                handleException(exceptionThrownRule);
            }
        }
    }

    private void handleException(ExceptionThrownRule exceptionThrownRule) {
        Response response = getDesiredResponse();
        String contentType = (response != null ? response.getContentType() : null);
        Response targetResponse = exceptionThrownRule.getResponse(contentType);
        if (targetResponse != null) {
            // Clear produced results. No reflection to ProcessResult.
            translet.setProcessResult(null);

            ActionList actionList = targetResponse.getActionList();
            if (actionList != null) {
                execute(actionList);
            }

            reserveResponse(targetResponse);
        }
    }

    /**
     * Execute actions.
     *
     * @param actionList the action list
     */
    protected void execute(ActionList actionList) {
        ProcessResult processResult = translet.getProcessResult();
        if (processResult == null) {
            processResult = new ProcessResult(1);
            translet.setProcessResult(processResult);
        }

        ContentResult contentResult = processResult.getContentResult(actionList.getName(), actionList.isExplicit());
        if (contentResult == null) {
            contentResult = new ContentResult(processResult, actionList.size());
            contentResult.setName(actionList.getName());
            if (!processResult.isExplicit()) {
                contentResult.setExplicit(actionList.isExplicit());
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
     * Execute action.
     *
     * @param action the executable action
     * @param contentResult the content result
     */
    private void execute(Executable action, ContentResult contentResult) {
        try {
            ChooseWhenRule chooseWhenRule = null;
            if (action.getCaseNo() > 0) {
                ChooseRuleMap chooseRuleMap = getTransletRule().getChooseRuleMap();
                if (chooseRuleMap == null || chooseRuleMap.isEmpty()) {
                    throw new IllegalRuleException("No defined choose rules");
                }
                ChooseRule chooseRule = chooseRuleMap.getChooseRule(action.getCaseNo());
                if (chooseRule == null) {
                    throw new IllegalRuleException("No choose rule with case number: " +
                            ChooseRule.toCaseGroupNo(action.getCaseNo()));
                }
                chooseWhenRule = chooseRule.getChooseWhenRule(action.getCaseNo());
                if (chooseWhenRule == null) {
                    throw new IllegalRuleException("No choose rule with case number: " + action.getCaseNo());
                }
                if (processedChooses != null && processedChooses.contains(chooseRule.getCaseNo())) {
                    return;
                }
                if (processedChooses == null || !processedChooses.contains(chooseWhenRule.getCaseNo())) {
                    BooleanExpression expression = new BooleanExpression(this);
                    if (expression.evaluate(chooseWhenRule)) {
                        if (processedChooses == null) {
                            processedChooses = new HashSet<>();
                        }
                        processedChooses.add(chooseRule.getCaseNo());
                        processedChooses.add(chooseWhenRule.getCaseNo());
                    } else {
                        return;
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Action " + action);
            }

            Object resultValue = action.execute(this);
            if (!action.isHidden() && contentResult != null && resultValue != ActionResult.NO_RESULT) {
                if (resultValue instanceof ProcessResult) {
                    contentResult.addActionResult(action, (ProcessResult) resultValue);
                } else {
                    contentResult.addActionResult(action, resultValue);
                }
            }

            if (action.isLastInChooseWhen() && chooseWhenRule != null && chooseWhenRule.getResponse() != null) {
                reserveResponse(chooseWhenRule.getResponse());
            }
        } catch (ActionExecutionException e) {
            throw e;
        } catch (Exception e) {
            setRaisedException(e);
            throw new ActionExecutionException("Failed to execute action " + action, e);
        }
    }

    @Override
    public <T extends Activity> T newActivity() {
        throw new UnsupportedOperationException("newActivity");
    }

    /**
     * Create a new {@code CoreTranslet} instance.
     *
     * @param requestMethod the request method
     * @param requestName the request name
     * @param transletRule the translet rule
     * @param parentTranslet the process result that was created earlier
     */
    private void newTranslet(MethodType requestMethod, String requestName, TransletRule transletRule,
                             Translet parentTranslet) {
        translet = new CoreTranslet(transletRule, this);
        translet.setRequestName(requestName);
        translet.setRequestMethod(requestMethod);
        if (parentTranslet != null) {
            translet.setProcessResult(parentTranslet.getProcessResult());
        }
    }

    @Override
    public Translet getTranslet() {
        return translet;
    }

    @Override
    public ProcessResult getProcessResult() {
        return translet.getProcessResult();
    }

    @Override
    public Object getProcessResult(String actionId) {
        return translet.getProcessResult().getResultValue(actionId);
    }

    private TransletRule getTransletRule(String transletName, MethodType requestMethod) {
        return getActivityContext().getTransletRuleRegistry().getTransletRule(transletName, requestMethod);
    }

    /**
     * Returns the translet rule.
     *
     * @return the translet rule
     */
    protected TransletRule getTransletRule() {
        return translet.getTransletRule();
    }

    /**
     * Returns the request rule.
     *
     * @return the request rule
     */
    protected RequestRule getRequestRule() {
        return translet.getRequestRule();
    }

    /**
     * Returns the response rule.
     *
     * @return the response rule
     */
    protected ResponseRule getResponseRule() {
        return translet.getResponseRule();
    }

    @Override
    public Response getDeclaredResponse() {
        return (getResponseRule() != null ? getResponseRule().getResponse() : null);
    }

}
