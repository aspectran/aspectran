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

import com.aspectran.core.context.builder.apon.params.TriggerParameters;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.type.BeanReferrerType;
import com.aspectran.core.context.rule.type.TriggerType;
import com.aspectran.core.util.StringUtils;
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
	
	private Parameters triggerParameters;

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

	public TriggerType getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(TriggerType triggerType) {
		this.triggerType = triggerType;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public Parameters getTriggerParameters() {
		return triggerParameters;
	}

	public void setTriggerParameters(Parameters triggerParameters) {
		this.triggerParameters = triggerParameters;
	}

	public String getSchedulerBeanId() {
		return schedulerBeanId;
	}

	public void setSchedulerBeanId(String schedulerBeanId) {
		this.schedulerBeanId = schedulerBeanId;
	}

	public Class<?> getSchedulerBeanClass() {
		return schedulerBeanClass;
	}

	public void setSchedulerBeanClass(Class<?> schedulerBeanClass) {
		this.schedulerBeanClass = schedulerBeanClass;
	}

	public List<JobRule> getJobRuleList() {
		return jobRuleList;
	}

	public void setJobRuleList(List<JobRule> jobRuleList) {
		this.jobRuleList = jobRuleList;
	}

	public void addJobRule(JobRule jobRule) {
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
		tsb.append("trigger", triggerParameters);
		tsb.append("scheduler", schedulerBeanId);
		tsb.append("jobRules", jobRuleList);
		return tsb.toString();
	}
	
	public static ScheduleRule newInstance(String id) {
		ScheduleRule scheduleRule = new ScheduleRule();
		scheduleRule.setId(id);
		return scheduleRule;
	}

	public static void updateTrigger(ScheduleRule scheduleRule, String type, String text) {
		updateTriggerType(scheduleRule, type);
		if(scheduleRule.getTriggerType() == TriggerType.SIMPLE) {
			if(StringUtils.hasText(text)) {
				Parameters triggerParameters = new TriggerParameters(text);
				scheduleRule.setTriggerParameters(triggerParameters);
			}
		} else {
			String[] fields = StringUtils.tokenize(text, " ");
			if(fields.length != 6) {
				throw new IllegalArgumentException(String.format("Cron expression must consist of 6 fields (found %d in %s)", fields.length, text));
			}
			Parameters triggerParameters = new TriggerParameters();
			triggerParameters.putValue(TriggerParameters.type, TriggerType.CRON.toString());
			triggerParameters.putValue(TriggerParameters.expression, text);
			scheduleRule.setTriggerParameters(triggerParameters);
		}
	}

	public static void updateTriggerType(ScheduleRule scheduleRule, String type) {
		TriggerType triggerType;
		if(type != null) {
			triggerType = TriggerType.resolve(type);
			if(triggerType == null) {
				throw new IllegalArgumentException("Unknown trigger type '" + type + "'. Trigger type for Scheduler must be 'cron' or 'simple'.");
			}
		} else {
			triggerType = TriggerType.CRON;
		}
		scheduleRule.setTriggerType(triggerType);
	}

}
