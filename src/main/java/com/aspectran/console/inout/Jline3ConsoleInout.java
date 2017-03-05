/**
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.console.inout;

import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

import org.jline.builtins.Options;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * <p>Created: 2017. 3. 4.</p>
 */
public class Jline3ConsoleInout extends AbstractConsoleInout {

	private static final String encoding = Charset.defaultCharset().name();

	private final Terminal terminal;

	private final LineReader reader;

	public Jline3ConsoleInout() throws IOException {
		this.terminal = TerminalBuilder.builder().encoding(encoding).build();
		this.reader = LineReaderBuilder.builder().appName("Aspectran Console").terminal(terminal).build();
	}

	@Override
	public String readLine() {
		return reader.readLine();
	}

	@Override
	public String readLine(String prompt) {
		return reader.readLine(prompt);
	}

	@Override
	public String readLine(String format, Object... args) {
		return reader.readLine(String.format(format, args));
	}

	@Override
	public void write(String string) {
		try {
			getWriter().write(string);
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	@Override
	public void write(String format, Object... args) {
		write(String.format(format, args));
	}

	@Override
	public void writeLine(String string) {
		write(string);
		writeLine();
	}

	@Override
	public void writeLine(String format, Object... args) {
		write(format, args);
		writeLine();
	}

	@Override
	public void writeLine() {
		write(Options.NL);
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public OutputStream getOutput() {
		return terminal.output();
	}

	@Override
	public Writer getWriter() {
		return terminal.writer();
	}

}
