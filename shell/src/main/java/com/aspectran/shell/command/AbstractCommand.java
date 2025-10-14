/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.shell.command.option.HelpFormatter;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.Options;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.service.ShellService;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for {@link Command} implementations.
 * <p>Provides access to the {@link CommandRegistry}, collects option and argument
 * metadata to drive parsing and help output, and offers convenience methods for
 * printing usage information via a {@link HelpFormatter}. Subclasses should
 * implement the {@link #execute} method and use {@link #addOption(Option)} and
 * {@link #touchArguments()} to declare their command-line contract.</p>
 */
public abstract class AbstractCommand implements Command {

    private final CommandRegistry registry;

    private final Options options = new Options();

    private final List<Arguments> argumentsList = new ArrayList<>();

    /**
     * Instantiates a new abstract command.
     * @param registry the command registry
     */
    public AbstractCommand(CommandRegistry registry) {
        if (registry == null) {
            throw new IllegalArgumentException("Command registry must not be null");
        }
        this.registry = registry;
    }

    /**
     * Returns the command registry.
     * @return the command registry
     */
    public CommandRegistry getCommandRegistry() {
        return registry;
    }

    /**
     * Returns the console commander.
     * @return the console commander
     */
    public ConsoleCommander getCommandRunner() {
        return registry.getConsoleCommander();
    }

    /**
     * Returns the shell service.
     * @return the shell service
     * @throws IllegalStateException if the shell service is not available
     */
    public ShellService getShellService() {
        ShellService shellService = (getCommandRunner() != null ? getCommandRunner().getShellService() : null);
        if (shellService == null) {
            throw new IllegalStateException("SERVICE NOT AVAILABLE");
        }
        return shellService;
    }

    /**
     * Returns the active shell service.
     * @return the active shell service
     * @throws IllegalStateException if the shell service is not active
     */
    public ShellService getActiveShellService() {
        ShellService shellService = getShellService();
        if (!shellService.getServiceLifeCycle().isActive()) {
            throw new IllegalStateException("SERVICE NOT AVAILABLE");
        }
        return shellService;
    }

    /**
     * Returns whether the shell service is available and active.
     * @return true if the shell service is active, false otherwise
     */
    public boolean isServiceAvailable() {
        return (getCommandRunner() != null && getCommandRunner().getShellService() != null &&
                getCommandRunner().getShellService().getServiceLifeCycle().isActive());
    }

    /**
     * Adds an option to the command.
     * @param option the option to add
     */
    protected void addOption(Option option) {
        options.addOption(option);
    }

    /**
     * Adds an arguments object to the command.
     * @param arguments the arguments object to add
     */
    protected void addArguments(Arguments arguments) {
        argumentsList.add(arguments);
    }

    /**
     * Creates and adds a new {@link Arguments} object to this command.
     * @return the newly created {@link Arguments} object
     */
    protected Arguments touchArguments() {
        Arguments arguments = new Arguments();
        addArguments(arguments);
        return arguments;
    }

    /**
     * Specifies that option parsing should stop at the first non-option encountered.
     */
    protected void skipParsingAtNonOption() {
        options.setSkipParsingAtNonOption(true);
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public List<Arguments> getArgumentsList() {
        return argumentsList;
    }

    @Override
    public void printHelp(ShellConsole console) {
        if (getDescriptor().getDescription() != null) {
            console.writeLine(getDescriptor().getDescription());
        }
        printQuickHelp(console);
    }

    @Override
    public void printQuickHelp(ShellConsole console) {
        HelpFormatter formatter = new HelpFormatter(console);
        formatter.printHelp(this);
    }

}
