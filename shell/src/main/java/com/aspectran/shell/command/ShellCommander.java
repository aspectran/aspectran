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
import com.aspectran.shell.command.builtins.QuitCommand;
import com.aspectran.shell.command.option.OptionParserException;
import com.aspectran.shell.command.option.ParsedOptions;
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
            if (this.commandRegistry.getCommand(QuitCommand.class) == null) {
                this.commandRegistry.addCommand(QuitCommand.class);
            }
        }
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

                Command command = commandRegistry.getCommand(lineParser.getCommandName());
                if (command != null) {
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
                        log.error("Failed to execute command: " + commandLine, e);
                    }
                } else {
                    TransletCommandLine transletCommandLine = new TransletCommandLine(lineParser);
                    if (transletCommandLine.getTransletName() != null) {
                        try {
                            service.execute(transletCommandLine);
                            console.writeLine();
                        } catch (TransletNotFoundException e) {
                            console.writeLine("No command or translet mapped to '" + e.getTransletName() + "'");
                        } catch (ConsoleTerminatedException e) {
                            throw e;
                        } catch (Exception e) {
                            log.error("Failed to execute command: " + commandLine, e);
                        }
                    } else {
                        console.writeLine("No command or translet mapped to '" + commandLine + "'");
                    }
                }
            }
        } catch (ConsoleTerminatedException e) {
            // Will be shutdown
        } catch (Exception e) {
            log.error("Error occurred while processing shell command", e);
        } finally {
            if (service.getServiceController().isActive()) {
                if (log.isDebugEnabled()) {
                    log.debug("Do not terminate this application while releasing all resources");
                }
            }
        }
    }

}
