package com.aspectran.scheduler.quartz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.rule.AspectJobAdviceRule;
import com.aspectran.core.rule.AspectRule;
import com.aspectran.core.rule.AspectRuleMap;
import com.aspectran.core.rule.PointcutRule;
import com.aspectran.core.type.JoinpointTargetType;
import com.aspectran.core.type.PointcutType;
import com.aspectran.scheduler.AspectranScheduler;

public class QuartzAspectranScheduler implements AspectranScheduler {

	private final Logger logger = LoggerFactory.getLogger(QuartzAspectranScheduler.class);

	private AspectranContext context;
	
	public QuartzAspectranScheduler(AspectranContext context) {
		this.context = context;
	}
	
	public void startup() throws SchedulerException {
		startup(0);
	}
	
	public void startup(int delaySeconds) throws SchedulerException {
		AspectRuleMap aspectRuleMap = context.getAspectRuleMap();
		
		if(aspectRuleMap == null)
			return;
		
		for(AspectRule aspectRule : aspectRuleMap) {
			JoinpointTargetType joinpointTarget = aspectRule.getJoinpointTarget();
			
			if(joinpointTarget == JoinpointTargetType.SCHEDULER) {
				String schedulerFactoryBeanId = aspectRule.getAdviceBeanId();
				PointcutRule pointcutRule = aspectRule.getPointcutRule();
				
				SchedulerFactory schedulerFactory = (SchedulerFactory)context.getBeanRegistry().getBean(schedulerFactoryBeanId);
				Scheduler scheduler = schedulerFactory.getScheduler();
				Trigger trigger = buildTrigger(aspectRule.getId(), pointcutRule);
				JobDetail[] jobDetails = buildJobDetails(aspectRule.getAspectJobAdviceRuleList());
				
				if(jobDetails.length > 0) {
					for(JobDetail jobDetail : jobDetails) {
						scheduler.scheduleJob(jobDetail, trigger);
					}
					
					if(!scheduler.isStarted()) {
						logger.info("scheduler starts... " + scheduler);
						
						if(delaySeconds > 0)
							scheduler.startDelayed(delaySeconds);
						else
							scheduler.start();
					}
				}
			}
		}
	}
	
	public void shutdown() throws SchedulerException {
		shutdown(false);
	}
	
	public void shutdown(boolean waitForJobsToComplete) throws SchedulerException {
		AspectRuleMap aspectRuleMap = context.getAspectRuleMap();
		
		if(aspectRuleMap == null)
			return;
		
		for(AspectRule aspectRule : aspectRuleMap) {
			JoinpointTargetType joinpointTarget = aspectRule.getJoinpointTarget();
			
			if(joinpointTarget == JoinpointTargetType.SCHEDULER) {
				String schedulerFactoryBeanId = aspectRule.getAdviceBeanId();
				
				SchedulerFactory schedulerFactory = (SchedulerFactory)context.getBeanRegistry().getBean(schedulerFactoryBeanId);
				Collection<Scheduler> schedulers = schedulerFactory.getAllSchedulers();
				
				if(schedulers != null && schedulers.size() > 0) {
					for(Scheduler scheduler : schedulers) {
						if(!scheduler.isShutdown())
							scheduler.shutdown(waitForJobsToComplete);
					}
				}
			}
		}
	}
	
	public void pause(String schedulerId) throws SchedulerException {
		Scheduler scheduler = getScheduler(schedulerId);
		
		if(scheduler != null && scheduler.isStarted()) {
			scheduler.pauseJobs(GroupMatcher.jobGroupEquals(schedulerId));
		}
	}
	
	public void resume(String schedulerId) throws SchedulerException {
		Scheduler scheduler = getScheduler(schedulerId);
		
		if(scheduler != null && scheduler.isStarted()) {
			scheduler.resumeJobs(GroupMatcher.jobGroupEquals(schedulerId));
		}
	}

	private Scheduler getScheduler(String schedulerId) throws SchedulerException {
		AspectRuleMap aspectRuleMap = context.getAspectRuleMap();
		
		if(aspectRuleMap == null)
			return null;
		
		Scheduler scheduler = null;
		
		AspectRule aspectRule = aspectRuleMap.get(schedulerId);
		
		if(aspectRule.getId().equals(schedulerId)) {
			String schedulerFactoryBeanId = aspectRule.getAdviceBeanId();
			
			if(schedulerFactoryBeanId != null) {
				SchedulerFactory schedulerFactory = (SchedulerFactory)context.getBeanRegistry().getBean(schedulerFactoryBeanId);
				
				if(schedulerFactory != null) {
					scheduler = schedulerFactory.getScheduler();
				}
			}
		}
		
		return scheduler;
	}
	
	private Trigger buildTrigger(String aspectId, PointcutRule pointcutRule) {
		Trigger trigger = null;
		String triggerName = aspectId;
		String triggerGroup = aspectId;
		
		if(pointcutRule.getPointcutType() == PointcutType.SIMPLE_TRIGGER) {
			SimpleScheduleBuilder simpleSchedule = SimpleScheduleBuilder.simpleSchedule();
			simpleSchedule.withRepeatCount(1);
			
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(triggerName, triggerGroup)
					.startNow()
					.withSchedule(simpleSchedule)
					.build();
		} else {
			CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(pointcutRule.getPatternString());
			
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(triggerName, triggerGroup)
					.startNow()
					.withSchedule(cronSchedule)
					.build();
			
		}
		
		return trigger;
	}
	
	private JobDetail[] buildJobDetails(List<AspectJobAdviceRule> aspectJobAdviceRuleList) {
		List<JobDetail> jobDetailList = new ArrayList<JobDetail>();
		
		for(int i = 0; i < aspectJobAdviceRuleList.size(); i++) {
			AspectJobAdviceRule aspectJobAdviceRule = (AspectJobAdviceRule)aspectJobAdviceRuleList.get(i);
			JobDetail jobDetail = buildJobDetail(aspectJobAdviceRule);
			
			if(jobDetail != null)
				jobDetailList.add(jobDetail);
		}
		
		return jobDetailList.toArray(new JobDetail[jobDetailList.size()]);
	}

	private JobDetail buildJobDetail(AspectJobAdviceRule aspectJobAdviceRule) {
		if(aspectJobAdviceRule.isDisabled())
			return null;
		
		String jobName = aspectJobAdviceRule.getJobTransletName();
		String jobGroup = aspectJobAdviceRule.getAspectId();
		
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("aspectranContext", context);
		jobDataMap.put("transletName", aspectJobAdviceRule.getJobTransletName());
		
		JobDetail jobDetail = JobBuilder.newJob(ScheduleActivityRunJob.class)
				.withIdentity(jobName, jobGroup)
				.setJobData(jobDataMap)
				.build();
		
		return jobDetail;
	}
	
}
