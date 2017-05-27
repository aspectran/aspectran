/*
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

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Console I/O implementation that supports System Console.
 *
 * <p>Created: 2017. 3. 4.</p>
 */
public class SystemConsoleInout extends AbstractConsoleInout {

    @Override
    public String readCommand() {
        return System.console().readLine(getCommandPrompt());
    }

    @Override
    public String readLine() {
        return System.console().readLine();
    }

    @Override
    public String readLine(String prompt) {
        return System.console().readLine(prompt);
    }

    @Override
    public String readLine(String format, Object... args) {
        return readLine(String.format(format, args));
    }

    @Override
    public String readPassword() {
        return new String(System.console().readPassword());
    }

    @Override
    public String readPassword(String prompt) {
        return new String(System.console().readPassword(prompt));
    }

    @Override
    public String readPassword(String format, Object... args) {
        return new String(System.console().readPassword(String.format(format, args)));
    }

    @Override
    public void write(String string) {
        System.out.print(string);
    }

    @Override
    public void write(String format, Object ...args) {
        System.console().format(format, args);
    }

    @Override
    public void writeLine(String string) {
        System.out.println(string);
    }

    @Override
    public void writeLine(String format, Object ...args) {
        write(format, args);
        System.out.println();
    }

    @Override
    public void writeLine() {
        System.out.println();
    }

    @Override
    public void flush() {
        System.out.flush();
    }

    @Override
    public String getEncoding() {
        return Charset.defaultCharset().name();
    }

    public OutputStream getOutput() {
        return System.out;
    }

    @Override
    public Writer getWriter() {
        return System.console().writer();
    }

    @Override
    public void setStyle(String... styles) {
        // Do Nothing
    }

    @Override
    public void offStyle() {
        // Do Nothing
    }

}
