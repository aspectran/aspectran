package com.aspectran.scheduler.quartz;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.aspectran.core.activity.CoreActivityException;
import com.aspectran.core.context.AspectranContext;
import com.aspectran.scheduler.activity.ScheduleActivity;
import com.aspectran.scheduler.activity.ScheduleActivityImpl;

public class ScheduleActivityRunJob implements Job {
	
	public ScheduleActivityRunJob() {
	}
	
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		try {
			JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
			AspectranContext context = (AspectranContext)jobDataMap.get("aspectranContext");
			String transletName = jobDataMap.getString("transletName");
			
			runActivity(context, transletName);
		} catch(CoreActivityException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private void runActivity(AspectranContext context, String transletName) throws CoreActivityException {
		ScheduleActivity activity = new ScheduleActivityImpl(context);
		activity.init(transletName);
		activity.run();
	}
	
}
