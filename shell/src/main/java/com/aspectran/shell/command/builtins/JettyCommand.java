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
import com.aspectran.with.jetty.JettyServer;

import java.net.BindException;

/**
 * Use the command 'jetty' to control the Jetty Server.
 */
public class JettyCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "jetty";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public JettyCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("server")
                .valueName("name")
                .withEqualSign()
                .desc("ID of bean that defined Jetty server")
                .build());
        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());

        Arguments arguments = touchArguments();
        arguments.setTitle("Commands:");
        arguments.put("start", "Start Jetty server");
        arguments.put("stop", "Stop Jetty server");
        arguments.put("restart", "Restart Jetty server");
        arguments.put("status", "Display a brief status report");
        arguments.setRequired(true);
    }

    @Override
    public void execute(ParsedOptions options, Console console) throws Exception {
        ShellService service = getService();

        String serverName = options.getValue("server", "jetty.server");
        BeanRegistry beanRegistry = service.getActivityContext().getBeanRegistry();
        JettyServer jettyServer;
        try {
            jettyServer = beanRegistry.getBean(com.aspectran.with.jetty.JettyServer.class, serverName);
        } catch (Exception e) {
            console.writeError("Jetty server is not available. Cause: " + e.getMessage());
            return;
        }

        String command = null;
        if (options.hasArgs()) {
            String[] args = options.getArgs();
            if (args.length > 0) {
                command = args[0];
            }
            if ("start".equals(command)) {
                if (jettyServer.isRunning()) {
                    console.writeError("Jetty server is already running");
                    return;
                }
                try {
                    jettyServer.start();
                    printStatus(jettyServer, console);
                } catch (BindException e) {
                    console.writeError("Jetty Server Error - Port already in use");
                }
            } else if ("stop".equals(command)) {
                if (!jettyServer.isRunning()) {
                    console.writeError("Jetty Server is not running");
                    return;
                }
                try {
                    jettyServer.stop();
                    printStatus(jettyServer, console);
                } catch (BindException e) {
                    console.writeError("Jetty Server Error - " + e.getMessage());
                }
            } else if ("restart".equals(command)) {
                try {
                    if (jettyServer.isRunning()) {
                        jettyServer.stop();
                    }
                    jettyServer.start();
                    printStatus(jettyServer, console);
                } catch (BindException e) {
                    console.writeError("Jetty Server Error - Port already in use");
                }
            } else if ("status".equals(command)) {
                printStatus(jettyServer, console);
            } else {
                console.writeError("Unknown command '" + String.join(" ", args) + "'");
                printQuickHelp(console);
            }
        } else {
            printQuickHelp(console);
        }
    }

    private void printStatus(JettyServer jettyServer, Console console) {
        console.setStyle("YELLOW");
        console.write(jettyServer.getState());
        console.styleOff();
        console.writeLine(" - Jetty " + JettyServer.getVersion());
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    private class CommandDescriptor implements Descriptor {

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
            return "Use the command 'jetty' to control the Jetty server";
        }

        @Override
        public String getUsage() {
            return null;
        }

    }

}
