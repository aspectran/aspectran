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
package com.aspectran.core.context.rule;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.context.builder.apon.params.JoinpointParameters;
import com.aspectran.core.context.builder.apon.params.PointcutTargetParameters;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.type.BeanReferrerType;
import com.aspectran.core.context.rule.type.JoinpointType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.core.context.rule.type.TriggerType;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class ScheduleRule.
 */
public class ScheduleRule implements BeanReferenceInspectable {

	private static final BeanReferrerType BEAN_REFERRER_TYPE = BeanReferrerType.SCHEDULE_RULE;

	private String id;
	
	private TriggerType triggerType;
	
	private String cronExpression;
	
	private Parameters simpleTriggerParameters;

	private String schedulerBeanId;

	private Class<?> schedulerBeanClass;
	
	private List<JobRule> jobRuleList = new ArrayList<>();
	
	private String description;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Parameters getSimpleTriggerParameters() {
		return simpleTriggerParameters;
	}

	public void setSimpleTriggerParameters(Parameters simpleTriggerParameters) {
		this.simpleTriggerParameters = simpleTriggerParameters;
	}
	
	public String getAdviceBeanId() {
		return schedulerBeanId;
	}

	public void setAdviceBeanId(String adviceBeanId) {
		this.schedulerBeanId = adviceBeanId;
	}

	public Class<?> getAdviceBeanClass() {
		return schedulerBeanClass;
	}

	public void setAdviceBeanClass(Class<?> adviceBeanClass) {
		this.schedulerBeanClass = adviceBeanClass;
	}

	public List<JobRule> getJobRuleList() {
		return jobRuleList;
	}

	public void setJobRuleList(List<JobRule> jobRuleList) {
		this.jobRuleList = jobRuleList;
	}

	public void addJobRuleList(JobRule jobRule) {
		jobRuleList.add(jobRule);
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public BeanReferrerType getBeanReferrerType() {
		return BEAN_REFERRER_TYPE;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("id", id);
		tsb.append("triggerType", triggerType);
		tsb.append("cronExpression", cronExpression);
		tsb.append("simpleTriggerParameters", simpleTriggerParameters);
		tsb.append("jobRuleList", jobRuleList);
		return tsb.toString();
	}
	
	public static ScheduleRule newInstance(String id, String usedFor) {
		AspectTargetType aspectTargetType;
		
		if(usedFor != null) {
			aspectTargetType = AspectTargetType.resolve(usedFor);
			if(aspectTargetType == null)
				throw new IllegalArgumentException("No aspect target type registered for '" + usedFor + "'.");
		} else {
			aspectTargetType = AspectTargetType.TRANSLET;
		}
		
		ScheduleRule aspectRule = new ScheduleRule();
		aspectRule.setId(id);
		aspectRule.setAspectTargetType(aspectTargetType);
		
		return aspectRule;
	}
	
	public static void updateJoinpointScope(ScheduleRule aspectRule, String scope) {
		JoinpointType joinpointScope;
		
		if(scope != null) {
			joinpointScope = JoinpointType.resolve(scope);
			if(joinpointScope == null)
				throw new IllegalArgumentException("No joinpoint scope type registered for '" + scope + "'.");
		} else {
			joinpointScope = JoinpointType.TRANSLET;
		}
		
		aspectRule.setJoinpointScope(joinpointScope);
	}
	
	public static void updateTargetMethods(ScheduleRule aspectRule, String method) {
		MethodType[] allowedMethods = null;
		if(method != null) {
			allowedMethods = MethodType.parse(method);
			if(allowedMethods == null)
				throw new IllegalArgumentException("No request method type registered for '" + method + "'.");
		}

		aspectRule.setAllowedMethods(allowedMethods);
	}
	
	public static PointcutRule updateJoinpoint(ScheduleRule aspectRule, String type, Parameters joinpointParameters) {
		if(aspectRule.getAspectTargetType() == AspectTargetType.SCHEDULER) {
			PointcutType pointcutType = null;
			Parameters simpleTriggerParameters = null;
			Parameters cronTriggerParameters = null;

			if(joinpointParameters != null) {
				simpleTriggerParameters = joinpointParameters.getParameters(JoinpointParameters.simpleTrigger);
				cronTriggerParameters = joinpointParameters.getParameters(JoinpointParameters.cronTrigger);
	
				if(simpleTriggerParameters != null) {
					pointcutType = PointcutType.SIMPLE_TRIGGER;
				} else if(cronTriggerParameters != null) {
					pointcutType = PointcutType.CRON_TRIGGER;
				}
			}
			
			if(pointcutType == null) {
				pointcutType = PointcutType.resolve(type);
				
				if(pointcutType != PointcutType.SIMPLE_TRIGGER && pointcutType != PointcutType.CRON_TRIGGER)
					throw new IllegalArgumentException("Unknown pointcut type '" + type + "'. Pointcut type for Scheduler must be 'simpleTrigger' or 'cronTrigger'.");
			}
			
			if(pointcutType == PointcutType.SIMPLE_TRIGGER && simpleTriggerParameters == null)
				throw new IllegalArgumentException("Not specified 'simpleTrigger'.");
			else if(pointcutType == PointcutType.CRON_TRIGGER && cronTriggerParameters == null)
				throw new IllegalArgumentException("Not specified 'cronTrigger'.");
			
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
					type = pointcutParameters.getString(PointcutTargetParameters.type);
				
				if(type != null) {
					pointcutType = PointcutType.resolve(type);
					if(pointcutType == null)
						throw new IllegalArgumentException("Unknown pointcut type '" + type + "'. Pointcut type for Translet must be 'wildcard' or 'regexp'.");
				}
				
				pointcutRule = new PointcutRule(pointcutType);
				
				List<Parameters> targetParametersList = pointcutParameters.getParametersList(PointcutTargetParameters.targets);
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
}
