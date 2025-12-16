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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.component.aspect.AdviceRuleRegistry;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.rule.ability.Describable;
import com.aspectran.core.context.rule.ability.HasActionRules;
import com.aspectran.core.context.rule.ability.HasResponseRules;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.PrefixSuffixPattern;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.wildcard.WildcardPattern;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete blueprint for handling a single request.
 * A TransletRule aggregates all the necessary rules for a request-response cycle,
 * including request parsing, a list of actions to execute, and how to generate the final response.
 * It acts as a central container for a unit of work within the framework.
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class TransletRule
        implements HasActionRules, HasResponseRules, Replicable<TransletRule>, Describable {

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

    private AdviceRuleRegistry adviceRuleRegistry;

    private DescriptionRule descriptionRule;

    /**
     * Gets the translet name.
     * @return the translet name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the translet name.
     * @param name the new translet name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the array of HTTP methods allowed for this translet.
     * @return the allowed methods
     */
    public MethodType[] getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * Sets the array of HTTP methods allowed for this translet.
     * @param allowedMethods the allowed methods
     */
    public void setAllowedMethods(MethodType[] allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * Gets the wildcard pattern for matching translet names.
     * @return the name pattern
     */
    public WildcardPattern getNamePattern() {
        return namePattern;
    }

    /**
     * Sets the wildcard pattern for matching translet names.
     * @param namePattern the new name pattern
     */
    public void setNamePattern(WildcardPattern namePattern) {
        this.namePattern = namePattern;
    }

    /**
     * Gets the tokens parsed from the translet name, used for path variables.
     * @return the name tokens
     */
    public Token[] getNameTokens() {
        return nameTokens;
    }

    /**
     * Sets the tokens parsed from the translet name.
     * @param nameTokens the new name tokens
     */
    public void setNameTokens(Token[] nameTokens) {
        this.nameTokens = nameTokens;
    }

    /**
     * Gets the scan path for dynamically generating translets.
     * @return the scan path
     */
    public String getScanPath() {
        return scanPath;
    }

    /**
     * Sets the scan path for dynamically generating translets.
     * @param scanPath the new scan path
     */
    public void setScanPath(String scanPath) {
        this.scanPath = scanPath;
    }

    /**
     * Gets the mask pattern to exclude from scanning.
     * @return the mask pattern
     */
    public String getMaskPattern() {
        return maskPattern;
    }

    /**
     * Sets the mask pattern to exclude from scanning.
     * @param maskPattern the new mask pattern
     */
    public void setMaskPattern(String maskPattern) {
        this.maskPattern = maskPattern;
    }

    /**
     * Gets the filter parameters for scanning.
     * @return the filter parameters
     */
    public FilterParameters getFilterParameters() {
        return filterParameters;
    }

    /**
     * Sets the filter parameters for scanning.
     * @param filterParameters the new filter parameters
     */
    public void setFilterParameters(FilterParameters filterParameters) {
        this.filterParameters = filterParameters;
    }

    /**
     * Returns whether this translet should be executed asynchronously.
     * @return true if asynchronous, false otherwise
     */
    public boolean isAsync() {
        return BooleanUtils.toBoolean(async);
    }

    /**
     * Sets whether this translet should be executed asynchronously.
     * @param async true for asynchronous execution
     */
    public void setAsync(Boolean async) {
        this.async = async;
    }

    /**
     * Gets the timeout for asynchronous execution.
     * @return the timeout in milliseconds
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout for asynchronous execution.
     * @param timeout the timeout in milliseconds
     */
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the request rule for this translet.
     * @return the request rule
     */
    public RequestRule getRequestRule() {
        return requestRule;
    }

    /**
     * Sets the request rule for this translet.
     * @param requestRule the new request rule
     */
    public void setRequestRule(RequestRule requestRule) {
        this.requestRule = requestRule;
    }

    /**
     * Gets the request rule, creating it if it does not exist.
     * @param explicit whether the rule is explicitly defined
     * @return the request rule
     */
    public RequestRule touchRequestRule(boolean explicit) {
        if (requestRule == null) {
            requestRule = RequestRule.newInstance(explicit);
        }
        return requestRule;
    }

    /**
     * Gets the content list which contains the actions to be executed.
     * @return the content list
     */
    public ContentList getContentList() {
        return contentList;
    }

    /**
     * Sets the content list.
     * @param contentList the new content list
     */
    public void setContentList(ContentList contentList) {
        this.contentList = contentList;
    }

    /**
     * Returns whether the translet name has tokens for extracting path variables.
     * @return true if the translet name has path variables
     */
    public boolean hasPathVariables() {
        return (nameTokens != null);
    }

    @Override
    public Executable putActionRule(HeaderActionRule headerActionRule) {
        return touchActionList().putActionRule(headerActionRule);
    }

    @Override
    public Executable putActionRule(EchoActionRule echoActionRule) {
        return touchActionList().putActionRule(echoActionRule);
    }

    @Override
    public Executable putActionRule(InvokeActionRule invokeActionRule) {
        return touchActionList().putActionRule(invokeActionRule);
    }

    @Override
    public Executable putActionRule(AnnotatedActionRule annotatedActionRule) {
        return touchActionList().putActionRule(annotatedActionRule);
    }

    @Override
    public Executable putActionRule(IncludeActionRule includeActionRule) {
        return touchActionList().putActionRule(includeActionRule);
    }

    @Override
    public Executable putActionRule(ChooseRule chooseRule) {
        return touchActionList().putActionRule(chooseRule);
    }

    @Override
    public void putActionRule(Executable action) {
        touchActionList().putActionRule(action);
    }

    /**
     * Returns the action list, creating it if it does not yet exist.
     * @return the action list
     */
    private ActionList touchActionList() {
        if (contentList != null) {
            if (contentList.isExplicit() || contentList.size() != 1) {
                contentList = null;
            } else {
                ActionList actionList = contentList.getFirst();
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
            actionList = contentList.getFirst();
        }
        return actionList;
    }

    /**
     * Gets the primary response rule.
     * @return the response rule
     */
    public ResponseRule getResponseRule() {
        return responseRule;
    }

    /**
     * Sets the primary response rule.
     * @param responseRule the new response rule
     */
    public void setResponseRule(ResponseRule responseRule) {
        this.responseRule = responseRule;
    }

    /**
     * Gets the list of all response rules, used when multiple responses are defined.
     * @return the list of response rules
     */
    public List<ResponseRule> getResponseRuleList() {
        return responseRuleList;
    }

    /**
     * Sets the list of response rules.
     * @param responseRuleList the new list of response rules
     */
    public void setResponseRuleList(List<ResponseRule> responseRuleList) {
        this.responseRuleList = responseRuleList;
    }

    /**
     * Adds a response rule to the list.
     * @param responseRule the response rule to add
     */
    public void addResponseRule(ResponseRule responseRule) {
        if (responseRuleList == null) {
            responseRuleList = new ArrayList<>();
        }
        responseRuleList.add(responseRule);
    }

    @Override
    public Response putResponseRule(TransformRule transformRule) {
        if (responseRule == null) {
            responseRule = new ResponseRule(false);
        }
        return responseRule.putResponseRule(transformRule);
    }

    @Override
    public Response putResponseRule(DispatchRule dispatchRule) {
        if (responseRule == null) {
            responseRule = new ResponseRule(false);
        }
        return responseRule.putResponseRule(dispatchRule);
    }

    @Override
    public Response putResponseRule(ForwardRule forwardRule) {
        if (responseRule == null) {
            responseRule = new ResponseRule(false);
        }
        return responseRule.putResponseRule(forwardRule);
    }

    @Override
    public Response putResponseRule(RedirectRule redirectRule) {
        if (responseRule == null) {
            responseRule = new ResponseRule(false);
        }
        return responseRule.putResponseRule(redirectRule);
    }

    /**
     * Determines the final response rule to be used, especially when multiple are defined.
     */
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

    /**
     * Gets the exception handling rule for this translet.
     * @return the exception rule
     */
    public ExceptionRule getExceptionRule() {
        return exceptionRule;
    }

    /**
     * Sets the exception handling rule for this translet.
     * @param exceptionRule the new exception rule
     */
    public void setExceptionRule(ExceptionRule exceptionRule) {
        this.exceptionRule = exceptionRule;
    }

    /**
     * Gets the registry for advice rules applicable to this translet.
     * @return the advice rule registry
     */
    public AdviceRuleRegistry getAdviceRuleRegistry() {
        return adviceRuleRegistry;
    }

    /**
     * Sets the registry for advice rules.
     * @param adviceRuleRegistry the new advice rule registry
     */
    public void setAdviceRuleRegistry(AdviceRuleRegistry adviceRuleRegistry) {
        this.adviceRuleRegistry = adviceRuleRegistry;
    }

    /**
     * Gets the advice rule registry, creating it if it does not exist.
     * @return the advice rule registry
     */
    public AdviceRuleRegistry touchAdviceRuleRegistry() {
        if (adviceRuleRegistry == null) {
            adviceRuleRegistry = new AdviceRuleRegistry();
        }
        return adviceRuleRegistry;
    }

    /**
     * Creates a replica of the advice rule registry.
     * @return a replicated advice rule registry, or null if none exists
     */
    public AdviceRuleRegistry replicateAdviceRuleRegistry() {
        return (adviceRuleRegistry != null ? adviceRuleRegistry.replicate() : null);
    }

    @Override
    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    @Override
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
                (requestRule.getAllowedMethod() != null ||
                    requestRule.getEncoding() != null ||
                    requestRule.getParameterItemRuleMap() != null ||
                    requestRule.getAttributeItemRuleMap() != null)) {
            tsb.append("requestRule", requestRule);
        }
        if (responseRule != null &&
                (responseRule.getEncoding() != null ||
                    responseRule.getResponse() != null)) {
            tsb.append("responseRule", responseRule);
        }
        tsb.append("exceptionRule", exceptionRule);
        return tsb.toString();
    }

    /**
     * Creates a new instance of TransletRule.
     * @param name the translet name
     * @param scanPath the path to scan for dynamic translets
     * @param maskPattern the pattern to exclude from scanning
     * @param method the allowed HTTP methods
     * @param async whether to execute asynchronously
     * @param timeout the timeout for async execution
     * @return a new TransletRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
    public static TransletRule newInstance(
            String name, String scanPath, String maskPattern, String method,
            Boolean async, String timeout) throws IllegalRuleException {
        return newInstance(name, scanPath, maskPattern, parseAllowedMethods(method), async, parseTimeout(timeout));
    }

    /**
     * Creates a new instance of TransletRule.
     * @param name the translet name
     * @param scanPath the path to scan for dynamic translets
     * @param maskPattern the pattern to exclude from scanning
     * @param method the allowed HTTP methods
     * @param async whether to execute asynchronously
     * @param timeout the timeout for async execution
     * @return a new TransletRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
    public static TransletRule newInstance(
            String name, String scanPath, String maskPattern, String method,
            Boolean async, Long timeout) throws IllegalRuleException {
        return newInstance(name, scanPath, maskPattern, parseAllowedMethods(method), async, timeout);
    }

    /**
     * Creates a new instance of TransletRule.
     * @param name the translet name
     * @param scanPath the path to scan for dynamic translets
     * @param maskPattern the pattern to exclude from scanning
     * @param allowedMethods the allowed HTTP methods
     * @param async whether to execute asynchronously
     * @param timeout the timeout for async execution
     * @return a new TransletRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
    public static TransletRule newInstance(
            String name, String scanPath, String maskPattern, MethodType[] allowedMethods,
            Boolean async, String timeout) throws IllegalRuleException {
        return newInstance(name, scanPath, maskPattern, allowedMethods, async, parseTimeout(timeout));
    }

    /**
     * Creates a new instance of TransletRule.
     * @param name the translet name
     * @param allowedMethods the allowed HTTP methods
     * @param async whether to execute asynchronously
     * @param timeout the timeout for async execution
     * @return a new TransletRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
    public static TransletRule newInstance(
            String name, MethodType[] allowedMethods, Boolean async, Long timeout)
            throws IllegalRuleException {
        return newInstance(name, null, null, allowedMethods, async, timeout);
    }

    @NonNull
    private static TransletRule newInstance(
            String name, String scanPath, String maskPattern, MethodType[] allowedMethods,
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
        if (StringUtils.hasLength(timeout)) {
            try {
                parsedTimeout = Long.parseLong(timeout);
            } catch (NumberFormatException e) {
                throw new IllegalRuleException("Invalid attribute 'timeout' of element 'translet': " + timeout);
            }
        }
        return parsedTimeout;
    }

    /**
     * Creates a replica of the given TransletRule.
     * @param transletRule the translet rule to replicate
     * @return a new, replicated instance of TransletRule
     */
    @NonNull
    public static TransletRule replicate(@NonNull TransletRule transletRule) {
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

    /**
     * Creates a replica of the given TransletRule with a new name.
     * @param transletRule the translet rule to replicate
     * @param newName the new name for the replicated translet
     * @return a new, replicated instance of TransletRule
     */
    @NonNull
    public static TransletRule replicate(@NonNull TransletRule transletRule, String newName) {
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
            ResponseRule rr = replicate(responseRule, newName);
            tr.setResponseRule(rr);
        }
        if (transletRule.getResponseRuleList() != null) {
            List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
            List<ResponseRule> newResponseRuleList = new ArrayList<>(responseRuleList.size());
            for (ResponseRule responseRule : responseRuleList) {
                ResponseRule rr = replicate(responseRule, newName);
                newResponseRuleList.add(rr);
            }
            tr.setResponseRuleList(newResponseRuleList);
        }
        return tr;
    }

    @NonNull
    private static ResponseRule replicate(@NonNull ResponseRule responseRule, String newName) {
        ResponseRule rr = responseRule.replicate();
        if (rr.getResponse() != null) {
            // assign dispatch name if the dispatch response exists
            if (rr.getResponse() instanceof DispatchResponse dispatchResponse) {
                DispatchRule dispatchRule = dispatchResponse.getDispatchRule();
                String name = dispatchRule.getName();
                PrefixSuffixPattern prefixSuffixPattern = PrefixSuffixPattern.of(name);
                if (prefixSuffixPattern != null) {
                    dispatchRule.setName(prefixSuffixPattern.enclose(newName));
                } else {
                    if (name != null) {
                        dispatchRule.setName(name + newName);
                    } else {
                        dispatchRule.setName(newName);
                    }
                }
            }
        }
        return rr;
    }

}
