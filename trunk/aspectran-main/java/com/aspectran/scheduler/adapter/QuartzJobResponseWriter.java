package com.aspectran.scheduler.adapter;

import java.io.StringWriter;

import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.AspectranConstant;

public class QuartzJobResponseWriter extends StringWriter {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(QuartzJobResponseWriter.class);

	private JobDetail jobDetail;
	
	public QuartzJobResponseWriter(JobDetail jobDetail) {
		super();
		
		this.jobDetail = jobDetail;
	}
	
	@Override
	public void flush() {
		StringBuilder sb = new StringBuilder();
		sb.append("results of job [").append(jobDetail.getJobDataMap().get("transletName")).append("]");
		sb.append(AspectranConstant.LINE_SEPARATOR).append(toString());
		
		logger.info(sb.toString());
	}

}
