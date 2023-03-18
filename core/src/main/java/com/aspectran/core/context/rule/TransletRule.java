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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.component.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.PrefixSuffixPattern;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.wildcard.WildcardPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class TransletRule.
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class TransletRule implements ActionRuleApplicable, ResponseRuleApplicable, Replicable<TransletRule> {

    private String name;

    private MethodType[] allowedMethods;

    private WildcardPattern namePattern;

    private Token[] nameTokens;

    private String scanPath;

    private String maskPattern;

    private FilterParameters filterParameters;

    private Boolean async;

    private Long timeout;

    private RequestRule requestRule;

    private ContentList contentList;

    private ResponseRule responseRule;

    private List<ResponseRule> responseRuleList;

    private ExceptionRule exceptionRule;

    private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;

    private DescriptionRule descriptionRule;

    /**
     * Instantiates a new TransletRule.
     */
    public TransletRule() {
    }

    /**
     * Gets the translet name.
     *
     * @return the translet name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the array of methods allowed on the requested resource.
     *
     * @return the allowed methods
     */
    public MethodType[] getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * Sets the array of methods allowed on the requested resource.
     *
     * @param allowedMethods the allowed methods
     */
    public void setAllowedMethods(MethodType[] allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * Gets the name pattern.
     *
     * @return the name pattern
     */
    public WildcardPattern getNamePattern() {
        return namePattern;
    }

    /**
     * Sets the name pattern.
     *
     * @param namePattern the new name pattern
     */
    public void setNamePattern(WildcardPattern namePattern) {
        this.namePattern = namePattern;
    }

    /**
     * Gets the name tokens.
     *
     * @return the name tokens
     */
    public Token[] getNameTokens() {
        return nameTokens;
    }

    /**
     * Sets the name tokens.
     *
     * @param nameTokens the new name tokens
     */
    public void setNameTokens(Token[] nameTokens) {
        this.nameTokens = nameTokens;
    }

    /**
     * Gets the scan path.
     *
     * @return the scan path
     */
    public String getScanPath() {
        return scanPath;
    }

    /**
     * Sets the scan path.
     *
     * @param scanPath the new scan path
     */
    public void setScanPath(String scanPath) {
        this.scanPath = scanPath;
    }

    /**
     * Gets the mask pattern.
     *
     * @return the mask pattern
     */
    public String getMaskPattern() {
        return maskPattern;
    }

    /**
     * Sets the mask pattern.
     *
     * @param maskPattern the new mask pattern
     */
    public void setMaskPattern(String maskPattern) {
        this.maskPattern = maskPattern;
    }

    /**
     * Gets the filter parameters.
     *
     * @return the filter parameters
     */
    public FilterParameters getFilterParameters() {
        return filterParameters;
    }

    /**
     * Sets the filter parameters.
     *
     * @param filterParameters the new filter parameters
     */
    public void setFilterParameters(FilterParameters filterParameters) {
        this.filterParameters = filterParameters;
    }

    public boolean isAsync() {
        return BooleanUtils.toBoolean(async);
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the request rule.
     *
     * @return the request rule
     */
    public RequestRule getRequestRule() {
        return requestRule;
    }

    /**
     * Sets the request rule.
     *
     * @param requestRule the new request rule
     */
    public void setRequestRule(RequestRule requestRule) {
        this.requestRule = requestRule;
    }

    public RequestRule touchRequestRule(boolean explicit) {
        if (requestRule != null) {
            return requestRule;
        } else {
            requestRule = RequestRule.newInstance(explicit);
            return requestRule;
        }
    }

    /**
     * Gets the content list.
     *
     * @return the content list
     */
    public ContentList getContentList() {
        return contentList;
    }

    /**
     * Sets the content list.
     *
     * @param contentList the new content list
     */
    public void setContentList(ContentList contentList) {
        this.contentList = contentList;
    }

    /**
     * Returns whether the translet name has tokens for extracting parameters or attributes.
     *
     * @return true if the translet name has tokens for extracting parameters or attributes
     */
    public boolean hasPathVariables() {
        return (nameTokens != null);
    }

    @Override
    public Executable applyActionRule(HeaderActionRule headerActionRule) {
        return touchActionList().applyActionRule(headerActionRule);
    }

    @Override
    public Executable applyActionRule(EchoActionRule echoActionRule) {
        return touchActionList().applyActionRule(echoActionRule);
    }

    @Override
    public Executable applyActionRule(InvokeActionRule invokeActionRule) {
        return touchActionList().applyActionRule(invokeActionRule);
    }

    @Override
    public Executable applyActionRule(AnnotatedActionRule annotatedActionRule) {
        return touchActionList().applyActionRule(annotatedActionRule);
    }

    @Override
    public Executable applyActionRule(IncludeActionRule includeActionRule) {
        return touchActionList().applyActionRule(includeActionRule);
    }

    @Override
    public Executable applyActionRule(ChooseRule chooseRule) {
        return touchActionList().applyActionRule(chooseRule);
    }

    @Override
    public void applyActionRule(Executable action) {
        touchActionList().applyActionRule(action);
    }

    /**
     * Returns the action list.
     * If not yet instantiated then create a new one.
     *
     * @return the action list
     */
    private ActionList touchActionList() {
        if (contentList != null) {
            if (contentList.isExplicit() || contentList.size() != 1) {
                contentList = null;
            } else {
                ActionList actionList = contentList.get(0);
                if (actionList.isExplicit()) {
                    contentList = null;
                }
            }
        }
        ActionList actionList;
        if (contentList == null) {
            contentList = new ContentList(false);
            actionList = new ActionList(false);
            contentList.add(actionList);
        } else {
            actionList = contentList.get(0);
        }
        return actionList;
    }

    /**
     * Gets the response rule.
     *
     * @return the response rule
     */
    public ResponseRule getResponseRule() {
        return responseRule;
    }

    /**
     * Sets the response rule.
     *
     * @param responseRule the new response rule
     */
    public void setResponseRule(ResponseRule responseRule) {
        this.responseRule = responseRule;
    }

    public List<ResponseRule> getResponseRuleList() {
        return responseRuleList;
    }

    public void setResponseRuleList(List<ResponseRule> responseRuleList) {
        this.responseRuleList = responseRuleList;
    }

    public void addResponseRule(ResponseRule responseRule) {
        if (responseRuleList == null) {
            responseRuleList = new ArrayList<>();
        }
        responseRuleList.add(responseRule);
    }

    @Override
    public Response applyResponseRule(TransformRule transformRule) {
        if (responseRule == null) {
            responseRule = new ResponseRule(false);
        }
        return responseRule.applyResponseRule(transformRule);
    }

    @Override
    public Response applyResponseRule(DispatchRule dispatchRule) {
        if (responseRule == null) {
            responseRule = new ResponseRule(false);
        }
        return responseRule.applyResponseRule(dispatchRule);
    }

    @Override
    public Response applyResponseRule(ForwardRule forwardRule) {
        if (responseRule == null) {
            responseRule = new ResponseRule(false);
        }
        return responseRule.applyResponseRule(forwardRule);
    }

    @Override
    public Response applyResponseRule(RedirectRule redirectRule) {
        if (responseRule == null) {
            responseRule = new ResponseRule(false);
        }
        return responseRule.applyResponseRule(redirectRule);
    }

    public void determineResponseRule() {
        if (responseRule == null) {
            responseRule = new ResponseRule(false);
        } else {
            String responseName = responseRule.getName();
            if (responseName != null && !responseName.isEmpty()) {
                setName(name + responseName);
            }
        }
        setResponseRuleList(null);
    }

    public ExceptionRule getExceptionRule() {
        return exceptionRule;
    }

    public void setExceptionRule(ExceptionRule exceptionRule) {
        this.exceptionRule = exceptionRule;
    }

    public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
        return aspectAdviceRuleRegistry;
    }

    public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
        this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
    }

    public AspectAdviceRuleRegistry touchAspectAdviceRuleRegistry() {
        if (aspectAdviceRuleRegistry == null) {
            aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
        }
        return aspectAdviceRuleRegistry;
    }

    public AspectAdviceRuleRegistry replicateAspectAdviceRuleRegistry() {
        return (aspectAdviceRuleRegistry != null ? aspectAdviceRuleRegistry.replicate() : null);
    }

    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    public void setDescriptionRule(DescriptionRule descriptionRule) {
        this.descriptionRule = descriptionRule;
    }

    @Override
    public TransletRule replicate() {
        return replicate(this);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("method", allowedMethods);
        tsb.append("namePattern", namePattern);
        tsb.append("async", async);
        tsb.append("timeout", timeout);
        if (requestRule != null &&
                (requestRule.getAllowedMethod() != null || requestRule.getEncoding() != null ||
                        requestRule.getParameterItemRuleMap() != null || requestRule.getAttributeItemRuleMap() != null)) {
            tsb.append("requestRule", requestRule);
        }
        if (responseRule != null &&
                (responseRule.getEncoding() != null || responseRule.getResponse() != null)) {
            tsb.append("responseRule", responseRule);
        }
        tsb.append("exceptionRule", exceptionRule);
        return tsb.toString();
    }

    public static TransletRule newInstance(String name, String scanPath, String maskPattern, String method,
                                           Boolean async, String timeout) throws IllegalRuleException {
        return newInstance(name, scanPath, maskPattern, parseAllowedMethods(method), async, parseTimeout(timeout));
    }

    public static TransletRule newInstance(String name, String scanPath, String maskPattern, String method,
                                           Boolean async, Long timeout) throws IllegalRuleException {
        return newInstance(name, scanPath, maskPattern, parseAllowedMethods(method), async, timeout);
    }

    public static TransletRule newInstance(String name, String scanPath, String maskPattern, MethodType[] allowedMethods,
                                           Boolean async, String timeout) throws IllegalRuleException {
        return newInstance(name, scanPath, maskPattern, allowedMethods, async, parseTimeout(timeout));
    }

    public static TransletRule newInstance(String name, MethodType[] allowedMethods, Boolean async, Long timeout)
            throws IllegalRuleException {
        return newInstance(name, null, null, allowedMethods, async, timeout);
    }

    private static TransletRule newInstance(String name, String scanPath, String maskPattern, MethodType[] allowedMethods,
                                            Boolean async, Long timeout) throws IllegalRuleException {
        if (name == null && scanPath == null) {
            throw new IllegalRuleException("The 'translet' element requires a 'name' attribute");
        }

        TransletRule transletRule = new TransletRule();
        transletRule.setName(name);
        if (allowedMethods != null && allowedMethods.length > 0) {
            transletRule.setAllowedMethods(allowedMethods);
        } else {
            transletRule.setScanPath(scanPath);
            transletRule.setMaskPattern(maskPattern);
        }
        if (async != null) {
            transletRule.setAsync(async);
        }
        if (timeout != null && timeout >= 0) {
            transletRule.setTimeout(timeout);
        }
        return transletRule;
    }

    private static MethodType[] parseAllowedMethods(String method) throws IllegalRuleException {
        MethodType[] allowedMethods = null;
        if (method != null) {
            allowedMethods = MethodType.parse(method);
            if (allowedMethods == null) {
                throw new IllegalRuleException("No request method type for '" + method + "'");
            }
        }
        return allowedMethods;
    }

    private static Long parseTimeout(String timeout) throws IllegalRuleException {
        Long parsedTimeout = null;
        if (!StringUtils.isEmpty(timeout)) {
            try {
                parsedTimeout = Long.parseLong(timeout);
            } catch (NumberFormatException e) {
                throw new IllegalRuleException("The value of 'timeout' attribute on element 'translet' is not valid for 'long'");
            }
        }
        return parsedTimeout;
    }

    public static TransletRule replicate(TransletRule transletRule) {
        TransletRule tr = new TransletRule();
        tr.setName(transletRule.getName());
        tr.setAllowedMethods(transletRule.getAllowedMethods());
        tr.setAsync(transletRule.isAsync());
        tr.setTimeout(transletRule.getTimeout());
        tr.setRequestRule(transletRule.getRequestRule());
        if (transletRule.getContentList() != null) {
            ContentList contentList = transletRule.getContentList().replicate();
            tr.setContentList(contentList);
        }
        tr.setResponseRule(transletRule.getResponseRule());
        tr.setExceptionRule(transletRule.getExceptionRule());
        tr.setDescriptionRule(transletRule.getDescriptionRule());
        return tr;
    }

    public static TransletRule replicate(TransletRule transletRule, String newDispatchName) {
        TransletRule tr = new TransletRule();
        tr.setName(transletRule.getName());
        tr.setAllowedMethods(transletRule.getAllowedMethods());
        tr.setAsync(transletRule.isAsync());
        tr.setTimeout(transletRule.getTimeout());
        tr.setRequestRule(transletRule.getRequestRule());
        tr.setExceptionRule(transletRule.getExceptionRule());
        tr.setDescriptionRule(transletRule.getDescriptionRule());
        if (transletRule.getResponseRule() != null) {
            ResponseRule responseRule = transletRule.getResponseRule();
            ResponseRule rr = replicate(responseRule, newDispatchName);
            tr.setResponseRule(rr);
        }
        if (transletRule.getResponseRuleList() != null) {
            List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
            List<ResponseRule> newResponseRuleList = new ArrayList<>(responseRuleList.size());
            for (ResponseRule responseRule : responseRuleList) {
                ResponseRule rr = replicate(responseRule, newDispatchName);
                newResponseRuleList.add(rr);
            }
            tr.setResponseRuleList(newResponseRuleList);
        }
        return tr;
    }

    private static ResponseRule replicate(ResponseRule responseRule, String newDispatchName) {
        ResponseRule rr = responseRule.replicate();
        if (rr.getResponse() != null) {
            // assign dispatch name if the dispatch response exists
            if (rr.getResponse() instanceof DispatchResponse) {
                DispatchResponse dispatchResponse = (DispatchResponse)rr.getResponse();
                DispatchRule dispatchRule = dispatchResponse.getDispatchRule();
                String dispatchName = dispatchRule.getName();

                PrefixSuffixPattern prefixSuffixPattern = new PrefixSuffixPattern(dispatchName);
                if (prefixSuffixPattern.isSplitted()) {
                    dispatchRule.setName(prefixSuffixPattern.join(newDispatchName));
                } else {
                    if (dispatchName != null) {
                        dispatchRule.setName(dispatchName + newDispatchName);
                    } else {
                        dispatchRule.setName(newDispatchName);
                    }
                }
            }
        }
        return rr;
    }

}
