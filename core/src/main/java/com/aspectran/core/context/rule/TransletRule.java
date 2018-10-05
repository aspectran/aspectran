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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.component.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.PrefixSuffixPattern;
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

    private RequestRule requestRule;

    private ContentList contentList;

    private boolean explicitContent;

    private ResponseRule responseRule;

    private boolean implicitResponse;

    private List<ResponseRule> responseRuleList;

    private ExceptionRule exceptionRule;

    private Class<? extends Translet> transletInterfaceClass;

    private Class<? extends CoreTranslet> transletImplementationClass;

    private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;

    private String description;

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
        this.explicitContent = (contentList != null);
    }

    /**
     * Returns the content list.
     * If not yet instantiated then create a new one.
     *
     * @return the content list
     */
    public ContentList touchContentList() {
        return touchContentList(false, true);
    }

    /**
     * Returns the content list.
     * If not yet instantiated then create a new one.
     *
     * @param explicitContent whether the content element is explicitly declared
     * @param omittable whether the content list can be omitted
     * @return the content list
     */
    public ContentList touchContentList(boolean explicitContent, boolean omittable) {
        if (contentList == null) {
            contentList = new ContentList();
            this.explicitContent = explicitContent;
            if (omittable) {
                contentList.setOmittable(Boolean.TRUE);
            }
        }
        return contentList;
    }

    /**
     * Returns whether the content element is explicitly declared.
     *
     * @return whether the content element is explicitly declared
     */
    public boolean isExplicitContent() {
        return explicitContent;
    }

    public boolean hasPathVariable() {
        return (nameTokens != null);
    }

    /**
     * Sets the content list.
     *
     * @param contentList the content list
     * @param explicitContent whether the content element is explicitly declared
     */
    private void setContentList(ContentList contentList, boolean explicitContent) {
        this.contentList = contentList;
        this.explicitContent = explicitContent;
    }

    @Override
    public void applyActionRule(BeanActionRule beanActionRule) {
        touchActionList().applyActionRule(beanActionRule);
    }

    @Override
    public void applyActionRule(MethodActionRule methodActionRule) {
        touchActionList().applyActionRule(methodActionRule);
    }

    @Override
    public void applyActionRule(IncludeActionRule includeActionRule) {
        touchActionList().applyActionRule(includeActionRule);
    }

    @Override
    public void applyActionRule(EchoActionRule echoActionRule) {
        touchActionList().applyActionRule(echoActionRule);
    }

    @Override
    public void applyActionRule(HeadingActionRule headingActionRule) {
        touchActionList().applyActionRule(headingActionRule);
    }

    /**
     * Returns the action list.
     * If not yet instantiated then create a new one.
     *
     * @return the action list
     */
    private ActionList touchActionList() {
        touchContentList();

        if (contentList.size() == 1) {
            return contentList.get(0);
        } else {
            return contentList.newActionList(!explicitContent);
        }
    }

    /**
     * Adds the action list.
     *
     * @param actionList the action list
     */
    private void addActionList(ActionList actionList) {
        if (actionList != null) {
            touchContentList();
            contentList.add(actionList);
        }
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
        this.implicitResponse = false;
    }

    public List<ResponseRule> getResponseRuleList() {
        return responseRuleList;
    }

    public void setResponseRuleList(List<ResponseRule> responseRuleList) {
        this.responseRuleList = responseRuleList;
        this.implicitResponse = false;
    }

    public void addResponseRule(ResponseRule responseRule) {
        if (responseRuleList == null) {
            responseRuleList = new ArrayList<>();
        }
        responseRuleList.add(responseRule);
        implicitResponse = false;
    }

    public boolean isImplicitResponse() {
        return implicitResponse;
    }

    @Override
    public Response applyResponseRule(TransformRule transformRule) {
        if (responseRule == null) {
            responseRule = new ResponseRule();
        }
        implicitResponse = true;

        return responseRule.applyResponseRule(transformRule);
    }

    @Override
    public Response applyResponseRule(DispatchResponseRule dispatchResponseRule) {
        if (responseRule == null) {
            responseRule = new ResponseRule();
        }
        implicitResponse = true;

        return responseRule.applyResponseRule(dispatchResponseRule);
    }

    @Override
    public Response applyResponseRule(RedirectResponseRule redirectResponseRule) {
        if (responseRule == null) {
            responseRule = new ResponseRule();
        }
        implicitResponse = true;

        return responseRule.applyResponseRule(redirectResponseRule);
    }

    @Override
    public Response applyResponseRule(ForwardResponseRule forwardResponseRule) {
        if (responseRule == null) {
            responseRule = new ResponseRule();
        }
        implicitResponse = true;

        return responseRule.applyResponseRule(forwardResponseRule);
    }

    public void determineResponseRule() {
        if (responseRule == null) {
            responseRule = new ResponseRule();
        } else {
            if (responseRule.getResponse() != null) {
                addActionList(responseRule.getResponse().getActionList());
            }

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

    public Class<? extends Translet> getTransletInterfaceClass() {
        return transletInterfaceClass;
    }

    public void setTransletInterfaceClass(Class<? extends Translet> transletInterfaceClass) {
        this.transletInterfaceClass = transletInterfaceClass;
    }

    public Class<? extends CoreTranslet> getTransletImplementationClass() {
        return transletImplementationClass;
    }

    public void setTransletImplementationClass(Class<? extends CoreTranslet> transletImplementationClass) {
        this.transletImplementationClass = transletImplementationClass;
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

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
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
        tsb.append("requestRule", requestRule);
        tsb.append("responseRule", responseRule);
        tsb.append("exceptionRule", exceptionRule);
        tsb.append("transletInterfaceClass", transletInterfaceClass);
        tsb.append("transletImplementationClass", transletImplementationClass);
        tsb.append("explicitContent", explicitContent);
        tsb.append("implicitResponse", implicitResponse);
        return tsb.toString();
    }

    public static TransletRule newInstance(String name, String scanPath, String maskPattern, String method)
            throws IllegalRuleException {
        if (name == null && scanPath == null) {
            throw new IllegalRuleException("The 'translet' element requires a 'name' attribute");
        }

        MethodType[] allowedMethods = null;
        if (method != null) {
            allowedMethods = MethodType.parse(method);
            if (allowedMethods == null) {
                throw new IllegalRuleException("No request method type for '" + method + "'");
            }
        }

        return newInstance(name, scanPath, maskPattern, allowedMethods);
    }

    public static TransletRule newInstance(String name, String scanPath, String maskPattern, MethodType[] allowedMethods) {
        TransletRule transletRule = new TransletRule();
        transletRule.setName(name);
        if (allowedMethods != null && allowedMethods.length > 0) {
            transletRule.setAllowedMethods(allowedMethods);
        } else {
            transletRule.setScanPath(scanPath);
            transletRule.setMaskPattern(maskPattern);
        }
        return transletRule;
    }

    public static TransletRule newInstance(String name, String method) throws IllegalRuleException {
        return newInstance(name, null, null, method);
    }

    public static TransletRule newInstance(String name, MethodType[] allowedMethods) {
        return newInstance(name, null, null, allowedMethods);
    }

    public static TransletRule replicate(TransletRule transletRule) {
        TransletRule tr = new TransletRule();
        tr.setName(transletRule.getName());
        tr.setAllowedMethods(transletRule.getAllowedMethods());
        tr.setRequestRule(transletRule.getRequestRule());
        tr.setExceptionRule(transletRule.getExceptionRule());
        tr.setTransletInterfaceClass(transletRule.getTransletInterfaceClass());
        tr.setTransletImplementationClass(transletRule.getTransletImplementationClass());
        tr.setDescription(transletRule.getDescription());
        if (transletRule.getContentList() != null) {
            ContentList contentList = transletRule.getContentList().replicate();
            tr.setContentList(contentList, transletRule.isExplicitContent());
        }
        return tr;
    }

    public static TransletRule replicate(TransletRule transletRule, String newDispatchName) {
        TransletRule tr = new TransletRule();
        tr.setName(transletRule.getName());
        tr.setAllowedMethods(transletRule.getAllowedMethods());
        tr.setRequestRule(transletRule.getRequestRule());
        tr.setExceptionRule(transletRule.getExceptionRule());
        tr.setTransletInterfaceClass(transletRule.getTransletInterfaceClass());
        tr.setTransletImplementationClass(transletRule.getTransletImplementationClass());
        tr.setDescription(transletRule.getDescription());
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
                DispatchResponseRule dispatchResponseRule = dispatchResponse.getDispatchResponseRule();
                String dispatchName = dispatchResponseRule.getName();

                PrefixSuffixPattern prefixSuffixPattern = new PrefixSuffixPattern(dispatchName);

                if (prefixSuffixPattern.isSplitted()) {
                    dispatchResponseRule.setName(prefixSuffixPattern.join(newDispatchName));
                } else {
                    if (dispatchName != null) {
                        dispatchResponseRule.setName(dispatchName + newDispatchName);
                    } else {
                        dispatchResponseRule.setName(newDispatchName);
                    }
                }
            }
        }
        return rr;
    }

}
