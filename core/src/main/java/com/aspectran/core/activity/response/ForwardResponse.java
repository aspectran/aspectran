/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The Class ForwardResponse.
 *
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class ForwardResponse implements Response {

    private static final Logger logger = LoggerFactory.getLogger(ForwardResponse.class);

    private final ForwardRule forwardRule;

    /**
     * Instantiates a new ForwardResponse.
     * @param forwardRule the forward rule
     */
    public ForwardResponse(ForwardRule forwardRule) {
        this.forwardRule = forwardRule;
    }

    @Override
    public void respond(Activity activity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Response {}", forwardRule);
        }

        ItemRuleMap itemRuleMap = forwardRule.getAttributeItemRuleMap();
        if (itemRuleMap != null && !itemRuleMap.isEmpty()) {
            Map<String, Object> valueMap = activity.getItemEvaluator().evaluate(itemRuleMap);
            activity.getRequestAdapter().putAllAttributes(valueMap);
        }
    }

    @Override
    public ResponseType getResponseType() {
        return ForwardRule.RESPONSE_TYPE;
    }

    @Override
    public String getContentType() {
        if (forwardRule != null) {
            return forwardRule.getContentType();
        } else {
            return null;
        }
    }

    @Override
    public Response replicate() {
        return new ForwardResponse(this.forwardRule.replicate());
    }

    /**
     * Returns the forward rule.
     * @return the forward rule
     */
    public ForwardRule getForwardRule() {
        return forwardRule;
    }

    @Override
    public String toString() {
        return forwardRule.toString();
    }

}
