package com.aspectran.scheduler.quartz;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.AspectranConstant;
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
	
	private List<Scheduler> startedSchedulerList = new ArrayList<Scheduler>();
	
	private Map<String, Scheduler> eachAspectSchedulerMap = new LinkedHashMap<String, Scheduler>();
	
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
		
		Date startDate = new Date();
		
		if(delaySeconds > 0) {
			startDate = new Date(startDate.getTime() + (delaySeconds * 1000L));
		}
		
		for(AspectRule aspectRule : aspectRuleMap) {
			JoinpointTargetType joinpointTarget = aspectRule.getJoinpointTarget();
			
			if(joinpointTarget == JoinpointTargetType.SCHEDULER) {
				String schedulerFactoryBeanId = aspectRule.getAdviceBeanId();
				PointcutRule pointcutRule = aspectRule.getPointcutRule();
				
				SchedulerFactory schedulerFactory = (SchedulerFactory)context.getBeanRegistry().getBean(schedulerFactoryBeanId);
				Scheduler scheduler = schedulerFactory.getScheduler();
				JobDetail[] jobDetails = buildJobDetails(aspectRule.getAspectJobAdviceRuleList());
				
				if(jobDetails.length > 0) {
					for(JobDetail jobDetail : jobDetails) {
						String triggerName = jobDetail.getKey().getName();
						String triggerGroup = aspectRule.getId();
						Trigger trigger = buildTrigger(triggerName, triggerGroup, pointcutRule, startDate);

						scheduler.scheduleJob(jobDetail, trigger);
					}
				}

				if(!scheduler.isStarted()) {
					logger.info("Now try to start scheduler '{}'.", scheduler.getSchedulerName());
					
					if(delaySeconds > 0)
						scheduler.startDelayed(delaySeconds);
					else
						scheduler.start();
					
					if(!startedSchedulerList.contains(scheduler))
						startedSchedulerList.add(scheduler);
				}

				eachAspectSchedulerMap.put(aspectRule.getId(), scheduler);
			}
		}
	}
	
	public void shutdown() throws SchedulerException {
		shutdown(false);
	}
	
	public void shutdown(boolean waitForJobsToComplete) throws SchedulerException {
		for(Scheduler scheduler : startedSchedulerList) {
			if(!scheduler.isShutdown()) {
				logger.info("Now try to stop scheduler '{}'.", scheduler.getSchedulerName());
				scheduler.shutdown(waitForJobsToComplete);
			}
		}
	}
	
	public void pause(String aspectId) throws SchedulerException {
		Scheduler scheduler = getScheduler(aspectId);
		
		if(scheduler != null && scheduler.isStarted()) {
			scheduler.pauseJobs(GroupMatcher.jobGroupEquals(aspectId));
		}
	}
	
	public void resume(String aspectId) throws SchedulerException {
		Scheduler scheduler = getScheduler(aspectId);
		
		if(scheduler != null && scheduler.isStarted()) {
			scheduler.resumeJobs(GroupMatcher.jobGroupEquals(aspectId));
		}
	}

	private Scheduler getScheduler(String aspectId) throws SchedulerException {
		return eachAspectSchedulerMap.get(aspectId);
	}
	
	private Trigger buildTrigger(String name, String group, PointcutRule pointcutRule, Date startDate) {
		Trigger trigger = null;

		if(pointcutRule.getPointcutType() == PointcutType.SIMPLE_TRIGGER) {
			SimpleScheduleBuilder simpleSchedule = SimpleScheduleBuilder.simpleSchedule();
			simpleSchedule.withRepeatCount(1);
			
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(name, group)
					.startAt(startDate)
					.withSchedule(simpleSchedule)
					.build();
		} else {
			CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(pointcutRule.getPatternString());
			
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(name, group)
					.startAt(startDate)
					.withSchedule(cronSchedule)
					.build();
			
		}
		
		return trigger;
	}
	
	private JobDetail[] buildJobDetails(List<AspectJobAdviceRule> aspectJobAdviceRuleList) {
		List<JobDetail> jobDetailList = new ArrayList<JobDetail>();
		
		for(int i = 0; i < aspectJobAdviceRuleList.size(); i++) {
			AspectJobAdviceRule aspectJobAdviceRule = (AspectJobAdviceRule)aspectJobAdviceRuleList.get(i);
			JobDetail jobDetail = buildJobDetail(aspectJobAdviceRule, i);
			
			if(jobDetail != null)
				jobDetailList.add(jobDetail);
		}
		
		return jobDetailList.toArray(new JobDetail[jobDetailList.size()]);
	}

	private JobDetail buildJobDetail(AspectJobAdviceRule aspectJobAdviceRule, int index) {
		if(aspectJobAdviceRule.isDisabled())
			return null;
		
		String jobName = index + (AspectranConstant.TRANSLET_NAME_SEPARATOR + aspectJobAdviceRule.getJobTransletName());
		String jobGroup = aspectJobAdviceRule.getAspectId();
		
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("aspectranContext", context);
		jobDataMap.put("transletName", aspectJobAdviceRule.getJobTransletName());
		
		JobDetail jobDetail = JobBuilder.newJob(TaskActivityRunJob.class)
				.withIdentity(jobName, jobGroup)
				.setJobData(jobDataMap)
				.build();
		
		return jobDetail;
	}
	
}
