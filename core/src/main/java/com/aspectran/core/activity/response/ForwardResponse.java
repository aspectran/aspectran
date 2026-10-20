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
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * A response that performs a server-side forward to another resource (e.g., a
 * different translet or a JSP page).
 *
 * <p>This response type evaluates the attributes specified in the forward rule and
 * sets them on the request adapter, making them available to the target resource.
 * The actual forwarding is then handled by the underlying adapter (e.g., using
 * {@code RequestDispatcher.forward()}).</p>
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class ForwardResponse implements Response {

    private static final Logger logger = LoggerFactory.getLogger(ForwardResponse.class);

    private final ForwardRule forwardRule;

    /**
     * Instantiates a new ForwardResponse.
     * @param forwardRule the rule that defines the forwarding target and attributes
     */
    public ForwardResponse(@NonNull ForwardRule forwardRule) {
        Assert.notNull(forwardRule, "forwardRule must not be null");
        this.forwardRule = forwardRule;
    }

    /**
     * Executes the forward by evaluating and setting attributes on the request adapter.
     * @param activity the current activity
     * @throws ResponseException if an error occurs during attribute evaluation
     */
    @Override
    public void respond(Activity activity) throws ResponseException {
        if (logger.isDebugEnabled()) {
            logger.debug("Response {}", forwardRule);
        }

        try {
            ItemRuleMap itemRuleMap = forwardRule.getAttributeItemRuleMap();
            if (itemRuleMap != null && !itemRuleMap.isEmpty()) {
                Map<String, Object> valueMap = activity.getItemEvaluator().evaluate(itemRuleMap);
                activity.getRequestAdapter().putAllAttributes(valueMap);
            }
        } catch (Exception e) {
            throw new ResponseException("Failed to respond with forward rule " + forwardRule, e);
        }
    }

    @Override
    public ResponseType getResponseType() {
        return ForwardRule.RESPONSE_TYPE;
    }

    @Override
    public String getContentType() {
        return forwardRule.getContentType();
    }

    @Override
    public Response replicate() {
        return new ForwardResponse(forwardRule.replicate());
    }

    /**
     * Returns the rule that defines the forwarding parameters.
     * @return the forward rule
     */
    public ForwardRule getForwardRule() {
        return forwardRule;
    }

    @Override
    public String toString() {
        if (forwardRule == null) {
            return super.toString();
        }
        return forwardRule.toString();
    }

}
