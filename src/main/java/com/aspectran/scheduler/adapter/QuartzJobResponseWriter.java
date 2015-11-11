/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.scheduler.adapter;

import java.io.StringWriter;

import org.quartz.JobDetail;

import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.scheduler.service.QuartzSchedulerService;

/**
 * The Class QuartzJobResponseWriter.
 */
public class QuartzJobResponseWriter extends StringWriter {

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
		sb.append(jobDetail.getJobDataMap().get(QuartzSchedulerService.TRANSLET_NAME_DATA_KEY)).append("]");
		sb.append(AspectranConstant.LINE_SEPARATOR);
		sb.append(toString());
		
		log.info(sb.toString());
	}

}
