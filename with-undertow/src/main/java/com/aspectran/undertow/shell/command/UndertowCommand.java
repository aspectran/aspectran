/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.undertow.shell.command;

import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.service.ShellService;
import com.aspectran.undertow.server.TowServer;

import java.net.BindException;

/**
 * Use the command 'undertow' to control the Undertow Server.
 */
public class UndertowCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "undertow";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public UndertowCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("server")
                .valueName("name")
                .withEqualSign()
                .desc("ID of bean that defined Undertow server")
                .build());
        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());

        Arguments arguments = touchArguments();
        arguments.setTitle("Available commands:");
        arguments.put("start", "Start Undertow server");
        arguments.put("stop", "Stop Undertow server");
        arguments.put("restart", "Restart Undertow server");
        arguments.put("status", "Display a brief status report");
        arguments.setRequired(true);
    }

    @Override
    public void execute(ParsedOptions options, ShellConsole console) throws Exception {
        if (!options.hasOptions() && !options.hasArgs()) {
            printQuickHelp(console);
            return;
        }
        if (options.hasOption("help")) {
            printHelp(console);
            return;
        }

        String command = null;
        if (options.hasArgs()) {
            command = options.getFirstArg();
        }

        ShellService shellService = getActiveShellService();

        String serverName = options.getValue("server", "tow.server");
        BeanRegistry beanRegistry = shellService.getActivityContext().getBeanRegistry();

        boolean justCreated = !beanRegistry.hasSingleton(TowServer.class, serverName);
        if (justCreated) {
            if ("stop".equals(command) || "restart".equals(command)) {
                console.writeError("Undertow server is not running");
                return;
            }
        }

        TowServer towServer;
        try {
            towServer = beanRegistry.getBean(TowServer.class, serverName);
        } catch (Exception e) {
            console.writeError("Undertow server is not available. Cause: " + e.getMessage());
            return;
        }

        if (command != null) {
            switch (command) {
                case "start":
                    if (!justCreated && towServer.isRunning()) {
                        console.writeError("Undertow server is already running");
                        return;
                    }
                    try {
                        if (!towServer.isAutoStart()) {
                            towServer.start();
                        }
                        printStatus(towServer, console);
                    } catch (BindException e) {
                        console.writeError("Undertow Server Error - Port already in use");
                    }
                    break;
                case "stop":
                    if (!towServer.isRunning()) {
                        console.writeError("Undertow server is not running");
                        return;
                    }
                    try {
                        towServer.stop();
                        printStatus(towServer, console);
                        beanRegistry.destroySingleton(towServer);
                    } catch (Exception e) {
                        console.writeError("Undertow Server Error - " + e.getMessage());
                    }
                    break;
                case "restart":
                    try {
                        if (towServer.isRunning()) {
                            towServer.stop();
                            beanRegistry.destroySingleton(towServer);
                            towServer = beanRegistry.getBean(TowServer.class, serverName);
                        }
                        if (!towServer.isAutoStart()) {
                            towServer.start();
                        }
                        printStatus(towServer, console);
                    } catch (BindException e) {
                        console.writeError("Undertow Server Error - Port already in use");
                    }
                    break;
                case "status":
                    printStatus(towServer, console);
                    break;
                default:
                    console.writeError("Unknown command '" + String.join(" ", options.getArgs()) + "'");
                    printQuickHelp(console);
                    break;
            }
        } else {
            printQuickHelp(console);
        }
    }

    private void printStatus(TowServer towServer, ShellConsole console) {
        console.writeLine("----------------------------------------------------------------------------");
        console.setStyle("YELLOW");
        console.write(towServer.getState());
        console.resetStyle();
        console.writeLine(" - Undertow " + towServer.getVersion());
        console.writeLine("----------------------------------------------------------------------------");
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    private static class CommandDescriptor implements Descriptor {

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public String getName() {
            return COMMAND_NAME;
        }

        @Override
        public String getDescription() {
            return "Use the command 'undertow' to control the Undertow server";
        }

        @Override
        public String getUsage() {
            return null;
        }

    }

}
