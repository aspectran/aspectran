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
package com.aspectran.console.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;

import com.aspectran.console.activity.ConsoleActivity;
import com.aspectran.core.adapter.GenericResponseAdapter;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.util.SystemUtils;

/**
 * The Class ConsoleResponseAdapter.
 * 
 * @author Juho Jeong
 * @since 2016. 1. 18.
 */
public class ConsoleResponseAdapter extends GenericResponseAdapter {

	private PrintStream printStream;
	
	/**
	 * Instantiates a new ConsoleResponseAdapter.
	 *
	 * @param activity the console activity
	 */
	public ConsoleResponseAdapter(ConsoleActivity activity) {
		super(activity);
		printStream = System.out;
		setCharacterEncoding(SystemUtils.getProperty("file.encoding"));
	}

	@Override
	public OutputStream getOutputStream() {
		return printStream;
	}

	@Override
	public Writer getWriter() throws IOException {
		if(getCharacterEncoding() != null)
			return new ConsolePrintWriter(printStream, getCharacterEncoding());
		else
			return new ConsolePrintWriter(printStream);
	}

	@Override
	public void redirect(String target) {
	}

	@Override
	public String redirect(RedirectResponseRule redirectResponseRule) {
		return null;
	}

}
