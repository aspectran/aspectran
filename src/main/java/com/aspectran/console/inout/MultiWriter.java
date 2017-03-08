package com.aspectran.console.inout;

import java.io.IOException;
import java.io.Writer;

/**
 * <p>Created: 2017. 3. 9.</p>
 */
public class MultiWriter extends Writer {

	private final Writer[] writers;

	public MultiWriter(Writer[] writers) {
		this.writers = writers;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		for (Writer writer : writers) {
			writer.write(cbuf, off, len);
		}
	}

	@Override
	public void flush() throws IOException {
		for (Writer writer : writers) {
			writer.flush();
		}
	}

	@Override
	public void close() throws IOException {
		for (Writer writer : writers) {
			writer.close();
		}
	}

}
