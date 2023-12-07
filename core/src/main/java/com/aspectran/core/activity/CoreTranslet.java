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

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.CustomTransformResponse;
import com.aspectran.core.activity.response.transform.CustomTransformer;
import com.aspectran.core.activity.response.transform.TransformResponseFactory;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.expr.TokenEvaluation;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.support.i18n.message.NoSuchMessageException;
import com.aspectran.core.util.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * The Class CoreTranslet.
 *
 * <p>This class is generally not thread-safe.
 * It is primarily designed for use in a single thread only.</p>
 */
public class CoreTranslet extends AbstractTranslet {

    private final CoreActivity activity;

    private ProcessResult processResult;

    private ActivityData activityData;

    private TokenEvaluator evaluator;

    /**
     * Instantiates a new CoreTranslet.
     * @param transletRule the translet rule
     * @param activity the current Activity
     */
    public CoreTranslet(TransletRule transletRule, CoreActivity activity) {
        super(transletRule);
        this.activity = activity;
    }

    @Override
    public String getDescription() {
        DescriptionRule descriptionRule = activity.getTransletRule().getDescriptionRule();
        if (descriptionRule != null) {
            return DescriptionRule.render(descriptionRule, activity);
        } else {
            return null;
        }
    }

    @Override
    public Environment getEnvironment() {
        return activity.getEnvironment();
    }

    @Override
    public boolean acceptsProfiles(String... profiles) {
        return getEnvironment().acceptsProfiles(profiles);
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return activity.getApplicationAdapter();
    }

    @Override
    public SessionAdapter getSessionAdapter() {
        return activity.getSessionAdapter();
    }

    @Override
    public RequestAdapter getRequestAdapter() {
        return activity.getRequestAdapter();
    }

    @Override
    public ResponseAdapter getResponseAdapter() {
        return activity.getResponseAdapter();
    }

    @Override
    public <V> V getSessionAdaptee() {
        SessionAdapter sessionAdapter = getSessionAdapter();
        return (sessionAdapter != null ? sessionAdapter.getAdaptee() : null);
    }

    @Override
    public <V> V getRequestAdaptee() {
        return getRequestAdapter().getAdaptee();
    }

    @Override
    public <V> V getResponseAdaptee() {
        return getResponseAdapter().getAdaptee();
    }

    @Override
    public String getIntendedRequestEncoding() {
        return activity.getIntendedRequestEncoding();
    }

    @Override
    public String getIntendedResponseEncoding() {
        return activity.getIntendedResponseEncoding();
    }

    @Override
    public ProcessResult getProcessResult() {
        return processResult;
    }

    @Override
    public Object getProcessResult(String actionId) {
        return (processResult != null ? processResult.getResultValue(actionId) : null);
    }

    @Override
    public void setProcessResult(ProcessResult processResult) {
        this.processResult = processResult;
    }

    @Override
    public ActivityData getActivityData() {
        if (activityData == null) {
            activityData = new ActivityData(activity);
        } else {
            activityData.refresh();
        }
        return activityData;
    }

    @Override
    public <V> V getSetting(String settingName) {
        return activity.getSetting(settingName);
    }

    @Override
    public <V> V getProperty(String name) {
        return getEnvironment().getProperty(name, activity);
    }

    @Override
    public String getParameter(String name) {
        return getRequestAdapter().getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        return getRequestAdapter().getParameterValues(name);
    }

    @Override
    public Collection<String> getParameterNames() {
        return getRequestAdapter().getParameterNames();
    }

    @Override
    public void setParameter(String name, String value) {
        getRequestAdapter().setParameter(name, value);
    }

    @Override
    public void setParameter(String name, String[] values) {
        getRequestAdapter().setParameter(name, values);
    }

    @Override
    public Map<String, Object> getAllParameters() {
        return getRequestAdapter().getAllParameters();
    }

    @Override
    public void extractParameters(Map<String, Object> targetParameters) {
        getRequestAdapter().extractParameters(targetParameters);
    }

    @Override
    public FileParameter getFileParameter(String name) {
        return getRequestAdapter().getFileParameter(name);
    }

    @Override
    public FileParameter[] getFileParameterValues(String name) {
        return getRequestAdapter().getFileParameterValues(name);
    }

    @Override
    public Collection<String> getFileParameterNames() {
        return getRequestAdapter().getFileParameterNames();
    }

    @Override
    public void setFileParameter(String name, FileParameter fileParameter) {
        getRequestAdapter().setFileParameter(name, fileParameter);
    }

    @Override
    public void setFileParameter(String name, FileParameter[] fileParameters) {
        getRequestAdapter().setFileParameter(name, fileParameters);
    }

    @Override
    public void removeFileParameter(String name) {
        getRequestAdapter().removeFileParameter(name);
    }

    @Override
    public <V> V getAttribute(String name) {
        return getRequestAdapter().getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        getRequestAdapter().setAttribute(name, value);
    }

    @Override
    public Collection<String> getAttributeNames() {
        return getRequestAdapter().getAttributeNames();
    }

    @Override
    public void removeAttribute(String name) {
        getRequestAdapter().removeAttribute(name);
    }

    @Override
    public void transform(TransformRule transformRule) {
        if (transformRule == null) {
            throw new IllegalArgumentException("transformRule must not be null");
        }
        Response res = TransformResponseFactory.create(transformRule);
        response(res);
    }

