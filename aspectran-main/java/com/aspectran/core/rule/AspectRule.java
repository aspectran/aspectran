/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.rule;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.type.JoinpointScopeType;
import com.aspectran.core.type.JoinpointTargetType;

public class AspectRule {

	private String id;

	private JoinpointTargetType joinpointTarget;
	
	private JoinpointScopeType joinpointScope;
	
	private PointcutRule pointcutRule;
	
	private String adviceBeanId;
	
	private SettingsAdviceRule settingsAdviceRule;
	
	private List<AspectAdviceRule> aspectAdviceRuleList;
	
	private List<AspectJobAdviceRule> aspectJobAdviceRuleList; // for scheduling aspects
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public JoinpointTargetType getJoinpointTarget() {
		return joinpointTarget;
	}

	public void setJoinpointTarget(JoinpointTargetType joinpointTarget) {
		this.joinpointTarget = joinpointTarget;
	}

	public JoinpointScopeType getJoinpointScope() {
		return joinpointScope;
	}

	public void setJoinpointScope(JoinpointScopeType joinpointScope) {
		this.joinpointScope = joinpointScope;
	}

	public PointcutRule getPointcutRule() {
		return pointcutRule;
	}

	public void setPointcutRule(PointcutRule pointcutRule) {
		this.pointcutRule = pointcutRule;
	}

	public String getAdviceBeanId() {
		return adviceBeanId;
	}

	public void setAdviceBeanId(String adviceBeanId) {
		this.adviceBeanId = adviceBeanId;
	}

	public SettingsAdviceRule getSettingsAdviceRule() {
		return settingsAdviceRule;
	}

	public void setSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule) {
		this.settingsAdviceRule = settingsAdviceRule;
	}

	public List<AspectAdviceRule> getAspectAdviceRuleList() {
		return aspectAdviceRuleList;
	}

	public void setAspectAdviceRuleList(List<AspectAdviceRule> aspectAdviceRuleList) {
		this.aspectAdviceRuleList = aspectAdviceRuleList;
	}
	
	public void addAspectAdviceRule(AspectAdviceRule aar) {
		if(aspectAdviceRuleList == null)
			aspectAdviceRuleList = new ArrayList<AspectAdviceRule>();
		
		aspectAdviceRuleList.add(aar);
	}

	public List<AspectJobAdviceRule> getAspectJobAdviceRuleList() {
		return aspectJobAdviceRuleList;
	}

	public void setAspectJobAdviceRuleList(List<AspectJobAdviceRule> aspectJobAdviceRuleList) {
		this.aspectJobAdviceRuleList = aspectJobAdviceRuleList;
	}
	
	public void addAspectJobAdviceRule(AspectJobAdviceRule atar) {
		if(aspectJobAdviceRuleList == null)
			aspectJobAdviceRuleList = new ArrayList<AspectJobAdviceRule>();
		
		aspectJobAdviceRuleList.add(atar);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{id=").append(id);
		sb.append(", joinpointTarget=").append(joinpointTarget);
		sb.append(", joinpointScope=").append(joinpointScope);
		sb.append(", pointcutRule=").append(pointcutRule);
		if(joinpointTarget == JoinpointTargetType.TRANSLET) {
			sb.append(", settingsAdviceRule=").append(settingsAdviceRule);
			sb.append(", aspectAdviceRuleList=").append(aspectAdviceRuleList);
		} else if(joinpointTarget == JoinpointTargetType.SCHEDULER) {
			sb.append(", aspectTriggerAdviceRuleList=").append(aspectJobAdviceRuleList);
		}
		sb.append("}");
		
		return sb.toString();
	}
	
}
