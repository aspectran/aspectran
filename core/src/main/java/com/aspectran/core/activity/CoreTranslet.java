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

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformResponseFactory;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.support.i18n.message.NoSuchMessageException;
import com.aspectran.core.util.StringUtils;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * The Class CoreTranslet.
 *
 * <p>This class is generally not thread-safe. It is primarily designed for use in a single thread only.</p>
 */
public class CoreTranslet extends AbstractTranslet {

    private final CoreActivity activity;

    private ProcessResult processResult;

    private ActivityDataMap activityDataMap;

    /**
     * Instantiates a new CoreTranslet.
     *
     * @param transletRule the translet rule
     * @param activity the current Activity
     */
    public CoreTranslet(TransletRule transletRule, CoreActivity activity) {
        super(transletRule);
        this.activity = activity;
    }

    @Override
    public String getDescription() {
        return activity.getTransletRule().getDescription();
    }

    @Override
    public Environment getEnvironment() {
        return activity.getEnvironment();
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return getEnvironment().getApplicationAdapter();
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
    public <T> T getApplicationAdaptee() {
        return getApplicationAdapter().getAdaptee();
    }

    @Override
    public <T> T getSessionAdaptee() {
        SessionAdapter sessionAdapter = getSessionAdapter();
        return (sessionAdapter != null ? sessionAdapter.getAdaptee() : null);
    }

    @Override
    public <T> T getRequestAdaptee() {
        return getRequestAdapter().getAdaptee();
    }

    @Override
    public <T> T getResponseAdaptee() {
        return getResponseAdapter().getAdaptee();
    }

    @Override
    public String getRequestEncoding() {
        return activity.resolveRequestEncoding();
    }

    @Override
    public String getResponseEncoding() {
        return activity.resolveResponseEncoding();
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
    public ProcessResult touchProcessResult() {
        return touchProcessResult(null);
    }

    @Override
    public ProcessResult touchProcessResult(String contentsName) {
        return touchProcessResult(contentsName, 5);
    }

    @Override
    public ProcessResult touchProcessResult(String contentsName, int initialCapacity) {
        if (processResult == null) {
            processResult = new ProcessResult(initialCapacity);
            if (contentsName != null) {
                processResult.setName(contentsName);
            }
        }
        return processResult;
    }

    @Override
    public ActivityDataMap getActivityDataMap() {
        return getActivityDataMap(false);
    }

    @Override
    public ActivityDataMap getActivityDataMap(boolean prefill) {
        if (prefill) {
            activityDataMap = new ActivityDataMap(activity, true);
        } else if (activityDataMap == null) {
            activityDataMap = new ActivityDataMap(activity, false);
        }
        return activityDataMap;
    }

    @Override
    public <T> T getSetting(String settingName) {
        return activity.getSetting(settingName);
    }

    @Override
    public <T> T getProperty(String name) {
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
    public <T> T getAttribute(String name) {
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
    public Response getDeclaredResponse() {
        return activity.getDeclaredResponse();
    }

    @Override
    public void response() {
        activity.reserveResponse();
    }

    @Override
    public void response(Response response) {
        activity.reserveResponse(response);
    }

    @Override
    public void transform(TransformRule transformRule) {
        Response res = TransformResponseFactory.createTransformResponse(transformRule);
        response(res);
    }

    @Override
    public void dispatch(String name) {
        dispatch(name, null);
    }

    @Override
    public void dispatch(String name, String dispatcherName) {
        DispatchResponseRule drr = new DispatchResponseRule();
        drr.setName(name, null);
        drr.setDispatcherName(dispatcherName);
        dispatch(drr);
    }

    @Override
    public void dispatch(DispatchResponseRule dispatchResponseRule) {
        Response res = new DispatchResponse(dispatchResponseRule);
        response(res);
    }

    @Override
    public void forward(String transletName) {
        ForwardResponseRule frr = new ForwardResponseRule();
        frr.setTransletName(transletName);
        forward(frr);
    }

    @Override
    public void forward(ForwardResponseRule forwardResponseRule) {
        if (forwardResponseRule.getTransletName() == null) {
            forwardResponseRule.setTransletName(StringUtils.EMPTY);
        }
        Response res = new ForwardResponse(forwardResponseRule);
        response(res);
    }

    @Override
    public void redirect(String path) {
        redirect(path, null);
    }

    @Override
    public void redirect(String path, Map<String, String> parameters) {
        RedirectResponseRule rrr = new RedirectResponseRule();
        rrr.setPath(path, null);
        rrr.setParameters(parameters);
        redirect(rrr);
    }

    @Override
    public void redirect(RedirectResponseRule redirectResponseRule) {
        Response res = new RedirectResponse(redirectResponseRule);
        response(res);
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
    public void clearRaisedException() {
        activity.clearRaisedException();
    }

    @Override
    public Throwable getRootCauseOfRaisedException() {
        return activity.getRootCauseOfRaisedException();
    }

    @Override
    public boolean acceptsProfiles(String... profiles) {
        return activity.getActivityContext().getEnvironment().acceptsProfiles(profiles);
    }

    @Override
    public <T> T getAspectAdviceBean(String aspectId) {
        return activity.getAspectAdviceBean(aspectId);
    }

    @Override
    public <T> T getBeforeAdviceResult(String aspectId) {
        return activity.getBeforeAdviceResult(aspectId);
    }

    @Override
    public <T> T getAfterAdviceResult(String aspectId) {
        return activity.getAfterAdviceResult(aspectId);
    }

    @Override
    public <T> T getAroundAdviceResult(String aspectId) {
        return activity.getAroundAdviceResult(aspectId);
    }

    @Override
    public <T> T getFinallyAdviceResult(String aspectId) {
        return activity.getFinallyAdviceResult(aspectId);
    }

    @Override
    public boolean hasPathVariable() {
        return activity.getTransletRule().hasPathVariable();
    }

    //---------------------------------------------------------------------
    // Implementation of BeanRegistry interface
    //---------------------------------------------------------------------

    @Override
    public <T> T getBean(String id) {
        return activity.getBean(id);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return activity.getBean(requiredType);
    }

    @Override
    public <T> T getBean(String id, Class<T> requiredType) {
        return activity.getBean(id, requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType, String id) {
        return activity.getBean(requiredType, id);
    }

    @Override
    public <T> T getConfigBean(Class<T> classType) {
        return activity.getConfigBean(classType);
    }

    @Override
    public boolean containsBean(String id) {
        return activity.containsBean(id);
    }

    @Override
    public boolean containsBean(Class<?> requiredType) {
        return activity.containsBean(requiredType);
    }

    //---------------------------------------------------------------------
    // Implementation of MessageSource interface
    //---------------------------------------------------------------------

    @Override
    public String getMessage(String code, Object args[], String defaultMessage, Locale locale) {
        return activity.getActivityContext().getMessageSource().getMessage(code, args, defaultMessage, locale);
    }

    @Override
    public String getMessage(String code, Object args[], Locale locale) throws NoSuchMessageException {
        return activity.getActivityContext().getMessageSource().getMessage(code, args, locale);
    }

    @Override
    public String getMessage(String code, Object args[], String defaultMessage) {
        return getMessage(code, args, defaultMessage, getRequestAdapter().getLocale());
    }

    @Override
    public String getMessage(String code, Object args[]) throws NoSuchMessageException {
        return getMessage(code, args, getRequestAdapter().getLocale());
    }

}
