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
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.Map;

/**
 * The Class ForwardResponse.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class ForwardResponse implements Response {

    private final Log log = LogFactory.getLog(ForwardResponse.class);

    private final boolean debugEnabled = log.isDebugEnabled();

    private final ForwardResponseRule forwardResponseRule;

    /**
     * Instantiates a new ForwardResponse.
     *
     * @param forwardResponseRule the forward response rule
     */
    public ForwardResponse(ForwardResponseRule forwardResponseRule) {
        this.forwardResponseRule = forwardResponseRule;
    }

    @Override
    public void commit(Activity activity) {
        RequestAdapter requestAdapter = activity.getRequestAdapter();
        if (requestAdapter == null) {
            return;
        }

        if (debugEnabled) {
            log.debug("response " + forwardResponseRule);
        }

        if (forwardResponseRule.getAttributeItemRuleMap() != null) {
            ItemEvaluator evaluator = new ItemExpressionParser(activity);
            Map<String, Object> valueMap = evaluator.evaluate(forwardResponseRule.getAttributeItemRuleMap());
            requestAdapter.putAllAttributes(valueMap);
        }
    }

    @Override
    public ResponseType getResponseType() {
        return ForwardResponseRule.RESPONSE_TYPE;
    }

    @Override
    public String getContentType() {
        if (forwardResponseRule != null) {
            return forwardResponseRule.getContentType();
        } else {
            return null;
        }
    }

    @Override
    public ActionList getActionList() {
        return forwardResponseRule.getActionList();
    }

    @Override
    public Response replicate() {
        ForwardResponseRule frr = forwardResponseRule.replicate();
        return new ForwardResponse(frr);
    }

    /**
     * Returns the forward response rule.
     *
     * @return the forward response rule
     */
    public ForwardResponseRule getForwardResponseRule() {
        return forwardResponseRule;
    }

    @Override
    public String toString() {
        return forwardResponseRule.toString();
    }

}
