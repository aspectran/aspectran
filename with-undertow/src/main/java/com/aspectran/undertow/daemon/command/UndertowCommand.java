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
package com.aspectran.undertow.daemon.command;

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
import com.aspectran.undertow.server.TowServer;

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
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        DaemonService daemonService = getDaemonService();

        try {
            ClassLoader classLoader = daemonService.getActivityContext().getApplicationAdapter().getClassLoader();
            classLoader.loadClass("com.aspectran.undertow.server.TowServer");
        } catch (ClassNotFoundException e) {
            return failed("Unable to load class com.aspectran.undertow.server.TowServer " +
                    "due to missing dependency 'aspectran-with-undertow'", e);
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
                serverName = "tow.server";
            }

            BeanRegistry beanRegistry = daemonService.getActivityContext().getBeanRegistry();

            boolean justCreated = !beanRegistry.hasSingleton(TowServer.class, serverName);
            if (justCreated) {
                if ("stop".equals(mode) || "restart".equals(mode)) {
                    return failed("Undertow server is not running");
                }
            }

            TowServer towServer;
            try {
                towServer = beanRegistry.getBean(TowServer.class, serverName);
            } catch (Exception e) {
                return failed("Undertow server is not available", e);
            }

            if (mode == null) {
                return failed("'mode' parameter is not specified");
            }

            switch (mode) {
                case "start":
                    if (!justCreated && towServer.isRunning()) {
                        return failed(warn("Undertow server is already running"));
                    }
                    try {
                        if (!towServer.isAutoStart()) {
                            towServer.start();
                        }
                        return success(info(getStatus(towServer)));
                    } catch (BindException e) {
                        return failed("Undertow Server Error - Port already in use", e);
                    }
                case "stop":
                    if (!towServer.isRunning()) {
                        return failed(warn("Undertow server is not running"));
                    }
                    try {
                        towServer.stop();
                        beanRegistry.destroySingleton(towServer);
                        return success(info(getStatus(towServer)));
                    } catch (Exception e) {
                        return failed("Undertow server stop failed", e);
                    }
                case "restart":
                    try {
                        if (towServer.isRunning()) {
                            towServer.stop();
                            beanRegistry.destroySingleton(towServer);
                            towServer = beanRegistry.getBean(TowServer.class, serverName);
                        }
                        if (!towServer.isAutoStart()) {
                            towServer.start();
                        }
                        return success(info(getStatus(towServer)));
                    } catch (BindException e) {
                        return failed("Undertow Server Error - Port already in use");
                    }
                case "status":
                    return success(getStatus(towServer));
                default:
                    return failed(error("Unknown mode '" + mode + "'"));
            }
        } catch (Exception e) {
            return failed(e);
        }
    }

    private String getStatus(TowServer towServer) {
        return towServer.getState() + " - " + "Undertow " + towServer.getVersion();
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

    }

}
