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
package com.aspectran.core.context.aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class AspectAdviceRuleRegistry.
 */
public class AspectAdviceRuleRegistry implements Replicable<AspectAdviceRuleRegistry> {

	private Map<String, Object> settings;

	private List<SettingsAdviceRule> settingsAdviceRuleList;

	private List<AspectAdviceRule> beforeAdviceRuleList;

	private List<AspectAdviceRule> afterAdviceRuleList;

	private List<AspectAdviceRule> finallyAdviceRuleList;

	private List<ExceptionRule> exceptionRuleList;

	private int aspectRuleCount;

	@SuppressWarnings("unchecked")
	public <T> T getSetting(String settingName) {
		if(settings == null)
			return null;

		return (T)settings.get(settingName);
	}

	protected void setSettings(Map<String, Object> settings) {
		this.settings = settings;
	}

	protected void addSettings(Map<String, Object> settings) {
		if(settings != null) {
			if(this.settings == null) {
				this.settings = new HashMap<>(settings);
			} else {
				this.settings.putAll(settings);
			}
		}
	}

	protected List<SettingsAdviceRule> getSettingsAdviceRuleList() {
		return settingsAdviceRuleList;
	}

	protected void setSettingsAdviceRuleList(List<SettingsAdviceRule> settingsAdviceRuleList) {
		this.settingsAdviceRuleList = settingsAdviceRuleList;

		for(SettingsAdviceRule settingsAdviceRule : settingsAdviceRuleList) {
			addSettings(settingsAdviceRule.getSettings());
		}
	}

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

	public void addAspectAdviceRule(SettingsAdviceRule settingsAdviceRule) {
		if(settingsAdviceRuleList == null) {
			settingsAdviceRuleList = new ArrayList<>();
		}
		settingsAdviceRuleList.add(settingsAdviceRule);

		addSettings(settingsAdviceRule.getSettings());
	}

	public void addBeforeAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(beforeAdviceRuleList == null) {
			beforeAdviceRuleList = new ArrayList<>();
		}
		beforeAdviceRuleList.add(aspectAdviceRule);
	}

	public void addAfterAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(afterAdviceRuleList == null) {
			afterAdviceRuleList = new ArrayList<>();
		}
		afterAdviceRuleList.add(0, aspectAdviceRule);
	}

	public void addFinallyAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(finallyAdviceRuleList == null) {
			finallyAdviceRuleList = new ArrayList<>();
		}
		finallyAdviceRuleList.add(0, aspectAdviceRule);
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
		}
	}

	public List<ExceptionRule> getExceptionRuleList() {
		return exceptionRuleList;
	}

	public void setExceptionRuleList(List<ExceptionRule> exceptionRuleList) {
		this.exceptionRuleList = exceptionRuleList;
	}

	public void addExceptionRule(ExceptionRule exceptionRule) {
		if(exceptionRuleList == null) {
			exceptionRuleList = new ArrayList<>();
		}
		exceptionRuleList.add(0, exceptionRule);
	}

	@Override
	public AspectAdviceRuleRegistry replicate() {
		AspectAdviceRuleRegistry aarr = new AspectAdviceRuleRegistry();
		aarr.setAspectRuleCount(aspectRuleCount);

		if(settings != null) {
			Map<String, Object> newSettings = new HashMap<>(settings);
			aarr.setSettings(newSettings);
		}
		if(settingsAdviceRuleList != null) {
			aarr.setSettingsAdviceRuleList(new ArrayList<>(settingsAdviceRuleList));
		}
		if(beforeAdviceRuleList != null) {
			aarr.setBeforeAdviceRuleList(new ArrayList<>(beforeAdviceRuleList));
		}
		if(afterAdviceRuleList != null) {
			aarr.setAfterAdviceRuleList(new ArrayList<>(afterAdviceRuleList));
		}
		if(finallyAdviceRuleList != null) {
			aarr.setFinallyAdviceRuleList(new ArrayList<>(finallyAdviceRuleList));
		}
		if(exceptionRuleList != null) {
			aarr.setExceptionRuleList(new ArrayList<>(exceptionRuleList));
		}
		
		return aarr;
	}

	public int getAspectRuleCount() {
		return aspectRuleCount;
	}

	private void setAspectRuleCount(int aspectRuleCount) {
		this.aspectRuleCount = aspectRuleCount;
	}
	
	public void increaseAspectRuleCount() {
		this.aspectRuleCount++;
	}
	
	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder(94);
		tsb.appendSize("settings", settings);
		tsb.append("beforeAdvices", beforeAdviceRuleList);
		tsb.append("afterAdvices", afterAdviceRuleList);
		tsb.append("finallyAdvices", finallyAdviceRuleList);
		tsb.append("exceptionRules", exceptionRuleList);
		return tsb.toString();
	}
	
}
