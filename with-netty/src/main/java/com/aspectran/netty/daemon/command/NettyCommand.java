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
package com.aspectran.netty.daemon.command;

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.component.bean.BeanException;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.context.asel.item.ItemEvaluator;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.netty.server.NettyServer;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.NonNull;

import java.net.BindException;

/**
 * A command for the Aspectran Daemon to control an embedded Netty server.
 * <p>Supports starting, stopping, restarting, and querying the status of a {@link NettyServer} bean.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "netty";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    /**
     * Instantiates a new {@code NettyCommand}.
     * @param registry the command registry
     */
    public NettyCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        try {
            String mode = null;
            String serverName = null;
            ItemRuleMap parameterItemRuleMap = parameters.getParameterItemRuleMap();
            if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
                ItemEvaluator evaluator = getDaemonService().getDefaultActivity().getItemEvaluator();
                ParameterMap parameterMap = evaluator.evaluateAsParameterMap(parameterItemRuleMap);
                mode = parameterMap.getParameter("mode");
                serverName = parameterMap.getParameter("server");
            }
            if (mode == null) {
                return failed("'mode' parameter is not specified");
            }
            if (!StringUtils.hasLength(serverName)) {
                serverName = "netty.server";
            }

            switch (mode) {
                case "start":
                    return startNettyServer(serverName);
                case "stop":
                    return stopNettyServer(serverName);
                case "restart":
                    CommandResult commandResult = stopNettyServer(serverName);
                    if (commandResult.isSuccess()) {
                        commandResult = startNettyServer(serverName);
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

    private CommandResult startNettyServer(String serverName) throws Exception {
        NettyServer nettyServer = null;
        try {
            if (hasNettyServer(serverName)) {
                nettyServer = getNettyServer(serverName);
                if (nettyServer.isRunning()) {
                    return failed(warn("Netty server is already running"));
                } else {
                    nettyServer.start();
                    return success(info(getStatus(nettyServer.getState().toString())));
                }
            } else {
                nettyServer = getNettyServer(serverName);
                if (!nettyServer.isRunning()) {
                    nettyServer.start();
                }
                return success(info(getStatus(nettyServer.getState().toString())));
            }
        } catch (Exception e) {
            if (nettyServer != null) {
                try {
                    nettyServer.stop();
                } catch (Exception ex) {
                    // ignore
                }
            }
            Throwable cause = ExceptionUtils.getRootCause(e);
            if (cause instanceof BindException) {
                return failed(error(cause.getMessage()));
            } else {
                throw e;
            }
        }
    }

    private CommandResult stopNettyServer(String serverName) throws Exception {
        if (!hasNettyServer(serverName)) {
            return failed(warn("Netty server is not running"));
        }
        NettyServer nettyServer = getNettyServer(serverName);
        if (!nettyServer.isRunning()) {
            return failed(warn("Netty server is not running"));
        }
        nettyServer.stop();
        return success(info(getStatus(nettyServer.getState().toString())));
    }

    private CommandResult printServerStatus(String serverName) {
        if (!hasNettyServer(serverName)) {
            return success(info("Netty server is not available"));
        }
        NettyServer nettyServer = getNettyServer(serverName);
        return success(info(getStatus(nettyServer.getState().toString())));
    }

    private boolean hasNettyServer(String serverName) {
        BeanRegistry beanRegistry = getDaemonService().getActivityContext().getBeanRegistry();
        return beanRegistry.hasSingleton(NettyServer.class, serverName);
    }

    @NonNull
    private NettyServer getNettyServer(String serverName) {
        BeanRegistry beanRegistry = getDaemonService().getActivityContext().getBeanRegistry();
        NettyServer nettyServer = beanRegistry.getBean(NettyServer.class, serverName);
        if (nettyServer == null) {
            throw new BeanException("No NettyServer bean found named '" + serverName + "'");
        }
        return nettyServer;
    }

    @NonNull
    private String getStatus(String status) {
        return status + " - " + "Netty " + NettyServer.getVersion();
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
            return "Use the command 'netty' to control the Netty server";
        }

    }

}
