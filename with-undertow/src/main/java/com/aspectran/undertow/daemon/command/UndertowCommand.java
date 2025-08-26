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
package com.aspectran.undertow.daemon.command;

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.component.bean.BeanException;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.context.asel.item.ItemEvaluator;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.undertow.server.TowServer;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.lifecycle.LifeCycle;

import java.net.BindException;

/**
 * A command for the Aspectran Daemon to control an embedded Undertow server.
 * <p>This command allows for starting, stopping, restarting, and checking the status
 * of a {@link TowServer} bean defined in the application context.</p>
 */
public class UndertowCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "undertow";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public UndertowCommand(CommandRegistry registry) {
        super(registry);
    }

    /**
     * Executes the undertow command based on the provided parameters.
     * The required 'mode' parameter can be 'start', 'stop', 'restart', or 'status'.
     * @param parameters the command parameters
     * @return the result of the command execution
     */
    @Override
    public CommandResult execute(CommandParameters parameters) {
        try {
            String mode = null;
            String serverName = null;
            ItemRuleMap parameterItemRuleMap = parameters.getParameterItemRuleMap();
            if ((parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty())) {
                ItemEvaluator evaluator = getDaemonService().getDefaultActivity().getItemEvaluator();
                ParameterMap parameterMap = evaluator.evaluateAsParameterMap(parameterItemRuleMap);
                mode = parameterMap.getParameter("mode");
                serverName = parameterMap.getParameter("server");
            }
            if (mode == null) {
                return failed("'mode' parameter is not specified");
            }
            if (!StringUtils.hasLength(serverName)) {
                serverName = "tow.server";
            }

            switch (mode) {
                case "start":
                    return startTowServer(serverName);
                case "stop":
                    return stopTowServer(serverName);
                case "restart":
                    CommandResult commandResult = stopTowServer(serverName);
                    if (commandResult.isSuccess()) {
                        commandResult = startTowServer(serverName);
                    }
                    return commandResult;
                case "status":
                    return printServerStatus(serverName);
                default:
                    return failed(error("Unknown mode '" + mode + "'"));
            }
        } catch (Exception e) {
            return failed(e);
        }
    }

    /**
     * Starts the specified Undertow server.
     * @param serverName the name of the TowServer bean
     * @return the command result
     * @throws Exception if an error occurs during server startup
     */
    private CommandResult startTowServer(String serverName) throws Exception {
        TowServer towServer = null;
        try {
            if (hasTowServer(serverName)) {
                towServer = getTowServer(serverName);
                if (towServer.isRunning()) {
                    return failed(warn("Undertow server is already running"));
                } else {
                    towServer.start();
                    return success(info(getStatus(towServer.getState())));
                }
            } else {
                towServer = getTowServer(serverName);
                if (!towServer.isRunning()) {
                    towServer.start();
                }
                return success(info(getStatus(towServer.getState())));
            }
        } catch (Exception e) {
            if (towServer != null) {
                destroyTowServer(towServer);
            }
            Throwable cause = ExceptionUtils.getRootCause(e);
            if (cause instanceof BindException) {
                return failed("Undertow server failed to start. Cause: Port already in use", e);
            } else {
                return failed(e);
            }
        }
    }

    /**
     * Stops the specified Undertow server.
     * @param serverName the name of the TowServer bean
     * @return the command result
     */
    private CommandResult stopTowServer(String serverName) {
        try {
            if (hasTowServer(serverName)) {
                TowServer towServer = getTowServer(serverName);
                towServer.stop();
                destroyTowServer(towServer);
                return success(info(getStatus(LifeCycle.STOPPED)));
            } else {
                return failed(warn("Undertow server is not running"));
            }
        } catch (Exception e) {
            return failed(e);
        }
    }

    /**
     * Prints the current status of the specified Undertow server.
     * @param serverName the name of the TowServer bean
     * @return the command result containing the server status
     */
    private CommandResult printServerStatus(String serverName) {
        try {
            if (hasTowServer(serverName)) {
                TowServer towServer = getTowServer(serverName);
                if (towServer.isStarted()) {
                    return success(info(getStatus(LifeCycle.RUNNING)));
                } else {
                    return success(info(getStatus(towServer.getState())));
                }
            } else {
                return success(info(getStatus(LifeCycle.STOPPED)));
            }
        } catch (BeanException e) {
            return failed("Undertow server is not available", e);
        } catch (Exception e) {
            return failed(e);
        }
    }

    @NonNull
    private String getStatus(String status) {
        return status + " - " + "Undertow " + TowServer.getVersion();
    }

    private TowServer getTowServer(String serverName) {
        BeanRegistry beanRegistry = getDaemonService().getActivityContext().getBeanRegistry();
        return beanRegistry.getBean(TowServer.class, serverName);
    }

    private boolean hasTowServer(String serverName) {
        BeanRegistry beanRegistry = getDaemonService().getActivityContext().getBeanRegistry();
        return beanRegistry.hasSingleton(TowServer.class, serverName);
    }

    private void destroyTowServer(TowServer towServer) throws Exception {
        BeanRegistry beanRegistry = getDaemonService().getActivityContext().getBeanRegistry();
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

    }

}
