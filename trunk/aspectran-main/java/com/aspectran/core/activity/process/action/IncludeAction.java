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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ActionPathMaker;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.rule.IncludeActionRule;
import com.aspectran.core.token.expression.ItemTokenExpression;
import com.aspectran.core.token.expression.ItemTokenExpressor;

/**
 * <p>Created: 2008. 06. 05 오후 9:22:05</p>
 */
public class IncludeAction implements Executable {
	
	private final Log log = LogFactory.getLog(IncludeAction.class);

	private final IncludeActionRule includeActionRule;

	private final ActionList parent;
	
	private boolean ignoreTicket;
	
	/**
	 * Instantiates a new process call action.
	 * 
	 * @param includeActionRule the process call action rule
	 * @param parent the parent
	 */
	public IncludeAction(IncludeActionRule includeActionRule, ActionList parent) {
		this.includeActionRule = includeActionRule;
		this.parent = parent;
		this.ignoreTicket = (includeActionRule.getIgnoreTicket() == Boolean.TRUE);
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#execute(org.jhlabs.translets.action.Translet)
	 */
	public Object execute(AspectranActivity activity) throws ActionExecutionException {
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
			
			AspectranActivity newActivity = activity.newAspectranActivity();
			newActivity.request(includeActionRule.getTransletName());
			return newActivity.process(ignoreTicket);
		} catch(Exception e) {
			log.error("Execute error: IncludeActionRule " + includeActionRule.toString());
			throw new ActionExecutionException(this, e);
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
	 * @see org.jhlabs.translets.engine.process.action.Executable#getParent()
	 */
	public ActionList getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#getId()
	 */
	public String getId() {
		if(includeActionRule == null)
			return null;

		return includeActionRule.getId();
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{actionPath=").append(ActionPathMaker.concatActionPath(parent.getContentId(), includeActionRule.getId()));
		sb.append(", includeActionRule=").append(includeActionRule.toString());
		sb.append("}");

		return sb.toString();
	}
}
