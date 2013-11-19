package com.aspectran.scheduler.quartz;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.aspectran.core.activity.CoreActivityException;
import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.rule.AspectJobAdviceRule;
import com.aspectran.core.rule.AspectRule;
import com.aspectran.core.rule.AspectRuleMap;
import com.aspectran.core.rule.PointcutRule;
import com.aspectran.core.type.JoinpointTargetType;
import com.aspectran.core.type.PointcutType;
import com.aspectran.scheduler.activity.SchedulingActivity;
import com.aspectran.scheduler.activity.SchedulingActivityImpl;

public class QuartzScheduler {

	private final Log log = LogFactory.getLog(QuartzScheduler.class);

	private final boolean debugEnabled = log.isDebugEnabled();
	
	private AspectranContext context;
	
	public QuartzScheduler(AspectranContext context) throws SchedulerException {
		this.context = context;
		
		register();
	}
	
	public void shutdown(String schedulerId) throws SchedulerException {
		AspectRuleMap aspectRuleMap = context.getAspectRuleMap();
		
		if(aspectRuleMap == null)
			return;
		
		AspectRule aspectRule = aspectRuleMap.get(schedulerId);
		String schedulerFactoryBeanId = aspectRule.getAdviceBeanId();
		
		if(schedulerFactoryBeanId != null) {
			SchedulerFactory schedulerFactory = (SchedulerFactory)context.getBeanRegistry().getBean(schedulerFactoryBeanId);
			
			if(schedulerFactory != null) {
				Scheduler scheduler = schedulerFactory.getScheduler();
				
				if(scheduler.isInStandbyMode())
					scheduler.shutdown();
			}
		}
	}
	
	private void register() throws SchedulerException {
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
					
					scheduler.start();
				}
			}
		}
	}
	
	private Trigger buildTrigger(String aspectId, PointcutRule pointcutRule) {
		Trigger trigger = null;
		
		if(pointcutRule.getPointcutType() == PointcutType.SIMPLE_TRIGGER) {
			String triggerName = aspectId + "." + PointcutType.SIMPLE_TRIGGER;
			String triggerGroup = aspectId;
			
			SimpleScheduleBuilder simpleSchedule = SimpleScheduleBuilder.simpleSchedule();
			simpleSchedule.withRepeatCount(1);
			
			trigger = TriggerBuilder.newTrigger()
					.withIdentity(triggerName, triggerGroup)
					.startNow()
					.withSchedule(simpleSchedule)
					.build();
		} else {
			String triggerName = aspectId + "." + PointcutType.CRON_TRIGGER;
			String triggerGroup = aspectId;
			
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
		
		final String transletName = aspectJobAdviceRule.getTriggerTransletName();
		
		Job job = new Job() {
			public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
				try {
					runActivity(transletName);
				} catch(CoreActivityException e) {
					throw new JobExecutionException(e);
				}
			}
		};
		
		JobDetail jobDetail = JobBuilder.newJob(job.getClass())
				.withIdentity("job1", "group1")
				.build();
		
		return jobDetail;
	}
	
	private void runActivity(String transletName) throws CoreActivityException {
		SchedulingActivity activity = new SchedulingActivityImpl(context);
		activity.init(transletName);
		activity.run();
	}
	
}
