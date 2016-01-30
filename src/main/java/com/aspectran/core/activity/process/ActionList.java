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
package com.aspectran.core.activity.process;

import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.action.IncludeAction;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.util.BooleanUtils;

import java.util.ArrayList;

/**
 * Then Class ActionList.
 * 
 * <p>Created: 2008. 03. 23 AM 1:38:14</p>
 */
public class ActionList extends ArrayList<Executable> implements ActionRuleApplicable {
	
	/** @serial */
	static final long serialVersionUID = 4636431127789162551L;

	private String name;
	
	private Boolean hidden;
	
	private Boolean omittable;
	
	/**
	 * Instantiates a new ActionList.
	 */
	public ActionList() {
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Checks if is hidden.
	 * 
	 * @return true, if is hidden
	 */
	public boolean isHidden() {
		return BooleanUtils.toBoolean(hidden);
	}

	public Boolean getHidden() {
		return hidden;
	}

	/**
	 * Sets the hidden.
	 * 
	 * @param hidden the new hidden
	 */
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isOmittable() {
		return BooleanUtils.toBoolean(omittable);
	}
	
	public Boolean getOmittable() {
		return omittable;
	}

	public void setOmittable(Boolean omittable) {
		this.omittable = omittable;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.context.rule.ability.ActionRuleApplicable#applyActionRule(com.aspectran.core.context.rule.EchoActionRule)
	 */
	public void applyActionRule(EchoActionRule echoActionRule) {
		EchoAction echoAction = new EchoAction(echoActionRule, this);
		add(echoAction);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.context.rule.ability.ActionRuleApplicable#applyActionRule(com.aspectran.core.context.rule.BeanActionRule)
	 */
	public void applyActionRule(BeanActionRule beanActionRule) {
		BeanAction beanAction = new BeanAction(beanActionRule, this);
		add(beanAction);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.context.rule.ability.ActionRuleApplicable#applyActionRule(com.aspectran.core.context.rule.IncludeActionRule)
	 */
	public void applyActionRule(IncludeActionRule includeActionRule) {
		IncludeAction includeAction = new IncludeAction(includeActionRule, this);
		add(includeAction);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{name=").append(name);
		sb.append(", hidden=").append(hidden);
		sb.append(", omittable=").append(omittable);
		sb.append(", executables=");
		sb.append('[');
		for(int i = 0; i < size(); i++) {
			Executable action = get(i);

			if(i > 0)
				sb.append(", ");

			sb.append(action.getActionId());
		}
		sb.append(']');
		sb.append("}");

		return sb.toString();
	}
	
	public static ActionList newInstance(String name, Boolean omittable, Boolean hidden) {
		ActionList actionList = new ActionList();
		actionList.setName(name);
		actionList.setOmittable(omittable);
		actionList.setHidden(hidden);
		
		return actionList;
	}
	
}
