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
package com.aspectran.shell.command.builtins;

import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.service.ShellService;
import com.aspectran.undertow.server.TowServer;
import io.undertow.Version;

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
        arguments.setTitle("Commands:");
        arguments.put("start", "Start Undertow server");
        arguments.put("stop", "Stop Undertow server");
        arguments.put("restart", "Restart Undertow server");
        arguments.put("status", "Display a brief status report");
        arguments.setRequired(true);
    }

    @Override
    public void execute(ParsedOptions options, Console console) throws Exception {
        if (!options.hasOptions() && !options.hasArgs()) {
            printQuickHelp(console);
            return;
        }
        if (options.hasOption("help")) {
            printHelp(console);
            return;
        }

        ShellService service = getService();

        String serverName = options.getValue("server", "tow.server");
        BeanRegistry beanRegistry = service.getActivityContext().getBeanRegistry();
        TowServer towServer;
        try {
            towServer = beanRegistry.getBean(TowServer.class, serverName);
        } catch (Exception e) {
            console.writeError("Undertow server is not available. Cause: " + e.getMessage());
            return;
        }

        if (options.hasArgs()) {
            String command = options.getFirstArg();
            if ("start".equals(command)) {
                if (towServer.isStarted()) {
                    console.writeError("Undertow server is already running");
                    return;
                }
                try {
                    towServer.start();
                    printStatus(towServer, console);
                } catch (BindException e) {
                    console.writeError("Undertow Server Error - Port already in use");
                }
            } else if ("stop".equals(command)) {
                if (!towServer.isStarted()) {
                    console.writeError("Undertow server is not running");
                    return;
                }
                try {
                    towServer.stop();
                    printStatus(towServer, console);
                } catch (Exception e) {
                    console.writeError("Undertow Server Error - " + e.getMessage());
                }
            } else if ("restart".equals(command)) {
                try {
                    if (towServer.isStarted()) {
                        towServer.stop();
                    }
                    towServer.start();
                    printStatus(towServer, console);
                } catch (BindException e) {
                    console.writeError("Undertow Server Error - Port already in use");
                }
            } else if ("status".equals(command)) {
                printStatus(towServer, console);
            } else {
                console.writeError("Unknown command '" + String.join(" ", options.getArgs()) + "'");
                printQuickHelp(console);
            }
        } else {
            printQuickHelp(console);
        }
    }

    private void printStatus(TowServer towServer, Console console) {
        console.writeLine("----------------------------------------------------------------------------");
        console.setStyle("YELLOW");
        console.write(towServer.isStarted() ? "STARTED" : "STOPPED");
        console.styleOff();
        console.writeLine(" - Undertow " + Version.getVersionString());
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
