/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import com.aspectran.core.activity.variable.ValueMap;
import com.aspectran.core.activity.variable.token.ItemTokenExpression;
import com.aspectran.core.activity.variable.token.ItemTokenExpressor;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class EchoAction.
 * 
 * <p>Created: 2008. 03. 22 오후 5:50:44</p>
 */
public class EchoAction extends AbstractAction implements Executable {

	private static final Log log = LogFactory.getLog(EchoAction.class);
	
	private final EchoActionRule echoActionRule;
	
	/**
	 * Instantiates a new echo action.
	 * 
	 * @param echoActionRule the echo action rule
	 * @param parent the parent
	 */
	public EchoAction(EchoActionRule echoActionRule, ActionList parent) {
		super(echoActionRule.getActionId(), parent);
		this.echoActionRule = echoActionRule;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#execute(org.jhlabs.translets.action.Translet)
	 */
	public Object execute(Activity activity) throws Exception {
		if(echoActionRule.getAttributeItemRuleMap() == null)
			return null;
		
		try {
			ItemTokenExpressor expressor = new ItemTokenExpression(activity);
			ValueMap valueMap = expressor.express(echoActionRule.getAttributeItemRuleMap());
			
			return valueMap;
		} catch(Exception e) {
			log.error("action execution error: echoActionRule " + echoActionRule + " Cause: " + e.toString());
			throw e;
		}
	}
	
	/**
	 * Gets the echo action rule.
	 * 
	 * @return the echoActionRule
	 */
	public EchoActionRule getEchoActionRule() {
		return echoActionRule;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#getId()
	 */
	public String getActionId() {
		return echoActionRule.getActionId();
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#isHidden()
	 */
	public boolean isHidden() {
		return echoActionRule.isHidden();
	}
	
	public ActionType getActionType() {
		return ActionType.ECHO;
	}

	@SuppressWarnings("unchecked")
	public <T> T getActionRule() {
		return (T)echoActionRule;
	}

	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return echoActionRule.getAspectAdviceRuleRegistry();
	}
	
	public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		echoActionRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{qualifiedActionId=").append(qualifiedActionId);
		sb.append(", actionType=").append(getActionType());
		sb.append(", echoActionRule=").append(echoActionRule.toString());
		sb.append("}");
		
		return sb.toString();
	}
}
