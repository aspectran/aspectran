/*
 * Copyright 2008-2017 Juho Jeong
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

import java.io.IOException;
import java.lang.reflect.Constructor;

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
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class CoreActivity.
 *
 * <p>This class is generally not thread-safe. It is primarily designed for use in a single thread only.
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

        TransletRule transletRule = getTransletRuleRegistry().getTransletRule(transletName);

        // for RESTful
        if (transletRule == null && requestMethod != null) {
            transletRule = getTransletRuleRegistry().getRestfulTransletRule(transletName, requestMethod);
        }

        if (transletRule == null) {
            throw new TransletNotFoundException(transletName);
        }

        // for RESTful
        PathVariableMap pathVariableMap = getTransletRuleRegistry().getPathVariableMap(transletRule, transletName);

        prepare(transletRule, processResult);

        if (pathVariableMap != null) {
            pathVariableMap.apply(translet);
        }
    }

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

            if (forwardTransletName == null) {
                resolveLocale();
            }
        } catch (ActivityTerminatedException e) {
            throw e;
        } catch (Exception e) {
            throw new ActivityException("Failed to prepare activity", e);
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
        performTranslet();
    }

    @Override
    public void performWithoutResponse() {
        withoutResponse = true;
        performTranslet();
    }

    @Override
    public void finish() {
        try {
            release();
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

    /**
     * Performs an activity.
     */
    private void performTranslet() {
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
            throw new ActivityException("Activity failed to perform", e);
        } finally {
            Scope requestScope = getRequestAdapter().getRequestScope(false);
            if (requestScope != null) {
                requestScope.destroy();
            }
        }
    }

    /**
     * Produces content.
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

    protected Response getDeclaredResponse() {
        return (responseRule != null ? responseRule.getResponse() : null);
    }

    private void response() throws IOException {
        Response res = (this.reservedResponse != null) ? this.reservedResponse : getDeclaredResponse();

        if (res != null) {
            if (res.getResponseType() != ResponseType.FORWARD) {
                getResponseAdapter().flush();
            }

            res.respond(this);

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

        prepare(forwardTransletName, requestMethod, translet.getProcessResult());
        perform();
    }

    @Override
    public void handleException(ExceptionRule exceptionRule) {
        if (log.isDebugEnabled()) {
            log.debug("Exception handling for raised exception: " + getOriginRaisedException());
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
                newResponseRule.setCharacterEncoding(this.responseRule.getCharacterEncoding());
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

        //create a custom translet instance
        try {
            Constructor<?> transletImplementConstructor = transletImplementationClass.getConstructor(CoreActivity.class);
            Object[] args = new Object[] { activity };

            return (CoreTranslet)transletImplementConstructor.newInstance(args);
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
     * Determine the request character encoding.
     *
     * @return the request character encoding
     */
    protected String resolveRequestCharacterEncoding() {
        String characterEncoding = requestRule.getCharacterEncoding();
        if (characterEncoding == null) {
            characterEncoding = getSetting(RequestRule.CHARACTER_ENCODING_SETTING_NAME);
        }
        return characterEncoding;
    }

    /**
     * Determine the response character encoding.
     *
     * @return the response character encoding
     */
    protected String resolveResponseCharacterEncoding() {
        String characterEncoding = requestRule.getCharacterEncoding();
        if (characterEncoding == null) {
            characterEncoding = resolveRequestCharacterEncoding();
        }
        return characterEncoding;
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
