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

import com.aspectran.base.rule.BeanActionRule;
import com.aspectran.base.rule.EchoActionRule;
import com.aspectran.base.rule.IncludeActionRule;
import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.action.IncludeAction;

/**
 * The action list class.
 * 
 * <p>Created: 2008. 03. 23 오전 1:38:14</p>
 */
public class ActionList extends ArrayList<Executable> {
	
	/** @serial */
	static final long serialVersionUID = 4636431127789162551L;

	private String contentId;

	private Boolean hidden;
	
	private ContentList parent;
	
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
		return (hidden == Boolean.TRUE);
	}

	/**
	 * Sets the hidden.
	 * 
	 * @param hidden the new hidden
	 */
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
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
	public void addEchoAction(EchoActionRule echoActionRule) {
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
	public void addBeanAction(BeanActionRule beanActionRule) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NoSuchMethodException {
		BeanAction beanAction = new BeanAction(beanActionRule, this);

		add(beanAction);
	}

	/**
	 * Adds the process-call action.
	 * 
	 * @param includeActionRule the process call action rule
	 */
	public void addIncludeAction(IncludeActionRule includeActionRule) {
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
		sb.append(", executables=");

		if(size() > 0) {
			sb.append('[');

			for(int i = 0; i < size(); i++) {
				Executable action = get(i);

				if(i > 0)
					sb.append(", ");

				sb.append(action.getId());
			}

			sb.append(']');
		}

		sb.append("}");

		return sb.toString();
	}
}
