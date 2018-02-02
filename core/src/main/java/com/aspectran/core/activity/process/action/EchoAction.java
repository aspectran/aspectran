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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class EchoAction.
 * 
 * <p>Created: 2008. 03. 22 PM 5:50:44</p>
 */
public class EchoAction extends AbstractAction {

    private static final Log log = LogFactory.getLog(EchoAction.class);

    private final EchoActionRule echoActionRule;

    /**
     * Instantiates a new EchoAction.
     *
     * @param echoActionRule the echo action rule
     * @param parent the parent
     */
    public EchoAction(EchoActionRule echoActionRule, ActionList parent) {
        super(parent);
        this.echoActionRule = echoActionRule;
    }

    @Override
    public Object execute(Activity activity) throws Exception {
        if (echoActionRule.getAttributeItemRuleMap() == null) {
            return null;
        }

        try {
            ItemEvaluator evaluator = new ItemExpressionParser(activity);
            return evaluator.evaluate(echoActionRule.getAttributeItemRuleMap());
        } catch (Exception e) {
            log.error("Failed to execute an action that echoes attributes. echoActionRule " + echoActionRule);
            throw e;
        }
    }

    /**
     * Returns the echo action rule.
     *
     * @return the echoActionRule
     */
    public EchoActionRule getEchoActionRule() {
        return echoActionRule;
    }

    @Override
    public String getActionId() {
        return echoActionRule.getActionId();
    }

    @Override
    public boolean isHidden() {
        return echoActionRule.isHidden();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ECHO;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getActionRule() {
        return (T)echoActionRule;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("actionType", getActionType());
        tsb.append("echoActionRule", echoActionRule);
        return tsb.toString();
    }

}
