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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.rule.IncludeActionRule;
import com.aspectran.core.token.expression.ItemTokenExpression;
import com.aspectran.core.token.expression.ItemTokenExpressor;
import com.aspectran.core.type.ActionType;

/**
 * <p>Created: 2008. 06. 05 오후 9:22:05</p>
 */
public class IncludeAction extends AbstractAction implements Executable {
	
	private final Logger logger = LoggerFactory.getLogger(IncludeAction.class);

	private final IncludeActionRule includeActionRule;

	/**
	 * Instantiates a new process call action.
	 * 
	 * @param includeActionRule the process call action rule
	 * @param parent the parent
	 */
	public IncludeAction(IncludeActionRule includeActionRule, ActionList parent) {
		super(includeActionRule.getActionId(), parent);
		this.includeActionRule = includeActionRule;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#execute(org.jhlabs.translets.action.Translet)
	 */
	public Object execute(CoreActivity activity) throws Exception {
		try {
			RequestAdapter request = activity.getRequestAdapter();
			Map<String, Object> valueMap = null;
			
			if(includeActionRule.getAttributeItemRuleMap() != null) {
				ItemTokenExpressor expressor = new ItemTokenExpression(activity);
				valueMap = expressor.express(includeActionRule.getAttributeItemRuleMap());
			}
			
			if(valueMap != null) {
				for(Map.Entry<String, Object> entry : valueMap.entrySet())
					request.setAttribute(entry.getKey(), entry.getValue());
			}
			
			CoreActivity newActivity = activity.newCoreActivity();
			newActivity.init(includeActionRule.getTransletName());
			newActivity.runWithoutResponse();
			
			return newActivity.getProcessResult();
			
		} catch(Exception e) {
			logger.error("action execution error: includeActionRule " + includeActionRule + " Cause: " + e.toString());
			throw e;
		}
	}

	/**
	 * Gets the process call action rule.
	 * 
	 * @return the process call action rule
	 */
	public IncludeActionRule getIncludeActionRule() {
		return includeActionRule;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#getId()
	 */
	public String getActionId() {
		return includeActionRule.getActionId();
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#isHidden()
	 */
	public boolean isHidden() {
		if(includeActionRule.getHidden() == Boolean.TRUE)
			return true;
		else
			return false;
	}
	
	public ActionType getActionType() {
		return ActionType.INCLUDE;
	}

	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return includeActionRule.getAspectAdviceRuleRegistry();
	}
	
	public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		includeActionRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{fullActionId=").append(fullActionId);
		sb.append(", actionType=").append(getActionType());
		sb.append(", includeActionRule=").append(includeActionRule.toString());
		sb.append("}");

		return sb.toString();
	}
}
