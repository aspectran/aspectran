/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.shell.command;

import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.Options;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.Console;

import java.io.PrintWriter;
import java.util.List;

public class CommandWrapper implements Command {

    private final Command command;

    private PrintWriter writer;

    public CommandWrapper(Command command) {
        this.command = command;
    }

    public void setOutputWriter(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public Options getOptions() {
        return command.getOptions();
    }

    @Override
    public List<Arguments> getArgumentsList() {
        return command.getArgumentsList();
    }

    @Override
    public void execute(ParsedOptions options) throws Exception {
        command.execute(options);
    }

    @Override
    public void writeLine(String string) {
        if (writer != null) {
            writer.println();
        } else {
            command.writeLine(string);
        }
    }

    @Override
    public void writeLine(String format, Object... args) {
        if (writer != null) {
            writer.println(String.format(format, args));
        } else {
            command.writeLine(format, args);
        }
    }

    @Override
    public void writeLine() {
        if (writer != null) {
            writer.println();
        } else {
            command.writeLine();
        }
    }

    @Override
    public void writeError(String string) {
        command.writeError(string);
    }

    @Override
    public void writeError(String format, Object... args) {
        command.writeError(format, args);
    }

    @Override
    public void setStyle(String... styles) {
        command.setStyle(styles);
    }

    @Override
    public void styleOff() {
        command.styleOff();
    }

    @Override
    public void printUsage() {
        command.printUsage();
    }

    @Override
    public void printUsage(Console console) {
        command.printUsage(console);
    }

    @Override
    public Descriptor getDescriptor() {
        return command.getDescriptor();
    }

}
