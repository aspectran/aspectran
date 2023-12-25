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
package com.aspectran.jetty.daemon.command;

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.component.bean.BeanException;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.context.expr.ItemEvaluation;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.jetty.JettyServer;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
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
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        try {
            String mode = null;
            String serverName = null;
            ItemRuleMap parameterItemRuleMap = parameters.getParameterItemRuleMap();
            if ((parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty())) {
                ItemEvaluator evaluator = new ItemEvaluation(getDaemonService().getDefaultActivity());
                ParameterMap parameterMap = evaluator.evaluateAsParameterMap(parameterItemRuleMap);
                mode = parameterMap.getParameter("mode");
                serverName = parameterMap.getParameter("server");
            }
            if (mode == null) {
                return failed("'mode' parameter is not specified");
            }
            if (!StringUtils.hasLength(serverName)) {
                serverName = "jetty.server";
            }

            switch (mode) {
                case "start":
                    return startJettyServer(serverName);
                case "stop":
                    return stopJettyServer(serverName);
                case "restart":
                    CommandResult commandResult = stopJettyServer(serverName);
                    if (commandResult.isSuccess()) {
                        commandResult = startJettyServer(serverName);
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

    private CommandResult startJettyServer(String serverName) throws Exception {
        JettyServer jettyServer = null;
        try {
            if (hasJettyServer(serverName)) {
                jettyServer = getJettyServer(serverName);
                if (jettyServer.isRunning()) {
                    return failed(warn("Jetty server is already running"));
                } else {
                    jettyServer.start();
                    return success(info(getStatus(jettyServer.getState())));
                }
            } else {
                jettyServer = getJettyServer(serverName);
                if (!jettyServer.isRunning()) {
                    jettyServer.start();
                }
                return success(info(getStatus(jettyServer.getState())));
            }
        } catch (Exception e) {
            if (jettyServer != null) {
                destroyTowServer(jettyServer);
            }
            Throwable cause = ExceptionUtils.getRootCause(e);
            if (cause instanceof BindException) {
                return failed("Jetty server failed to start. Cause: Port already in use", e);
            } else {
                return failed(e);
            }
        }
    }

    private CommandResult stopJettyServer(String serverName) {
        try {
            if (hasJettyServer(serverName)) {
                JettyServer jettyServer = getJettyServer(serverName);
                destroyTowServer(jettyServer);
                return success(info(getStatus(LifeCycle.STOPPED)));
            } else {
                return failed(warn("Jetty server is not running"));
            }
        } catch (Exception e) {
            return failed(e);
        }
    }

    private CommandResult printServerStatus(String serverName) {
        try {
            if (hasJettyServer(serverName)) {
                JettyServer jettyServer = getJettyServer(serverName);
                if (jettyServer.isStarted()) {
                    return success(info(getStatus(LifeCycle.RUNNING)));
                } else {
                    return success(info(getStatus(jettyServer.getState())));
                }
            } else {
                return success(info(getStatus(LifeCycle.STOPPED)));
            }
        } catch (BeanException e) {
            return failed("Jetty server is not available", e);
        } catch (Exception e) {
            return failed(e);
        }
    }

    private String getStatus(String status) {
        return status + " - " + "Jetty " + JettyServer.getVersion();
    }

    private JettyServer getJettyServer(String serverName) {
        BeanRegistry beanRegistry = getDaemonService().getActivityContext().getBeanRegistry();
        return beanRegistry.getBean(JettyServer.class, serverName);
    }

    private boolean hasJettyServer(String serverName) {
        BeanRegistry beanRegistry = getDaemonService().getActivityContext().getBeanRegistry();
        return beanRegistry.hasSingleton(JettyServer.class, serverName);
    }

    private void destroyTowServer(JettyServer jettyServer) throws Exception {
        BeanRegistry beanRegistry = getDaemonService().getActivityContext().getBeanRegistry();
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
        public String getDescription() {
            return "Use the command 'jetty' to control the Jetty server";
        }

    }

}
