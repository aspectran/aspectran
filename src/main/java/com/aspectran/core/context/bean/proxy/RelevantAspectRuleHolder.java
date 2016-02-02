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
package com.aspectran.core.context.bean.proxy;

import java.util.List;

import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.rule.AspectRule;

/**
 * The Class RelevantAspectRuleHolder.
 */
public class RelevantAspectRuleHolder {

	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	private List<AspectRule> activityAspectRuleList;

	/**
	 * Gets the aspect advice rule registry.
	 *
	 * @return the aspect advice rule registry
	 */
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}

	/**
	 * Sets the aspect advice rule registry.
	 *
	 * @param aspectAdviceRuleRegistry the new aspect advice rule registry
	 */
	public void setAspectAdviceRuleRegistry(
			AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
	}

	/**
	 * Gets the activity aspect rule list.
	 *
	 * @return the activity aspect rule list
	 */
	public List<AspectRule> getActivityAspectRuleList() {
		return activityAspectRuleList;
	}

	/**
	 * Sets the activity aspect rule list.
	 *
	 * @param activityAspectRuleList the new activity aspect rule list
	 */
	public void setActivityAspectRuleList(List<AspectRule> activityAspectRuleList) {
		this.activityAspectRuleList = activityAspectRuleList;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{aspectAdviceRuleRegistry=").append(aspectAdviceRuleRegistry);
		sb.append(", activityAspectRuleList=").append(activityAspectRuleList);
		sb.append("}");
		
		return sb.toString();
	}
	
}
