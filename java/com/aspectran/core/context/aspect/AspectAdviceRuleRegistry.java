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
package com.aspectran.core.context.aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;

public class AspectAdviceRuleRegistry implements Cloneable {

	private Map<String, Object> settings;
	
	private List<AspectAdviceRule> beforeAdviceRuleList;
	
	private List<AspectAdviceRule> afterAdviceRuleList;
	
	private List<AspectAdviceRule> finallyAdviceRuleList;
	
	private List<AspectAdviceRule> exceptionRaisedAdviceRuleList;
	
	private int aspectRuleCount;

	public List<AspectAdviceRule> getBeforeAdviceRuleList() {
		return beforeAdviceRuleList;
	}

	public void setBeforeAdviceRuleList(List<AspectAdviceRule> beforeAdviceRuleList) {
		this.beforeAdviceRuleList = beforeAdviceRuleList;
	}

	public List<AspectAdviceRule> getAfterAdviceRuleList() {
		return afterAdviceRuleList;
	}

	public void setAfterAdviceRuleList(List<AspectAdviceRule> afterAdviceRuleList) {
		this.afterAdviceRuleList = afterAdviceRuleList;
	}

	public List<AspectAdviceRule> getFinallyAdviceRuleList() {
		return finallyAdviceRuleList;
	}

	public void setFinallyAdviceRuleList(List<AspectAdviceRule> finallyAdviceRuleList) {
		this.finallyAdviceRuleList = finallyAdviceRuleList;
	}

	public List<AspectAdviceRule> getExceptionRaisedAdviceRuleList() {
		return exceptionRaisedAdviceRuleList;
	}

	public void setExceptionRaisedAdviceRuleList(List<AspectAdviceRule> exceptionRaisedAdviceRuleList) {
		this.exceptionRaisedAdviceRuleList = exceptionRaisedAdviceRuleList;
	}

	public void addAspectAdviceRule(SettingsAdviceRule settingsAdviceRule) {
		if(settingsAdviceRule.getSettings() != null) {
			if(settings == null)
				settings = new HashMap<String, Object>();

			settings.putAll(settingsAdviceRule.getSettings());
		}
	}
	
	public void addBeforeAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(beforeAdviceRuleList == null)
			beforeAdviceRuleList = new ArrayList<AspectAdviceRule>();
		
		beforeAdviceRuleList.add(aspectAdviceRule);
	}
		
	public void addAfterAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(afterAdviceRuleList == null)
			afterAdviceRuleList = new ArrayList<AspectAdviceRule>();
		
		afterAdviceRuleList.add(0, aspectAdviceRule);
	}
	
	public void addFinallyAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(finallyAdviceRuleList == null)
			finallyAdviceRuleList = new ArrayList<AspectAdviceRule>();
		
		finallyAdviceRuleList.add(0, aspectAdviceRule);
	}
	
	public void addExceptionRaisedAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(exceptionRaisedAdviceRuleList == null)
			exceptionRaisedAdviceRuleList = new ArrayList<AspectAdviceRule>();
		
		exceptionRaisedAdviceRuleList.add(0, aspectAdviceRule);
	}
	
	public void addAspectAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
			addBeforeAdviceRule(aspectAdviceRule);
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER) {
			addAfterAdviceRule(aspectAdviceRule);
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY) {
			addFinallyAdviceRule(aspectAdviceRule);
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AROUND) {
			addBeforeAdviceRule(aspectAdviceRule);
			addAfterAdviceRule(aspectAdviceRule);
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.EXCPETION_RAISED) {
			addExceptionRaisedAdviceRule(aspectAdviceRule);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getSetting(String settingName) {
		if(settings == null)
			return null;
		
		return (T)settings.get(settingName);
	}
	
	public AspectAdviceRuleRegistry clone() throws CloneNotSupportedException {
		// shallow copy
		return (AspectAdviceRuleRegistry)super.clone();              
	}

	public int getAspectRuleCount() {
		return aspectRuleCount;
	}

	public void setAspectRuleCount(int aspectRuleCount) {
		this.aspectRuleCount = aspectRuleCount;
	}
	
	public void increaseAspectRuleCount() {
		this.aspectRuleCount++;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{settings=").append(settings != null ? settings.size() : 0);
		sb.append(", beforeAdvices=").append(beforeAdviceRuleList != null ? beforeAdviceRuleList.size() : 0);
		sb.append(", afterAdvices=").append(afterAdviceRuleList != null ? afterAdviceRuleList.size() : 0);
		sb.append(", finallyAdvices=").append(finallyAdviceRuleList != null ? finallyAdviceRuleList.size() : 0);
		sb.append(", exceptionRaisedAdvices=").append(exceptionRaisedAdviceRuleList != null ? exceptionRaisedAdviceRuleList.size() : 0);
		sb.append("}");
		
		return sb.toString();
	}
	
}
