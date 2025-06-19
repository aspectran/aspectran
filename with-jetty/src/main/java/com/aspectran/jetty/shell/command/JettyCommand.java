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
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.lifecycle.LifeCycle;

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
        arguments.setTitle("Available commands:");
        arguments.put("start", "Start Jetty server");
        arguments.put("stop", "Stop Jetty server");
        arguments.put("restart", "Restart Jetty server");
        arguments.put("status", "Display a brief status report");
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
                    console.writeError("Unknown command '" + String.join(" ", options.getArgs()) + "'");
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
                    console.writeError("Jetty server is already running");
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
            console.writeError("Jetty server is not available. Cause: " + e);
        } catch (Exception e) {
            if (jettyServer != null) {
                destroyJettyServer(jettyServer);
            }
            Throwable cause = ExceptionUtils.getRootCause(e);
            if (cause instanceof BindException) {
                console.writeError("Jetty server failed to start. Cause: Port already in use");
            } else {
                console.writeError(e.toString());
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
                console.writeError("Jetty server is not running");
            }
        } catch (BeanException e) {
            console.writeError("Jetty server is not available. Cause: " + e);
        } catch (Exception e) {
            console.writeError(e.toString());
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
            console.writeError("Jetty server is not available. Cause: " + e);
        } catch (Exception e) {
            console.writeError(e.toString());
        }
    }

    private void printStatus(@NonNull String status, @NonNull ShellConsole console) {
        console.writeLine("----------------------------------------------------------------------------");
        console.setStyle("YELLOW");
        console.write(status);
        console.resetStyle();
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
            return "Use the command 'jetty' to control the Jetty server";
        }

        @Override
        @Nullable
        public String getUsage() {
            return null;
        }

    }

}
