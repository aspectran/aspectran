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

import com.aspectran.core.context.AspectranConstants;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.scheduler.service.QuartzSchedulerService;
import org.quartz.JobDetail;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The Class QuartzJobResponseOutputStream.
 */
public class QuartzJobResponseOutputStream extends OutputStream {

	private final Log log = LogFactory.getLog(QuartzJobResponseOutputStream.class);

	private JobDetail jobDetail;

	private StringBuilder out;

	public QuartzJobResponseOutputStream(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
		this.out = new StringBuilder();
	}

	public void write(int b) throws IOException {
		out.append(b);
	}

	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		if((off | len | (b.length - (len + off)) | (off + len)) < 0)
			throw new IndexOutOfBoundsException();

		for(int i = 0 ; i < len ; i++) {
			write(b[off + i]);
		}
	}

	public void flush() {
		if(out.length() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("results of job [");
			sb.append(jobDetail.getJobDataMap().get(QuartzSchedulerService.TRANSLET_NAME_DATA_KEY)).append("]");
			sb.append(AspectranConstants.LINE_SEPARATOR);
			sb.append(out);

			log.info(sb.toString());

			out.setLength(0);
		}
	}

	public void close() throws IOException {
		flush();
	}

}
