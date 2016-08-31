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
package com.aspectran.scheduler.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.apon.params.TriggerParameters;
import com.aspectran.core.context.rule.JobRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.type.TriggerType;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class QuartzSchedulerService.
 */
public class QuartzSchedulerService implements SchedulerService {

	public final static String ACTIVITY_CONTEXT_DATA_KEY = "ACTIVITY_CONTEXT";

	public final static String TRANSLET_NAME_DATA_KEY = "TRANSLET_NAME";
	
	private final Log log = LogFactory.getLog(QuartzSchedulerService.class);

	private ActivityContext context;

	private List<Scheduler> startedSchedulerList = new ArrayList<Scheduler>();
	
	private Map<String, Scheduler> eachAspectSchedulerMap = new LinkedHashMap<>();
	
	private int startDelaySeconds = 0;
	
	private boolean waitOnShutdown = false;
	
	public QuartzSchedulerService(ActivityContext context) {
		this.context = context;
	}

	@Override
	public int getStartDelaySeconds() {
		return startDelaySeconds;
	}

	@Override
	public void setStartDelaySeconds(int startDelaySeconds) {
		this.startDelaySeconds = startDelaySeconds;
	}

	@Override
	public boolean isWaitOnShutdown() {
		return waitOnShutdown;
	}

	@Override
	public void setWaitOnShutdown(boolean waitOnShutdown) {
		this.waitOnShutdown = waitOnShutdown;
	}

	@Override
	public void startup(int delaySeconds) throws SchedulerServiceException {
		this.startDelaySeconds = delaySeconds;
		startup();
	}

	@Override
	public void startup() throws SchedulerServiceException {
		System.out.println("==" + context.getScheduleRuleRegistry());
		if(context.getScheduleRuleRegistry() == null)
			return;

		Map<String, ScheduleRule> scheduleRuleMap = context.getScheduleRuleRegistry().getScheduleRuleMap();
		if(scheduleRuleMap == null)
			return;
		
		try {
			for(ScheduleRule scheduleRule : scheduleRuleMap.values()) {
				String schedulerBeanId = scheduleRule.getSchedulerBeanId();
				
				SchedulerFactory schedulerFactory = context.getBeanRegistry().getBean(schedulerBeanId);
				Scheduler scheduler = schedulerFactory.getScheduler();
				JobDetail[] jobDetails = buildJobDetails(scheduleRule.getJobRuleList());
				
				if(jobDetails.length > 0) {
					for(JobDetail jobDetail : jobDetails) {
						String triggerName = jobDetail.getKey().getName();
						String triggerGroup = scheduleRule.getId();
						Trigger trigger = buildTrigger(triggerName, triggerGroup, scheduleRule);

						scheduler.scheduleJob(jobDetail, trigger);
					}
				}

				if(!startedSchedulerList.contains(scheduler) && !scheduler.isStarted()) {
					log.info("Now try to start scheduler '" + scheduler.getSchedulerName() + "'.");
					
					if(startDelaySeconds > 0) {
						scheduler.startDelayed(startDelaySeconds);
					} else {
						scheduler.start();
					}
					
					startedSchedulerList.add(scheduler);
				}

				eachAspectSchedulerMap.put(scheduleRule.getId(), scheduler);
			}

			log.info("SchedulerService was started successfully.");
		} catch(Exception e) {
			throw new SchedulerServiceException("QuartzSchedulerService startup failed.", e);
		}
	}

	@Override
	public void shutdown(boolean waitForJobsToComplete) throws SchedulerServiceException {
		this.waitOnShutdown = waitForJobsToComplete;
		shutdown();
	}

	@Override
	public void shutdown() throws SchedulerServiceException {
		try {
			for(Scheduler scheduler : startedSchedulerList) {
				if(!scheduler.isShutdown()) {
					log.info("Shuting down Quartz scheduler '" + scheduler.getSchedulerName() + "' with waitForJobsToComplete=" + waitOnShutdown);
					scheduler.shutdown(waitOnShutdown);
				}
			}
			startedSchedulerList.clear();
		} catch(Exception e) {
			throw new SchedulerServiceException("SchedulerService shutdown failed.", e);
		}
	}

	@Override
	public void refresh(ActivityContext context) throws SchedulerServiceException {
		this.context = context;
		shutdown();
		startup();
	}

