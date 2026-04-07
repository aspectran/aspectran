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

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.transform.CustomTransformer;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.TokenParser;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.support.i18n.message.NoSuchMessageException;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * A stateless {@link Translet} implementation for use in non-translet environments,
 * such as when executing an {@link InstantAction} or within a {@link ProxyActivity}.
 * <p>This implementation delegates most of its operations to the underlying {@link Activity}
 * and is intended to provide a compatible interface for components (like AOP advice)
 * that expect a {@code Translet} instance even when one is not naturally available.</p>
 *
 * <p>Created: 2026. 04. 03.</p>
 */
public class InstantTranslet implements Translet {

    private static final String RESPONSE_ACTION_NOT_SUPPORTED =
            "Response-related actions are not supported in a non-translet environment";

    private static final String PROCESS_RESULT_SETTING_NOT_SUPPORTED =
            "Setting process result is not supported in a non-translet environment";

    private final Activity activity;

    public InstantTranslet(@NonNull Activity activity) {
        this.activity = activity;
    }

    @Override
    public Activity.Mode getMode() {
        return activity.getMode();
    }

    @Override
    public String getContextPath() {
        return activity.getContextPath();
    }

    @Override
    public String getRequestName() {
        return null;
    }

    @Override
    public String getActualRequestName() {
        return null;
    }

    @Override
    public MethodType getRequestMethod() {
        return null;
    }

    @Override
    public String getTransletName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Environment getEnvironment() {
        return activity.getActivityContext().getEnvironment();
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return activity.getActivityContext().getApplicationAdapter();
    }

    @Override
    public boolean hasSessionAdapter() {
        return activity.hasSessionAdapter();
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
        return (hasSessionAdapter() ? activity.getSessionAdapter().getAdaptee() : null);
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
    public String getDefinitiveRequestEncoding() {
        return null;
    }

    @Override
    public String getDefinitiveResponseEncoding() {
        return null;
    }

    @Override
    public ProcessResult getProcessResult() {
        return null;
    }

    @Override
    public Object getProcessResult(String actionId) {
        return null;
    }

    @Override
    public void setProcessResult(ProcessResult processResult) {
        throw new UnsupportedOperationException(PROCESS_RESULT_SETTING_NOT_SUPPORTED);
    }

    @Override
    public ActivityData getActivityData() {
        return activity.getActivityData();
    }

    @Override
    public <V> V getSetting(String settingName) {
        return activity.getSetting(settingName);
    }

    @Override
    public <V> V getProperty(String name) {
        return activity.getActivityContext().getEnvironment().getProperty(name);
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
    public boolean hasInputFlashMap() {
        return false;
    }

    @Override
    public Map<String, ?> getInputFlashMap() {
        return Collections.emptyMap();
    }

    @Override
    public boolean hasOutputFlashMap() {
        return false;
    }

    @Override
    public FlashMap getOutputFlashMap() {
        return null;
    }

    @Override
    public HintParameters peekHint(String type) {
        return activity.peekHint(type);
    }

    @Override
    public void transform(TransformRule transformRule) {
        throw new UnsupportedOperationException(RESPONSE_ACTION_NOT_SUPPORTED);
    }

    @Override
    public void transform(CustomTransformer transformer) {
        throw new UnsupportedOperationException(RESPONSE_ACTION_NOT_SUPPORTED);
    }

    @Override
    public void dispatch(String name) {
        throw new UnsupportedOperationException(RESPONSE_ACTION_NOT_SUPPORTED);
    }

    @Override
    public void dispatch(String name, String dispatcherName) {
        throw new UnsupportedOperationException(RESPONSE_ACTION_NOT_SUPPORTED);
    }

    @Override
    public void dispatch(DispatchRule dispatchRule) {
        throw new UnsupportedOperationException(RESPONSE_ACTION_NOT_SUPPORTED);
    }

    @Override
    public void forward(String transletName) {
        throw new UnsupportedOperationException(RESPONSE_ACTION_NOT_SUPPORTED);
    }

    @Override
    public void forward(ForwardRule forwardRule) {
        throw new UnsupportedOperationException(RESPONSE_ACTION_NOT_SUPPORTED);
    }

    @Override
    public void redirect(String path) {
        throw new UnsupportedOperationException(RESPONSE_ACTION_NOT_SUPPORTED);
    }

    @Override
    public void redirect(String path, Map<String, String> parameters) {
        throw new UnsupportedOperationException(RESPONSE_ACTION_NOT_SUPPORTED);
    }

    @Override
    public void redirect(RedirectRule redirectRule) {
        throw new UnsupportedOperationException(RESPONSE_ACTION_NOT_SUPPORTED);
    }

    @Override
    public void response(Response response) {
        throw new UnsupportedOperationException(RESPONSE_ACTION_NOT_SUPPORTED);
    }

    @Override
    public void response() {
        throw new UnsupportedOperationException(RESPONSE_ACTION_NOT_SUPPORTED);
    }

    @Override
    public Response getDeclaredResponse() {
        return null;
    }

    @Override
    public boolean isResponseReserved() {
        return false;
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
    public Throwable getRootCauseOfRaisedException() {
        return activity.getRootCauseOfRaisedException();
    }

    @Override
    public void removeRaisedException() {
        activity.clearRaisedException();
    }

    @Override
    public <V> V getAdviceBean(String aspectId) {
        return activity.getAdviceBean(aspectId);
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
        return false;
    }

    @Override
    public String getWrittenResponse() {
        return null;
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
        return (V)activity.getTokenEvaluator().evaluate(tokens);
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
