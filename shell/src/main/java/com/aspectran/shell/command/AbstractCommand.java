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
import com.aspectran.shell.command.option.DefaultOptionParser;
import com.aspectran.shell.command.option.HelpFormatter;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.OptionParser;
import com.aspectran.shell.command.option.OptionParserException;
import com.aspectran.shell.command.option.Options;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.console.DefaultConsole;
import com.aspectran.shell.service.ShellService;

public abstract class AbstractCommand implements Command {

    private final CommandRegistry registry;

    protected final Options options = new Options();

    public AbstractCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    public ShellService getService() {
        ShellService service = registry.getService();
        if (service == null || !service.getServiceController().isActive()) {
            throw new IllegalStateException("SERVICE NOT AVAILABLE");
        }
        return service;
    }

    public Console getConsole() {
        if (registry != null) {
            return getService().getConsole();
        } else {
            return new DefaultConsole();
        }
    }

    public CommandRegistry getCommandRegistry() {
        return registry;
    }

    public Options getOptions() {
        return options;
    }

    protected void addOption(Option option) {
        options.addOption(option);
    }

    protected void addArguments(Arguments arguments) {
        options.addArguments(arguments);
    }

    protected Arguments touchArguments() {
        Arguments arguments = new Arguments();
        addArguments(arguments);
        return arguments;
    }

    protected ParsedOptions parse(String[] args) throws OptionParserException {
        return getParser().parse(options, args);
    }

    protected OptionParser getParser() {
        if (registry != null) {
            return registry.getParser();
        } else {
            return new DefaultOptionParser();
        }
    }

    @Override
    public void printUsage() {
        printUsage(getConsole());
    }

    @Override
    public void printUsage(Console console) {
        HelpFormatter formatter = new HelpFormatter(console);
        if (getDescriptor().getUsage() != null && getDescriptor().getUsage().length() > 0) {
            formatter.printHelp(getDescriptor().getUsage(), options, false);
        } else {
            formatter.printHelp(getDescriptor().getName(), options, true);
        }
    }

}
