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

import java.util.Map;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.variable.ValueMap;
import com.aspectran.core.activity.variable.token.ItemTokenExpression;
import com.aspectran.core.activity.variable.token.ItemTokenExpressor;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class IncludeAction.
 * 
 * <p>Created: 2008. 06. 05 오후 9:22:05</p>
 */
public class IncludeAction extends AbstractAction implements Executable {
	
	/** The Constant log. */
	private static final Log log = LogFactory.getLog(IncludeAction.class);

	/** The include action rule. */
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
	public Object execute(Activity activity) throws Exception {
		try {
			RequestAdapter request = activity.getRequestAdapter();
			
			if(includeActionRule.getAttributeItemRuleMap() != null) {
				ItemTokenExpressor expressor = new ItemTokenExpression(activity);
				ValueMap valueMap = expressor.express(includeActionRule.getAttributeItemRuleMap());

				for(Map.Entry<String, Object> entry : valueMap.entrySet())
					request.setAttribute(entry.getKey(), entry.getValue());
			}
			
			Activity newActivity = activity.newActivity();
			newActivity.ready(includeActionRule.getTransletName());
			newActivity.performWithoutResponse();
			newActivity.finish();
			
			return newActivity.getProcessResult();
			
		} catch(Exception e) {
			log.error("action execution error: includeActionRule " + includeActionRule + " Cause: " + e.toString());
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
		return includeActionRule.isHidden();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.process.action.Executable#getActionType()
	 */
	public ActionType getActionType() {
		return ActionType.INCLUDE;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.process.action.Executable#getActionRule()
	 */
	@SuppressWarnings("unchecked")
	public <T> T getActionRule() {
		return (T)includeActionRule;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{qualifiedActionId=").append(qualifiedActionId);
		sb.append(", actionType=").append(getActionType());
		sb.append(", includeActionRule=").append(includeActionRule.toString());
		sb.append("}");

		return sb.toString();
	}

}
