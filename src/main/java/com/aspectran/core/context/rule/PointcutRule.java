/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.rule;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.context.builder.apon.params.PointcutParameters;
import com.aspectran.core.context.builder.apon.params.TargetParameters;
import com.aspectran.core.context.rule.type.AspectTargetType;
import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class PointcutRule.
 */
public class PointcutRule {
	
	private final PointcutType pointcutType;
	
	private List<PointcutPatternRule> pointcutPatternRuleList;
	
	private List<Parameters> targetParametersList;
	
	private Parameters simpleTriggerParameters;
	
	private Parameters cronTriggerParameters;
	
	public PointcutRule(PointcutType pointcutType) {
		this.pointcutType = pointcutType;
	}
	
	public PointcutType getPointcutType() {
		return pointcutType;
	}

	public List<PointcutPatternRule> getPointcutPatternRuleList() {
		return pointcutPatternRuleList;
	}

	public void setPointcutPatternRuleList(List<PointcutPatternRule> pointcutPatternRuleList) {
		for(PointcutPatternRule ppr : pointcutPatternRuleList) {
			ppr.setPointcutType(pointcutType);
		}

		this.pointcutPatternRuleList = pointcutPatternRuleList;
	}
	
	public synchronized void addPointcutPatternRule(List<PointcutPatternRule> pointcutPatternRuleList) {
		for(PointcutPatternRule ppr : pointcutPatternRuleList) {
			ppr.setPointcutType(pointcutType);
		}
		
		if(this.pointcutPatternRuleList == null) {
			this.pointcutPatternRuleList = pointcutPatternRuleList;
		} else {
			this.pointcutPatternRuleList.addAll(pointcutPatternRuleList);
		}
	}
	
	public void addPointcutPatternRule(PointcutPatternRule pointcutPatternRule) {
		pointcutPatternRule.setPointcutType(pointcutType);

		touchPointcutPatternRuleList();

		pointcutPatternRuleList.add(pointcutPatternRule);
	}
	
	public synchronized List<PointcutPatternRule> touchPointcutPatternRuleList() {
		if(pointcutPatternRuleList == null) {
			pointcutPatternRuleList = newPointcutPatternRuleList();
		}
		
		return pointcutPatternRuleList;
	}

	public static List<PointcutPatternRule> newPointcutPatternRuleList() {
		return new ArrayList<PointcutPatternRule>();
	}

	public List<Parameters> touchTargetParametersList() {
		if(targetParametersList == null)
			targetParametersList = new ArrayList<Parameters>();
		
		return targetParametersList;
	}

	public List<Parameters> getTargetParametersList() {
		return targetParametersList;
	}
	
	public void setTargetParametersList(List<Parameters> targetParametersList) {
		this.targetParametersList = targetParametersList;
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
		//sb.append(", targetParametersList=").append(targetParametersList);
		//sb.append(", simpleTriggerParameters=").append(simpleTriggerParameters);
		//sb.append(", cronTriggerParameters=").append(cronTriggerParameters);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static PointcutRule newInstance(AspectRule aspectRule, String type, String text) {
		if(StringUtils.hasText(text)) {
			Parameters pointcutParameters = new PointcutParameters(text);
			return newInstance(aspectRule, type, pointcutParameters);
		} else {
			return newInstance(aspectRule, type, (Parameters)null);
		}
	}
	
