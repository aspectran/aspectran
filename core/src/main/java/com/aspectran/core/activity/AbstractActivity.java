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

import com.aspectran.core.activity.aspect.result.AspectAdviceResult;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.component.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.component.template.TemplateProcessor;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.util.ExceptionUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.List;

/**
 * The Class AbstractActivity.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public abstract class AbstractActivity implements Activity {

    private static final Log log = LogFactory.getLog(AbstractActivity.class);

    private final ActivityContext context;

    private boolean included;

    private Activity outerActivity;

    private Throwable raisedException;

    private SessionAdapter sessionAdapter;

    private RequestAdapter requestAdapter;

    private ResponseAdapter responseAdapter;

    private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;

    private AspectAdviceResult aspectAdviceResult;

    /**
     * Instantiates a new abstract activity.
     *
     * @param context the activity context
     */
    protected AbstractActivity(ActivityContext context) {
        this.context = context;
    }

    @Override
    public ActivityContext getActivityContext() {
        return context;
    }

    @Override
    public Environment getEnvironment() {
        return context.getEnvironment();
    }

    /**
     * Gets the current activity.
     *
     * @return the current activity
     */
    protected Activity getCurrentActivity() {
        return context.getCurrentActivity();
    }

    /**
     * Saves the current activity.
     */
    protected void saveCurrentActivity() {
        context.setCurrentActivity(this);
    }

    /**
     * Backups the current activity.
     */
    protected void backupCurrentActivity() {
        outerActivity = getCurrentActivity();
        saveCurrentActivity();
    }

    /**
     * Removes the current activity.
     */
    protected void removeCurrentActivity() {
        if (outerActivity != null) {
            context.setCurrentActivity(outerActivity);
        } else {
            context.removeCurrentActivity();
        }
    }

    @Override
    public boolean isIncluded() {
        return included;
    }

    /**
     * Sets whether this activity is included in other activity.
     *
     * @param included whether or not included in other activity
     */
    public void setIncluded(boolean included) {
        this.included = included;
    }

    @Override
    public Throwable getRaisedException() {
        return raisedException;
    }

    @Override
    public void setRaisedException(Throwable raisedException) {
        if (this.raisedException == null) {
            if (log.isDebugEnabled()) {
                log.error("Raised exception: ", ExceptionUtils.getRootCause(raisedException));
            }
            this.raisedException = raisedException;
        }
    }

    @Override
    public boolean isExceptionRaised() {
        return (this.raisedException != null);
    }

    @Override
    public Throwable getRootCauseOfRaisedException() {
        return ExceptionUtils.getRootCause(raisedException);
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return getEnvironment().getApplicationAdapter();
    }

    @Override
    public SessionAdapter getSessionAdapter() {
        return sessionAdapter;
    }

    /**
     * Sets the session adapter.
     *
     * @param sessionAdapter the new session adapter
     */
    protected void setSessionAdapter(SessionAdapter sessionAdapter) {
        this.sessionAdapter = sessionAdapter;
    }

    @Override
    public RequestAdapter getRequestAdapter() {
        return requestAdapter;
    }

    /**
     * Sets the request adapter.
     *
     * @param requestAdapter the new request adapter
     */
    protected void setRequestAdapter(RequestAdapter requestAdapter) {
        this.requestAdapter = requestAdapter;
    }

    @Override
    public ResponseAdapter getResponseAdapter() {
        return responseAdapter;
    }

    /**
     * Sets the response adapter.
     *
     * @param responseAdapter the new response adapter
     */
    protected void setResponseAdapter(ResponseAdapter responseAdapter) {
        this.responseAdapter = responseAdapter;
    }

    protected AspectAdviceRuleRegistry touchAspectAdviceRuleRegistry() {
        if (aspectAdviceRuleRegistry == null) {
            aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
        }
        return aspectAdviceRuleRegistry;
    }

    protected List<AspectAdviceRule> getBeforeAdviceRuleList() {
        if (aspectAdviceRuleRegistry != null) {
            return aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
        } else {
            return null;
        }
    }

    protected List<AspectAdviceRule> getAfterAdviceRuleList() {
        if (aspectAdviceRuleRegistry != null) {
            return aspectAdviceRuleRegistry.getAfterAdviceRuleList();
        } else {
            return null;
        }
    }

    protected List<AspectAdviceRule> getFinallyAdviceRuleList() {
        if (aspectAdviceRuleRegistry != null) {
            return aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
        } else {
            return null;
        }
    }

    protected List<ExceptionRule> getExceptionRuleList() {
        if (aspectAdviceRuleRegistry != null) {
            return aspectAdviceRuleRegistry.getExceptionRuleList();
        } else {
            return null;
        }
    }

    @Override
    public <T> T getSetting(String settingName) {
        return (aspectAdviceRuleRegistry != null ? aspectAdviceRuleRegistry.getSetting(settingName) : null);
    }

    /**
     * Gets the aspect advice bean.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the aspect advice bean
     */
    @SuppressWarnings("unchecked")
    public <T> T getAspectAdviceBean(String aspectId) {
        return (aspectAdviceResult != null ? (T)aspectAdviceResult.getAspectAdviceBean(aspectId) : null);
    }

    /**
     * Puts the aspect advice bean.
     *
     * @param aspectId the aspect id
     * @param adviceBean the advice bean
     */
    protected void putAspectAdviceBean(String aspectId, Object adviceBean) {
        if (aspectAdviceResult == null) {
            aspectAdviceResult = new AspectAdviceResult();
        }
        aspectAdviceResult.putAspectAdviceBean(aspectId, adviceBean);
    }

    /**
     * Gets the before advice result.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the before advice result
     */
    @SuppressWarnings("unchecked")
    public <T> T getBeforeAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (T)aspectAdviceResult.getBeforeAdviceResult(aspectId) : null);
    }

    /**
     * Gets the after advice result.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the after advice result
     */
    @SuppressWarnings("unchecked")
    public <T> T getAfterAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (T)aspectAdviceResult.getAfterAdviceResult(aspectId) : null);
    }

    /**
     * Gets the around advice result.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the around advice result
     */
    @SuppressWarnings("unchecked")
    public <T> T getAroundAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (T)aspectAdviceResult.getAroundAdviceResult(aspectId) : null);
    }

    /**
     * Gets the finally advice result.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the finally advice result
     */
    @SuppressWarnings("unchecked")
    public <T> T getFinallyAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (T)aspectAdviceResult.getFinallyAdviceResult(aspectId) : null);
    }

    /**
     * Puts the result of the advice.
     *
     * @param aspectAdviceRule the aspect advice rule
     * @param adviceActionResult the advice action result
     */
    protected void putAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult) {
        if (aspectAdviceResult == null) {
            aspectAdviceResult = new AspectAdviceResult();
        }
        aspectAdviceResult.putAdviceResult(aspectAdviceRule, adviceActionResult);
    }

    /**
     * Gets the aspect rule registry.
     *
     * @return the aspect rule registry
     */
    protected AspectRuleRegistry getAspectRuleRegistry() {
        return context.getAspectRuleRegistry();
    }

    /**
     * Gets the translet rule registry.
     *
     * @return the translet rule registry
     */
    protected TransletRuleRegistry getTransletRuleRegistry() {
        return context.getTransletRuleRegistry();
    }

    @Override
    public TemplateProcessor getTemplateProcessor() {
        return context.getTemplateProcessor();
    }

    @Override
    public BeanRegistry getBeanRegistry() {
        return context.getBeanRegistry();
    }

    @Override
    public <T> T getBean(String id) {
        return context.getBeanRegistry().getBean(id);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return context.getBeanRegistry().getBean(requiredType);
    }

    @Override
    public <T> T getBean(String id, Class<T> requiredType) {
        return context.getBeanRegistry().getBean(id, requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType, String id) {
        return context.getBeanRegistry().getBean(requiredType, id);
    }

    @Override
    public <T> T getConfigBean(Class<T> classType) {
        return context.getBeanRegistry().getConfigBean(classType);
    }

    @Override
    public boolean containsBean(String id) {
        return context.getBeanRegistry().containsBean(id);
    }

    @Override
    public boolean containsBean(Class<?> requiredType) {
        return context.getBeanRegistry().containsBean(requiredType);
    }

    @Override
    public void terminate() {
        terminate("Terminated by user code");
    }

    @Override
    public void terminate(String cause) {
        throw new ActivityTerminatedException(cause);
    }

    @Override
    public void registerAspectRule(AspectRule aspectRule) {
        if (!isAcceptable(aspectRule)) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("apply AspectRule " + aspectRule);
        }

        List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
        if (aspectAdviceRuleList != null) {
            for (AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
                if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
                    executeAdvice(aspectAdviceRule);
                }
            }
        }

        /*
         * The before advice is excluded because it was already executed.
         */
        touchAspectAdviceRuleRegistry().registerDynamically(aspectRule);
    }

    protected void prepareAspectAdviceRule(TransletRule transletRule) {
        if (transletRule.hasPathVariable()) {
            AspectAdviceRulePostRegister aarPostRegister = new AspectAdviceRulePostRegister();
            for (AspectRule aspectRule : getAspectRuleRegistry().getAspectRules()) {
                JoinpointTargetType joinpointTargetType = aspectRule.getJoinpointTargetType();
                if (!aspectRule.isBeanRelevant() && joinpointTargetType == JoinpointTargetType.TRANSLET) {
                    if (isAcceptable(aspectRule)) {
                        Pointcut pointcut = aspectRule.getPointcut();
                        if (pointcut == null || pointcut.matches(transletRule.getName())) {
                            if (log.isDebugEnabled()) {
                                log.debug("apply AspectRule " + aspectRule);
                            }
                            aarPostRegister.register(aspectRule);
                        }
                    }
                }
            }
            this.aspectAdviceRuleRegistry = aarPostRegister.getAspectAdviceRuleRegistry();
        } else {
            this.aspectAdviceRuleRegistry = transletRule.replicateAspectAdviceRuleRegistry();
        }
    }

    private boolean isAcceptable(AspectRule aspectRule) {
        if (aspectRule.getMethods() != null) {
            if (getRequestMethod() == null || !getRequestMethod().containsTo(aspectRule.getMethods())) {
                return false;
            }
        }
        if (aspectRule.getHeaders() != null) {
            boolean contained = false;
            for (String header : aspectRule.getHeaders()) {
                if (getRequestAdapter().containsHeader(header)) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                return false;
            }
        }
        return true;
    }

}
