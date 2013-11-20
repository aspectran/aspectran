package com.aspectran.scheduler.quartz;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.aspectran.core.activity.CoreActivityException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.AspectranContext;
import com.aspectran.scheduler.activity.JobActivity;
import com.aspectran.scheduler.activity.JobActivityImpl;
import com.aspectran.scheduler.adapter.QuartzJobRequestAdapter;
import com.aspectran.scheduler.adapter.QuartzJobResponseAdapter;

public class JobActivityRunJob implements Job {
	
	public JobActivityRunJob() {
	}
	
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		try {
			JobDetail jobDetail = jobExecutionContext.getJobDetail();
			JobDataMap jobDataMap = jobDetail.getJobDataMap();
			AspectranContext context = (AspectranContext)jobDataMap.get(QuartzAspectranScheduler.ASPECTRAN_CONTEXT_DATA_KEY);
			String transletName = jobDataMap.getString(QuartzAspectranScheduler.TRANSLET_NAME_DATA_KEY);
			
			runActivity(context, transletName, jobDetail);
		} catch(CoreActivityException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private void runActivity(AspectranContext context, String transletName, JobDetail jobDetail) throws CoreActivityException {
		RequestAdapter requestAdapter = new QuartzJobRequestAdapter(jobDetail);
		ResponseAdapter responseAdapter = new QuartzJobResponseAdapter(jobDetail);
		
		JobActivity activity = new JobActivityImpl(context, requestAdapter, responseAdapter);
		activity.init(transletName);
		activity.run();
	}
	
}
