/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.scheduler.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.quartz.JobDetail;

import com.aspectran.core.adapter.CommonResponseAdapter;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.type.ContentType;

/**
 * The Class QuartzJobResponseAdapter.
 * 
 * @since 2013. 11. 20.
 */
public class QuartzJobResponseAdapter extends CommonResponseAdapter {

	private JobDetail jobDetail;
	
	public QuartzJobResponseAdapter(JobDetail jobDetail) {
		super(null);
		this.jobDetail = jobDetail;
		setContentType(ContentType.TEXT_PLAIN.toString());
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new QuartzJobOutputStream(jobDetail);
	}

	@Override
	public Writer getWriter() throws IOException {
		String characterEncoding = getCharacterEncoding();

		OutputStream os = getOutputStream();
		Writer writer;

		if(characterEncoding != null)
			writer = new OutputStreamWriter(os, characterEncoding);
		else
			writer = new OutputStreamWriter(os);

		return writer;
	}

	@Override
	public void redirect(String target) throws IOException {
	}

	@Override
	public String redirect(RedirectResponseRule redirectResponseRule) {
		throw new UnsupportedOperationException("redirect");
	}

}
