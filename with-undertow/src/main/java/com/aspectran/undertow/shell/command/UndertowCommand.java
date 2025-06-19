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
package com.aspectran.undertow.shell.command;

import com.aspectran.core.component.bean.BeanException;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.undertow.server.TowServer;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.lifecycle.LifeCycle;

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
        String serverName = options.getValue("server", "tow.server");

        if (command != null) {
            switch (command) {
                case "start":
                    startTowServer(serverName, console);
                    break;
                case "stop":
                    stopTowServer(serverName, console);
                    break;
                case "restart":
                    if (stopTowServer(serverName, console)) {
                        startTowServer(serverName, console);
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

    private void startTowServer(String serverName, ShellConsole console) throws Exception {
        TowServer towServer = null;
        try {
            if (hasTowServer(serverName)) {
                towServer = getTowServer(serverName);
                if (towServer.isRunning()) {
                    console.writeError("Undertow server is already running");
                } else {
                    towServer.start();
                    printStatus(towServer.getState(), console);
                }
            } else {
                towServer = getTowServer(serverName);
                if (!towServer.isRunning()) {
                    towServer.start();
                }
                printStatus(towServer.getState(), console);
            }
        } catch (BeanException e) {
            console.writeError("Undertow server is not available. Cause: " + e);
        } catch (Exception e) {
            if (towServer != null) {
                destroyTowServer(towServer);
            }
            Throwable cause = ExceptionUtils.getRootCause(e);
            if (cause instanceof BindException) {
                console.writeError("Undertow server failed to start. Cause: Port already in use");
            } else {
                console.writeError(e.toString());
            }
        }
    }

    private boolean stopTowServer(String serverName, ShellConsole console) {
        boolean success = false;
        try {
            if (hasTowServer(serverName)) {
                TowServer towServer = getTowServer(serverName);
                towServer.stop();
                destroyTowServer(towServer);
                printStatus(LifeCycle.STOPPED, console);
                success = true;
            } else {
                console.writeError("Undertow server is not running");
            }
        } catch (BeanException e) {
            console.writeError("Undertow server is not available. Cause: " + e);
        } catch (Exception e) {
            console.writeError(e.toString());
        }
        return success;
    }

    private void printServerStatus(String serverName, ShellConsole console) {
        try {
            if (hasTowServer(serverName)) {
                TowServer towServer = getTowServer(serverName);
                if (towServer.isStarted()) {
                    printStatus(LifeCycle.RUNNING, console);
                } else {
                    printStatus(towServer.getState(), console);
                }
            } else {
                printStatus(LifeCycle.STOPPED, console);
            }
        } catch (BeanException e) {
            console.writeError("Undertow server is not available. Cause: " + e);
        } catch (Exception e) {
            console.writeError(e.toString());
        }
    }

    private void printStatus(String status, @NonNull ShellConsole console) {
        console.writeLine("----------------------------------------------------------------------------");
        console.setStyle("YELLOW");
        console.write(status);
        console.resetStyle();
        console.writeLine(" - Undertow " + TowServer.getVersion());
        console.writeLine("----------------------------------------------------------------------------");
    }

    private TowServer getTowServer(String serverName) {
        BeanRegistry beanRegistry = getActiveShellService().getActivityContext().getBeanRegistry();
        return beanRegistry.getBean(TowServer.class, serverName);
    }

    private boolean hasTowServer(String serverName) {
        BeanRegistry beanRegistry = getActiveShellService().getActivityContext().getBeanRegistry();
        return beanRegistry.hasSingleton(TowServer.class, serverName);
    }

    private void destroyTowServer(TowServer towServer) throws Exception {
        BeanRegistry beanRegistry = getActiveShellService().getActivityContext().getBeanRegistry();
        beanRegistry.destroySingleton(towServer);
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
            return "Use the command 'undertow' to control the Undertow server";
        }

        @Override
        @Nullable
        public String getUsage() {
            return null;
        }

    }

}
