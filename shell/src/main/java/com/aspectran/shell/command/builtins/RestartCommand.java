package com.aspectran.shell.command.builtins;

import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.Command;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.service.ShellAspectranService;

import java.util.Map;

public class RestartCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "restart";

    public RestartCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public String execute(String[] args) throws Exception {
        getService().restart();
        return null;
    }

    @Override
    public Descriptor getDescriptor() {
        return new Command.Descriptor() {

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
                return "Restart Aspectran Shell to reload all resources";
            }

            @Override
            public String getUsage() {
                return "Type 'restart'";
            }

            @Override
            public Map<String, String> getOptions() {
                return null;
            }

        };
    }

}
