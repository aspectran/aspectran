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
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.context.expr.ItemEvaluation;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.util.StringUtils;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.daemon.service.DaemonService;
import com.aspectran.jetty.JettyServer;

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
        DaemonService daemonService = getDaemonService();

        try {
            ClassLoader classLoader = daemonService.getActivityContext().getApplicationAdapter().getClassLoader();
            classLoader.loadClass("com.aspectran.jetty.JettyServer");
        } catch (ClassNotFoundException e) {
            return failed("Unable to load class com.aspectran.jetty.JettyServer " +
                    "due to missing dependency 'aspectran-with-jetty'", e);
        }

        try {
            String mode = null;
            String serverName = null;

            ItemRuleMap parameterItemRuleMap = parameters.getParameterItemRuleMap();
            if ((parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty())) {
                ItemEvaluator evaluator = new ItemEvaluation(daemonService.getDefaultActivity());
                ParameterMap parameterMap = evaluator.evaluateAsParameterMap(parameterItemRuleMap);
                mode = parameterMap.getParameter("mode");
                serverName = parameterMap.getParameter("server");
            }

            if (!StringUtils.hasLength(serverName)) {
                serverName = "jetty.server";
            }

            BeanRegistry beanRegistry = daemonService.getActivityContext().getBeanRegistry();

            boolean justCreated = !beanRegistry.hasSingleton(JettyServer.class, serverName);
            if (justCreated) {
                if ("stop".equals(mode) || "restart".equals(mode)) {
                    return failed("Jetty server is not running");
                }
            }

            JettyServer jettyServer;
            try {
                jettyServer = beanRegistry.getBean(JettyServer.class, serverName);
            } catch (Exception e) {
                return failed("Jetty server is not available", e);
            }

            if (mode == null) {
                return failed("'mode' parameter is not specified");
            }

            switch (mode) {
                case "start":
                    if (!justCreated && jettyServer.isRunning()) {
                        return failed(warn("Jetty server is already running"));
                    }
                    try {
                        jettyServer.start();
                        return success(info(getStatus(jettyServer)));
                    } catch (BindException e) {
                        return failed("Jetty Server Error - Port already in use", e);
                    }
                case "restart":
                    try {
                        if (jettyServer.isRunning()) {
                            jettyServer.stop();
                        }
                        jettyServer.start();
                        return success(info(getStatus(jettyServer)));
                    } catch (BindException e) {
                        return failed("Jetty Server Error - Port already in use");
                    }
                case "stop":
                    if (!jettyServer.isRunning()) {
                        return failed(warn("Jetty server is not running"));
                    }
                    try {
                        jettyServer.stop();
                        return success(info(getStatus(jettyServer)));
                    } catch (Exception e) {
                        return failed("Jetty server stop failed", e);
                    }
                case "status":
                    return success(getStatus(jettyServer));
                default:
                    return failed(error("Unknown mode '" + mode + "'"));
            }
        } catch (Exception e) {
            return failed(e);
        }
    }

    private String getStatus(JettyServer jettyServer) {
        return jettyServer.getState() + " - " + "Jetty " + JettyServer.getVersion();
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
