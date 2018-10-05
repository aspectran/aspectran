/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.command.builtin.QuitCommand;
import com.aspectran.shell.command.option.OptionParserException;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.service.ShellService;

/**
 * The Shell Command Handler.
 *
 * <p>Created: 2017. 6. 3.</p>
 */
public class ShellCommander {

    private static final Log log = LogFactory.getLog(ShellCommander.class);

    private final ShellService service;

    private final Console console;

    private final CommandRegistry commandRegistry;

    public ShellCommander(ShellService service) {
        this.service = service;
        this.console = service.getConsole();
        if (service.getCommandRegistry() != null) {
            this.commandRegistry = service.getCommandRegistry();
        } else {
            this.commandRegistry = new CommandRegistry(service);
            this.commandRegistry.addCommand(QuitCommand.class);
        }
    }

    public void perform() {
        try {
            for (;;) {
                String commandLine = console.readCommandLine();
                if (commandLine == null) {
                    continue;
                }
                commandLine = commandLine.trim();
                if (commandLine.isEmpty()) {
                    continue;
                }

                CommandLineParser commandLineParser = CommandLineParser.parse(commandLine);
                String commandName = commandLineParser.getCommandName();
                String[] args = commandLineParser.getArgs();
                Command command = commandRegistry.getCommand(commandName);
                if (command != null) {
                    String result;
                    try {
                        result = command.execute(args);
                        if (result != null) {
                            console.writeLine(result);
                        }
                    } catch (ConsoleTerminatedException e) {
                        throw e;
                    } catch (OptionParserException e) {
                        console.writeLine(e.getMessage());
                        command.printUsage();
                    } catch (Exception e) {
                        log.error("Command execution failed", e);
                    }
                } else {
                    try {
                        service.execute(commandLine);
                        console.writeLine();
                    } catch (TransletNotFoundException e) {
                        console.writeLine("No command or executable translet mapped to '"
                                + e.getTransletName() +  "'");
                    } catch (ConsoleTerminatedException e) {
                        throw e;
                    } catch (Exception e) {
                        log.error("Command execution failed", e);
                    }
                }
            }
        } catch (ConsoleTerminatedException e) {
            // Will be shutdown
        } catch (Exception e) {
            log.error("Error occurred while processing shell command", e);
        } finally {
            if (service.isActive()) {
                if (log.isDebugEnabled()) {
                    log.debug("Do not terminate this application while releasing all resources");
                }
            }
        }
    }

}
