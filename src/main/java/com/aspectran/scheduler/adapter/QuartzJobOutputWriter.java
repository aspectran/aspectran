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
import java.io.Writer;

/**
 * The Class QuartzJobOutputWriter.
 */
public class QuartzJobOutputWriter extends Writer {

	private final StringBuilder buffer;

	public QuartzJobOutputWriter() {
		this.buffer = new StringBuilder(128);
	}

	@Override
	public void write(int c) {
		buffer.append((char)c);
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		if((off < 0) || (off > cbuf.length) || (len < 0) ||
				((off + len) > cbuf.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if(len == 0) {
			return;
		}
		buffer.append(cbuf, off, len);
	}

	@Override
	public void write(String str) {
		buffer.append(str);
	}

	@Override
	public void write(String str, int off, int len)  {
		buffer.append(str.substring(off, off + len));
	}

	@Override
	public QuartzJobOutputWriter append(CharSequence csq) {
		if(csq == null)
			write("null");
		else
			write(csq.toString());
		return this;
	}

	@Override
	public QuartzJobOutputWriter append(CharSequence csq, int start, int end) {
		CharSequence cs = (csq == null ? "null" : csq);
		write(cs.subSequence(start, end).toString());
		return this;
	}

	@Override
	public QuartzJobOutputWriter append(char c) {
		write(c);
		return this;
	}

	@Override
	public String toString() {
		return buffer.toString();
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws IOException {
	}

}
