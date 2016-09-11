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
import java.io.Writer;

import com.aspectran.core.adapter.BasicResponseAdapter;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.type.ContentType;

/**
 * The Class QuartzJobResponseAdapter.
 * 
 * @since 2013. 11. 20.
 */
public class QuartzJobResponseAdapter extends BasicResponseAdapter {

	private QuartzJobOutputWriter writer;
	
	public QuartzJobResponseAdapter() {
		super(null);
		setContentType(ContentType.TEXT_PLAIN.toString());
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Writer getWriter() throws IOException {
		if (writer == null) {
			writer = new QuartzJobOutputWriter();
		}
		return writer;
	}

	@Override
	public void redirect(String target) throws IOException {
	}

	@Override
	public String redirect(RedirectResponseRule redirectResponseRule) {
		throw new UnsupportedOperationException("redirect");
	}

	@Override
	public void flush() {
		// nothing to do
	}

}
