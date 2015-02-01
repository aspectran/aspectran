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
package com.aspectran.core.var.rule;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.context.builder.apon.params.CronTriggerParameters;
import com.aspectran.core.context.builder.apon.params.SimpleTriggerParameters;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.var.apon.Parameters;
import com.aspectran.core.var.type.AspectTargetType;
import com.aspectran.core.var.type.PointcutType;

public class PointcutRule {
	
	private PointcutType pointcutType;
	
	private List<PointcutPatternRule> pointcutPatternRuleList;
	
	private String patternString;
	
	private Parameters simpleTriggerParameters;
	
	private Parameters cronTriggerParameters;
	
	public PointcutType getPointcutType() {
		return pointcutType;
	}

	public void setPointcutType(PointcutType pointcutType) {
		this.pointcutType = pointcutType;
	}

	public List<PointcutPatternRule> getPointcutPatternRuleList() {
		return pointcutPatternRuleList;
	}

	public void setPointcutPatternRuleList(List<PointcutPatternRule> pointcutPatternRuleList) {
		this.pointcutPatternRuleList = pointcutPatternRuleList;
	}
	
	public void addPointcutPatternRule(PointcutPatternRule pointcutPatternRule) {
		if(pointcutPatternRuleList == null) {
			pointcutPatternRuleList = new ArrayList<PointcutPatternRule>();
			pointcutPatternRuleList.add(pointcutPatternRule);
		} else
			pointcutPatternRuleList.add(pointcutPatternRule);
	}
	
	public String getPatternString() {
		return patternString;
	}

	public void setPatternString(String patternString) {
		this.patternString = patternString;
	}
	
	public Parameters getSimpleTriggerParameters() {
		return simpleTriggerParameters;
	}

	public void setSimpleTriggerParameters(Parameters simpleTriggerParameters) {
		this.simpleTriggerParameters = simpleTriggerParameters;
	}
	
	public Parameters getCronTriggerParameters() {
		return cronTriggerParameters;
	}

	public void setCronTriggerParameters(Parameters cronTriggerParameters) {
		this.cronTriggerParameters = cronTriggerParameters;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{pointcutType=").append(pointcutType);
		sb.append(", patternString=").append(patternString);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static PointcutRule newInstance(AspectRule aspectRule, String type, String text) {
		PointcutType pointcutType = null;
		
		if(aspectRule.getAspectTargetType() == AspectTargetType.SCHEDULER) {
			pointcutType = PointcutType.valueOf(type);
			
			if(pointcutType != PointcutType.SIMPLE_TRIGGER && pointcutType != PointcutType.CRON_TRIGGER)
				throw new IllegalArgumentException("scheduler's pointcut-type must be 'simpleTrigger' or 'cronTrigger'.");
			
			if(!StringUtils.hasText(text))
				throw new IllegalArgumentException("Pointcut pattern can not be null");
		} else {
			if(type != null) {
				pointcutType = PointcutType.valueOf(type);

				if(pointcutType != PointcutType.WILDCARD && pointcutType != PointcutType.REGEXP)
					throw new IllegalArgumentException("translet's pointcut-type must be 'wildcard' or 'regexp'.");
			} else {
				pointcutType = PointcutType.WILDCARD;
			}
		}
		
		PointcutRule pointcutRule = new PointcutRule();
		pointcutRule.setPointcutType(pointcutType);
		pointcutRule.setPatternString(text);
		
		if(pointcutType == PointcutType.SIMPLE_TRIGGER) {
			Parameters simpleTriggerParameters = new SimpleTriggerParameters(text);
			pointcutRule.setSimpleTriggerParameters(simpleTriggerParameters);
		} else if(pointcutType == PointcutType.CRON_TRIGGER) {
			Parameters cronTriggerParameters = new CronTriggerParameters(text);
			pointcutRule.setCronTriggerParameters(cronTriggerParameters);
		}
		
		return pointcutRule;
	}
	
}
