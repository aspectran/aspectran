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
package com.aspectran.core.activity.process;

import java.util.ArrayList;

import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.action.IncludeAction;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.util.BooleanUtils;

/**
 * The action list class.
 * 
 * <p>Created: 2008. 03. 23 오전 1:38:14</p>
 */
public class ActionList extends ArrayList<Executable> implements ActionRuleApplicable {
	
	/** @serial */
	static final long serialVersionUID = 4636431127789162551L;

	private final String contentId;

	private Boolean hidden;
	
	private Boolean omittable;
	
	private final ContentList parent;

	public ActionList() {
		this(null, null);
	}
	
	/**
	 * Instantiates a new action list.
	 * 
	 * @param parent the action list
	 */
	public ActionList(String contentId, ContentList parent) {
		super();
		this.contentId = contentId;
		this.parent = parent;
	}
	
	/**
	 * Gets the content id.
	 * 
	 * @return the content id
	 */
	public String getContentId() {
		return contentId;
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

	/**
	 * Gets the content list.
	 * 
	 * @return the content list
	 */
	public ContentList getParent() {
		return parent;
	}

	/**
	 * Adds the echo action.
	 * 
	 * @param echoActionRule the echo action rule
	 */
	public void applyEchoActionRule(EchoActionRule echoActionRule) {
		EchoAction echoAction = new EchoAction(echoActionRule, this);
		add(echoAction);
	}

	/**
	 * Adds the bean action.
	 * 
	 * @param beanActionRule the bean action rule
	 * 
	 * @throws ClassNotFoundException the class is not found exception
	 * @throws InstantiationException the instantiation exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws NoSuchMethodException the no such method exception
	 */
	public void applyBeanActionRule(BeanActionRule beanActionRule) {
		BeanAction beanAction = new BeanAction(beanActionRule, this);

		add(beanAction);
	}

	/**
	 * Adds the process-call action.
	 * 
	 * @param includeActionRule the process call action rule
	 */
	public void applyIncludeActionRule(IncludeActionRule includeActionRule) {
		IncludeAction includeAction = new IncludeAction(includeActionRule, this);
		add(includeAction);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{id=").append(contentId);
		sb.append(", hidden=").append(hidden);
		sb.append(", omittable=").append(omittable);
		sb.append(", executables=");

		if(size() > 0) {
			sb.append('[');

			for(int i = 0; i < size(); i++) {
				Executable action = get(i);

				if(i > 0)
					sb.append(", ");

				sb.append(action.getActionId());
			}

			sb.append(']');
		}

		sb.append("}");

		return sb.toString();
	}
	
	public static ActionList newInstance(String id, Boolean hidden, ContentList contentList) {
		ActionList actionList = new ActionList(id, contentList);
		
		if(hidden != null)
			actionList.setHidden(hidden);
		
		return actionList;
	}
	
}
