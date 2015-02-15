package com.aspectran.scheduler.quartz;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.scheduler.activity.JobActivity;
import com.aspectran.scheduler.adapter.QuartzJobRequestAdapter;
import com.aspectran.scheduler.adapter.QuartzJobResponseAdapter;

public class JobActivityRunJob implements Job {
	
	public JobActivityRunJob() {
	}
	
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		try {
			JobDetail jobDetail = jobExecutionContext.getJobDetail();
			JobDataMap jobDataMap = jobDetail.getJobDataMap();
			ActivityContext context = (ActivityContext)jobDataMap.get(QuartzAspectranScheduler.ASPECTRAN_CONTEXT_DATA_KEY);
			String transletName = jobDataMap.getString(QuartzAspectranScheduler.TRANSLET_NAME_DATA_KEY);
			
			runActivity(context, transletName, jobDetail);
		} catch(ActivityException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private void runActivity(ActivityContext context, String transletName, JobDetail jobDetail) throws ActivityException {
		RequestAdapter requestAdapter = new QuartzJobRequestAdapter(jobDetail);
		ResponseAdapter responseAdapter = new QuartzJobResponseAdapter(jobDetail);
		
		Activity activity = new JobActivity(context, requestAdapter, responseAdapter);
		activity.ready(transletName);
		activity.perform();
		activity.finish();
	}
	
}
