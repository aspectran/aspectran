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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.AbstractResponseAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.type.ContentType;

/**
 * The Class QuartzJobResponseAdapter.
 * 
 * @since 2013. 11. 20.
 */
public class QuartzJobResponseAdapter extends AbstractResponseAdapter implements ResponseAdapter {

	private String characterEncoding;
	
	private JobDetail jobDetail;
	
	public QuartzJobResponseAdapter(JobDetail jobDetail) {
		super(null);
		
		this.jobDetail = jobDetail;
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	@Override
	public String getContentType() {
		return ContentType.TEXT_PLAIN.toString();
	}

	@Override
	public void setContentType(String contentType) {
		// only text/plain
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
	public void redirect(String requestUri) throws IOException {
	}

	@Override
	public String redirect(Activity activity, RedirectResponseRule redirectResponseRule) throws IOException {
		throw new UnsupportedOperationException("redirect");
	}

}