	@Override
	public void pause(String aspectId) throws SchedulerServiceException {
		try {
			Scheduler scheduler = getScheduler(aspectId);

			if(scheduler != null && scheduler.isStarted()) {
				scheduler.pauseJobs(GroupMatcher.jobGroupEquals(aspectId));
			}
		} catch(Exception e) {
			throw new SchedulerServiceException("SchedulerService pause failed.", e);
		}
	}

	@Override
	public void resume(String aspectId) throws SchedulerServiceException {
		try {
			Scheduler scheduler = getScheduler(aspectId);

			if(scheduler != null && scheduler.isStarted()) {
				scheduler.resumeJobs(GroupMatcher.jobGroupEquals(aspectId));
			}
		} catch(Exception e) {
			throw new SchedulerServiceException("SchedulerService resume failed.", e);
		}
	}

	private Scheduler getScheduler(String aspectId) throws SchedulerException {
		return eachAspectSchedulerMap.get(aspectId);
	}
	
	private Trigger buildTrigger(String name, String group, ScheduleRule scheduleRule) {
		Trigger trigger;

		Parameters triggerParameters = scheduleRule.getTriggerParameters();
		Integer triggerStartDelaySeconds = triggerParameters.getInt(TriggerParameters.startDelaySeconds);
		int intTriggerStartDelaySeconds = (triggerStartDelaySeconds != null) ? triggerStartDelaySeconds.intValue() : 0;

		Date firstFireTime;
		if(startDelaySeconds > 0 || (triggerStartDelaySeconds != null && triggerStartDelaySeconds > 0)) {
			firstFireTime = new Date(System.currentTimeMillis() + ((startDelaySeconds + intTriggerStartDelaySeconds) * 1000L));
		} else {
			firstFireTime = new Date();
		}
		
		if(scheduleRule.getTriggerType() == TriggerType.SIMPLE) {
			Long intervalInMilliseconds = triggerParameters.getLong(TriggerParameters.intervalInMilliseconds);
			Integer intervalInSeconds = triggerParameters.getInt(TriggerParameters.intervalInSeconds);
			Integer intervalInMinutes = triggerParameters.getInt(TriggerParameters.intervalInMinutes);
			Integer intervalInHours = triggerParameters.getInt(TriggerParameters.intervalInHours);
			Integer repeatCount = triggerParameters.getInt(TriggerParameters.repeatCount);
			Boolean repeatForever = triggerParameters.getBoolean(TriggerParameters.repeatForever);

			SimpleScheduleBuilder builder = SimpleScheduleBuilder.simpleSchedule();

			if(intervalInMilliseconds != null)
				builder.withIntervalInMilliseconds(intervalInMilliseconds);
			if(intervalInMinutes != null)
				builder.withIntervalInMinutes(intervalInMinutes);
			if(intervalInSeconds != null)
				builder.withIntervalInSeconds(intervalInSeconds);
			if(intervalInHours != null)
				builder.withIntervalInHours(intervalInHours);
			if(repeatCount != null)
				builder.withRepeatCount(repeatCount);
			if(Boolean.TRUE.equals(repeatForever))
				builder.repeatForever();
				
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(name, group)
					.startAt(firstFireTime)
					.withSchedule(builder)
					.build();
		} else {
			String expression = triggerParameters.getString(TriggerParameters.expression);
			CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(expression);
			
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(name, group)
					.startAt(firstFireTime)
					.withSchedule(cronSchedule)
					.build();
		}
		
		return trigger;
	}
	
	private JobDetail[] buildJobDetails(List<JobRule> jobRuleList) {
		List<JobDetail> jobDetailList = new ArrayList<>(jobRuleList.size());
		
		for(int i = 0; i < jobRuleList.size(); i++) {
			JobRule jobRule = jobRuleList.get(i);
			JobDetail jobDetail = buildJobDetail(jobRule, i);
			if(jobDetail != null) {
				jobDetailList.add(jobDetail);
			}
		}
		
		return jobDetailList.toArray(new JobDetail[jobDetailList.size()]);
	}

	private JobDetail buildJobDetail(JobRule jobRule, int index) {
		if(jobRule.isDisabled())
			return null;
		
		String jobName = index + (ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR + jobRule.getTransletName());
		String jobGroup = jobRule.getScheduleRule().getId();
		
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(ACTIVITY_CONTEXT_DATA_KEY, context);
		jobDataMap.put(TRANSLET_NAME_DATA_KEY, jobRule.getTransletName());

		return JobBuilder.newJob(ActivityLauncherJob.class)
				.withIdentity(jobName, jobGroup)
				.setJobData(jobDataMap)
				.build();
	}
	
}
