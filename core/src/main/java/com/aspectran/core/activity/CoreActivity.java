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

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.MissingMandatoryParametersException;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.BasicSessionAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.bean.scope.Scope;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.component.translet.TransletInstantiationException;
import com.aspectran.core.component.translet.TransletNotFoundException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.lang.reflect.Constructor;

/**
 * The Class CoreActivity.
 *
 * <p>This class is generally not thread-safe. It is primarily designed for use in a single thread only.</p>
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class CoreActivity extends BasicActivity {

    private static final Log log = LogFactory.getLog(CoreActivity.class);

    private TransletRule transletRule;

    private RequestRule requestRule;

    private ResponseRule responseRule;

    private Class<? extends Translet> transletInterfaceClass;

    private Class<? extends CoreTranslet> transletImplementationClass;

    private Translet translet;

    private String transletName;

    private MethodType requestMethod;

    private String forwardTransletName;

    private boolean withoutResponse;

    private Response reservedResponse;

    private volatile boolean committed;

    /**
     * Instantiates a new CoreActivity.
     *
     * @param context the activity context
     */
    public CoreActivity(ActivityContext context) {
        super(context);
    }

    @Override
    public void prepare(String transletName) {
        this.transletName = transletName;
        this.requestMethod = MethodType.GET;

        TransletRule transletRule = getTransletRuleRegistry().getTransletRule(transletName);
        if (transletRule == null) {
            throw new TransletNotFoundException(transletName);
        }

        prepare(transletRule, null);
    }

    @Override
    public void prepare(TransletRule transletRule) {
        this.transletName = transletRule.getName();
        this.requestMethod = MethodType.GET;

        prepare(transletRule, null);
    }


    @Override
    public void prepare(String transletName, TransletRule transletRule) {
        this.transletName = transletName;
        this.requestMethod = MethodType.GET;

        prepare(transletRule, null);
    }

    @Override
    public void prepare(String transletName, String requestMethod) {
        prepare(transletName, MethodType.resolve(requestMethod));
    }

    @Override
    public void prepare(String transletName, MethodType requestMethod) {
        prepare(transletName, requestMethod, null);
    }

    private void prepare(String transletName, MethodType requestMethod, ProcessResult processResult) {
        this.transletName = transletName;
        this.requestMethod = (requestMethod == null ? MethodType.GET : requestMethod);

        TransletRule transletRule = getTransletRuleRegistry().getTransletRule(this.transletName, this.requestMethod);
        if (transletRule == null) {
            throw new TransletNotFoundException(transletName);
        }

        prepare(transletRule, processResult);
    }

    /**
     * Prepare activity for Translet with process result.
     *
     * @param transletRule the translet rule
     * @param processResult the process result
     */
    private void prepare(TransletRule transletRule, ProcessResult processResult) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("translet " + transletRule);
            }

            if (transletRule.getTransletInterfaceClass() != null) {
                setTransletInterfaceClass(transletRule.getTransletInterfaceClass());
            }
            if (transletRule.getTransletImplementationClass() != null) {
                setTransletImplementationClass(transletRule.getTransletImplementationClass());
            }

            translet = newTranslet(this, transletRule);
            if (processResult != null) {
                translet.setProcessResult(processResult);
            }

            if (forwardTransletName == null) {
                if (isIncluded()) {
                    backupCurrentActivity();
                } else {
                    saveCurrentActivity();
                }
                adapt();
            }

            prepareAspectAdviceRule(transletRule);
            parseRequest();
            parsePathVariable();

            if (forwardTransletName == null) {
                resolveLocale();
            }
        } catch (ActivityTerminatedException e) {
            throw e;
        } catch (Exception e) {
            throw new ActivityException("Failed to prepare activity for Translet " + transletRule, e);
        }
    }

    protected void adapt() throws AdapterException {
        SessionAdapter sessionAdapter = getSessionAdapter();
        if (sessionAdapter != null) {
            if (sessionAdapter instanceof BasicSessionAdapter) {
                SessionAgent agent = ((BasicSessionAdapter)sessionAdapter).getSessionAgent();
                agent.access();
            }
        }
    }

    protected void release() {
        SessionAdapter sessionAdapter = getSessionAdapter();
        if (sessionAdapter != null) {
            if (sessionAdapter instanceof BasicSessionAdapter) {
                SessionAgent agent = ((BasicSessionAdapter)sessionAdapter).getSessionAgent();
                agent.complete();
            }
        }
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

    @Override
    public void perform() {
        performActivity();
    }

    @Override
    public void performWithoutResponse() {
        withoutResponse = true;
        performActivity();
    }

    @Override
    public void finish() {
        try {
            release();

            if (getResponseAdapter() != null) {
                getResponseAdapter().flush();
            }
        } catch (Exception e) {
            log.error("Failed to finish activity", e);
        } finally {
            removeCurrentActivity();
        }
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
        ItemRuleMap parameterItemRuleMap = requestRule.getParameterItemRuleMap();
        if (parameterItemRuleMap != null) {
            ItemEvaluator evaluator = null;
            ItemRuleList missingItemRules = null;
            for (ItemRule itemRule : parameterItemRuleMap.values()) {
                Token[] tokens = itemRule.getTokens();
                if (tokens != null) {
                    if (evaluator == null) {
                        evaluator = new ItemExpressionParser(this);
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
        ItemRuleMap attributeItemRuleMap = requestRule.getAttributeItemRuleMap();
        if (attributeItemRuleMap != null) {
            ItemEvaluator evaluator = new ItemExpressionParser(this);
            for (ItemRule itemRule : attributeItemRuleMap.values()) {
                Object value = evaluator.evaluate(itemRule);
                getRequestAdapter().setAttribute(itemRule.getName(), value);
            }
        }
    }

    private void parsePathVariable() {
        Token[] nameTokens = transletRule.getNameTokens();
        if (nameTokens != null && !(nameTokens.length == 1 && nameTokens[0].getType() == TokenType.TEXT)) {
            PathVariableMap pathVariableMap = PathVariableMap.newInstance(nameTokens, transletName);
            pathVariableMap.apply(translet);
        }
    }

    /**
     * Perform core activity for Translet.
     */
    private void performActivity() {
        try {
            try {
                // execute the Before Advice Action for Translet Joinpoint
                if (getBeforeAdviceRuleList() != null) {
                    executeAdvice(getBeforeAdviceRuleList());
                }

                if (!isResponseReserved()) {
                    if (transletRule.getContentList() != null) {
                        produce();
                    }
                }

                // execute the After Advice Action for Translet Joinpoint
                if (getAfterAdviceRuleList() != null) {
                    executeAdvice(getAfterAdviceRuleList());
                }
            } catch (Exception e) {
                setRaisedException(e);
            } finally {
                if (getFinallyAdviceRuleList() != null) {
                    executeAdviceWithoutThrow(getFinallyAdviceRuleList());
                }
            }

            if (isExceptionRaised()) {
                reserveResponse(null);
                committed = false;

                if (transletRule.getExceptionRule() != null) {
                    handleException(transletRule.getExceptionRule());
                }
                if (getExceptionRuleList() != null) {
                    handleException(getExceptionRuleList());
                }
            }

            if (!withoutResponse) {
                response();
            }
        } catch (Exception e) {
            throw new ActivityException("An error occurred while performing the activity", e);
        } finally {
            Scope requestScope = getRequestAdapter().getRequestScope(false);
            if (requestScope != null) {
                requestScope.destroy();
            }
        }
    }

    /**
     * Produce the result of the content and its subordinate actions.
     */
    private void produce() {
        ContentList contentList = transletRule.getContentList();
        if (contentList != null) {
            ProcessResult processResult = translet.touchProcessResult(contentList.getName(), contentList.size());
            if (transletRule.isExplicitContent()) {
                processResult.setOmittable(contentList.isOmittable());
            } else {
                if (contentList.getVisibleCount() < 2) {
                    processResult.setOmittable(true);
                }
            }
            for (ActionList actionList : contentList) {
                execute(actionList);
                if (isResponseReserved()) {
                    break;
                }
            }
        }
    }

    private void response() {
        if (!committed) {
            committed = true;
        } else {
            return;
        }

        Response res = (this.reservedResponse != null ? this.reservedResponse : getDeclaredResponse());
        if (res != null) {
            res.commit(this);

            if (res.getResponseType() == ResponseType.FORWARD) {
                ForwardResponse forwardResponse = (ForwardResponse)res;
                this.forwardTransletName = forwardResponse.getForwardResponseRule().getTransletName();
            } else {
                this.forwardTransletName = null;
            }
            if (forwardTransletName != null) {
                forward();
            }
        }
    }

    @Override
    public Response getDeclaredResponse() {
        return (responseRule != null ? responseRule.getResponse() : null);
    }

    /**
     * Responds immediately, and the remaining jobs will be canceled.
     *
     * @param response the response
     */
    protected void reserveResponse(Response response) {
        this.reservedResponse = response;
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

    /**
     * Forwarding from current translet to other translet.
     */
    private void forward() {
        if (log.isDebugEnabled()) {
            log.debug("Forwarding from [" + transletName + "] to [" + forwardTransletName + "]");
        }

        reserveResponse(null);
        committed = false;

        prepare(forwardTransletName, requestMethod, translet.getProcessResult());
        perform();
    }

    @Override
    public void handleException(ExceptionRule exceptionRule) {
        if (log.isDebugEnabled()) {
            log.debug("Handling the Exception Raised: " + getRootCauseOfRaisedException());
        }

        ExceptionThrownRule exceptionThrownRule = exceptionRule.getExceptionThrownRule(getRaisedException());
        if (exceptionThrownRule != null) {
            Executable action = exceptionThrownRule.getExecutableAction();
            if (action != null) {
                executeAdvice(action);
            }
            if (!isResponseReserved() && translet != null) {
                handleException(exceptionThrownRule);
            }
        }
    }

    private void handleException(ExceptionThrownRule exceptionThrownRule) {
        Response response = getDeclaredResponse();
        Response targetResponse;
        if (response != null && response.getContentType() != null) {
            targetResponse = exceptionThrownRule.getResponse(response.getContentType());
        } else {
            targetResponse = exceptionThrownRule.getDefaultResponse();
        }
        if (targetResponse != null) {
            ResponseRule newResponseRule = new ResponseRule();
            newResponseRule.setResponse(targetResponse);
            if (this.responseRule != null) {
                newResponseRule.setEncoding(this.responseRule.getEncoding());
            }
            setResponseRule(newResponseRule);

            if (log.isDebugEnabled()) {
                log.debug("Response by Content Type " + newResponseRule);
            }

            // Clear produced results. No reflection to ProcessResult.
            translet.setProcessResult(null);
            translet.touchProcessResult(null, 0).setOmittable(true);

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
        ContentResult contentResult = null;
        if (translet.getProcessResult() != null) {
            contentResult = new ContentResult(translet.getProcessResult(), actionList.size());
            contentResult.setName(actionList.getName());
            if (transletRule.isExplicitContent()) {
                contentResult.setOmittable(actionList.isOmittable());
            } else if (actionList.getName() == null && actionList.getVisibleCount() < 2) {
                contentResult.setOmittable(true);
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
        if (log.isDebugEnabled()) {
            log.debug("action " + action);
        }

        try {
            Object resultValue = action.execute(this);
            if (contentResult != null && resultValue != ActionResult.NO_RESULT) {
                ActionResult actionResult = new ActionResult(contentResult);
                actionResult.setActionId(action.getActionId());
                actionResult.setResultValue(resultValue);
                actionResult.setHidden(action.isHidden());
            }

            if (log.isTraceEnabled()) {
                log.trace("actionResult " + resultValue);
            }
        } catch (Exception e) {
            setRaisedException(e);
            throw new ActionExecutionException("Failed to execute action " + action, e);
        }
    }

    /**
     * Create a new {@code Translet} instance.
     *
     * @param activity the core activity
     * @param transletRule the translet rule
     * @return the new {@code Translet} instance
     */
    private Translet newTranslet(CoreActivity activity, TransletRule transletRule) {
        if (transletRule != null) {
            this.transletRule = transletRule;
            this.requestRule = transletRule.getRequestRule();
            this.responseRule = transletRule.getResponseRule();
        }

        if (this.transletInterfaceClass == null) {
            this.transletInterfaceClass = Translet.class;
        }
        if (this.transletImplementationClass == null) {
            this.transletImplementationClass = CoreTranslet.class;
            return new CoreTranslet(activity);
        }

        // create a custom translet instance
        try {
            Class<?>[] types = new Class<?>[] { CoreActivity.class };
            Object[] args = new Object[] { activity };
            Constructor<?> transletImplementCtor = transletImplementationClass.getConstructor(types);
            return (CoreTranslet)transletImplementCtor.newInstance(args);
        } catch (Exception e) {
            throw new TransletInstantiationException(transletInterfaceClass, transletImplementationClass, e);
        }
    }

    /**
     * Returns an interface class for the {@code Translet}.
     *
     * @return the interface class for the translet
     */
    protected Class<? extends Translet> getTransletInterfaceClass() {
        return transletInterfaceClass;
    }

    /**
     * Sets the translet interface class.
     *
     * @param transletInterfaceClass the new translet interface class
     */
    protected void setTransletInterfaceClass(Class<? extends Translet> transletInterfaceClass) {
        this.transletInterfaceClass = transletInterfaceClass;
    }

    /**
     * Returns an implementation class for the {@code Translet}.
     *
     * @return the implementation class for the translet
     */
    protected Class<? extends CoreTranslet> getTransletImplementationClass() {
        return transletImplementationClass;
    }

    /**
     * Sets the translet implement class.
     *
     * @param transletImplementationClass the new translet implementation class
     */
    protected void setTransletImplementationClass(Class<? extends CoreTranslet> transletImplementationClass) {
        this.transletImplementationClass = transletImplementationClass;
    }

    /**
     * Returns the translet rule.
     *
     * @return the translet rule
     */
    protected TransletRule getTransletRule() {
        return transletRule;
    }

    /**
     * Returns the request rule.
     *
     * @return the request rule
     */
    protected RequestRule getRequestRule() {
        return requestRule;
    }

    /**
     * Returns the response rule.
     *
     * @return the response rule
     */
    protected ResponseRule getResponseRule() {
        return responseRule;
    }

    /**
     * Replace the response rule.
     *
     * @param responseRule the response rule
     */
    protected void setResponseRule(ResponseRule responseRule) {
        this.responseRule = responseRule;
    }

    /**
     * Determines the request encoding.
     *
     * @return the request encoding
     */
    protected String resolveRequestEncoding() {
        String encoding = requestRule.getEncoding();
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
        String encoding = requestRule.getEncoding();
        if (encoding == null) {
            encoding = resolveRequestEncoding();
        }
        return encoding;
    }

    @Override
    public <T extends Activity> T newActivity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTransletName() {
        return transletName;
    }

    @Override
    public MethodType getRequestMethod() {
        return requestMethod;
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

}
