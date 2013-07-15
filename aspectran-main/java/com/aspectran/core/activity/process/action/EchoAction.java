/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ActionPathMaker;
import com.aspectran.core.rule.EchoActionRule;
import com.aspectran.core.token.expression.ItemTokenExpression;
import com.aspectran.core.token.expression.ItemTokenExpressor;
import com.aspectran.core.var.ValueMap;

/**
 * <p>Created: 2008. 03. 22 오후 5:50:44</p>
 */
public class EchoAction implements Executable {

	private final EchoActionRule echoActionRule;
	
	private final ActionList parent;
	
	/**
	 * Instantiates a new echo action.
	 * 
	 * @param echoActionRule the echo action rule
	 * @param parent the parent
	 */
	public EchoAction(EchoActionRule echoActionRule, ActionList parent) {
		this.echoActionRule = echoActionRule;
		this.parent = parent;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#execute(org.jhlabs.translets.action.Translet)
	 */
	public Object execute(AspectranActivity activity) {
		if(echoActionRule.getItemRuleMap() == null)
			return null;
		
		ItemTokenExpressor expressor = new ItemTokenExpression(activity);
		ValueMap valueMap = expressor.express(echoActionRule.getItemRuleMap());
		
		return valueMap;
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
	 * @see org.jhlabs.translets.engine.process.action.Executable#getContent()
	 */
	public ActionList getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#getId()
	 */
	public String getId() {
		if(echoActionRule == null)
			return null;
		
		return echoActionRule.getActionId();
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#isHidden()
	 */
	public boolean isHidden() {
		if(echoActionRule.getHidden() == Boolean.TRUE)
			return true;
		else
			return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{actionPath=").append(ActionPathMaker.concatActionPath(parent.getContentId(), echoActionRule.getActionId()));
		sb.append(", echoActionRule=").append(echoActionRule.toString());
		sb.append("}");
		
		return sb.toString();
	}
}
