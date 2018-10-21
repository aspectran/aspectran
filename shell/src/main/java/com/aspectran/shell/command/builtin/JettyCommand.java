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
package com.aspectran.shell.command.builtin;

import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.with.jetty.JettyServer;

import java.net.BindException;
import java.util.Collection;

public class JettyCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "jetty";

    private CommandDescriptor descriptor = new CommandDescriptor();

    public JettyCommand(CommandRegistry registry) {
        super(registry);

        addOption(new Option("status", "Displays a brief status report"));
        addOption(new Option("start", "Start the Jetty Server"));
        addOption(new Option("stop", "Stops the Jetty Server"));
    }

    @Override
    public String execute(String[] args) throws Exception {
        BeanRegistry beanRegistry = getService().getActivityContext().getBeanRegistry();
        JettyServer jettyServer;
        try {
            jettyServer = beanRegistry.getBean(com.aspectran.with.jetty.JettyServer.class, "jetty.server");
        } catch (Exception e) {
            getConsole().writeLine("Jetty Server is not available. Cause: " + e.getMessage());
            return null;
        }

        ParsedOptions options = parse(args);
        if (options.hasOption("status")) {
            printStatus(jettyServer);
        } else if (options.hasOption("start")) {
            if (jettyServer.isRunning()) {
                getConsole().writeLine("Jetty Server is already running");
                return null;
            }
            try {
                jettyServer.start();
                printStatus(jettyServer);
            } catch (BindException e) {
                getConsole().writeLine("Jetty Server Error - Port already in use");
            }
        } else if (options.hasOption("stop")) {
            if (!jettyServer.isRunning()) {
                getConsole().writeLine("Jetty Server is not running");
                return null;
            }
            try {
                jettyServer.stop();
                printStatus(jettyServer);
            } catch (BindException e) {
                getConsole().writeLine("Jetty Server Error - " + e.getMessage());
            }
        } else {
            printUsage();
        }
        return null;
    }

    private void printStatus(JettyServer jettyServer) {
        getConsole().write(jettyServer.getState());
        getConsole().write(" - ");
        getConsole().write("Jetty ");
        getConsole().writeLine(JettyServer.getVersion());
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
            return "To control Jetty, try one of the following commands:";
        }

        @Override
        public String getUsage() {
            return null;
        }

        @Override
        public Collection<Option> getOptions() {
            return null;
        }

    }

}
