package com.aspectran.scheduler.adapter;

import java.io.StringWriter;

import org.quartz.JobDetail;

import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.scheduler.quartz.QuartzAspectranScheduler;

/**
 * The Class QuartzJobResponseWriter.
 */
public class QuartzJobResponseWriter extends StringWriter {

	/** The log. */
	private final Log log = LogFactory.getLog(QuartzJobResponseWriter.class);

	private JobDetail jobDetail;
	
	public QuartzJobResponseWriter(JobDetail jobDetail) {
		super();
		
		this.jobDetail = jobDetail;
	}
	
	@Override
	public void flush() {
		StringBuilder sb = new StringBuilder();
		sb.append("results of job [");
		sb.append(jobDetail.getJobDataMap().get(QuartzAspectranScheduler.TRANSLET_NAME_DATA_KEY)).append("]");
		sb.append(AspectranConstant.LINE_SEPARATOR);
		sb.append(toString());
		
		log.info(sb.toString());
	}

}
