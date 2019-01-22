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
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.command.option.OptionParserException;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.service.ShellService;

/**
 * The Shell Command Interpreter.
 *
 * <p>Created: 2017. 6. 3.</p>
 */
public class ShellCommandInterpreter {

    private static final Log log = LogFactory.getLog(ShellCommandInterpreter.class);

    private final Console console;

    private CommandRegistry commandRegistry;

    private ShellService service;

    public ShellCommandInterpreter(Console console) {
        if (console == null) {
            throw new IllegalArgumentException("console must not be null");
        }

        this.console = console;
    }

    public Console getConsole() {
        return console;
    }

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public void setCommandRegistry(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    public ShellService getService() {
        return service;
    }

    public void setService(ShellService service) {
        this.service = service;
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
            String result = command.execute(options);
            if (result != null) {
                console.writeLine(result);
            }
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

}
