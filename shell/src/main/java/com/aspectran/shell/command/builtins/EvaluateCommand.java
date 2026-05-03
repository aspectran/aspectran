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
package com.aspectran.shell.command.builtins;

import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Built-in command that evaluates an AsEL expression.
 * <p>Command name: "evaluate" (namespace: "builtins").</p>
 */
public class EvaluateCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "evaluate";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public EvaluateCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());

        Arguments arguments = touchArguments();
        arguments.put("<expression>", "The AsEL expression to evaluate");
        arguments.setRequired(false);
    }

    @Override
    public void execute(@NonNull ParsedOptions options, ShellConsole console) throws Exception {
        if (options.hasOption("help")) {
            printHelp(console);
        } else {
            String expression = String.join(" ", options.getArgs());
            if (StringUtils.isEmpty(expression)) {
                printQuickHelp(console);
                return;
            }

            try {
                ActivityContext context = getActiveShellService().getActivityContext();
                InstantActivity activity = new InstantActivity(context);
                Object result = activity.perform(() -> activity.evaluate(expression));

                if (result != null) {
                    console.writeLine(result.toString());
                    if (getActiveShellService().isVerbose()) {
                        console.writeLine("Result type: " + result.getClass().getName());
                    }
                } else {
                    console.writeLine("null");
                }
            } catch (Exception e) {
                String msg = (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                console.writeError(msg);
            }
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
            return "Evaluates an AsEL expression";
        }

        @Override
        @Nullable
        public String getUsage() {
            return null;
        }

    }

}
