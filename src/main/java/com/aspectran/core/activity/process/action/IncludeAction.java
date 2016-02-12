/**
 * Copyright 2008-2016 Juho Jeong
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

import java.util.Map;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.expr.ItemTokenExpression;
import com.aspectran.core.context.expr.ItemTokenExpressor;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.variable.ValueMap;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class IncludeAction.
 * 
 * <p>Created: 2008. 06. 05 PM 9:22:05</p>
 */
public class IncludeAction extends AbstractAction implements Executable {
	
	private static final Log log = LogFactory.getLog(IncludeAction.class);

	private final IncludeActionRule includeActionRule;

	/**
	 * Instantiates a new IncludeAction.
	 * 
	 * @param includeActionRule the process call action rule
	 * @param parent the parent
	 */
	public IncludeAction(IncludeActionRule includeActionRule, ActionList parent) {
		super(includeActionRule.getActionId(), parent);
		this.includeActionRule = includeActionRule;
	}

	@Override
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
			log.error("Action execution error: includeActionRule " + includeActionRule + " Cause: " + e.toString());
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

	@Override
	public String getActionId() {
		return includeActionRule.getActionId();
	}

	@Override
	public boolean isHidden() {
		return includeActionRule.isHidden();
	}

	@Override
	public ActionType getActionType() {
		return ActionType.INCLUDE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getActionRule() {
		return (T)includeActionRule;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("actionType", getActionType());
		tsb.append("includeActionRule", includeActionRule);
		return tsb.toString();
	}

}
