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
package com.aspectran.daemon.command.builtins;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.daemon.service.DaemonService;
import com.aspectran.utils.OutputStringWriter;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Writer;
import java.util.Map;

/**
 * Built-in command that executes a translet and returns its response output if any.
 * <p>Command name: "translet" (namespace: "builtins").</p>
 */
public class TransletCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "translet";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public TransletCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public CommandResult execute(@NonNull CommandParameters parameters) {
        DaemonService daemonService = getDaemonService();

        String transletName = parameters.getTransletName();
        if (transletName == null) {
            return failed(error("The 'translet' parameter is required to specify the translet to execute."));
        }

        try {
            Map<String, Object> attributeMap = parameters.getAttributeMap();
            ParameterMap parameterMap = parameters.getParameterMap();
            Translet translet = daemonService.translate(transletName, attributeMap, parameterMap);
            Writer writer = translet.getResponseAdapter().getWriter();
            if (writer instanceof OutputStringWriter stringWriter && !stringWriter.isDirty()) {
                return success(null);
            } else {
                return success(writer.toString());
            }
        } catch (Exception e) {
            return failed(e);
        }
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
            return "Executes a translet and displays its response";
        }

    }

}