	public static PointcutRule newInstance(AspectRule aspectRule, String type, Parameters pointcutParameters) {
		PointcutRule pointcutRule = null;

		if(aspectRule.getAspectTargetType() == AspectTargetType.SCHEDULER) {
			PointcutType pointcutType = null;
			Parameters simpleTriggerParameters = null;
			Parameters cronTriggerParameters = null;

			if(pointcutParameters != null) {
				simpleTriggerParameters = pointcutParameters.getParameters(PointcutParameters.simpleTrigger);
				cronTriggerParameters = pointcutParameters.getParameters(PointcutParameters.cronTrigger);
	
				if(simpleTriggerParameters != null) {
					pointcutType = PointcutType.SIMPLE_TRIGGER;
				} else if(cronTriggerParameters != null) {
					pointcutType = PointcutType.CRON_TRIGGER;
				}
			}
			
			if(pointcutType == null) {
				pointcutType = PointcutType.valueOf(type);
				
				if(pointcutType != PointcutType.SIMPLE_TRIGGER && pointcutType != PointcutType.CRON_TRIGGER)
					throw new IllegalArgumentException("Unknown pointcut-type '" + type + "'. Scheduler's pointcut-type must be 'simpleTrigger' or 'cronTrigger'.");
			}
			
			if(pointcutType == PointcutType.SIMPLE_TRIGGER && simpleTriggerParameters == null)
				throw new IllegalArgumentException("Not specified 'simpleTrigger'. Scheduler's pointcut-type must be 'simpleTrigger' or 'cronTrigger'.");
			else if(pointcutType == PointcutType.CRON_TRIGGER && cronTriggerParameters == null)
				throw new IllegalArgumentException("Not specified 'cronTrigger'. Scheduler's pointcut-type must be 'simpleTrigger' or 'cronTrigger'.");
			
			pointcutRule = new PointcutRule(pointcutType);
			
			if(simpleTriggerParameters != null) {
				pointcutRule.setSimpleTriggerParameters(simpleTriggerParameters);
			} else if(cronTriggerParameters != null) {
				pointcutRule.setCronTriggerParameters(cronTriggerParameters);
			}

		} else {
			if(pointcutParameters != null) {
				PointcutType pointcutType = null;
				
				if(type == null)
					type = pointcutParameters.getString(PointcutParameters.type);
				
				if(type != null) {
					pointcutType = PointcutType.valueOf(type);
					if(pointcutType == null)
						throw new IllegalArgumentException("Unknown pointcut-type '" + type + "'. Translet's pointcut-type must be 'wildcard' or 'regexp'.");
				}
				
				pointcutRule = new PointcutRule(pointcutType);
				
				List<Parameters> targetParametersList = pointcutParameters.getParametersList(PointcutParameters.targets);
				if(targetParametersList != null) {
					for(Parameters targetParameters : targetParametersList) {
						addPointcutPatternRule(pointcutRule.touchPointcutPatternRuleList(), targetParameters);
						pointcutRule.touchTargetParametersList().add(targetParameters);
					}
				}
			}
		}

		return pointcutRule;
	}

	private static List<PointcutPatternRule> addPointcutPatternRule(List<PointcutPatternRule> pointcutPatternRuleList, Parameters targetParameters) {
		String translet = targetParameters.getString(TargetParameters.translet);
		String bean = targetParameters.getString(TargetParameters.bean);
		String method = targetParameters.getString(TargetParameters.method);
		List<Parameters> excludeTargetParametersList = targetParameters.getParametersList(TargetParameters.excludeTargets);
		
		if(StringUtils.hasLength(translet) || StringUtils.hasLength(bean) || StringUtils.hasLength(method) || (excludeTargetParametersList != null && !excludeTargetParametersList.isEmpty())) {
			PointcutPatternRule pointcutPatternRule = PointcutPatternRule.newInstance(translet, bean, method);
			
			if(excludeTargetParametersList != null && !excludeTargetParametersList.isEmpty()) {
				for(Parameters excludeTargetParameters : excludeTargetParametersList) {
					addExcludePointcutPatternRule(pointcutPatternRule, excludeTargetParameters);
				}
			}
			
			pointcutPatternRuleList.add(pointcutPatternRule);
		}
		
		List<String> plusPatternStringList = targetParameters.getStringList(TargetParameters.pluses);
		List<String> minusPatternStringList = targetParameters.getStringList(TargetParameters.minuses);
		
		List<PointcutPatternRule> minusPointcutPatternRuleList = null;
		
		if(minusPatternStringList != null && !minusPatternStringList.isEmpty()) {
			minusPointcutPatternRuleList = new ArrayList<PointcutPatternRule>(minusPatternStringList.size());
			
			for(String patternString : minusPatternStringList) {
				PointcutPatternRule pointcutPatternRule = PointcutPatternRule.parsePatternString(patternString);
				minusPointcutPatternRuleList.add(pointcutPatternRule);
			}
		}
		
		if(plusPatternStringList != null && !plusPatternStringList.isEmpty()) {
			for(String patternString : plusPatternStringList) {
				PointcutPatternRule pointcutPatternRule = PointcutPatternRule.parsePatternString(patternString);
				if(minusPointcutPatternRuleList != null)
					pointcutPatternRule.setExcludePointcutPatternRuleList(minusPointcutPatternRuleList);
				
				pointcutPatternRuleList.add(pointcutPatternRule);
			}
		}
		
		return pointcutPatternRuleList;
	}

	private static void addExcludePointcutPatternRule(PointcutPatternRule pointcutPatternRule, Parameters excludeTargetParameters) {
		String translet = excludeTargetParameters.getString(TargetParameters.translet);
		String bean = excludeTargetParameters.getString(TargetParameters.bean);
		String method = excludeTargetParameters.getString(TargetParameters.method);

		if(StringUtils.hasLength(translet) || StringUtils.hasLength(bean) || StringUtils.hasLength(method)) {
			PointcutPatternRule ppr = PointcutPatternRule.newInstance(translet, bean, method);
			pointcutPatternRule.addExcludePointcutPatternRule(ppr);
		}
	}

}
