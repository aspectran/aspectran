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

import java.util.ArrayList;

import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.util.BooleanUtils;

/**
 * The Class ContentList.
 * 
 * <p>Created: 2008. 03. 22 PM 5:47:57</p>
 */
public class ContentList extends ArrayList<ActionList> {
	
	/** @serial */
	static final long serialVersionUID = 2567969961069441527L;
	
	private String name;
	
	private Boolean omittable;
	
	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	 * Adds the action list.
	 * 
	 * @param actionList the action list
	 */
	public void addActionList(ActionList actionList) {
		add(actionList);
	}

	public ActionList newActionList(boolean omittable) {
		ActionList actionList = new ActionList();
		if(omittable)
			actionList.setOmittable(Boolean.TRUE);
		
		add(actionList);
		
		return actionList;
	}
	
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}

	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry(boolean clone) throws CloneNotSupportedException {
		if(clone && aspectAdviceRuleRegistry != null)
			return aspectAdviceRuleRegistry.clone();
		
		return aspectAdviceRuleRegistry;
	}

	public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{name=").append(name);
		sb.append(", omittable=").append(omittable);
		sb.append(", contents=");
		sb.append('[');
		for(int i = 0; i < size(); i++) {
			ActionList content = get(i);
			if(i > 0)
				sb.append(", ");
			sb.append(content.toString());
		}
		sb.append(']');
		sb.append('}');
		
		return sb.toString();
	}
	
	public static ContentList newInstance(String name, Boolean omittable) {
		ContentList contentList = new ContentList();
		contentList.setName(name);
		contentList.setOmittable(omittable);
		
		return contentList;
	}
	
}
