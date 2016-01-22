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
package com.aspectran.console.adapter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * The Class ConsolePrintWriter.
 * 
 * @author Juho Jeong
 * @since 2016. 1. 22.
 */
public class ConsolePrintWriter extends PrintWriter {

	public ConsolePrintWriter(OutputStream out) {
		super(out, true);
	}

	public ConsolePrintWriter(OutputStream out, String characterEncoding) throws UnsupportedEncodingException {
		super(new OutputStreamWriter(out, characterEncoding), true);
	}
	
	@Override
	public void close() {
		// Do not close the console output stream until the application is terminated.
	}

}
