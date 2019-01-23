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

import com.aspectran.core.component.translet.TransletNotFoundException;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ShellConfig;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.command.builtins.QuitCommand;
import com.aspectran.shell.command.option.OptionParserException;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.service.AspectranShellService;
import com.aspectran.shell.service.ShellService;

import java.io.File;

/**
 * The Shell Command Interpreter.
 *
 * <p>Created: 2017. 6. 3.</p>
 */
public class ShellCommandInterpreter implements CommandInterpreter {

    private static final Log log = LogFactory.getLog(ShellCommandInterpreter.class);

    private final Console console;

    private ShellCommandRegistry commandRegistry;

    private AspectranShellService service;

    public ShellCommandInterpreter(Console console) {
        if (console == null) {
            throw new IllegalArgumentException("console must not be null");
        }
        this.console = console;
    }

    @Override
    public Console getConsole() {
        return console;
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    public ShellService getService() {
        return service;
    }

    public void init(String basePath, File aspectranConfigFile) throws Exception {
        AspectranConfig aspectranConfig = new AspectranConfig();
        try {
            AponReader.parse(aspectranConfigFile, aspectranConfig);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse aspectran config file: " +
                    aspectranConfigFile, e);
        }
        if (basePath != null) {
            aspectranConfig.updateBasePath(basePath);
        }

        ShellConfig shellConfig = aspectranConfig.touchShellConfig();
        String commandPrompt = shellConfig.getString(ShellConfig.prompt);
        if (commandPrompt != null) {
            console.setCommandPrompt(commandPrompt);
        }

        if (aspectranConfig.isValueAssigned(AspectranConfig.context)) {
            service = AspectranShellService.create(aspectranConfig, console);
            service.start();
        } else {
            String greetings = shellConfig.getString(ShellConfig.greetings);
            if (StringUtils.hasText(greetings)) {
                console.writeLine(greetings);
            }
        }

        commandRegistry = new ShellCommandRegistry(this);
        commandRegistry.addCommand(shellConfig.getStringArray(ShellConfig.commands));
        if (commandRegistry.getCommand(QuitCommand.class) == null) {
            commandRegistry.addCommand(QuitCommand.class);
        }

        console.setInterpreter(this);
    }

    public void perform() {
        try {
            for (;;) {
                String commandLine = console.readCommandLine();
                if (!StringUtils.hasLength(commandLine)) {
                    continue;
                }

                CommandLineParser lineParser = new CommandLineParser(commandLine);
                if (lineParser.getCommandName() == null) {
                    continue;
                }

                Command command = null;
                if (commandRegistry != null) {
                    command = commandRegistry.getCommand(lineParser.getCommandName());
                }
                if (command != null) {
                    execute(command, lineParser);
                } else if (service != null) {
                    TransletCommandLine transletCommandLine = new TransletCommandLine(lineParser);
                    execute(transletCommandLine);
                } else {
                    console.writeLine("No command mapped to '" + lineParser.getCommandName() + "'");
                }
            }
        } catch (ConsoleTerminatedException e) {
            // Will be shutdown
        } catch (Exception e) {
            log.error("Error occurred while processing shell command", e);
        } finally {
            if (service != null && service.getServiceController().isActive()) {
                if (log.isDebugEnabled()) {
                    log.debug("Do not terminate this application while releasing all resources");
                }
            }
        }
    }

    /**
     * Executes a command built into Aspectran Shell.
     *
     * @param command an instance of the built-in command to be executed
     * @param lineParser the command line parser
     */
    private void execute(Command command, CommandLineParser lineParser) {
        try {
            ParsedOptions options = lineParser.getParsedOptions(command.getOptions());
            command.execute(options);
        } catch (ConsoleTerminatedException e) {
            throw e;
        } catch (OptionParserException e) {
            console.setStyle("RED");
            console.writeLine(e.getMessage());
            console.offStyle();
            command.printUsage();
        } catch (Exception e) {
            log.error("Failed to execute command: " + lineParser.getCommandLine(), e);
        }
    }

    /**
     * Executes a Translet defined in Aspectran.
     *
     * @param transletCommandLine the {@code TransletCommandLine} instance
     */
    private void execute(TransletCommandLine transletCommandLine) {
        if (transletCommandLine.getTransletName() != null) {
            try {
                service.execute(transletCommandLine);
                console.writeLine();
            } catch (TransletNotFoundException e) {
                console.writeLine("No command or translet mapped to '" + e.getTransletName() + "'");
            } catch (ConsoleTerminatedException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failed to execute command: " +
                        transletCommandLine.getLineParser().getCommandLine(), e);
            }
        } else {
            console.writeLine("No command or translet mapped to '" +
                    transletCommandLine.getLineParser().getCommandLine() + "'");
        }
    }

    public void release() {
        if (service != null) {
            service.stop();
            service = null;
        }
    }

}
