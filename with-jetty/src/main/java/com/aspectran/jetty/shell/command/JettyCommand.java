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
package com.aspectran.jetty.shell.command;

import com.aspectran.core.component.bean.BeanException;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.jetty.server.JettyServer;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.lifecycle.LifeCycle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
                .desc("The bean ID of the Jetty server to control")
                .build());
        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());

        Arguments arguments = touchArguments();
        arguments.setTitle("Available commands:");
        arguments.put("start", "Starts the Jetty server");
        arguments.put("stop", "Stops the Jetty server");
        arguments.put("restart", "Restarts the Jetty server");
        arguments.put("status", "Displays the status of the Jetty server");
        arguments.setRequired(true);
    }

    @Override
    public void execute(@NonNull ParsedOptions options, ShellConsole console) throws Exception {
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
        String serverName = options.getValue("server", "jetty.server");

        if (command != null) {
            switch (command) {
                case "start":
                    startJettyServer(serverName, console);
                    break;
                case "stop":
                    stopJettyServer(serverName, console);
                    break;
                case "restart":
                    if (stopJettyServer(serverName, console)) {
                        startJettyServer(serverName, console);
                    }
                    break;
                case "status":
                    printServerStatus(serverName, console);
                    break;
                default:
                    console.writeError("Invalid command: " + String.join(" ", options.getArgs()));
                    printQuickHelp(console);
                    break;
            }
        } else {
            printQuickHelp(console);
        }
    }

    private void startJettyServer(String serverName, ShellConsole console) throws Exception {
        JettyServer jettyServer = null;
        try {
            if (hasJettyServer(serverName)) {
                jettyServer = getJettyServer(serverName);
                if (jettyServer.isRunning()) {
                    console.writeError("The Jetty server is already running.");
                } else {
                    jettyServer.start();
                    printStatus(jettyServer.getState(), console);
                }
            } else {
                jettyServer = getJettyServer(serverName);
                if (!jettyServer.isRunning()) {
                    jettyServer.start();
                }
                printStatus(jettyServer.getState(), console);
            }
        } catch (BeanException e) {
            console.writeError("The Jetty server bean '" + serverName + "' could not be found.");
        } catch (Exception e) {
            if (jettyServer != null) {
                destroyJettyServer(jettyServer);
            }
            Throwable cause = ExceptionUtils.getRootCause(e);
            if (cause instanceof BindException) {
                console.writeError("Failed to start the Jetty server: Port is already in use.");
            } else {
                console.writeError("Failed to start the Jetty server: " + e);
            }
        }
    }

    private boolean stopJettyServer(String serverName, ShellConsole console) {
        boolean success = false;
        try {
            if (hasJettyServer(serverName)) {
                JettyServer jettyServer = getJettyServer(serverName);
                jettyServer.stop();
                destroyJettyServer(jettyServer);
                printStatus(LifeCycle.STOPPED, console);
                success = true;
            } else {
                console.writeError("The Jetty server is not running.");
            }
        } catch (BeanException e) {
            console.writeError("The Jetty server bean '" + serverName + "' could not be found.");
        } catch (Exception e) {
            console.writeError("Failed to stop the Jetty server: " + e);
        }
        return success;
    }

    private void printServerStatus(String serverName, ShellConsole console) {
        try {
            if (hasJettyServer(serverName)) {
                JettyServer jettyServer = getJettyServer(serverName);
                if (jettyServer.isStarted()) {
                    printStatus(LifeCycle.RUNNING, console);
                } else {
                    printStatus(jettyServer.getState(), console);
                }
            } else {
                printStatus(LifeCycle.STOPPED, console);
            }
        } catch (BeanException e) {
            console.writeError("The Jetty server bean '" + serverName + "' could not be found.");
        } catch (Exception e) {
            console.writeError("Failed to get the status of the Jetty server: " + e);
        }
    }

    private void printStatus(@NonNull String status, @NonNull ShellConsole console) {
        console.writeLine("----------------------------------------------------------------------------");
        console.getStyler().setStyle("YELLOW");
        console.write(status);
        console.getStyler().resetStyle();
        console.writeLine(" - Jetty " + JettyServer.getVersion());
        console.writeLine("----------------------------------------------------------------------------");
    }

    private JettyServer getJettyServer(String serverName) {
        BeanRegistry beanRegistry = getActiveShellService().getActivityContext().getBeanRegistry();
        return beanRegistry.getBean(JettyServer.class, serverName);
    }

    private boolean hasJettyServer(String serverName) {
        BeanRegistry beanRegistry = getActiveShellService().getActivityContext().getBeanRegistry();
        return beanRegistry.hasSingleton(JettyServer.class, serverName);
    }

    private void destroyJettyServer(JettyServer jettyServer) throws Exception {
        BeanRegistry beanRegistry = getActiveShellService().getActivityContext().getBeanRegistry();
        beanRegistry.destroySingleton(jettyServer);
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
        @NonNull
        public String getDescription() {
            return "Controls the embedded Jetty server";
        }

        @Override
        @Nullable
        public String getUsage() {
            return null;
        }

    }

}