    @Override
    public void transform(CustomTransformer transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("transformer must not be null");
        }
        Response res = new CustomTransformResponse(transformer);
        response(res);
    }

    @Override
    public void dispatch(String name) {
        dispatch(name, null);
    }

    @Override
    public void dispatch(String name, String dispatcherName) {
        DispatchRule dispatchRule = new DispatchRule();
        dispatchRule.setName(name, null);
        dispatchRule.setDispatcherName(dispatcherName);
        dispatch(dispatchRule);
    }

    @Override
    public void dispatch(DispatchRule dispatchRule) {
        if (dispatchRule == null) {
            throw new IllegalArgumentException("transformRule must not be null");
        }
        Response res = new DispatchResponse(dispatchRule);
        response(res);
    }

    @Override
    public void forward(String transletName) {
        ForwardRule forwardRule = new ForwardRule();
        forwardRule.setTransletName(transletName);
        forward(forwardRule);
    }

    @Override
    public void forward(ForwardRule forwardRule) {
        if (forwardRule == null) {
            throw new IllegalArgumentException("forwardRule must not be null");
        }
        if (forwardRule.getTransletName() == null) {
            forwardRule.setTransletName(StringUtils.EMPTY);
        }
        Response res = new ForwardResponse(forwardRule);
        response(res);
    }

    @Override
    public void redirect(String path) {
        redirect(path, null);
    }

    @Override
    public void redirect(String path, Map<String, String> parameters) {
        RedirectRule redirectRule = new RedirectRule();
        redirectRule.setPath(path);
        redirectRule.setParameters(parameters);
        redirect(redirectRule);
    }

    @Override
    public void redirect(RedirectRule redirectRule) {
        if (redirectRule == null) {
            throw new IllegalArgumentException("redirectRule must not be null");
        }
        Response res = new RedirectResponse(redirectRule);
        response(res);
    }

    @Override
    public void response(Response response) {
        activity.reserveResponse(response);
    }

    @Override
    public void response() {
        activity.reserveResponse();
    }

    @Override
    public Response getDeclaredResponse() {
        return activity.getDeclaredResponse();
    }

    @Override
    public boolean isResponseReserved() {
        return activity.isResponseReserved();
    }

    @Override
    public boolean isExceptionRaised() {
        return activity.isExceptionRaised();
    }

    @Override
    public Throwable getRaisedException() {
        return activity.getRaisedException();
    }

    @Override
    public void removeRaisedException() {
        activity.clearRaisedException();
    }

    @Override
    public Throwable getRootCauseOfRaisedException() {
        return activity.getRootCauseOfRaisedException();
    }

    @Override
    public <V> V getAspectAdviceBean(String aspectId) {
        return activity.getAspectAdviceBean(aspectId);
    }

    @Override
    public <V> V getBeforeAdviceResult(String aspectId) {
        return activity.getBeforeAdviceResult(aspectId);
    }

    @Override
    public <V> V getAfterAdviceResult(String aspectId) {
        return activity.getAfterAdviceResult(aspectId);
    }

    @Override
    public <V> V getAroundAdviceResult(String aspectId) {
        return activity.getAroundAdviceResult(aspectId);
    }

    @Override
    public <V> V getFinallyAdviceResult(String aspectId) {
        return activity.getFinallyAdviceResult(aspectId);
    }

    @Override
    public boolean hasPathVariables() {
        return activity.getTransletRule().hasPathVariables();
    }

    @Override
    public String toString() {
        if (getResponseAdapter() != null && getResponseAdapter().getAdaptee() == null) {
            try {
                return getResponseAdapter().getWriter().toString();
            } catch (IOException e) {
                // ignore
            }
        }
        return super.toString();
    }

    //---------------------------------------------------------------------
    // Implementation for token expression evaluation
    //---------------------------------------------------------------------

    @Override
    public <V> V evaluate(String expression) {
        Token[] tokens = TokenParser.parse(expression);
        return evaluate(tokens);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V evaluate(Token[] tokens) {
        if (evaluator == null) {
            evaluator = new TokenEvaluation(activity);
        }
        return (V)evaluator.evaluate(tokens);
    }

    //---------------------------------------------------------------------
    // Implementation of BeanRegistry interface
    //---------------------------------------------------------------------

    @Override
    public <V> V getBean(String id) {
        return activity.getBean(id);
    }

    @Override
    public <V> V getBean(Class<V> type) {
        return activity.getBean(type);
    }

    @Override
    public <V> V getBean(Class<V> type, String id) {
        return activity.getBean(type, id);
    }

    @Override
    public boolean containsBean(String id) {
        return activity.containsBean(id);
    }

    @Override
    public boolean containsBean(Class<?> type) {
        return activity.containsBean(type);
    }

    @Override
    public boolean containsBean(Class<?> type, String id) {
        return activity.containsBean(type, id);
    }

    //---------------------------------------------------------------------
    // Implementation of MessageSource interface
    //---------------------------------------------------------------------

    @Override
    public String getMessage(String code) throws NoSuchMessageException {
        return getMessage(code, (Object[])null);
    }

    @Override
    public String getMessage(String code, Object[] args) throws NoSuchMessageException {
        return getMessage(code, args, getRequestAdapter().getLocale());
    }

    @Override
    public String getMessage(String code, String defaultMessage) {
        return getMessage(code, null, defaultMessage);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage) {
        return getMessage(code, args, defaultMessage, getRequestAdapter().getLocale());
    }

    @Override
    public String getMessage(String code, Locale locale) throws NoSuchMessageException {
        return getMessage(code, (Object[])null, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return activity.getActivityContext().getMessageSource().getMessage(code, args, locale);
    }

    @Override
    public String getMessage(String code, String defaultMessage, Locale locale) {
        return getMessage(code, null, defaultMessage, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return activity.getActivityContext().getMessageSource().getMessage(code, args, defaultMessage, locale);
    }

}
